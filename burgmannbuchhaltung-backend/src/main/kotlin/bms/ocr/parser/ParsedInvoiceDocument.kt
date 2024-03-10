package bms.ocr.parser

import org.joda.money.Money

data class ExtraParsingInformation(
    val sizeInformation: OcrOutputParser.SizeInformation,
    val removedGarbage: List<String>,
    val checkList: ArrayList<ParsingCalculationCheck>,
    val warnings: ArrayList<Pair<String, Int>>
)

inline fun <T> Iterable<T>.sumOfInts(selector: (T) -> Int): Int {
    var sum = 0
    for (element in this) sum += selector(element)
    return sum
}

data class ParsedInvoiceDocument(
    val header: OcrOutputParser.ParsedHeader,

    val lines: List<OcrOutputParser.ParsedPriceLine>,

    val endLine: LinkedHashMap<String, Money>,

    val footer: OcrOutputParser.ParsedFooter,

    val extra: ExtraParsingInformation
) {
    fun getUnknownList() = lines.filter { it.category == null && it.price != null }

    fun parseErrorScore(): Int {
        var score = 0

        score += extra.checkList.filterNot { it.isPassing() }.size * 10
        score += getUnknownList().size * 10
        score += extra.warnings.sumOf { it.second }

        return score
    }
}
