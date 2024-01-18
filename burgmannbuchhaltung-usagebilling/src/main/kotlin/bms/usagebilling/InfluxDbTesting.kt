package bms.usagebilling

import com.influxdb.annotations.Column
import com.influxdb.annotations.Measurement
import com.influxdb.client.domain.WritePrecision
import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import com.influxdb.client.kotlin.WriteKotlinApi
import com.influxdb.client.write.Point
import java.time.Instant
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.measureTime
import kotlin.time.measureTimedValue


suspend fun WriteKotlinApi.testWritePoint() {
    measureTime {
        val points = measureTimedValue {
            (1..1_000_000).map {
                Point
                    .measurement("mem")
                    .addTag("host", "host${Random.nextInt(1..9)}")
                    .addField("used_percent", Random.nextDouble())
                    .time(Instant.now(), WritePrecision.NS)
            }
        }.also { println("Point creation: ${it.duration}") }.value

        measureTime {
            writePoints(points)
        }.let { println("Point write: $it") }
    }.let { println("Point overall: $it") }
}

suspend fun WriteKotlinApi.testWriteLineProtocol() {
    measureTime {
        val records = measureTimedValue {
            (1..1_000_000).map {
                "mem,host=host${Random.nextInt(1..9)} used_percent=${Random.nextDouble()}"
            }
        }.also { println("Line creation: ${it.duration}") }.value

        measureTime {
            writeRecords(records, WritePrecision.NS)
        }.let { println("Line write: $it") }
    }.let { println("Line overall: $it") }
}


suspend fun WriteKotlinApi.testWriteDataClasses() {
    measureTime {
        val records = measureTimedValue {
            (1..1_000_000).map {
                Mem("host${Random.nextInt(1..9)}", Random.nextDouble(), Instant.now())
            }
        }.also { println("Class creation: ${it.duration}") }.value

        measureTime {
            writeMeasurements(records, WritePrecision.NS)
        }.let { println("Class write: $it") }
    }.let { println("Class overall: $it") }
}


@Measurement(name = "mem")
data class Mem(
    @Column(tag = true) val host: String,
    @Column val used_percent: Double,
    @Column(timestamp = true) val time: Instant
)


suspend fun main() {
    println("InfluxDB")

    val token = "IOgGTMqkuKiI_VPuYlm0Ui0it3DXjitUu-emB1275K58_506Z7cWYaKfyOh39MOoFmcmpFxttt9HbFEWtLTG6Q=="
    val org = "rootorg"
    val bucket = "b2"

    println("-- 1 --")
    var client = InfluxDBClientKotlinFactory.create("http://localhost:8086", token.toCharArray(), org, bucket)
    client.use {
        it.getWriteKotlinApi().run {
            testWritePoint()
        }
    }

    println("-- 3 --")
    client = InfluxDBClientKotlinFactory.create("http://localhost:8086", token.toCharArray(), org, bucket)
    client.use {
        it.getWriteKotlinApi().run {
            testWriteLineProtocol()
        }
    }

    println("-- 2 --")
    client = InfluxDBClientKotlinFactory.create("http://localhost:8086", token.toCharArray(), org, bucket)
    client.use {
        it.getWriteKotlinApi().run {
            testWriteDataClasses()
        }
    }

}
