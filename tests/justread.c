#include <stdio.h>
#include "mmio.h"

#define TOBEREAD 0x2000

unsigned int justRead_ref() {
  return 230;
}

int main(void)
{
  uint32_t result, ref;

  // wait for peripheral to complete
  //while ((reg_read32(TOBEREAD) & 230) == 0) ;

  result = reg_read32(TOBEREAD);
  ref = justRead_ref();

  if (result != ref) {
    printf("Hardware result %d does not match reference value %d\n", result, ref);
    return 1;
  }
  printf("Correct, because result = %d and reference value also = %d\n\n", result, ref);
  return 0;
}
