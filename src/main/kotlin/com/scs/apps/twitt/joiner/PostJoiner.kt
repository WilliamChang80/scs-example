package com.scs.apps.twitt.joiner

import com.scs.apps.twitt.*
import com.scs.apps.twitt.serde.EnrichedPostSerde
import com.scs.apps.twitt.serde.PostSerde
import mu.KotlinLogging
import org.apache.kafka.common.utils.Bytes
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.state.KeyValueStore
import org.springframework.stereotype.Component

@Component
class PostJoiner(private val postSerde: PostSerde, private val enrichedPostSerde: EnrichedPostSerde) {
    private val logger = KotlinLogging.logger {}

    fun joinPostWithUser(post: PostMessage, user: UserMessage?): EnrichedPostMessage {
        logger.info("joining postMessage $post with user message $user")
        val enrichedPostBuilder: EnrichedPostMessage.Builder = EnrichedPostMessage.newBuilder()
            .setId(post.id)
            .setContent(post.content)
            .setTitle(post.title)
            .setCreatedAt(post.createdAt)
            .setUpdatedAt(post.updatedAt)

        if (user != null) {
            enrichedPostBuilder.creatorId = user.id
            enrichedPostBuilder.user = user
        }

        return enrichedPostBuilder.build()
    }

    fun materialized(): Materialized<PostKey, EnrichedPostMessage, KeyValueStore<Bytes, ByteArray>> {
        return Materialized.`as`<PostKey, EnrichedPostMessage, KeyValueStore<Bytes, ByteArray>>("post-user-joined-state-store")
            .withKeySerde(postSerde.postKeySerde())
            .withValueSerde(enrichedPostSerde.enrichedPostMessageSerde())
    }

    fun foreignKeyExtractor(postMessage: PostMessage): UserKey {
        return UserKey.newBuilder().setId(postMessage.creatorId).build()
    }
}