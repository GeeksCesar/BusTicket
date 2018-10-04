package com.smartgeeks.busticket.Menu;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.smartgeeks.busticket.Api.Service;
import com.smartgeeks.busticket.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SelectRutas extends AppCompatActivity {

    public static final String ID = "ID" ;

    Bundle bundle;
    DecimalFormat formatea = new DecimalFormat("###,###.##");

    Button btnSiguiente, btnMenos, btnMas ;
    Spinner spInicio, spFin, spPasajero ;
    CheckBox cbAsiento, cbDePie ;
    TextView tvPrecioPasaje , tvCountItem;

    private JSONArray resutlParaderos;
    private JSONArray resultUsuarios;
    private ArrayList<String> listParaderos;
    private ArrayList<String> lisUsuarios;

    //VOLLEY
    JsonArrayRequest jsonArrayRequest;
    RequestQueue requestQueue;
    StringRequest stringRequest;

    int id_paradero;

    int countPasajes = 1;
    int precio_sum_pasaje ;
    int precioPasaje ;

    Context context ;

    private File pdfFile;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_select_rutas);

        initWidget();

        spFin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
               id_paradero = Integer.parseInt(getIdParadero(position)) ;
                Log.d(Service.TAG, "id: "+id_paradero);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spPasajero.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int postion, long id) {

                Log.d(Service.TAG, "id: "+id);
                if (id == 0){
                   precioPasaje = Integer.parseInt(getPrecio(postion, "t_adulto"));
                }else if (id == 1){
                    precioPasaje = Integer.parseInt(getPrecio(postion, "t_estudiante"));
                }else if (id == 2){
                    precioPasaje = Integer.parseInt(getPrecio(postion, "t_frecuente"));
                }else if (id == 3){
                    precioPasaje = Integer.parseInt(getPrecio(postion, "t_normal"));
                }

                formatPrecio(precioPasaje);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnMas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countPasajes ++ ;

                precio_sum_pasaje = precioPasaje * countPasajes ;

                Log.d(Service.TAG , "precio; "+precio_sum_pasaje);

                formatPrecio(precio_sum_pasaje);
                tvCountItem.setText(""+countPasajes);

            }
        });

        btnMenos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (countPasajes > 1){

                    countPasajes--;

                    precio_sum_pasaje = precioPasaje * countPasajes ;

                    formatPrecio(precio_sum_pasaje);
                    tvCountItem.setText("" + countPasajes);
                }

            }
        });

    }

    private void formatPrecio(int precio){
        String formatPrecio = formatea.format(precio);
        formatPrecio = formatPrecio.replace(',', '.');

        tvPrecioPasaje.setText("$ "+formatPrecio);
    }

    private void initWidget() {
        context = SelectRutas.this;
        requestQueue = Volley.newRequestQueue(context);


        btnSiguiente = findViewById(R.id.btnNext);
        btnMas = findViewById(R.id.btnSumar);
        btnMenos= findViewById(R.id.btnRestar);
        spInicio = findViewById(R.id.spInicio);
        spFin = findViewById(R.id.spFIn);
        spPasajero = findViewById(R.id.spUsuarios);
        cbAsiento = findViewById(R.id.cbAsientos);
        cbDePie = findViewById(R.id.cbPie);
        tvCountItem = findViewById(R.id.textCount);
        tvPrecioPasaje = findViewById(R.id.tvPrecio);


        bundle = getIntent().getExtras();
        String id = bundle.getString(ID);

        listParaderos = new ArrayList<String>();
        lisUsuarios = new ArrayList<String>();


        btnSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    createPdfWrapper();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
            }
        });

        getParaderos();
        getUsuarios();

        validarCheckBox();
        tvCountItem.setText(""+ countPasajes);

    }

    private void validarCheckBox() {
        cbAsiento.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (cbAsiento.isChecked()){
                    cbDePie.setChecked(false);
                }
            }
        });

        cbDePie.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (cbDePie.isChecked()){
                    cbAsiento.setChecked(false);
                }
            }
        });

    }

    private void getParaderos() {
        listParaderos.clear();

        stringRequest = new StringRequest(Service.GET_PARADEROS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(Service.TAG, "response: "+response);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    resutlParaderos = jsonObject.getJSONArray("estacion");

                    if (resutlParaderos.length() > 0) {
                        for (int i = 0; i < resutlParaderos.length(); i++) {
                            try {
                                JSONObject json = resutlParaderos.getJSONObject(i);
                                String nombreInstitucion = json.getString("paradero");

                                listParaderos.add(nombreInstitucion);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        //setAdapter
                        spInicio.setAdapter(new ArrayAdapter<String>(context, R.layout.custom_spinner_inicio, R.id.txtName, listParaderos));
                        spFin.setAdapter(new ArrayAdapter<String>(context, R.layout.custom_spinner_fin, R.id.txtName, listParaderos));

                    }else {

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });

        requestQueue.add(stringRequest);

    }


    private void getUsuarios() {

        lisUsuarios.clear();

        stringRequest = new StringRequest(Service.GET_USUARIOS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                    resultUsuarios = jsonObject.getJSONArray("usuario");

                    if (resultUsuarios.length() > 0) {
                        for (int i = 0; i < resultUsuarios.length(); i++) {
                            try {
                                JSONObject json = resultUsuarios.getJSONObject(i);
                                String nombreInstitucion = json.getString("nombre");

                                lisUsuarios.add(nombreInstitucion);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        //setAdapter
                        spPasajero.setAdapter(new ArrayAdapter<String>(context, R.layout.custom_spinner_tipo_pasajero, R.id.txtName, lisUsuarios));
                    }else {

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });

        requestQueue.add(stringRequest);
        
    }

    private String getIdParadero(int position){
        String idParadero = "";
        try {
            JSONObject object = resutlParaderos.getJSONObject(position);
            idParadero = object.getString("id");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return idParadero;
    }

    public String getPrecio(int position , String name_usuario){
        Log.d(Service.TAG, "usuario: "+name_usuario);
        String precio = "";
        try {
            JSONObject object = resutlParaderos.getJSONObject(position);
            precio = object.getString(name_usuario);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return  precio;

    }


    private void createPdfWrapper() throws FileNotFoundException,DocumentException {

        int hasWriteStoragePermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteStoragePermission != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_CONTACTS)) {

                }

                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
            return;
        }else {
            createPdf();
        }
    }

    private void createPdf() throws FileNotFoundException, DocumentException {

        File docsFolder = new File(Environment.getExternalStorageDirectory() + "/Documents");
        if (!docsFolder.exists()) {
            docsFolder.mkdir();
            Log.i(Service.TAG, "Created a new directory for PDF");
        }

        pdfFile = new File(docsFolder.getAbsolutePath(),"ticket.pdf");
        OutputStream output = new FileOutputStream(pdfFile);
        Document document = new Document();
        PdfWriter.getInstance(document, output);
        document.open();

        String Empresa = "Empresa :  Coomotor" ;
        String Placa = "PLACA: 44DHJU" ;
        String Operador = "USUARIO: Cesar Lizcano" ;
        String Ruta = "RUTA: Neiva - Campoalegre" ;
        String Precio = "PRECIO: $ 5.000" ;
        String Hora = "HORA SALIDA: 09:00" ;
        String Fecha = "FECHA: 28-09-2018" ;

        document.add(new Paragraph(Empresa));
        document.add(new Paragraph(Placa));
        document.add(new Paragraph(Operador));
        document.add(new Paragraph(Ruta));
        document.add(new Paragraph(Precio));
        document.add(new Paragraph(Hora));
        document.add(new Paragraph(Fecha));

        document.close();

        previewPdf();

    }



    private void previewPdf() {

        PackageManager packageManager = getPackageManager();
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        testIntent.setType("application/pdf");
        List list = packageManager.queryIntentActivities(testIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (list.size() > 0) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            Uri uri = Uri.fromFile(pdfFile);
            intent.setDataAndType(uri, "application/pdf");

            startActivity(intent);
        }else{
            Toast.makeText(this,"Download a PDF Viewer to see the generated PDF",Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    try {
                        createPdfWrapper();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }
                } else {
                    // Permission Denied
                    Toast.makeText(this, "WRITE_EXTERNAL Permission Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
