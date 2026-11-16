package yesweburp

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import yesweburp.api.Page
import yesweburp.api.Program
import yesweburp.api.ShortProgram
import yesweburp.config.BurpConfig
import yesweburp.config.BurpConfigResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The API and config mappers are configured identically; these tests pin the two behaviours
 * the rest of the code relies on: Kotlin default values and tolerance of unknown fields.
 */
class JsonMappingTest {

    private val mapper: JsonMapper = JsonMapper.builder()
        .addModule(KotlinModule.Builder().build())
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build()

    @Test
    fun `program parses with unknown fields and absent optionals`() {
        val json = """
            {
              "title": "Acme",
              "slug": "acme",
              "rules_html": "<p>rules</p>",
              "scopes": [{"scope": "*.acme.com", "scope_type": "web-application", "asset_value": "HIGH"}],
              "out_of_scope": ["blog.acme.com"],
              "public": true,
              "business_unit": {"currency": "EUR", "unmapped": 1},
              "vpn_active": false,
              "a_field_the_api_added_later": "ignored"
            }
        """.trimIndent()

        val program: Program = mapper.readValue(json)

        assertEquals("Acme", program.title)
        assertEquals(1, program.scopes.size)
        assertEquals("EUR", program.business_unit.currency)
        // Defaults must apply rather than blowing up or yielding nulls in non-null fields.
        assertTrue(program.qualifying_vulnerability.isEmpty())
        assertNull(program.user_agent)
        assertNull(program.reward_grid_default)
    }

    @Test
    fun `paged listing parses generically`() {
        val json = """
            {
              "items": [{"title": "Acme", "slug": "acme"}, {"title": "Globex", "slug": "globex"}],
              "pagination": {"nb_pages": 3, "page": 1, "result_per_page": 25}
            }
        """.trimIndent()

        val page: Page<ShortProgram> = mapper.readValue(json)

        assertEquals(3, page.pagination.nb_pages)
        assertEquals(listOf("acme", "globex"), page.items.map(ShortProgram::slug))
    }

    @Test
    fun `burp project options round-trip through the config mapper`() {
        val exported = """
            {"target": {"scope": {"advanced_mode": false, "include": [
              {"enabled": true, "protocol": "any", "host": "^acme\\.com$", "port": "", "file": ""}
            ], "exclude": []}}}
        """.trimIndent()

        val config: BurpConfigResponse = BurpConfig.mapper.readValue(exported)
        assertEquals(1, config.target!!.scope.include.size)

        config.target!!.scope.advanced_mode = true
        val reserialized = BurpConfig.mapper.writeValueAsString(config)

        // Burp's importer keys off these names, so they must survive the round trip verbatim.
        assertTrue(reserialized.contains("\"advanced_mode\":true"))
        assertTrue(reserialized.contains("^acme\\\\.com$"))
    }
}
