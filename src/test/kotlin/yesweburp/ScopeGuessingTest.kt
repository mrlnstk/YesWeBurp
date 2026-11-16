package yesweburp

import yesweburp.api.Scope
import yesweburp.ui.BurpConfig.guessScope
import yesweburp.ui.BurpConfig.hostToRegex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScopeGuessingTest {

    private fun scope(value: String, type: String = "web-application") =
        Scope(scope = value, scope_type = type, asset_value = "MEDIUM")

    @Test
    fun `wildcard host becomes an optional subdomain regex`() {
        val regex = hostToRegex("*.example.com")
        assertEquals("^(.*\\.)?example\\.com$", regex)
        assertTrue(Regex(regex).matches("example.com"))
        assertTrue(Regex(regex).matches("api.example.com"))
    }

    @Test
    fun `plain host is anchored and dots are escaped`() {
        val regex = hostToRegex("example.com")
        assertEquals("^example\\.com$", regex)
        assertTrue(Regex(regex).matches("example.com"))
        // The dot must not act as a wildcard.
        assertTrue(!Regex(regex).matches("exampleXcom"))
    }

    @Test
    fun `ip-address scopes are used verbatim`() {
        val rule = guessScope(scope("10.0.0.1", type = "ip-address"))!!
        assertEquals("10.0.0.1", rule.host)
        assertEquals("any", rule.protocol)
    }

    @Test
    fun `full url is split into protocol host port and file`() {
        val rule = guessScope(scope("https://api.example.com:8443/v1/"))!!
        assertEquals("https", rule.protocol)
        assertEquals("^api\\.example\\.com$", rule.host)
        assertEquals("8443", rule.port)
        assertEquals("/v1/", rule.file)
    }

    @Test
    fun `bare host defaults to any protocol and empty port and file`() {
        val rule = guessScope(scope("*.example.com"))!!
        assertEquals("any", rule.protocol)
        assertEquals("^(.*\\.)?example\\.com$", rule.host)
        assertEquals("", rule.port)
        assertEquals("", rule.file)
    }
}
