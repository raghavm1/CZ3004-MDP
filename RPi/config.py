LOCALE = 'UTF-8'

#Android BT connection settings

RFCOMM_CHANNEL = 1
RPI_MAC_ADDR = 'b8:27:eb:5a:34:dc'
UUID = ''
ANDROID_SOCKET_BUFFER_SIZE = 512

# Algorithm Wifi connection settings
# raspberryHotPotato: 192.168.3.1
WIFI_IP = '192.168.3.1'
WIFI_PORT = 8080
ALGORITHM_SOCKET_BUFFER_SIZE = 512

# Arduino USB connection settings
# SERIAL_PORT = '/dev/ttyACM0'
# Symbolic link to always point to the correct port that arduino is connected to
SERIAL_PORT = 'ttyACM0'
BAUD_RATE = 115200

# Image Recognition Settings
STOPPING_IMAGE = ''

IMAGE_WIDTH = 1920
IMAGE_HEIGHT = 1080
IMAGE_FORMAT = 'bgr'

BASE_IP = 'tcp://192.168.3.1'
PORT = ':8080'
'''
IMAGE_PROCESSING_SERVER_URLS = {
    'cheyanne': BASE_IP + '54' + PORT,
    'elbert': BASE_IP + '00' + PORT,  # don't have elbert's ip address yet
    'jason': BASE_IP + '52' + PORT,
    'joshua': BASE_IP + '93' + PORT,
    'mingyang': BASE_IP + '74' + PORT,
    'reuben': BASE_IP + '00' + PORT,  # don't have reuben's ip address yet
    'winston': BASE_IP + '55' + PORT,
    'yingting': BASE_IP + '90' + PORT,
}
'''

