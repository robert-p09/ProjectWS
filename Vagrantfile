# -*- mode: ruby -*-
# vi: set ft=ruby :

# ssh-keygen -t rsa -b 4096 -C "ansible"

$os_packages_update = <<SCRIPT
echo "Update OS packages"
apt update && apt upgrade -y
SCRIPT

$user_setup = <<SCRIPT
#!/bin/bash

function create_user {
  USER_EXISTS=0
  USERS=`getent passwd | cut -d":" -f1`
  
  for USER in $USERS;
  do
    if [[ $1 == $USER ]]; then
      echo "$1 user exists"
      USER_EXISTS=1
    fi
  done

  if [[ $USER_EXISTS -eq 0 ]]; then
    echo "Creating user: $1"
    adduser --disabled-password --gecos "" $1
  fi
}

function add_to_sudoers {

  if [ ! -f /etc/sudoers.d/$1 ]; then
    echo "Granting sudo access for user: $1"
    echo "$1 ALL=(ALL) NOPASSWD:ALL" > /etc/sudoers.d/$1
  else
    echo "User has been already added to sudoers"
  fi
}

create_user "jenkins"
add_to_sudoers "jenkins"

SCRIPT

$ssh_key_setup = <<SCRIPT
#!/bin/bash
function set_authorized_key {
  if [[ ! -d /home/$1/.ssh ]]; then
    echo "Creating .ssh folder for $1 ssh access"
    mkdir /home/$1/.ssh
  fi
  
  if [[ ! -f /vagrant/sshkey/id_ed25519 ]]; then
    echo "Make sure the vagrant folder is mounted and the key exists!"
    exit 1
  fi

  cp /vagrant/sshkey/id_ed25519 /home/$1/.ssh

  chown -R $1:$1 /home/$1/.ssh
  chmod 600 /home/$1/.ssh/id_ed25519
}


set_authorized_key "jenkins"

SCRIPT


$ssh_cpkey = <<SCRIPT
#!/bin/bash

function copy_public_key {
  
  
  if [[ ! -f /vagrant/sshkey/id_ed25519.pub ]]; then
    echo "Make sure the vagrant folder is mounted and the file exists!"
    exit 1
  fi

  cp -v /vagrant/sshkey/id_ed25519.pub /home/vagrant/.ssh
  chmod 700 /home/vagrant/.ssh
  cat /home/vagrant/.ssh/id_ed25519.pub >> /home/vagrant/.ssh/authorized_keys
  chmod 600 /home/vagrant/.ssh/authorized_keys

  
}

copy_public_key

SCRIPT

$docker_install = <<SCRIPT
#!/bin/bash
echo "Installing docker"
apt install docker.io -y
SCRIPT

$dockercomp_install = <<SCRIPT
#!/bin/bash
echo "Installing docker compose"
DOCKER_CONFIG=${DOCKER_CONFIG:-$HOME/.docker}
mkdir -p $DOCKER_CONFIG/cli-plugins
curl -SL https://github.com/docker/compose/releases/download/v2.5.0/docker-compose-linux-x86_64 \
      -o $DOCKER_CONFIG/cli-plugins/docker-compose
chmod +x $DOCKER_CONFIG/cli-plugins/docker-compose      
SCRIPT

$jenkins_setup_war_package = <<SCRIPT
#!/bin/bash

JENKINS_URL="https://get.jenkins.io/war/2.349/jenkins.war"

echo "Installing Jenkins dependencies..."
apt install openjdk-11-jre -y

# Check if Jenkins dir exists
if [ ! -d /opt/jenkins ]; then
   mkdir /opt/jenkins
fi

# Check whether jenkins.war file exists
if [ ! -f /opt/jenkins/jenkins.war ]; then
   wget $JENKINS_URL --directory-prefix=/opt/jenkins
fi

if [ ! -d /opt/jenkins/plugins ]; then
   mkdir /opt/jenkins/plugins
fi

if [ ! -f /vagrant/Jenkinsfiles/plugins.txt ]; then
   echo "Make sure vagrant folder is mounted and/or the plugins.txt file exists" 
exit
fi

PLUGINS=$(cat /vagrant/Jenkinsfiles/plugins.txt) 
for PLUGIN in $PLUGINS; 
do
   
   JPI=`echo $PLUGIN | cut -d"." -f1`.jpi
   PLUGIN_URL=https://updates.jenkins-ci.org/latest/$PLUGIN
   
   
    echo "Downloading $PLUGIN..."
    wget -O /opt/jenkins/plugins/$JPI "$PLUGIN_URL"
   
   
done

chown -R jenkins:jenkins /opt/jenkins

rm -v -rf /opt/jenkins/init.groovy.d
mkdir -v /opt/jenkins/init.groovy.d
cp -v /vagrant/Jenkinsfiles/SetAdminEmail.groovy /opt/jenkins/init.groovy.d

if [ ! -f /lib/systemd/system/jenkins.service ]; then
   if [ ! -f /vagrant/Jenkinsfiles/jenkins.service ]; then
      echo "Make sure vagrant folder is mounted and/or the jenkins.service file exists" 
      exit
   fi

   
   cp /vagrant/Jenkinsfiles/jenkins.service /lib/systemd/system 

   systemctl daemon-reload
   systemctl enable jenkins.service 
   systemctl start jenkins.service

  
fi



SCRIPT



Vagrant.configure("2") do |config|
  config.vm.define "jenkins" do |j|
      j.vm.box = "ubuntu/focal64"
      j.vm.hostname = "jenkins"
      j.vm.network "private_network", ip: "192.168.56.80"
      j.vm.provision "shell", :inline => $os_packages_update
      j.vm.provision "shell", :inline => $user_setup
      j.vm.provision "shell", :inline => $ssh_key_setup  
      j.vm.provision "shell", :inline => $jenkins_setup_war_package
      j.vm.provision "shell", :inline => $docker_install
      
      j.vm.provider "virtualbox" do |vb|
          
          vb.cpus = 2
          vb.memory = 2048
      end

      
      
  end
 
  config.vm.define "app_server" do |app|
    app.vm.box = "ubuntu/focal64"
    app.vm.hostname = "appserver"
    app.vm.network "private_network", ip: "192.168.56.90"
    app.vm.provision "shell", :inline => $os_packages_update
    app.vm.provision "shell", :inline => $ssh_cpkey
    app.vm.provision "shell", :inline => $docker_install
    app.vm.provision "shell", :inline => $dockercomp_install
    app.vm.provider "virtualbox" do |vb|
        
        vb.cpus = 2
        vb.memory = 2048
    end

    

  end
  
end
