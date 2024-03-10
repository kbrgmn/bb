package bms.buchhaltung.services.usagebilling.db

import bms.buchhaltung.db.model.organizations.Organizations
import bms.buchhaltung.utils.CronSetupData
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.uuid.exposed.kotlinxUUID
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.json

object AutomatedBillingConfigurations : Table() {
    val organization = reference("organization", Organizations)
    val id = kotlinxUUID("id")

    val name = varchar("name", 128)

    val defaultBillingCycle = json("billing_cycle", Json, CronSetupData.serializer())

    override val primaryKey = PrimaryKey(organization, id)
}

fun main() {
    CronSetupData(
        hour = 23, // FRONTEND OK
        minute = 59, // FRONTEND OK
        second = 59, // FRONTEND OK
        day = null, // FRONTEND OK
        dayAt = null, // FRONTEND ~~~~
        days = null, // FRONTEND OK
        daysOfWeek = null, // FRONTEND ~~~~
        lastDayOfWeek = null, // FRONTEND ~~~~
        month = null, // FRONTEND ~~~~
        months = null, // FRONTEND ~~~~
        year = null, // FRONTEND TODO
//        dateEvery = null // FRONTEND ~~~~
        dateEvery = CronSetupData.OriginPlusEveryXDays(days = 28, date = LocalDateTime(2023, 1, 1, 12, 30, 42)),
    ).let {
        println(Json.encodeToString(it))
        println("Next date: " + it.nextDate())
        println(it.expression())

        println("" + it.datesUntilEndOfYear().size + "x until end of year (${it.datesUntilEndOfYear().take(4)})")

        it.nextDateList(20).forEachIndexed { index, date ->
            println("${index + 1}. $date (${date.dayOfWeek.name})")
        }
    }
}
