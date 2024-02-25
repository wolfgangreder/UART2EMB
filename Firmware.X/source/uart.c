#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/atomic.h>
#include <stdbool.h>
#include "init.h"
#include "uart.h"

volatile uint8_t readOffset;
volatile uint8_t writeOffset;
uint8_t ringBuffer[16];

#define incrementPointer(ptr) (ptr) = (++(ptr)) & 0x0f;

void uartInit()
{
#ifndef BAUD
#  error BAUD not set
#endif
#ifndef F_CPU
#  error F_CPU not set
#endif
#include <util/setbaud.h>
  UBRRH = UBRRH_VALUE;
  UBRRL = UBRRL_VALUE;
#if USE_2X
  UCSRA |= _BV(U2X);
#else
  UCSRA &= ~_BV(U2X);
#endif
  UCSRB = _BV(RXEN) | _BV(TXEN) | _BV(RXCIE);
  UCSRC = _BV(URSEL) | _BV(UCSZ1) | _BV(UCSZ0);
  readOffset = 0;
  writeOffset = 0;
}

bool peekByte(uint8_t* buffer)
{

  ATOMIC_BLOCK(ATOMIC_RESTORESTATE)
  {
    if (readOffset != writeOffset) {
      *buffer = ringBuffer[readOffset];
      incrementPointer(readOffset);
      return true;
    }
  }
  return false;
}

void uartRead(uint8_t numBytes, uint8_t* buffer)
{
  while (numBytes-- > 0) {
    if (peekByte(buffer)) {
      --numBytes;
      ++buffer;
    }
  }
}

void uartWrite(uint8_t numBytes, uint8_t* buffer)
{
  while (numBytes-- > 0) {
    while (!(UCSRA & _BV(UDRE)));
    UDR = *buffer;
    ++buffer;
  }
}

ISR(USART_RXC_vect)
{
  ringBuffer[writeOffset] = UDR;
  incrementPointer(writeOffset);
}
