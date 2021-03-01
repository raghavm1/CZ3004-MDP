'''
Communication protocols.
They are pre-defined so it allows all subsystems to know the common ways of communication
'''

MESSAGE_SEPARATOR = '|'.encode()
NEWLINE = '\n'.encode()

ANDROID_HEADER = 'AND'.encode()
ARDUINO_HEADER = 'ARD'.encode()
ALGORITHM_HEADER = 'ALG'.encode()

class Status:
    IDLE = 'idle'.encode()
    EXPLORING = 'exploring'.encode()
    SHORTEST_PATH = 'shortest path'.encode()


class AndroidToArduino:
    MOVE_FORWARD = 'U1|'.encode()
    MOVE_BACK = 'B1|'.encode()
    TURN_LEFT = 'L1|'.encode()
    TURN_RIGHT = 'W1|'.encode()
    DO_SHORTCUT_1 = 'F1|'.encode()
    DO_SHORTCUT_2 = 'F2|'.encode()

    ALL_MESSAGES = [
        MOVE_FORWARD,
        MOVE_BACK,
        TURN_LEFT,
        TURN_RIGHT,
        DO_SHORTCUT_1,
        DO_SHORTCUT_2,
    ]


class AndroidToAlgorithm:
    START_EXPLORATION = 'SE|'.encode()
    START_SHORTEST_PATH = 'SP|'.encode()
    SEND_ARENA = 'SendArena'.encode()
    


class AndroidToRPi:
    CALIBRATE_SENSOR = 'CS|'.encode()


class AlgorithmToAndroid:
    MOVE_FORWARD = 'FS'.encode()[0]
    TURN_LEFT = 'L'.encode()[0]
    TURN_RIGHT = 'W'.encode()[0]
    CALIBRATING_CORNER = 'C'.encode()[0]
    SENSE_ALL = 'S'.encode()[0]
    ALIGN_RIGHT = 'AR'.encode()[0]
    ALIGN_FRONT = 'AF'.encode()[0]

    MDF_STRING = 'M'.encode()[0]


class AlgorithmToRPi:
    TAKE_PICTURE = 'C'.encode()[0]
    EXPLORATION_COMPLETE = 'D'.encode()


class RPiToAndroid:
    STATUS_EXPLORATION = '{"status":"exploring"}'.encode()
    STATUS_SHORTEST_PATH = '{"status":"shortest path"}'.encode()
    STATUS_TURN_LEFT = '{"status":"turning left"}'.encode()
    STATUS_TURN_RIGHT = '{"status":"turning right"}'.encode()
    STATUS_IDLE = '{"status":"idle"}'.encode()
    STATUS_TAKING_PICTURE = '{"status":"taking picture"}'.encode()
    STATUS_CALIBRATING_CORNER = '{"status":"calibrating corner"}'.encode()
    STATUS_SENSE_ALL = '{"status":"sense all"}'.encode()
    STATUS_MOVING_FORWARD = '{"status":"moving forward"}'.encode()
    STATUS_ALIGN_RIGHT = '{"status":"align right"}'.encode()
    STATUS_ALIGN_FRONT = '{"status":"align front"}'.encode()

    MOVE_UP = '{"move":[{"direction":"forward"}]}'.encode()
    TURN_LEFT = '{"move":[{"direction":"left"}]}'.encode()
    TURN_RIGHT = '{"move":[{"direction":"right"}]}'.encode()


class RPiToArduino:
    CALIBRATE_SENSOR = 'L|A|'.encode()
    START_EXPLORATION = 'E|'.encode()
    START_SHORTEST_PATH = 'F|'.encode()


class RPiToAlgorithm:
    DONE_TAKING_PICTURE = 'D'.encode()
    DONE_IMG_REC = 'I'.encode()
