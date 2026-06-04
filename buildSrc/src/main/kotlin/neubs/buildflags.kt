package neubs

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.WriteProperties
import org.gradle.kotlin.dsl.*
import java.nio.charset.StandardCharsets

const val NEU_BUILDFLAGS_PREFIX = "neu.buildflags."

class NEUBuildFlags : Plugin<Project> {

    override fun apply(target: Project) {
        val props = target.properties
            .filterKeys { it.startsWith(NEU_BUILDFLAGS_PREFIX) }
            .mapValues { it.value as String }

        target.extensions.add("buildflags", Extension(props))

        target.tasks.register<WriteProperties>("generateBuildFlags") {
            encoding = StandardCharsets.UTF_8.name()
            setProperties(props)
            comment = "Store build time configuration for NEU"
            // Gradle 9 modern property
            destinationFile.set(target.layout.buildDirectory.file("buildflags.properties"))
        }
    }

    class Extension(val props: Map<String, String>) {
        fun bool(name: String) = props["$NEU_BUILDFLAGS_PREFIX$name"] == "true"
    }
}

val Project.buildFlags: NEUBuildFlags.Extension
    get() = the<NEUBuildFlags.Extension>()
