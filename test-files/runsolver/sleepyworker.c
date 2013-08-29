#include <unistd.h>
#include <stdio.h>

int main()
{

  int i,j;
  for(i=0; i < 20000000; i++)
  {
    usleep(1000500);
    for(j = 0; j < 500000; j++);
    printf("Alex is a horndog!\n");
    //fflush(stdout);
  }
  
  
  
}