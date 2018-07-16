# tinycbor

* This project is a C implementation of Concise Binary Object Representation or [CBOR](https://datatracker.ietf.org/doc/rfc7049/).
* This implementation is a copy of [tinycbor](https://github.com/intel/tinycbor)
* This implementation is memory efficient. 

## Contributing

Go ahead, file issues, make pull requests.

## Building and Tests

The project has unit tests for Mbed OS and QTTests for Linux. 
They are compiled as part of internal infrastructure that isn't released.
One who would like to compile them, will need to use his own build system.

The library compiles for Mbed OS with GCC_ARM and ARMCC compilers.
Currently isn't compiled with IAR

## Restrictions

APIs can be broken in the future.
