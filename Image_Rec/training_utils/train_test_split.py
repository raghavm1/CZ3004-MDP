import os
from glob import glob
import shutil
from sklearn.model_selection import train_test_split
# do test train splitting

# find image names
image_files = glob("./Processed/3_classes_dataset/*.png", recursive=True)
# remove file extension
image_names = [name.replace(".png", "") for name in image_files]
# Use scikit learn function for convenience
test_names, train_names = train_test_split(image_names, test_size=0.2)

def batch_move_files(file_list, source_path, destination_path):
    for file in file_list:
        image = file+'.png'
        xml = file+'.xml'
        shutil.move(image, destination_path)
        shutil.move(xml, destination_path)
    return


source_dir = "./Processed/3_classes_dataset/"
test_dir = "./Processed/3_classes_test/"
train_dir = "./Processed/3_classes_train/"

batch_move_files(test_names, source_dir, test_dir)
batch_move_files(train_names, source_dir, train_dir)
