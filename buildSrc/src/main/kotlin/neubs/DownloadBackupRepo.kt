package neubs

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.net.URL

abstract class DownloadBackupRepo : DefaultTask() {
    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Input
    abstract val branch: Property<String>

    @TaskAction
    fun downloadRepo() {
        val url = URL("https://github.com/NotEnoughUpdates/NotEnoughUpdates-REPO/archive/refs/heads/${branch.get()}.zip")
        val file = outputDirectory.get().asFile.resolve("assets/notenoughupdates/repo.zip")
        file.parentFile.mkdirs()
        file.outputStream().use { out ->
            url.openStream().use { inp ->
                inp.copyTo(out)
            }
        }
    }
}
