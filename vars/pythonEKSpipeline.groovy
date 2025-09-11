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
                        def 
                    }
                }
            }
        }
    }
}