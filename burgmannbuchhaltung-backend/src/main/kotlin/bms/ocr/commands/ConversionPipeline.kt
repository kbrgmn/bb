package bms.ocr.commands

import bms.ocr.commands.CommandRunner.runCommand
import java.io.File
import java.nio.file.Path
import java.security.SecureRandom
import java.util.*
import kotlin.io.path.*
import kotlin.random.Random

object ConversionPipeline {

    private val tmpDir = Path.of(System.getProperty("java.io.tmpdir")).resolve("burgmann-ocr").createDirectories()

    private val secureRandom = SecureRandom.getInstanceStrong()
    private val base64Encoder = Base64.getUrlEncoder()
    fun randomString() = "${System.currentTimeMillis()}-${
        base64Encoder.encodeToString(Random.nextBytes(8))
            .replace("-", "")
            .replace("_", "")
            .replace("=", "")
    }"

    fun getTmpFile(): Path = tmpDir.resolve(randomString())

    // Convert PDF (portable document format) to PGM (portable grayscale map) using pdftoppm
    fun convertPdfToPgm(name: String, inputPath: Path): File {
        val tmpFile = getTmpFile()

        val args = /*when {
            "singlefile" in pdftoppmSupported -> */ arrayOf("-singlefile", "-gray")
            /*else -> arrayOf("-gray")
        }*/

        return runCommand(
            name,
            1, "pdftoppm", arrayOf(
                *args,
                inputPath.absolutePathString(),
                tmpFile.absolutePathString()
            )
        ).workingDir!!.resolve("${tmpFile.absolutePathString()}.pgm")
    }

    fun unpaperPgm(name: String, inputPath: File, outputPath: String): File {
        val outputFile = outputPath.replace(".pgm", "")

        val args = when {
            unpaperVersion.startsWith("7") -> arrayOf(
                "--post-border", "70,50,50,40",
                "-l", "none",
                "--overwrite",
                "--sheet", "1"
            )

            unpaperVersion.startsWith("6") -> arrayOf(
                "--post-border", "70,50,50,40",
                "-l", "none",
                "--overwrite",
                "--sheet", "1"
            )

            else -> throw IllegalArgumentException("Unsupported pdftoppm version: $unpaperVersion")
        }

        return runCommand(
            name,
            2, "unpaper", arrayOf(
                *args,
                inputPath.absolutePath,
                "$outputFile.pgm",
            ),
            printStderr = false
        ).workingDir!!.resolve("$outputFile.pgm")
    }

    fun addTesseractConfig(version: Int, config: String) {
        if (Path("t-conf$version").notExists())
            Path("t-conf$version").writeLines(config.lines().map { it.trim() }.filterNot { it.isBlank() })
    }

    fun runTesseract(name: String, inputPath: File, version: Int): String {

        when (version) {
            1 -> addTesseractConfig(
                version,
                """
                tessedit_char_whitelist abcdefghijklmnoopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZäöüÄÖÜß0:1.2,34 567€89
                textord_heavy_nr 1
                """
            )

            2 -> addTesseractConfig(
                version,
                """
                tessedit_char_whitelist abcdefghijklmnoopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZäöüÄÖÜß0:1.2,34 567€89
                """
            )
        }

        return runCommand(
            name,
            3, "tesseract", arrayOf(
                inputPath.absolutePath,
                "stdout",
                "-l", "deu",
                "--psm", "6",
                //"-c", "textord_heavy_nr=1",
                //"-c", "tessedit_char_whitelist=abcdefghijklmnoopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZäöüÄÖÜß0123456789€.,: ",
                "hocr",
                "t-conf$version"
            ),
            printStdout = false,
            workingDir = null
        ).stdout
    }

    /*private val pdftoppmSupported by lazy {
        runCommand("Config pdftoppm", 1, "pdftoppm", arrayOf("--help")).stderr
    }*/

    private val unpaperVersion by lazy {
        runCommand("Config unpaper", 0, "unpaper", arrayOf("--version")).stdout
    }

    fun runConversionPipeline(inputPath: Path, version: Int): String {
        val name = inputPath.name

        val pgmFile = convertPdfToPgm(name, inputPath)
        println("$name: >> Created Portable Grayscale Map: ${pgmFile.absolutePath}")

        val unpaperFile = unpaperPgm(name, pgmFile, pgmFile.absolutePath + "-normalized")
        pgmFile.deleteOnExit() // FI/XME DEBUG

        println("$name: >> Created normalized file:        ${unpaperFile.absolutePath}")
        val tesseractXml = runTesseract(name, unpaperFile, version)
        unpaperFile.deleteOnExit() // FI/XME DEBUG
        //Path.of("work/3-tesseract-out/${unpaperFile.nameWithoutExtension}-ocr.xml").writeText(tesseractXml)

        return tesseractXml
    }
}
