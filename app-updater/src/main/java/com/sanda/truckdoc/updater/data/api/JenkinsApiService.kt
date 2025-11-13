package com.sanda.truckdoc.updater.data.api

import com.sanda.truckdoc.updater.data.model.JenkinsBuild
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface JenkinsApiService {
    
    @GET("job/{jobName}/api/json")
    suspend fun getJobInfo(
        @Path("jobName") jobName: String
    ): JobInfo
    
    @GET("job/{jobName}/lastSuccessfulBuild/api/json")
    suspend fun getLastSuccessfulBuild(
        @Path("jobName") jobName: String
    ): JenkinsBuild
    
    @GET("job/{jobName}/lastBuild/api/json")
    suspend fun getLastBuild(
        @Path("jobName") jobName: String
    ): JenkinsBuild
    
    @GET("job/{jobName}/{buildNumber}/api/json")
    suspend fun getBuildInfo(
        @Path("jobName") jobName: String,
        @Path("buildNumber") buildNumber: Int
    ): JenkinsBuild
    
    @GET("job/{jobName}/api/json")
    suspend fun getBuilds(
        @Path("jobName") jobName: String,
        @Query("tree") tree: String = "builds[number,url,timestamp,result,artifacts[fileName,relativePath,size],building]"
    ): BuildsResponse
}

data class JobInfo(
    val name: String,
    val url: String,
    val builds: List<BuildReference>?
)

data class BuildReference(
    val number: Int,
    val url: String
)

data class BuildsResponse(
    val builds: List<JenkinsBuild>?
) 