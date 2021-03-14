import serial
import time

from config import SERIAL_PORT
from config import BAUD_RATE
from config import LOCALE

SERIAL_PORT = "/dev/ttyACM0"
BAUD_RATE = 115200
LOCALE = 'utf-8'

class Arduino:
    def __init__(self, serial_port=SERIAL_PORT, baud_rate=BAUD_RATE):
        self.serial_port = serial_port
        self.baud_rate = baud_rate
        self.connection = None

    def connect(self):
        count = 10000
        while True:
            retry = False

            try:
                if count >= 10000:
                    print('Establishing connection with Arduino')

                self.connection = serial.Serial(self.serial_port, self.baud_rate)

                if self.connection is not None:
                    print('Successfully connected with Arduino: ' + str(self.connection.name))
                    retry = False

            except Exception as error:
                if count >= 10000:
                    print('Connection with Arduino failed: ' + str(error))

                retry = True

            if not retry:
                break

            if count >= 10000:
                print('Retrying Arduino connection...')
                count = 0

            count += 1

    def disconnect(self):
        try:
            if self.connection is not None:
                self.connection.close()
                self.connection = None

                print('Successfully closed connection with Arduino')

        except Exception as error:
            print('Arduino close connection failed: ' + str(error))

    def read(self):
        try:
            message = self.connection.readline().strip()
            print('From Arduino:')
            print(message)

            if len(message) > 0:
                return message

            return None

        except Exception as error:
            print('Arduino read failed: ' + str(error))
            raise error

    def write(self, message):
        try:
            print('To Arduino:')
            print(message)
            self.connection.write(message)

        except Exception as error:
            print('Arduino write failed: ' + str(error))
            raise error

if __name__ == '__main__':

    #Ar = Arduino()
    #Ar.__init__()
    #Ar.connect()
    ser = serial.Serial('/dev/ttyACM0', 115200, timeout =1)
    ser.flush()
   # time.sleep(0.5)
    while True: 
   # message = input("enter command : ")
      # ser.write(bytes(message, 'utf-8'))
       ser.write(b"W|\n")
       line = ser.readline().decode('utf-8').rstrip()
       print(line)
       
       time.sleep(1)
       #if message is not "":
         #ser.write(bytes(message, 'utf-8'))
         #ser.write(b"{}".format(message))
         #message = ""
         #ser.flush()
         #line = ser.readline().decode('utf-8').rstrip()
         #print(line)
         # time.sleep(1)
       #message = input("enter command : ")
       #message = message+"\n"
       #A.write(message.encode('utf-8'))
       #A.write(message.encode('utf-8'))
       #message = Ar.readline().decode('utf-8').rstrip()
      # print("Arduino script succesfully ran.")

    

