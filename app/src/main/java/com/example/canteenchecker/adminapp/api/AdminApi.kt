package com.example.canteenchecker.adminapp.api

import com.example.canteenchecker.adminapp.core.*

interface AdminApi {

    // Authenticate to Admin API - will return the authentication token with the canteen reference
    //
    suspend fun authenticate(userName: String, password: String): Result<String>

    // Returns the canteen Details with the authentication token
    //
    suspend fun getCanteen(authenticationToken: String): Result<CanteenDetails>

    // Updating the dish of the current Canteen
    //
    suspend fun updateDish(authenticationToken: String, dishName: String, dishPrice: Double) : Result<Unit>
    suspend fun updateWaitingTime(authenticationToken: String, waitingTime : Int) : Result<Unit>

    // Returning the review statistics for the current Canteen
    //
    suspend fun getReviewStatisticsForCanteen(authenticationToken: String): Result<ReviewData>

    // Returning the reviews for the current Canteen
    //
    suspend fun getReviewsForCanteen(authenticationToken: String) : Result<List<ReviewEntry>>

    // Delete the review of the canteen
    //
    suspend fun deleteReview(authenticationToken: String, reviewId: String) : Result<Unit>

    // Updating the canteen Details of the current Canteen
    //
    suspend fun updateCanteenData(authenticationToken: String, updateParameters: CanteenUpdateParameters): Result<Unit>



}