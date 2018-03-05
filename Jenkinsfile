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

import java.net.ServerSocket

def isEmptyString(my_string) {
    return my_string==null || my_string.isEmpty()
}

def resetRepositoryToBaseline() {
    sh '''#!/bin/bash -xe
    /usr/bin/find -name .git -type d -exec bash -c 'echo "------ cleaning $(dirname {})" && git -C $(dirname {}) reset --hard && git -C $(dirname {}) clean -fdx' \\;
    '''
}

def printNodeNameAndCurrentDir() {
    echo "running on ${env.NODE_NAME} in " + pwd()
}

def removeLastStringFromPath(str) {
    // Return './' incase no path introduced
    String newstr = './'

    if ((null != str) && (str.length() > 0)) {
        int endIndex = str.lastIndexOf("/")
        if (endIndex != -1) {
            newstr = str.substring(0, endIndex)
        }
    }
    return newstr
}

@NonCPS
def isPortBusy()
{
    def success = true;
    try {
        (new Socket('127.0.0.1', 7777)).close();
        success = false;
    } catch(SocketException e) {
        // Could not connect.
    }
    return success;
}

def clone_repository(Map m) {
    def branches     = m.branches
    def url          = m.url
    def clean_method = m.clean_method

    echo "Cloning " + url + '#' + branches + ' using ' +  env.CLEAN_BUILD_WORKSPACE + ' method'

    checkout(
        [
            $class: 'GitSCM',
            branches: branches,
            doGenerateSubmoduleConfigurations: false,
            extensions: [
                [$class: clean_method],
                [
                    $class: 'CloneOption',
                    depth: 0,
                    noTags: true,
                    reference: '',
                    shallow: true,
                    timeout: 15
                ],
            ],
            submoduleCfg: [],
            userRemoteConfigs: [
                [
                    credentialsId: '01ff549f-c379-4717-bbcb-9df5227ad097',
                    url: url
                ]
            ]
        ]
    )

    if (clean_method != 'WipeWorkspace') {
        resetRepositoryToBaseline()
    }
}

def build(Map m) {
    def name               = m.name
    def os                 = m.os
    def platform           = m.platform
    def toolchain          = m.toolchain
    def build_type         = m.build_type
    def env_vars           = m.env
    def deploy_groovy_file = m.deploy
    def setup_cmd          = m.setup
    def build_cmd          = m.cmd
    def artifacts          = m.artifacts
    def node_name          = m.node_name
    def _scm               = m.scm
    
    def branches           = _scm.branches
    def repo_url           = _scm.userRemoteConfigs[0].url
  
 
        timeout(time: 120, unit: 'MINUTES') {

            node(node_name) {
                clone_repository(
                    branches: branches,
                    url: repo_url,
                    clean_method: env.CLEAN_BUILD_WORKSPACE
                )
                withEnv(env_vars) {
                    try {
                        printNodeNameAndCurrentDir()

                        // Deploy tree (if needed)
                        if (deploy_groovy_file != null) {
                            def deployer = load(deploy_groovy_file)
                            deployer.deploy()
                        }

                        // Setup and Build
                        sh """#!/bin/bash -lxe
                        ${setup_cmd}
                        ${build_cmd}
                        """

                        // Stash the resulting artifact(s)
                        if (!artifacts?.empty) {
                            def _artifacts = artifacts.join(",")
                            echo "stash: ${_artifacts}"
                            stash name: "${name}.${platform}.${os}.${toolchain}.${build_type}", includes: "${_artifacts}"
                            archiveArtifacts artifacts: "${_artifacts}", onlyIfSuccessful: true
                        }
                    } catch (Exception e) {
                        throw e
                    } finally {
                        if (env.REPORT_DIR != null) {
                            publishHTML(
                                [
                                     allowMissing: false,
                                     alwaysLinkToLastBuild: true,
                                     keepAll: true,
                                     reportDir: "${env.REPORT_DIR}",
                                     reportFiles: "${env.REPORT_INDEX}",
                                     reportName: "${env.REPORT_NAME}",
                                     reportTitles: ''
                                ]
                            )
                        }
                    }
                }
            }
        }
    
}

def test(Map m) {
    def name         = m.name
    def os           = m.os
    def platform     = m.platform
    def toolchain    = m.toolchain
    def build_type   = m.build_type
    def artifact     = m.artifact
    def raas_daemon  = m.raas_daemon

    
        timeout(time: 140, unit: 'MINUTES') {
            node('prov-test-linux') {
                deleteDir()
                dir('secsrv-qe-scripts') {
                    clone_repository(
                        branches: [[ name: '*/master' ]],
                        url: 'git@github.com:ARMmbed/secsrv-qe-scripts.git',
                        clean_method: 'CleanBeforeCheckout'
                    )
                }
                def raas_log_name = "${artifact}.log_raas"
                try {
                    unstash name: "${name}.${platform}.${os}.${toolchain}.${build_type}"
                    def pathToArtifact = removeLastStringFromPath(artifact)
                    printNodeNameAndCurrentDir()

                    sh """#!/bin/bash -lxe
                    if [ ${platform} == "PC" ]; then
                        set -o pipefail
                        ./${artifact} 2>&1 | tee ${artifact}.log
                    else
                        python secsrv-qe-scripts/raas/raas_unit_test.py --platform=K64F --bin=${artifact} --log=${artifact}.log --alloc-timeout=7200 --expiration=7200 --baud=115200 --raas-log=${raas_log_name} --raas=${raas_daemon}
                    fi
                    python secsrv-qe-scripts/unit_tests/qe_unity_to_junit.py ${pathToArtifact} --parse-from-string="----< Test - Start >----"
                    python secsrv-qe-scripts/unit_tests/qe_unit_test_status.py ${pathToArtifact}
                    """
                } catch (Exception e) {
                    error "Running ${artifact} on ${platform} failed"
                    throw e
                } finally {
                    archiveArtifacts artifacts: "${artifact}.log", onlyIfSuccessful: false
                    if (platform != "PC") {
                        archiveArtifacts artifacts: "${raas_log_name}", onlyIfSuccessful: false
                    }
                    junit allowEmptyResults:false, healthScaleFactor:0.0, keepLongStdio:true, testResults: artifact + '.xml'
                }
            }
        }
    
}


