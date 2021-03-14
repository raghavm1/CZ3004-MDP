import time
import collections
from datetime import datetime
from multiprocessing import Process, Value
from multiprocessing.managers import BaseManager

import cv2
# import imagezmq

from picamera import PiCamera
from picamera.array import PiRGBArray

from src.communicator.Android import Android
from src.communicator.Arduino import Arduino
from src.communicator.Algorithm import Algorithm
from src.config import STOPPING_IMAGE, IMAGE_WIDTH, IMAGE_HEIGHT, IMAGE_FORMAT
from src.protocols import *

class DequeManager(BaseManager):
    pass
    
class DequeProxy(object):
    def __init__(self, *args):
        self.deque = collections.deque(*args)
    def __len__(self):
        return self.deque.__len__()
    def appendleft(self, x):
        self.deque.appendleft(x)
    def append(self, x):
        self.deque.append(x)
    def popleft(self):
        return self.deque.popleft()
        
DequeManager.register('DequeProxy', DequeProxy,
                      exposed=['__len__', 'append', 'appendleft', 'popleft'])   

class MultiProcessComms:
    """
    This class handles multi-processing communications between Arduino, Algorithm and Android.
    """
    def __init__(self, image_processing_server_url: str=None):
        """
        Instantiates a MultiProcess Communications session and set up the necessary variables.

        Upon instantiation, RPi begins connecting to
        - Arduino
        - Algorithm
        - Android
        in this exact order.

        Also instantiates the queues required for multiprocessing.
        """
        print('Initializing Multiprocessing Communication')

        self.arduino = Arduino()  # handles connection to Arduino
        self.algorithm = Algorithm()  # handles connection to Algorithm
        self.android = Android()  # handles connection to Android
        
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

        self.image_process = None

        if image_processing_server_url is not None:
            self.image_process = Process(target=self._process_pic)

            # pictures taken using the PiCamera are placed in this queue
            self.image_deque = self.manager.DequeProxy()

            self.image_processing_server_url = image_processing_server_url
            self.image_count = Value('i',0)
        
        
    def start(self):        
        try:
            self.arduino.connect()
            self.algorithm.connect()
            self.android.connect()

            print('Connected to Arduino, Algorithm and Android')
            
            self.read_arduino_process.start()
            self.read_algorithm_process.start()
            self.read_android_process.start()
            self.write_process.start()
            self.write_android_process.start()

            # if self.image_process is not None:
                # self.image_process.start()
            
            print('Started all processes: read-arduino, read-algorithm, read-android, write, image')

            print('Multiprocess communication session started')
            
        except Exception as error:
            raise error

        self._allow_reconnection()

    def end(self):
        # children processes should be killed once this parent process is killed
        self.algorithm.disconnect_all()
        self.android.disconnect_all()
        print('Multiprocess communication session ended')

    def _allow_reconnection(self):
        print('You can reconnect to RPi after disconnecting now')

        while True:
            try:
                if not self.read_arduino_process.is_alive():
                    self._reconnect_arduino()
                    
                if not self.read_algorithm_process.is_alive():
                    self._reconnect_algorithm()
                    
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

        print('Reconnected to Arduino')

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

        print('Reconnected to Algorithm')

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

        print('Reconnected to Android')
        
    def _read_arduino(self):
        while True:
            try:
                raw_message = self.arduino.read()
                
                if raw_message is None:
                    continue
                message_list = raw_message.splitlines()
                
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
                raw_message = self.algorithm.read()
                
                if raw_message is None:
                    continue
                
                message_list = raw_message.splitlines()
                
                for message in message_list:
                
                    if len(message) <= 0:
                        continue

                    elif message[0] == AlgorithmToRPi.TAKE_PICTURE:

                        if self.image_count.value >= 5:
                            self.message_deque.append(self._format_for(
                            ALGORITHM_HEADER, 
                            RPiToAlgorithm.DONE_IMG_REC + NEWLINE
                        ))
                        
                        else:
                            
                            message = message[2:-1]  # to remove 'C[' and ']'
                            # self.to_android_message_deque.append(
                                # RPiToAndroid.STATUS_TAKING_PICTURE + NEWLINE
                            # )
                            image = self._take_pic()
                            print('Picture taken')
                            self.message_deque.append(self._format_for(
                                ALGORITHM_HEADER, 
                                RPiToAlgorithm.DONE_TAKING_PICTURE + NEWLINE
                            ))
                            self.image_deque.append([image,message])

                    elif message == AlgorithmToRPi.EXPLORATION_COMPLETE:
                        # to let image processing server end all processing and display all images
                        self.status = Status.IDLE
                        self.image_deque.append([cv2.imread(STOPPING_IMAGE),"-1,-1|-1,-1|-1,-1"])

                        # self.to_android_message_deque.append(
                            # RPiToAndroid.STATUS_IDLE + NEWLINE
                        # )

                    elif message[0] == AlgorithmToAndroid.MDF_STRING:
                        self.to_android_message_deque.append( 
                            message[1:] + NEWLINE
                        )
                    
                    else:  # (message[0]=='W' or message in ['D|', 'A|', 'Z|']):
                        #self._forward_message_algorithm_to_android(message)
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
                
            elif message_for_android[0] == AlgorithmToAndroid.TURN_LEFT:
                self.to_android_message_deque.append(
                    RPiToAndroid.TURN_LEFT + NEWLINE
                )
                
                # self.to_android_message_deque.append(
                    # RPiToAndroid.STATUS_TURNING_LEFT + NEWLINE
                # )
            
            elif message_for_android[0] == AlgorithmToAndroid.TURN_RIGHT:
                self.to_android_message_deque.append(
                    RPiToAndroid.TURN_RIGHT + NEWLINE
                )
                
                # self.to_android_message_deque.append(
                    # RPiToAndroid.STATUS_TURNING_RIGHT + NEWLINE
                # )
            
            elif message_for_android[0] == AlgorithmToAndroid.CALIBRATING_CORNER:
                self.to_android_message_deque.append(
                    RPiToAndroid.STATUS_CALIBRATING_CORNER + NEWLINE
                )
            
            elif message_for_android[0] == AlgorithmToAndroid.SENSE_ALL:
                self.to_android_message_deque.append(
                    RPiToAndroid.STATUS_SENSE_ALL + NEWLINE
                )
                
            elif message_for_android[0] == AlgorithmToAndroid.ALIGN_RIGHT:
                self.to_android_message_deque.append(
                    RPiToAndroid.STATUS_ALIGN_RIGHT + NEWLINE
                )
                
            # elif message_for_android[0] == AlgorithmToAndroid.ALIGN_FRONT:
                # self.to_android_message_deque.append(
                    # RPiToAndroid.STATUS_ALIGN_FRONT + NEWLINE
                # )
            
            elif message_for_android[0] == AlgorithmToAndroid.MOVE_FORWARD:
                # if self.status == Status.EXPLORING:
                    # self.to_android_message_deque.append(
                        # RPiToAndroid.STATUS_EXPLORING + NEWLINE
                    # )
                
                # elif self.status == Status.FASTEST_PATH:
                    # self.to_android_message_deque.append(
                        # RPiToAndroid.STATUS_FASTEST_PATH + NEWLINE
                    # )
                num_steps_forward = int(message_for_android.decode()[1:])

                # TODO
                print('Number of steps to move forward:', num_steps_forward)
                for _ in range(num_steps_forward):
                    self.to_android_message_deque.append(
                        RPiToAndroid.MOVE_UP + NEWLINE
                    )           
                    
                    # self.to_android_message_deque.append(
                        # RPiToAndroid.STATUS_MOVING_FORWARD + NEWLINE
                    # )        

    def _read_android(self):
        while True:
            try:
                raw_message = self.android.read()
                
                if raw_message is None:
                    continue
                  
                message_list = raw_message.splitlines()
                
                for message in message_list:
                    if len(message) <= 0:
                        continue

                    elif message in (AndroidToArduino.ALL_MESSAGES + [AndroidToRPi.CALIBRATE_SENSOR]):
                        if message == AndroidToRPi.CALIBRATE_SENSOR:
                            self.message_deque.append(self._format_for(
                                ARDUINO_HEADER, 
                                RPiToArduino.CALIBRATE_SENSOR + NEWLINE
                            ))
                        
                        else:
                            self.message_deque.append(self._format_for(
                                ARDUINO_HEADER, message + NEWLINE
                            ))
                        
                    else:  # if message in ['SE|', 'SSP|', 'SendArena']:
                        if message == AndroidToAlgorithm.START_EXPLORATION:
                            self.status = Status.EXPLORING
                            time.sleep(0.5)
                            self.message_deque.append(self._format_for(
                                ARDUINO_HEADER, 
                                RPiToArduino.START_EXPLORATION + NEWLINE
                            ))

                        elif message == AndroidToAlgorithm.START_SHORTEST_PATH:
                            self.status = Status.SHORTEST_PATH
                            self.message_deque.append(self._format_for(
                                ARDUINO_HEADER, 
                                RPiToArduino.START_SHORTEST_PATH + NEWLINE
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
        try:
            start_time = datetime.now()

            # initialize the camera and grab a reference to the raw camera capture
            camera = PiCamera(resolution=(IMAGE_WIDTH, IMAGE_HEIGHT))  # '1920x1080'
            rawCapture = PiRGBArray(camera)
            
            # allow the camera to warmup
            time.sleep(0.1)
            
            # grab an image from the camera
            camera.capture(rawCapture, format=IMAGE_FORMAT)
            image = rawCapture.array
            camera.close()

            print('Time taken to take picture: ' + str(datetime.now() - start_time) + 'seconds')
            
            # to gather training images
            # os.system("raspistill -o images/test"+
            # str(start_time.strftime("%d%m%H%M%S"))+".png -w 1920 -h 1080 -q 100")
        
        except Exception as error:
            print('Taking picture failed: ' + str(error))
        
        return image
    
    def _process_pic(self):
        # initialize the ImageSender object with the socket address of the server
        image_sender = imagezmq.ImageSender(
            connect_to=self.image_processing_server_url)
        image_id_list = []
        while True:
            try:
                if not self.image_deque.empty():
                    start_time = datetime.now()
                    
                    image_message =  self.image_deque.popleft()
                    # format: 'x,y|x,y|x,y'
                    obstacle_coordinates = image_message[1]
                    
                    reply = image_sender.send_image(
                        'image from RPi', 
                        image_message[0]
                    )
                    reply = reply.decode('utf-8')

                    if reply == 'End':
                        break  # stop sending images
                    
                    # example replies
                    # "1|2|3" 3 symbols in order from left to right
                    # "1|-1|3" 2 symbols, 1 on the left, 1 on the right
                    # "1" 1 symbol either on the left, middle or right
                    else:
                        detections = reply.split(MESSAGE_SEPARATOR)
                        obstacle_coordinate_list = obstacle_coordinates.split(MESSAGE_SEPARATOR)

                        for detection, coordinates in zip(detections, obstacle_coordinate_list):
                            
                            if coordinates == '-1,-1':
                                continue  # if no obstacle, skip mapping of symbol id
                            elif detection == '-1':
                                continue  # if no symbol detected, skip mapping of symbol id
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

                    print('Time taken to process image: ' + \
                        str(datetime.now() - start_time) + ' seconds')

            except Exception as error:
                print('Image processing failed: ' + str(error))

    def _format_for(self, target, payload):
        return {
            'target': target,
            'payload': payload,
        }
