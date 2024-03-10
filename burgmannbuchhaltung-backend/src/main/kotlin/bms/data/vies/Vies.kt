package bms.data.vies

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object Vies {
    private const val BASE_URL = "https://ec.europa.eu/taxation_customs/vies/rest-api/"
    private val http = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        defaultRequest {
            url(BASE_URL)
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
    }

    /** Check VAT nr with country prefix */
    suspend fun checkPrefixedVat(vat: String) = checkVat(vat.take(2), vat.drop(2))
    suspend fun checkPrefixedVat(vat: String, requesterVat: String) = checkVat(vat.take(2), vat.drop(2), requesterVat.take(2), requesterVat.drop(2))

    suspend fun checkVat(
        country: String,
        vat: String,
        requesterCountry: String? = null,
        requesterVat: String? = null
    ): ViesResponse {
        return http.get("ms/$country/vat/$vat") {
            url {
                if (requesterVat != null) {
                    check(requesterCountry != null) { "Requester VAT was provided without requester country" }
                    parameters.append("requesterMemberStateCode", requesterCountry)
                    parameters.append("requesterNumber", requesterVat)
                }
            }
        }.body<ViesResponse>()
    }
}

suspend fun main() {
    println(Vies.checkPrefixedVat("ATU46832307"))
    //println(Vies.checkPrefixedVat("ATU46832307", "ATU46832307"))
}
