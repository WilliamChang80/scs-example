package com.scs.apps.twitt.utils

import com.google.protobuf.Timestamp
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@Component
class DateTimeUtils {

    fun now(): Timestamp {
        val now = Instant.now()
        return Timestamp.newBuilder().setSeconds(now.epochSecond).setNanos(now.nano).build()
    }

    fun parseToTimestamp(dateTime: ZonedDateTime): Timestamp {
        val instant = Instant.ofEpochSecond(
            dateTime.toEpochSecond()
        )

        return Timestamp.newBuilder().setSeconds(instant.epochSecond).setNanos(instant.nano).build()
    }

    fun parseToZdt(timestamp: Timestamp): ZonedDateTime {
        val instant = Instant.ofEpochSecond(
            timestamp.seconds,
            timestamp.nanos.toLong()
        )

        return ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"))
    }
}