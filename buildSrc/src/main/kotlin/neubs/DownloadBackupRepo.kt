/*
 * Copyright (C) 2024 NotEnoughUpdates contributors
 */

package neubs

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.*
import java.net.URL

abstract class DownloadBackupRepo : DefaultTask() {

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @get:Input
    abstract val branch: Property<String>

    @get:Internal
    val repoFile get() = outputDirectory.get().asFile.resolve("assets/notenoughupdates/repo.zip")

    @TaskAction
    fun downloadRepo() {
        val url =
            URL("https://github.com/NotEnoughUpdates/NotEnoughUpdates-REPO/archive/refs/heads/${branch.get()}.zip")
        val file = repoFile
        file.parentFile.mkdirs()
        file.outputStream().use { out ->
            url.openStream().use { inp ->
                inp.copyTo(out)
            }
        }
    }
}
