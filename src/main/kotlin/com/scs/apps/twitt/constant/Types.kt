package com.scs.apps.twitt.constant

import com.scs.apps.twitt.*
import com.scs.apps.twitt.Comment.CommentKey
import com.scs.apps.twitt.Comment.CommentMessage
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.KTable

typealias UserKTable = KTable<UserKey, UserMessage>
typealias CommentKTable = KTable<CommentKey, CommentMessage>
typealias PostKTable = KTable<PostKey, PostMessage>

typealias EnrichedCommentKStream = KStream<EnrichedCommentKey, EnrichedCommentMessage>
typealias PostCdcKStream = KStream<PostCdcKey, PostCdcMessage>
typealias PostKStream = KStream<PostKey, PostMessage>
typealias EnrichedPostKStream = KStream<EnrichedPostKey, EnrichedPostMessage>
typealias ActivityKStream = KStream<ActivityKey, ActivityMessage>
typealias AggregatedActivityKStream = KStream<ActivityKey, AggregatedActivityMessage>