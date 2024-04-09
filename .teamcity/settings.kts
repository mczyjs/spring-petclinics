import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.PullRequests
import jetbrains.buildServer.configs.kotlin.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.buildFeatures.jiraCloudIntegration
import jetbrains.buildServer.configs.kotlin.buildFeatures.notifications
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildFeatures.pullRequests
import jetbrains.buildServer.configs.kotlin.buildSteps.SSHUpload
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
import jetbrains.buildServer.configs.kotlin.buildSteps.sshExec
import jetbrains.buildServer.configs.kotlin.buildSteps.sshUpload
import jetbrains.buildServer.configs.kotlin.projectFeatures.jira
import jetbrains.buildServer.configs.kotlin.triggers.vcs
import jetbrains.buildServer.configs.kotlin.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2023.11"

project {

    vcsRoot(HttpsGithubComMczyjsSpringPetclinicRefsHeadsMain1)

    buildType(Build)
    buildType(Deploy)

    features {
        jira {
            id = "PROJECT_EXT_7"
            displayName = "Demo"
            host = "https://xdatatech-team-a1bchh7kh5hr.atlassian.net/"
            userName = "mazs@xdatatech.com"
            password = "credentialsJSON:5370317a-04c6-4561-8322-26ddff69bfd0"
            projectKeys = "SPN"
            cloudClientID = "awNqC8oK0J7BzqcNAV1yC1BsPIPKv2NE"
            cloudSecret = "credentialsJSON:835e9f4d-84db-44d1-8b61-6f786b126441"
        }
    }
}

object Build : BuildType({
    name = "Deploy to cloud"

    enablePersonalBuilds = false
    artifactRules = "target/*.jar"
    maxRunningBuilds = 1
    publishArtifacts = PublishMode.SUCCESSFUL

    vcs {
        root(DslContext.settingsRoot)

        checkoutMode = CheckoutMode.ON_AGENT
        cleanCheckout = true
    }

    steps {
        sshUpload {
            id = "ssh_deploy_runner"
            transportProtocol = SSHUpload.TransportProtocol.SCP
            sourcePath = "*.jar"
            targetUrl = "1.92.88.210:/root/target/"
            authMethod = password {
                username = "root"
                password = "credentialsJSON:3ec89384-2735-4900-99d2-45d0162f3f0e"
            }
        }
        sshExec {
            id = "ssh_exec_runner"
            commands = "java -jar /root/target/*.jar&"
            targetUrl = "1.92.88.210"
            authMethod = password {
                username = "root"
                password = "credentialsJSON:3ec89384-2735-4900-99d2-45d0162f3f0e"
            }
        }
    }

    triggers {
        vcs {
            triggerRules = "-:.teamcity/**"
            watchChangesInDependencies = true
        }
    }

    features {
        perfmon {
        }
        jiraCloudIntegration {
            issueTrackerConnectionId = "PROJECT_EXT_7"
        }
        notifications {
            notifierSettings = emailNotifier {
                email = "mazs@xdatatech.com"
            }
            buildStarted = true
            buildFailed = true
            buildFinishedSuccessfully = true
        }
    }

    dependencies {
        dependency(Deploy) {
            snapshot {
            }

            artifacts {
                artifactRules = "spring-petclinic-*.jar"
            }
        }
    }
})

object Deploy : BuildType({
    name = "Build"

    artifactRules = "target/*.jar"
    publishArtifacts = PublishMode.SUCCESSFUL

    vcs {
        root(HttpsGithubComMczyjsSpringPetclinicRefsHeadsMain1)

        cleanCheckout = true
    }

    steps {
        maven {
            id = "Maven2"
            goals = "clean test package"
            runnerArgs = "-Dmaven.test.failure.ignore=true"
            jdkHome = "%env.JDK_17_0_x64%"
        }
    }

    features {
        perfmon {
        }
        pullRequests {
            vcsRootExtId = "${HttpsGithubComMczyjsSpringPetclinicRefsHeadsMain1.id}"
            provider = github {
                authType = token {
                    token = "credentialsJSON:24a69dbb-bb49-4471-955e-6e1be8b549a4"
                }
                filterAuthorRole = PullRequests.GitHubRoleFilter.MEMBER
            }
        }
        commitStatusPublisher {
            vcsRootExtId = "${HttpsGithubComMczyjsSpringPetclinicRefsHeadsMain1.id}"
            publisher = github {
                githubUrl = "https://api.github.com"
                authType = personalToken {
                    token = "credentialsJSON:e97fd1e1-0c84-4a08-844d-c62a1a00cbff"
                }
            }
        }
    }
})

object HttpsGithubComMczyjsSpringPetclinicRefsHeadsMain1 : GitVcsRoot({
    name = "https://github.com/mczyjs/spring-petclinic#refs/heads/main (1)"
    url = "https://github.com/mczyjs/spring-petclinic"
    branch = "refs/heads/main"
    branchSpec = "+:refs/heads/main"
    authMethod = password {
        userName = "mczyjs"
        password = "credentialsJSON:ef0ab93d-a247-4fbb-bc3e-090e05878bfb"
    }
})
