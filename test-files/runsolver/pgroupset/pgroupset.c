#include <unistd.h>
#include <stdio.h>
#include <errno.h>
#include <string.h>

int main(int argc, char** argv)
{
  
  if(argc < 2)
  {
    printf("No executable found");
    return 210;
  } else
  {
    
    printf("My pid is %d\n", getpid());
    printf("My process group is %d\n", getpgrp());
    
    fflush(stdout);
    int retVal = execvp(argv[1],argv+1);
    int myErrNo = errno;
    if(retVal != -1)
    {
      //This shouldn't ever happen
      //as exec should only return -1 (if it succeed this function is dead)
      printf("Not sure how I got into this state %d", retVal);
      return 255; 
    } else
    {
      printf("Error occured while executing process (%s) : %s code: (%d)", argv[1], strerror(myErrNo), myErrNo);
      return myErrNo;
      
    }
  }
}
