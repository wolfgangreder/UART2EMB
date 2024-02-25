/*
 * File:   init.h
 * Author: wolfi
 *
 * Created on 29. Dezember 2023, 14:20
 */

#ifndef INIT_H
#define	INIT_H

#include <avr/io.h>

//#define F_CPU (7372800)
#define F_CPU (14745600)

// drive indicator led
//#ifndef INDICATOR_REG
#define INDICATOR_REG(reg) reg##D
#define INDICATOR_BIT1 2
#define INDICATOR_BIT2 3
//#endif

// implement CMD_EEPROM_READ and CDM_EEPROM_WRITE
#ifndef CMD_EEPROM
#define CMD_EEPROM 1
#endif

// implement CMD_SIGNATURE_READ
#ifndef CMD_SIGNATURE
#define CMD_SIGNATURE 1
#endif

// implement CMD_CHECK_PROGRAM
#ifndef CMD_CHECKPROGRAM
#define CMD_CHECKPROGRAM 1
#endif

// use paranoid initializations
#ifndef PARANOID_INIT
#define PARANOID_INIT 1
#endif

// check page after programming
#ifndef CHECK_PAGE
#define CHECK_PAGE 1
#endif

#ifdef	__cplusplus
extern "C" {
#endif

#if defined PGM_USART && defined PGM_TWI
#error define only one of PGM_USART or PGM_TWI
#endif
#ifdef PGM_USART
  /* Baud rate configuration */
#define BAUD 115200

#endif

#define FLASH_SIZE  (FLASHEND+1)

  // initializations for ATmega8
#ifdef _AVR_IOM8_H_

#ifndef BOOT_PAGES
#define BOOT_PAGES  16
#endif
#if BOOT_PAGES == 4
  // 4 pages
  // .text=0xf80
#define BOOTEND_FUSE               (0xff)
#define MAPPED_APPLICATION_SIZE  0x1f00
#elif BOOT_PAGES == 8
  // 8 pages
  // .text=0xf00
#define BOOTEND_FUSE               (FUSE_BOOTSZ0)
#elif BOOT_PAGES == 16
  // 16 pages
  // .text=0xe00
#define BOOTEND_FUSE               (FUSE_BOOTSZ1)
#elif BOOT_PAGES == 32
  // 32 pages
  // .text=0c00
#define BOOTEND_FUSE               (FUSE_BOOTSZ1 & FUSE_BOOTSZ0)
#else
#error undefined BOOT_PAGES
#endif

#define BOOT_SIZE (BOOT_PAGES*SPM_PAGESIZE)
#define APPLICATION_START          (0x0000)
#define MAPPED_APPLICATION_START   (0x0000)
#define MAPPED_APPLICATION_SIZE   (FLASH_SIZE-BOOT_SIZE)

#else
#error unexpected device
#endif

#if !defined BOOT_SIZE || !defined APPLICATION_START || !defined MAPPED_APPLICATION_START || !defined MAPPED_APPLICATION_SIZE
#error not all memory settings defined!
#endif

#ifdef	__cplusplus
}
#endif


#endif	/* INIT_H */

