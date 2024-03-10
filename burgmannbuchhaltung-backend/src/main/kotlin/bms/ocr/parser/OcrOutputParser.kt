package bms.ocr.parser

import bms.ocr.parser.CoordinateUtils.getCoordinateBox
import bms.ocr.parser.MoneyUtils.toFormattedString
import bms.ocr.parser.StringUtils.isCorrectPriceNumber
import bms.ocr.parser.StringUtils.parseToMoney
import bms.ocr.parser.StringUtils.split2
import kotlinx.datetime.LocalDate
import org.joda.money.CurrencyUnit
import org.joda.money.Money
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class OcrOutputParser {

    private lateinit var ocrPage: Element
    private lateinit var ocrCArea: Element

    lateinit var parsedSizeInformation: SizeInformation

    data class SizeInformation(val paperPageHeight: String, val areaSizes: List<String>, val lineCount: Int)

    private fun parseXmlToLines(xml: String): MutableList<Element> {
        val doc = Jsoup.parse(xml)

        ocrPage = doc.body().getElementsByClass("ocr_page").first()!!
        val paperPageSize = ocrPage.getCoordinateBox().getSizeString()

        println("Paper page size is: $paperPageSize")

        ocrCArea = ocrPage.getElementsByClass("ocr_carea").first()!!
        val areas = ocrCArea.getElementsByClass("ocr_par")

        println("Parsing ${areas.size} areas...")

        val lines = areas.flatMap {
            println("Area with size: ${it.getCoordinateBox().getSizeString()}")
            it.getElementsByClass("ocr_line")
        }.toMutableList()

        println("Parsing ${lines.size} lines...")

        parsedSizeInformation = SizeInformation(paperPageSize, areas.map { it.getCoordinateBox().getSizeString() }, lines.size)

        return lines
    }

    data class ParsedFooter(val pageNum: Int?)

    private val warnings = ArrayList<Pair<String, Int>>()

    private fun parseFooter(lines: MutableList<Element>): ParsedFooter {
        val pageNumLine = lines.removeLast()
        val pageNum = pageNumLine.children().eachText()
        if (pageNum[0] != "Seite") println("WARNING: Invalid footer: $pageNum")

        val possiblePageNum = pageNum[1].toIntOrNull()

        if (pageNum.size > 2) println("WARNING: Got gibberish at the end: ${pageNum.drop(2)}")
        if (possiblePageNum !is Int) println("WARNING: Could not find page number: $pageNum")

        return ParsedFooter(possiblePageNum)
    }

    data class HeaderDate(val day: Int, val month: String, val year: Int) {
        override fun toString() = "$day.$month.$year"

        fun getInts(): Triple<Int, Int, Int> {
            val yr = if (year < 2000) 2000 + year else year
            val month = MonthEnum.valueOf(month).monthId
            val day = day

            return Triple(yr, month, day)
        }

        fun getStrings(): Triple<Int, String, String> {
            val (year, month, day) = getInts()
            val monthString = month.toString().padStart(2, '0')
            val dayString = day.toString().padStart(2, '0')

            return Triple(year, monthString, dayString)
        }

        fun toGermanStringDate(): String {
            val (year, month, day) = getStrings()
            return "${day}.${month}.$year"
        }

        fun getLocalDate(): LocalDate {
            val (year, month, day) = getStrings()
            return LocalDate.parse("$year-$month-$day")
        }
    }
    data class TableHeaderXCoordinates(val map: Map<IntRange, String>)
    data class ParsedHeader(val date: HeaderDate, val coordinates: TableHeaderXCoordinates)

    private val removedGarbage = ArrayList<String>()
    private fun dropNonPriceGarbage(lines: MutableList<Element>) {
        while ("€" !in lines.first().text()) {
            val garbage = lines.removeFirst().text()
            println("Removed garbage: $garbage")
            removedGarbage.add(garbage)
        }
    }

    private val categoryCoordinateMap = HashMap<IntRange, String>()

    private fun parseHeader(lines: MutableList<Element>): ParsedHeader {
        val firstLine = lines.removeFirst().text()

        var day: Int? = null
        val day2: Int?
        if (firstLine.toIntOrNull() is Int) {
            day = firstLine.toInt()
        }

        val monthLine = lines.removeFirst()
        require("Monat" == monthLine.child(0).text()) { "Did not find Monat at ${monthLine.child(0)}" }

        val (month, yearString) = monthLine.child(1).text().split2(".")
        if (month !in MonthEnumUtils.monthEnumNameCache.value) println("WARNING: Invalid month: $month")

        val year = yearString.toIntOrNull() ?: -9999

        val titleHeaderLine = lines.removeFirst()

        if (titleHeaderLine.childrenSize() != 6) println("WARNING: Title header line size != 6")

        val shouldBeDayField = titleHeaderLine.firstElementChild()?.text() ?: ""
        if (shouldBeDayField !in arrayOf("Tag", "Tag:")) println("WARNING: Day2 field not found: $shouldBeDayField")

        day2 = titleHeaderLine.child(1).text().toIntOrNull()
        if (day2 != null) {
            if (day == null) day = day2
            if (day != day2) println("WARNING: Day1 != Day2: $day != $day2")
        }

        if (day == null || day < 0) {
            println("WARNING: Day is null")
            day = -9999
        }
        if (year < 0) println("WARNING: YEAR is null")

        val processedMap: Map<IntRange, String> = titleHeaderLine.children().drop(2).associate { word ->
            word.getCoordinateBox().getHorizontalRange() to word.text()
        }

        categoryCoordinateMap.putAll(processedMap)

        return ParsedHeader(HeaderDate(day, month, year), TableHeaderXCoordinates(processedMap))
    }

    private fun Int.correctPixelsRightward(): Int = (this + (this * 0.025)).toInt()
    private fun findCategory(priceStartPixels: Int): String? {
        return categoryCoordinateMap[categoryCoordinateMap.filterKeys { priceStartPixels in it }.keys.firstOrNull()
            ?: return null]
    }

    private fun Element.getStartCoordinates() = getCoordinateBox().startLeft.correctPixelsRightward()
    private fun Element.findCategory() = findCategory(getStartCoordinates())

    data class ParsedPriceLine(val desc: String, val price: Money?, val category: String?, val priceCoordinates: Int?)

    fun parsePriceLine(words: List<Element>): ParsedPriceLine {
        if (words.find { it.text().contains("€") } == null)
            return ParsedPriceLine(words.joinToString(" ") { it.text() }, null, null, null)

        //assertTrue { words.find { it.text().contains("€") } != null }

        val textLine = words.joinToString(" ") { it.text() }
        val splitted = textLine.split2("€")

        val description = splitted.first.trim().removeSuffix(":")
        val priceValue = splitted.second.trim()

        val priceElement = words.find { it.text().startsWith("€") }!!

        val category = priceElement.findCategory()

        println("$description: €$priceValue [$category]")

        val money = priceValue.parseToMoney()

        return ParsedPriceLine(description, money, category, priceElement.getStartCoordinates())

    }

    fun parsePriceLine(line: Element) = parsePriceLine(line.children())

    fun safeParsePriceLine(line: Element): ParsedPriceLine {
        val lineWords = line.children().toList()
        val filteredWords = mutableListOf(lineWords.first())

        filteredWords.addAll(
            lineWords.filter { it.text().startsWith("€") || it.text().isCorrectPriceNumber() }
        )

        removedGarbage.addAll(lineWords.minus(filteredWords.toSet()).map { it.text() })

        return parsePriceLine(filteredWords)
    }

    val targetCategoryMoney = LinkedHashMap<String, Money>()

    private fun parseEndLine(name: String, version: Int, line: Element) {
        require(4 == line.children().eachText().joinToString(" ").replace("€ ", "€").trim().count { it == ',' }) { "Could not parse endline: $line" }

        line.children().toList().filter { ',' in it.text() }.forEach {
            if (it.text().isCorrectPriceNumber()) {
                val category = it.findCategory()
                val money = it.text().parseToMoney()

                if (category != null) {
                    if (targetCategoryMoney.containsKey(category))
                        println("$name, $version: WARNING: Overwriting ${targetCategoryMoney[category]} with $money")

                    targetCategoryMoney[category] = money
                } else {
                    val warn = "$name, $version: Could not find category for: ${it.text()} @ ${it.getCoordinateBox()}"
                    warnings += Pair(warn, 25)
                    println(warn)
                }
            }
        }
    }

    fun calculateTotals(name: String, version: Int, categorizedLines: Map<String?, List<ParsedPriceLine>>, ignoredCategories: List<String>): ArrayList<ParsingCalculationCheck> {
        val calculatedTotals = categorizedLines
            .mapValues {
                it.value.map { it.price ?: Money.zero(CurrencyUnit.EUR) }.reduce { a, b -> a + b }
            }.toMutableMap()

        if (calculatedTotals.containsKey(null) && calculatedTotals[null] != null) {
            if (calculatedTotals[null] == Money.zero(CurrencyUnit.EUR)) {
                calculatedTotals.remove(null)
            } else println("ERROR: There are ${calculatedTotals[null]} unmapped!")
        }

        val checkList = ArrayList<ParsingCalculationCheck>()

        println("$name, $version: --- END LINE CHECK ---")
        targetCategoryMoney.forEach { (category, targetMoney) ->
            val calculatedMoney = calculatedTotals[category]
            val ignored = category in ignoredCategories

            val check = ParsingCalculationCheck(category, targetMoney, calculatedMoney, ignored)
            checkList.add(check)

            println(
                if (check.isPassing()) "$name, $version: OK: Category \"$category\", target: ${targetMoney.toFormattedString()}${if (ignored) " (ignored; calculated: $calculatedMoney)" else ""}"
                else "$name, $version: ERROR: Category \"$category\", target: ${targetMoney.toFormattedString()}, BUT actual calculated: $calculatedMoney"
            )
        }

        println()
        println("$name, $version: Checks passed:  ${checkList.count { it.isPassing() }}/${checkList.size}")
        println("$name, $version: Checks failed:  ${checkList.count { !it.isPassing() }}/${checkList.size}")

        return checkList
    }

    fun parse(name: String, version: Int, xml: String): ParsedInvoiceDocument {
        println("$name, $version: --- PARSING ---")
        val lines = parseXmlToLines(xml)

        val pageSize = ocrPage.getCoordinateBox()
        require(pageSize.startLeft == 0) { "pageSize.startLeft (${pageSize.startLeft}) != 0" }
        require(pageSize.startLeft == 0) { "pageSize.startTop (${pageSize.startTop}) != 0" }

        println()
        println("$name, $version: >> Findings:")
        // Footer
        val footer = parseFooter(lines)
        println("$name, $version: Parsed page num: ${footer.pageNum}")

        // Header
        val header = parseHeader(lines)
        println("$name, $version: Date: " + header.date)

        categoryCoordinateMap.entries.forEach {
            println("$name, $version: Found category: \"${it.value}\" at range ${it.key}")
        }

        dropNonPriceGarbage(lines)

        val priceLines = ArrayList<ParsedPriceLine>()

        // Safe parse for Vortag & Losung -> Split by " " and take first for desc and last for Price
        val vortag = safeParsePriceLine(lines.removeFirst()) // Vortag
        priceLines.add(vortag)

        val losung = safeParsePriceLine(lines.removeFirst()) // Losung
        priceLines.add(losung)

        dropNonPriceGarbage(lines)

        lines.takeWhile { it.text().count { it == '€' } != categoryCoordinateMap.size }.forEach {
            val priceLine = parsePriceLine(it)
            priceLines.add(priceLine)
            lines.remove(it)
        }
        // Parse normal until multiple € in line

        // Parse End line
        if (lines.size != 1) println("$name, $version: WARNING: (${lines.size} instead of 1 line remaining at end line?")

        parseEndLine(name, version, lines.removeFirst())


        // Vars
        val categorizedLines = priceLines.groupBy { it.category }


        // Calculate if correct
        val checkList = calculateTotals(name, version, categorizedLines, ignoredCategories = listOf("Saldo"))

        return ParsedInvoiceDocument(
            header = header,
            lines = priceLines,
            endLine = targetCategoryMoney,
            footer = footer,
            extra = ExtraParsingInformation(parsedSizeInformation, removedGarbage, checkList, warnings)
        )
    }
}

