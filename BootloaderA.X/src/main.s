#include <avr/io.h>
#include "../../include/protocol.h"
#include "init.h"

#define CMD_BUFFERSIZE 16
#define OFFS_CMD 1
#define OFFS_EOT 15
#define OFFS_PARAM 2
    zero = 2
    one = 3
//    two = 4
    programmode = 5
    bytes_programmed_lo = 6
    bytes_programmed_hi = 7
    tmp1_lo = 8
    tmp1_hi = 9
    pagesize = 10
    endcommand = 11
    mapped_app_size_lo = 18
    mapped_app_size_hi = 19
    indicator_mask = 20
    pageoffset = 21


.global main

.extern buffer
.extern cmdBuffer

    .text
.org 0x0

main:
    eor zero, zero
    eor one,one
    inc one
    out _SFR_IO_ADDR(SREG), zero
    ldi r16, lo8(RAMEND)
    out _SFR_IO_ADDR(SPL), r16
    ldi r16, hi8(RAMEND)
    out _SFR_IO_ADDR(SPH), r16
    ldi mapped_app_size_lo,lo8(MAPPED_APPLICATION_SIZE)
    ldi mapped_app_size_hi,hi8(MAPPED_APPLICATION_SIZE)
    ldi r16,SPM_PAGESIZE
    mov pagesize,r16
    #ifdef INDICATOR_REG
    ldi indicator_mask,(_BV(INDICATOR_BIT1)|_BV(INDICATOR_BIT2))
    out _SFR_IO_ADDR(INDICATOR_REG(DDR)), indicator_mask
    out _SFR_IO_ADDR(INDICATOR_REG(PORT)), indicator_mask
    #endif

global_loop:
    eor programmode, programmode
    eor endcommand, endcommand
    rcall isBootloaderRequested
    breq enter_bootloader
    push zero
    push zero
    ret


enter_bootloader:
    rcall init_comm
command_loop:
    ldi YL, lo8(cmdBuffer)
    ldi YH, hi8(cmdBuffer)
    ldi r16, CMD_BUFFERSIZE
    rcall memclr
    rcall read_comm
    // checkCommand
    ld r16, Y
    cpi r16, SOH
    brne checkCommand_ret
    ldd r16, Y+OFFS_EOT
    cpi r16, EOT
checkCommand_ret:
    brne framing_error
    rcall processCommand
    cpse endcommand,zero
    rjmp global_loop
    rjmp command_loop

framing_error:
    ldi r16,CMD_BUFFERSIZE
    rcall memclr
    ldi r16,SOH
    st Y,r16
    ldi r16,EOT
    std Y+OFFS_EOT,r16
    ldi r16,RSP_ERR
    std Y+OFFS_CMD,r16
    rcall write_comm
    rjmp command_loop

// Yptr buffer to process
// clobber r16,r17ZH,ZL
processCommand:

    ldi r17,RSP_ERR
    ldd r16,Y+OFFS_CMD
    andi r16,CMD_BOOT_MASK

    cpi r16,CMD_NOP
    brne processCommand_test_enter_bootloader
    rcall processCommand_nop
    rjmp processCommand_ret
processCommand_test_enter_bootloader:
    cpi r16,CMD_ENTER_BOOTLOADER
    brne processCommand_test_program
    rcall processCommand_enter_bootloader
    rjmp processCommand_ret
processCommand_test_program:
    cpi r16,CMD_PROGRAM
    brne processCommand_test_checkProgram
    rcall processCommand_program
    rjmp processCommand_ret
processCommand_test_checkProgram:
    cpi r16,CMD_CALC_CHECKSUM
    brne processCommand_test_reboot
    rcall processCommand_calcChecksum
    rjmp processCommand_ret

processCommand_test_reboot:
    cpi r16,CMD_REBOOT
    brne processCommand_test_signature
    rcall processCommand_reboot
    rjmp processCommand_ret

processCommand_test_signature:
    cpi r16,CMD_READ_SIGNATURE
    brne processCommand_test_read_eeprom
    rcall processCommand_signature
    rjmp processCommand_ret

processCommand_test_read_eeprom:
    cpi r16,CMD_READ_EEPROM
    brne processCommand_test_write_eeprom
    rcall processCommand_read_eeprom
    rjmp processCommand_ret

