package bms.buchhaltung.utils

import com.ucasoft.kcron.Cron
import com.ucasoft.kcron.core.builders.DelicateIterableApi
import com.ucasoft.kcron.core.extensions.at
import com.ucasoft.kcron.core.extensions.days
import com.ucasoft.kcron.core.extensions.daysOfWeek
import com.ucasoft.kcron.core.extensions.hours
import com.ucasoft.kcron.core.extensions.lastDay
import com.ucasoft.kcron.core.extensions.lastDayOfWeek
import com.ucasoft.kcron.core.extensions.lastWorkDay
import com.ucasoft.kcron.core.extensions.minutes
import com.ucasoft.kcron.core.extensions.months
import com.ucasoft.kcron.core.extensions.on
import com.ucasoft.kcron.core.extensions.seconds
import com.ucasoft.kcron.core.extensions.years
import com.ucasoft.kcron.kotlinx.datetime.plusDays
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class CronSetupData(
    val hour: Int,
    val minute: Int,
    val second: Int,

    /**
     * last day: -1
     * last weekday: -2
     *
     * exclusive with dayAt, days
     */
    val day: Int? = null,


    /**
     * every _first_ days; starting at _second_
     *
     * exclusive with day, days
     */
    val dayAt: Pair<Int, Int>? = null,

    /**
     * on specific days of the month
     *
     * exclusive with dayAt, day
     */
    val days: List<Int>? = null,


    /**
     * every *second*th *first*day of the month
     * e.g. every 5th Sunday of the month
     */
    val daysOfWeek: Pair<Int, Int>? = null,

    /**
     * 0: every end of week
     * 1: last monday
     * 7: last sunday
     */
    val lastDayOfWeek: Int? = null,

    /**
     * Month, exclusive with months
     */
    val month: Int? = null,

    /**
     * Months, exclusive with month
     */
    val months: List<Int>? = null,

    /**
     * _first_ start
     * _second_ end
     */
    val year: Pair<Int, Int?>? = null,


    /**
     * Determine date based on origin plus X days instead of fixed dates
     */
    val dateEvery: OriginPlusEveryXDays? = null
) {

    companion object {
        val START_MONTHLY = CronSetupData(hour = 0, minute = 0, second = 0, day = 1)
        val END_MONTHLY = CronSetupData(hour = 23, minute = 59, second = 59, day = -1)

        val START_QUARTERLY = CronSetupData(hour = 0, minute = 0, second = 0, day = 1, months = listOf(1, 4, 7, 10))
        val END_QUARTERLY = CronSetupData(hour = 23, minute = 59, second = 59, day = -1, months = listOf(3, 6, 9, 12))
    }

    @Serializable
    data class OriginPlusEveryXDays(
        var date: LocalDateTime = currentDate(),
        val days: Int,
    ) {

        companion object {
            private fun currentDate() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        }

        fun setNext() {
            date = nextDate()
        }

        fun nextDate() = date.plusDays(days)

        fun nextList(num: Int = 10): List<LocalDateTime> {
            val result = ArrayList<LocalDateTime>()
            var cur = date

            repeat(num) {
                result += cur
                cur = cur.plusDays(days)
            }

            return result
        }

        fun asIterable(max: Int = 400): Iterable<LocalDateTime> {
            var cur = date

            return Iterable {
                iterator {
                    repeat(max) {
                        yield(cur)
                        cur = cur.plusDays(days)
                    }
                }
            }
        }

        override fun toString(): String {
            return "every $days days (from $date)"
        }
    }

    private val kcronBuilder by lazy {
        Cron.builder()
            .hours(hour)
            .minutes(minute)
            .seconds(second)
            .apply {
                when {
                    day != null -> when (day) {
                        -1 -> lastDay()
                        -2 -> lastWorkDay()
                        0 -> days(0) // FIXME?
                        else -> days(day)
                    }

                    dayAt != null -> days(dayAt.first at dayAt.second)
                    days != null -> this.days(*days.toIntArray())
                }
                when {
                    daysOfWeek != null -> daysOfWeek(daysOfWeek.first on daysOfWeek.second)
                    lastDayOfWeek != null -> lastDayOfWeek(lastDayOfWeek)
                }

                when {
                    months != null -> months(*months.toIntArray())
                    month != null -> months(month)
                }

                if (year != null) years(year.first..(year.second ?: year.first))
            }
    }

    fun nextDate(): LocalDateTime? {
        return dateEvery?.nextDate() ?: kcronBuilder.nextRun
    }

    @OptIn(DelicateIterableApi::class)
    fun nextDateList(maxCount: Int = 10): List<LocalDateTime> {
        return dateEvery?.nextList(maxCount) ?: kcronBuilder.asIterable().take(maxCount)
    }

    @OptIn(DelicateIterableApi::class)
    fun datesUntilEndOfYear(): List<LocalDateTime> {
        val currentYear by lazy { Clock.System.now().toLocalDateTime(TimeZone.UTC).year }
        return (dateEvery?.asIterable() ?: kcronBuilder.asIterable()).takeWhile {
            it.year == (dateEvery?.date?.year ?: currentYear)
        }
    }

    fun expression(): String {
        return dateEvery?.toString() ?: kcronBuilder.expression
    }
}
