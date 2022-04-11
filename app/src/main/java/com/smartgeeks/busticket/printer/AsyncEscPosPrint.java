package com.smartgeeks.busticket.printer;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.dantsu.escposprinter.EscPosCharsetEncoding;
import com.dantsu.escposprinter.EscPosPrinter;
import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.exceptions.EscPosBarcodeException;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;
import com.dantsu.escposprinter.exceptions.EscPosEncodingException;
import com.dantsu.escposprinter.exceptions.EscPosParserException;

import java.lang.ref.WeakReference;

public abstract class AsyncEscPosPrint extends AsyncTask<AsyncEscPosPrinter, Integer, Integer> {
    protected final static int FINISH_SUCCESS = 1;
    protected final static int FINISH_NO_PRINTER = 2;
    protected final static int FINISH_PRINTER_DISCONNECTED = 3;
    protected final static int FINISH_PARSER_ERROR = 4;
    protected final static int FINISH_ENCODING_ERROR = 5;
    protected final static int FINISH_BARCODE_ERROR = 6;

    protected final static int PROGRESS_CONNECTING = 1;
    protected final static int PROGRESS_CONNECTED = 2;
    protected final static int PROGRESS_PRINTING = 3;
    protected final static int PROGRESS_PRINTED = 4;

    protected ProgressDialog dialog;
    protected WeakReference<Context> weakContext;


    public AsyncEscPosPrint(Context context) {
        this.weakContext = new WeakReference<>(context);
    }

    protected Integer doInBackground(AsyncEscPosPrinter... printersData) {
        if (printersData.length == 0) {
            return AsyncEscPosPrint.FINISH_NO_PRINTER;
        }

        this.publishProgress(AsyncEscPosPrint.PROGRESS_CONNECTING);

        AsyncEscPosPrinter printerData = printersData[0];

        try {
            DeviceConnection deviceConnection = printerData.getPrinterConnection();

            if(deviceConnection == null) {
                return AsyncEscPosPrint.FINISH_NO_PRINTER;
            }

            EscPosPrinter printer = new EscPosPrinter(
                    deviceConnection,
                    printerData.getPrinterDpi(),
                    printerData.getPrinterWidthMM(),
                    printerData.getPrinterNbrCharactersPerLine(),
                    new EscPosCharsetEncoding("windows-1252", 16)
            );

            this.publishProgress(AsyncEscPosPrint.PROGRESS_PRINTING);

            printer.printFormattedTextAndCut(printerData.getTextToPrint());

            this.publishProgress(AsyncEscPosPrint.PROGRESS_PRINTED);

        } catch (EscPosConnectionException e) {
            e.printStackTrace();
            return AsyncEscPosPrint.FINISH_PRINTER_DISCONNECTED;
        } catch (EscPosParserException e) {
            e.printStackTrace();
            return AsyncEscPosPrint.FINISH_PARSER_ERROR;
        } catch (EscPosEncodingException e) {
            e.printStackTrace();
            return AsyncEscPosPrint.FINISH_ENCODING_ERROR;
        } catch (EscPosBarcodeException e) {
            e.printStackTrace();
            return AsyncEscPosPrint.FINISH_BARCODE_ERROR;
        }

        return AsyncEscPosPrint.FINISH_SUCCESS;
    }

    protected void onPreExecute() {
        if (this.dialog == null) {
            Context context = weakContext.get();

            if (context == null) {
                return;
            }

            this.dialog = new ProgressDialog(context);
            this.dialog.setTitle("Impresión en progreso...");
            this.dialog.setMessage("...");
            this.dialog.setProgressNumberFormat("%1d / %2d");
            this.dialog.setCancelable(true);
            this.dialog.setIndeterminate(false);
            this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.dialog.show();
        }
    }

    protected void onProgressUpdate(Integer... progress) {
        switch (progress[0]) {
            case AsyncEscPosPrint.PROGRESS_CONNECTING:
                this.dialog.setMessage("Conectando impresora...");
                break;
            case AsyncEscPosPrint.PROGRESS_CONNECTED:
                this.dialog.setMessage("La impresora está conectada...");
                break;
            case AsyncEscPosPrint.PROGRESS_PRINTING:
                this.dialog.setMessage("Imprimiendo...");
                break;
            case AsyncEscPosPrint.PROGRESS_PRINTED:
                this.dialog.setMessage("La impresión ha finalizado...");
                break;
        }
        this.dialog.setProgress(progress[0]);
        this.dialog.setMax(4);
    }

    protected void onPostExecute(Integer result) {
        this.dialog.dismiss();
        this.dialog = null;

        Context context = weakContext.get();

        if (context == null) {
            return;
        }

        switch (result) {
            case AsyncEscPosPrint.FINISH_SUCCESS:
                new AlertDialog.Builder(context)
                        .setTitle("Éxito")
                        .setMessage("Se ha realizado la impresión!")
                        .show();
                break;
            case AsyncEscPosPrint.FINISH_NO_PRINTER:
                new AlertDialog.Builder(context)
                        .setTitle("Sin impresora")
                        .setMessage("La aplicación no encuentra ninguna impresora conectada.")
                        .show();
                break;
            case AsyncEscPosPrint.FINISH_PRINTER_DISCONNECTED:
                new AlertDialog.Builder(context)
                    .setTitle("Sin conexión")
                    .setMessage("No se puede conectar la impresora.")
                    .show();
                break;
            case AsyncEscPosPrint.FINISH_PARSER_ERROR:
                new AlertDialog.Builder(context)
                    .setTitle("Texto con formato no válido")
                    .setMessage("Parece ser un problema de sintaxis no válida.")
                    .show();
                break;
            case AsyncEscPosPrint.FINISH_ENCODING_ERROR:
                new AlertDialog.Builder(context)
                    .setTitle("Codificación mal seleccionada")
                    .setMessage("El carácter de codificación seleccionado que devuelve un error.")
                    .show();
                break;
            case AsyncEscPosPrint.FINISH_BARCODE_ERROR:
                new AlertDialog.Builder(context)
                    .setTitle("Código de barras no válido")
                    .setMessage("El envío de datos para convertir a código de barras o código QR parece no ser válido.")
                    .show();
                break;
        }
    }
}
