package com.smartgeeks.busticket

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.smartgeeks.busticket.api.Service
import com.smartgeeks.busticket.core.Resource
import com.smartgeeks.busticket.core.Resource.Loading
import com.smartgeeks.busticket.databinding.ActivityLoginBinding
import com.smartgeeks.busticket.presentation.AuthViewModel
import com.smartgeeks.busticket.sync.SyncServiceLocal
import com.smartgeeks.busticket.utils.Constantes
import com.smartgeeks.busticket.utils.DialogAlert
import com.smartgeeks.busticket.utils.Utilities
import com.smartgeeks.busticket.utils.RutaPreferences
import com.smartgeeks.busticket.utils.UsuarioPreferences
import dagger.hilt.android.AndroidEntryPoint

/**
 * user: PRUEBA
 * password: prueba321
 */
private var TAG: String = Login::class.java.simpleName

@AndroidEntryPoint
class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    var dialogAlert = DialogAlert()
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

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
                    showProgress(true)
                    binding.btnIniciarSession.visibility = View.GONE
                    signIn(email, password)
                }
            }
        }
    }

    private fun signIn(email: String, password: String) {
        // TODO Registrar inicio de sesion con DEVICE_ID, Lat y Long, fecha, hora y usuario (IP)
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
        authViewModel.setLoginLogs(userID, deviceID, "").observe(this, { result ->
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
}