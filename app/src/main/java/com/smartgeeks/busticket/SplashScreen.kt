package com.smartgeeks.busticket

import android.content.*
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewSwitcher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.smartgeeks.busticket.core.AppPreferences
import com.smartgeeks.busticket.core.Resource
import com.smartgeeks.busticket.databinding.ActivitySplashScreenBinding
import com.smartgeeks.busticket.presentation.AuthViewModel
import com.smartgeeks.busticket.presentation.TicketViewModel
import com.smartgeeks.busticket.sync.SyncServiceLocal
import com.smartgeeks.busticket.utils.Constantes
import com.smartgeeks.busticket.utils.UsuarioPreferences
import com.smartgeeks.busticket.utils.Utilities
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.UnknownHostException

private val TAG: String = SplashScreen::class.java.simpleName

@AndroidEntryPoint
class SplashScreen : AppCompatActivity() {

    private var syncedData: Boolean = false
    var preferences: SharedPreferences? = null
    var session: String? = null

    private lateinit var binding: ActivitySplashScreenBinding
    private val authViewModel: AuthViewModel by viewModels()
    private val ticketViewModel: TicketViewModel by viewModels()

    private var timer: CountDownTimer? = null
    private lateinit var textView: TextView
    private val scope = CoroutineScope(Job() + Dispatchers.IO)
    private var syncJob: Job? = null
    private val receiver = ResponseReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()

        // Filtro de acciones que serán alertadas
        val filter = IntentFilter(Constantes.ACTION_FINISH_LOCAL_SYNC)
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)

        preferences = getSharedPreferences(UsuarioPreferences.SHARED_PREF_NAME, MODE_PRIVATE)
        session = UsuarioPreferences.getInstance(this).sessionUser

        checkLockedDevice()
        // Get Message company - (DON'T include on the Thread)
        getMessageCompany()
    }

    private fun initViews() = with(binding) {
        tvInformation.setFactory(ViewSwitcher.ViewFactory {
            textView = TextView(this@SplashScreen)
            textView.setTextColor(Color.WHITE)
            textView.textSize = 20F
            textView.gravity = Gravity.CENTER_HORIZONTAL
            textView
        })
        tvInformation.setText("Cargando Datos...")
    }

    private fun checkLockedDevice() {
        authViewModel.checkLockedDevice(
            UsuarioPreferences.getInstance(this).idUser,
            Utilities.getDeviceId(this)
        ).observe(this) { result ->
            when (result) {
                is Resource.Failure -> {
                    goNextScreen()

                    when (result.exception) {
                        is UnknownHostException -> Toast.makeText(
                            this,
                            "Sin conexión al servidor",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Log.e(TAG, "checkLockedDevice: ${result.exception}")
                    binding.progresBar.visibility = View.GONE

                }
                is Resource.Loading -> binding.progresBar.visibility = View.VISIBLE
                is Resource.Success -> {
                    goNextScreen()
                    Log.e(TAG, "checkLockedDevice: ${result.data}")
                }
            }
        }
    }

    private fun goNextScreen() {
        if (session == "SessionSuccess" && !AppPreferences.isLockedDevice) {

            remoteSync()
            // Execute JOB on coroutines
            syncJob = scope.launch(Dispatchers.Main) {
                setInformationLoading()
            }

            scope.launch {
                // Manage in a service
                localSync()
            }
        } else if (session == "SessionFailed" || AppPreferences.isLockedDevice) {
            intent = Intent(this, Login::class.java)
            intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            finish()
            startActivity(intent)
        }
    }

    private fun setInformationLoading() {
        val messages = listOf(
            "Validando usuario...",
            "Cargando rutas...",
            "Cargando paraderos...",
            "Cargando Horarios...",
            "Cargando Vehículos...",
            "Cargando Precios..."
        )
        showCountDownTimer(messages)
    }

    private fun showCountDownTimer(messages: List<String>, timeOutSec: Long = 30) {
        var index = 0
        timer = object : CountDownTimer(timeOutSec * 1000, 2000) {
            override fun onTick(millisUntilFinished: Long) {
                if (index >= messages.size - 1)
                    index = 0
                binding.tvInformation.setText(messages[index])
                index++
            }

            override fun onFinish() {
                with(binding) {
                    tvInformation.setText("¿Deseas cancelar la sincronización?")
                    btnCancelSync.visibility = View.VISIBLE

                    btnCancelSync.setOnClickListener {
                        syncJob?.cancel()
                        goMainActivity()
                    }
                }
            }
        }
        timer?.start()
    }

    private fun getMessageCompany() {
        authViewModel.getMessageCompany(UsuarioPreferences.getInstance(this).idEmpresa)
            .observe(this) { result ->
                when (result) {
                    is Resource.Failure -> {
                        Log.e(TAG, "getMessageCompany: ${result.exception.message}")
                    }
                    is Resource.Loading -> binding.progresBar.visibility = View.VISIBLE
                    is Resource.Success -> {
                        UsuarioPreferences.getInstance(this).descEmpresa = result.data.data
                    }
                }

            }
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
        ticketViewModel.syncTickets().observe(this) { result ->
            when (result) {
                is Resource.Failure -> Unit
                is Resource.Loading -> Unit
                is Resource.Success -> Unit
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }

    override fun onResume() {
        super.onResume()
        if (syncedData)
            goMainActivity()
    }

    private fun goMainActivity() {
        val next = Intent(this, MainActivity::class.java)
        next.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(next)
    }

    // Broadcast receiver que recibe las emisiones desde los servicios
    private inner class ResponseReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Constantes.ACTION_FINISH_LOCAL_SYNC -> {

                    binding.tvInformation.setText("Bienvenido")
                    Handler(Looper.myLooper()!!).postDelayed({
                        goMainActivity()
                    }, 1500)

                    syncedData = true
                    timer?.cancel()
                    Log.e("Splash", "Finalizado guardado de datos")
                }
            }
        }
    }
}