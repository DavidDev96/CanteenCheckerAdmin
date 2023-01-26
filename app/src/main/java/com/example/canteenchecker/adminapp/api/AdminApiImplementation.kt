package com.example.canteenchecker.adminapp.api

import android.util.Log
import com.example.canteenchecker.adminapp.core.CanteenDetails
import com.example.canteenchecker.adminapp.core.CanteenUpdateParameters
import com.example.canteenchecker.adminapp.core.ReviewData
import com.example.canteenchecker.adminapp.core.ReviewEntry
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import retrofit2.http.*
import java.io.IOException
import java.time.LocalDateTime
import java.util.*

// Functions to convert API results to Kotlin Objects
private inline fun <T, R> Result<List<T>>.convertEach(map: T.() -> R): Result<List<R>> =
    this.map { it.map(map) }

private inline fun <T, R> Result<T>.convert(map: T.() -> R): Result<R> = this.map(map)

// Create Factory for Interaction with the Swagger UI API
/**
 * This returns an Instance of our API Interface with its Implementation
 * to interact with the swagger API, following its conventions
**/
object AdminApiFactory{
    fun createAdminApiInstance() : AdminApi =
        AdminApiImplementation("https://moc5.projekte.fh-hagenberg.at/CanteenChecker/api/admin/");
}

private class AdminApiImplementation(apiBaseUrl: String) : AdminApi{
    // Don't know hot but same as in the course
    private val retrofit =
        Retrofit.Builder().baseUrl(apiBaseUrl).addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create()).build()

    // Create Internal Interface that converts to the respective paths required by swagger
    private interface Api {
        @POST("authenticate")
        suspend fun postAuthenticate(
            @Query("userName") userName: String, @Query("password") password: String
        ): String

        @GET("canteen")
        suspend fun getCanteen(@Header("Authorization") authenticationToken: String): ApiCanteenDetails

        @GET("canteen/review-statistics")
        suspend fun getReviewStatisticsForCanteen(@Header("Authorization") authenticationToken: String): ApiCanteenReviewStatistics

        @PUT("canteen/data")
        suspend fun updateDataForCanteen(@Header("Authorization")  authenticationToken: String,
                                         @Query("name") name : String,
                                         @Query("address") address: String,
                                         @Query("website") website: String,
                                         @Query("phoneNumber") phoneNumber: String) : Response<Unit>

        @PUT("canteen/dish")
        suspend fun updateDataCanteenDish(@Header("Authorization")  authenticationToken: String,
                                          @Query("dish") dish : String,
                                          @Query("dishPrice") dishPrice: Double) : Response<Unit>

        @PUT("canteen/waiting-time")
        suspend fun updateWaitingTimeCanteen(@Header("Authorization")  authenticationToken: String,
                                             @Query("waitingTime") waitingTime: Int) : Response<Unit>

        @GET("canteen/reviews")
        suspend fun getReviewsForCanteen(@Header("Authorization")  authenticationToken: String) : List<ApiReviewDetail>

        @DELETE("canteen/reviews/{reviewId}")
        suspend fun deleteReviewForCanteen(@Header("Authorization")  authenticationToken: String,
                                           @Path("reviewId") reviewId : String) : Response<Unit>
    }

    private class ApiCanteenData(
        val id: String,
        val name: String,
        val dish: String,
        val dishPrice: Float,
        val averageRating: Float
    )

    private class ApiCanteenDetails(
        val id: String,
        val name: String,
        val address: String,
        val phoneNumber: String,
        val website: String,
        val dish: String,
        val dishPrice: Float,
        val waitingTime: Int
    )

    private class ApiCanteenReviewStatistics(
        val countOneStar: Int,
        val countTwoStars: Int,
        val countThreeStars: Int,
        val countFourStars: Int,
        val countFiveStars: Int
    )

    private class ApiReviewDetail(
        val id: String,
        val creationDate: String,
        val creator: String,
        val rating: Float,
        val remark: String
    )

    private inline fun <T> apiCall(call: Api.() -> T): Result<T> = try {
        Result.success(call(retrofit.create()))
    } catch (ex: HttpException) {
        Result.failure(ex)
    } catch (ex: IOException) {
        Result.failure(ex)
    }.onFailure {
        Log.e(TAG, "API call failed", it)
    }

    companion object {
        private val TAG = this::class.simpleName
    }

    /**
     * Authenticate User for Canteen -> this returns the Auth String -> must be saved
     * globally in the App Class
     * params: userName : String, password : String
     * result: authToken : String
     * **/
    override suspend fun authenticate(userName: String, password: String): Result<String> =
        apiCall {
            postAuthenticate(userName, password)
        }

    override suspend fun getCanteen(authenticationToken: String): Result<CanteenDetails> = apiCall {
        getCanteen("Bearer $authenticationToken")
    }.convert { CanteenDetails(id, name, phoneNumber, website, dish, dishPrice, address, waitingTime) }

    override suspend fun updateCanteenData(
        authenticationToken: String,
        updateParameters: CanteenUpdateParameters
    ): Result<Unit> = apiCall{
        updateDataForCanteen("Bearer $authenticationToken", updateParameters.name, updateParameters.address, updateParameters.website, updateParameters.phoneNumber)
    }.convert { }

    override suspend fun updateDish(
        authenticationToken: String,
        dishName: String,
        dishPrice: Double
    ): Result<Unit> = apiCall{
        updateDataCanteenDish("Bearer $authenticationToken", dishName, dishPrice)
    }.convert{ }

    override suspend fun updateWaitingTime(
        authenticationToken: String,
        waitingTime: Int
    ): Result<Unit> = apiCall{
        updateWaitingTimeCanteen("Bearer $authenticationToken", waitingTime)
    }.convert {  }

    override suspend fun getReviewStatisticsForCanteen(authenticationToken: String): Result<ReviewData> = apiCall {
        getReviewStatisticsForCanteen("Bearer $authenticationToken")
    }.convert { ReviewData(countOneStar, countTwoStars, countThreeStars, countFourStars, countFiveStars) }

    override suspend fun getReviewsForCanteen(authenticationToken: String): Result<List<ReviewEntry>> = apiCall {
        getReviewsForCanteen("Bearer $authenticationToken")
    }.convertEach { ReviewEntry(id, LocalDateTime.parse(creationDate), creator, rating, remark )  }

    override suspend fun deleteReview(authenticationToken: String, reviewId: String): Result<Unit> = apiCall {
        deleteReviewForCanteen("Bearer $authenticationToken", reviewId)
    }.convert{}

}

