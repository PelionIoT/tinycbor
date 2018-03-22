#!/usr/bin/env groovy
// ----------------------------------------------------------------------------
// Copyright 2016-2017 ARM Ltd.
//  
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//  
//     http://www.apache.org/licenses/LICENSE-2.0
//  
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ----------------------------------------------------------------------------


def qt_test(jenkinsUtils)
{
	command = """
				chmod +x linux-tests.sh
				./linux-tests.sh
				"""
	jenkinsUtils.execute_cmd(
							cmd: command,
							node_name: "prov-test-linux"
							)
	
}

def os_to_bin(os, plat, toolchain) {
	def os_bin_mapping = [
								"mbedos": "BUILD/tests/${plat}/${toolchain}/TESTS/runner/tiny_cbor_main/tiny_cbor_main.bin"
								]
	return os_bin_mapping[os]
}

def os_to_artifact(os, plat, toolchain, build_mode) {
	def os_artifact_mapping = [
								"mbedos": "out/tiny_cbor_main_${plat}_${toolchain}_${build_mode}.bin"
								]
	return os_artifact_mapping[os]
}

def os_to_makefile(os) {
	def os_makefile_mapping = [
								"mbedos" : "makefile_mbed"
								]
	return os_makefile_mapping[os]
}


def run_job(jenkinsUtils) {
	def os_list = ["mbedos"]
	def platforms = ["K64F"]
	def build_modes = ["debug", "develop", "release"]
	def toolchains = ["GCC_ARM", "ARM"]
	def stepsForParallel = [:]
	def stepsForParallelTests = [:]
	def currBuildStatus = hudson.model.Result.SUCCESS
	
	try {
	
		// When building, we build all build modes in a single process. Parrallelism is for all the other combinations 
		
			for (os in os_list) {
				for (plat in platforms) {
					for(toolchain in toolchains) {
						def stepName = "BUILD_" + os + "_" + plat + "_" + toolchain
						// Define the variables for the stepsForParallel closure context. If we do not define these, when the closure is invoked the variables will evaluate to their value at the end of the for loop
						def _toolchain = toolchain
						def _plat = plat
						def _os = os
						def _makefile = os_to_makefile(_os)
						def bin = os_to_bin(_os, _plat, _toolchain)
						
						stepsForParallel[stepName] = {
							for(build_mode in build_modes) {
								def artifact = os_to_artifact(_os, _plat, _toolchain, build_mode)
								jenkinsUtils.build(
									name: "tinycbor",
									toolchain: _toolchain,
									build_type: build_mode,
									os: _os,
									platform: _plat,
									env: [],
									setup: "source env_setup.sh ${_toolchain} ${build_mode}",
									cmd: "make -f ${_makefile} && mkdir -p out && mv -f ${bin} ${artifact}",
									artifacts: [artifact],
									node_name: 'prov_bld',
									scm: scm
								)
								
							}
						}
					}
				}
			}
		
			for (os in os_list) {
				for (plat in platforms) {
					for(toolchain in toolchains) {
						def stepName = "TEST_" + plat + "_" + toolchain
						// Define the variables for the stepsForParallel closure context. If we do not define these, when the closure is invoked the variables will evaluate to their value at the end of the for loop
						def _toolchain = toolchain
						def _plat = plat
						def _os = os
						stepsForParallelTests[stepName] = {
							for(build_mode in build_modes) {
								def _artifact = os_to_artifact(_os, _plat, _toolchain, build_mode)
								jenkinsUtils.unittests_run(
									name: 'tinycbor',
									os: _os,
									platform: _plat,
									toolchain: _toolchain,
									build_type: build_mode,
									artifact: _artifact,
									raas_daemon: 'kfn-mbedos'
								)
								
							}
						}
					}
				}
			}
			
			// Add qt tests to parrallel testing
			stepsForParallelTests["QT Tests"] = { node ("prov_bld") { qt_test(jenkinsUtils) }}
			
			// Actually run the steps in parallel - parallel takes a map as an argument,
			stage ('Build') {
				parallel stepsForParallel
			}
			stage ('Tests') {
				parallel stepsForParallelTests
			}
		
	} catch (Exception e) {
        currBuildStatus = hudson.model.Result.FAILURE
        throw e
    } finally {
        jenkinsUtils.notify_job_result(currBuildStatus)
    }
	
}

stage ("Setup") {
	// Load the utils script from master node
	node ('master') {
		checkout scm
		jenkinsUtils = load 'JenkinsUtils.groovy'
	}
	
	// Call run_job() using a flightwight executer since almost always idle
	run_job(jenkinsUtils)
}

