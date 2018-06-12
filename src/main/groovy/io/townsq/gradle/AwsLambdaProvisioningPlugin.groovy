package io.townsq.gradle

import com.amazonaws.services.lambda.AWSLambdaClientBuilder
import com.amazonaws.services.lambda.model.UpdateFunctionCodeRequest
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import java.nio.file.Files
import java.nio.file.Paths
import org.gradle.api.Plugin
import org.gradle.api.Project

class AwsLambdaProvisioningPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.task('updateFunction') {

            group = 'AWS Lambda Provisioning'
            description = 'Update Lambda Function'

            doLast {
                def path = Paths.get project.functionCode
                def stream = Files.newInputStream path

                def key = "${project.functionName}/${project.functionVersion}.zip"
                def metadata = new ObjectMetadata()
                metadata.setContentLength(stream.available())

                def zip = new PutObjectRequest(project.bucketName, key, stream, metadata)

                AmazonS3ClientBuilder.defaultClient().putObject zip

                def request = new UpdateFunctionCodeRequest()
                        .withFunctionName(project.functionName)
                        .withS3Bucket(project.bucketName)
                        .withS3Key(key)

                AWSLambdaClientBuilder.defaultClient().updateFunctionCode request
            }
        }
    }

}
