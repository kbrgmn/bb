
package bms.buchhaltung.db.model.taxes

import kotlinx.serialization.Serializable

@Suppress("EnumEntryName", "NonAsciiCharacters")
@Serializable
enum class AustrianLegalForm(vararg shorts: String) {
    Einzelunternehmer,
    Eingetragener_Unternehmer("e.U."),
    Kommanditgesellschaft("KG"),
    Offene_Gesellschaft("OG"),
    Gesellschaft_mit_beschränkter_Haftung("GmbH", "GesmbH", "mbH"),
    Aktiengesellschaft("AG"),
}
