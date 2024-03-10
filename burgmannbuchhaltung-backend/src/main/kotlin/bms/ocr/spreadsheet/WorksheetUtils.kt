package bms.ocr.spreadsheet

import org.dhatim.fastexcel.StyleSetter
import org.dhatim.fastexcel.Worksheet
import java.time.LocalDate
import java.time.ZonedDateTime

object WorksheetUtils {
    internal fun Worksheet.valueStyle(r: Int, c: Int, value: String) = run { value(r, c, value); style(r, c) }
    internal fun Worksheet.valueStyle(r: Int, c: Int, value: Number) = run { value(r, c, value); style(r, c) }
    internal fun Worksheet.valueStyle(r: Int, c: Int, value: ZonedDateTime) = run { value(r, c, value); style(r, c) }
    internal fun Worksheet.valueStyle(r: Int, c: Int, value: LocalDate) = run { value(r, c, value); style(r, c) }
    internal fun Worksheet.valueStyle(r: Int, c: Int, value: Boolean) = run { value(r, c, value); style(r, c) }
    fun Worksheet.valueStyle(r: Int, c: Int, value: Any?): StyleSetter {
        when (value) {
            is String -> valueStyle(r, c, value)
            is Number -> valueStyle(r, c, value)
            is Boolean -> valueStyle(r, c, value)
            is ZonedDateTime -> valueStyle(r, c, value)
            is LocalDate -> valueStyle(r, c, value)
            null -> valueStyle(r, c, "null")
            else -> TODO("valueStyle has not yet been implemented for: ${value::class.simpleName}")
        }
        return style(r, c)
    }
}
