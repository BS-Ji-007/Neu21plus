package neubs

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.*

fun Project.applyPublishingInformation(vararg artifacts: Pair<String, Any>) {
    this.configure<PublishingExtension> {
        publications {
            create<MavenPublication>("maven") {
                for((name, source) in artifacts) {
                    artifact(source) {
                        classifier = name
                    }
                }
                pom {
                    name.set("NotEnoughUpdates")
                }
            }
        }
    }
}
