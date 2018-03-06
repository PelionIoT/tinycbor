export TINY_CBOR_TOP=`pwd`
CACHE_PATH=/opt/scratch/$(whoami)/tcbor-cache
source 

# Toolchain
TINYCBOR_TOOLCHAIN="$1"
export TINYCBOR_BUILD_TYPE="develop"

if [ -z $TINYCBOR_TOOLCHAIN ]; then
		echo "Please select one option : Select GCC_ARM or ARM"
	return 1
fi
if [ "$TINYCBOR_TOOLCHAIN" != "ARM" ] && [ "$TINYCBOR_TOOLCHAIN" != "GCC_ARM" ]; then
	echo "Please select one option : Select GCC_ARM or ARM"
	return 1
fi

if [ -n "$2" ]; then
	if [ "$2" != "develop" ] && [ "$2" != "debug" ] && [ "$2" != "release" ]; then
		echo "Second argument (build type) must be one of the following: develop (default), debug, release"
		return 1
	fi
	export TINYCBOR_BUILD_TYPE="$2"
fi

# Create new mbed project (if one does not exist).
# Creating the project is important so that we use local config and not global.
if [ ! -f .mbed ]; then
	mbed new .

	# Enable mbed-os local CACHE
	mbed config CACHE $CACHE_PATH

	# Enable mbed-os local PROTOCOL
	mbed config PROTOCOL ssh
    cd mbed-os
    mbed update $MBEDOS_REVISION
    cd ../
    
fi

if [ "$TINYCBOR_TOOLCHAIN" == "GCC_ARM" ]; then
	export TINYCBOR_TOOLCHAIN="GCC_ARM"
	mbed config GCC_ARM_PATH /opt/arm/gcc6-arm-none-eabi/bin
	
elif [ "$TINYCBOR_TOOLCHAIN" == "ARM" ]; then
	export TINYCBOR_TOOLCHAIN="ARM"
	export ARMLMD_LICENSE_FILE=$TINY_CBOR_TOP/licenses/arm_licenses/arm_licenses.lic
	mkdir -p $(dirname "${ARMLMD_LICENSE_FILE}")
	curl http://prov-jen-master.kfn.arm.com:8888/job/vivify_arm_license/lastSuccessfulBuild/artifact/arm_licenses_vivified.lic > $ARMLMD_LICENSE_FILE
	mbed config ARM_PATH /usr/local/DS-5_v5.25.0/ARMCompiler5.06u3
	
else 
	echo "Unsupported toolchain Select GCC_ARM or ARM "
	return 1
fi

if [ ! -d e2e-unity ]; then
	mbed add -I https://github.com/ARMmbed/e2e-unity/#$E2E_UNITY_REVISION
fi

MBEDIGNORE="*"

echo "$MBEDIGNORE"     >|  "$TINY_CBOR_TOP"/mbed-os/features/frameworks/.mbedignore
echo "$MBEDIGNORE"     >|  "$TINY_CBOR_TOP"/mbed-os/features/filesystem/littlefs/.mbedignore

echo "Enviornment set successfully! Toolchain $TINYCBOR_TOOLCHAIN, build mode $TINYCBOR_BUILD_TYPE"