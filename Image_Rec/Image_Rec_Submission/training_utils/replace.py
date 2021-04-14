import os
print(os.getcwd())
for filename in os.listdir(os.getcwd()):
    if filename.endswith(".txt"):
            with open('{}' .format(filename), 'r+') as file:
                lines = file.readlines()
                file.seek(0, 0) #set the pointer to 0,0 cordinate of file
                for line in lines:
                    row = line.strip().split(" ")
                    print('Current row')
                    print(row)
                    print('Current label') 
                    print(row[0])
                    row[0] = '0' # Edit label no here
                    print('New label')
                    print(row[0])
                    print('New row')
                    print(row)
                    print('------------------------------------------')
                    file.write(" ".join(row) + "\n")