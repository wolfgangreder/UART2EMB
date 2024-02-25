#include <avr/io.h>
#include <inttypes.h>
#include <stdbool.h>
#include "init.h"
#include "spi.h"

uint8_t ssControl;

void spiInit(SPIConfig* config)
{
  uint8_t spiTemp = _BV(SPE) | _BV(MSTR);
  if (config->lsbFirst) {
    spiTemp |= _BV(DORD);
  }
  switch (config->mode) {
    case 1: spiTemp |= _BV(CPHA);
      break;
    case 2: spiTemp |= _BV(CPOL);
      break;
    case 3: spiTemp |= _BV(CPHA) | _BV(CPOL);
      break;
  };
  if (config->ckSpeed & SPI_MASK_SPEED_DOUBLE) {
    SPSR |= _BV(SPI2X);
  }
  spiTemp |= (config->ckSpeed & SPI_MASK_SPEED);
  SPCR = spiTemp;
  ssControl = config->ssControl;
  SPI_PORT(DDR) |= _BV(SPI_MOSI) | _BV(SPI_SCK);
  switch (ssControl) {
    case SPI_SS_CONTROL_HI:
      SPI_PORT(DDR) |= _BV(SPI_SS);
      SPI_PORT(PORT) &= ~_BV(SPI_SS);
      break;
    case SPI_SS_CONTROL_LO:
      SPI_PORT(DDR) |= _BV(SPI_SS);
      SPI_PORT(PORT) |= _BV(SPI_SS);
      break;
  }
}

void spiSendReceive(uint8_t numBytes, uint8_t* buffer)
{
  if (numBytes == 0) {
    return;
  }
  if (ssControl != 0) {
    SPI_PORT(PORT) ^= _BV(SPI_SS);
  }
  while (!(SPSR & (1 << SPIF)));
  while (numBytes-- > 0) {
    SPDR = *buffer;
    while (!(SPSR & (1 << SPIF)));
    *buffer = SPDR;
    ++buffer;
  }
  if (ssControl != 0) {
    SPI_PORT(PORT) ^= _BV(SPI_SS);
  }
}