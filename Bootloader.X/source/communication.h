/*
 * File:   communication.h
 * Author: wolfi
 *
 * Created on 29. Dezember 2023, 14:59
 */

#ifndef COMMUNICATION_H
#define	COMMUNICATION_H

#include "init.h"

#ifdef	__cplusplus
extern "C" {
#endif

  extern void initUsart();
  extern void readUsart(uint8_t numBytes, uint8_t* buffer);
  extern void writeUsart(uint8_t numBytes, uint8_t* buffer);

  extern void initTwi();
  extern void readTwi(uint8_t numBytes, uint8_t* buffer);
  extern void writeTwi(uint8_t numBytes, uint8_t* buffer);


#if defined PGM_USART

#define initComm initUsart
#define readComm(numBytes, buffer) readUsart((numBytes),((uint8_t*)(buffer)))
#define writeComm(numBytes, buffer) writeUsart((numBytes),((uint8_t*)(buffer)))

#elif defined PGM_TWI

#define initComm initTwi
#define readComm(numBytes, buffer) readTwi((numBytes),((uint8_t*)(buffer)))
#define writeComm(numBytes, buffer) writeTwi((numBytes),((uint8_t*)(buffer)))

#endif

#ifdef	__cplusplus
}
#endif

#endif	/* COMMUNICATION_H */

