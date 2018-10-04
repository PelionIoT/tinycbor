
# includes
ADD_GLOBALDIR(${CMAKE_CURRENT_SOURCE_DIR}/src)

FILE(
	GLOB_RECURSE  
	tiny_cbor

	"${CMAKE_CURRENT_SOURCE_DIR}/src/*.c"
)

message ("*********************************************************************")
message ("tiny_cbor = [[${tiny_cbor}]]")
message ("*********************************************************************")

CREATE_LIBRARY(tiny_cbor "${tiny_cbor}" "")

ADDSUBDIRS()