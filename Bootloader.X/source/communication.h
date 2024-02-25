/*
 * File:   communication.h
 * Author: wolfi
 *
 * Created on 29. Dezember 2023, 14:59
 */

#ifndef COMMUNICATION_H
#define	COMMUNICATION_H

#include "init.h"
#include "protocol.h"

#ifdef	__cplusplus
extern "C" {
#endif

  extern void initUsart();
  extern void readUsart(CommandRecord* record);
  extern void writeUsart(CommandRecord* record);

  extern void initTwi();
  extern void readTwi(CommandRecord* record);
  extern void writeTwi(CommandRecord* record);

  extern void initDummy();
  extern void readDummy(CommandRecord* record);
  extern void writeDummy(CommandRecord* record);


#if defined PGM_USART

#define initComm initUsart
#define readComm(buffer) readUsart((buffer))
#define writeComm(buffer) writeUsart((buffer))

#elif defined PGM_TWI

#define initComm initTwi
#define readComm(buffer) readTwi((buffer))
#define writeComm(buffer) writeTwi((buffer))

#elif defined PGM_DUMMY
#define initComm initDummy
#define readComm(buffer) readDummy((buffer))
#define writeComm(buffer) writeDummy((buffer))

#endif

#ifdef	__cplusplus
}
#endif

#endif	/* COMMUNICATION_H */

