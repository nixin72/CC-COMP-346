#!/bin/python

# Wrote this quick script instead of using grep since it's a 
# platform independent solution.  

import os;
import re;

def main():
    for i in range(3):
        os.system("javac StackManager.java")
        output = os.popen("java StackManager").read()
        
        grep = ""
        for line in re.split("\n", output):
            if re.match("Stack S", line):
                grep += line + "\n"

        file = open("../Output_" + str(i) + ".txt", "w+")
        file.write(output)
        
        file = open("../Grep_" + str(i) + ".txt", "w+")
        file.write(grep)

if __name__ == "__main__":
    main()
