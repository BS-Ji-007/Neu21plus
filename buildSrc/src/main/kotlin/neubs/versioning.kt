package neubs

import org.gradle.api.Project
import java.io.InputStream
import java.util.*

fun Project.setVersionFromEnvironment(): String {
    fun git(vararg args: String): String {
        return try {
            val process = ProcessBuilder("git", *args)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()
            process.inputStream.bufferedReader().readText().trim()
        } catch (e: Exception) {
            ""
        }
    }

    val baseVersion = git("describe", "--tags", "--abbrev=0")
    
    val buildExtra = mutableListOf<String>()
    val buildVersion = properties["BUILD_VERSION"] as? String
    if (buildVersion != null) buildExtra.add(buildVersion)
    if (System.getenv("CI") == "true" && System.getenv("NEU_RELEASE") != "true") buildExtra.add("ci")

    val shortHash = git("rev-parse", "--short", "HEAD")
    if (shortHash.isNotEmpty()) {
        buildExtra.add(shortHash)
    }

    val status = git("status", "--porcelain")
    if (status.isNotEmpty()) {
        buildExtra.add("dirty")
    }

    version = baseVersion + (if (buildExtra.isEmpty()) "" else buildExtra.joinToString(prefix = "+", separator = "."))
    return baseVersion
}