processCommand_test_write_eeprom:
    cpi r16,CMD_WRITE_EEPROM
    brne processCommand_not_impl
    rcall processCommand_write_eeprom
    rjmp processCommand_ret

processCommand_not_impl:
    ldi r17,RSP_NOT_IMPLEMENTED

processCommand_ret:
    std Y+OFFS_CMD,r17
    rcall write_comm
    ret


processCommand_nop:
    rcall clrParamSection
    rcall calcProgramChecksum
    ldi r17,RSP_OK_BOOTLOADER
    std Y+OFFS_PARAM,r24
    std Y+(OFFS_PARAM+1),r25
    ret


processCommand_enter_bootloader:
    mov programmode,one
    mov pageoffset,pagesize
    ldi XL,lo8(buffer)
    ldi XH,hi8(buffer)
    mov bytes_programmed_lo,zero
    mov bytes_programmed_hi,zero
    rcall clrParamSection
    ldi r17,RSP_OK_BOOTLOADER
    std Y+OFFS_PARAM,mapped_app_size_lo
    std Y+(OFFS_PARAM+1),mapped_app_size_hi
    #ifdef INDICATOR_REG
    in r16, _SFR_IO_ADDR(INDICATOR_REG(PORT))
    andi r16,(~_BV(INDICATOR_BIT1))
    out _SFR_IO_ADDR(INDICATOR_REG(PORT)), r16
    #endif
    ret

processCommand_program:
    cpse programmode,one // check if in programm mode
    rjmp processCommand_ret
    ldi r17,RSP_OK_BOOTLOADER
    ldd r16,Y+OFFS_PARAM
    st X+,r16
    ldd r16,Y+(OFFS_PARAM+1)
    st X+,r16
    rcall clrParamSection
    inc bytes_programmed_lo
    adc bytes_programmed_hi,zero
    inc bytes_programmed_lo
    adc bytes_programmed_hi,zero
    std Y+OFFS_PARAM,bytes_programmed_lo
    std Y+(OFFS_PARAM+1),bytes_programmed_hi
    dec pageoffset
    dec pageoffset
    brne processCommand_program_ret
    ldi XL,lo8(buffer)
    ldi XH,hi8(buffer)
    // programm page and set r17 to correct return code
    rcall program_page
    cp bytes_programmed_lo,mapped_app_size_lo
    cpc bytes_programmed_hi,mapped_app_size_hi
    breq processCommand_program_fillChecksum // programming done, return checksum
    mov pageoffset,pagesize
processCommand_program_ret:
    ret

processCommand_program_fillChecksum:
    rcall calcProgramChecksum
    ldi r17,RSP_DONE
    std Y+OFFS_PARAM,r24
    std Y+(OFFS_PARAM+1),r25
    ret

processCommand_calcChecksum:
    rcall clrParamSection
    rcall calcProgramChecksum
    ldi r17,RSP_OK_BOOTLOADER
    std Y+OFFS_PARAM,r24
    std Y+(OFFS_PARAM+1),r25
    ret

processCommand_reboot:
    #ifdef INDICATOR_REG
    in r16,_SFR_IO_ADDR(INDICATOR_REG(PORT))
    ori r16,_BV(INDICATOR_BIT1)
    out _SFR_IO_ADDR(INDICATOR_REG(PORT)),r16
    #endif
    ldi r17,RSP_OK_BOOTLOADER
    ldd r16,Y+OFFS_PARAM
    cpi r16,REBOOT_WRITE_CRC
    brne processCommand_reboot_doReboot
    ldi XL,lo8(EE_CRCSUM)
    ldi XH,hi8(EE_CRCSUM)
    ldd r20,Y+(OFFS_PARAM+1)
    rcall writeEeprom
    adiw XL,1
    ldd r20,Y+(OFFS_PARAM+2)
    rcall writeEeprom
    rcall clrParamSection
processCommand_reboot_doReboot:
    mov endcommand,one
    ret

processCommand_signature:
    rcall clrParamSection
    ldi r16,0x1e
    std Y+OFFS_PARAM,r16
    ldi r16,0x93
    std Y+(OFFS_PARAM+1),r16
    ldi r16,0x07
    std Y+(OFFS_PARAM+2),r26
    ldi r17,RSP_OK_BOOTLOADER
    ret

