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

	echo "Cloning " + url + '#' + branches + ' using ' +  clean_method + ' method'

	checkout poll: false, scm:
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
  
	echo "branches: ${branches}"
	echo "repo_url: ${repo_url}"
	echo "env.CLEAN_BUILD_WORKSPACE: ${env.CLEAN_BUILD_WORKSPACE}"

		timeout(time: 120, unit: 'MINUTES') {
			echo "in build"
			node(node_name) {
				echo "in node ${node_name}"
				clone_repository(
					branches: branches,
					url: repo_url,
					clean_method: env.CLEAN_BUILD_WORKSPACE
				)
				
				withEnv(env_vars) {
					try {
						printNodeNameAndCurrentDir()
						echo "deployer ${deploy_groovy_file}"
						// Deploy tree (if needed)
						if (deploy_groovy_file != null) {
							def deployer = load(deploy_groovy_file)
							deployer.deploy()
						}

						// Setup and Build
						sh """#!/bin/bash -lxe
						echo "SETUP!!"
						${setup_cmd}
						${build_cmd}
						"""

						// Stash the resulting artifact(s)
						if (!artifacts.isEmpty()) {
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

def unittests_run(Map m) {
	def name         = m.name
	def os           = m.os
	def platform     = m.platform
	def toolchain    = m.toolchain
	def build_type   = m.build_type
	def artifact     = m.artifact
	def raas_daemon  = m.raas_daemon

	
		timeout(time: 140, unit: 'MINUTES') {
			node('prov-jen-lin2') {
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

def execute_cmd(Map m) {
	def cmd	= m.cmd
	def node_name = m.node_name
	
	clone_repository(
					branches: scm.branches,
					url: scm.userRemoteConfigs[0].url,
					clean_method: env.CLEAN_BUILD_WORKSPACE
				)
	sh """#!/bin/bash -lxe
		${cmd}
		"""
}

def notify_job_result(def currJobStatus) {
    def subjectStatus = 'SUCCESS'
    def shouldSend = true
    def prevBuildStatus = currentBuild.rawBuild.getPreviousBuild()?.getResult()

    if (env.NOTIFICATIONS_DISABLE) {
        if (env.NOTIFICATIONS_DISABLE == 'true') {
            return
        }
    }

    if (currJobStatus.equals(hudson.model.Result.SUCCESS)) {
        if (prevBuildStatus.equals(hudson.model.Result.FAILURE)  ||
            prevBuildStatus.equals(hudson.model.Result.ABORTED)  ||
            prevBuildStatus.equals(hudson.model.Result.UNSTABLE) ||
            prevBuildStatus.equals(hudson.model.Result.NOT_BUILT)) {
            subjectStatus = 'FIXED'
        } else {
            shouldSend = false
        }
    } else {
        if (prevBuildStatus.equals(hudson.model.Result.SUCCESS)) {
            subjectStatus = 'BROKEN'
        } else {
            subjectStatus = 'Still FAILED'
        }
    }

    if (shouldSend) {
        def to_recipients = 'iot-eng-provisioning-client@arm.com;Evgeni.Bolotin@arm.com;Nimrod.Zimerman@arm.com'
        emailext(
            body: 'Please go to ${BUILD_URL} and verify the build', 
            subject: "${subjectStatus}! Job ${JOB_NAME} (${BUILD_NUMBER})", 
            to: to_recipients
        )
    }
}

return this
