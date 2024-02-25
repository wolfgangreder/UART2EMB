/*
 * File:   spi.h
 * Author: wolfi
 *
 * Created on 2. Januar 2024, 16:55
 */

#ifndef SPI_H
#define	SPI_H

#include "protocol.h"

#ifdef	__cplusplus
extern "C" {
#endif

  extern void spiInit(SPIConfig* config);
  extern void spiSendReceive(uint8_t numBytes, uint8_t* buffer);

#ifdef	__cplusplus
}
#endif

#endif	/* SPI_H */

