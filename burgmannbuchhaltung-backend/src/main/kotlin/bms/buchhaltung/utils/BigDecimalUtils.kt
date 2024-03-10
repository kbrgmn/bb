package bms.buchhaltung.utils

import java.math.BigDecimal

@Suppress("EqualsOrHashCode")
object BigDecimalUtils {

    /**
     * Is equals that does not compare scale (so thath: 5.1 == 5.10 == 5.100000)
     */
    @Suppress("NOTHING_TO_INLINE")
    inline infix fun BigDecimal.isEqual(other: BigDecimal): Boolean = this.compareTo(other) == 0
    inline infix fun BigDecimal.isNotEqual(other: BigDecimal): Boolean = !(this isEqual other)

    infix fun BigDecimal.equals(other: Any?): Boolean = this.compareTo(
        when (other) {
            is BigDecimal -> other
            is Int -> BigDecimal(other)
            is Long -> BigDecimal(other)
            is Double -> BigDecimal(other)
            is CharArray -> BigDecimal(other)
            is String -> BigDecimal(other)
            else -> throw IllegalArgumentException("")
        }
    ) == 0


}
