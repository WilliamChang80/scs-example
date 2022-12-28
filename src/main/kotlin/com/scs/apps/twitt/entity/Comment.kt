package com.scs.apps.twitt.entity

import lombok.Builder
import javax.persistence.Entity
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Builder
@Table(name = "comments")
open class Comment(
    open val comment: String = "",
    open val postId: String = "",

    @OneToOne
    open val user: Author? = null
) : BaseEntity()