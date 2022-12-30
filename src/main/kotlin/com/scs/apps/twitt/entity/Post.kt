package com.scs.apps.twitt.entity

import lombok.Builder
import lombok.Value
import org.springframework.data.elasticsearch.annotations.Document
import javax.persistence.Entity
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Document(indexName = "posts")
@Value
@Builder
@Table(name = "posts")
open class Post(
    open val content: String = "",
    open val rating: Double = 0.0,
    open val isDeleted: Boolean = false,
    open val title: String = "",
    open val views: Long = 0,
    open val likes: Int = 0,
    open val dislikes: Int = 0,

    @OneToOne
    open val creator: Author? = null,

    @OneToMany
    open val comments: List<Comment> = emptyList()
) : BaseEntity()