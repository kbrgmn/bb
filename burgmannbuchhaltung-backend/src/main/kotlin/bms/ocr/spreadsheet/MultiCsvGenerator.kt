package bms.ocr.spreadsheet

import bms.ocr.parser.ParsedInvoiceDocument
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.OutputStream

object MultiCsvGenerator {

    fun comboToAccountingRecord(combo: Pair<String, String>, file: String) =
        when (combo) {
            Pair("Losung", "Eingang") -> Pair("2700", "4000")
            Pair("Lottoauszahlung", "Ausgang") -> Pair("2010", "2700")
            Pair("Kartenzahlung", "Ausgang") -> Pair("2775", "2700")
            Pair("Automatenverkauf", "Ausgang") -> Pair("2790", "2700")
            Pair("Bankeinzahlung", "Ausgang") -> Pair("2770", "2700")
            Pair("Zähldifferenz", "Eingang") -> Pair("2700", "2771")
            Pair("Zähldifferenz", "Ausgang") -> Pair("2771", "2700")
            Pair("Manko", "Eingang") -> Pair("2700", "2771")
            Pair("Manko", "Ausgang") -> Pair("2771", "2700")
            Pair("Bankspesen", "Ausgang") -> Pair("7790", "2700")
            Pair("Bank spesen", "Ausgang") -> Pair("7790", "2700")
            Pair("Spesen", "Ausgang") -> Pair("7790", "2700")
            Pair("BIPA", "Ausgang") -> Pair("7600", "2700")
            Pair("WienBillett", "Ausgang") -> Pair("7600", "2700")
            Pair("Hofer", "Ausgang") -> Pair("7600", "2700")
            Pair("Post", "Ausgang") -> Pair("7380", "2700")
            Pair("Vortag", "Eingang") -> null
            Pair("Kassa Bank Sparkassa", "Kassastand"), Pair("Münzladenstand", "Kassastand") -> null
            else -> {
                println("Unknown combo in $file: $combo")

                when(combo.second) {
                    "Eingang" -> Pair("2700", "9999")
                    "Ausgang" -> Pair("9999", "2700")
                    else ->Pair("9999", "9999")
                }

            }
        }

    fun documentToCsvLines(doc: ParsedInvoiceDocument): List<List<String>> =
        doc.lines.filter { it.category != null }.mapNotNull {
            val accRec = comboToAccountingRecord(Pair(it.desc, it.category!!), doc.header.date.toGermanStringDate())

            if (accRec != null) {
                val (debit, credit) = accRec
                listOf(doc.header.date.toGermanStringDate(), debit, credit, it.price.toString().substringAfter("EUR "))
            } else {
                null
            }
        }

    private val csvFormat = listOf("date", "debit", "credit", "amount")

    fun makeCsvString(lines: String, outputStream: OutputStream) {
        println("Making CSV of:\n $lines")
        val outLines = Json.parseToJsonElement(lines).jsonArray.map {
            it.jsonObject.let {
                csvFormat.map { column -> it[column]!!.jsonPrimitive.content }
            }
        }.toMutableList().also {
            it.add(0, csvFormat) // title
        }

        csvWriter().writeAll(outLines, outputStream)
    }

    fun makeCsv(documents: List<ParsedInvoiceDocument>) {
        val lines = ArrayList<List<String>>()
        lines.add(listOf("date", "debit", "credit", "amount"))

        val documentLines = HashMap<String, List<List<String>>>()

        lines.addAll(documents.sortedBy { it.header.date.toGermanStringDate() }.flatMap {
            documentToCsvLines(it).apply { documentLines[it.header.date.toGermanStringDate()] = this }
        })

        csvWriter().writeAll(lines, "parsed-documents.csv")
    }

}
