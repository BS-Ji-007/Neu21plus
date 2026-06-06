package neubs

import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.gradle.api.tasks.Exec
import java.io.ByteArrayOutputStream
import java.util.*

fun Project.setVersionFromEnvironment(): String {
    val baos = ByteArrayOutputStream()
    this@setVersionFromEnvironment.exec {
        commandLine("git", "describe", "--tags", "--abbrev=0")
        standardOutput = baos
        isIgnoreExitValue = true
    }
    val baseVersion = baos.toString().trim().ifEmpty { "2.1.1" }
    
    val buildExtra = mutableListOf<String>()
    val buildVersion = properties["BUILD_VERSION"] as? String
    if (buildVersion != null) buildExtra.add(buildVersion)
    if (System.getenv("CI") == "true") buildExtra.add("ci")

    val stdout = ByteArrayOutputStream()
    this.exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
        standardOutput = stdout
        isIgnoreExitValue = true
    }
    val shortHash = stdout.toString().trim()
    if (shortHash.isNotEmpty()) {
        buildExtra.add(shortHash)
    }

    version = baseVersion + (if (buildExtra.isEmpty()) "" else buildExtra.joinToString(prefix = "+", separator = "."))
    return baseVersion
}
