import serial

from src.config import SERIAL_PORT
from src.config import BAUD_RATE
from src.config import LOCALE


class Arduino_communicator:
    def __init__(self, serial_port=SERIAL_PORT, baud_rate=BAUD_RATE):
        self.serial_port = serial_port
        self.baud_rate = baud_rate
        self.connection = None

    def connect_arduino(self):
        count = 10000
        while True:
            retry = False

            try:
                if count >= 10000:
                    print('Now building connection with Arduino Board')

                self.connection = serial.Serial(self.serial_port, self.baud_rate)

                if self.connection is not None:
                    print('Successfully connected with Arduino Board: ' + str(self.connection.name))
                    retry = False

            except Exception as error:
                if count >= 10000:
                    print('Connection with Arduino Board failed: ' + str(error))

                retry = True

            if not retry:
                break

            if count >= 10000:
                print('Retrying Arduino connection...')
                count = 0

            count += 1

    def disconnect_arduino(self):
        try:
            if self.connection is not None:
                self.connection.close()
                self.connection = None

                print('Successfully closed connection with Arduino')

        except Exception as error:
            print('Arduino close connection failed: ' + str(error))

    def read_arduino(self):
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

    def write_arduino(self, message):
        try:
            print('To Arduino:')
            print(message)
            self.connection.write(message)

        except Exception as error:
            print('Arduino write failed: ' + str(error))
            raise error

if __name__ == '__main__':
    A = Arduino_communicator()
    A.connect_arduino()
    A.read_arduino()
    message = "Hello Arduino"
<<<<<<< HEAD:RPi/src/communicator/Arduino.py
    A.write(message)
=======
    A.write_arduino(message)
>>>>>>> f2afaf36a2b19dd5407c0283ddd18d146ad6b9d5:RPi/src/communicator/Arduino_com.py
    print("Arduino script succesfully ran.")
