package yesweburp

import burp.api.montoya.MontoyaApi
import java.awt.Desktop
import java.io.IOException
import java.net.URI

const val VERSION = "3.0.0"

/** Set once by [YesWeBurp.initialize], before any UI is created. */
lateinit var burpApi: MontoyaApi
    internal set

fun openInBrowser(url: String) {
    if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
        burpApi.logging().logToError("Cannot open $url: no browser available on this platform")
        return
    }
    try {
        Desktop.getDesktop().browse(URI(url))
    } catch (e: IOException) {
        burpApi.logging().logToError("Failed to open $url", e)
    }
}
