/*
 * File:   uart.h
 * Author: wolfi
 *
 * Created on 2. Januar 2024, 16:40
 */

#ifndef UART_H
#define	UART_H

#ifdef	__cplusplus
extern "C" {
#endif

  extern void uartInit();
  extern void uartRead(uint8_t numBytes, uint8_t* buffer);
  extern void uartWrite(uint8_t numBytes, uint8_t* buffer);



#ifdef	__cplusplus
}
#endif

#endif	/* UART_H */

