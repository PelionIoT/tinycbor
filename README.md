# tinycbor

## Build the code with mbed-od GCC_ARM
- Create a new mbed project with mbed-os: `mbed new .`
- Build the app: `mbed compile -t GCC_ARM -m K64F`
- The binary file wil be BUILD/K64F/GCC_ARM/tinycbor.bin
- Open teraterm or putty and configre it to 9600 baud rate. Then reset the device.

## main.cpp
* The file was created to evaluate the performance of the tinycbor repository.
* For activating the run time memory stats used for the evaluation, clone git@github.com:nuket/mbed-memory-status.git and follow the README.
* IMPORTANT: When analyzing stack usage make sure that you do not use printf prior to any output of the stat functions. 
 printf uses a HUGE amount of stack, much more than theses actual tests. the stats lib outputs the stats with direct calls to mbed-os serial_api.h.

* This main contains 5 different tests for evaluating performance. They are run in separate threads to make the memory analysis simpler.
* Note that The code is not portable and builds only for mbed-os.
* These tests are scenarios similar to our CBOR usage in FCC and SDA, and therefore is good reference code on how to use the tinycbor library.

## Building and running the built in tests (linux)
* Install qmake `sudo apt-get install qt5-default` (Should add to docker if we wish to build them on regular basis without porting the build). 
* Build the lib: `make`
* cd into tests and build them `make`
* from tests directory cd into whichever the directory that contains the test you wish to run, and run the binary file.
* Note that in the encoder test there some segmentation fault on data structure of QTEST framework in test 70.