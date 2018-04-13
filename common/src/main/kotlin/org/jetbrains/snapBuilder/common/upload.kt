package org.jetbrains.snapBuilder.common

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

val DOCKER_IMAGE = System.getProperty("dockerImage") ?: "snapcore/snapcraft:latest"
private val LOG: Logger = LoggerFactory.getLogger("snap-builder-upload")


fun upload(file: File) {
    val command = listOf(
            "docker", "run", "-t", "--rm",
            "--volume=$configFile:/root/.config/snapcraft/snapcraft.cfg",
            "--volume=$file:/build/${file.name}",
            "--workdir=/build",
            DOCKER_IMAGE,
            "snapcraft", "push", file.name
    )

    ProcessBuilder(command).run(LOG, verbose = true)
            .also {
                if (it.isFailed) LOG.error("Upload failed")
            }
}

fun getVersionInStoreByChannel(snapName: String, channel: String = "stable"): String? {
    require(channel in listOf("stable", "candidate", "beta", "edge")) {
        "Wrong channel $channel"
    }

    val command = listOf(
            "docker", "run", "-t", "--rm",
            "--volume=$configFile:/root/.config/snapcraft/snapcraft.cfg",
            DOCKER_IMAGE,
            "snapcraft", "status", snapName
    )

    return ProcessBuilder(command).run(LOG)
            .output
            .firstOrNull { channel in it }
            ?.let {
                if (channel == "stable") {
                    it.split("\\s+".toRegex())[3]
                } else {
                    it.split("\\s+".toRegex())[2]
                }
            }
}
