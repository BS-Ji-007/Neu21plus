package neubs

import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import java.io.ByteArrayOutputStream
import java.util.*

fun Project.setVersionFromEnvironment(): String {
    val baos = ByteArrayOutputStream()
    exec {
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
    exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
        standardOutput = stdout
        isIgnoreExitValue = true
    }
    val shortHash = stdout.toString().trim()
    if (shortHash.isNotEmpty()) {
        buildExtra.add(shortHash)
    }

    val statusBaos = ByteArrayOutputStream()
    exec {
        commandLine("git", "status", "--porcelain")
        standardOutput = statusBaos
        isIgnoreExitValue = true
    }
    if (statusBaos.toString().trim().isNotEmpty()) {
        buildExtra.add("dirty")
    }

    version = baseVersion + (if (buildExtra.isEmpty()) "" else buildExtra.joinToString(prefix = "+", separator = "."))
    return baseVersion
}
