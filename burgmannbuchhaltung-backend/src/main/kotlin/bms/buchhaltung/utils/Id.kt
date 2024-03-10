package bms.buchhaltung.utils

import kotlinx.serialization.Serializable

@Serializable
class Id(private val id: Long) {

    companion object {
        private val hashids = Hashids("aimooPhe2eepohlungee8fai", 5, "df87e00123b69a5c4" /* 01234567890abcdef */)
        private fun decode(string: String) = hashids.decode(string).first()

        fun Number.asId() = Id(this.toLong())
        fun String.asId() = Id(this.toLong())
    }

    private val encoded by lazy { hashids.encode(id) }

    constructor(string: String) : this(decode(string))
    //constructor(int: Int) : this(int.toLong())


    fun toLong() = id
    fun toInt() = id.toInt()
    override fun hashCode() = id.toInt()
    override fun toString() = encoded
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Id

        return id == other.id
    }

}

fun main() {
    val id = Id(1)
    println(id.toInt())
}
