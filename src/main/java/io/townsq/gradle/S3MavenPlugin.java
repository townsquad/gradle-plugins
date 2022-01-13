package io.townsq.gradle;

import static java.lang.String.format;
import static java.lang.System.getenv;
import static java.util.Optional.ofNullable;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.credentials.AwsCredentials;
import org.gradle.api.internal.artifacts.repositories.DefaultMavenArtifactRepository;
import org.gradle.api.logging.Logger;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.internal.credentials.DefaultAwsCredentials;

public class S3MavenPlugin implements Plugin<Project> {

    Logger log;
    AwsCredentials credentials;

    @Override
    public void apply(Project project) {
        this.log = project.getLogger();
        this.credentials = awsCredentials(project);
        
        project.getAllprojects().forEach(this::evaluate);
    }

    public AwsCredentials awsCredentials(Project project) {
        log.info("Resolving AWS credentials");

        var extension = project.getExtensions().create("s3maven", S3MavenPluginExtension.class);
        var access = ofNullable(getenv(extension.accessKeyEnvironmentVariable)).orElse("none");
        var secret = ofNullable(getenv(extension.secretKeyEnvironmentVariable)).orElse("none");
        var fromVariables = new BasicAWSCredentials(access, secret);
        var fromEnvironment = new AWSStaticCredentialsProvider(fromVariables);
        var fromProfile = new ProfileCredentialsProvider(extension.profile);
        var chain = new AWSCredentialsProviderChain(fromProfile, fromEnvironment);
        var resolved = chain.getCredentials();
        var credentials = new DefaultAwsCredentials();

        credentials.setAccessKey(resolved.getAWSAccessKeyId());
        credentials.setSecretKey(resolved.getAWSSecretKey());

        return credentials;
    }

    void evaluate(Project wrapper) {
        wrapper.afterEvaluate(project -> {
            project.getRepositories().all(this::evaluate);

            ofNullable(project.getExtensions().findByType(PublishingExtension.class)).ifPresent(extension -> {
                extension.getRepositories().all(this::evaluate);
            });
        });
    }

    void evaluate(ArtifactRepository repo) {
        if (repo instanceof MavenArtifactRepository) {
            var mavenRepo = (DefaultMavenArtifactRepository) repo;
            var url = mavenRepo.getUrl();

            if ("s3".equals(url.getScheme())) {
                mavenRepo.setConfiguredCredentials(credentials);
                log.info(format("Set AWS credentials for repo %s", url));
            }
        }
    }

}
