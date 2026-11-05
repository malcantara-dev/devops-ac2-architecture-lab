// Pipeline do Core (Desafio 6). O ia-service tem o seu proprio: ia-service/Jenkinsfile.
pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    triggers {
        pollSCM('H/5 * * * *')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Testes') {
            steps {
                sh 'chmod +x mvnw && ./mvnw -B test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        // Bloqueia o deploy: testes de arquitetura (ArchUnit) ja rodaram acima;
        // aqui o JaCoCo exige 90% de linhas no dominio.
        stage('Gate de cobertura') {
            steps {
                sh './mvnw -B verify -DskipTests=false'
            }
            post {
                always {
                    archiveArtifacts artifacts: 'target/site/jacoco/**', allowEmptyArchive: true
                }
            }
        }

        stage('Imagem') {
            steps {
                sh './mvnw -B -q package -DskipTests'
                sh 'docker build -t gamificacao-core:${BUILD_NUMBER} -t gamificacao-core:latest .'
            }
        }

        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                sh 'docker compose up -d --no-deps --build app'
            }
        }
    }
}
