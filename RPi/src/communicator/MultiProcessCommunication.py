from src.communicator.Android_com import Android
from src.communicator.Arduino_com import Arduino
from src.communicator.Algorithm_com import Algorithm
from src.config import STOPPING_IMAGE, IMAGE_WIDTH, IMAGE_HEIGHT, IMAGE_FORMAT
from src.protocols import *

from PIL import Image
import numpy as np
import imagezmq
from picamera import PiCamera
from picamera.array import PiRGBArray


import collections
from multiprocessing import Process, Value
from multiprocessing.managers import BaseManager

img_count = 0
class DequeProxy(object):
    def __init__(self, *args):
        self.deque = collections.deque(*args)
    def __len__(self):
        return self.deque.__len__()
    def append(self, x):
        self.deque.append(x)
    def appendleft(self, x):
        self.deque.appendleft(x)
    def popleft(self):
        return self.deque.popleft()
    def empty(self):
        if self.deque:
            return False
        else:
            return True

class DequeManager(BaseManager):
    pass
    

        
DequeManager.register('DequeProxy', DequeProxy,
                      exposed=['__len__', 'append', 'appendleft', 'popleft', 'empty'])   

class MultiProcessCommunicator:
    """
    This class will carry out multi-processing communication process between Algorithm, Android and Arduino.
    """
    def __init__(self, image_processing_server_url: str=None):
        """
        Start the multiprocessing process and set up all the relevant variables

        Upon starting, RPi will connect to individual devices in the following order
        - Algorithm
        - Arduino
        - Android

        The multiprocessing queue will also be instantiated
        """
        print('Starting Multiprocessing Communication')

        self.algorithm = Algorithm_communicator()  # handles connection to Algorithm
        self.arduino = Arduino_communicator()  # handles connection to Arduino
        self.android = Android_communicator()  # handles connection to Android
        
        self.manager = DequeManager()
        self.manager.start()

        # messages from Arduino, Algorithm and Android are placed in this queue before being read
        self.message_deque = self.manager.DequeProxy()
        self.to_android_message_deque = self.manager.DequeProxy()

        self.read_arduino_process = Process(target=self._read_arduino)
        self.read_algorithm_process = Process(target=self._read_algorithm)
        self.read_android_process = Process(target=self._read_android)
        
        self.write_process = Process(target=self._write_target)
        self.write_android_process = Process(target=self._write_android)
        
        
        # the current action / status of the robot
        self.status = Status.IDLE  # robot starts off being idle

        self.dropped_connection = Value('i',0) # 0 - arduino, 1 - algorithm

        # for image recognition
        self.image_process = None
        self.image_deque = None
        
        if image_processing_server_url is not None:
            print('------Start Exploration with image recognition-----')
            self.image_process = Process(target=self._process_pic)
            # pictures taken using the PiCamera are placed in this queue
            self.image_deque = self.manager.DequeProxy()

            self.image_processing_server_url = image_processing_server_url
            self.image_count = Value('i',0)
            print("self.image_deque:", self.image_deque)


        
        
    def start(self):        
        try:
            self.algorithm.connect_arduino()
            self.arduino.connect_arduino()
            self.android.connect_arduino()

            print('Connected to Algorithm, Arduino and Android')

            self.read_algorithm_process.start_algo()
            self.read_arduino_process.start_arduino()
            self.read_android_process.start_android()
            self.write_process.start()
            self.write_android_process.start()

            if self.image_process is not None:
                self.image_process.start()
                print('All processes have started : read-arduino, read-algorithm, read-android, write-android, image_processing')
            else:
                print('All processes have started : read-arduino, read-algorithm, read-android, write-android')

            print('Multiprocess communication session started')
            
        except Exception as error:
            raise error

        self._allow_reconnection()

    def end(self):

        self.algorithm.disconnect_all_algo()
        self.android.disconnect_all()
        print('Multiprocess communication has just stopped')

        

    def _allow_reconnection(self):
        print('RPi can reconnect now')

        while True:
            try:
                if not self.read_algorithm_process.is_alive():
                    self._reconnect_algorithm()

                if not self.read_arduino_process.is_alive():
                    self._reconnect_arduino()
                    
                if not self.read_android_process.is_alive():
                    self._reconnect_android()
                    
                if not self.write_process.is_alive():
                    if self.dropped_connection.value == 0:
                        self._reconnect_arduino()
                    elif self.dropped_connection.value == 1:
                        self._reconnect_algorithm()
                        
                if not self.write_android_process.is_alive():
                    self._reconnect_android()
                
                if self.image_process is not None and not self.image_process.is_alive():
                   self.image_process.terminate()
                    
            except Exception as error:
                print("Error during reconnection: ",error)
                raise error

    def _reconnect_algorithm(self):
        self.algorithm.disconnect()

        self.read_algorithm_process.terminate()
        self.write_process.terminate()
        self.write_android_process.terminate()

        self.algorithm.connect()

        self.read_algorithm_process = Process(target=self._read_algorithm)
        self.read_algorithm_process.start()

        self.write_process = Process(target=self._write_target)
        self.write_process.start()

        self.write_android_process = Process(target=self._write_android)
        self.write_android_process.start()

        print('Successfully reconnected to Algorithm')

    def _reconnect_arduino(self):
        self.arduino.disconnect()
        
        self.read_arduino_process.terminate()
        self.write_process.terminate()
        self.write_android_process.terminate()

        self.arduino.connect()

        self.read_arduino_process = Process(target=self._read_arduino)
        self.read_arduino_process.start()

        self.write_process = Process(target=self._write_target)
        self.write_process.start()
        
        self.write_android_process = Process(target=self._write_android)
        self.write_android_process.start()

        print('Successfully reconnected to Arduino')


    def _reconnect_android(self):
        self.android.disconnect()
        
        self.read_android_process.terminate()
        self.write_process.terminate()
        self.write_android_process.terminate()
        
        self.android.connect()
        
        self.read_android_process = Process(target=self._read_android)
        self.read_android_process.start()

        self.write_process = Process(target=self._write_target)
        self.write_process.start()
        
        self.write_android_process = Process(target=self._write_android)
        self.write_android_process.start()

        print('Successfully reconnected to Android')
        
    def _read_arduino(self):
        while True:
            try:
                messages = self.arduino.read()
                
                if messages is None:
                    continue
                message_list = messages.splitlines()
                
                for message in message_list:
                
                    if len(message) <= 0:
                        continue    
                        
                    self.message_deque.append(self._format_for(
                        ALGORITHM_HEADER, 
                        message + NEWLINE
                    ))
                    
            except Exception as error:
                print('Process read_arduino failed: ' + str(error))
                break    

    def _read_algorithm(self):
        while True:
            try:
                messages = self.algorithm.read()
                
                if messages is None:
                    continue
                
                message_list = messages.splitlines()
                
                for message in message_list:
                
                    if len(message) <= 0:
                        continue
                    
                    # image recognition
                    elif message[0] == Algorithm-RPi.TAKE_PICTURE:

                        if self.image_count.value >= 5:
                            self.message_deque.append(self._format_for(
                            ALGORITHM_HEADER, 
                            RPi-Algorithm.DONE_IMG_REC + NEWLINE
                        ))
                        
                        else:
                            
                            message = message[2:-1]  # to remove 'C[' and ']'
                            self.to_android_message_deque.append(
                                RPi-Android.STATUS_TAKING_PICTURE + NEWLINE
                            )
                            image = self._take_pic()
                            print('Picture taken')
                            self.message_deque.append(self._format_for(
                                ALGORITHM_HEADER, 
                                RPi-Algorithm.DONE_TAKING_PICTURE + NEWLINE
                            ))
                            self.image_deque.append([image,message])

                    elif message == Algorithm-RPi.EXPLORATION_COMPLETE:
                        # to let image processing server end all processing and display all images
                        self.status = Status.IDLE
                        
                        # to use pillow instead of cv2
                        pil_img = Image.open(STOPPING_IMAGE).convert('RGB')
                        cv_img = np.array(pil_img)
                        cv_img = cv_img[:,:,::-1].copy()
                        self.image_deque.append([cv_img,"-1,-1|-1,-1|-1,-1"])

                    elif message[0] == Algorithm-Android.MDF_STRING:
                        self.to_android_message_deque.append( 
                            message[1:] + NEWLINE
                        )
                    
                    else:  # (message[0]=='W' or message in ['D|', 'A|', 'Z|']):
                        self._forward_message_algorithm_to_android(message)
                        self.message_deque.append(self._format_for(
                            ARDUINO_HEADER, 
                            message + NEWLINE
                        ))
                
            except Exception as error:
                print('Process read_algorithm failed: ' + str(error))
                break

    def _forward_message_algorithm_to_android(self, message):
        messages_for_android = message.split(MESSAGE_SEPARATOR)

        for message_for_android in messages_for_android:
            
            if len(message_for_android) <= 0:
                continue

            elif message_for_android[0] == Algorithm-Android.CALIBRATING_CORNER:
                self.to_android_message_deque.append(
                    RPi-Android.STATUS_CALIBRATING_CORNER + NEWLINE
                )

            elif message_for_android[0] == Algorithm-Android.SENSE_ALL:
                self.to_android_message_deque.append(
                    RPi-Android.STATUS_SENSE_ALL + NEWLINE
                )

            elif message_for_android[0] == Algorithm-Android.ALIGN_RIGHT:
                self.to_android_message_deque.append(
                    RPi-Android.STATUS_ALIGN_RIGHT + NEWLINE
                )

            elif message_for_android[0] == Algorithm-Android.ALIGN_FRONT:
                self.to_android_message_deque.append(
                    RPi-Android.STATUS_ALIGN_FRONT + NEWLINE
                )

            elif message_for_android[0] == Algorithm-Android.MOVE_FORWARD:
                if self.status == Status.EXPLORING:
                    self.to_android_message_deque.append(
                        RPi-Android.STATUS_EXPLORING + NEWLINE
                    )

            elif message_for_android[0] == Algorithm-Android.TURN_LEFT:
                self.to_android_message_deque.append(
                    RPi-Android.TURN_LEFT + NEWLINE
                )
                
                self.to_android_message_deque.append(
                   RPi-Android.STATUS_TURNING_LEFT + NEWLINE
                )
            
            elif message_for_android[0] == Algorithm-Android.TURN_RIGHT:
                self.to_android_message_deque.append(
                    RPi-Android.TURN_RIGHT + NEWLINE
                )
                
                self.to_android_message_deque.append(
                   RPi-Android.STATUS_TURNING_RIGHT + NEWLINE
                )
            


                forward_steps_no = int(message_for_android.decode()[1:])

                print('Move forward by this number of steps:', forward_steps_no)
                for _ in range(forward_steps_no):
                    self.to_android_message_deque.append(
                        RPi-Android.MOVE_UP + NEWLINE
                    )           
                    
                    self.to_android_message_deque.append(
                        RPi-Android.STATUS_MOVING_FORWARD + NEWLINE
                    )        

    def _read_android(self):
        while True:
            try:
                messages = self.android.read()
                
                if messages is None:
                    continue
                  
                message_list = messages.splitlines()
                
                for message in message_list:
                    if len(message) <= 0:
                        continue

                    elif message in (Android-Arduino.ALL_MESSAGES + [Android-RPi.CALIBRATE_SENSOR]):
                        if message == Android-RPi.CALIBRATE_SENSOR:
                            self.message_deque.append(self._format_for(
                                ARDUINO_HEADER, 
                                RPi-Arduino.CALIBRATE_SENSOR + NEWLINE
                            ))
                        
                        else:
                            self.message_deque.append(self._format_for(
                                ARDUINO_HEADER, message + NEWLINE
                            ))
                        
                    else:
                        if message == Android-Algorithm.START_SHORTEST_PATH:
                            self.status = Status.SHORTEST_PATH
                            self.message_deque.append(self._format_for(
                                ARDUINO_HEADER,
                                RPi-Arduino.START_SHORTEST_PATH + NEWLINE
                            ))

                        elif message == Android-Algorithm.START_EXPLORATION:
                            self.status = Status.EXPLORING
                            time.sleep(0.5)
                            self.message_deque.append(self._format_for(
                                ARDUINO_HEADER, 
                                RPi-Arduino.START_EXPLORATION + NEWLINE
                            ))

                        self.message_deque.append(self._format_for(
                            ALGORITHM_HEADER, 
                            message + NEWLINE
                        ))
			
                    
            except Exception as error:
                print('Process read_android failed: ' + str(error))
                break

    def _write_target(self):
        while True:
            target = None
            try:
                if len(self.message_deque)>0:
                    message = self.message_deque.popleft()
                    target, payload = message['target'], message['payload']

                    if target == ARDUINO_HEADER:
                        self.arduino.write(payload)
                        
                    elif target == ALGORITHM_HEADER:
                        self.algorithm.write(payload)
                        
                    else:
                        print("Invalid header", target)
                
            except Exception as error:
                print('Process write_target failed: ' + str(error))

                if target == ARDUINO_HEADER:
                    self.dropped_connection.value = 0

                elif target == ALGORITHM_HEADER:
                    self.dropped_connection.value = 1
                    
                self.message_deque.appendleft(message)
                
                break
                
    def _write_android(self):
        while True:
            try:
                if len(self.to_android_message_deque)>0:
                    message = self.to_android_message_deque.popleft()
                    
                    self.android.write(message)
                
            except Exception as error:
                print('Process write_android failed: ' + str(error))
                self.to_android_message_deque.appendleft(message)
                break
				
    def _take_pic(self):
        global img_count
        try:

            # initialise pi camera
            camera = PiCamera(resolution=(IMAGE_WIDTH, IMAGE_HEIGHT))  # '640x360'
            camera.awb_mode = 'horizon'

            # allow the camera to warmup
            time.sleep(3)
            rawCapture = PiRGBArray(camera)
            
            # grab an image from the camera
            camera.capture(rawCapture, format=IMAGE_FORMAT)
            image = rawCapture.array
            pil_img = np.asarray(image)
            pil_img = Image.fromarray(pil_img[:,:,::-1])
            pil_img.save('./frame_{}.jpg'.format(img_count))
            img_count += 1

            camera.close()
        
        except Exception as error:
            print('Picture taking process failed: ' + str(error))
        
        return image
    
    def _process_pic(self):
        print("----------------------- Image Processing Starts -------------------")
        print(self.image_processing_server_url)
        print(type(self.image_processing_server_url))
        image_sender = imagezmq.ImageSender(
            connect_to=self.image_processing_server_url)
        print(type(self.image_deque))
        image_id_list = []
        while True:
            try:
                if not self.image_deque.empty():
                    
                    message_for_image =  self.image_deque.popleft()
                    # format: 'x,y|x,y|x,y'
                    obstacle_coordinates = message_for_image[1]

                    #print("-----------Prepareing to send image---------")
                    reply = image_sender.send_image(
                        'image message from RPi',
                        message_for_image[0]
                    )
                    
                    reply = reply.decode('utf-8')
                    print("Reply:" + reply)
                    

                    if reply == 'End':
                        break  # stop sending images
                    
                    # example replies
                    # "1|2|3" 3 symbols in order from left to right
                    # "1|-1|3" 2 symbols, 1 on the left, 1 on the right
                    # "1" 1 symbol either on the left, middle or right
                    else:
                        detections = reply.split(MESSAGE_SEPARATOR) #from server
                        coordinate_list_obstacle = obstacle_coordinates.split(MESSAGE_SEPARATOR) #from algo

                        for detection, coordinates in zip(detections, coordinate_list_obstacle):
                            print("Coordinate:",coordinates)
                            print("Detection:",detection)

                            if detection == '-1':
                                continue  # if there isn't any  symbol detected, mapping of symbol id will  be skipped
                            elif coordinates == '-1,-1':
                                continue  # if there isn't any obstacle detected, mapping of id symbol will skipped

                            else:
                                id_string_to_android = '{"image":[' + coordinates + \
                                ',' + detection + ']}'
                                print(id_string_to_android)
                                
                                if detection not in image_id_list:
                                    self.image_count.value += 1
                                    image_id_list.append(detection)
                                
                                self.to_android_message_deque.append(
                                    id_string_to_android + NEWLINE
                                )


            except Exception as error:
                print('Image processing process has failed: ' + str(error))

    def _format_for(self, target, payload):
        return {
            'target': target,
            'payload': payload,
        }
