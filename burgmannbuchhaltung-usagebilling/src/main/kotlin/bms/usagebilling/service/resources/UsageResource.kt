package bms.usagebilling.service.resources

import bms.usagebilling.db.get
import bms.usagebilling.db.getInstant
import bms.usagebilling.db.getNullableInstant
import bms.usagebilling.db.writeDateTime64
import bms.usagebilling.db.writeNotNull
import bms.usagebilling.db.writeNull
import bms.usagebilling.db.writeString
import bms.usagebilling.db.writeUuid
import com.clickhouse.data.ClickHouseOutputStream
import com.clickhouse.data.ClickHouseRecord
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID
import kotlinx.uuid.toKotlinUUID

@Serializable
data class InsertUsageResource(
    val group: UUID,
    val id: UUID,
    val type: String,
    val startDate: Instant = Clock.System.now(),
    val endDate: Instant? = null,
    val billable: Boolean = true,
    val reference: String = "",
    val properties: String = ""
)

data class UsageEndResource(
    val group: UUID,
    val id: UUID,
    val end: Instant
)


@Serializable
data class UsageResource(
    val organization: UUID,
    val group: UUID,
    val id: UUID,
    val type: String,
    val start: Instant,
    val end: Instant?,
    val billable: Boolean = true,
    val reference: String = "",
    val properties: String = ""
) {
    fun writeToClickhouse(stream: ClickHouseOutputStream) {
        stream.apply {
            writeUuid(organization)
            writeUuid(group)
            writeUuid(id)

            writeString(type)
            writeDateTime64(start)
            if (end != null) {
                writeNotNull()
                writeDateTime64(end)
            } else writeNull()
            writeBoolean(billable)

            writeString(reference)
            writeString(properties)
        }
    }

    companion object {

        fun fromClickhouseRecord(record: ClickHouseRecord) = UsageResource(
            organization = record[0].asUuid().toKotlinUUID(),
            group = record[1].asUuid().toKotlinUUID(),
            id = record[2].asUuid().toKotlinUUID(),
            type = record[3].asString(),
            start = record[4].getInstant(),
            end = record[5].getNullableInstant(),
            billable = record[6].asBoolean(),
            reference = record[7].asString(),
            properties = record[8].asString()
        )
    }
}


