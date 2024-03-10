package bms.ocr.parser

import org.joda.money.Money
import org.joda.money.format.MoneyFormatterBuilder
import java.util.*

object MoneyUtils {

    val priceRegex = Regex("^€( )?[\\d|.]+,\\d\\d\$")

    private val moneyFormatter = MoneyFormatterBuilder()
        .appendCurrencySymbolLocalized()
        .appendAmountLocalized()
        .toFormatter(Locale.GERMAN)

    fun Money.toFormattedString() = StringBuilder().also { moneyFormatter.print(it, this) }.toString()

}
