package io.townsq.gradle;

public class S3MavenPluginExtension {
    public String profile = "maven";
    public String accessKeyEnvironmentVariable = "MAVEN_IAM_USER_ACCESS_KEY";
    public String secretKeyEnvironmentVariable = "MAVEN_IAM_USER_SECRET_KEY";
}
