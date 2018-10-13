package com.smartgeeks.busticket.Menu;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.smartgeeks.busticket.Login;
import com.smartgeeks.busticket.R;
import com.smartgeeks.busticket.Utils.UsuarioPreferences;

public class Perfil extends Fragment {

    View view;
    Context context;
    Button btnCerrarSession;

    SharedPreferences preferences;

    ImageView imgPerfil;
    TextView tvNombreUsuario, tvDocuUsuario;

    public Perfil() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.menu_perfil, container, false);

        context = getActivity();

        imgPerfil = view.findViewById(R.id.ivImgPerfil);
        tvNombreUsuario = view.findViewById(R.id.tvNameUsuario);
        tvDocuUsuario = view.findViewById(R.id.tvDocuUsuario);
        btnCerrarSession = view.findViewById(R.id.btnCerrarSession);


        tvNombreUsuario.setText(UsuarioPreferences.getInstance(context).getNombre());

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


        return view;
    }

}
