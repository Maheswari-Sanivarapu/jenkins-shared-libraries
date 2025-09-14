def call(Map configMap) {
    pipeline{
        agent{
            label 'LABEL_1'
        }
        environment {
            REGION = 'us-east-1'
            appVersion = ''
            ACC_ID = 557690617909
            PROJECT = configMap.get('PROJECT')
            COMPONENT = configMap.get('COMPONENT')
        }
        options {
            disableConcurrentBuilds()
            timeout(time: 30 , unit: 'MINUTES')
            ansiColor('xterm')
        }
        parameters{
            booleanParam(name: 'deploy', defaultValue:false, description: 'Toggle this value')
        }
        stages{
            stage('Read package.json file'){
                steps{
                    script{
                        def packageJson = readJSON file: 'package.json'
                        appVersion = packageJson.version
                        echo "Package Version: ${appVersion}"
                    }
                }
            }
            stage('install npm dependencies'){
                steps{
                    script{
                        sh """
                            npm install
                        """
                    }
                }
            }
            stage('Building the Docker Image'){
                steps{
                    script{
                        withAWS(credentials: 'aws-auth', region: 'us-east-1'){
                            sh """
                                aws ecr get-login-password --region ${REGION} | docker login --username AWS --password-stdin ${ACC_ID}.dkr.ecr.us-east-1.amazonaws.com
                                docker build -t ${ACC_ID}.dkr.ecr.us-east-1.amazonaws.com/${PROJECT}/${COMPONENT}:${appVersion} .
                                docker push ${ACC_ID}.dkr.ecr.us-east-1.amazonaws.com/${PROJECT}/${COMPONENT}:${appVersion}
                            """
                        }
                    }
                }
            }
            stage("Trigger cd"){
                when {
                    expression {params.deploy}
                }
                steps{
                    script{
                        build job: "${COMPONENT}-cd",
                        parameters: [
                            string(name: 'appVersion', value: "${appVersion}"),
                            string(name: 'deploy_to', value: 'dev')
                        ],
                        propagate: false,
                        wait: false
                    }
                }
            }
        }
        post {
            always {
                echo 'i will always say hello again'
                deleteDir()
            }
            success {
                echo 'Success'
            }
            failure {
                echo 'Failure'
            }
        }
    }
}