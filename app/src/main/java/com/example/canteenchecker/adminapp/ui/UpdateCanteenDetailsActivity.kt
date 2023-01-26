package com.example.canteenchecker.adminapp.ui

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.canteenchecker.adminapp.*
import com.example.canteenchecker.adminapp.api.AdminApiFactory
import com.example.canteenchecker.adminapp.core.CanteenUpdateParameters
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

class UpdateCanteenDetailsActivity : AppCompatActivity() {
    companion object {
        private const val DEFAULT_ZOOM_FACTOR = 15f
        fun intent(context: Context) =
            Intent(context, UpdateCanteenDetailsActivity::class.java)
    }

    private val receiver = object: CanteenChangedBroadcastReceiver() {
        override fun onReceiveCanteenChanged(canteenId: String) {
            if(currentCanteenId == canteenId ){
                loadCanteenDetails()
            }
        }
    }

    private lateinit var authenticationToken : String
    private lateinit var currentCanteenId : String

    private lateinit var edtName: EditText
    private lateinit var edtLocation: EditText
    private lateinit var edtPhone: EditText
    private lateinit var edtWebsite: EditText

    private lateinit var btnUpdateCanteen : Button

    private lateinit var mapFragment: SupportMapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_canteen_details)

        authenticationToken = (application as CanteenCheckerAdminApplication).authenticationToken
        currentCanteenId = (application as CanteenCheckerAdminApplication).canteenId
        edtName = findViewById(R.id.editCanteenUpdateName)
        edtLocation = findViewById(R.id.editCanteenAddress)
        edtPhone = findViewById(R.id.editCanteenPhoneNumber)
        edtWebsite = findViewById(R.id.editCanteenWebsite)
        btnUpdateCanteen = findViewById(R.id.btnUpdateCanteen)
        btnUpdateCanteen.setOnClickListener{ updateCanteenDetails() }

        registerCanteenChangedBroadcastReceiver(receiver)

        mapFragment = supportFragmentManager.findFragmentByTag(getString(R.string.tag_map_fragment)) as SupportMapFragment
        mapFragment.getMapAsync{
            val apply = it.uiSettings.apply {
                setAllGesturesEnabled(true)
                isZoomControlsEnabled = true
            }
            it.setOnMarkerDragListener(object: GoogleMap.OnMarkerDragListener {
                override fun onMarkerDragStart(p0: Marker) {}

                override fun onMarkerDrag(p0: Marker) {}

                override fun onMarkerDragEnd(currentMarker: Marker) {
                    lifecycleScope.launch {
                        var newPosition = Geocoder(this@UpdateCanteenDetailsActivity)
                            .getFromLocation(currentMarker.position.latitude, currentMarker.position.longitude, 1) //, geocodeListener);
                            ?.get(0)?.getAddressLine(0)?.toString()

                        edtLocation.setText(newPosition)
                    }
                }
            })
        }
        loadCanteenDetails()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterCanteenChangedBroadcastReceiver(receiver)
    }

    private fun loadCanteenDetails() = lifecycleScope.launch{
        if(authenticationToken.isNotBlank()){
            AdminApiFactory.createAdminApiInstance().getCanteen(authenticationToken)
                .onFailure {
                    Toast.makeText(this@UpdateCanteenDetailsActivity, R.string.message_canteen_not_found, Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
                .onSuccess { currentCanteen ->
                    if(currentCanteenId == ""){
                        currentCanteenId = currentCanteen.id
                        (application as CanteenCheckerAdminApplication).canteenId = currentCanteenId
                    }

                    edtName.setText(currentCanteen.name)
                    edtLocation.setText(currentCanteen.location)
                    edtPhone.setText(currentCanteen.phoneNumber)
                    edtWebsite.setText(currentCanteen.website)

                    lifecycleScope.launch{
                        val address = Geocoder(this@UpdateCanteenDetailsActivity)
                            .getFromLocationName(currentCanteen.location, 1)
                            ?.firstOrNull()?.run{ LatLng(latitude, longitude) }

                        mapFragment.getMapAsync{ map ->
                            map.apply {
                                clear()
                                //show address
                                if(address != null){
                                    addMarker(MarkerOptions().position(address).draggable(true))
                                    animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(address,
                                            UpdateCanteenDetailsActivity.DEFAULT_ZOOM_FACTOR
                                        ))
                                } else {
                                    animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(0.0,0.0), 0f))
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun updateCanteenDetails() = lifecycleScope.launch{
        var canteenToUpdate = CanteenUpdateParameters(
            edtName.text.toString(),
            edtLocation.text.toString(),
            edtWebsite.text.toString(),
            edtPhone.text.toString()
        )

        if(authenticationToken.isNotBlank()) {
            AdminApiFactory.createAdminApiInstance()
                .updateCanteenData(authenticationToken, canteenToUpdate)
                .onFailure {
                    Toast.makeText(
                        this@UpdateCanteenDetailsActivity,
                        R.string.message_update_canteen_failure,
                        Toast.LENGTH_LONG
                    ).show()
                }
                .onSuccess {
                    Toast.makeText(
                        this@UpdateCanteenDetailsActivity,
                        R.string.message_update_canteen_success,
                        Toast.LENGTH_LONG
                    ).show()
                    //loadCanteenDetails()
                    finish()
                }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_activity_admin_canteen_details, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.mniLogout)?.isVisible = true
        menu?.findItem(R.id.mniOverview)?.isVisible = true
        menu?.findItem(R.id.mniDetails)?.isVisible = false
        menu?.findItem(R.id.mniReviews)?.isVisible = false
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean  =  when(item.itemId){
        R.id.mniLogout -> logoutUser().let { true }
        R.id.mniOverview -> navigateToOverview().let { true }
        else -> super.onOptionsItemSelected(item)
    }

    private fun logoutUser() {
        if(authenticationToken.isNotBlank()){
            (application as CanteenCheckerAdminApplication).authenticationToken = ""
            startActivity(LoginActivity.intent(this))
        }
    }

    private fun navigateToOverview(){
        startActivity(CanteenDetailActivity.intent(this))
    }
}