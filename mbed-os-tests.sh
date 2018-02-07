# ----------------------------------------------------------------------------
# Copyright 2016-2017 ARM Ltd.
#  
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#  
#     http://www.apache.org/licenses/LICENSE-2.0
#  
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ----------------------------------------------------------------------------
# source (!) this file while at SPV tree top - DO NOT run it
export TINY_CBOR_TOP=`pwd`

CACHE_PATH=/opt/scratch/$(whoami)/tcbor-cache

# Create new mbed project (if one does not exist).
# Creating the project is important so that we use local config and not global.
if [ ! -f .mbed ]; then
	mbed new .

	# Enable mbed-os local CACHE
	mbed config CACHE $CACHE_PATH

	# Enable mbed-os local PROTOCOL
	mbed config PROTOCOL ssh
    cd mbed-os
    mbed update caeaa49d68c67ee00275cece10cd88e0ed0f6ed3
    cd ../
    mbed add https://github.com/ARMmbed/e2e-unity/#2b08a73292c4de21c13475e240d0b035df2abc53
fi

# Toolchain
TOOLCHAIN="$1"

if [ -z $TOOLCHAIN ]; then
	echo "Please select one option : Select GCC_ARM or ARM"
	exit 1
fi

if [ "$TOOLCHAIN" == "GCC_ARM" ]; then
	export TOOLCHAIN="GCC_ARM"
elif [ "$TOOLCHAIN" == "ARM" ]; then
	export TOOLCHAIN="ARM"
	export ARMLMD_LICENSE_FILE=$TINY_CBOR_TOP/TESTS/arm_licenses/arm_licenses.lic
else 
	echo "Unsupported toolchain Select GCC_ARM or ARM "
	exit 1
fi

MBEDIGNORE="*"

echo "$MBEDIGNORE"     >|  "$TINY_CBOR_TOP"/mbed-os/features/frameworks/.mbedignore
echo "$MBEDIGNORE"     >|  "$TINY_CBOR_TOP"/mbed-os/features/filesystem/littlefs/.mbedignore

make -f mbed_Makefile

