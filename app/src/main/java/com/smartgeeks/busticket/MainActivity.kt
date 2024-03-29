package com.smartgeeks.busticket

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.smartgeeks.busticket.Modelo.Horario
import com.smartgeeks.busticket.Modelo.Paradero
import com.smartgeeks.busticket.Modelo.Ruta
import com.smartgeeks.busticket.Modelo.SubRuta
import com.smartgeeks.busticket.Modelo.TarifaParadero
import com.smartgeeks.busticket.Modelo.TipoUsuario
import com.smartgeeks.busticket.Modelo.Vehiculo
import com.smartgeeks.busticket.core.Resource
import com.smartgeeks.busticket.databinding.ActivityMainBinding
import com.smartgeeks.busticket.menu.Inicio
import com.smartgeeks.busticket.menu.IntercityFragment
import com.smartgeeks.busticket.menu.Perfil
import com.smartgeeks.busticket.menu.Ticket
import com.smartgeeks.busticket.presentation.AuthViewModel
import com.smartgeeks.busticket.presentation.TicketViewModel
import com.smartgeeks.busticket.utils.RutaPreferences
import com.smartgeeks.busticket.utils.UsuarioPreferences
import com.smartgeeks.busticket.utils.Utilities
import com.smartgeeks.busticket.utils.setTint
import dagger.hilt.android.AndroidEntryPoint

private val TAG: String = MainActivity::class.java.simpleName

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    var context: Context? = null
    var preferences: SharedPreferences? = null
    var bundle: Bundle? = null
    var goBack = false

    private lateinit var binding: ActivityMainBinding
    private val authViewModel: AuthViewModel by viewModels()
    private val ticketViewModel: TicketViewModel by viewModels()

    //_STRING OPCIONES DEL MENU
    val MenuItems = arrayOf("Perfil", "Inicio", "Tickets", "Cerrar Sesión")
    var icons_categoria = intArrayOf(
        R.mipmap.icon_perfil,
        R.mipmap.icon_casa,
        R.mipmap.icon_ticket,
        R.mipmap.icon_cerrar_sesion
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this@MainActivity

        //set DrawerLayout
        // binding.ivNameViewPager.setBackgroundResource(R.mipmap.img_logotipo_color)
        binding.ivNameViewPager.apply {
            setTint(R.color.colorPrimary)
            setImageResource(R.drawable.logotipo_busticket)
        }
        binding.lvNavItems.adapter = AdapterSpinner(context, R.layout.custom_menu, MenuItems)
        binding.lvNavItems.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, view, pos, id ->
                when (pos) {
                    0 -> {
                        binding.drawerLayout.closeDrawer(binding.lvNavItems)
                        binding.ivNameViewPager.setImageResource(R.mipmap.header_perfil)
                        setFragment(0)
                    }
                    1 -> {
                        binding.drawerLayout.closeDrawer(binding.lvNavItems)
                        binding.ivNameViewPager.setImageResource(R.drawable.logotipo_busticket)
                        setFragment(1)
                    }
                    2 -> {
                        binding.drawerLayout.closeDrawer(binding.lvNavItems)
                        binding.ivNameViewPager.setImageResource(R.mipmap.header_tickets)
                        setFragment(2)
                    }
                    3 -> {
                        binding.drawerLayout.closeDrawer(binding.lvNavItems)
                        cerrarSession()
                        clearRuta()
                    }
                }
            }
        bundle = intent.extras
        if (bundle != null) {
            goBack = bundle!!.getBoolean(BACK, false)
            if (goBack) {
                setFragment(2)
            } else {
            }
        } else {
            setFragment(1)
        }
        binding.btnAbrirMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(
                binding.lvNavItems
            )
        }
    }

    fun setFragment(pos: Int) {
        var fragment: Fragment? = null
        when (pos) {
            0 -> {
                fragment = Perfil()
                supportFragmentManager.beginTransaction().replace(R.id.main, fragment).commit()
            }
            1 -> {
                fragment = Inicio()
                supportFragmentManager.beginTransaction().replace(R.id.main, fragment).commit()
            }
            2 -> {
                fragment = Ticket()
                supportFragmentManager.beginTransaction().replace(R.id.main, fragment).commit()
            }
            3 -> {
                fragment = IntercityFragment()
                supportFragmentManager.beginTransaction().replace(R.id.main, fragment).commit()
            }
        }
    }

    /**
     * ADAPTER CUSTOM SPINNER
     */
    inner class AdapterSpinner(
        context: Context?,
        textViewResourceId: Int,
        objects: Array<String>?
    ) : ArrayAdapter<String?>(
        context!!, textViewResourceId, objects!!
    ) {
        override fun getDropDownView(position: Int, convertView: View, parent: ViewGroup): View {
            return getCustomView(position, convertView, parent)
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getCustomView(position, convertView, parent)
        }

        fun getCustomView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val inflater = layoutInflater
            val row = inflater.inflate(R.layout.custom_menu, parent, false)
            val label = row.findViewById<TextView>(R.id.txtTitle)
            label.text = MenuItems[position]
            val icon = row.findViewById<ImageView>(R.id.imgIcono)
            icon.setImageResource(icons_categoria[position])
            return row
        }
    }

    private fun cerrarSession() {
        TruncateDatabase().execute()
    }

    private fun clearRuta() {
        preferences = context!!.getSharedPreferences(RutaPreferences.SHARED_PREF_NAME, MODE_PRIVATE)
        preferences?.edit {
            clear()
            apply()
        }
    }

    private inner class TruncateDatabase : AsyncTask<String?, Void?, String?>() {

        override fun onPostExecute(s: String?) {
            super.onPostExecute(s)
            val intent = Intent(context, Login::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        override fun doInBackground(vararg params: String?): String? {
            // Borrado de datos
            preferences =
                context!!.getSharedPreferences(UsuarioPreferences.SHARED_PREF_NAME, MODE_PRIVATE)
            preferences?.edit {
                clear()
                apply()
            }
            TipoUsuario.deleteAll(TipoUsuario::class.java)
            TarifaParadero.deleteAll(TarifaParadero::class.java)
            Vehiculo.deleteAll(Vehiculo::class.java)
            Ruta.deleteAll(Ruta::class.java)
            Horario.deleteAll(Horario::class.java)
            Paradero.deleteAll(Paradero::class.java)
            SubRuta.deleteAll(SubRuta::class.java)
            return null
        }
    }

    /**
     * Handle set StatusUser
     */
    private fun setUserStatus(status: Int) {
        try {
            authViewModel.setUserStatus(
                UsuarioPreferences.getInstance(this).idUser,
                Utilities.getDeviceId(this),
                status
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val BACK = "BACK_INTENT"
    }

    override fun onResume() {
        super.onResume()

        try {
            setUserStatus(1)
            observeRemoteSync()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun observeRemoteSync() {
        ticketViewModel.getCountTickets().observe(this) { count ->
            Log.e(TAG, "observeRemoteSync: $count  ${ticketViewModel.isSyncing}")
            if (count > 0 && !ticketViewModel.isSyncing) {
                handleSendLocalTickets()
            }
        }
    }

    private fun handleSendLocalTickets() {
        ticketViewModel.syncTickets().observe(this) { result ->
            when (result) {
                is Resource.Failure -> Unit
                is Resource.Loading -> Unit
                is Resource.Success -> {
                    if (result.data.estado == 1) {
                        Toast.makeText(this, "Datos sincronizados", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.e(TAG, "onPause: User Inactive")
        setUserStatus(0)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "onDestroy: ")
    }
}