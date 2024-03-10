package bms.ocr.parser

import org.joda.money.Money

object StringUtils {

    fun String.split2(separator: String): Pair<String, String> = Pair(substringBefore(separator), substringAfter(separator))

    fun String.isCorrectPriceNumber(): Boolean {
        val dotSections = substringAfter("€")
            .substringBefore(",")
            .split(".")

        return count { it == ',' } == 1
                && dotSections.first().length <= 3
                && dotSections.drop(1).all { it.length == 3 }
                && dotSections.all { it.toIntOrNull() in 0..999 }
                && split2(",").second.toIntOrNull() in 0..99
    }

    fun parseMoneyOrNull(rawMoney: String) = runCatching { Money.parse("EUR $rawMoney") }.getOrNull()

    fun String.replaceLast(oldChar: Char, newChar: Char) = this.reversed().replaceFirst(oldChar, newChar).reversed()

    private val numbersList = listOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    fun String.substringAtFirstNumber() = this.substring(this.indexOfFirst { it in numbersList })

    fun String.parseToMoney(): Money {
        assert(this.isCorrectPriceNumber())

        val rawMoney = this
            .substringAfter("€", this.substringAtFirstNumber())
            .replace(".", "")
            .replaceLast(',', '.')
            .replace(",", "")
            .filterNot { it in 'A'..'Z' || it in 'a'..'z' }
            .trim()

        return parseMoneyOrNull(rawMoney) ?: throw IllegalArgumentException("Could not parse money: $rawMoney (from $this)")
    }
}
