package bms.ocr.spreadsheet

import bms.ocr.parser.MonthEnum
import bms.ocr.parser.OcrOutputParser
import bms.ocr.parser.ParsedInvoiceDocument
import bms.ocr.parser.ParsingCalculationCheck
import bms.ocr.spreadsheet.WorksheetUtils.valueStyle
import org.dhatim.fastexcel.BorderStyle
import org.dhatim.fastexcel.Workbook
import org.dhatim.fastexcel.Worksheet
import org.joda.money.CurrencyUnit
import org.joda.money.Money
import java.nio.file.Path
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.io.path.absolutePathString
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.outputStream

class SheetGenerator {

    private val euroFormat =/*by lazy {
        if (System.getProperty("os.name").startsWith("Windows"))
            "€ #.##0;-€ #.##0" // MS Office Excel
        else*/ "[\$€-C07] #,##0.--;[RED]-[\$€-C07] #,##0.--" // LibreOffice Calc
    //}

    private fun Worksheet.writeTitleHead(filePath: Path): Int {
        valueStyle(0, 0, "Burgmann-OCR")
            .fontSize(13).set()

        valueStyle(2, 0, "File:").bold().set()
        value(2, 1, filePath.name)

        valueStyle(3, 0, "Generated:").bold().set()
        valueStyle(3, 1, ZonedDateTime.now()).format("dd.MM.yyyy HH:mm:ss").set()

        return 4
    }

    private fun Worksheet.writePageInfo(pageNum: Int?, topOffset: Int): Int {
        valueStyle(topOffset, 0, "Page").bold().set()
        valueStyle(topOffset + 1, 0, pageNum ?: "?").horizontalAlignment("left").set()
        range(topOffset, 0, topOffset + 1, 0).style().borderStyle(BorderStyle.MEDIUM).set()

        return topOffset + 2
    }

    private fun Worksheet.writeFallbackDate(date: OcrOutputParser.HeaderDate, topOffset: Int): Int {
        date.run {
            range(topOffset, 0, topOffset, 2).merge()
            valueStyle(topOffset, 0, "Date").bold().horizontalAlignment("center").set()

            value(topOffset + 1, 0, "Year")
            value(topOffset + 2, 0, year)
            value(topOffset + 1, 1, "Month")
            value(topOffset + 2, 1, month)
            value(topOffset + 1, 2, "Day")
            value(topOffset + 2, 2, day)
            range(topOffset, 0, topOffset + 2, 2).style().borderStyle(BorderStyle.MEDIUM).set()
        }

        return topOffset + 3
    }

    private fun Worksheet.writeParsedDate(parsedDate: OcrOutputParser.HeaderDate, topOffset: Int): Int {
        val date = runCatching {
            parsedDate.let {
                val yr = if (it.year < 2000) 2000 + it.year else it.year
                val month = MonthEnum.valueOf(it.month).monthId
                val day = it.day

                LocalDate.of(yr, month, day)
            }
        }.getOrNull()

        valueStyle(topOffset, 0, "Parsed date:").bold().set()
        if (date is LocalDate) valueStyle(topOffset + 1, 0, date).format("dd.MM.yyyy").set()
        else value(topOffset + 1, 0, "Invalid date!")
        range(topOffset, 0, topOffset + 1, 0).style().borderStyle(BorderStyle.MEDIUM).set()

        return topOffset + 2
    }

