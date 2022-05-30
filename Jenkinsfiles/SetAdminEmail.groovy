#!groovy

import jenkins.model.*
import hudson.security.*

def jenkinsLocationConfiguration = JenkinsLocationConfiguration.get()

jenkinsLocationConfiguration.setAdminAddress("jenkins <jenkins@mail.com>")

jenkinsLocationConfiguration.save()

instance.save()