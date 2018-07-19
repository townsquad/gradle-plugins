package io.townsq.gradle

import groovy.json.JsonBuilder
import java.nio.file.Files
import java.nio.file.Paths
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Zip

class AwsEbDockerBundlePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.task('createDockerrunAwsJson') {
            group = 'AWS EB Docker Bundle'
            description = 'Create Dockerrun.aws.json'

            doLast {
                def authentication = [
                        Bucket: project.dockerConfigBucket,
                        Key   : 'config.json'
                ]

                def image = [
                        Name  : "${project.dockerImageName}:${project.applicationVersion}",
                        Update: 'true'
                ]

                def port = [
                        ContainerPort: project.applicationPort
                ]

                def dockerrun = [
                        AWSEBDockerrunVersion: '1',
                        Authentication       : authentication,
                        Image                : image,
                        Ports                : [port]
                ]

                def distribution = "${project.buildDir}/aws/eb"
                def folder = Paths.get distribution

                if (Files.notExists(folder)) {
                    Files.createDirectories folder
                }

                def path = "$distribution/Dockerrun.aws.json"
                def file = Paths.get path

                if (Files.notExists(file)) {
                    Files.createFile file
                }

                def json = new JsonBuilder(dockerrun)

                file.write json.toPrettyString()
            }
        }

        project.task('copyEbExtensionsFolder', type: Copy) {
            group = 'AWS EB Docker Bundle'
            description = 'Copy .ebextensions from src/main/resources/.ebextensions'

            from "${project.buildDir}/resources/main/.ebextensions"
            into "${project.buildDir}/aws/eb/.ebextensions"
        }

        project.task('bundleAwsEbDockerDescriptors', type: Zip) {
            group = 'AWS EB Docker Bundle'
            description = 'Bundle .ebextensions and Dockerrun.aws.json for upload into build/distributions/'

            from "${project.buildDir}/aws/eb"
        }
    }

}
