
#include <avr/io.h>
#include "../../include/protocol.h"
#include "init.h"

FUSES = {
  .high = (FUSE_SPIEN & BOOTEND_FUSE & FUSE_BOOTRST),
  .low = (FUSE_SUT0 & FUSE_CKSEL3 & FUSE_CKSEL2 & FUSE_CKSEL1 & FUSE_CKSEL0),
};

uint8_t buffer[SPM_PAGESIZE];
uint8_t cmdBuffer[sizeof (CommandRecord)];