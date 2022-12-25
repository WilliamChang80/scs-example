package com.scs.apps.twitt.utils

import org.springframework.stereotype.Component
import java.util.*

@Component
class UuidUtils {

    fun randomUuid(): UUID {
        return UUID.randomUUID()
    }
}