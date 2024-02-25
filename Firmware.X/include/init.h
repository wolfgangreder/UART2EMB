/*
 * File:   init.h
 * Author: wolfi
 *
 * Created on 29. Dezember 2023, 14:20
 */

#ifndef INIT_H
#define	INIT_H

#define F_CPU (14745600)

#define INDICATOR_REG(reg) reg##D
#define INDICATOR_BIT 3

#include <avr/io.h>

#ifdef	__cplusplus
extern "C" {
#endif

#define BAUD (115200)


#ifdef _AVR_IOM8_H_

#define MAX_BUFFERSIZE  64
#define SPI_PORT(reg) reg##B
#define SPI_SS 2
#define SPI_MOSI 3
#define SPI_MISO 4
#define SPI_SCK 5

#else
#error unexpected device
#endif

#ifdef	__cplusplus
}
#endif

#endif	/* INIT_H */

