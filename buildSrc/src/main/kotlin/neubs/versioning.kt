package neubs

import org.gradle.api.Project
import java.io.File
import java.lang.ProcessBuilder
import java.util.*

fun Project.setVersionFromEnvironment(): String {
    val root = this.projectDir

    fun git(vararg args: String): String {
        return try {
            val pb = ProcessBuilder("git", *args)
            pb.directory(root)
            val process = pb.start()
            val output = process.inputStream.bufferedReader().readText().trim()
            process.waitFor()
            output
        } catch (e: Exception) {
            ""
        }
    }

    val baseVersion = git("describe", "--tags", "--abbrev=0").ifEmpty { "2.1.1" }
    
    val buildExtra = mutableListOf<String>()
    val buildVersion = properties["BUILD_VERSION"] as? String
    if (buildVersion != null) buildExtra.add(buildVersion)
    if (System.getenv("CI") == "true") buildExtra.add("ci")

    val shortHash = git("rev-parse", "--short", "HEAD")
    if (shortHash.isNotEmpty()) buildExtra.add(shortHash)

    version = baseVersion + (if (buildExtra.isEmpty()) "" else buildExtra.joinToString(prefix = "+", separator = "."))
    return baseVersion
}