/*
def build(toolchain, build_mode, plat, os)
{
	node('prov_bld') {		
		def os_artifact_mapping = [
								"mbedos" : "BUILD/tests/${plat}/${toolchain}/TESTS/runner/tiny_cbor_main/tiny_cbor_main.bin"
								]
		
		def os_makefile_mapping = [
								"mbedos" : "makefile_mbed"
								]
		
		clone_repository(
				branches: scm.branches,
				url: scm.userRemoteConfigs[0].url,
				clean_method: clean_method
			)
		
		sh """#!/bin/bash -xe
			source env_setup.sh ${toolchain} ${build_mode}
			make -f ${os_makefile_mapping[os]} 
			
			# Rename the built artifact so that we don't loose any
			
			mv ${os_artifact_mapping[os]} tiny_cbor_main.${os}.${plat}.${toolchain}.${build_mode}.bin
		"""
		
		stash name: "${plat}.${os}.${toolchain}.${build_mode}", includes : "tiny_cbor_main.${os}.${plat}.${toolchain}.${build_mode}.bin"
		archiveArtifacts artifacts: "tiny_cbor_main.${os}.${plat}.${toolchain}.${build_mode}.bin", onlyIfSuccessful: false
	}
}
*/

def qt_test()
{
	node('prov-jen-lin2') {
		clone_repository(
				branches: scm.branches,
				url: scm.userRemoteConfigs[0].url,
				clean_method: clean_method
			)
		
		sh """#!/bin/bash -xe
			chmod +x linux-tests.sh
			./linux-tests.sh
		"""
	}
}

node('prov-test-linux') {
	def os_list = ["mbedos"]
	def platforms = ["K64F"]
	def build_modes = ["debug", "develop", "release"]
	def toolchains = ["GCC_ARM", "ARM"]
	def stepsForParallel = [:]
	def stepsForParallelTests = [:]
	def currBuildStatus = hudson.model.Result.SUCCESS
	
	load 'JenkinsUtils.groovy'
	hello_func()
	//def jenkinsUtils = new JenkinsUtils()
	
	try {
	
		stage ('Build') {
			for (os in os_list) {
				for (plat in platforms) {
					for(toolchain in toolchains) {
						def os_artifact_mapping = [
								"mbedos" : "BUILD/tests/${plat}/${toolchain}/TESTS/runner/tiny_cbor_main/tiny_cbor_main.bin"
								]
		
						def os_makefile_mapping = [
								"mbedos" : "makefile_mbed"
								]
						def stepName = "BUILD_" + os + "_" + plat + "_" + toolchain
						// Define the variables for the stepsForParallel closure context. If we do not define these, when the closure is invoked the variables will evaluate to their value at the end of the for loop
						def _toolchain = toolchain
						def _plat = plat
						def _os = os
						def _makefile = os_makefile_mapping[_os]
						def _artifact = os_artifact_mapping[_os]
						
						
						
						stepsForParallel[stepName] = {
							for(build_mode in build_modes) {
								echo _toolchain
								echo _plat
								echo _os
								echo _makefile
								echo _artifact
								echo build_mode
								jenkinsUtils.build(
									toolchain: _toolchain,
									os: _os,
									platform: _plat,
									env: [],
									deploy: '',
									setup: "source env_setup.sh ${_toolchain} ${build_mode}",
									cmd: "make -f ${_makefile}",
									artifacts: [_artifact],
									node_name: 'prov_bld',
									scm: scm
								)
							}
						}
					}
				}
			}
			
			// Actually run the steps in parallel - parallel takes a map as an argument,
			parallel stepsForParallel
		}
		
		stage ('Tests') {
			for (os in os_list) {
				for (plat in platforms) {
					for(toolchain in toolchains) {
						def os_artifact_mapping = [
								"mbedos" : "BUILD/tests/${plat}/${toolchain}/TESTS/runner/tiny_cbor_main/tiny_cbor_main.bin"
								]
						def stepName = "TEST_" + plat + "_" + toolchain
						// Define the variables for the stepsForParallel closure context. If we do not define these, when the closure is invoked the variables will evaluate to their value at the end of the for loop
						def _toolchain = toolchain
						def _plat = plat
						def _os = os
						def _artifact = os_artifact_mapping[_os]
						stepsForParallelTests[stepName] = {
							for(build_mode in build_modes) {
								jenkinsUtils.test(
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
			
			// Add qt tests to parrallel
			stepsForParallelTests["QT Tests"] = { qt_test() }
			parallel stepsForParallelTests
			echo "Finished parrallel"
		}
	} catch (Exception e) {
        currBuildStatus = hudson.model.Result.FAILURE
        throw e
    } finally {
        //notify_job_result(currBuildStatus)
    }
}