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
 