package bms.data

import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.hours


@Serializable
data class TaxOfficeListItem(
    /*@SerialName("DiSTelvw")
    val diSTelvw: String = "",
    */
    @SerialName("DisBankbez")
    val bankName: String = "", // BAWAG P.S.K.
    @SerialName("DisBic")
    val bankBIC: String = "", // BUNDATWW
    @SerialName("DisBld")
    val federalState: String = "", // W
    /*
    @SerialName("DisBlz")
    val disBlz: String = "",
    */
    /*
    @SerialName("DisDVR")
    val disDVR: String = "",
    */
    @SerialName("DisFax")
    val fax: String = "", // +43 50 233 5914001
    @SerialName("DisFotoUrl")
    val image: String? = null, // https://service.bmf.gv.at/service/anwend/behoerden/img/resize260adr/1030_marxergasse_4.jpg
    /*
    @SerialName("DisGiro")
    val disGiro: String = "",
    */
    @SerialName("DisIban")
    val bankIban: String = "", // AT87 0100 0000 0550 4037
    @SerialName("DisId")
    val idShort: String = "", // FA03
    @SerialName("DisKz")
    val idLong: String = "", // FA03.FA03
    @SerialName("DisLatitude")
    val lat: String = "", // 48.20793
    @SerialName("DisLongitude")
    val lon: String = "", // 16.385049
    @SerialName("DisNameLang")
    val name: String = "", // Dienststelle Wien 3/6/7/11/15 Schwechat Gerasdorf
    @SerialName("DisOeffnung")
    val opening: String = "", // Mo-Fr : Der Selbstbedienungsbereich ist Mo. und Di. von 7.30 bis 15.30, am Mi. und Fr. von 7.30 bis 12.00 und am Do. von 7.30 bis 17.00 zugänglich. Für einen persönlichen Kontakt ist eine Terminvereinbarung unbedingt erforderlich.
    @SerialName("DisOrt")
    val city: String = "", // Wien
    @SerialName("DisPlz")
    val zip: String = "", // 1030
    @SerialName("DisStrasse")
    val street: String = "", // Marxergasse 4
    @SerialName("DisTel")
    val phone: String = "", // +43 50 233 233
    @SerialName("DisTyp")
    val type: String = "" // FA
) {
    override fun toString() =
        "[$type: $idShort / $idLong: $name, $federalState $zip/$city, $street; Tel: $phone, Fax: $fax; Pay to: $bankName $bankIban $bankBIC; Opening: $opening; At: $lat / $lon; Image: $image]"
}


object TaxOffices {

    private val jsonHttp = HttpClient() {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(ContentEncoding) {
            deflate(1.0F)
            gzip(0.9F)
        }
    }

    private val basicHttp = HttpClient() {
        install(ContentEncoding) {
            deflate(1.0F)
            gzip(0.9F)
        }
    }

    var lastSuccessTaxOffice: List<TaxOfficeListItem>? = null

    private val taxOfficeImageCache = Cache.Builder<String, Boolean>()
        .expireAfterWrite(6.hours)
        .build()

    suspend fun checkImageCorrect(imgUrl: String): Boolean {
        return taxOfficeImageCache.get(imgUrl) {
            println("Checking image: $imgUrl...")
            basicHttp.head(imgUrl).also {
                println("RESPONSE: $it")
                println("CONTENT LENGTH: ${it.contentLength()}")
            }.contentLength()?.let { it > 0 } ?: false
        }
    }

    suspend fun requestTaxOffices() = runCatching {
        println("--- Requesting tax offices...")
        jsonHttp.get("https://service.bmf.gv.at/Finanzamtsliste.json").body<List<TaxOfficeListItem>>().map {
            println("Checking ${it.name}")
            it.copy(image = if (it.image?.isNotBlank() == true && checkImageCorrect(it.image)) it.image else null)
        }
    }.onSuccess { lastSuccessTaxOffice = it }


    private val taxOfficeListCache = Cache.Builder<Boolean, List<TaxOfficeListItem>>()
        .expireAfterWrite(6.hours)
        .build()

    const val cacheKey = true

    suspend fun getTaxOffices(): List<TaxOfficeListItem> = taxOfficeListCache.get(cacheKey) {
        requestTaxOffices().getOrElse {
            println("ERROR: Was unable to get tax offices. Falling back...")
            lastSuccessTaxOffice ?: throw IllegalStateException("Could at no point request tax offices.")
        }
    }

    suspend fun suggestTaxOffice(zip: String, city: String? = null): TaxOfficeListItem? {
        check(zip.length == 4) { "Invalid zip length: $zip" }

        val taxOffices = getTaxOffices()

        if (zip[0] == '1' && zip[1] in arrayOf('0', '1', '2')) {
            val firstDigit = zip[1]
            val secondDigit = zip[2]

            val viennaDistrict = "${if (firstDigit != '0') firstDigit else ""}$secondDigit"

            return taxOffices.filter { it.federalState == "W" }
                .associateWith { it.name.substringAfter("Wien ").split("/", " ") }
                .entries.firstOrNull { it.value.contains(viennaDistrict) }?.key
        }

        return taxOffices.singleOrNull { it.zip == zip }
            ?: city?.let {
                taxOffices.singleOrNull {
                    it.name.substringAfter(" Dienststelle ").split(" ").contains(city)
                }
            }
    }
}

suspend fun main() {
    TaxOffices.getTaxOffices().forEach {
        println(it)
    }

    val s = "1220"
    val c = "Wien"
    println("Tax office for: $s")
    println(TaxOffices.suggestTaxOffice(s, c))
}
