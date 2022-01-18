package com.smartgeeks.busticket.Menu

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.smartgeeks.busticket.Login
import com.smartgeeks.busticket.R
import com.smartgeeks.busticket.api.Service
import com.smartgeeks.busticket.databinding.MenuPerfilBinding
import com.smartgeeks.busticket.utils.RutaPreferences
import com.smartgeeks.busticket.utils.UsuarioPreferences

class Perfil : Fragment(R.layout.menu_perfil) {

    var namePrint: String? = null
    var estadoPrint = false

    private lateinit var binding: MenuPerfilBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = MenuPerfilBinding.bind(view)

        initViews()
        setupPrinterViews()
    }

    private fun setupPrinterViews() = with(binding) {
        btnCerrarSession.setOnClickListener {
            val intent = Intent(context, Login::class.java)
            val preferences = context!!.getSharedPreferences(
                UsuarioPreferences.SHARED_PREF_NAME,
                Context.MODE_PRIVATE
            )
            preferences.edit().clear().apply()
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        btnChangePrint.setOnClickListener {
            val builder = AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
            builder.setTitle(context!!.resources.getString(R.string.app_name))
            builder.setMessage(context!!.resources.getString(R.string.dialogMessagePrint))
            builder.setPositiveButton("Si") { dialog, which ->
                val preferences = context!!.getSharedPreferences(
                    RutaPreferences.PREFERENCES_PRINT,
                    Context.MODE_PRIVATE
                )
                preferences.edit {
                    putBoolean(RutaPreferences.ESTADO_PRINT, false)
                    apply()
                }
            }
            builder.setNegativeButton("No") { dialog, which -> dialog.cancel() }
            builder.create().show()
        }
    }

    private fun initViews() = with(binding) {

        tvNameUsuario.text = UsuarioPreferences.getInstance(context).nombre
        tvDocuUsuario.text = UsuarioPreferences.getInstance(context).rut
        namePrint = RutaPreferences.getInstance(context).namePrint
        estadoPrint = RutaPreferences.getInstance(context).estadoPrint
        RutaPreferences.getInstance(context).estadoRuta = false
        Log.e(Service.TAG, "namePrint: $namePrint")
        Log.e(Service.TAG, "estadoPrint$estadoPrint")
        if (!estadoPrint) {
            btnChangePrint.visibility = View.GONE
        } else {
            btnChangePrint.visibility = View.VISIBLE
        }
    }
}