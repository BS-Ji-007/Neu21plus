/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
 */

package neubs

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.*

fun Project.applyPublishingInformation(
    vararg artifacts: Pair<String, Any>
) {
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
                    description.set("A feature rich 1.26.1 Minecraft fabric mod for Hypixel Skyblock")
                    licenses {
                        license {
                            name.set("GNU Lesser General Public License")
                            url.set("https://github.com/NotEnoughUpdates/NotEnoughUpdates/blob/master/COPYING.LESSER")
                        }
                    }
                    developers {
                        developer {
                            name.set("Moulberry")
                        }
                        developer {
                            name.set("The NotEnoughUpdates Contributors and Maintainers")
                        }
                    }
                }
            }
        }
    }

}
