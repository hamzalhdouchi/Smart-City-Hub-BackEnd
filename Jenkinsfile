pipeline {

    agent any

    tools {
        maven 'Maven-3.9'
        jdk   'JDK-17'
    }

    environment {
        APP_NAME          = 'smart-city-hub'
        DOCKER_REGISTRY   = 'docker.io'
        DOCKER_IMAGE      = "${DOCKER_REGISTRY}/${DOCKERHUB_USERNAME}/${APP_NAME}"
        DOCKER_TAG        = "${env.BUILD_NUMBER}"

        DOCKERHUB_CRED    = 'dockerhub-credentials'
        DEPLOY_SSH_CRED   = 'deploy-server-ssh'
        DEPLOY_SERVER_IP  = credentials('deploy-server-ip')
        ENV_FILE_CRED     = 'prod-env-file'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
        timestamps()
    }

    triggers {
        pollSCM('H/5 * * * *')
    }

    stages {

        stage('Checkout') {
            steps {
                echo "Checking out branch: ${env.GIT_BRANCH}"
                checkout scm
            }
        }

        stage('Test') {
            steps {
                echo 'Running unit tests with coverage...'
                catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                    bat 'mvn verify -B'
                }
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml',
                          allowEmptyResults: true

                    archiveArtifacts artifacts: 'target/surefire-reports/**',
                                     allowEmptyArchive: true

                    jacoco(
                        execPattern: 'target/jacoco.exec',
                        classPattern: 'target/classes',
                        sourcePattern: 'src/main/java',
                        exclusionPattern: '**/dto/**,**/entity/**,**/config/**,**/exception/**',
                        changeBuildStatus: false
                    )

                    publishHTML(target: [
                        allowMissing         : true,
                        alwaysLinkToLastBuild: true,
                        keepAll              : true,
                        reportDir            : 'target/site/jacoco',
                        reportFiles          : 'index.html',
                        reportName           : 'JaCoCo Coverage Report'
                    ])
                }
            }
        }

        stage('Build') {
            steps {
                echo 'Building application JAR...'
                bat 'mvn package -DskipTests -B'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar',
                                     fingerprint: true
                }
            }
        }

        stage('Docker Build') {
            steps {
                echo "Building Docker image: ${DOCKER_IMAGE}:${DOCKER_TAG}"
                bat """
                    docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} --no-cache .
                """
            }
        }

        stage('Docker Push') {
            steps {
                echo "Pushing Docker image to registry..."
                script {
                    docker.withRegistry("https://${DOCKER_REGISTRY}", DOCKERHUB_CRED) {
                        dockerImage.push("${DOCKER_TAG}")
                        dockerImage.push('latest')
                    }
                }
            }
            post {
                always {
                    bat "docker rmi ${DOCKER_IMAGE}:${DOCKER_TAG} || true"
                    bat "docker rmi ${DOCKER_IMAGE}:latest || true"
                }
            }
        }

        stage('Deploy') {
            when {
                anyOf {
                    branch 'main'
                    branch 'master'
                }
            }
            steps {
                echo "Deploying to production server: ${DEPLOY_SERVER_IP}"
                withCredentials([
                    sshUserPrivateKey(
                        credentialsId: DEPLOY_SSH_CRED,
                        keyFileVariable: 'SSH_KEY',
                        usernameVariable: 'SSH_USER'
                    ),
                    file(credentialsId: ENV_FILE_CRED, variable: 'ENV_FILE')
                ]) {
                    bat '''
                        scp -i ${SSH_KEY} \
                            -o StrictHostKeyChecking=no \
                            ${ENV_FILE} \
                            ${SSH_USER}@${DEPLOY_SERVER_IP}:/opt/smart-city-hub/.env

                        scp -i ${SSH_KEY} \
                            -o StrictHostKeyChecking=no \
                            docker-compose.prod.yml \
                            ${SSH_USER}@${DEPLOY_SERVER_IP}:/opt/smart-city-hub/docker-compose.prod.yml

                        ssh -i ${SSH_KEY} \
                            -o StrictHostKeyChecking=no \
                            ${SSH_USER}@${DEPLOY_SERVER_IP} << EOF
                                set -e
                                cd /opt/smart-city-hub

                                export DOCKER_IMAGE=''' + DOCKER_IMAGE + '''
                                export DOCKER_TAG=''' + DOCKER_TAG + '''

                                docker-compose -f docker-compose.prod.yml pull app

                                docker-compose -f docker-compose.prod.yml up -d --no-deps app

                                docker image prune -f
EOF
                    '''
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline succeeded for build #${env.BUILD_NUMBER}"
        }
        failure {
            echo "Pipeline FAILED for build #${env.BUILD_NUMBER}"
        }
        always {
            cleanWs()
        }
    }
}
