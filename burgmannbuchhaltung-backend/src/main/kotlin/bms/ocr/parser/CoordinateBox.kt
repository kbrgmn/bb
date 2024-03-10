package bms.ocr.parser

import org.jsoup.nodes.Element

data class CoordinateBox(val startLeft: Int, val startTop: Int, val endRight: Int, val endBottom: Int) {
    init {
        check(startLeft < endRight)
        check(startTop < endBottom)
    }

    fun getHorizontalRange(): IntRange = startLeft..endRight

    fun getSizeString() = "${endRight - startLeft}x${endBottom - startTop}"
}

object CoordinateUtils {
    fun Element.getCoordinateBox() = attr("title").parseTitleToCoordinates()

    fun String.parseTitleToCoordinates(): CoordinateBox {
        val nums = this
            .split("; ")
            .first { it.startsWith("bbox ") }
            .removePrefix("bbox ")
            .split(" ")
            .map { it.toInt() }
        return CoordinateBox(nums[0], nums[1], nums[2], nums[3])
    }
}
