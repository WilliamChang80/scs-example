package com.scs.apps.twitt.entity

import lombok.Data
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.elasticsearch.annotations.DateFormat
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
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
    open var id: UUID? = null

    @CreationTimestamp
    @Field(type = FieldType.Date, format = [DateFormat.basic_date_time])
    open var createdAt: ZonedDateTime? = null

    @UpdateTimestamp
    @Field(type = FieldType.Date, format = [DateFormat.basic_date_time])
    open var updatedAt: ZonedDateTime? = null

}