processCommand_read_eeprom:
    rcall clrParamSection
    cpse programmode,zero // check if not in programm mode
    rjmp processCommand_ret
    ldd XL,Y+(OFFS_PARAM+1)
    ldd XH,Y+(OFFS_PARAM+2)
    rcall readEeprom
    std Y+OFFS_PARAM,r20
    ldi r17,RSP_OK_BOOTLOADER
    rjmp processCommand_ret

processCommand_write_eeprom:
    cpse programmode,zero // check if not in programm mode
    rjmp processCommand_ret
    ldd XL,Y+(OFFS_PARAM+1)
    ldd XH,Y+(OFFS_PARAM+2)
    ldd r20,Y+OFFS_PARAM
    rcall writeEeprom
    rcall clrParamSection
    ldi r17,RSP_OK_BOOTLOADER
    rjmp processCommand_ret



// X -> buffer
    // programm page and set r17 to correct return code
program_page:
    push XL
    push XH
    push ZL
    push ZH
    push tmp1_lo
    push r16
    #ifdef INDICATOR_REG
    in r16,_SFR_IO_ADDR(INDICATOR_REG(PORT))
    andi r16,~_BV(INDICATOR_BIT2)
    out _SFR_IO_ADDR(INDICATOR_REG(PORT)),r16
    #endif

    mov ZL,bytes_programmed_lo
    mov ZH,bytes_programmed_hi
    sub ZL,pagesize
    sbc ZH,0
    // page erase
    ldi r22,_BV(PGERS)|_BV(SPMEN)
    rcall do_spm
    // enable rww
    ldi r22, _BV(RWWSRE) | _BV(SPMEN)
    rcall do_spm
    // transfer buffer
    mov tmp1_lo,pagesize
program_page_fill_loop:
    ld r0,X+
    ld r1,X+
    ldi r22, _BV(SPMEN)
    rcall do_spm
    adiw ZL,2
    dec tmp1_lo
    dec tmp1_lo
    brne program_page_fill_loop
    // page write
    sub ZL,pagesize
    sbc ZH,0
    ldi r22,_BV(PGWRT)|_BV(SPMEN)
    rcall do_spm
    // enable rww
    ldi r22, _BV(RWWSRE) | _BV(SPMEN)
    rcall do_spm
    // checkpage
    sub XL,pagesize
    sbc XH,0
    mov tmp1_lo,pagesize
program_page_read_loop:
    lpm r0,Z+
    ld r1,X+
    cpse r0,r1
    brne program_page_error
    dec tmp1_lo
    brne program_page_read_loop
    ldi r17,RSP_OK_BOOTLOADER
program_page_exit:
    // enable rww
    ldi r22, _BV(RWWSRE) | _BV(SPMEN)
    rcall do_spm
    #ifdef INDICATOR_REG
    in r16,_SFR_IO_ADDR(INDICATOR_REG(PORT))
    ori r16,_BV(INDICATOR_BIT2)
    out _SFR_IO_ADDR(INDICATOR_REG(PORT)),r16
    #endif
    pop r16
    pop tmp1_lo
    pop ZH
    pop ZL
    pop XH
    pop XL
    ret
program_page_error:
    ldi r17,RSP_ERR_CHECK
    rjmp program_page_exit
do_spm:
    in r23,_SFR_IO_ADDR(SPMCR)
    sbrc r23, SPMEN
    rjmp do_spm
    in r23,_SFR_IO_ADDR(SREG)
    cli
do_spm_wait_ee:
    sbic _SFR_IO_ADDR(EECR), EEWE
    rjmp do_spm_wait_ee
    out _SFR_IO_ADDR(SPMCR),r22
    spm
    out _SFR_IO_ADDR(SREG),r23
    ret

clrParamSection:
    push YL
    push YH
    push r16
    ldi YL,lo8(cmdBuffer)
    ldi YH,hi8(cmdBuffer)
    adiw YL,OFFS_PARAM
    ldi r16,CMD_BUFFERSIZE-3
    rcall memclr
    pop r16
    pop YH
    pop YL
    ret

// Y address, r16 size
memclr:
    push r16
