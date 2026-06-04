/*
 * Copyright (C) 2024 NotEnoughUpdates contributors
 */

package neubs

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.*
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.util.*

abstract class CustomSignTask : DefaultTask() {

    @TaskAction
    fun run() {
        println("Hash to sign: ")
        val hash = readLine()!!.trim().uppercase()
        require(hash.matches("[A-F0-9]{64}".toRegex())) { "Please provide a valid sha256 hash" }
        val secrets = project.file("secrets").listFiles()?.toList()
            ?.filter { !it.name.startsWith(".") } ?: emptyList()

        if (secrets.isEmpty()) error("Could not find any secret files.")
        secrets.forEach { require(it.name.endsWith(".der")) { "Invalid secret file ${it.name}" } }
        project.file("build/signatures").mkdirs()
        for (secret in secrets) {
            val keySpec = PKCS8EncodedKeySpec(secret.readBytes())
            val signature = Signature.getInstance("SHA256withRSA")
            signature.initSign(KeyFactory.getInstance("RSA").generatePrivate(keySpec))
            signature.update(hash.encodeToByteArray())
            val file = project.file("build/signatures/_${secret.nameWithoutExtension}.asc")
            file.writeBytes(signature.sign())
            println("Generated signature at ${file.absolutePath}")
        }
    }

    init {
        outputs.upToDateWhen { false }
    }
}
