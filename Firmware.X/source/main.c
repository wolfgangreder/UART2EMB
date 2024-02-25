#include <avr/io.h>
#include <assert.h>
#include <stdbool.h>
#include <avr/interrupt.h>
#include "init.h"
#include "protocol.h"
#include "uart.h"
#include "spi.h"

uint8_t buffer[sizeof (DataHeader) + MAX_BUFFERSIZE + 1];
DataHeader* header = &buffer;
uint8_t* data = (&buffer) + sizeof (DataHeader);


void doSPIConfig(SPIConfig* config);
void doSPISendReceive();

int main()
{
  uartInit();
  sei();

  while (true) {
    uartRead(sizeof (DataHeader), (uint8_t*) & buffer);
    if (header->soh == SOH) {
      if (header->flags <= CMD_BOOT_END) {
        return 1; // goto bootloader!
      }
      uartRead(header->numBytes, data);
      if (data[header->numBytes] == EOT) {
        switch (header->flags) {
          case CH_SPI_WRITE:
            doSPISendReceive();
            break;
          case CH_SPI_CONFIG:
            doSPIConfig((SPIConfig*) & buffer);
            break;
          case CH_ONE_READ:
          case CH_ONE_WRITE:
          case CH_TWI_READ:
          case CH_TWI_WRITE:
          default:
            header->flags = RSP_ERR;
            header->numBytes = 0;
            data[0] = EOT;
        }
      } else {
        header->flags = RSP_ERR;
        header->numBytes = 0;
        data[0] = EOT;
      }
      uartWrite(sizeof (DataHeader) + 1, (uint8_t*) & buffer);
    }
  }
  return 0;
}

void doSPIConfig(SPIConfig* config)
{

  if ((config->eot == EOT) && (config->header.numBytes == (sizeof (SPIConfig) - sizeof (DataHeader) - 1))) {
    spiInit(config);
    config->header.flags = RSP_OK;
  } else {

    config->header.flags = RSP_ERR;
  }
  config->header.numBytes = 0;
  config->eot = EOT;
}

void doSPISendReceive()
{
  if (header->numBytes <= MAX_BUFFERSIZE && data[header->numBytes] == EOT) {
    spiSendReceive(header->numBytes, data);
    header->flags = RSP_OK;
  } else {
    header->flags = RSP_ERR;
  }
}
