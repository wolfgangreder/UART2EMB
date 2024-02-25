
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
#include <avr/fuse.h>
#ifdef _AVR_IOM8_H_

FUSES = {
  .high = (FUSE_SPIEN & BOOTEND_FUSE & FUSE_BOOTRST),
  .low = (FUSE_SUT0 & FUSE_CKSEL3 & FUSE_CKSEL2 & FUSE_CKSEL1 & FUSE_CKSEL0),
};

#  define readSignature(command) \
      (command).dwData = 0x1e9307;

#else
#  error unexpected device
#endif
uint16_t theValue;
/* Interface function prototypes */
static bool is_bootloader_requested(void);
static bool boot_program_page(uint16_t flashAddress);
static uint16_t calcProgramChecksum();

static inline bool isPageEnd(uint16_t address)
{
  return !(address & (SPM_PAGESIZE - 1));
}

static inline uint16_t getPageAddress(uint16_t a)
{
  return a >> 5;
}

static void testCrc()
{
  uint16_t result = 0xcafe;
  uint8_t data = 0xaf;
  theValue = _crc16_update(result, data);
}

/*
 * Main boot function
 * Put in the constructors section (.ctors) to save Flash.
 * Naked attribute used since function prologue and epilogue is unused
 */
__attribute__((naked)) __attribute__((section(".ctors")))
int main(void)
{
  uint16_t address;
  bool success = true;

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
#ifdef INDICATOR_REG
  INDICATOR_REG(DDR) |= _BV(INDICATOR_BIT1) | _BV(INDICATOR_BIT2);
  INDICATOR_REG(PORT) |= _BV(INDICATOR_BIT1) | _BV(INDICATOR_BIT2);
#endif
  testCrc();
  for (;;) {
    if (!is_bootloader_requested() || theValue == 1) {
      app_t app = (app_t) (APPLICATION_START);
      app();
    }
    cli();
    CommandRecord command;
    bool endCommandLoop = false;
    /* Initialize communication interface */
    initComm();
    address = 0;

    do {
#ifdef PARANOID_INIT
      memset(&command, 0, sizeof (command));
#endif
      readComm(&command);
#ifdef PARANOID_INIT
      if (command.soh == SOH && command.eot == EOT) {
#endif

        switch (command.cmd) {
          case CMD_NOP:
            command.cmd = RSP_OK_BOOTLOADER;
            command.dwData = 0;
            command.wData = calcProgramChecksum();
            break;
#ifdef CMD_CHECKPROGRAM
          case CMD_CHECK_PROGRAM:
            command.cmd = (calcProgramChecksum() == command.wData) ? RSP_OK_BOOTLOADER : RSP_ERR;
            break;
#endif
          case CMD_REBOOT:
            endCommandLoop = true;
            if (command.rebootType == REBOOT_WRITE_CRC) {
              eeprom_write_word((uint16_t *) (EE_CRCSUM), command.crc16);
            }
            command.cmd = RSP_OK_BOOTLOADER;
#ifdef INDICATOR_REG
            INDICATOR_REG(PORT) |= _BV(INDICATOR_BIT1);
            INDICATOR_REG(PORT) |= _BV(INDICATOR_BIT2);
#endif
            break;
          case CMD_ENTER_BOOTLOADER:
            address = 0;
            command.cmd = RSP_OK_BOOTLOADER;
            command.wData = MAPPED_APPLICATION_SIZE;
#ifdef INDICATOR_REG
            INDICATOR_REG(PORT) &= ~_BV(INDICATOR_BIT1);
            INDICATOR_REG(PORT) &= ~_BV(INDICATOR_BIT2);
#endif
            break;
          case CMD_PROGRAM:
            boot_page_fill(address, command.wData);
            address += 2;
            if (isPageEnd(address)) {
              success = boot_program_page(getPageAddress(address));
            }
            if (address >= MAPPED_APPLICATION_SIZE) {
              command.wData = calcProgramChecksum();
              command.cmd = RSP_DONE;
            } else {
              command.wData = address;
              command.cmd = success ? RSP_OK_BOOTLOADER : RSP_ERR;
            }
            break;
#ifdef CMD_SIGNATURE
          case CMD_READ_SIGNATURE:
            memset(&command.data, 0, sizeof (command.data));
            readSignature(command);
            command.cmd = RSP_OK_BOOTLOADER;
            break;
#endif
#ifdef CMD_EEPROM
          case CMD_READ_EEPROM:
            if (command.address >= EE_CRCSUM) {
              command.cmd = RSP_ERR;
            } else {
              command.cmd = RSP_OK_BOOTLOADER;
              command.memData = eeprom_read_byte((uint8_t*) command.address);
            }
            break;
          case CMD_WRITE_EEPROM:
            if (command.address >= EE_CRCSUM) {
              command.cmd = RSP_ERR;
            } else {
              command.cmd = RSP_OK_BOOTLOADER;
              eeprom_write_byte((uint8_t*) command.address, command.memData);
            }
            break;
#endif
          default:
            command.cmd = RSP_NOT_IMPLEMENTED;
        }
        writeComm(&command);
#ifdef PARANOID_INIT
      }
#endif
    } while (!endCommandLoop);
  }
}

static uint16_t calcProgramChecksum()
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
  uint16_t storedSum = eeprom_read_word((const uint16_t *) (EE_CRCSUM));
  if (storedSum == 0xffff) {
    return true;
  }
  uint16_t calucaltedSum = calcProgramChecksum();
  return calucaltedSum != storedSum;
}

static bool boot_program_page(uint16_t address)
{
#ifdef INDICATOR_REG
  INDICATOR_REG(PORT) ^= _BV(INDICATOR_BIT2);
#endif
  // page_erase
  boot_spm_busy_wait();
  boot_page_erase(address);
  // enable rww
  boot_rww_enable();
  // program page
  boot_page_write(address);
  // enable rww
  boot_rww_enable();
#if CHECK_PAGE == 1
  // check page
#endif
  return true;
}
