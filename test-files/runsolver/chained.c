#include <unistd.h>
#include <stdio.h>
#include <errno.h>
#include <string.h>
#include <sys/types.h>


       
int main(int argc, char** argv)
{
  
   printf("My pid is %d\n", getpid());
  
  if(argc < 2)
  {
    printf("No number passed");
    return 210;
  } else
  {
    
    printf("My pid is %d\n", getpid());
    printf("My process group is %d\n", getpgrp());
    
    fflush(stdout);
    int pid;
  
    if(atoi(argv[1]) >= 0)
    {  
      
      if ((pid = fork()) < 0) {
          
      } else if (pid == 0) {      /* child */
        

         sprintf(argv[1],"%d",atoi(argv[1])-1);
         
         int retVal = execvp(argv[0],argv);
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
    
    usleep(50000000);
   
  }
}
