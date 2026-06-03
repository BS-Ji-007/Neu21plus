package neubs

import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import java.util.*

fun Project.setVersionFromEnvironment(): String {
    val baos = ByteArrayOutputStream()
    this.project.exec {
        commandLine("git", "describe", "--tags", "--abbrev=0")
        standardOutput = baos
        isIgnoreExitValue = true
    }
    val baseVersion = (baos.toByteArray()).decodeToString().trim()
    
    val buildExtra = mutableListOf<String>()
    val buildVersion = properties["BUILD_VERSION"] as? String
    if (buildVersion != null) buildExtra.add(buildVersion)
    if (System.getenv("CI") == "true" && System.getenv("NEU_RELEASE") != "true") buildExtra.add("ci")

    val stdout = ByteArrayOutputStream()
    val execResult = this.project.exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
        standardOutput = stdout
        isIgnoreExitValue = true
    }
    if (execResult.exitValue == 0) {
        buildExtra.add(String(stdout.toByteArray()).trim())
    }

    val gitDiffStdout = ByteArrayOutputStream()
    val gitDiffResult = this.project.exec {
        commandLine("git", "status", "--porcelain")
        standardOutput = gitDiffStdout
        isIgnoreExitValue = true
    }
    if (gitDiffResult.exitValue == 0 && gitDiffStdout.toByteArray().isNotEmpty()) {
        buildExtra.add("dirty")
    }

    version = baseVersion + (if (buildExtra.isEmpty()) "" else buildExtra.joinToString(prefix = "+", separator = "."))
    return baseVersion
}
