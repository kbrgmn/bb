package bms.ocr.parser

import org.joda.money.Money

data class ParsingCalculationCheck(val category: String, val target: Money, val calculated: Money?, val ignored: Boolean) {
    fun isPassing() = calculated == target || (ignored && calculated == null)
    override fun toString() = "[$category: $calculated should be $target]"
}
