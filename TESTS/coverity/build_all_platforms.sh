#!/bin/bash
#----------------------------------------------------------------------------
#   The confidential and proprietary information contained in this file may
#   only be used by a person authorised under and to the extent permitted
#   by a subsisting licensing agreement from ARM Limited or its affiliates.
#
#          (C) COPYRIGHT 2013-2016 ARM Limited or its affiliates.
#              ALL RIGHTS RESERVED
#
#   This entire notice must be reproduced on all copies of this file
#   and copies of this file may only be made by a person if such person is
#   permitted to do so under the terms of a subsisting license agreement
#   from ARM Limited or its affiliates.
#----------------------------------------------------------------------------

set -e

mbed new .
mbed config PROTOCOL ssh

declare -a toolchains=(GCC_ARM ARM)
declare -a platforms=(K64F)

for tc in ${toolchains[@]}
do
	for plat in ${platforms[@]}
	do
		source env_setup.sh $tc
		make -f makefile_mbed
	done
done

