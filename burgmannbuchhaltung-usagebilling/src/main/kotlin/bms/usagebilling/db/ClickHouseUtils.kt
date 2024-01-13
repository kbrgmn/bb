@file:Suppress("NOTHING_TO_INLINE")

package bms.usagebilling.db

import com.clickhouse.data.ClickHouseOutputStream
import com.clickhouse.data.ClickHouseRecord
import com.clickhouse.data.ClickHouseValue
import com.clickhouse.data.format.BinaryStreamUtils
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toJavaZoneId
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.uuid.UUID
import kotlinx.uuid.toJavaUUID

inline operator fun ClickHouseRecord.get(i: Int): ClickHouseValue = this.getValue(i)

inline fun ClickHouseValue.getInstant() = asInstant(3).toKotlinInstant()
inline fun ClickHouseValue.getNullableInstant() = if (isNullOrEmpty) null else getInstant()

inline fun ClickHouseOutputStream.writeUuid(uuid: UUID) = BinaryStreamUtils.writeUuid(this, uuid.toJavaUUID())
inline fun ClickHouseOutputStream.writeUuid(uuid: java.util.UUID) = BinaryStreamUtils.writeUuid(this, uuid)

inline fun ClickHouseOutputStream.writeString(string: String) = BinaryStreamUtils.writeString(this, string)

val JAVA_UTC_TIMEZONE: java.util.TimeZone = java.util.TimeZone.getTimeZone(TimeZone.UTC.toJavaZoneId())
inline fun ClickHouseOutputStream.writeDateTime64(utcTimestamp: Instant) = BinaryStreamUtils.writeDateTime64(
    this,
    utcTimestamp.toLocalDateTime(TimeZone.UTC).toJavaLocalDateTime(),
    JAVA_UTC_TIMEZONE
)

inline fun ClickHouseOutputStream.writeNull() = BinaryStreamUtils.writeNull(this)
inline fun ClickHouseOutputStream.writeNotNull() = BinaryStreamUtils.writeNonNull(this)


//inline fun ClickHouseOutputStream.writeBoolean(boolean: Boolean)= BinaryStreamUtils.writeBoolean(this, boolean)
