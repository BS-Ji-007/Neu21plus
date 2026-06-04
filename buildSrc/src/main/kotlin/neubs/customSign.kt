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
        val hash = readLine()?.trim()?.uppercase() ?: return
        val secrets = project.file("secrets").listFiles()?.toList()
            ?.filter { !it.name.startsWith(".") } ?: emptyList()

        if (secrets.isEmpty()) return
        
        project.file("build/signatures").mkdirs()
        for (secret in secrets) {
            if (!secret.name.endsWith(".der")) continue
            val keySpec = PKCS8EncodedKeySpec(secret.readBytes())
            val signature = Signature.getInstance("SHA256withRSA")
            signature.initSign(KeyFactory.getInstance("RSA").generatePrivate(keySpec))
            signature.update(hash.encodeToByteArray())
            val file = project.file("build/signatures/_${secret.nameWithoutExtension}.asc")
            file.writeBytes(signature.sign())
        }
    }
}
