package com.smartgeeks.busticket.core;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnections;
import com.dantsu.escposprinter.exceptions.EscPosConnectionException;

import java.io.IOException;
import java.util.UUID;

public class MyBluetoothPrintersConnections extends BluetoothConnections {

    private static final UUID PRINTER_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    /**
     * Easy way to get the first bluetooth printer paired / connected.
     *
     * @return a EscPosPrinterCommands instance
     */
    public static BluetoothConnection selectFirstPaired() {
        MyBluetoothPrintersConnections printers = new MyBluetoothPrintersConnections();
        BluetoothConnection[] bluetoothPrinters = printers.getList();

        if (bluetoothPrinters != null && bluetoothPrinters.length > 0) {
            for (BluetoothConnection printer : bluetoothPrinters) {
                try {
                    return printer.connect();
                } catch (EscPosConnectionException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    /**
     * Get a list of bluetooth printers.
     *
     * @return an array of EscPosPrinterCommands
     */
    @SuppressLint("MissingPermission")
    public BluetoothConnection[] getList() {
        BluetoothConnection[] bluetoothDevicesList = super.getList();

        if (bluetoothDevicesList == null) {
            return null;
        }

        int i = 0;
        BluetoothConnection[] printersTmp = new BluetoothConnection[bluetoothDevicesList.length];
        for (BluetoothConnection bluetoothConnection : bluetoothDevicesList) {
            BluetoothDevice device = bluetoothConnection.getDevice();

            int majDeviceCl = device.getBluetoothClass().getMajorDeviceClass(),
                    deviceCl = device.getBluetoothClass().getDeviceClass();

            if ((majDeviceCl == BluetoothClass.Device.Major.IMAGING && (deviceCl == 1664 || deviceCl == BluetoothClass.Device.Major.IMAGING))
                    || device.getName().equals("InnerPrinter")  || device.getAddress().equals("00:11:22:33:44:55") ) {
                printersTmp[i++] = new BluetoothConnection(device);
            }
        }
        BluetoothConnection[] bluetoothPrinters = new BluetoothConnection[i];
        System.arraycopy(printersTmp, 0, bluetoothPrinters, 0, i);
        return bluetoothPrinters;
    }

    @SuppressLint("MissingPermission")
    private static BluetoothSocket getSocket(BluetoothDevice device) throws IOException {
        BluetoothSocket socket;
        socket = device.createRfcommSocketToServiceRecord(PRINTER_UUID);
        socket.connect();
        return  socket;
    }


}