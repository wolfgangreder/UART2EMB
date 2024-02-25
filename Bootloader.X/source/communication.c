#include <avr/io.h>
#include <string.h>
#include <util/atomic.h>
#include "communication.h"
#include "protocol.h"

#if PGM_USART == 1

void initUsart()
{
#  ifndef BAUD
#    warning BAUD not set using 38400 as default
#  endif
#  ifndef F_CPU
#    error F_CPU not set
#  endif
#  include <util/setbaud.h>
  UBRRH = UBRRH_VALUE;
  UBRRL = UBRRL_VALUE;
#  if USE_2X
  UCSRA |= _BV(U2X);
#  else
  UCSRA &= ~_BV(U2X);
#  endif
  UCSRB = _BV(RXEN) | _BV(TXEN);
  UCSRC = _BV(URSEL) | _BV(UCSZ1) | _BV(UCSZ0);
}

void readUsart(CommandRecord* record)
{
  uint8_t* buffer = (uint8_t*) record;
  for (uint8_t i = 0; i<sizeof (CommandRecord); ++i) {
    while (!(UCSRA & _BV(RXC)));
    buffer[i] = UDR;
  }
}

void writeUsart(CommandRecord* record)
{
  uint8_t* buffer = (uint8_t*) record;
  for (uint8_t i = 0; i<sizeof (CommandRecord); ++i) {
    while (!(UCSRA & _BV(UDRE)));
    UDR = buffer[i];
  }
}
#endif

#if PGM_TWI == 1

void initTwi()
{

}

void readTwi(CommandRecord* buffer)
{
}

void writeTwi(CommandRecord* buffer)
{

}

#endif

#if PGM_DUMMY == 1

void initDummy()
{
}

void readDummy(CommandRecord* buffer)
{
  buffer->soh = SOH;
  buffer->cmd = CMD_ENTER_BOOTLOADER;
  buffer->wData = 0x1234;
  buffer->eot = EOT;
}

void writeDummy(CommandRecord* buffer)
{

}

#endif