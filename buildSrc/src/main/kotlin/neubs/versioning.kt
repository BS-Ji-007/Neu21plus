package neubs

import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import java.util.*

fun Project.setVersionFromEnvironment(): String {
    val baseVersion = run {
        val baos = ByteArrayOutputStream()
        this.exec {
            it.commandLine("git", "describe", "--tags", "--abbrev=0")
            it.standardOutput = baos
            it.isIgnoreExitValue = true
        }
        baos.toString().trim()
    }
    
    val buildExtra = mutableListOf<String>()
    val buildVersion = properties["BUILD_VERSION"] as? String
    if (buildVersion != null) buildExtra.add(buildVersion)
    if (System.getenv("CI") == "true" && System.getenv("NEU_RELEASE") != "true") buildExtra.add("ci")

    val stdout = ByteArrayOutputStream()
    this.exec {
        it.commandLine("git", "rev-parse", "--short", "HEAD")
        it.standardOutput = stdout
        it.isIgnoreExitValue = true
    }
    if (stdout.toString().trim().isNotEmpty()) {
        buildExtra.add(stdout.toString().trim())
    }

    val gitDiffStdout = ByteArrayOutputStream()
    this.exec {
        it.commandLine("git", "status", "--porcelain")
        it.standardOutput = gitDiffStdout
        it.isIgnoreExitValue = true
    }
    if (gitDiffStdout.toString().trim().isNotEmpty()) {
        buildExtra.add("dirty")
    }

    version = baseVersion + (if (buildExtra.isEmpty()) "" else buildExtra.joinToString(prefix = "+", separator = "."))
    return baseVersion
}
