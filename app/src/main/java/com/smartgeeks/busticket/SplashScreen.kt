package com.smartgeeks.busticket

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.smartgeeks.busticket.core.Resource
import com.smartgeeks.busticket.databinding.ActivitySplashScreenBinding
import com.smartgeeks.busticket.presentation.AuthViewModel
import com.smartgeeks.busticket.sync.SyncServiceLocal
import com.smartgeeks.busticket.sync.SyncServiceRemote
import com.smartgeeks.busticket.utils.Constantes
import com.smartgeeks.busticket.utils.UsuarioPreferences
import com.smartgeeks.busticket.utils.Utilities
import dagger.hilt.android.AndroidEntryPoint

private val TAG: String = SplashScreen::class.java.simpleName

@AndroidEntryPoint
class SplashScreen : AppCompatActivity() {

    var splash: Thread? = null
    var preferences: SharedPreferences? = null
    var session: String? = null

    private lateinit var binding: ActivitySplashScreenBinding
    private val authViewModel: AuthViewModel by viewModels()
    private var isLockedDevice : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Filtro de acciones que serán alertadas
        val filter = IntentFilter(Constantes.ACTION_FINISH_LOCAL_SYNC)
        val receiver = ResponseReceiver()
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)

        preferences = getSharedPreferences(UsuarioPreferences.SHARED_PREF_NAME, MODE_PRIVATE)
        session = UsuarioPreferences.getInstance(this).sessionUser

        // Get Message company - (DON'T include on the Thread)
        checkLockedDevice()
        getMessageCompany()

        splash = object : Thread() {
            override fun run() {
                try {
                    //Duracion
                    sleep(3000)
                    goNextScreen()
                } catch (e: Exception) {
                    e.message
                }
            }
        }
        (splash as Thread).start()
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
                    Log.e(TAG, "checkLockedDevice: ${result.data}")
                }
            }
        })
    }

    private fun goNextScreen() {
        if (session == "SessionSuccess" && !isLockedDevice) {
            localSync()
            remoteSync()
        } else if (session == "SessionFailed" || isLockedDevice) {
            intent = Intent(this, Login::class.java)
            intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            finish()
            startActivity(intent)
        }
    }

    private fun getMessageCompany() {
        authViewModel.getMessageCompany(UsuarioPreferences.getInstance(this).idEmpresa)
            .observe(this, { result ->
                when (result) {
                    is Resource.Failure -> {
                        Log.e(TAG, "getMessageCompany: ${result.exception.message}")
                    }
                    is Resource.Loading -> Unit
                    is Resource.Success -> {
                        UsuarioPreferences.getInstance(this).descEmpresa = result.data.data
                    }
                }

            })
    }

    private fun localSync() {
        /**
         * Ejecutar el servicio de Sincronización Local
         */
        val sync = Intent(this, SyncServiceLocal::class.java)
        sync.action = Constantes.ACTION_RUN_LOCAL_SYNC
        startService(sync)
    }

    private fun remoteSync() {
        val sync = Intent(this, SyncServiceRemote::class.java)
        sync.action = Constantes.ACTION_RUN_REMOTE_SYNC
        startService(sync)
    }

    // Broadcast receiver que recibe las emisiones desde los servicios
    private inner class ResponseReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Constantes.ACTION_FINISH_LOCAL_SYNC -> {
                    val next = Intent(context, MainActivity::class.java)
                    next.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(next)
                    Log.e("Splash", "Finalizado guardado de datos")
                }
            }
        }
    }
}