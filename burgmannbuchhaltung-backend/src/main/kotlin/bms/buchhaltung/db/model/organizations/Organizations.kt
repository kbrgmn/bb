package bms.buchhaltung.db.model.organizations

import bms.buchhaltung.db.model.taxes.AustrianLegalForm
import bms.buchhaltung.db.model.taxes.TaxNrCodesList
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.uuid.UUID
import kotlinx.uuid.exposed.KotlinxUUIDTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.json.json

object Organizations : KotlinxUUIDTable() {
    val name = varchar("name", 128)

    val phone = varchar("phone", 32).nullable()
    val email = varchar("email", 128).nullable()
    val website = varchar("website", 128).nullable()

    val address = varchar("address", 128)
    val zip = varchar("zip", 12)
    val city = varchar("city", 64)
    val country = varchar("country", 2)

    val legalForm = enumerationByName<AustrianLegalForm>("legal_form", 64)

    val bank = varchar("bank", 64).nullable()
    val iban = varchar("iban", 48).nullable()

    val image = varchar("image", 256).nullable()

    val vat = varchar("vat", 32).nullable()
    val taxNr = varchar("tax_nr", 32).nullable()
    val taxNrCodesList = json<TaxNrCodesList>("tax_codes", Json).nullable()
    val eori = varchar("eori", 24).nullable()
    val companyRegisterNr = varchar("company_resgister_nr", 24).nullable()
    val companyRegister = varchar("company_register", 128).nullable()

    val kleinunternehmer = bool("kleinunternehmer").default(false)
}

@Serializable
data class Organization(
    val id: UUID? = null,
    val name: String,

    val phone: String? = null,
    val email: String? = null,
    val website: String? = null,

    val address: String,
    val zip: String,
    val city: String,
    val country: String,

    val legalForm: AustrianLegalForm,

    val bank: String? = null,
    val iban: String? = null,

    val image: String? = null,

    val vat: String? = null,
    val taxNr: String? = null,
    val taxNrCodesList: TaxNrCodesList? = null,
    val eori: String? = null,
    val companyRegisterNr: String? = null,
    val companyRegister: String? = null,

    val kleinunternehmer: Boolean
) {
    constructor(r: ResultRow): this(
        id = r[Organizations.id].value,
        name = r[Organizations.name],
        phone = r[Organizations.phone],
        email = r[Organizations.email],
        website = r[Organizations.website],
        address = r[Organizations.address],
        zip = r[Organizations.zip],
        city = r[Organizations.city],
        country = r[Organizations.country],
        legalForm = r[Organizations.legalForm],
        bank = r[Organizations.bank],
        iban = r[Organizations.iban],
        image = r[Organizations.image],
        vat = r[Organizations.vat],
        taxNr = r[Organizations.taxNr],
        taxNrCodesList = r[Organizations.taxNrCodesList],
        eori = r[Organizations.eori],
        companyRegisterNr = r[Organizations.companyRegisterNr],
        companyRegister = r[Organizations.companyRegister],
        kleinunternehmer = r[Organizations.kleinunternehmer]
    )

}
