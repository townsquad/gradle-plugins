package io.townsq.gradle

import com.amazonaws.auth.AWSCredentialsProviderChain
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.credentials.AwsCredentials
import org.gradle.api.internal.artifacts.repositories.DefaultMavenArtifactRepository
import org.gradle.api.logging.Logger
import org.gradle.api.publish.PublishingExtension
import org.gradle.internal.credentials.DefaultAwsCredentials

class S3MavenPlugin implements Plugin<Project> {

    Logger log
    AwsCredentials credentials
    DependencySet dependencies

    @Override
    void apply(Project project) {
        this.log = project.logger
        this.credentials = awsCredentials project
        this.dependencies = project.configurations.compile.allDependencies
        project.allprojects.each { evaluate(it) }
    }

    AwsCredentials awsCredentials(Project project) {
        def extension = project.extensions.create 's3maven', S3MavenPluginExtension
        def access = System.getenv(extension.accessKeyEnvironmentVariable) ?: 'none'
        def secret = System.getenv(extension.secretKeyEnvironmentVariable) ?: 'none'
        def fromVariables = new BasicAWSCredentials(access, secret)
        def fromEnvironment = new AWSStaticCredentialsProvider(fromVariables)
        def fromProfile = new ProfileCredentialsProvider(extension.profile)
        def chain = new AWSCredentialsProviderChain(fromProfile, fromEnvironment)

        log.info 'Resolving AWS credentials'
        return new DefaultAwsCredentials(
                accessKey: chain.credentials.AWSAccessKeyId,
                secretKey: chain.credentials.AWSSecretKey
        )
    }

    void evaluate(Project project) {
        project.afterEvaluate {
            project.repositories.all { evaluate(it) }

            def extension = project.extensions.findByType PublishingExtension

            if (extension) {
                extension.repositories.all { evaluate(it) }
            }
        }
    }

    void evaluate(ArtifactRepository repo) {
        if (repo instanceof MavenArtifactRepository) {
            def mavenRepo = repo as DefaultMavenArtifactRepository
            def url = mavenRepo.url

            if (url?.scheme == 's3') {
                mavenRepo.configuredCredentials = credentials
                log.info "Set AWS credentials for repo $url"
            }
        }
    }

}
