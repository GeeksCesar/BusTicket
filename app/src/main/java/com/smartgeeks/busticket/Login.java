package com.smartgeeks.busticket;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.smartgeeks.busticket.Api.ApiService;
import com.smartgeeks.busticket.Api.Service;
import com.smartgeeks.busticket.Modelo.Signin;
import com.smartgeeks.busticket.Objcect.User;
import com.smartgeeks.busticket.Utils.Constantes;
import com.smartgeeks.busticket.Utils.DialogAlert;
import com.smartgeeks.busticket.Utils.UsuarioPreferences;
import com.smartgeeks.busticket.sync.SyncService;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
                            localSync();
                            goMainActivity();

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

    private void goMainActivity() {

        new Thread() {
            @Override
            public void run() {
                try {
                    //Duracion
                    sleep(5000);

                    Intent intent = new Intent(context, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.left_in, R.anim.left_out);

                } catch (Exception e) {

                }
            }
        }.start();

    }

    private void localSync() {
        /**
         * Ejecutar el servicio de Sincronización Local
         */
        Intent sync = new Intent(context, SyncService.class);
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
    public boolean dispatchTouchEvent(final MotionEvent ev) {
        final View currentFocus = getCurrentFocus();
        if (!(currentFocus instanceof EditText) || !isTouchInsideView(ev, currentFocus)) {
            ((InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
        return super.dispatchTouchEvent(ev);
    }
    private boolean isTouchInsideView(final MotionEvent ev, final View currentFocus) {
        final int[] loc = new int[2];
        currentFocus.getLocationOnScreen(loc);
        return ev.getRawX() > loc[0] && ev.getRawY() > loc[1] && ev.getRawX() < (loc[0] + currentFocus.getWidth())
                && ev.getRawY() < (loc[1] + currentFocus.getHeight());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (call != null) {
            call.cancel();
            call = null;
        }
    }


}
