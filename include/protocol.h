/*
 * File:   protocol.h
 * Author: wolfi
 *
 * Created on 29. Dezember 2023, 13:48
 */

#ifndef PROTOCOL_H
#define	PROTOCOL_H

#ifdef	__cplusplus
extern "C" {
#endif

#define SOH (0x01)
#define EOT (0x04)

#define CMD_NOP                     0x00

#define CMD_ENTER_BOOTLOADER        0x01

#define CMD_PROGRAM                 0x02

#define CMD_CALC_CHECKSUM           0x03

#define CMD_REBOOT                  0x04

#define CMD_READ_SIGNATURE          0x05

#define CMD_READ_EEPROM             0x06

#define CMD_WRITE_EEPROM            0x07

#define CMD_BOOT_END                0x0f
#define CMD_BOOT_MASK               0x0f


#define RSP_OK                      0xff
#define RSP_DONE                    0xfe
#define RSP_OK_BOOTLOADER           0xfd
#define RSP_NOT_IMPLEMENTED         0xfc
#define RSP_ERR_CHECK               0xfb
#define RSP_ERR                     0x00

#define REBOOT_WRITE_CRC            0x01
#define REBOOT_NORMAL               0x00

#define EE_CRCSUM                  (E2END-1)

#ifndef __ASSEMBLER__
  typedef int(*const app_t) (void);

  typedef struct {
    uint8_t soh;
    uint8_t cmd;

    union {
      uint8_t bData;
      uint16_t wData;
      uint32_t dwData;
      uint8_t data[sizeof (uint32_t)];

      struct {
        uint8_t memData;
        uint16_t address;
      };

      struct {
        uint8_t rebootType;
        uint16_t crc16;
      };
      uint8_t rawBuffer[13];
    };
    uint8_t eot;
  } CommandRecord;
#endif

#define MASK_TWI      0x10
#define MASK_SPI      0x20
#define MASK_ONE      0x40
#define MASK_WRITE    0x01
#define MASK_READ     0x02
#define MASK_CONFIG   0x04
#define MASK_SS       0x08

#define CH_TWI_WRITE  MASK_TWI | MASK_WRITE
#define CH_TWI_READ   MASK_TWI | MASK_READ
#define CH_TWI_CONFIG MASK_TWI | MASK_CONFIG
#define CH_SPI_WRITE  MASK_SPI | MASK_WRITE
#define CH_SPI_CONFIG MASK_SPI | MASK_CONFIG
#define CH_ONE_WRITE  MASK_ONE | MASK_WRITE
#define CH_ONE_READ   MASK_ONE | MASK_READ
#define CH_ONE_CONFIG MASK_ONE | MASK_CONFIG

#ifndef __ASSEMBLER__

  typedef struct {
    uint8_t soh;
    uint8_t flags;
    uint8_t numBytes;
  } DataHeader;

  typedef struct {
    DataHeader header;
  } TWIConfig;
#endif

#define SPI_SPEED_2    0x04
#define SPI_SPEED_4    0x00
#define SPI_SPEED_8    0x05
#define SPI_SPEED_16   0x01
#define SPI_SPEED_32   0x06
#define SPI_SPEED_64   0x02
#define SPI_SPEED_128  0x03
#define SPI_MASK_SPEED_DOUBLE 0x04
#define SPI_MASK_SPEED 0x03
#define SPI_SS_CONTROL_OFF 0x0
#define SPI_SS_CONTROL_HI 0x01
#define SPI_SS_CONTROL_LO 0x02

#ifndef __ASSEMBLER__

  typedef struct {
    DataHeader header;
    uint8_t ckSpeed : 3;
    uint8_t mode : 2;
    uint8_t lsbFirst : 1;
    uint8_t ssControl : 2;
    uint8_t eot;
  } SPIConfig;

  typedef struct {
    DataHeader header;

  } ONEConfig;
#endif

#ifdef	__cplusplus
}
#endif

#endif	/* PROTOCOL_H */

