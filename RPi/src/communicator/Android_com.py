import bluetooth as bt
from src.config import ANDROID_SOCKET_BUFFER_SIZE, LOCALE, RFCOMM_CHANNEL, UUID

class Android_communicator:
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

    def connect_android(self):
        while True:
            retry = False

            try:
                print('Now Builing connection with Android Tablet...')

                if self.client_sock is None:
                    print('Please accept the connection request on Android Tablet')
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

    def disconnect_android(self):
        try:
            if self.client_sock is not None:
                self.client_sock.close()
                self.client_sock = None

            print("Android disconnected Successfully")

        except Exception as error:
            print("Android disconnect failed: " + str(error))

    def disconnect_all_android(self):
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

    def read_android(self):
        try:
            message = self.client_sock.recv(ANDROID_SOCKET_BUFFER_SIZE).strip()
            print('The message from android:')
            print(message)

            if message is None:
                return None

            if len(message) > 0:
                return message

            return None

        except Exception as error:
            print('Android read process has failed: ' + str(error))
            raise error

    def write_android(self, message):
        try:
            print('To Android Tablet:')
            print(message)
            self.client_sock.send(message)

        except Exception as error:
            print('Android write process has failed: ' + str(error))
            raise error

if __name__ == '__main__':
    A = Android_communicator()
    A.connect_android()
    A.read_android()
    message = 'Hello Android!'
    A.write_android(message)
    print("Android script successfully ran.")

