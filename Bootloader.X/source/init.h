/*
 * File:   init.h
 * Author: wolfi
 *
 * Created on 29. Dezember 2023, 14:20
 */

#ifndef INIT_H
#define	INIT_H

#define F_CPU (7372800)

#include <avr/io.h>

#ifdef	__cplusplus
extern "C" {
#endif

#if defined PGM_USART && defined PGM_TWI
#error define only one of PGM_USART or PGM_TWI
#endif
#if defined PGM_USART
  /* Baud rate configuration */
#define BAUD (38400)

#elif defined PGM_TWI
#error not available yet
#else
#error define one of PGM_USART or PGM_TWI
#endif

#define FLASH_SIZE  (FLASHEND+1)

#ifdef _AVR_IOM8_H_

#define BOOT_PAGES  16
#if BOOT_PAGES == 4
  // 4 pages (12328 words)
  // .text=0xf80
#define BOOTEND_FUSE               (0xff)
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
  // .text=0xc00
#define BOOTEND_FUSE               (FUSE_BOOTSZ1 & FUSE_BOOTSZ0)
#else
#error undefined BOOT_PAGES
#endif

#define BOOT_SIZE (BOOT_PAGES*SPM_PAGESIZE)
#define APPLICATION_START          (0x0000)
#define MAPPED_APPLICATION_START   (0x0000)
#define MAPPED_APPLICATION_SIZE    (FLASH_SIZE - BOOT_SIZE)

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

