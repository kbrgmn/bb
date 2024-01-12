@file:Suppress("NOTHING_TO_INLINE")

package bms.usagebilling.db

import com.clickhouse.data.ClickHouseOutputStream
import com.clickhouse.data.format.BinaryStreamUtils
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toJavaZoneId
import kotlinx.datetime.toLocalDateTime
import kotlinx.uuid.UUID
import kotlinx.uuid.toJavaUUID

inline fun ClickHouseOutputStream.writeUuid(uuid: UUID) = BinaryStreamUtils.writeUuid(this, uuid.toJavaUUID())
inline fun ClickHouseOutputStream.writeUuid(uuid: java.util.UUID) = BinaryStreamUtils.writeUuid(this, uuid)

inline fun ClickHouseOutputStream.writeString(string: String) = BinaryStreamUtils.writeString(this, string)

val JAVA_UTC_TIMEZONE: java.util.TimeZone = java.util.TimeZone.getTimeZone(TimeZone.UTC.toJavaZoneId())
inline fun ClickHouseOutputStream.writeDateTime64(utcTimestamp: Instant) = BinaryStreamUtils.writeDateTime64(
    this,
    utcTimestamp.toLocalDateTime(TimeZone.UTC).toJavaLocalDateTime(),
    JAVA_UTC_TIMEZONE
)

//inline fun ClickHouseOutputStream.writeBoolean(boolean: Boolean)= BinaryStreamUtils.writeBoolean(this, boolean)
