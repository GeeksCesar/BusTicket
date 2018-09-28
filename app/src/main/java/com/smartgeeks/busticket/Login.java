package com.smartgeeks.busticket;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;

import com.smartgeeks.busticket.Api.ApiService;
import com.smartgeeks.busticket.Api.Service;
import com.smartgeeks.busticket.Modelo.Signin;
import com.smartgeeks.busticket.Objcect.User;
import com.smartgeeks.busticket.Utils.DialogAlert;
import com.smartgeeks.busticket.Utils.UsuarioPreferences;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {

    private Context context ;
    ImageView imgBannner ;
    EditText edUsuario, edPassword ;
    Button mSignInButton;
    CheckBox checkRecordarme ;
    private View mProgressView;

    ApiService apiService;
    DialogAlert dialogAlert = new DialogAlert();

    Call<Signin> call ;

    boolean errorSiginin ;
    String messageSignin;

    //Prefrences
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);

        initWidget();

        checkRecordarme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                preferences = getSharedPreferences(UsuarioPreferences.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                editor = preferences.edit();

                if (checkRecordarme.isChecked()){
                    editor.putString(UsuarioPreferences.KEY_SESSION, "SessionSuccess") ;
                    editor.commit();
                }else {
                    editor.putString(UsuarioPreferences.KEY_SESSION, "SessionFailed") ;
                    editor.commit();
                }
            }
        });


        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Store values at the time of the login attempt.
                String email = edPassword.getText().toString();
                String password = edUsuario.getText().toString();

                if (!dialogAlert.verificaConexion(context)){
                    dialogAlert.showDialogErrorConexion(context);
                }else {
                   // showProgress(true);
                    signIn(email, password);
                }

            }
        });



    }

    private void signIn(String email, String password) {
        call = apiService.userLogin(email, password);

        call.enqueue(new Callback<Signin>() {
            @Override
            public void onResponse(Call<Signin> call, Response<Signin> response) {
                Log.d(Service.TAG, "response: " + response);

                if (response.isSuccessful()){
                    errorSiginin = response.body().getError();
                    messageSignin = response.body().getMessage();

                    Log.d(Service.TAG, "error: "+errorSiginin);
                    Log.d(Service.TAG, "message: "+messageSignin);

                    if (errorSiginin == true){

                        User user = response.body().getUser();

                        if (user.getIdRol() == 2){
                            Intent intent = new Intent(context, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            UsuarioPreferences.getInstance(context).userPreferences(response.body().getUser());
                            overridePendingTransition(R.anim.left_in, R.anim.left_out);
                        }else {
                            dialogAlert.showDialogFailed(context, "Error", "No tiene permiso", SweetAlertDialog.ERROR_TYPE);
                            showProgress(false);
                        }


                    }else if (errorSiginin == false){
                        if (messageSignin.equals("Password Incorrecta")){
                            dialogAlert.showDialogFailed(context, "Error", "Contraseña incorrecta", SweetAlertDialog.ERROR_TYPE);
                            showProgress(false);
                        }else if (messageSignin.equals("Usuario no existe")){
                            dialogAlert.showDialogFailed(context, "Error", "Usuario no Existe", SweetAlertDialog.ERROR_TYPE);
                            showProgress(false);
                        }
                    }


                }else {
                    dialogAlert.showDialogErrorConexion(context);
                    showProgress(false);
                }

            }

            @Override
            public void onFailure(Call<Signin> call, Throwable t) {
                Log.d(Service.TAG, "response: " + t.getMessage());
                dialogAlert.showDialogErrorConexion(context);
                showProgress(false);
            }
        });

    }

    private void showProgress(boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void initWidget() {
        apiService = Service.getApiService();
        context = Login.this ;

        imgBannner = findViewById(R.id.ivLogoPrincipal) ;
        mSignInButton = findViewById(R.id.btnIniciarSession);
        edPassword = findViewById(R.id.edPassword);
        edUsuario = findViewById(R.id.edUsuario);
        checkRecordarme = findViewById(R.id.cbRecordarme);
        mProgressView = findViewById(R.id.login_progress) ;


       // Animation slideUpIn = AnimationUtils.loadAnimation(context, R.anim.shide_in);
        //imgBannner.startAnimation(slideUpIn);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (call != null){
            call.cancel();
            call = null ;
        }
    }
}
