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
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.smartgeeks.busticket.sync.SyncServiceLocal
import com.smartgeeks.busticket.sync.SyncServiceRemote
import com.smartgeeks.busticket.sync.VolleySingleton
import com.smartgeeks.busticket.utils.Constantes
import com.smartgeeks.busticket.utils.UsuarioPreferences
import org.json.JSONException
import org.json.JSONObject

private val TAG: String = SplashScreen::class.java.simpleName

class SplashScreen : AppCompatActivity() {

    private val mContext: Context = this@SplashScreen
    var progresBar: ProgressBar? = null
    var splash: Thread? = null
    var preferences: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    var session: String? = null
    var context: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_splash_screen)
        context = this@SplashScreen

        // Filtro de acciones que serán alertadas
        val filter = IntentFilter(Constantes.ACTION_FINISH_LOCAL_SYNC)
        val receiver: ResponseReceiver = ResponseReceiver()
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)
        progresBar = findViewById(R.id.progresBar)
        // progresBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_IN)
        preferences = getSharedPreferences(UsuarioPreferences.SHARED_PREF_NAME, MODE_PRIVATE)
        session = UsuarioPreferences.getInstance(context).sessionUser
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

    private val dataUser: Unit
        private get() {
            val url =
                Constantes.GET_MESSAGE_COMPANY + UsuarioPreferences.getInstance(context).idEmpresa
            Log.e("TAG", "getDataUser: $url")
            VolleySingleton.getInstance(context).addToRequestQueue(
                StringRequest(
                    Request.Method.GET,
                    url,
                    { response ->
                        try {
                            val `object` = JSONObject(response)
                            UsuarioPreferences.getInstance(context).descEmpresa =
                                `object`.getString("data")
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                ) { error -> Log.e("SplashScreen", "" + error) }
            )
        }

    private fun goNextScreen() {
        if (session == "SessionSuccess") {
            dataUser
            localSync()
            remoteSync()
        } else if (session == "SessionFailed") {
            intent = Intent(context, Login::class.java)
            intent!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            finish()
            startActivity(intent)
        }
    }

    private fun localSync() {
        /**
         * Ejecutar el servicio de Sincronización Local
         */
        val sync = Intent(context, SyncServiceLocal::class.java)
        sync.action = Constantes.ACTION_RUN_LOCAL_SYNC
        startService(sync)
    }

    private fun remoteSync() {
        val sync = Intent(context, SyncServiceRemote::class.java)
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