memclr_loop:
    ST Y+, zero
    dec r16
    brne memclr_loop
    sbiw YL, CMD_BUFFERSIZE
    pop r16
    ret

// z -> requested
isBootloaderRequested:
    push r20
    push r21
    push XL
    push XH
    push r24
    push r25

    ldi XL,lo8(EE_CRCSUM)
    ldi XH,hi8(EE_CRCSUM)
    ldi r24,0xff
    ldi r25,0xff
    rcall readEeprom
    adiw XL,1
    mov r21,r20
    rcall readEeprom
    cp r20,r25
    cpc r21,r24
    breq isBootloaderRequested_return

    rcall calcProgramChecksum

    cp r20,r24
    cpc r21,r25
    breq isBootloaderRequested_return
    sez

isBootloaderRequested_return:
    pop r25
    pop r24
    pop XH
    pop XL
    pop r21
    pop r20
    ret

// X -> address in
// r20 -> data out
readEeprom:
    sbic _SFR_IO_ADDR(EECR),EEWE
    rjmp readEeprom
    out _SFR_IO_ADDR(EEARL), XL
    out _SFR_IO_ADDR(EEARH), XH
    sbi _SFR_IO_ADDR(EECR),EERE
    in r20,_SFR_IO_ADDR(EEDR)
    ret

// X -> address
// r20 -> data
writeEeprom:
    sbic _SFR_IO_ADDR(EECR),EEWE
    rjmp writeEeprom
    out _SFR_IO_ADDR(EEARL), XL
    out _SFR_IO_ADDR(EEARH), XH
    sbi _SFR_IO_ADDR(EECR), EEWE
    out _SFR_IO_ADDR(EEDR), r20
    ret

// r24:r25 -> return
calcProgramChecksum:
    push r18
    push r19
    push r22

    ldi r18, lo8(MAPPED_APPLICATION_SIZE)
    ldi r19, hi8(MAPPED_APPLICATION_SIZE)
    mov r24, zero
    mov r25, zero
    mov ZL, zero
    mov ZH, zero
calcProgramChecksum_loop:
    lpm r22,Z+
    rcall crc16
    cp ZL,r18
    cpc ZH,r19
    brne calcProgramChecksum_loop

    pop r22
    pop r19
    pop r18
    ret


// r24:r25 -> crc in/out
// r22 -> update
// r16,r17 clobber
crc16:
    eor r24, r22
    ldi r16, 8
crc16_loop:
    clc
    ror r25
    ror r24
    brcc crc16_end_loop
    ldi r17,0xa0;
    eor r25,r17
    ldi r17,0x01
    eor r24,r17
crc16_end_loop:
    dec r16
    brne crc16_loop
    ret

    #define BAUD 115200
init_comm:
    #include <util/setbaud.h>
    ldi r16,UBRRH_VALUE
    out _SFR_IO_ADDR(UBRRH),r16
    ldi r16,UBRRL_VALUE
    out _SFR_IO_ADDR(UBRRL),r16
    #if USE_2X
    sbi _SFR_IO_ADDR(UCSRA),U2X
    #else
    cbi _SFR_IO_ADDR(UCSRA),U2X
    #endif
    ldi r16,_BV(RXEN)|_BV(TXEN)
    out _SFR_IO_ADDR(UCSRB),r16
    ldi r16,_BV(URSEL)|_BV(UCSZ1)|_BV(UCSZ0)
    out _SFR_IO_ADDR(UCSRC),r16
    ret

read_comm:
    ldi r16, CMD_BUFFERSIZE
readCommand_loop:
    sbis _SFR_IO_ADDR(UCSRA), RXC
    rjmp readCommand_loop
    in r17,_SFR_IO_ADDR(UDR)
    ST Y+,r17
    dec r16
    brne readCommand_loop
    sbiw YL, CMD_BUFFERSIZE
    ret

write_comm:
    ldi r16, CMD_BUFFERSIZE
sendResponse_loop:
    sbis _SFR_IO_ADDR(UCSRA),UDRE
    rjmp sendResponse_loop
    LD r17,Y+
    out _SFR_IO_ADDR(UDR),r17
    dec r16
    brne sendResponse_loop
    sbiw YL,CMD_BUFFERSIZE
    ret

