package com.smartgeeks.busticket;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.smartgeeks.busticket.Utils.UsuarioPreferences;

public class SplashScreen extends AppCompatActivity {

    private Context mContext = SplashScreen.this;
    ProgressBar progresBar;

    Thread splash;
    Intent intent ;

    SharedPreferences preferences;
    SharedPreferences.Editor editor;
    String session;
    Context context ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);

        context = SplashScreen.this ;

        progresBar = findViewById(R.id.progresBar) ;
        //Style ProgressBar
        progresBar = findViewById(R.id.progresBar);
        progresBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_IN);


        preferences = getSharedPreferences(UsuarioPreferences.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        session = UsuarioPreferences.getInstance(context).getSessionUser();

        splash =  new Thread(){
            @Override
            public void run() {
                try {
                    //Duracion
                    sleep(2000);
                    if (session.equals("SessionSuccess")) {
                        intent = new Intent(context, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }else if (session.equals("SessionFailed")) {
                        intent = new Intent(context, Login.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                }catch (Exception e){

                }
            }
        };
        splash.start();


    }
}
