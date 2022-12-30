package com.scs.apps.twitt.service.impl

import com.scs.apps.twitt.*
import com.scs.apps.twitt.constant.KafkaTopic
import com.scs.apps.twitt.converter.PostConverter
import com.scs.apps.twitt.dto.RequestDto
import com.scs.apps.twitt.entity.Author
import com.scs.apps.twitt.entity.Post
import com.scs.apps.twitt.exception.NotFoundException
import com.scs.apps.twitt.producer.StreamsProducer
import com.scs.apps.twitt.repository.AuthorJPARepository
import com.scs.apps.twitt.repository.PostJPARepository
import com.scs.apps.twitt.serde.ActivitySerde
import com.scs.apps.twitt.serde.PostCdcSerde
import com.scs.apps.twitt.service.PostService
import com.scs.apps.twitt.utils.DateTimeUtils
import com.scs.apps.twitt.utils.UuidUtils
import org.apache.kafka.streams.KeyValue
import org.springframework.stereotype.Service
import java.util.*


@Service
class PostServiceImpl(
    private val streamsProducer: StreamsProducer, private val postCdcSerde: PostCdcSerde,
    private val authorRepository: AuthorJPARepository, private val postRepository: PostJPARepository,
    private val postConverter: PostConverter, private val activitySerde: ActivitySerde,
    private val dateTimeUtils: DateTimeUtils, private val uuidUtils: UuidUtils
) : PostService {

    override fun createPost(createPostRequestDto: RequestDto.CreatePostRequestDto, userId: String) {
        val author: Author = authorRepository.findById(UUID.fromString(userId))
            .orElseThrow { NotFoundException("User with id $userId is not found.") }

        val post = Post(
            content = createPostRequestDto.content, title = createPostRequestDto.title,
            creator = author
        )

        val savedPost: Post = postRepository.save(post)
        publishChangedPost(savedPost)
    }

    override fun getPostById(id: UUID): Post {
        val post: Optional<Post> = postRepository.findById(id)
        if (post.isEmpty) {
            return Post()
        }

        post.get().id?.let { publishViewedPost(it) }
        return post.get()
    }

    private fun publishViewedPost(postId: UUID) {
        val activityId: String = uuidUtils.randomUuid().toString()
        val activityMessage: ActivityMessage = ActivityMessage.newBuilder().setId(activityId)
            .setPostId(postId.toString())
            .setActionType(ActivityActionType.POST_VIEWED)
            .setSyncAt(dateTimeUtils.now())
            .build()

        val activityKey: ActivityKey = ActivityKey.newBuilder().setId(activityId).build()
        val message: KeyValue<ActivityKey, ActivityMessage> = KeyValue.pair(activityKey, activityMessage)

        streamsProducer.publish(
            KafkaTopic.ACTIVITY_UPDATED, message, activitySerde.activityKeySerde(),
            activitySerde.activityMessageSerde()
        )
    }

    private fun publishChangedPost(post: Post) {
        val message: KeyValue<PostCdcKey, PostCdcMessage> = postConverter.toPostCdcMessage(post)

        streamsProducer.publish(
            KafkaTopic.POST_CREATED_TOPIC, message, postCdcSerde.postCdcKeySerde(),
            postCdcSerde.postCdcMessageSerde()
        )
    }
}