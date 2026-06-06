package neubs

import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

fun Project.setVersionFromEnvironment(): String {
    fun runCmd(timeoutSeconds: Long = 5, vararg cmd: String): String {
        return try {
            val pb = ProcessBuilder(*cmd).redirectErrorStream(true)
            val proc = pb.start()
            val output = proc.inputStream.bufferedReader().use { it.readText() }
            proc.waitFor(timeoutSeconds, TimeUnit.SECONDS)
            output.trim()
        } catch (t: Throwable) {
            ""
        }
    }

    val baseVersion = runCmd(5, "git", "describe", "--tags", "--abbrev=0").ifEmpty { "2.1.1" }

    val buildExtra = mutableListOf<String>()
    val buildVersion = properties["BUILD_VERSION"] as? String
    if (!buildVersion.isNullOrEmpty()) buildExtra.add(buildVersion)
    if (System.getenv("CI") == "true") buildExtra.add("ci")

    val shortHash = runCmd(5, "git", "rev-parse", "--short", "HEAD")
    if (shortHash.isNotEmpty()) buildExtra.add(shortHash)

    val gitStatus = runCmd(5, "git", "status", "--porcelain")
    if (gitStatus.isNotEmpty()) buildExtra.add("dirty")

    version = baseVersion + (if (buildExtra.isEmpty()) "" else buildExtra.joinToString(prefix = "+", separator = "."))
    return baseVersion
}
