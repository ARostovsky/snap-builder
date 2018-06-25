package org.jetbrains.snapBuilder.kotlin

import org.jetbrains.snapBuilder.common.getVersionInStoreByChannel
import org.jetbrains.snapBuilder.common.upload
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL


const val SNAP_NAME = "kotlin"
val DOCKER_IMAGE = System.getProperty("dockerImage") ?: "snapcore/snapcraft:latest"
val LOG: Logger = LoggerFactory.getLogger("snap-builder-$SNAP_NAME")
val url = URL("https://api.github.com/repos/JetBrains/kotlin/releases/latest")

fun main(args: Array<String>) {
    require(args.size == 1) {
        "Path argument where snap package should be stored is missing"
    }
    val dir = File(args.first())
            .also { if (!it.exists()) it.mkdir() }

    val snap = build(dir)

    System.getProperty("upload")
            .let { it?.toBoolean() ?: false }
            .also {
                if (!it) {
                    // Only build, no upload
                    LOG.info("Upload won't be done")
                    return
                }
            }

    if (version == getVersionInStoreByChannel(SNAP_NAME)) {
        LOG.info("Build with such version $version is already in Snap Store")
        return
    } else {
        upload(snap)
    }
}
