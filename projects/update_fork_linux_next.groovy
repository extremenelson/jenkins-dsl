freeStyleJob('update_fork_linux_next') {
    displayName('update-fork-linux-next')
    description('Rebase the linux-next branch in jessfraz/linux fork.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/jessfraz/linux')
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('git@github.com:jessfraz/linux.git')
                name('origin')
                credentials('ssh-github-key')
                refspec('+refs/heads/linux-next:refs/remotes/origin/linux-next')
            }
            remote {
                url('https://git.kernel.org/pub/scm/linux/kernel/git/next/linux-next.git')
                name('upstream')
                refspec('+refs/heads/master:refs/remotes/upstream/master')
            }
            branches('linux-next', 'upstream/master')
            extensions {
                wipeOutWorkspace()
                cleanAfterCheckout()
            }
            configure { node ->
                node / 'extensions' / 'hudson.plugins.git.extensions.impl.CloneOption' {
                    noTags 'true'
                    timeout '40'
                }
            }
        }
    }

    triggers {
        cron('H H * * *')
    }

    wrappers { colorizeOutput() }

    steps {
        shell('git reset --hard upstream/master')
    }

    publishers {
        git {
            branch('origin', 'linux-next')
            pushOnlyIfSuccess()
            forcePush()
        }

        extendedEmail {
            recipientList('$DEFAULT_RECIPIENTS')
            contentType('text/plain')
            triggers {
                stillFailing {
                    attachBuildLog(true)
                }
            }
        }

        wsCleanup()
    }
}
