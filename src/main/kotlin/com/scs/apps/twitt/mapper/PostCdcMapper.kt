package com.scs.apps.twitt.mapper

import com.scs.apps.twitt.PostCdcKey
import com.scs.apps.twitt.PostCdcMessage
import com.scs.apps.twitt.PostKey
import com.scs.apps.twitt.PostMessage
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.kstream.KeyValueMapper
import org.springframework.stereotype.Component

@Component
class PostCdcMapper : KeyValueMapper<PostCdcKey, PostCdcMessage, KeyValue<PostKey, PostMessage>> {

    override fun apply(key: PostCdcKey, value: PostCdcMessage): KeyValue<PostKey, PostMessage> {
        return KeyValue.pair(
            PostKey.newBuilder().setId(key.id).build(),
            PostMessage.newBuilder().setId(value.id).setRating(value.rating).setContent(value.content)
                .setIsDeleted(value.isDeleted)
                .setCreatedAt(value.createdAt).build()
        )
    }
}