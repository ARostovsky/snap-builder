package org.jetbrains.snapBuilder.common

import java.io.File

val configFile: File by lazy {
    val macaroon = System.getProperty("macaroon")
            ?: throw IllegalArgumentException("macaroon system argument isn't specified")
    val unbound_discharge = System.getProperty("unbound_discharge")
            ?: throw IllegalArgumentException("unbound_discharge system argument isn't specified")
    val email = System.getProperty("email") ?: throw IllegalArgumentException("email system argument isn't specified")
    val account_id = System.getProperty("account_id")
            ?: throw IllegalArgumentException("account_id system argument isn't specified")


    val configFile = File.createTempFile("snapcraft", null)
    configFile.writeText("""[login.ubuntu.com]
macaroon = $macaroon
unbound_discharge = $unbound_discharge
email = $email
account_id = $account_id

"""
    )
    configFile
}