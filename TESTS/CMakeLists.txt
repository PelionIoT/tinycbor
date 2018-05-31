
# PAL's example code reference
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/../../mbed-client-pal/Examples/PlatformBSP/Include)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/../../mbed-client-pal/Test/Unitest/Includes)

# includes
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/../src)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/../../unity)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/runner/tiny_cbor_runner)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/tests)

set(TARGET_NAME "tiny_cbor_tests")
# tests includes
FILE(
	GLOB_RECURSE  
	tiny_cbor_tests_src

	"${CMAKE_CURRENT_SOURCE_DIR}/runner/tiny_cbor_main/*.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/runner/tiny_cbor_main/*.cpp"
	"${CMAKE_CURRENT_SOURCE_DIR}/runner/tiny_cbor_main/*.h"

	"${CMAKE_CURRENT_SOURCE_DIR}/runner/tiny_cbor_runner/*.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/runner/tiny_cbor_runner/*.cpp"
	"${CMAKE_CURRENT_SOURCE_DIR}/runner/tiny_cbor_runner/*.h"

	"${CMAKE_CURRENT_SOURCE_DIR}/tests/*.c"
	"${CMAKE_CURRENT_SOURCE_DIR}/tests/*.cpp"
	"${CMAKE_CURRENT_SOURCE_DIR}/tests/*.h"
	
	${CMAKE_CURRENT_SOURCE_DIR}/../../mbed-client-pal/Test/Unitest/TestRunner/pal_test${OS_BRAND}.c
)

message ("*********************************************************************")
message ("tiny_cbor_tests_src = [[${tiny_cbor_tests_src}]]")
message ("*********************************************************************")


CREATE_TEST_LIBRARY(${TARGET_NAME} "${tiny_cbor_tests_src}" "")

