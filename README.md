# tinycbor

* This project is a C implementation of The Concise Binary Object Representation [CBOR](https://datatracker.ietf.org/doc/rfc7049/).
* This implementation is a copy of [tinycbor](https://github.com/intel/tinycbor).
* This implementation is memory-efficient. 

## Contributing

Go ahead, file issues, make pull requests.

## Building and Tests

The project includes unit tests for Mbed OS and QTTests for Linux. 
They are compiled as a part of internal infrastructure that isn't released.
If you want to compile them yourself, you need to use your own build system.

The library compiles for Mbed OS with GCC_ARM and ARMCC compilers.
Currently, you cannot compile with IAR.

## Restrictions

APIs can be broken in the future.

# NOTE
The development was migrated to a folder in https://github.com/ARMmbed/mbed-cloud-client-internal
