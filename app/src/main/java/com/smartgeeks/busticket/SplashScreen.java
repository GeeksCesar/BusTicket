package com.smartgeeks.busticket;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.smartgeeks.busticket.Utils.Constantes;
import com.smartgeeks.busticket.Utils.UsuarioPreferences;
import com.smartgeeks.busticket.sync.SyncService;
import com.smartgeeks.busticket.sync.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

public class SplashScreen extends AppCompatActivity {

    private Context mContext = SplashScreen.this;
    ProgressBar progresBar;

    Thread splash;
    Intent intent;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String session;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        context = SplashScreen.this;

        progresBar = findViewById(R.id.progresBar);
        //Style ProgressBar
        progresBar = findViewById(R.id.progresBar);
        progresBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_IN);

        preferences = getSharedPreferences(UsuarioPreferences.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        session = UsuarioPreferences.getInstance(context).getSessionUser();

        if (session.equals("SessionSuccess")) {
            getDataUser();
            localSync();
            remotoSync();
        }

        splash = new Thread() {
            @Override
            public void run() {
                try {
                    //Duracion
                    sleep(3000);
                    goNextScreen();

                } catch (Exception e) {

                }
            }
        };
        splash.start();


    }

    private void getDataUser() {
        VolleySingleton.getInstance(context).addToRequestQueue(
                new StringRequest(
                        Request.Method.GET,
                        Constantes.GET_MESSAGE_COMPANY + UsuarioPreferences.getInstance(context).getIdEmpresa(),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject object = new JSONObject(response);
                                    UsuarioPreferences.getInstance(context).setDescEmpresa(object.getString("data"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e("SplashScreen", "" + error);
                            }
                        }
                )
        );
    }

    private void goNextScreen() {
        if (session.equals("SessionSuccess")) {
            intent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (session.equals("SessionFailed")) {
            intent = new Intent(context, Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private void localSync() {
        /**
         * Ejecutar el servicio de Sincronización Local
         */
        Intent sync = new Intent(context, SyncService.class);
        sync.setAction(Constantes.ACTION_RUN_LOCAL_SYNC);
        startService(sync);
    }

    private void remotoSync() {
        Intent sync = new Intent(context, SyncService.class);
        sync.setAction(Constantes.ACTION_RUN_REMOTE_SYNC);
        startService(sync);
    }


}
