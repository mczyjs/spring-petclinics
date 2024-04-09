import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.PullRequests
import jetbrains.buildServer.configs.kotlin.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.buildFeatures.jiraCloudIntegration
import jetbrains.buildServer.configs.kotlin.buildFeatures.notifications
import jetbrains.buildServer.configs.kotlin.buildFeatures.perfmon
import jetbrains.buildServer.configs.kotlin.buildFeatures.pullRequests
import jetbrains.buildServer.configs.kotlin.buildSteps.SSHUpload
import jetbrains.buildServer.configs.kotlin.buildSteps.maven
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

version = "2024.03"

project {

    vcsRoot(HttpsGithubComMczyjsSpringPetclinicsRefsHeadsMain1)

    buildType(Build)
    buildType(DeployToCloud)

    features {
        jira {
            id = "PROJECT_EXT_9"
            displayName = "Demo"
            host = "https://xdatatech-team-a1bchh7kh5hr.atlassian.net/"
            userName = "mazs@xdatatech.com"
            password = "credentialsJSON:2105bb63-daf6-4d74-b953-2d340208d34e"
            projectKeys = "ST"
            cloudClientID = "eVHIFwnymcrevK1Q90YJwiUZPHC1SE5g"
            cloudSecret = "credentialsJSON:b78d52a3-c866-459d-92bc-db655bfa8d39"
        }
    }
}

object Build : BuildType({
    name = "Build"

    artifactRules = "target/*.jar"
    publishArtifacts = PublishMode.SUCCESSFUL

    vcs {
        root(DslContext.settingsRoot)
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
            vcsRootExtId = "${DslContext.settingsRoot.id}"
            provider = github {
                authType = token {
                    token = "credentialsJSON:404c13d0-f31f-4950-9e8d-40dc197283db"
                }
                filterAuthorRole = PullRequests.GitHubRoleFilter.MEMBER
            }
        }
        commitStatusPublisher {
            vcsRootExtId = "${DslContext.settingsRoot.id}"
            publisher = github {
                githubUrl = "https://api.github.com"
                authType = personalToken {
                    token = "credentialsJSON:404c13d0-f31f-4950-9e8d-40dc197283db"
                }
            }
        }
    }
})

object DeployToCloud : BuildType({
    name = "Deploy to cloud"

    vcs {
        root(HttpsGithubComMczyjsSpringPetclinicsRefsHeadsMain1)
    }

    steps {
        sshUpload {
            id = "ssh_deploy_runner"
            transportProtocol = SSHUpload.TransportProtocol.SCP
            sourcePath = "*.jar"
            targetUrl = "1.92.88.210:/root/target/"
            authMethod = password {
                username = "root"
                password = "credentialsJSON:932dabb5-0818-4d5e-b3dd-9a3447bfbbab"
            }
        }
    }

    triggers {
        vcs {
            triggerRules = "-:.teamcity/**"
        }
    }

    features {
        perfmon {
        }
        jiraCloudIntegration {
            issueTrackerConnectionId = "PROJECT_EXT_9"
        }
        notifications {
            enabled = false
            notifierSettings = emailNotifier {
                email = "mazs@xdatatech.com"
            }
            buildStarted = true
            buildFailed = true
            buildFinishedSuccessfully = true
        }
    }

    dependencies {
        dependency(Build) {
            snapshot {
            }

            artifacts {
                artifactRules = "spring-petclinic-*.jar"
            }
        }
    }
})

object HttpsGithubComMczyjsSpringPetclinicsRefsHeadsMain1 : GitVcsRoot({
    name = "https://github.com/mczyjs/spring-petclinics#refs/heads/main (1)"
    url = "https://github.com/mczyjs/spring-petclinics"
    branch = "refs/heads/main"
    branchSpec = "refs/heads/*"
    authMethod = password {
        userName = "mczyjs"
        password = "credentialsJSON:404c13d0-f31f-4950-9e8d-40dc197283db"
    }
})
