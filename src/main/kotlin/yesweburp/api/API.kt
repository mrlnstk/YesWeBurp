package yesweburp.api

import burp.api.montoya.http.HttpService
import burp.api.montoya.http.message.MimeType
import burp.api.montoya.http.message.requests.HttpRequest
import burp.api.montoya.http.message.responses.HttpResponse
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import yesweburp.VERSION
import yesweburp.burpApi
import java.util.stream.Collectors

object API {
    private const val HOST = "api.yeswehack.com"
    private const val PORT = 443
    private const val BASE_URL = "https://$HOST"

    private val service: HttpService = HttpService.httpService(HOST, PORT, true)

    private val jsonMapper: JsonMapper = JsonMapper.builder()
        .addModule(KotlinModule.Builder().build())
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build()

    /**
     * Read concurrently by [fetchPrograms]'s parallel stream, so it is volatile and only
     * ever replaced wholesale from the (single) authenticating thread.
     */
    @Volatile
    private var token: String? = null

    private fun baseRequest(method: String, path: String): HttpRequest {
        var request = HttpRequest.httpRequestFromUrl(BASE_URL + path)
            .withService(service)
            .withMethod(method)
            .withUpdatedHeader("User-Agent", "YesWeBurp $VERSION")
            .withUpdatedHeader("Accept", "application/json")

        token?.let { request = request.withUpdatedHeader("Authorization", "Bearer $it") }
        return request
    }

    private fun send(request: HttpRequest): String {
        val response: HttpResponse = burpApi.http().sendRequest(request).response()
            ?: throw APIException("No response from $HOST (check Burp's upstream proxy and network settings)")

        val body = response.bodyToString()
        if (response.statusCode().toInt() != 200) {
            if (response.mimeType() == MimeType.JSON) {
                runCatching { jsonMapper.readValue<APIError>(body) }
                    .getOrNull()
                    ?.let { throw APIException(it.message) }
            }
            throw APIException("API Error ${response.statusCode()}")
        }
        return body
    }

    private inline fun <reified T> get(path: String): T = jsonMapper.readValue(send(baseRequest("GET", path)))

    private inline fun <reified TForm, reified TResp> post(path: String, form: TForm): TResp {
        val request = baseRequest("POST", path)
            .withUpdatedHeader("Content-Type", "application/json")
            .withBody(jsonMapper.writeValueAsString(form))
        return jsonMapper.readValue(send(request))
    }

    /** Authenticate anonymously: only public programs will be visible. */
    fun auth() {
        token = null
    }

    fun auth(email: String, password: String) {
        token = null
        val response: LoginResponse = post("/login", Login(email, password))
        if (response.totp_token != null) {
            throw APIException("OTP Required")
        }
        token = response.token ?: throw APIException("Login succeeded but no token was returned")
    }

    fun auth(email: String, password: String, otp: String) {
        token = null
        val otpResponse: LoginResponseTOTP = post("/login", Login(email, password))
        val response: LoginResponse = post("/account/totp", LoginOTP(otpResponse.totp_token, otp))
        token = response.token ?: throw APIException("Login succeeded but no token was returned")
    }

    /**
     * Fetch every program visible to the current token. The listing endpoint is paged and only
     * returns slugs, so each program's details are then fetched in parallel.
     */
    fun fetchPrograms(): List<Program> {
        val slugs = mutableListOf<String>()
        var page = 1
        var pageCount: Int
        do {
            val response: Page<ShortProgram> = get("/programs?page=$page")
            slugs += response.items.map(ShortProgram::slug)
            pageCount = response.pagination.nb_pages
        } while (page++ < pageCount)

        return slugs.parallelStream()
            .map { get<Program>("/programs/$it") }
            .collect(Collectors.toList())
            .sortedBy(Program::title)
    }
}
