package com.smartgeeks.busticket

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.edit
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.smartgeeks.busticket.api.Service
import com.smartgeeks.busticket.core.Resource
import com.smartgeeks.busticket.core.Resource.Loading
import com.smartgeeks.busticket.databinding.ActivityLoginBinding
import com.smartgeeks.busticket.presentation.AuthViewModel
import com.smartgeeks.busticket.sync.SyncServiceLocal
import com.smartgeeks.busticket.utils.Constantes
import com.smartgeeks.busticket.utils.DialogAlert
import com.smartgeeks.busticket.utils.RutaPreferences
import com.smartgeeks.busticket.utils.UsuarioPreferences
import com.smartgeeks.busticket.utils.Utilities
import dagger.hilt.android.AndroidEntryPoint

/**
 * user: PRUEBA
 * password: prueba321
 */
private var TAG: String = Login::class.java.simpleName
private const val REQUEST_CODE_LOCATION = 0

@AndroidEntryPoint
class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    var dialogAlert = DialogAlert()
    private val authViewModel: AuthViewModel by viewModels()
    private var isLockedDevice: Boolean = false

    // Permission variable
    private var googleApiClient: GoogleApiClient? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val REQUESTLOCATION = 199

    private var userLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()

        // Filtro de acciones que serán alertadas
        val filter = IntentFilter(Constantes.ACTION_FINISH_LOCAL_SYNC)
        val receiver = ResponseReceiver()
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)

        binding.btnIniciarSession.setOnClickListener {
            val email = binding.edUsuario.text.toString().trim { it <= ' ' }
            val password = binding.edPassword.text.toString().trim { it <= ' ' }

            if (!DialogAlert.verificaConexion(this)) {
                dialogAlert.showDialogErrorConexion(this)
            } else {
                if (email.isEmpty() || password.isEmpty()) {
                    DialogAlert.showDialogFailed(
                        this,
                        "Alerta",
                        "Rellene los campos",
                        SweetAlertDialog.WARNING_TYPE
                    )
                } else {

                    if (isLockedDevice) {
                        showDialogLockedDevice()
                        return@setOnClickListener
                    }

                    if (userLocation == null) {
                        showDialogLocation()
                        return@setOnClickListener
                    } else {
                        showProgress(true)
                        binding.btnIniciarSession.visibility = View.GONE
                        signIn(email, password)
                    }
                }
            }
        }

        checkLockedDevice()
    }

    private fun signIn(email: String, password: String) {
        authViewModel.userLogin(email, password).observe(this, { result ->
            when (result) {
                is Loading -> {
                    Log.e(TAG, "example: Cargando datos")
                }
                is Resource.Failure -> {
                    Log.d(Service.TAG, "response: ${result.exception}")
                    dialogAlert.showDialogErrorConexion(this)
                    showProgress(false)
                    binding.btnIniciarSession.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    with(result.data) {

                        if (error) {
                            if (user.idRol == 2 || user.idRol == 3) {
                                UsuarioPreferences.getInstance(this@Login).userPreferences(user)
                                sendLoginLogs(user.idUsuario)
                                setDataPreferences()
                                dialogSelectRoleUser()
                            } else {
                                DialogAlert.showDialogFailed(
                                    this@Login,
                                    "Error",
                                    "No tiene permiso",
                                    SweetAlertDialog.ERROR_TYPE
                                )
                                showProgress(false)
                                binding.btnIniciarSession.visibility = View.VISIBLE
                            }
                        } else if (!error) {
                            if (message == "Password Incorrecta") {
                                DialogAlert.showDialogFailed(
                                    this@Login,
                                    "Error",
                                    "Contraseña incorrecta",
                                    SweetAlertDialog.ERROR_TYPE
                                )
                                showProgress(false)
                                binding.btnIniciarSession.visibility = View.VISIBLE
                            } else if (message == "Usuario no existe") {
                                DialogAlert.showDialogFailed(
                                    this@Login,
                                    "Error",
                                    "Usuario no Existe",
                                    SweetAlertDialog.ERROR_TYPE
                                )
                                showProgress(false)
                                binding.btnIniciarSession.visibility = View.VISIBLE
                            }
                        }

                    }
                }
            }
        })
    }

    private fun sendLoginLogs(userID: Int) {
        val deviceID = Utilities.getDeviceId(this)
        val location = "${userLocation?.latitude},${userLocation?.longitude}"
        authViewModel.setLoginLogs(userID, deviceID, location).observe(this, { result ->
            when (result) {
                is Resource.Failure -> {
                    Toast.makeText(this, result.exception.message, Toast.LENGTH_SHORT).show()
                }
                is Loading -> {
                    Log.d(TAG, "sendingLoginLogs")
                }
                is Resource.Success -> {
                    Log.d(TAG, "Logs have been sent")
                }
            }
        })
    }

    private fun checkLockedDevice() {
        authViewModel.checkLockedDevice(
            UsuarioPreferences.getInstance(this).idUser,
            Utilities.getDeviceId(this)
        ).observe(this, { result ->
            when (result) {
                is Resource.Failure -> {
                    Toast.makeText(this, "${result.exception.message}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Loading -> Unit
                is Resource.Success -> {
                    isLockedDevice = result.data
                    if (result.data) {
                        showDialogLockedDevice()
                    }
                }
            }
        })
    }

    private fun showDialogLockedDevice() {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Dispositivo Deshabilitado")
            .setContentText("Contacte con el administrador para habilitar el equipo.")
            .show()
    }

    private fun showDialogLocation() {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Permiso de ubicación")
            .setContentText("Debes habilitar el permiso de ubicación.")
            .setConfirmClickListener {
                getLastLocation()
                it.dismiss()
            }
            .show()
    }

    private fun dialogSelectRoleUser() {
        val alertDialog = SweetAlertDialog(
            this,
            SweetAlertDialog.NORMAL_TYPE
        )
        alertDialog.setTitleText("Selecciona el modo de venta")
            .setConfirmText("Conductor")
            .setCancelText("Boletería")
            .setConfirmClickListener { sDialog ->
                sDialog.dismissWithAnimation()
                localSync()
                RutaPreferences.getInstance(this).estadoRuta = false
                UsuarioPreferences.getInstance(this).roleVenta = "conductor"
            }
            .setCancelClickListener { sDialog ->
                sDialog.cancel()
                UsuarioPreferences.getInstance(this).roleVenta = "operador"
                localSync()
            }
            .show()
    }

    private fun goMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        overridePendingTransition(R.anim.left_in, R.anim.left_out)
    }

    private fun localSync() {
        /**
         * Ejecutar el servicio de Sincronización Local
         */
        val sync = Intent(this, SyncServiceLocal::class.java)
        sync.action = Constantes.ACTION_RUN_LOCAL_SYNC
        startService(sync)
    }

    private fun showProgress(show: Boolean) {
        binding.loginProgress.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun setDataPreferences() {
        val sharPref = getSharedPreferences(UsuarioPreferences.SHARED_PREF_NAME, MODE_PRIVATE)
        sharPref.edit {
            putString(UsuarioPreferences.KEY_SESSION, "SessionSuccess")
            apply()
        }
    }

    // Broadcast receiver que recibe las emisiones desde los servicios
    private inner class ResponseReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Constantes.ACTION_FINISH_LOCAL_SYNC -> {
                    Log.e("Login", "Finalizado guardado de datos")
                    goMainActivity()
                }
            }
        }
    }

    // Location
    fun checkPermission(): Boolean = (ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_CODE_LOCATION
        )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager =
            this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun getLastLocation() {
        if (checkPermission()) {
            if (isLocationEnabled()) {
                fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
                    val location: Location? = try {
                        task.result
                    } catch (e: Exception) {
                        Toast.makeText(
                            this,
                            "LocationServices.API is not available on this device.",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e(TAG, "getLastLocation: ${e.localizedMessage}")
                        null
                    }

                    if (location == null) {
                        newLocationData()
                    } else {
                        Log.e(
                            "Debug",
                            "You Current Location is : Long: " + location.longitude + " , Lat: " + location.latitude + "\n"
                        )
                        userLocation = location
                    }
                }
            } else {
                enableLoc()
                Toast.makeText(
                    this,
                    "Please Turn on Your device Location",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            requestPermission()
        }
    }

    private fun enableLoc() {

        googleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                override fun onConnected(bundle: Bundle?) {}
                override fun onConnectionSuspended(i: Int) {
                    googleApiClient?.connect()
                }
            })
            .addOnConnectionFailedListener {
            }.build()

        googleApiClient?.connect()
        val locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 30 * 1000.toLong()
        locationRequest.fastestInterval = 5 * 1000.toLong()

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        val result =
            LocationServices.SettingsApi.checkLocationSettings(googleApiClient!!, builder.build())

        result.setResultCallback { result ->
            val status: Status = result.status

            Log.e(TAG, "enableLoc: $status")
            when (status.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                    status.startResolutionForResult(
                        this,
                        REQUESTLOCATION
                    )
                } catch (e: IntentSender.SendIntentException) {
                    Log.e(TAG, "enableLoc: ${e.message}")
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun newLocationData() {
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 0
        locationRequest.fastestInterval = 0
        locationRequest.numUpdates = 1
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)
        Looper.myLooper()?.let {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback, it
            )
        }
    }

    private val locationCallback = object : LocationCallback() {
        @SuppressLint("SetTextI18n")
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation: Location = locationResult.lastLocation
            Log.e(
                TAG,
                "You Last Location is : Long: " + lastLocation.longitude + " , Lat: " + lastLocation.latitude + "\n"
            )
            userLocation = lastLocation
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e(TAG, "onActivityResult: $resultCode - $requestCode")
        when (requestCode) {
            REQUESTLOCATION -> when (resultCode) {
                Activity.RESULT_OK -> Log.e(TAG, "OK Granted")
                Activity.RESULT_CANCELED -> Log.e(TAG, "CANCEL")
                else -> {
                    Log.e(TAG, "onActivityResult: $resultCode")
                }
            }
        }
    }
}