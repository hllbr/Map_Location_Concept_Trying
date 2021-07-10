package com.hllbr.myapplication

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.hllbr.myapplication.databinding.ActivityMapsBinding
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    //Auto System Properties
    private lateinit var mMap: GoogleMap
    private lateinit var locationManager : LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(myListener)

        //--mMap.onLocationChanged
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object : LocationListener{
            override fun onLocationChanged(location: Location) {
                if(location != null){
                    val sharedPreferences = this@MapsActivity.getSharedPreferences("com.hllbr.myapplication",Context.MODE_PRIVATE)
                    val firstTimeCheck =  sharedPreferences.getBoolean("notFirstTime",false)//first time info equals = false maybe second maybe third
                    if(!firstTimeCheck){
                        mMap.clear()
                        val newUserLocation = LatLng(location.latitude,location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newUserLocation,15f))
                        sharedPreferences.edit().putBoolean("notFirstTime",true).apply()

                    }

                }
            }
        }
        //manifest control operation area
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            //if not acces operation
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)

        }else {
            //if we have permissions
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                2,
                2f,
                locationListener
            )

            val intent = intent
            val info = intent.getStringExtra("info")
            if (info.equals("new")) {
                val lastLocation =
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastLocation != null) {
                    val lastLocationLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocationLatLng, 15f))
                }
            } else {
                //info equals old
                mMap.clear()
                val selectedPlace = intent.getSerializableExtra("selectedPlace") as Place
                val selectedlocation = LatLng(selectedPlace.latitude!!, selectedPlace.longitude!!)
                mMap.addMarker(
                    MarkerOptions().title(selectedPlace.address).position(selectedlocation)
                )
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedlocation, 15f))
            }

        }
        }

        val myListener = object : GoogleMap.OnMapLongClickListener{
            override fun onMapLongClick(p0: LatLng) {
                val geocoder = Geocoder(this@MapsActivity, Locale.getDefault())
                var address = ""

                if (p0 != null){
                    try {
                        val addressList = geocoder.getFromLocation(p0.latitude,p0.longitude,1)

                        if (addressList != null && addressList.size > 0 ){
                            if (addressList[0].thoroughfare != null){
                            address += addressList[0].thoroughfare
                                if (addressList[0].subThoroughfare != null){
                                    address += addressList[0].subThoroughfare
                                }
                        }

                    }else{
                        address = "NEW PLACE"
                        }
                }catch (e: Exception){
                    Toast.makeText(this@MapsActivity,"new place error404",Toast.LENGTH_LONG).show()


                    }
                    mMap.clear()
                    mMap.addMarker(MarkerOptions().position(p0).title(address))
                   // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(p0,15f))

                    val newPlace =
                        Place(
                            address,
                            p0.latitude,
                            p0.longitude
                        )//This is a object
                    val dialog = AlertDialog.Builder(this@MapsActivity)
                    dialog.setCancelable(false)//The user has to choose for this build
                    dialog.setTitle("Are u Sure¿")
                    dialog.setMessage(newPlace.address)
                    dialog.setPositiveButton("Yes"){dialog,which ->
                        //SQLite operation ==>
                        try{
                            val database = openOrCreateDatabase("Place",Context.MODE_PRIVATE,null)//no filter
                            database.execSQL("CREATE TABLE IF NOT EXISTS places (address VARCHAR,latitude DOUBLE,longitude DOUBLE)")
                            val toCompile = "INSERT INTO places(address,latitude,longitude) VALUES(?,?,?)"
                            val sqliteStatement = database.compileStatement(toCompile)
                            sqliteStatement.bindString(1,newPlace.address)
                            sqliteStatement.bindDouble(2,newPlace.latitude!!)
                            sqliteStatement.bindDouble(3,newPlace.longitude!!)
                            sqliteStatement.execute()



                        }catch (e : Exception){
                            Toast.makeText(this@MapsActivity,"SQLite error550",Toast.LENGTH_LONG).show()

                        }
                    }.setNegativeButton("No"){dialog,whic->
                        Toast.makeText(this@MapsActivity,"Canceled!",Toast.LENGTH_LONG).show()

                    }
                    dialog.show()
                }
            }

        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1){
            if (grantResults.size>0){
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,2,2f,locationListener)

                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    //will happen when the back button is pressed

    override fun onBackPressed() {
        super.onBackPressed()
        val intentToMain = Intent(this,MainActivity::class.java)
        startActivity(intentToMain)
        finish()
    }

}