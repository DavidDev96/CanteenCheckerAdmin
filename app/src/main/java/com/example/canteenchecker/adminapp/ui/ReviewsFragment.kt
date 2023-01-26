package com.example.canteenchecker.adminapp.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.example.canteenchecker.adminapp.R
import com.example.canteenchecker.adminapp.*
import com.example.canteenchecker.adminapp.api.AdminApiFactory
import kotlinx.coroutines.launch
import java.text.NumberFormat


// ReviewsFragement - copied from the labour project
class ReviewsFragment: Fragment(R.layout.fragment_reviews) {

    companion object {
        private const val CANTEEN_ID = "CanteenId"
        private const val AUTHENTICATION_TOKEN = "AuthenticationToken"

        fun FragmentTransaction.addReviewsFragment(@IdRes containerViewId: Int, authToken : String,
                                                   canteenId: String) : FragmentTransaction {
            val bundle = Bundle()
            bundle.putString(AUTHENTICATION_TOKEN, authToken)
            bundle.putString(CANTEEN_ID, canteenId)
            return add(containerViewId, ReviewsFragment::class.java, bundle)
        }
    }

    private val receiver = object: CanteenChangedBroadcastReceiver() {
        override fun onReceiveCanteenChanged(canteenId: String) {
            if(canteenId == this@ReviewsFragment.canteenId){
                updateReviews()
            }
        }
    }

    private lateinit var txvAverageRating :TextView
    private lateinit var rtnBarAverageRating :RatingBar
    private lateinit var txvTotalRatings : TextView
    private lateinit var prbRatingOne: ProgressBar
    private lateinit var prbRatingTwo: ProgressBar
    private lateinit var prbRatingThree: ProgressBar
    private lateinit var prbRatingFour: ProgressBar
    private lateinit var prbRatingFive: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.run {
            txvAverageRating = findViewById(R.id.txvAverageRating)
            rtnBarAverageRating = findViewById(R.id.rtbAverageRating)
            txvTotalRatings = findViewById(R.id.txvTotalRatings)
            prbRatingOne = findViewById(R.id.prbRatingsOne)
            prbRatingTwo = findViewById(R.id.prbRatingsTwo)
            prbRatingThree = findViewById(R.id.prbRatingsThree)
            prbRatingFour = findViewById(R.id.prbRatingsFour)
            prbRatingFive = findViewById(R.id.prbRatingsFive)
        }

        requireContext().registerCanteenChangedBroadcastReceiver(receiver)
        updateReviews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireContext().unregisterCanteenChangedBroadcastReceiver(receiver)
    }

    private val authenticationToken get() = arguments?.getString(AUTHENTICATION_TOKEN) ?: ""
    private val canteenId get() = arguments?.getString(CANTEEN_ID) ?: ""

    private fun updateReviews() = lifecycleScope.launch {
        AdminApiFactory.createAdminApiInstance().getReviewStatisticsForCanteen(authenticationToken)
            .onFailure {
                txvAverageRating.text = null
                rtnBarAverageRating.rating = 0f
                txvTotalRatings.text = null
                prbRatingOne.progress = 0
                prbRatingOne.max = 1
                prbRatingTwo.progress = 0
                prbRatingTwo.max = 1
                prbRatingThree.progress = 0
                prbRatingThree.max = 1
                prbRatingFour.progress = 0
                prbRatingFour.max = 1
                prbRatingFive.progress = 0
                prbRatingFive.max = 1
            }
            .onSuccess { rating ->
                txvAverageRating.text = NumberFormat.getNumberInstance().format(rating.averageRating)
                rtnBarAverageRating.rating = rating.averageRating
                txvTotalRatings.text = NumberFormat.getNumberInstance().format(rating.totalRatings)
                prbRatingOne.progress = rating.ratingsOne
                prbRatingOne.max = rating.totalRatings
                prbRatingTwo.progress = rating.ratingsTwo
                prbRatingTwo.max = rating.totalRatings
                prbRatingThree.progress = rating.ratingsThree
                prbRatingThree.max = rating.totalRatings
                prbRatingFour.progress = rating.ratingsFour
                prbRatingFour.max = rating.totalRatings
                prbRatingFive.progress = rating.ratingsFive
                prbRatingFive.max = rating.totalRatings
            }
    }
}