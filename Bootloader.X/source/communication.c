#include <avr/io.h>
#include <string.h>
#include <util/atomic.h>
#include "communication.h"
#include "protocol.h"

void initUsart()
{
#ifndef BAUD
#  warning BAUD not set using 38400 as default
#endif
#ifndef F_CPU
#  error F_CPU not set
#endif
#include <util/setbaud.h>
  UBRRH = UBRRH_VALUE;
  UBRRL = UBRRL_VALUE;
#if USE_2X
  UCSRA |= (1 << U2X);
#else
  UCSRA &= ~(1 << U2X);
#endif
  UCSRB = RXEN | TXEN;
  UCSRC = UCSZ1 | UCSZ2;
  DDRD |= _BV(DDD1);
}

void readUsart(uint8_t numBytes, uint8_t* buffer)
{
  while (numBytes-- > 0) {
    while (!(UCSRA & _BV(RXC)));
    *buffer = UDR;
    ++buffer;
  }
}

void writeUsart(uint8_t numBytes, uint8_t* buffer)
{
  while (numBytes-- > 0) {
    while (!(UCSRA & _BV(UDRE)));
    UDR = *buffer;
    ++buffer;
  }
}

void initTwi()
{

}

void readTwi(uint8_t numBytes, uint8_t* buffer)
{
}

void writeTwi(uint8_t numBytes, uint8_t* buffer)
{

}


