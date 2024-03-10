package bms.buchhaltung.services.ocr.imports

import bms.buchhaltung.utils.json.SerializableBigDecimal
import kotlinx.serialization.Serializable


@Serializable
data class ReceiptImportParsedFile(
    val categories: List<Category>,
    val lines: List<Line>
) {
    @Serializable
    data class Category(
        val name: String,
        val expected: SerializableBigDecimal,
        val actual: SerializableBigDecimal?,
        val ignored: Boolean
    )

    @Serializable
    data class Line(
        val name: String,
        val category: String? = null,
        val amount: SerializableBigDecimal? = null,
    )
}
