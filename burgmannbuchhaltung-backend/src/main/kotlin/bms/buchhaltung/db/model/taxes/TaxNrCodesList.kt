package bms.buchhaltung.db.model.taxes

import kotlinx.serialization.Serializable

@Serializable
data class TaxNrCodesList(val taxCodes: List<String>)

enum class TaxTypes(taxCode: String, originalName: String) {
    INCOME_TAX("E", "Einkommensteuer"),
    VAT("U", "Umsatzsteuer")
}
