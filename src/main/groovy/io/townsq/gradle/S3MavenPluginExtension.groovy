package io.townsq.gradle

class S3MavenPluginExtension {
    String profile = 'maven'
    String accessKeyEnvironmentVariable = 'MAVEN_IAM_USER_ACCESS_KEY'
    String secretKeyEnvironmentVariable = 'MAVEN_IAM_USER_SECRET_KEY'
}
