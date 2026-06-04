package neubs

import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.*
import java.io.ByteArrayOutputStream
import java.util.*

fun Project.setVersionFromEnvironment(): String {
    val baos = ByteArrayOutputStream()
    this.exec { spec ->
        spec.commandLine("git", "describe", "--tags", "--abbrev=0")
        spec.standardOutput = baos
        spec.isIgnoreExitValue = true
    }
    val baseVersion = baos.toString().trim()
    
    val buildExtra = mutableListOf<String>()
    val buildVersion = properties["BUILD_VERSION"] as? String
    if (buildVersion != null) buildExtra.add(buildVersion)
    if (System.getenv("CI") == "true" && System.getenv("NEU_RELEASE") != "true") buildExtra.add("ci")

    val stdout = ByteArrayOutputStream()
    this.exec { spec ->
        spec.commandLine("git", "rev-parse", "--short", "HEAD")
        spec.standardOutput = stdout
        spec.isIgnoreExitValue = true
    }
    if (stdout.toString().trim().isNotEmpty()) {
        buildExtra.add(stdout.toString().trim())
    }

    val gitDiffStdout = ByteArrayOutputStream()
    this.exec { spec ->
        spec.commandLine("git", "status", "--porcelain")
        spec.standardOutput = gitDiffStdout
        spec.isIgnoreExitValue = true
    }
    if (gitDiffStdout.toString().trim().isNotEmpty()) {
        buildExtra.add("dirty")
    }

    version = baseVersion + (if (buildExtra.isEmpty()) "" else buildExtra.joinToString(prefix = "+", separator = "."))
    return baseVersion
}
