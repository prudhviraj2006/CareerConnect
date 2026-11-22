package com.careerconnect.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
    @Query("SELECT * FROM cached_jobs ORDER BY id DESC")
    fun getAllJobs(): Flow<List<JobEntity>>

    @Query("SELECT * FROM cached_jobs ORDER BY id DESC")
    suspend fun getJobList(): List<JobEntity>

    @Query("SELECT * FROM cached_jobs WHERE id = :jobId")
    suspend fun getJobById(jobId: Int): JobEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobs(jobs: List<JobEntity>)

    @Query("DELETE FROM cached_jobs")
    suspend fun clearAll()
}
