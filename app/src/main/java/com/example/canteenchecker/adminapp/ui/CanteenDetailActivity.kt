package com.example.canteenchecker.adminapp.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.canteenchecker.adminapp.*
import com.example.canteenchecker.adminapp.api.AdminApiFactory
import com.example.canteenchecker.adminapp.ui.ReviewsFragment.Companion.addReviewsFragment
import kotlinx.coroutines.launch

class CanteenDetailActivity : AppCompatActivity() {
    companion object {
        fun intent(context: Context) =
            Intent(context, CanteenDetailActivity::class.java)
        private var currentOptionsMenu : Menu? = null
    }

    // Create receiver since it is a abstract class
    private val receiver = object: CanteenChangedBroadcastReceiver() {
        override fun onReceiveCanteenChanged(canteenId: String) {
            if(currentCanteenId == canteenId ){
                loadCanteenDetails()
            }
        }
    }

    // not possible in companion object
    private lateinit var authenticationToken : String
    private lateinit var currentCanteenId : String

    // All Elements
    private lateinit var txvName: TextView
    private lateinit var txvLocation: TextView
    private lateinit var txvWebsite: TextView
    private lateinit var txvPhone: TextView
    private lateinit var txvWaitingTime: TextView
    private lateinit var txvDish: TextView
    private lateinit var txvDishPrice: TextView

