
#include <avr/io.h>
#include <assert.h>
#include <stdbool.h>
#include <util/crc16.h>
#include <avr/eeprom.h>
#include <avr/boot.h>
#include <avr/interrupt.h>
#include <string.h>
#include "init.h"
#include "protocol.h"
#include "communication.h"

#ifdef _AVR_IOM8_H_

FUSES = {
  .high = (FUSE_SPIEN & BOOTEND_FUSE & FUSE_BOOTRST),
  .low = (FUSE_SUT0 & FUSE_CKSEL3 & FUSE_CKSEL2 & FUSE_CKSEL1 & FUSE_CKSEL0)
};

#  define readSignature(command) \
      (command)->dwData = 0x1e930700;

#else
#  error unexpected device
#endif

/* Interface function prototypes */
static bool is_bootloader_requested(void);
static bool processCommand(CommandRecord* command);

static uint32_t address;
static uint8_t buffer[SPM_PAGESIZE / 2];
static uint8_t offset;

/*
 * Main boot function
 * Put in the constructors section (.ctors) to save Flash.
 * Naked attribute used since function prologue and epilogue is unused
 */
__attribute__((naked)) __attribute__((section(".ctors"))) int main(void)
{
  asm volatile(
      "clr r1\n\t"
      "out 0x3f, r1\n\t"
      "ldi r28,%0\n\t"
      "ldi r29,%1\n\t"
      "out 0x3e,r29\n\t"
      "out 0x3d,r28\n\t"
      :
      :
      "M"(((uint8_t) (RAMEND))),
      "M"(((uint8_t) (RAMEND >> 8))));


  while (true) {
    if (!is_bootloader_requested()) {
      app_t app = (app_t) (APPLICATION_START);
      app();
    }
    CommandRecord command;
    bool endCommandLoop = false;
    /* Initialize communication interface */
    initComm();
    memset(buffer, 0, sizeof (buffer));
    offset = 0;
    address = 0;

    while (!endCommandLoop) {
      memset(&command, 0, sizeof (command));
      readComm(sizeof (command), &command);
      if (command.soh == SOH && command.eot == EOT) {
        endCommandLoop = processCommand(&command);
      }
    }
  }
}

static uint16_t calcProgamChecksum()
{
  const __flash uint8_t* program = NULL;
  uint16_t result = 0;
  for (uint16_t addr = 0; addr < MAPPED_APPLICATION_SIZE; ++addr) {
    result = _crc16_update(result, program[addr]);
  }
  return result;
}

/*
 * Boot access request function
 */
static bool is_bootloader_requested(void)
{
  uint16_t calucaltedSum = calcProgamChecksum();
  uint16_t storedSum = eeprom_read_word((const uint16_t *) (EE_CRCSUM));
  return calucaltedSum != storedSum;
}

static void boot_program_page(uint32_t page, uint8_t *buf)
{
  uint16_t i;
  uint8_t sreg;
  // Disable interrupts.
  sreg = SREG;
  cli();
  eeprom_busy_wait();
  boot_page_erase(page);
  boot_spm_busy_wait(); // Wait until the memory is erased.
  for (i = 0; i < SPM_PAGESIZE; i += 2) {
    // Set up little-endian word.
    uint16_t w = *buf++;
    w += (*buf++) << 8;

    boot_page_fill(page + i, w);
  }
  boot_page_write(page); // Store buffer in flash page.
  boot_spm_busy_wait(); // Wait until the memory is written.
  // Reenable RWW-section again. We need this if we want to jump back
  // to the application after bootloading.
  boot_rww_enable();
  // Re-enable interrupts (if they were ever enabled).
  SREG = sreg;
}

static bool processCommand(CommandRecord* command)
{
  bool result = false;
  uint16_t checkSum;

  switch (command->cmd) {
    case CMD_NOP:
      command->cmd = RSP_OK;
      break;
    case CMD_CHECK_PROGRAM:
      checkSum = calcProgamChecksum();
      command->cmd = (checkSum == command->wData) ? RSP_OK : RSP_ERR;
      break;
    case CMD_REBOOT:
      checkSum = calcProgamChecksum();
      if (checkSum == command->wData) {
        result = true;
        command->cmd = RSP_OK;
      } else {
        result = false;
        command->cmd = RSP_ERR;
      }
      break;
    case CMD_ENTER_BOOTLOADER:
      address = 0;
      command->cmd = RSP_OK;
      break;
    case CMD_PROGRAM:
      ++address;
      buffer[offset++] = command->bData;
      if (offset == sizeof (buffer)) {
        boot_program_page(address / SPM_PAGESIZE, buffer);
      }
      if (address == MAPPED_APPLICATION_SIZE) {
        checkSum = calcProgamChecksum();
        eeprom_write_word((uint16_t *) (EE_CRCSUM), checkSum);
        command->cmd = RSP_DONE;
      } else {
        command->cmd = RSP_OK;
      }
      break;
    case CMD_READ_SIGNATURE:
      memset(&command->data, 0, sizeof (command->data));
      readSignature(command);
      command->cmd = RSP_OK;
      break;
    default:
      command->cmd = RSP_ERR;
  }
  writeComm(sizeof (CommandRecord), command);
  return result;
}