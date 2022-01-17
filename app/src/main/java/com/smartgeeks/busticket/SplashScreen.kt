package com.smartgeeks.busticket

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewSwitcher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.itextpdf.text.SpecialSymbol.index
import com.smartgeeks.busticket.core.Resource
import com.smartgeeks.busticket.databinding.ActivitySplashScreenBinding
import com.smartgeeks.busticket.presentation.AuthViewModel
import com.smartgeeks.busticket.sync.SyncServiceLocal
import com.smartgeeks.busticket.sync.SyncServiceRemote
import com.smartgeeks.busticket.utils.Constantes
import com.smartgeeks.busticket.utils.UsuarioPreferences
import com.smartgeeks.busticket.utils.Utilities
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private val TAG: String = SplashScreen::class.java.simpleName

@AndroidEntryPoint
class SplashScreen : AppCompatActivity() {

    var splash: Thread? = null
    var preferences: SharedPreferences? = null
    var session: String? = null

    private lateinit var binding: ActivitySplashScreenBinding
    private val authViewModel: AuthViewModel by viewModels()
    private var isLockedDevice: Boolean = false
    private lateinit var timer: CountDownTimer
    private lateinit var textView: TextView
    private val scope = CoroutineScope(Job() + Dispatchers.IO)

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

            // Execute JOB on coroutines
            scope.launch(Dispatchers.Main) {
                setInformationLoading()
            }

            scope.launch {
                localSync()
                remoteSync()
                Log.e(TAG, "Carga FINALIAZADDA")
            }

        } else if (session == "SessionFailed" || isLockedDevice) {
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
            "Cargando Precios..."
        )
        showCountDownTimer(30, messages)
    }

    private fun showCountDownTimer(timeOut : Long, messages : List<String>) {
        var index = 0
        timer = object: CountDownTimer(timeOut*1000, 2000) {
            override fun onTick(millisUntilFinished: Long) {
                if (index >= messages.size - 1)
                    index = 0
                binding.tvInformation.setText(messages[index])
                index++
            }

            override fun onFinish() {
                binding.tvInformation.setText("Show the button")
                // TODO: Show button to cancel load
            }
        }
        timer.start()
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

    override fun onDestroy() {
        super.onDestroy()
        try {
            timer.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Broadcast receiver que recibe las emisiones desde los servicios
    private inner class ResponseReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Constantes.ACTION_FINISH_LOCAL_SYNC -> {
                    val next = Intent(context, MainActivity::class.java)
                    next.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    Handler(Looper.myLooper()!!).postDelayed({
                        startActivity(next)
                    }, 1500)

                    Log.e("Splash", "Finalizado guardado de datos")
                }
            }
        }
    }
}