    // buttons
    //private lateinit var btnToUpdateCanteen : Button
    //private lateinit var btnShowReviews : Button
    private lateinit var btnUpdateDish : Button
    private lateinit var btnUpdateWaitingTime : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_canteen_overview)

        authenticationToken = (application as CanteenCheckerAdminApplication).authenticationToken
        currentCanteenId = ""

        txvName = findViewById(R.id.overviewCanteenName)
        txvLocation = findViewById(R.id.overviewCanteenAddress)
        txvWebsite = findViewById(R.id.overviewCanteenWebsite)
        txvPhone = findViewById(R.id.overviewCanteenPhone)
        txvWaitingTime = findViewById(R.id.overviewCanteenWaitingTime)
        txvDish = findViewById(R.id.overviewCanteenDish)
        txvDishPrice = findViewById(R.id.overviewCanteenDishPrice)

        //btnToUpdateCanteen = findViewById(R.id.goToUpdateCanteenButton)
        //btnToUpdateCanteen.setOnClickListener{ startActivity(UpdateCanteenDetailsActivity.intent(this)) }
        //btnToUpdateCanteen.setOnClickListener{ navigateToUpdateCanteen() }

        //btnShowReviews = findViewById(R.id.showReviews)
        //btnShowReviews.setOnClickListener{ startActivity(ReviewsActivity.intent(this)) }
        //btnShowReviews.setOnClickListener{ navigateToReviews() }

        btnUpdateDish = findViewById(R.id.updateDishButton)
        btnUpdateDish.setOnClickListener{ updateDishDetails() }

        btnUpdateWaitingTime = findViewById(R.id.updateWaitingTimeButton)
        btnUpdateWaitingTime.setOnClickListener{ updateWaitingTime() }

        // Add listener for Firebase
        registerCanteenChangedBroadcastReceiver(receiver)

        verifyToken()
        //loadCanteenDetails()

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterCanteenChangedBroadcastReceiver(receiver)
    }

    override fun onStart() {
        super.onStart()
        if(authenticationToken.isBlank()){
            authenticationToken = (application as CanteenCheckerAdminApplication).authenticationToken
        }
        if(currentOptionsMenu != null && currentCanteenId.isBlank()){
            this.invalidateOptionsMenu()
            onPrepareOptionsMenu(currentOptionsMenu)
        }
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            authenticationToken = (application as CanteenCheckerAdminApplication).authenticationToken
            loadCanteenDetails()
        }
    }

    private fun verifyToken() {
        if(authenticationToken.isBlank()) {
            resultLauncher.launch(AdminLoginActivity.intent(this@CanteenDetailActivity))
        } else {
            loadCanteenDetails()
        }
    }

    private fun loadCanteenDetails() = lifecycleScope.launch{
        if(authenticationToken.isNotBlank()){
            AdminApiFactory.createAdminApiInstance().getCanteen(authenticationToken)
                .onFailure {
                    Toast.makeText(this@CanteenDetailActivity, R.string.message_canteen_not_found, Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
                .onSuccess { currentCanteen ->
                    if(currentCanteenId == ""){
                        currentCanteenId = currentCanteen.id
                        (application as CanteenCheckerAdminApplication).canteenId = currentCanteenId

                        // Reload Options menu
                        if(currentOptionsMenu != null){
                            this@CanteenDetailActivity.invalidateOptionsMenu()
                            onPrepareOptionsMenu(currentOptionsMenu)
                        }

                        supportFragmentManager.beginTransaction().setReorderingAllowed(true)
                            .addReviewsFragment(R.id.fcwReviews, authenticationToken, currentCanteenId).commit()
                    }

                    txvName.text = currentCanteen.name
                    txvLocation.text = currentCanteen.location
                    txvWebsite.text = currentCanteen.website
                    txvPhone.text = currentCanteen.phoneNumber
                    txvWaitingTime.text = currentCanteen.waitingTime.toString()
                    txvDish.text = currentCanteen.dish
                    txvDishPrice.text = currentCanteen.dishPrice.toString()
            }
        }
    }

    private fun updateDishDetails() {
        val view = layoutInflater.inflate(R.layout.dialog_update_dish, null)
        // Set values
        view.findViewById<EditText>(R.id.dishUpdateNameField).setText(txvDish.text.toString())
        view.findViewById<com.google.android.material.slider.RangeSlider>(R.id.rangeSliderUpdateDishPrice).setValues(txvDishPrice.text.toString().toFloat())

        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_update_dish_title)
            .setView(view)
            .setPositiveButton(R.string.dialog_update_dish_positive_button){ dialog, _ ->
                dialog.dismiss()
                lifecycleScope.launch{
                    AdminApiFactory.createAdminApiInstance().updateDish(
                        authenticationToken,
                        view.findViewById<EditText>(R.id.dishUpdateNameField).text.toString(),
                        view.findViewById<com.google.android.material.slider.RangeSlider>(R.id.rangeSliderUpdateDishPrice).values.last().toDouble()
                    )
                        .onFailure {
                            Toast.makeText(this@CanteenDetailActivity, R.string.dish_update_failure, Toast.LENGTH_SHORT).show()
                        }
                        .onSuccess {
                            Toast.makeText(this@CanteenDetailActivity, R.string.dish_update_success, Toast.LENGTH_SHORT).show()
                        }
                }
            }.create().show()
    }

    private fun updateWaitingTime() {
        val view = layoutInflater.inflate(R.layout.dialog_update_waitingtime, null)
        // Set Values
        view.findViewById<com.google.android.material.slider.RangeSlider>(R.id.rangeSliderUpdateWaitingTime).setValues(txvWaitingTime.text.toString().toFloat())
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_update_waiting_time_title)
            .setView(view)
            .setPositiveButton(R.string.dialog_update_waitingtime_positive_button)
            { dialog, _ ->
                dialog.dismiss()
                lifecycleScope.launch{
                    AdminApiFactory.createAdminApiInstance().updateWaitingTime(
                        authenticationToken,
                        view.findViewById<com.google.android.material.slider.RangeSlider>(R.id.rangeSliderUpdateWaitingTime).values.last().toInt()
                    )
                        .onFailure {
                            Toast.makeText(this@CanteenDetailActivity, R.string.waitingtime_update_failed, Toast.LENGTH_SHORT).show()
                        }
                        .onSuccess {
                            Toast.makeText(this@CanteenDetailActivity, R.string.waitingtime_update_sucess, Toast.LENGTH_SHORT).show()
                        }
                }
            }.create().show()
    }

    // Prepare Options Menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        currentOptionsMenu = menu
        menuInflater.inflate(R.menu.menu_activity_admin_canteen_details, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.mniLogout)?.isVisible = authenticationToken.isNotBlank()
        menu?.findItem(R.id.mniOverview)?.isVisible = false // true
        menu?.findItem(R.id.mniDetails)?.isVisible = currentCanteenId.isNotBlank()
        menu?.findItem(R.id.mniReviews)?.isVisible = currentCanteenId.isNotBlank()
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean  =  when(item.itemId){
        R.id.mniLogout -> logoutUser().let { true }
        //R.id.mniOverview -> logoutUser().let { true }
        R.id.mniDetails -> navigateToUpdateCanteen().let { true }
        R.id.mniReviews -> navigateToReviews().let { true }
        else -> super.onOptionsItemSelected(item)
    }

    private fun logoutUser() {
        if(authenticationToken.isNotBlank()){
            (application as CanteenCheckerAdminApplication).authenticationToken = ""
            startActivity(AdminLoginActivity.intent(this))
        }
    }

    private fun navigateToUpdateCanteen(){
        startActivity(UpdateCanteenDetailsActivity.intent(this))
    }

    private fun navigateToReviews(){
        startActivity(ReviewsActivity.intent(this))
    }
}