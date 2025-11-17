package com.sanda.truckdoc.updater.data.api

import com.sanda.truckdoc.updater.data.model.GitHubRelease
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * GitHub API service for fetching releases
 */
interface GitHubApiService {
    
    /**
     * Get the latest release for a repository
     * 
     * @param owner Repository owner
     * @param repo Repository name
     * @return The latest non-draft, non-prerelease release
     */
    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): GitHubRelease
    
    /**
     * Get all releases for a repository
     * 
     * @param owner Repository owner
     * @param repo Repository name
     * @return List of all releases (including drafts and prereleases)
     */
    @GET("repos/{owner}/{repo}/releases")
    suspend fun getAllReleases(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): List<GitHubRelease>
}