    fun Worksheet.writeDataTable(
        lines: List<OcrOutputParser.ParsedPriceLine>,
        endLine: LinkedHashMap<String, Money>,
        coordinates: OcrOutputParser.TableHeaderXCoordinates
    ) {
        val startLeft = 7
        val startTop = 3

        valueStyle(startTop - 1, startLeft - 1, "⟱ Coordinates ⭆").fontSize(12).bold().set()
        valueStyle(startTop - 1, startLeft, "Category coordinates ⭆").bold().set()
        valueStyle(startTop, startLeft - 1, "⟱ Amount coords").bold().set()
        valueStyle(startTop, startLeft, "⟱ Description / Category ⭆").fontSize(12).bold().set()

        val categories = endLine.keys

        categories.forEachIndexed { index, category ->
            val categoryCoordinates = coordinates.map.filterValues { it == category }.keys.first()
            value(startTop - 1, startLeft + index + 1, "${categoryCoordinates.first} - ${categoryCoordinates.last}")

            valueStyle(startTop, startLeft + index + 1, category).bold().underlined().set()
        }

        val unknownList = ArrayList<OcrOutputParser.ParsedPriceLine>()
        lines.forEachIndexed { index, line ->
            val lineRow = startTop + index + 1

            // Set description
            value(lineRow, startLeft, line.desc)

            if (line.category != null) {
                val categoryOffset = categories.indexOf(line.category) + 1

                valueStyle(lineRow, startLeft + categoryOffset, line.price!!.amount).format(euroFormat).set()
                valueStyle(lineRow, startLeft - 1, line.priceCoordinates!!).horizontalAlignment("left").set()
            } else if (line.price != null) {
                style(lineRow, startLeft).fontColor("FF00000").set()

                valueStyle(3, startLeft + categories.size + 1, "UNKNOWN CATEGORY").bold().underlined().fontColor("AA0000").set()
                valueStyle(lineRow, startLeft + categories.size + 1, line.price.amount).format(euroFormat).set()
                unknownList.add(line)
            }
        }

        val priceLinesOffset = lines.size

        range(startTop, startLeft, startTop + 2 /*priceLinesOffset - 1 - 5*/, startLeft + categories.size)
            .createTable("⟱ Description / Category ⭆", *categories.toTypedArray())
            .setDisplayName("Entry_lines")
            .setName("PriceLines")
        //.setTotalsRowShown(true)

        // dark header
        range(startTop, startLeft, startTop, startLeft + categories.size).style()
            .fillColor("9A9A9A").set()

        // basic background
        range(startTop + 1, startLeft, startTop + priceLinesOffset, startLeft + categories.size).style()
            .fillColor("CCCCCC").set()

        // alternative background
        range(startTop + 1, startLeft, startTop + priceLinesOffset, startLeft + categories.size).style()
            .shadeAlternateRows("EEEEEE").set()

        // calc line
        valueStyle(startTop + priceLinesOffset + 2, startLeft, "Calculated:").bold().underlined().set()
        range(startTop + priceLinesOffset + 2, startLeft, startTop + priceLinesOffset + 2, startLeft + categories.size).style()
            .fillColor("9A9A9A").set()

        endLine.keys.forEachIndexed { index, _ ->
            formula(startTop + priceLinesOffset + 2, startLeft + 1 + index,
                    "SUM(" + range(startTop + 1, startLeft + 1 + index, startTop + priceLinesOffset, startLeft + 1 + index) + ")"
            )
            style(startTop + priceLinesOffset + 2, startLeft + 1 + index).format(euroFormat).bold().set()
        }

        // Unmatched 1
        valueStyle(startTop + priceLinesOffset + 2, startLeft + endLine.keys.size + 1,
            "${unknownList.map { it.price ?: Money.zero(CurrencyUnit.EUR) }.reduceOrNull { l1, l2 -> l1 + l2  } ?: "No"} EUR amount unmatched")
            .fontColor(if (unknownList.isEmpty()) "00AA00" else "FF0000").set()

        // endline
        valueStyle(startTop + priceLinesOffset + 3, startLeft, "Endline:").bold().underlined().set()
        range(startTop + priceLinesOffset + 3, startLeft, startTop + priceLinesOffset + 3, startLeft + categories.size).style()
            .fillColor("9A9A9A").set()

        endLine.keys.forEachIndexed { index, category ->
            val money = endLine[category]!!
            valueStyle(startTop + priceLinesOffset + 3, startLeft + 1 + index, money.amount)
                .format(euroFormat).underlined().bold().set()
        }

        // Unmatched 2
        valueStyle(startTop + priceLinesOffset + 3, startLeft + endLine.keys.size + 1,
            "${unknownList.size} entries unmatched").fontColor(if (unknownList.isEmpty()) "00AA00" else "FF0000").set()


        // border
        range(startTop, startLeft, startTop + priceLinesOffset + 3, startLeft + categories.size).style()
            .borderStyle(BorderStyle.THIN).set()
    }

