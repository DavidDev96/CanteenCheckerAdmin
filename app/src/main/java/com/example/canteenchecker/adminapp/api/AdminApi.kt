package com.example.canteenchecker.adminapp.api

import com.example.canteenchecker.adminapp.core.*

interface AdminApi {
    // --- Canteen Data ---
    // Returns Auth Token -> has to be saved
    suspend fun authenticate(userName: String, password: String): Result<String>
    suspend fun getCanteen(authenticationToken: String): Result<CanteenDetails>
    // No extra class for two parameters
    suspend fun updateDish(authenticationToken: String, dishName: String, dishPrice: Double) : Result<Unit>
    suspend fun updateWaitingTime(authenticationToken: String, waitingTime : Int) : Result<Unit>

    // --- Review Functions ---
    // Since we know which canteen we are
    suspend fun getReviewStatisticsForCanteen(authenticationToken: String): Result<ReviewData>
    // and list of all reviews
    suspend fun getReviewsForCanteen(authenticationToken: String) : Result<List<ReviewEntry>>
    // Delete specified Review
    suspend fun deleteReview(authenticationToken: String, reviewId: String) : Result<Unit>

    // This is the put function, use extra model in lieu of struct
    suspend fun updateCanteenData(authenticationToken: String, updateParameters: CanteenUpdateParameters): Result<Unit>



}