#include <avr/io.h>
#include "init.h"

.global boot_program_page_asm

    ; SPM_PAGESIZE is in byte!
    ;PAGESIZEB = SPM_PAGESIZE * 2
    spmcrval = 20
    looplo = 19
    loophi = 20
    temp1 = 18


.section .text

    #if SPM_PAGESIZE<=256
    ; r24:r25 -> flashAddress
    ; r22:r23 -> ramAddress
boot_program_page_asm:

    push r0
    push r28
    push r29 ; push Y
    push r30
    push r31 ; push Z
    #ifdef INDICATOR_REG
    in r0,_SFR_IO_ADDR(INDICATOR_REG(PORT))
    eor r0,(1<<INDICATOR_BIT)
    out _SFR_IO_ADDR(INDICATOR_REG(PORT)), r0
    #endif
; page erase
    ldi spmcrval, (1<<PGERS) | (1<<SPMEN)
    rcall Do_spm
; re-enable the RWW section
    ldi spmcrval, (1<<RWWSRE) | (1<<SPMEN)
    rcall Do_spm
; transfer data from RAM to Flash page buffer
    ldi looplo, lo8(SPM_PAGESIZE) ;init loop variable
    mov r28,r22 ; init Y ptr
    mov r29,r23 ; init Y ptr
    mov r30,r24 ; init Z ptr
    mov r31,r25 ; init Z ptr
    eor r24,r24
Wrloop:
    ld r0, Y+
    ld r1, Y+
    ldi spmcrval, (1<<SPMEN)
    rcall Do_spm
    adiw ZL, 2
    subi looplo, 2
    brne Wrloop

; execute page write
    subi ZL, lo8(SPM_PAGESIZE) ;restore pointer
    ldi spmcrval, (1<<PGWRT) | (1<<SPMEN)
    rcall Do_spm

    ; re-enable the RWW section
    ldi spmcrval, (1<<RWWSRE) | (1<<SPMEN)
    rcall Do_spm

 ; read back and check, optional
    ldi looplo, lo8(SPM_PAGESIZE) ;init loop variable
    subi YL, lo8(SPM_PAGESIZE) ;restore pointer
    sbci YH, hi8(SPM_PAGESIZE)
Rdloop:
    lpm r0, Z+
    ld r1, Y+
    cpse r0, r1
    rjmp Return
    subi looplo, 1 ;use subi for SPM_PAGESIZE<=256
    brne Rdloop
    ldi r24,1
 ; return to RWW section
; verify that RWW section is safe to read
Return:
    in temp1, _SFR_IO_ADDR(SPMCR)
    sbrs temp1, RWWSB ; If RWWSB is set, the RWW section is notready yet
    #ifdef INDICATOR_REG
    in r0,_SFR_IO_ADDR(INDICATOR_REG(PORT))
    eor r0,(1<<INDICATOR_BIT)
    out _SFR_IO_ADDR(INDICATOR_REG(PORT)), r0
    #endif
    pop r31 ; pop Z
    pop r30
    pop r29 ; pop Y
    pop r28
    pop r0
    ret

; re-enable the RWW section
    ldi spmcrval, (1<<RWWSRE) | (1<<SPMEN)
    rcall Do_spm
    rjmp Return

Do_spm:
; check for previous SPM complete
Wait_spm:
    in temp1, _SFR_IO_ADDR(SPMCR)
    sbrc temp1, SPMEN
    rjmp Wait_spm

    ; check that no EEPROM write access is present
Wait_ee:
    sbic _SFR_IO_ADDR(EECR), EEWE
    rjmp Wait_ee
; SPM timed sequence
    out _SFR_IO_ADDR(SPMCR), spmcrval
    spm
    ret


    #else


boot_program_page:
    push r0
    in r0
; page erase
    ldi spmcrval, (1<<PGERS) | (1<<SPMEN)
    rcall Do_spm
; re-enable the RWW section
    ldi spmcrval, (1<<RWWSRE) | (1<<SPMEN)
    rcall Do_spm
; transfer data from RAM to Flash page buffer
    ldi looplo, lo8(SPM_PAGESIZE)
 ;init loop variable
    ldi loophi, hi8(SPM_PAGESIZE) ;not required for SPM_PAGESIZE<=256
Wrloop:
    ld r0, Y+
    ld r1, Y+
    ldi spmcrval, (1<<SPMEN)
    rcall Do_spm
    adiw ZL, 2
    sbiw looplo, 2
 ;use subi for SPM_PAGESIZE<=256
    brne Wrloop
; execute page write
    subi ZL, lo8(SPM_PAGESIZE)
 ;restore pointer
    sbci ZH, hi8(SPM_PAGESIZE)
 ;not required for SPM_PAGESIZE<=256
    ldi spmcrval, (1<<PGWRT) | (1<<SPMEN)
    rcall Do_spm
; re-enable the RWW section
    ldi spmcrval, (1<<RWWSRE) | (1<<SPMEN)
    rcall Do_spm
; read back and check, optional
    ldi looplo, lo8(SPM_PAGESIZE)
 ;init loop variable
    ldi loophi, hi8(SPM_PAGESIZE) ;not required for SPM_PAGESIZE<=256
    subi YL, lo8(SPM_PAGESIZE)
 ;restore pointer
    sbci YH, hi8(SPM_PAGESIZE)
Rdloop:
    lpm r0, Z+
    ld r1, Y+
    cpse r0, r1
    rjmp Return
    sbiw looplo, 1
 ;use subi for SPM_PAGESIZE<=256
    brne Rdloop
 ; return to RWW section
; verify that RWW section is safe to read
Return:
    in temp1, _SFR_IO_ADDR(SPMCR)
    sbrs temp1, RWWSB
 ; If RWWSB is set, the RWW section is notready yet
    pop r0
    ret
; re-enable the RWW section
    ldi spmcrval, (1<<RWWSRE) | (1<<SPMEN)
    rcall Do_spm
    rjmp Return
Do_spm:
; check for previous SPM complete
Wait_spm:
    in temp1, _SFR_IO_ADDR(SPMCR)
    sbrc temp1, SPMEN
    rjmp Wait_spm
; input: spmcrval determines SPM action
; disable interrupts if enabled, store status
    in temp2, _SFR_IO_ADDR(SREG)
    cli
; check that no EEPROM write access is present
Wait_ee:
    sbic _SFR_IO_ADDR(EECR), EEWE
    rjmp Wait_ee
; SPM timed sequence
    out _SFR_IO_ADDR(SPMCR), spmcrval
    spm
; restore SREG (to enable interrupts if originally enabled)
    out _SFR_IO_ADDR(SREG), temp2
    ret
#endif