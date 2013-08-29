#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>

int main()
{
  printf("Alex is a horndog-1!\n");
  fflush(stdout);
  system("/home/sjr/git/AutomaticConfiguratorLibrary/test-files/runsolver/sleepy");
}
