LOCALE = 'UTF-8'

#Android BT connection settings

RFCOMM_CHANNEL = 1
RPI_MAC_ADDR = 'b8:27:eb:5a:34:dc'
UUID = '48f2491c-350e-4c1c-b181-7e4a591cc311'
ANDROID_SOCKET_BUFFER_SIZE = 512

# Algorithm Wifi connection settings
# raspberryHotPotato: 192.168.3.1
WIFI_IP = '192.168.3.1'
WIFI_PORT = 8080
ALGORITHM_SOCKET_BUFFER_SIZE = 512

# Arduino USB connection settings
# SERIAL_PORT = '/dev/ttyACM0'
# Symbolic link to always point to the correct port that arduino is connected to
SERIAL_PORT = '/dev/ttyACM0'
BAUD_RATE = 115200

# Image Recognition Settings
STOPPING_IMAGE = 'stop_image_processing.jpg'

IMAGE_WIDTH = 640
IMAGE_HEIGHT = 360
IMAGE_FORMAT = 'bgr'

<<<<<<< HEAD
BASE_IP = 'tcp://192.168.3.'
PORT = ':5555'

IMAGE_PROCESSING_SERVER_URLS = {
    'cheyanne': BASE_IP + '1' + PORT,
    'elbert': BASE_IP + '00' + PORT,  # don't have elbert's ip address yet
    'jason': BASE_IP + '52' + PORT,
    'joshua': BASE_IP + '93' + PORT,
    'mingyang': BASE_IP + '74' + PORT,
    'reuben': BASE_IP + '00' + PORT,  # don't have reuben's ip address yet
    'winston': BASE_IP + '55' + PORT,
    'yingting': BASE_IP + '90' + PORT,
}
=======
#from seniors
BASE_IP = 'tcp://192.168.3.'
PORT = ':5555'

IMAGE_PROCESSING_SERVER_URLS = 'tcp://192.168.3.13:5555'
>>>>>>> f2afaf36a2b19dd5407c0283ddd18d146ad6b9d5


