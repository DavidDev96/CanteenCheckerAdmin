package com.example.canteenchecker.adminapp.core

import java.time.LocalDateTime

class Canteen(
    val id: String,
    val name: String,
    val dish: String,
    val dishPrice: Float,
    val averageRating: Float
)

class CanteenDetails(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val website: String,
    val dish: String,
    val dishPrice: Float,
    val location: String,
    val waitingTime: Int
)

class CanteenUpdateParameters(
    val name: String,
    val address: String, // this is location of details
    val website: String,
    val phoneNumber: String
)

class ReviewData(
    val ratingsOne: Int,
    val ratingsTwo: Int,
    val ratingsThree: Int,
    val ratingsFour: Int,
    val ratingsFive: Int
){
    val totalRatings = ratingsOne + ratingsTwo + ratingsThree + ratingsFour + ratingsFive
    val averageRating =
        if (totalRatings == 0) 0F
        else (ratingsOne * 1 + ratingsTwo *2 + ratingsThree*3 + ratingsFour*4 + ratingsFive*5) / totalRatings.toFloat()
}

// New class for a single Review for the respective Canteen
class ReviewEntry(
    val id: String,
    val creationDate: LocalDateTime,
    val creator: String,
    val rating: Float,
    val remark: String
)