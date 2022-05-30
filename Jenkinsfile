node {
    stage('Checkout'){
        git credentialsId: 'gitcred', url: 'https://github.com/robert-p09/ProjectWS'
    }
    
    stage('build docker images'){
        sh 'sudo docker build -f dockerfile.dr -t  robertp09/mydrupal .'
        sh 'sudo docker build -f dockerfile.lb -t  robertp09/load_balancer .'
    }

    stage('Push Docker Image'){
        withCredentials([string(credentialsId: 'docker-pwd', variable: 'dockerHubPwd')]) {
        sh "sudo docker login -u robertp09 -p ${dockerHubPwd}"
        }

        # sh 'sudo docker login -u robertp09 -p DockerHub'
        sh 'docker push robertp09/mydrupal' 
        sh 'docker push robertp09/load_balancer'
    }

    stage('Run Container on Server'){ 
        sshagent

        sh 'sudo docker-compose up -d'
    }
}