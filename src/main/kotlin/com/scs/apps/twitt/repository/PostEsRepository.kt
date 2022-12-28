package com.scs.apps.twitt.repository

import com.scs.apps.twitt.annotation.EsRepository
import com.scs.apps.twitt.entity.Post
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository
import org.springframework.stereotype.Repository
import java.util.*

@EsRepository
@Repository
interface PostEsRepository : ElasticsearchRepository<Post, UUID> {
}