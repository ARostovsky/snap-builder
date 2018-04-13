package org.jetbrains.snapBuilder.kotlin

import org.apache.commons.io.FileUtils
import org.jetbrains.snapBuilder.common.run
import org.jetbrains.snapBuilder.common.set755
import org.json.JSONObject
import java.io.File
import java.nio.file.Files


fun build(dir: File): File {

    val snap = File(dir, "${SNAP_NAME}_${version}_amd64.snap")

    val command = listOf(
            "docker", "run", "-t", "--rm",
            "--workdir=/build",
            "--volume=$assembledYaml:/build/snapcraft.yaml:ro",
            "--volume=$wrappersFolder:/build/wrappers:ro",
            "--volume=${dir.absolutePath}:/build/result",
            DOCKER_IMAGE,
            "snapcraft", "snap",
            "-o", "result/${snap.name}")
    ProcessBuilder(command).run(LOG, verbose = true)

    assembledYaml.delete()
    wrappersFolder.deleteRecursively()

    return snap
}

val response: JSONObject by lazy {
    LOG.debug("Sending 'GET' request to URL : $url")

    url.openStream()
            .bufferedReader()
            .use { JSONObject(it.readText()) }
}

val version = response.getString("tag_name")!!.substring(1)

val zipUrl = response.getJSONArray("assets")
        .getJSONObject(0)
        .getString("browser_download_url")!!

val assembledYaml: File by lazy {
    val content = ClassLoader.getSystemClassLoader()
            .getResource("snapcraft.yaml")
            .readText()
            .replace("%VERSION%", version)
            .replace("%URL%", zipUrl)
    File.createTempFile("snapcraft", null)
            .also { it.writeText(content) }
}

val wrappersFolder: File by lazy {
    val dir = Files.createTempDirectory("wrappers")
            .also { set755(it) }
            .toFile()

    val wrappers = listOf(
            "kotlin.wrapper",
            "kotlin-dce-js.wrapper",
            "kotlinc.wrapper",
            "kotlinc-js.wrapper",
            "kotlinc-jvm.wrapper"
    )

    wrappers.forEach {
        val file = File(dir, it)

        FileUtils.copyURLToFile(
                ClassLoader.getSystemClassLoader().getResource("wrappers/$it"),
                file
        )
        set755(file)
    }

    dir
}
