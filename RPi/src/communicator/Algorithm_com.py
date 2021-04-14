import socket

from src.config import LOCALE, ALGORITHM_SOCKET_BUFFER_SIZE, WIFI_IP, WIFI_PORT

class Algorithm_communicator:
    def __init__(self, host=WIFI_IP, port=WIFI_PORT):

        self.port = port
        self.host = host


        self.socket = None
        self.address = None
        self.client_sock = None

        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

        self.socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        print('The port address is :', self.port)
        print('The host address is :', self.host)
        self.socket.bind((self.host, self.port))
        self.socket.listen(1)

    def connect_algo(self):
        while True:
            retry = False

            try:
                print('Connecting with Algorithm PC')

                if self.client_sock is None:
                    self.client_sock, self.address = self.socket.accept()
                    print('Successfully connected with Algorithm PC: ' + str(self.address))
                    retry = False
                
            except Exception as error:
                print('Connection with Algorithm failed: ' + str(error))

                if self.client_sock is not None:
                    self.client_sock.close()
                    self.client_sock = None
                retry = True

            if not retry:
                break
            print("Retrying Algorithm connection...")

    def disconnect_algo(self):
        try:
            if self.client_sock is not None:
                self.client_sock.close()
                self.client_sock = None

            print("Algorithm disconnected Successfully")

        except Exception as error:
            print("Algorithm disconnect failed: " + str(error))

    def disconnect_all_algo(self):
        try:
            if self.client_sock is not None:
                self.client_sock.close()
                self.client_sock = None

            if self.socket is not None:
                self.socket.close()
                self.socket = None

            print("Algorithm disconnected Successfully")

        except Exception as error:
            print("Algorithm disconnect failed: " + str(error))

    def read_algo(self):
        try:
            message = self.client_sock.recv(ALGORITHM_SOCKET_BUFFER_SIZE).strip()

            if len(message) > 0:
                print('Message From Algorithm:')
                print(message)
                return message

            return None

        except Exception as error:
            print('Algorithm read process failed: ' + str(error))
            raise error

    def write_algo(self, message):
        try:
            print('To Algorithm:')
            print(message)
            self.client_sock.send(message)

        except Exception as error:
            print('Algorithm write process failed: ' + str(error))
            raise error

if __name__ == '__main__':
    message = "Hello from Rpi"
    A = Algorithm_communicator()
    A.connect_algo()
    print("Connection is successful") 
    A.write_algo(message)
    print("Message successfully sent")
    A.read_algo()
    print("Message successfully receved")
  #  Algorithm().write(message)
    print("Algo script successfully ran.")
