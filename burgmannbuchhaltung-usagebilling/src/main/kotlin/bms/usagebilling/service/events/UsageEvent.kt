package bms.usagebilling.service.events

import bms.usagebilling.dateTimeExample
import bms.usagebilling.db.writeDateTime64
import bms.usagebilling.db.writeString
import bms.usagebilling.db.writeUuid
import com.clickhouse.data.ClickHouseOutputStream
import com.clickhouse.data.ClickHouseRecord
import com.clickhouse.data.format.BinaryStreamUtils
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toJavaZoneId
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.uuid.UUID
import kotlinx.uuid.generateUUID
import kotlinx.uuid.toJavaUUID
import kotlinx.uuid.toKotlinUUID

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class InsertUsageEvent(
    /**
     * Ignored for InsertUsageEvent, taken from API key
     */
    @EncodeDefault(EncodeDefault.Mode.NEVER) val orgId: UUID? = null, // IGNORED
    val group: UUID? = null,
    val id: UUID? = UUID.generateUUID(),
    val name: String,
    val timestamp: Instant? = Clock.System.now(),
    val billable: Boolean? = true,
    val reference: String? = "",
    val properties: String? = ""
) {
    fun toNormalUsageEvent(organization: UUID, groupOverwrite: UUID? = null) = UsageEvent(
        organization = organization,
        group = groupOverwrite ?: group ?: error("No project ID was set for event"),
        id = id ?: UUID.generateUUID(),
        name = name,
        timestamp = timestamp ?: Clock.System.now(),
        billable = billable ?: true,
        reference = reference ?: "",
        properties = properties ?: ""
    )

     companion object {
        val minimalExample = InsertUsageEvent(
            group = UUID.generateUUID(),
            id = null,
            name = "WeatherForecast24hRequested",
            timestamp = null,
            billable = null,
            reference = null,
            properties = null
        )
        val fullExample = InsertUsageEvent(
            group = UUID.generateUUID(),
            id = UUID.generateUUID(),
            name = "WeatherForecast7dRequested",
            timestamp = dateTimeExample(),
            billable = true,
            reference = "ipv4=198.51.100.2,apiKey=cde456",
            properties = """{"key2": "value2"}"""
        )
    }
}

@Serializable
data class UsageEvent(
    val organization: UUID,
    val group: UUID,
    val id: UUID = UUID.generateUUID(),
    val name: String,
    val timestamp: Instant = Clock.System.now(),
    val billable: Boolean = true,
    val reference: String = "",
    val properties: String = ""
) {
    fun writeToClickhouse(stream: ClickHouseOutputStream) {
        stream.apply {
            writeUuid(organization)
            writeUuid(group)
            writeUuid(id)

            writeString(name)
            writeDateTime64(timestamp)
            writeBoolean(billable)

            writeString(reference)
            writeString(properties)
        }
    }

    companion object {
        val minimalExample = UsageEvent(
            organization = UUID.generateUUID(),
            group = UUID.generateUUID(),
            id = UUID.generateUUID(),
            name = "WeatherForecast24hRequested",
            timestamp = dateTimeExample(),
            reference = "ipv4=198.51.100.1,apiKey=apiKeyAbc123",
        )
        val fullExample = UsageEvent(
            organization = UUID.generateUUID(),
            group = UUID.generateUUID(),
            id = UUID.generateUUID(),
            name = "WeatherForecast7dRequested",
            timestamp = dateTimeExample(),
            billable = true,
            reference = "ipv4=198.51.100.2,apiKey=cde456",
            properties = """{"key2": "value2"}"""
        )


        fun fromClickhouseRecord(record: ClickHouseRecord): UsageEvent {
            return UsageEvent(
                organization = record.getValue(0).asUuid().toKotlinUUID(),
                group = record.getValue(1).asUuid().toKotlinUUID(),
                id = record.getValue(2).asUuid().toKotlinUUID(),
                name = record.getValue(3).asString(),
                timestamp = record.getValue(4).asInstant(3).toKotlinInstant(),
                billable = record.getValue(5).asBoolean(),
                reference = record.getValue(6).asString(),
                properties = record.getValue(7).asString()
            )
        }
    }
}
