package bms.ocr.parser

@Suppress("EnumEntryName", "NonAsciiCharacters")
enum class MonthEnum(val monthId: Int) {
    Unknown(0),
    Jan(1),
    Jän(1),
    Feb(2),
    Mar(3),
    Mär(3),
    Apr(4),
    Mai(5),
    May(5),
    Jun(6),
    Jul(7),
    Aug(8),
    Sep(9),
    Okt(10),
    Oct(10),
    Nov(11),
    Dez(12),
    Dec(12)
}

object MonthEnumUtils {
    val monthEnumNameCache = lazy { MonthEnum.values().map { it.name } }
}

