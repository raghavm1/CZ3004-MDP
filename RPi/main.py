import os
import argparse

from src.communicator.MultiProcessCommunication import MultiProcessCommunicator


def init():
    os.system("sudo hciconfig hci0 piscan")
    multiprocess_communication_process = MultiProcessCommunicator()
    multiprocess_communication_process.start()



if __name__ == '__main__':
    init()
 