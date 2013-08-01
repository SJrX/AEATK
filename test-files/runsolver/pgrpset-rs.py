#!/usr/bin/env python
import os
import inspect
import sys
from subprocess import call

os.setpgrp()
print "Python pid is " + str(os.getpid())
sys.stdout.flush()

call([os.path.dirname(os.path.abspath(inspect.getfile(inspect.currentframe()))) + '/runsolver','./chained','5'])
