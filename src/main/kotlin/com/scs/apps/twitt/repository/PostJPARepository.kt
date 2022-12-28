package com.scs.apps.twitt.repository

import com.scs.apps.twitt.annotation.PostgresRepository
import com.scs.apps.twitt.entity.Post
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@PostgresRepository
@Repository
interface PostJPARepository : JpaRepository<Post, UUID> {
}