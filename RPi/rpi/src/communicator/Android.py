import bluetooth as bt
from src.config import ANDROID_SOCKET_BUFFER_SIZE, LOCALE, RFCOMM_CHANNEL, UUID
#from config import ANDROID_SOCKET_BUFFER_SIZE, LOCALE, RFCOMM_CHANNEL, UUID
'''
Raspberry Pi acts as socket server whereas Android device acts as a client. Android N7 needs a client socket script as well to build connection.
Should be able to send and receive data and information through the server/client.

'''

class Android:
    def __init__(self):
        self.server_sock = None
        self.client_sock = None

        self.server_sock = bt.BluetoothSocket(bt.RFCOMM)
        self.server_sock.bind(("", RFCOMM_CHANNEL))

        self.server_sock.listen(RFCOMM_CHANNEL)
        bt.advertise_service(

            self.server_sock,
            'MDO_Group_3_RPi',
            profiles=[bt.SERIAL_PORT_PROFILE],
            service_id = UUID,
            service_classes = [UUID, bt.SERIAL_PORT_CLASS]
        )

        print('server socket:', str(self.server_sock))

    def connect(self):
        while True:
            retry = False

            try:
                print('Establishing connection with Android N7 Tablet...')

                if self.client_sock is None:
                    print('Please tap on the BT name on the tablet')
                    self.client_sock, address = self.server_sock.accept()
                    print("Successfully connected to Android at address: " + str(address))
                    retry = False

            except Exception as error:
                print("Connection with Android failed: " + str(error))

                if self.client_sock is not None:
                    self.client_sock.close()
                    self.client_sock = None

                retry = True

            if not retry:
                break

            print('Retrying Bluetooth Connection to Android...')

    def disconnect(self):
        try:
            if self.client_sock is not None:
                self.client_sock.close()
                self.client_sock = None

            print("Android disconnected Successfully")

        except Exception as error:
            print("Android disconnect failed: " + str(error))

    def disconnect_all(self):
        try:
            if self.client_sock is not None:
                self.client_sock.close()
                self.client_sock = None

            if self.server_sock is not None:
                self.server_sock.close()
                self.server_sock = None

            print("Android disconnected Successfully")

        except Exception as error:
            print("Android disconnect failed: " + str(error))

    def read(self):
        try:
            message = self.client_sock.recv(ANDROID_SOCKET_BUFFER_SIZE).strip()
            print('From android:')
            print(message)

            if message is None:
                return None

            if len(message) > 0:
                return message

            return None

        except Exception as error:
            print('Android read failed: ' + str(error))
            raise error

    def write(self, message):
        try:
            print('To Android:')
            print(message)
            self.client_sock.send(message)

        except Exception as error:
            print('Android write failed: ' + str(error))
            raise error

if __name__ == '__main__':
    A = Android()
    A.connect()
    A.read()
    message = 'Hello Android!'
    A.write(message)
    print("Android script successfully ran.")

