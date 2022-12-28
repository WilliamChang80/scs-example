package com.scs.apps.twitt.service

import com.scs.apps.twitt.EnrichedPostMessage

interface PostEventService {

    fun upsert(enrichedPostMessage: EnrichedPostMessage)
}