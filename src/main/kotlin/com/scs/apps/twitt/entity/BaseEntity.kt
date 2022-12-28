package com.scs.apps.twitt.entity

import lombok.Data
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.ZonedDateTime
import java.util.*
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.MappedSuperclass

@MappedSuperclass
@Data
open class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    lateinit var id: UUID

    @CreationTimestamp
    open var createdAt: ZonedDateTime? = null

    @UpdateTimestamp
    open var updatedAt: ZonedDateTime? = null

}