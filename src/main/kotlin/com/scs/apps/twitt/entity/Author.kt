package com.scs.apps.twitt.entity

import lombok.Builder
import org.springframework.data.elasticsearch.annotations.Document
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Builder
@Document(indexName = "users")
@Table(name = "authors")
open class Author(
    open val name: String = "",
    open val profilePic: String = ""
) : BaseEntity()