#!/usr/bin/env python
import subprocess
import sys

import random
import time
import os

numChildren = 6
#rint sys.argv
if(len(sys.argv) == 2):
  numChildren = int(sys.argv[1])
  
print numChildren

print "My pid is " + str(os.getpid())

if(numChildren > 0):
  subprocess.Popen([os.path.realpath(__file__), str(numChildren-1)])
  subprocess.Popen([os.path.realpath(__file__), str(numChildren-1)])
  
time.sleep(random.randint(1,15))

if(random.randint(0,2) > 1):
  subprocess.Popen(["kill","-KILL", str(os.getpid()) ])