package com.smartgeeks.busticket;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.smartgeeks.busticket.Api.ApiService;
import com.smartgeeks.busticket.Api.Service;
import com.smartgeeks.busticket.Modelo.Signin;
import com.smartgeeks.busticket.Objects.User;
import com.smartgeeks.busticket.Utils.Constantes;
import com.smartgeeks.busticket.Utils.DialogAlert;
import com.smartgeeks.busticket.Utils.RutaPreferences;
import com.smartgeeks.busticket.Utils.UsuarioPreferences;
import com.smartgeeks.busticket.sync.SyncServiceLocal;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * user: PRUEBA
 * password: prueba321
 */

public class Login extends AppCompatActivity {

    private Context context;
    ImageView imgBannner;
    EditText edUsuario, edPassword;
    Button mSignInButton;

    private View mProgressView;

    ApiService apiService;
    DialogAlert dialogAlert = new DialogAlert();

    Call<Signin> call;

    boolean errorSiginin;
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

        // Filtro de acciones que serán alertadas
        IntentFilter filter = new IntentFilter(Constantes.ACTION_FINISH_LOCAL_SYNC);
        ResponseReceiver receiver = new ResponseReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);


        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edUsuario.getText().toString().trim();
                String password = edPassword.getText().toString().trim();

                if (!DialogAlert.verificaConexion(context)) {
                    dialogAlert.showDialogErrorConexion(context);
                } else {
                    if (email.isEmpty() || password.isEmpty()) {
                        DialogAlert.showDialogFailed(context, "Alerta", "Rellene los campos", SweetAlertDialog.WARNING_TYPE);
                    } else {
                        showProgress(true);
                        mSignInButton.setVisibility(View.GONE);
                        signIn(email, password);
                    }

                }

            }
        });


    }

    private void signIn(String email, String password) {
        call = apiService.userLogin(email, password);

        call.enqueue(new Callback<Signin>() {
            @Override
            public void onResponse(Call<Signin> call, Response<Signin> response) {
                Log.e(Service.TAG, "response: " + response);

                if (response.isSuccessful()) {
                    errorSiginin = response.body().getError();
                    messageSignin = response.body().getMessage();

                    if (errorSiginin == true) {
                        User user = response.body().getUser();

                        if (user.getIdRol() == 2 || user.getIdRol() == 3) {
                            UsuarioPreferences.getInstance(context).userPreferences(user);
                            setDataPrefrences();
                            dialogSelectRoleUser();

                        } else {
                            DialogAlert.showDialogFailed(context, "Error", "No tiene permiso", SweetAlertDialog.ERROR_TYPE);
                            showProgress(false);
                            mSignInButton.setVisibility(View.VISIBLE);
                        }


                    } else if (errorSiginin == false) {
                        if (messageSignin.equals("Password Incorrecta")) {
                            DialogAlert.showDialogFailed(context, "Error", "Contraseña incorrecta", SweetAlertDialog.ERROR_TYPE);
                            showProgress(false);
                            mSignInButton.setVisibility(View.VISIBLE);
                        } else if (messageSignin.equals("Usuario no existe")) {
                            DialogAlert.showDialogFailed(context, "Error", "Usuario no Existe", SweetAlertDialog.ERROR_TYPE);
                            showProgress(false);
                            mSignInButton.setVisibility(View.VISIBLE);
                        }
                    }


                } else {
                    dialogAlert.showDialogErrorConexion(context);
                    showProgress(false);
                    mSignInButton.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onFailure(Call<Signin> call, Throwable t) {
                Log.d(Service.TAG, "response: " + t.getMessage());
                dialogAlert.showDialogErrorConexion(context);
                showProgress(false);
                mSignInButton.setVisibility(View.VISIBLE);
            }
        });

    }

    private void dialogSelectRoleUser() {

        SweetAlertDialog alertDialog = new SweetAlertDialog(Login.this,
                SweetAlertDialog.NORMAL_TYPE);
                alertDialog.setTitleText("Selecciona el modo de venta")
                .setConfirmText("Conductor")
                .setCancelText("Boletería")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        localSync();
                        RutaPreferences.getInstance(context).setEstadoRuta(false);
                        UsuarioPreferences.getInstance(Login.this).setRoleVenta("conductor");
                    }
                })
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.cancel();
                        UsuarioPreferences.getInstance(Login.this).setRoleVenta("operador");
                        localSync();
                    }
                })
                .show();

    }

    private void goMainActivity() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.left_in, R.anim.left_out);
    }

    private void localSync() {
        /**
         * Ejecutar el servicio de Sincronización Local
         */
        Intent sync = new Intent(context, SyncServiceLocal.class);
        sync.setAction(Constantes.ACTION_RUN_LOCAL_SYNC);
        startService(sync);
    }

    private void showProgress(boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void initWidget() {
        apiService = Service.getApiService();
        context = Login.this;

        imgBannner = findViewById(R.id.ivLogoPrincipal);
        mSignInButton = findViewById(R.id.btnIniciarSession);
        edPassword = findViewById(R.id.edPassword);
        edUsuario = findViewById(R.id.edUsuario);
        mProgressView = findViewById(R.id.login_progress);

        edPassword.setImeOptions(EditorInfo.IME_ACTION_DONE);
    }

    private void setDataPrefrences() {
        preferences = getSharedPreferences(UsuarioPreferences.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();

        editor.putString(UsuarioPreferences.KEY_SESSION, "SessionSuccess");
        editor.commit();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (call != null) {
            call.cancel();
            call = null;
        }
    }

    // Broadcast receiver que recibe las emisiones desde los servicios
    private class ResponseReceiver extends BroadcastReceiver {

        // Sin instancias
        private ResponseReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Constantes.ACTION_FINISH_LOCAL_SYNC:
                    Log.e("Login", "Finalizado guardado de datos");
                    goMainActivity();
                    break;
            }
        }
    }


}
