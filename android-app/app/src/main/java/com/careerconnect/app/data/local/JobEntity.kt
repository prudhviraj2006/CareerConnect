package com.careerconnect.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_jobs")
data class JobEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val companyName: String,
    val companyLogo: String?,
    val location: String?,
    val employmentType: String?,
    val experienceLevel: String?,
    val salary: String?,
    val publicationDate: String?,
    val originalUrl: String,
    val isRemote: Boolean,
    val sourceName: String?,
    val requiredSkills: String, // comma-separated
    val matchScore: Float?,
    val description: String?
)
