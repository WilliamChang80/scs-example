package com.scs.apps.twitt.service.impl

import com.google.protobuf.Timestamp
import com.google.protobuf.util.Timestamps
import com.scs.apps.twitt.EnrichedPostMessage
import com.scs.apps.twitt.converter.PostConverter
import com.scs.apps.twitt.entity.Post
import com.scs.apps.twitt.repository.PostEsRepository
import com.scs.apps.twitt.service.PostEventService
import com.scs.apps.twitt.utils.DateTimeUtils
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.*

@Service
class PostEventServiceImpl(
    private val dateTimeUtils: DateTimeUtils, private val postEsRepository: PostEsRepository,
    private val postConverter: PostConverter
) : PostEventService {

    private val logger = KotlinLogging.logger {}

    override fun upsert(enrichedPostMessage: EnrichedPostMessage) {
        val currentPost: Optional<Post> = postEsRepository.findById(UUID.fromString(enrichedPostMessage.id))
        if (currentPost.isPresent && isIgnoredMessage(currentPost.get().updatedAt, enrichedPostMessage.updatedAt)) {
            logger.info("ignored postMessage with uuid ${enrichedPostMessage.id}")
            return
        }

        val post: Post = postConverter.toEntity(enrichedPostMessage)

        postEsRepository.save(post)
    }

    private fun isIgnoredMessage(entityUpdatedAt: ZonedDateTime?, messageUpdatedAt: Timestamp): Boolean {
        if (entityUpdatedAt == null) {
            return false
        }

        return Timestamps.compare(
            dateTimeUtils.parseToTimestamp(entityUpdatedAt),
            messageUpdatedAt
        ) > 0
    }
}