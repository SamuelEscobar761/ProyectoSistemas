package com.example.newsistemasproyecto

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsistemasproyecto.Mapping.LocationMap
import com.example.newsistemasproyecto.Mapping.showShortMessage
import com.example.newsistemasproyecto.databinding.ActivityHomeNavigationBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Home_navigation : AppCompatActivity() {
    lateinit var recyclerViewUsersLocation: RecyclerView
    private lateinit var bindingNavigation: ActivityHomeNavigationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var databaseLocationReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var email: String
    private lateinit var userName: String
    private lateinit var userFromEmail: String
    private lateinit var listLocations: MutableList<LocationMap>
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var map: GoogleMap
    private val TIEMPO: Long = 5000
    private var weCanGetCurrentLocation = true
    var handler: Handler = Handler()
    var userLocation: LatLng? = null

    companion object {
        const val REQUEST_CODE_LOCATION = 1010
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingNavigation = ActivityHomeNavigationBinding.inflate(layoutInflater)
        setContentView(bindingNavigation.root)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database.reference.child("Users")
        databaseLocationReference = database.reference.child("Location")

        val switchLocalizacion = bindingNavigation.switchLocalizacion
        val mapButton = bindingNavigation.verEnMapaButton

        getLocationsData()
        getDatabaseData()
        getLastLocation()
        getCurrentLocation()

        switchLocalizacion.setOnClickListener {
            weCanGetCurrentLocation = switchLocalizacion.isChecked()
        }

        mapButton.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com.bo/maps/@" + userLocation?.latitude + "," + userLocation?.longitude + ",19z?hl=es")
            )
            startActivity(intent)
        }
    }

    private fun getDatabaseData() {
        val user = auth.currentUser!!.uid
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        databaseReference.child(user).get().addOnSuccessListener {
            email = it.child("email").value.toString()
            userName = it.child("userName").value.toString()
            showShortMessage(baseContext, email)
        }

        handler.postDelayed(object : Runnable {
            override fun run() {
                // función a ejecutar
                getUserFromEmail(email)
            }
        }, TIEMPO)

    }

    private fun getCurrentLocation() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (weCanGetCurrentLocation) {
                    // función a ejecutar
                    getLastLocation()
                    handler.postDelayed(this, TIEMPO)
                } else {
                    showShortMessage(baseContext, "Localizacion Bloqueada")
                }
            }
        }, TIEMPO)
    }

    private fun getLocationsData() {
        listLocations = mutableListOf()
        databaseReference = FirebaseDatabase.getInstance().reference.child("location")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (loc in snapshot.children) {
                        val location = LocationMap(
                            loc.getValue(LocationMap::class.java)!!.latitude,
                            loc.getValue(LocationMap::class.java)!!.longitud,
                            loc.getValue(LocationMap::class.java)!!.nombre,
                            loc.getValue(LocationMap::class.java)!!.email
                        )
                        listLocations.add(location)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getUserFromEmail(email: String) {
        var str = ""
        for (i in email.toCharArray()) {
            if (i != '@' && i != '.' && i != '#' && i != '$' && i != '[' && i != ']') {
                str += i
            } else if (i == '@') {
                break
            }
        }
        showShortMessage(baseContext, str)
        userFromEmail = str
    }

    private fun checkPermission() = ActivityCompat.checkSelfPermission(
        this,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun saveLocationAtDataBase() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                // función a ejecutar
                val location = LocationMap(userLocation?.latitude, userLocation?.longitude, userName, email)
                databaseLocationReference.child(userFromEmail).setValue(location)
            }
        }, TIEMPO)

    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation: Location = locationResult.lastLocation
            userLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
            saveLocationAtDataBase()
        }
    }

    private fun requestPermissionLocation() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) ||
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            Toast.makeText(this, "Activa los permisos en ajustes.", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_CODE_LOCATION
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun getNewLocation() {
        locationRequest = LocationRequest()
        with(locationRequest) {
            priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 0
            fastestInterval = 0
            numUpdates = 1
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.myLooper()
        )
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation() {
        if (checkPermission()) {
            if (isLocationEnabled()) {
                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        getNewLocation()
                    } else {
                        userLocation = LatLng(location.latitude, location.longitude)
                        saveLocationAtDataBase()
                    }
                }
            } else {
                Toast.makeText(this, "La localizacion esta desactivada!!!", Toast.LENGTH_SHORT)
                    .show()
            }
        } else {
            requestPermissionLocation()
        }
    }
}