    private fun Worksheet.writeCategoryListing(checkList: ArrayList<ParsingCalculationCheck>, topOffset: Int): Int {
        valueStyle(topOffset, 0, "Category").bold().set()
        valueStyle(topOffset, 1, "Target").bold().set()
        valueStyle(topOffset, 2, "Calculated").bold().set()
        valueStyle(topOffset, 3, "Passed").bold().set()
        valueStyle(topOffset, 4, "Ignored").bold().set()

        checkList.forEachIndexed { index, parsingCalculationCheck ->
            valueStyle(topOffset + index + 1, 0, parsingCalculationCheck.category).underlined().set()
            valueStyle(topOffset + index + 1, 1, parsingCalculationCheck.target.amount).format(euroFormat).set()
            valueStyle(topOffset + index + 1, 2, parsingCalculationCheck.calculated?.amount ?: "Empty").format(euroFormat).set()
            val isPass = parsingCalculationCheck.isPassing()
            valueStyle(topOffset + index + 1, 3, if (isPass) "OK" else "FAIL")
                .fontColor(if (isPass) "009900" else "FF0000").set()
            value(topOffset + index + 1, 4, if (parsingCalculationCheck.ignored) "ignored" else "calculated")
        }

        val checkCount = checkList.size

        range(topOffset, 0, topOffset + checkCount, 4)
            .createTable("Category", "Target", "Calculated", "Passed", "Ignored")

        range(topOffset, 0, topOffset + checkCount, 4)
            .style().fillColor("BBBBBB").borderStyle(BorderStyle.THIN).set()


        valueStyle(topOffset + checkCount + 2, 0, "Checks passed").bold().set()
        value(topOffset + checkCount + 2, 1, "${checkList.count { it.isPassing() }}/$checkCount")

        valueStyle(topOffset + checkCount + 3, 0, "Checks failed").bold().set()
        value(topOffset + checkCount + 3, 1, "${checkList.count { !it.isPassing() }}/$checkCount")

        range(topOffset + checkCount + 4, 0, topOffset + checkCount + 4, 1).merge()
        val allPass = checkList.all { it.isPassing() }
        valueStyle(topOffset + checkCount + 4, 0, if (allPass) "ALL CHECKS OK" else "FAILED CHECKS")
            .horizontalAlignment("center").fontColor(if (allPass) "009900" else "FF0000").set()

        range(topOffset + checkCount + 2, 0, topOffset + checkCount + 4, 1)
            .style().fillColor("BBBBBB").borderStyle(BorderStyle.THIN).set()

        return topOffset + checkCount + 4
    }

    private fun Worksheet.writeRemovedGarbage(removedGarbage: List<String>, topOffset: Int): Int {
        valueStyle(topOffset, 0, "Removed garbage:").bold().set()
        removedGarbage.forEachIndexed { index, text ->
            value(topOffset + 1 + index, 0, text)
        }
        range(topOffset, 0, topOffset + removedGarbage.size, 0)
            .style().fillColor("BBBBBB").borderStyle(BorderStyle.THIN).set()

        return removedGarbage.size
    }

    private fun Worksheet.writeMetadata(document: ParsedInvoiceDocument, topOffset: Int): Int {
        valueStyle(topOffset, 0, "Metadata:").bold().set()
        value(topOffset, 1, "⟱ Below ⟱")

        val pageInfoOffset = writePageInfo(document.footer.pageNum, topOffset + 2)

        val fDateOffset = writeFallbackDate(document.header.date, pageInfoOffset + 1)

        val pDateOffset = writeParsedDate(document.header.date, fDateOffset + 1)

        return writeRemovedGarbage(document.extra.removedGarbage, pDateOffset + 1)
    }

    fun generateSheet(document: ParsedInvoiceDocument, filePath: Path) {
        val outputPath = filePath.resolveSibling(filePath.name + ".xlsx")
        println("Writing spreadsheet to \"$outputPath\"...")

        Workbook(outputPath.outputStream(), "Burgmann-OCR", "1.0").apply {
            properties().apply {
                setCompany("Kevin Burgmann")
                setTitle("Burgmann-OCR – ${filePath.name}")
                setSubject("OCR of ${filePath.name}")
                setDescription("Optical Character Recognition of ${filePath.name}")
            }

            newWorksheet(filePath.nameWithoutExtension).apply {
                // Left
                val titleOffset = writeTitleHead(filePath)

                val categoriesOffset = writeCategoryListing(document.extra.checkList, titleOffset + 1)

                writeMetadata(document, categoriesOffset + 2)

                // Right
                writeDataTable(document.lines, document.endLine, document.header.coordinates)

            }
        }.also {
            println("Wrote spreadsheet to: ${outputPath.absolutePathString()}")
        }.finish()
    }


}
