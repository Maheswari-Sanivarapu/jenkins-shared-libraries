def call(Map configMap){
    pipeline{
        agent{
            label 'LABEL_1'
        }
        environment{
            REGION = 'us-east-1',
            ACC_ID = 557690617909
            appVersion = ''
            PROJECT = configMap.get('PROJECT')
            COMPONENT = configMap.get('COMPONENT')
        }
        options{
            disableConcurrentBuilds()
            timeout(time: 30 , unit: 'MINUTES')
            ansiColor('xterm')
        }
        parameters{
            booleanParam(name: 'deploy', defaultValue: false, description: 'toggle this value')
        }
        stages{
            stage('Read the appVersion'){
                steps{
                    script{
                        appVersion = readFile('version')
                        echo "appVersion : ${appVersion}"
                    }
                }
            }
            stage('install the dependencies'){
                steps{
                    script{
                        sh """
                            pip3 install -r requirements.txt
                        """
                    }
                }
            }
            stage('unit testing'){
                steps{
                    script{
                        sh """
                            echo "unit tests"
                        """
                    }
                }
            }
            stage('Docker Build'){
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
                when{
                    expression { params.deploy }
                }
                steps{
                    script{
                        build job: "${COMPONENT}-cd",
                        parameters: [
                            string(name: 'appVersion', value: "${appVersion}"),
                            string(name: 'deploy_to', value: 'dev')
                        ],
                        propagate: false,  // even SG fails VPC will not be effected
                        wait: false // VPC will not wait for SG pipeline completion
                    }
                }
            }
        }
        post {
            always{
                echo 'i will always say hello again'
                deleteDir()
            }
            success{
                echo "Success"
            }
            failure{
                echo "Failure"
            }
        }
    }
}