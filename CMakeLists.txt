
# includes
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/tinycbor)

FILE(
	GLOB_RECURSE  
	tiny_cbor

	"${CMAKE_CURRENT_SOURCE_DIR}/src/*.*"
)

message ("*********************************************************************")
message ("tiny_cbor = [[${tiny_cbor}]]")
message ("*********************************************************************")

CREATE_LIBRARY(tiny_cbor "${tiny_cbor}" "")
#uncomment to compile tests
#ADDSUBDIRS()