package bms.ocr

import bms.ocr.commands.ConversionPipeline
import bms.ocr.parser.OcrOutputParser
import bms.ocr.parser.ParsedInvoiceDocument
import bms.ocr.spreadsheet.MultiCsvGenerator
import bms.ocr.spreadsheet.SheetGenerator
import java.nio.file.Path
import java.util.concurrent.*
import kotlin.io.path.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.name
import kotlin.io.path.notExists

object OcrManager {

    fun tryParseDocument(path: Path): Result<ParsedInvoiceDocument> {
        try {

            val name = path.name
            val tries = ArrayList<ParsedInvoiceDocument>()

            for (version in 1..2) {
                println("$name, $version: Running conversion pipeline, version $version...")
                val xml = ConversionPipeline.runConversionPipeline(path, version)

                println("$name, $version: Parsing ${xml.lines().size} results...")

                try {
                    val tryParseInvoiceDocument = OcrOutputParser().parse(name, version, xml)

                    if (tryParseInvoiceDocument.parseErrorScore() == 0) return Result.success(tryParseInvoiceDocument)

                    println("$name, $version: Did not receive a score 0 document!")
                    tries.add(tryParseInvoiceDocument)
                } catch (e: Exception) {
                    println("$name, $version: ${e.stackTraceToString()}")
                    println("$name, $version: Was unable to read document.")
                }
            }

            println("$name: Parsing completed, scores: ${tries.map { it.parseErrorScore() }}")

            val bestResult = tries.minByOrNull { it.parseErrorScore() }

            return when {
                bestResult != null -> Result.success(bestResult)
                else -> Result.failure(IllegalArgumentException("Unable to parse document."))
            }

        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    fun ocrFiles(filePaths: List<Path>) {

        val executor: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2)
        try {
            executor.invokeAll(filePaths.map { path ->
                Callable {
                    val name = path.name

                    try {
                        val parsedDocument = tryParseDocument(path).getOrElse {
                            throw IllegalArgumentException("$name: Was completely unable to parse: $path\nThis document is probably completely distorted into unreadability. Please scan it again. Error: ${it.message}")
                        }
                        println("$name: Using document with score: ${parsedDocument.parseErrorScore()} (lower is better)")

                        println("$name: Generating OOXML spreadsheet...")
                        SheetGenerator().generateSheet(parsedDocument, path)
                        println("$name: Done.")
                        Pair(path, parsedDocument)
                    } catch (e: IllegalArgumentException) {
                        e.printStackTrace()
                        Pair(path, null)
                    }
                }
            }, (30 * filePaths.size).toLong(), TimeUnit.SECONDS).mapNotNull {
                it.get()
            }.also {
                println()
                println()
                println("==========")
                println("Statistics")
                println("==========")
                println()
                it.mapNotNull { it.second }.also {
                    // Dates
                    println("Read dates: ${
                        it.asSequence().map { it.header.date }.sortedBy { it.year }.sortedBy { it.month }.sortedBy { it.day }
                            .joinToString { it.run { "20$year-$month-$day" } }
                    }."
                    )
                    // Lines
                    println("Read ${it.sumOf { it.lines.size }} lines in ${it.size} files.")

                    // Warnings
                    it.filter { it.extra.warnings.isNotEmpty() }.forEach {
                        println("Warning in ${it.header.date}: ${it.extra.warnings.joinToString()}")
                    }
                    // Failed
                    it.filterNot { it.extra.checkList.all { it.isPassing() } }
                        .forEach {
                            println(
                                "Checks failed: ${it.header.date} (error score ${it.parseErrorScore()}): ${
                                    it.extra.checkList.filterNot { it.isPassing() }.joinToString()
                                }"
                            )
                        }
                    // Unknown categories
                    it.filter { it.getUnknownList().isNotEmpty() }.forEach {
                        println("Unknown: ${it.header.date}: ${it.getUnknownList().joinToString()}")
                    }
                }
            }.also {
                val fullCount = filePaths.size
                val failCount = it.count { it.second == null }
                val successCount = it.count { it.second != null }
                println("Read $successCount / $fullCount files. Unreadable: $failCount / $fullCount.")
                it.filter { it.second == null }.forEachIndexed { index, pair ->
                    println("Could not read ${index + 1} / $failCount: Unreadable ${pair.first}")
                }
            }.also {
                MultiCsvGenerator.makeCsv(it.mapNotNull { it.second })
                println("Wrote CSV file!")
            }
        } catch (e: ExecutionException) {
            e.printStackTrace()
            println("Error while executing tasks: ${e.message}")
        } finally {
            println("All tasks returned, shutting down executor...")
            executor.shutdown()
        }
    }
}


fun main(args: Array<String>) {
    println("OCR")
    println()
    println("You are here: ${Path("").absolutePathString()}")

    val suppliedFilePaths: Array<String> = when {
        args.isEmpty() -> {
            print("Enter PDF input: ")
            val enteredFile = args.getOrNull(0) ?: readlnOrNull() ?: return println("No input!")
            arrayOf(enteredFile)
        }

        args.size == 1 -> arrayOf(args.first())
        args.size > 1 -> args
        else -> args
    }

    val filePaths = suppliedFilePaths.map {
        val path = Path(it)
        println("Supplied \"${path.absolutePathString()}\"...")
        if (path.notExists()) return println("Could not find: ${path.absolutePathString()}!")
        path
    }

    OcrManager.ocrFiles(filePaths)

    println("All done!")
}
