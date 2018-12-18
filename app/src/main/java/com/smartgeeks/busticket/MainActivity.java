package com.smartgeeks.busticket;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.smartgeeks.busticket.Api.Service;
import com.smartgeeks.busticket.Menu.Inicio;
import com.smartgeeks.busticket.Menu.Perfil;
import com.smartgeeks.busticket.Menu.Ticket;
import com.smartgeeks.busticket.Utils.RutaPreferences;
import com.smartgeeks.busticket.Utils.UsuarioPreferences;

public class MainActivity extends AppCompatActivity {

    Context context;
    SharedPreferences preferences;
    Bundle bundle;

    boolean goBack ;

    public static final String BACK = "BACK_INTENT";

    ImageButton btnAbrirMenu;
    ImageView ivBanner;
    ListView navList;
    DrawerLayout drawer;
    //_STRING OPCIONES DEL MENU
    final String[] MenuItems = {"Perfil", "Inicio", "Tickets", "Cerrar Sesión"};

    int icons_categoria[] = {R.mipmap.icon_perfil,
            R.mipmap.icon_casa,
            R.mipmap.icon_ticket,
            R.mipmap.icon_cerrar_sesion};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        context = MainActivity.this;

        //set DrawerLayout
        btnAbrirMenu = (ImageButton) findViewById(R.id.imgAbrirMenu);
        ivBanner = (ImageView) findViewById(R.id.ivNameViewPager);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navList = (ListView) findViewById(R.id.drawer);

        ivBanner.setBackgroundResource(R.mipmap.img_logotipo_color);

        navList.setAdapter(new AdapterSpinner(context, R.layout.custom_menu, MenuItems));

        navList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {

                switch (pos) {
                    case 0:
                        drawer.closeDrawer(navList);
                        ivBanner.setBackgroundResource(R.mipmap.header_perfil);
                        setFragment(0);
                        break;

                    case 1:
                        drawer.closeDrawer(navList);
                        ivBanner.setBackgroundResource(R.mipmap.header_busticket);
                        setFragment(1);
                        break;

                    case 2:
                        drawer.closeDrawer(navList);
                        ivBanner.setBackgroundResource(R.mipmap.header_tickets);
                        setFragment(2);
                        break;

                    case 3:
                        drawer.closeDrawer(navList);
                        cerrarSession();
                        clearRuta();
                        break;
                }
            }
        });

        bundle = getIntent().getExtras();

        if (bundle != null){
            Log.e(Service.TAG, "entro a bundle") ;
            goBack = bundle.getBoolean(BACK, false);

            if (goBack == true){
                setFragment(2);
            }else {

            }

        }else {
            Log.e(Service.TAG, "no entro a bundle") ;
            setFragment(1);
        }




        btnAbrirMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(navList);
            }
        });
    }

    public void setFragment(int pos) {
        Fragment fragment = null;

        switch (pos) {
            case 0:
                fragment = new Perfil();
                getSupportFragmentManager().beginTransaction().replace(R.id.main, fragment).commit();
                break;

            case 1:
                fragment = new Inicio();
                getSupportFragmentManager().beginTransaction().replace(R.id.main, fragment).commit();
                break;

            case 2:
                fragment = new Ticket();
                getSupportFragmentManager().beginTransaction().replace(R.id.main, fragment).commit();
                break;

            case 3:

                break;
        }
    }

    /**
     * ADAPTER CUSTOM SPINNER
     */
    public class AdapterSpinner extends ArrayAdapter<String> {

        public AdapterSpinner(Context context, int textViewResourceId, String[] objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = getLayoutInflater();
            View row = inflater.inflate(R.layout.custom_menu, parent, false);
            TextView label = (TextView) row.findViewById(R.id.txtTitle);
            label.setText(MenuItems[position]);


            ImageView icon = (ImageView) row.findViewById(R.id.imgIcono);
            icon.setImageResource(icons_categoria[position]);

            return row;
        }
    }

    private void cerrarSession() {
        Intent intent = new Intent(context, Login.class);
        preferences = context.getSharedPreferences(UsuarioPreferences.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        preferences.edit().clear().commit();
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void clearRuta(){
        preferences = context.getSharedPreferences(RutaPreferences.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        preferences.edit().clear().commit();
    }
}
