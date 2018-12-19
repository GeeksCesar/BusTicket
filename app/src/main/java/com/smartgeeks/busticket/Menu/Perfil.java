package com.smartgeeks.busticket.Menu;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgeeks.busticket.Api.Service;
import com.smartgeeks.busticket.Login;
import com.smartgeeks.busticket.R;
import com.smartgeeks.busticket.Utils.RutaPreferences;
import com.smartgeeks.busticket.Utils.UsuarioPreferences;

public class Perfil extends Fragment {

    View view;
    Context context;
    Button btnCerrarSession;

    SharedPreferences preferences;
    SharedPreferences.Editor editor ;

    String namePrint;
    boolean estadoPrint;

    ImageView imgPerfil;
    TextView tvNombreUsuario, tvDocuUsuario, btnCerrarImpresora;

    public Perfil() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.menu_perfil, container, false);

        init();

        btnCerrarSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, Login.class);
                preferences = context.getSharedPreferences(UsuarioPreferences.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                preferences.edit().clear().commit();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        btnCerrarImpresora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
                builder.setTitle(context.getResources().getString(R.string.app_name));
                builder.setMessage(context.getResources().getString(R.string.dialogMessagePrint));

                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        preferences = context.getSharedPreferences(RutaPreferences.PREFERENCES_PRINT, Context.MODE_PRIVATE);
                        editor = preferences.edit();

                        editor.putBoolean(RutaPreferences.ESTADO_PRINT, false);
                        editor.commit();

                        init();
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.create().show();


            }
        });


        return view;
    }

    private void init() {
        context = getActivity();

        imgPerfil = view.findViewById(R.id.ivImgPerfil);
        tvNombreUsuario = view.findViewById(R.id.tvNameUsuario);
        tvDocuUsuario = view.findViewById(R.id.tvDocuUsuario);
        btnCerrarSession = view.findViewById(R.id.btnCerrarSession);
        btnCerrarImpresora = view.findViewById(R.id.btnChangePrint) ;

        tvNombreUsuario.setText(UsuarioPreferences.getInstance(context).getNombre());
        tvDocuUsuario.setText(UsuarioPreferences.getInstance(context).getRut());

        namePrint = RutaPreferences.getInstance(context).getNamePrint();
        estadoPrint = RutaPreferences.getInstance(context).getEstadoPrint();

        Log.e(Service.TAG, "namePrint: "+namePrint);
        Log.e(Service.TAG, "estadoPrint"+estadoPrint) ;

        if (estadoPrint == false){
            btnCerrarImpresora.setVisibility(View.GONE);
        }else{
            btnCerrarImpresora.setVisibility(View.VISIBLE);
        }
    }

}
