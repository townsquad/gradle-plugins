# Gradle plugins from TownSq team

### S3 Maven Repositories Plugin

Gradle plugin that resolves AWS credentials for private Maven
 repositories assembled on AWS S3.

###### Usage

- Declare plugin on `build.gradle`

 ```
 plugins {
    id 'io.townsq.s3-maven' version '1.0.0-snapshot' 
 }
 ```

- The AWS credentials are read from either environment variables
 or local AWS profile, which are named, by default, as following:
 
  - environment variables `MAVEN_IAM_USER_ACCESS_KEY`
   and `MAVEN_IAM_USER_SECRET_KEY`
   
  - profile `maven` from `~/.aws/credentials`
  
  In order to change those names, add `s3Maven` extension to
   `build.gradle`, with either of the following:
   
  ```
    s3Maven {
       profile = 'profile-name'
    }
  ```
    or
  ```
    s3Maven {
       accessKeyEnvironmentVariable = 'access-key-variable-name'
       secretKeyEnvironmentVariable = 'secret-key-variable-name'
    }
  ```

###### Credits

This plugin is source-based on [hamstercommnity's plugin](
 https://github.com/hamstercommunity/awsm-credentials-gradle),
 and extends it to support AWS credential resolution
 from environment variables.
 
### AWS Lambda Provisioning Plugin
 
 Gradle plugin that provides tasks for AWS Lambda Functions provisioning.
 
###### Usage
 
 - Declare plugin on `build.gradle`
 
  ```
  plugins {
     id 'io.townsq.aws-lambda-provisioning' version '1.0.0-snapshot' 
  }
  ```
 
 - The AWS credentials are, by default, read from environment variables `AWS_ACCESS_KEY_ID` and `AWS_ACCESS_SECRET_KEY`
    
 - The AWS region is, by default, read from environment variable `AWS_REGION`

### AWS Elastic Beanstalk Docker Bundle Plugin
 
 Gradle plugin that provides tasks for bundling deployment descriptors for AWS Elastic Beanstalk Single Docker applications.
 
###### Usage
 
 - Declare plugin on `build.gradle`
 
  ```
  plugins {
     id 'io.townsq.aws-eb-docker-bundle' version '1.0.0' 
  }
  ```
 
 - Copying `.ebextensions` from your repo to build temporary folder
 
 ```
 $ gradle copyEbExtensionsFolder
 ```
 
 - Generating `Dockerrun.aws.json` file descriptor
 
 ```
 $ gradle createDockerrunAwsJson \
    -PdockerImageName=<docker-image-name> \
    -PdockerImageTag=<docker-image-tag> \
    -PdockerConfigBucket=<aws-s3-bucket-name> \
    -PcontainerPorts=<comma-separated-list-of-ports-to-map>
 ```
 
  `dockerImageName` will be set on `Dockerrun.aws.json`
  
  `dockerImageTag` defines the Docker image tag to be used
 
  `dockerConfigBucket` names the AWS S3 bucket, which is expected to store `config.json` file containing potential private Docker repository credentials where the Docker image is hosted
  
  `containerPorts` is a comma-separated list of ports which will be mapped on the running container
 
 - Bundling `.ebextensions` and `Dockerrun.aws.json` together in a ZIP file
  
  ```
  $ gradle bundleAwsEbDockerDescriptors
  ```

### AWS Elastic Beanstalk Docker Update Plugin
 
 Gradle plugin that provides tasks for updating AWS Elastic Beanstalk Single Docker applications.
 
###### Usage
 
 - Declare plugin on `build.gradle`
 
  ```
  plugins {
     id 'io.townsq.aws-eb-docker-update' version '1.0.0' 
  }
  ```
  
 - Provide AWS credentials and region for both tasks (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` and `AWS_REGION`)
 
 - Updating AWS Elastic Beanstalk application environments
 
 ```
 $ gradle updateAwsEbApplicationEnvironment \
    -PapplicationName=<aws-eb-application-name> \
    -PapplicationEnvironment=<aws-eb-application-environment-name> \
    -PapplicationVersion=<aws-eb-application-version> \
    -PbucketName=<aws-s3-bucket-storing-aws-eb-application-deployment-bundles>
 ```
 
  `applicationName` is the name of the AWS Elastic Beanstalk application
  
  `applicationEnvironment` is the name of the AWS Elastic Beanstalk application environment
 
  `applicationVersion` labels the version of the AWS Elastic Beanstalk application that will be used to updated the environment
  
  `bucketName` refers to the AWS S3 bucket which stores deployment bundles for the AWS Elastic Beanstalk application
  
 - Monitoring AWS Elastic Beanstalk application environment updates
   
   ```
   $ gradle monitorAwsEbApplicationUpdate \
      -PapplicationName=<aws-eb-application-name> \
      -PapplicationEnvironment=<aws-eb-application-environment-name> \
      -PapplicationVersion=<aws-eb-application-version> \
      -Ptimeout=<monitor-timeout-in-minutes>
   ```
