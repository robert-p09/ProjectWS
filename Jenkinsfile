node {
    stage('Checkout'){
        git credentialsId: 'gitcred', url: 'https://github.com/robert-p09/ProjectWS'
    }
    
    stage('build docker images'){
        sh 'sudo docker build -f dockerfile.dr -t  robertp09/mydrupal .'
        sh 'sudo docker build -f dockerfile.lb -t  robertp09/load_balancer .'
    }

    stage('Login DockerHub'){
        withCredentials([string(credentialsId: 'docker-pwd', variable: 'dockerHubPwd')]) {
        sh 'sudo docker login -u robertp09 -p $dockerHubPwd'
        }
    }
    
    stage('Push Docker Image'){

        sh 'sudo docker push robertp09/mydrupal' 
        sh 'sudo docker push robertp09/load_balancer'
    }

     stage('Run Container on Server'){ 
        
        def DC = 'sudo docker-compose up -d'
        
        sshagent(['app-server']) {
        sh "ssh -o StrictHostKeyChecking=no jenkins@192.168.56.90 ${DC}"
    }

    }
}