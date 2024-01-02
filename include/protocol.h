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

#define CMD_ENTER_BOOTLOADER        0x02

#define CMD_PROGRAM                 0x03

#define CMD_CHECK_PROGRAM           0x06

#define CMD_REBOOT                  0x07

#define CMD_READ_SIGNATURE          0x08

#define CMD_BOOT_END                0x0f


#define RSP_OK                      0xff
#define RSP_DONE                    0xfe
#define RSP_ERR                     0x00


  typedef int (*const app_t) (void);

#define EE_CRCSUM                  (0x0)

  typedef struct {
    uint8_t soh;
    uint8_t cmd;

    union {
      uint8_t bData;
      uint16_t wData;
      uint32_t dwData;
      uint8_t data[sizeof (uint32_t)];
    };
    uint8_t eot;
  } CommandRecord;

#ifdef	__cplusplus
}
#endif

#endif	/* PROTOCOL_H */

