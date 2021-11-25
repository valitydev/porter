#!groovy
build('porter', 'java-maven') {
    checkoutRepo()
    loadBuildUtils()

    def pipeJavaServiceInsideDocker
    runStage('load JavaService pipeline') {
        pipeJavaServiceInsideDocker = load("build_utils/jenkins_lib/pipeJavaServiceInsideDocker.groovy")
    }

    def serviceName = env.REPO_NAME
    def mvnArgs = '-DjvmArgs="-Xmx256m" -Ddockerfile.registry="registry.hub.docker.com"'
    def serviceImageTag = '7e0c98bb'

    pipeJavaServiceInsideDocker(serviceName, mvnArgs, serviceImageTag)
}
