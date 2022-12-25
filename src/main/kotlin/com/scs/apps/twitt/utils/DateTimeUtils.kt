package com.scs.apps.twitt.utils

import com.google.protobuf.Timestamp
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class DateTimeUtils {

    fun now(): Timestamp {
        val now = Instant.now()
        return Timestamp.newBuilder().setSeconds(now.epochSecond).setNanos(now.nano).build()
    }
}