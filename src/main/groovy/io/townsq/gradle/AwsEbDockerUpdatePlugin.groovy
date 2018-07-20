package io.townsq.gradle

import com.amazonaws.services.elasticbeanstalk.AWSElasticBeanstalkClientBuilder
import com.amazonaws.services.elasticbeanstalk.model.CreateApplicationVersionRequest
import com.amazonaws.services.elasticbeanstalk.model.DescribeApplicationVersionsRequest
import com.amazonaws.services.elasticbeanstalk.model.DescribeEnvironmentsRequest
import com.amazonaws.services.elasticbeanstalk.model.S3Location
import com.amazonaws.services.elasticbeanstalk.model.UpdateEnvironmentRequest
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import org.gradle.api.Plugin
import org.gradle.api.Project

class AwsEbDockerUpdatePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.task('updateAwsEbApplicationEnvironment') {
            group = 'AWS Elastic Beanstalk Update'
            description = 'Update a AWS Elastic Beanstalk application environment with a new version'

            doLast {
                def key = "${project.applicationName}/${project.applicationVersion}"
                def beanstalk = AWSElasticBeanstalkClientBuilder.defaultClient()

                def describeVersionRequest = new DescribeApplicationVersionsRequest()
                        .withApplicationName(project.applicationName)
                        .withVersionLabels([project.applicationVersion])

                def describeVersionResult = beanstalk.describeApplicationVersions describeVersionRequest

                if (describeVersionResult.applicationVersions.empty) {
                    def path = Paths.get "${project.buildDir}/aws/eb/${project.name}-${project.version}.zip"
                    def stream = Files.newInputStream path

                    def metadata = new ObjectMetadata()
                    metadata.setContentLength(stream.available())

                    def zip = new PutObjectRequest(project.bucketName, key, stream, metadata)

                    AmazonS3ClientBuilder.defaultClient().putObject zip

                    def bundle = new S3Location(project.bucketName, key)

                    def createVersionRequest = new CreateApplicationVersionRequest()
                            .withApplicationName(project.applicationName)
                            .withVersionLabel(project.applicationVersion)
                            .withSourceBundle(bundle)

                    beanstalk.createApplicationVersion createVersionRequest
                }

                def updateEnvironmentRequest = new UpdateEnvironmentRequest()
                        .withApplicationName(project.applicationName)
                        .withVersionLabel(project.applicationVersion)
                        .withEnvironmentName(project.applicationEnvironment)

                beanstalk.updateEnvironment updateEnvironmentRequest
            }
        }

        project.task('monitorAwsEbApplicationUpdate') {
            group = 'AWS Elastic Beanstalk Update'
            description = 'Monitor an AWS Elastic Beanstalk application environment health'

            doLast {
                def beanstalk = AWSElasticBeanstalkClientBuilder.defaultClient()
                def request = new DescribeEnvironmentsRequest()
                        .withApplicationName(project.applicationName)
                        .withEnvironmentNames([project.applicationEnvironment])
                        .withVersionLabel(project.applicationVersion)

                def status = 'Updating'
                def health = 'Unknown'

                def timeout = (project.timeout as int) * 60
                def start = LocalDateTime.now()
                def time = ChronoUnit.SECONDS.between start, LocalDateTime.now()

                println "Status: $status..."

                while (time < timeout && status != 'Ready') {
                    sleep 15000

                    def result = beanstalk.describeEnvironments request
                    def minutes = ChronoUnit.MINUTES.between start, LocalDateTime.now()

                    time = ChronoUnit.SECONDS.between start, LocalDateTime.now()

                    if (! result.environments.empty) {
                        status = result.environments.first().status
                        health = result.environments.first().healthStatus
                    }

                    println "Timeout: $timeout Time: $time Condition: ${time < timeout}"
                    println "Status: $status after ${time == 0 ? '' : "$minutes min "}${time % 60} sec..."
                }

                if (time >= timeout) {
                    throw new IllegalStateException('Monitoring timed out')
                }

                if (health != 'Ok') {
                    throw new IllegalStateException("Environment update failed: health status $health")
                }
                println "Environment successfully updated"
            }
        }

    }

}
