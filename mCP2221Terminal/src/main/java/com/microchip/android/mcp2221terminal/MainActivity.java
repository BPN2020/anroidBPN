/*
 * Copyright (C) 2014 Microchip Technology Inc. and its subsidiaries. You may use this software and
 * any derivatives exclusively with Microchip products.
 *
 * THIS SOFTWARE IS SUPPLIED BY MICROCHIP "AS IS". NO WARRANTIES, WHETHER EXPRESS, IMPLIED OR
 * STATUTORY, APPLY TO THIS SOFTWARE, INCLUDING ANY IMPLIED WARRANTIES OF NON-INFRINGEMENT,
 * MERCHANTABILITY, AND FITNESS FOR A PARTICULAR PURPOSE, OR ITS INTERACTION WITH MICROCHIP
 * PRODUCTS, COMBINATION WITH ANY OTHER PRODUCTS, OR USE IN ANY APPLICATION.
 *
 * IN NO EVENT WILL MICROCHIP BE LIABLE FOR ANY INDIRECT, SPECIAL, PUNITIVE, INCIDENTAL OR
 * CONSEQUENTIAL LOSS, DAMAGE, COST OR EXPENSE OF ANY KIND WHATSOEVER RELATED TO THE SOFTWARE,
 * HOWEVER CAUSED, EVEN IF MICROCHIP HAS BEEN ADVISED OF THE POSSIBILITY OR THE DAMAGES ARE
 * FORESEEABLE. TO THE FULLEST EXTENT ALLOWED BY LAW, MICROCHIP'S TOTAL LIABILITY ON ALL CLAIMS IN
 * ANY WAY RELATED TO THIS SOFTWARE WILL NOT EXCEED THE AMOUNT OF FEES, IF ANY, THAT YOU HAVE PAID
 * DIRECTLY TO MICROCHIP FOR THIS SOFTWARE.
 *
 * MICROCHIP PROVIDES THIS SOFTWARE CONDITIONALLY UPON YOUR ACCEPTANCE OF THESE TERMS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.microchip.android.mcp2221terminal;

import android.R.drawable;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.microchip.android.mcp2221comm.Mcp2221Comm;
import com.microchip.android.mcp2221comm.Mcp2221Config;
import com.microchip.android.mcp2221comm.Mcp2221Constants;
import com.microchip.android.microchipusb.Constants;
import com.microchip.android.microchipusb.MCP2221;
import com.microchip.android.microchipusb.MicrochipUsb;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

/**
 * MCP2221 Terminal App main.
 */
public class MainActivity extends Activity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks {
    private static final String HELP_FRAGMENT = "HELP_FRAGMENT";
    private static final String I2C_TERMINAL = "I2C_TERMINAL";
    private static final String PIN_CONFIG_FRAGMENT = "PIN_CONFIG_FRAGMENT";
    private static final String PIN_FUNCTIONS_FRAGMENT = "PIN_FUNCTIONS_FRAGMENT";
    private static final String SERIAL_TERMINAL_FRAGMENT = "SERIAL_TERMINAL_FRAGMENT";
    private DrawerLayout mDrawerLayout;
    private static View mFragmentView;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private EULA eulaDialog;
    private static String myAddress = "90";
    private static String myNubmerOfBytes = "2";
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String T1;
    private String T2;
    private Mcp2221Config mMcp2221Config;
    byte[] pinDesignation = new byte[4];
    byte[] pinDirection = new byte[4];
    byte[] pinValue = new byte[4];


    private static final byte FUNCTION_GPIO = 0;
    private static final byte OUTPUT = 0;
    private static final byte LOW = 0;
    private static final byte HIGH = 1;


    /**
     * Microchip Product ID.
     */
    protected static final int MCP2221_PID = 0xDD;
    /**
     * Microchip Vendor ID.
     */
    protected static final int MCP2221_VID = 0x4D8;
    /**
     * TAG to be used for logcat.
     */
    protected static final String TAG = "MainActivity";
    /**
     * Custom toast - displayed in the center of the screen.
     */
    private static Toast sToast;
    /**
     * USB permission action for the USB broadcast receiver.
     */
    private static final String ACTION_USB_PERMISSION = "com.microchip.android.USB_PERMISSION";
    /**
     * public member to be used in the test project.
     */
    public MCP2221 mcp2221;
    /**
     * public member to be used in the test project.
     */
    public Mcp2221Comm mcp2221Comm;
    /**
     * Customized dialog fragment to clear the Data field.
     */
    private static ClearDialogFragment mClearDataDialog;
    /**
     * Customized dialog fragment to clear the Output field.
     */
    private static ClearDialogFragment mClearOutputDialog;
    /**
     * Pending Intent for requesting USB permission.
     */
    private PendingIntent mPermissionIntent;
    /**
     * Menu item. Used to update the connection status icon when a MCP2221 is attached/dettached
     */
    private Menu mMenu;
    private static TextView result1_text_view;
    private static TextView result2_text_view;
    private static Spinner rate_spinner;
    private static Button start_btn;
    private static Button stop_btn;
    private boolean stopFlag = false;
    Handler handler = new Handler();
    Runnable start_runnable;
    private static TextView debug_text_view;
    private static EditText desc_inp;
    public static boolean isHold = false;


    /*********************************************************
     * USB actions broadcast receiver.
     *********************************************************/
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            try {
                final String action = intent.getAction();

                if (ACTION_USB_PERMISSION.equals(action)) {
                    synchronized (this) {
                        final UsbDevice device =
                                (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                        // is usb permission has been granted, try to open a connection
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            if (device != null) {
                                // call method to set up device communication
                                final Constants result = mcp2221.open();

                                if (result != Constants.SUCCESS) {
                                    sToast.setText("Could not open MCP2221 connection");
                                    sToast.show();
                                } else {
                                    mcp2221Comm = new Mcp2221Comm(mcp2221);
                                    sToast.setText("MCP2221 connection opened");
                                    sToast.show();

                                    // if the nav drawer isn't displayed, update the icon
                                    if (!mNavigationDrawerFragment.isDrawerOpen()) {
                                        mMenu.findItem(R.id.action_connection_status).setIcon(
                                                drawable.presence_online);
                                    }
                                    updateFragments();
                                }
                            }
                        } else {
                            sToast.setText("USB Permission Denied");
                            sToast.show();
                        }
                    }
                }

                if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                    sToast.setText("Device Detached");
                    sToast.show();
                    // close the connection and
                    // release all resources
                    mcp2221.close();
                    // leave a bit of time for the COM thread to close
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        // e.printStackTrace();
                    }
                    mcp2221Comm = null;
                    // if the nav drawer isn't open change the action bar icon to show the device is
                    // detached
                    if (!mNavigationDrawerFragment.isDrawerOpen()) {
                        mMenu.findItem(R.id.action_connection_status).setIcon(
                                drawable.ic_notification_overlay);
                    }
                    updateFragments();

                }

                if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                    sToast.setText("Device Attached");
                    sToast.show();
                    final UsbDevice device =
                            (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device != null) {

                        // only try to connect if an MCP2221 is attached
                        if (device.getVendorId() == MCP2221_VID && device.getProductId() == MCP2221_PID) {
                            final Constants result = mcp2221.open();

                            switch (result) {
                                case SUCCESS:
                                    sToast.setText("MCP2221 Connection Opened");
                                    sToast.show();
                                    mcp2221Comm = new Mcp2221Comm(mcp2221);

                                    // if the nav drawer isn't open change the menu icon to show the MCP
                                    // is connected
                                    if (!mNavigationDrawerFragment.isDrawerOpen()) {
                                        mMenu.findItem(R.id.action_connection_status).setIcon(
                                                drawable.presence_online);
                                    }
                                    break;
                                case CONNECTION_FAILED:
                                    sToast.setText("Connection Failed");
                                    sToast.show();
                                    break;
                                case NO_USB_PERMISSION:
                                    mcp2221.requestUsbPermission(mPermissionIntent);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
            }catch (Exception e){
                debug_text_view.setText(e.getMessage());
            }

        }
    };

    /*********************************************************
     * Send Button Press event handler.
     *********************************************************/
    public void buttonStartClick(final View v) {
        buttonStopClick(v);
        if (desc_inp.getText() == null || desc_inp.getText().toString().equals("")) {
            sToast.setText("Description is Empty!");
            sToast.show();
            return;
        }
        if (mcp2221Comm == null) {
            sToast.setText("No MCP2221 Connected");
            sToast.show();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DBHelper dbHelper = new DBHelper(getApplicationContext());
                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    // Create a new map of values, where column names are the keys
                    ContentValues values_name = new ContentValues();
                    values_name.put(DB.TestNameEntry.COLUMN_NAME_TITLE, desc_inp.getText().toString());
                    values_name.put(DB.TestNameEntry.COLUMN_NAME_DATE, DB.formatter.format(new Date(System.currentTimeMillis())));

                    long newRowId = db.insert(DB.TestNameEntry.TABLE_NAME, null, values_name);
                } catch (Exception e) {
                }
            }
        }).start();

        final int position = rate_spinner.getSelectedItemPosition() + 1;
        getSample(position, v, true);

    }

    public void getSample(final int rate, final View v, final boolean saveToDataBase) {
        handler.postDelayed(start_runnable = new Runnable() {
            public void run() {
                try {
                    if (mcp2221Comm == null) {
                        sToast.setText("No MCP2221 Connected");
                        sToast.show();
//                        buttonStopClick(v);
                        return;
                    }
                    mcp2221Comm.setGpPinValue((byte) 0, (byte) 1);
                    mcp2221Comm.setGpPinValue((byte) 1, (byte) 0);
                    T1 = buttonStartSample(v, "90", result1_text_view);
                    handler.postDelayed(start_runnable, rate * 1000);
                    mcp2221Comm.setGpPinValue((byte) 0, (byte) 1);
                    mcp2221Comm.setGpPinValue((byte) 1, (byte) 1);
                    write(v, "02,00");
                    write(v, "01");
                    T2 = buttonStartSample(v, "80", result2_text_view);
                    if (saveToDataBase) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    DBHelper dbHelper = new DBHelper(getApplicationContext());
                                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                                    // insert to test
                                    // Create a new map of values, where column names are the keys
                                    ContentValues values = new ContentValues();
                                    values.put(DB.TestEntry.COLUMN_NAME_TITLE, desc_inp.getText().toString());
                                    values.put(DB.TestEntry.COLUMN_NAME_DATE, DB.formatter.format(new Date(System.currentTimeMillis())));
                                    values.put(DB.TestEntry.COLUMN_NAME_T1, T1);
                                    values.put(DB.TestEntry.COLUMN_NAME_T2, T2);

                                    // Insert the new row, returning the primary key value of the new row
                                    long newRowId = db.insert(DB.TestEntry.TABLE_NAME, null, values);
                                    debug_text_view.setText(newRowId + "");
                                } catch (Exception e) {

                                }
                            }
                        }
                        ).start();
                    }
                } catch (Exception e) {
                    debug_text_view.setText(e.getMessage());
                }
            }
        }, 0);

    }

    public String buttonStartSample(final View v, String maddress, TextView shower) {
        try {
            int result;
            ByteBuffer readData;
            ByteBuffer txData;
            int speed;
            int bytesToRead;
            byte address;
            byte regIndex;
            byte usesPec;

            final StringBuilder dataString = new StringBuilder("");

            // hide soft keyboard if it's displayed
            if (v != null) {
                final InputMethodManager imm =
                        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }


            if (true) {

                // if we got here, all input fields are valid
                // get the data to be transmitted
                address = Cmd.getAddress(maddress);
                txData = Cmd.getData("2");
                speed = Cmd.getSpeed();

                if (true) {
                    // populate the data string for the output
                    for (int i = 0; i < txData.limit(); i++) {
                        dataString.append(String.format("%02X", txData.get(i)));

                        // if we haven't reached the last item
                        // append a ","
                        if (i < txData.limit() - 1) {
                            dataString.append(",");
                        }

                    }
                } else {
                    // populate the data string for the output
                    // the first data byte for smbus is the register index, so
                    // don't print it
                    for (int i = 1; i < txData.limit(); i++) {
                        dataString.append(String.format("%02X", txData.get(i)));

                        // if we haven't reached the last item
                        // append a ","
                        if (i < txData.limit() - 1) {
                            dataString.append(",");
                        }
                    }
                }

                // the value in the data field = the number of bytes to read
                // allocate the readData buffer based on that size
                //bytesToRead = Integer.parseInt(mTxtData.getText().toString(), 16);
                bytesToRead = Integer.parseInt(myNubmerOfBytes, 16);
                ;
                readData = ByteBuffer.allocate(bytesToRead);

                // make sure the address is odd
                address |= 0x01;

                result = mcp2221Comm.readI2cData(address, readData, bytesToRead, speed);

                // if the read was successful, display the data
                if (result == Mcp2221Constants.ERROR_SUCCESSFUL) {
                    final String output_text = Output.formatText(readData, Output.DARK_GREEN, maddress).toString();
                    shower.setText(output_text);
                    return output_text;
                }
            }
        } catch (Exception e) {
            debug_text_view.setText(e.getMessage());
        }
        return "";
    }

    public void buttonStopClick(final View v) {
        if (start_runnable != null && handler != null)
            handler.removeCallbacks(start_runnable);
    }

    public void buttonSendOnClick(final View v) {

        int result;
        ByteBuffer readData;
        ByteBuffer txData;
        int speed;
        int bytesToRead;
        byte address;
        byte regIndex;
        byte usesPec;

        final StringBuilder dataString = new StringBuilder("");

        // hide soft keyboard if it's displayed
        final InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        //if (mSpinnerOperation.getSelectedItem().toString().contains("I2C")) {
        if (true) {
            // check that the needed fields aren't empty
//            if (mTxtAddress.getText().toString().isEmpty()
//                    || mTxtData.getText().toString().isEmpty()) {
//
//                if (mTxtAddress.getText().toString().isEmpty()) {
//                    mTxtAddress.setError("Address cannot be empty");
//                }
//
//                if (mTxtData.getText().toString().isEmpty()) {
//                    mTxtData.setError("Data cannot be empty");
//                }
//                sToast.setText("Error: Empty fields");
//                sToast.show();
//                return;
//            } else if (mTxtAddress.getError() != null || mTxtData.getError() != null) {
//                // if any of the fields have an error, don't send
//                // anything
//                sToast.setText("Error: Invalid values detected.");
//                sToast.show();
//                return;
//            }
//        } else if (mTxtAddress.getText().toString().isEmpty()
//                || mTxtData.getText().toString().isEmpty()
//                || mTxtRegIndex.getText().toString().isEmpty()) {
//
//            // if we have an smbus operation
//            // check that the needed fields aren't empty
//
//            if (mTxtAddress.getText().toString().isEmpty()) {
//                mTxtAddress.setError("Address cannot be empty");
//            }
//
//            if (mTxtData.getText().toString().isEmpty()) {
//                mTxtData.setError("Data cannot be empty");
//            }
//
//            if (mTxtRegIndex.getText().toString().isEmpty()) {
//                mTxtRegIndex.setError("Reg. index cannot be empty");
//            }
//            sToast.setText("Error: Empty fields");
//            sToast.show();
//            return;
//        } else if (mTxtAddress.getError() != null || mTxtData.getError() != null
//                || mTxtRegIndex.getError() != null) {
//            // / if any of the fields have an error, don't send
//            // anything
//            sToast.setText("Error: Invalid values detected.");
//            sToast.show();
//            return;
//        }

            // check that we have a connection open to a device
            if (mcp2221Comm == null) {
                sToast.setText("No MCP2221 Connected");
                sToast.show();
                return;
            }

            // if we got here, all input fields are valid
            // get the data to be transmitted
            address = Cmd.getAddress("90");
            txData = Cmd.getData("2");
            speed = Cmd.getSpeed();

            // there's a chance that the data field will contain only "," and no
            // valid data
            // so perform a final check here
//        if (txData.capacity() == 0) {
//            mTxtData.setError("Invalid Data");
//            return;
//        }

            //if (mSpinnerOperation.getSelectedItem().toString().contains("I2C")) {
            if (true) {
                // populate the data string for the output
                for (int i = 0; i < txData.limit(); i++) {
                    dataString.append(String.format("%02X", txData.get(i)));

                    // if we haven't reached the last item
                    // append a ","
                    if (i < txData.limit() - 1) {
                        dataString.append(",");
                    }

                }
            } else {
                // populate the data string for the output
                // the first data byte for smbus is the register index, so
                // don't print it
                for (int i = 1; i < txData.limit(); i++) {
                    dataString.append(String.format("%02X", txData.get(i)));

                    // if we haven't reached the last item
                    // append a ","
                    if (i < txData.limit() - 1) {
                        dataString.append(",");
                    }
                }
            }

            // the value in the data field = the number of bytes to read
            // allocate the readData buffer based on that size
            //bytesToRead = Integer.parseInt(mTxtData.getText().toString(), 16);
            bytesToRead = Integer.parseInt(myNubmerOfBytes, 16);
            ;
            readData = ByteBuffer.allocate(bytesToRead);

            // make sure the address is odd
            address |= 0x01;

            result = mcp2221Comm.readI2cData(address, readData, bytesToRead, speed);

            result1_text_view.append(Output.formatText("> I2C read (" + bytesToRead
                            + " bytes), Address = " + myAddress + "\n",
                    Color.BLACK));

            result1_text_view.append(Output.processErrorMessage(result));

            // if the read was successful, display the data
            if (result == Mcp2221Constants.ERROR_SUCCESSFUL) {
                result1_text_view.append("< " + Output.formatText(readData, Output.DARK_GREEN, "90"));
            }
//        switch (mSpinnerOperation.getSelectedItem().toString().toLowerCase(Locale.US)) {
//
//            case "i2c write":
//
//                // make sure the address is even
//                address &= 0xFE;
//
//                result = mcp2221Comm.writeI2cdata(address, txData, txData.limit(), speed);
//
//                mTxtOutput.append(Output.formatText("> I2C write, Address = "
//                        + mTxtAddress.getText().toString() + " Data: " + dataString.toString()
//                        + "\n", Color.BLACK));
//
//                // display the result of the operation
//                mTxtOutput.append(Output.processErrorMessage(result));
//                break;
//
//            case "i2c read":
//
//                // the value in the data field = the number of bytes to read
//                // allocate the readData buffer based on that size
//                bytesToRead = Integer.parseInt(mTxtData.getText().toString(), 16);
//
//                readData = ByteBuffer.allocate(bytesToRead);
//
//                // make sure the address is odd
//                address |= 0x01;
//
//                result = mcp2221Comm.readI2cData(address, readData, bytesToRead, speed);
//
//                mTxtOutput.append(Output.formatText("> I2C read (" + bytesToRead
//                        + " bytes), Address = " + mTxtAddress.getText().toString() + "\n",
//                        Color.BLACK));
//
//                mTxtOutput.append(Output.processErrorMessage(result));
//
//                // if the read was successful, display the data
//                if (result == Mcp2221Constants.ERROR_SUCCESSFUL) {
//                    mTxtOutput.append("< " + Output.formatText(readData, Output.DARK_GREEN));
//                }
//                break;
//
//            case " smbus write":
//
//                // for smbus the first entry in the data array is the register
//                // index value
//                usesPec = Cmd.getUsesPec();
//
//                result = mcp2221Comm.smbWriteBlock(address, txData, txData.limit(), speed, usesPec);
//
//                mTxtOutput.append(Output.formatText("> SMBus write, Address = "
//                        + mTxtAddress.getText().toString() + ", Reg. Index: "
//                        + mTxtRegIndex.getText().toString() + ", Data: " + dataString.toString()
//                        + "\n", Color.BLACK));
//
//                mTxtOutput.append(Output.processErrorMessage(result));
//
//                break;
//
//            case "smbus read":
//                regIndex = Cmd.getRegIndex();
//                usesPec = Cmd.getUsesPec();
//                // first byte of the data is the register index
//                // second item is the number of bytes to read back
//                bytesToRead = Integer.parseInt(mTxtData.getText().toString(), 16);
//                readData = ByteBuffer.allocate(bytesToRead);
//
//                result =
//                        mcp2221Comm.smbReadBlock(address, readData, bytesToRead, speed, usesPec,
//                                regIndex);
//
//                mTxtOutput.append(Output.formatText("> SMBus read (" + bytesToRead + " bytes), "
//                        + "Address = " + mTxtAddress.getText().toString() + ", Reg. Index: "
//                        + mTxtRegIndex.getText().toString() + "\n", Color.BLACK));
//
//                mTxtOutput.append(Output.processErrorMessage(result));
//
//                if (result == 0) {
//                    mTxtOutput.append("< " + Output.formatText(readData, Output.DARK_GREEN));
//                }
//                break;
//
//            default:
//                break;
//        }

        }
    }

    private int write(final View v, String data) {
        int result;
        ByteBuffer readData;
        ByteBuffer txData;
        int speed;
        int bytesToRead;
        byte address;
        byte regIndex;
        byte usesPec;

        final StringBuilder dataString = new StringBuilder("");

        // hide soft keyboard if it's displayed
        if (v != null) {
            final InputMethodManager imm =
                    (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
        // check that we have a connection open to a device
        if (mcp2221Comm == null) {
            sToast.setText("No MCP2221 Connected");
            sToast.show();
            return -100;
        }

        // if we got here, all input fields are valid
        // get the data to be transmitted
        address = Cmd.getAddress("80");
        txData = Cmd.getData(data);
        speed = Cmd.getSpeed();

        if (true) {
            // populate the data string for the output
            for (int i = 0; i < txData.limit(); i++) {
                dataString.append(String.format("%02X", txData.get(i)));

                // if we haven't reached the last item
                // append a ","
                if (i < txData.limit() - 1) {
                    dataString.append(",");
                }

            }
        } else {
            // populate the data string for the output
            // the first data byte for smbus is the register index, so
            // don't print it
            for (int i = 1; i < txData.limit(); i++) {
                dataString.append(String.format("%02X", txData.get(i)));

                // if we haven't reached the last item
                // append a ","
                if (i < txData.limit() - 1) {
                    dataString.append(",");
                }
            }
        }

        // make sure the address is even
        address &= 0xFE;

        result = mcp2221Comm.writeI2cdata(address, txData, txData.limit(), speed);

        result2_text_view.setText(Output.processErrorMessage(result));
        return result;

    }

//    private void setPinSelections(String first, String second) {
//        // will contain the spinner function selection
//        StringBuilder spinnerSelection = new StringBuilder();
//
//
//        if (mMcp2221Config == null) {
//            mMcp2221Config = new Mcp2221Config();
//        }
//
//        // **************************
//        // GP0 functions
//        // **************************
//        spinnerSelection.append("output");
//        pinDesignation[0] = FUNCTION_GPIO;
//        pinDirection[0] = OUTPUT;
//
//        if (first.equalsIgnoreCase("high")) {
//            pinValue[0] = HIGH;
//        } else {
//            pinValue[0] = LOW;
//        }
//
////        switch (spinnerSelection.toString().toLowerCase(Locale.US)) {
////            case "output":
////                pinDesignation[0] = FUNCTION_GPIO;
////                pinDirection[0] = OUTPUT;
////
////                if (first.equalsIgnoreCase("high")) {
////                    pinValue[0] = HIGH;
////                } else {
////                    pinValue[0] = LOW;
////                }
////                break;
////
////            case "input":
////                pinDesignation[0] = FUNCTION_GPIO;
////                pinDirection[0] = INPUT;
////                break;
////
////            case "sspnd":
////                pinDesignation[0] = FUNCTION_DEDICATED;
////                break;
////
////            case "led urx":
////                pinDesignation[0] = FUNCTION_ALTERNATE_0;
////                break;
////
////            default:
////                break;
////        }
////
//        // **************************
//        // GP1 functions
//        // **************************
//
//        // clear previous selection
//        spinnerSelection.setLength(0);
//        spinnerSelection.append("output");
//
//        pinDesignation[1] = FUNCTION_GPIO;
//        pinDirection[1] = OUTPUT;
//
//        if (second.equalsIgnoreCase("high")) {
//            pinValue[1] = HIGH;
//        } else {
//            pinValue[1] = LOW;
//        }
//        pinDirection[2] = pinDirection[3] = 0;
//        pinValue[2] = pinValue[3] = 0;
//        pinDesignation[2] = pinDesignation[3] = 0;
//
////        switch (spinnerSelection.toString().toLowerCase(Locale.US)) {
////            case "output":
////                pinDesignation[1] = FUNCTION_GPIO;
////                pinDirection[1] = OUTPUT;
////
////                if (second.equalsIgnoreCase("high")) {
////                    pinValue[1] = HIGH;
////                } else {
////                    pinValue[1] = LOW;
////                }
////                break;
////
////            case "input":
////                pinDesignation[1] = FUNCTION_GPIO;
////                pinDirection[1] = INPUT;
////                break;
////
////            case "clk out":
////                pinDesignation[1] = FUNCTION_DEDICATED;
////                break;
////
////            case "adc1":
////                pinDesignation[1] = FUNCTION_ALTERNATE_0;
////                break;
////
////            case "led utx":
////                pinDesignation[1] = FUNCTION_ALTERNATE_1;
////                break;
////
////            case "ioc":
////                pinDesignation[1] = FUNCTION_ALTERNATE_2;
////                break;
////            default:
////                break;
////        }
////
//        // **************************
//        // GP2 functions
//        // **************************
//
//        // clear previous selection
////        spinnerSelection.setLength(0);
////        spinnerSelection.append("usbcfg");
////
//////        switch (spinnerSelection.toString().toLowerCase(Locale.US)) {
//////            case "output":
//////                pinDesignation[2] = FUNCTION_GPIO;
//////                pinDirection[2] = OUTPUT;
//////
//////                if (spinnerGp2Levels.getItemAtPosition(spinnerGp2Levels.getSelectedItemPosition())
//////                        .toString().equalsIgnoreCase("high")) {
//////                    pinValue[2] = HIGH;
//////                } else {
//////                    pinValue[2] = LOW;
//////                }
//////                break;
//////
//////            case "input":
//////                pinDesignation[2] = FUNCTION_GPIO;
//////                pinDirection[2] = INPUT;
//////                break;
//////
//////            case "usbcfg":
//////                pinDesignation[2] = FUNCTION_DEDICATED;
//////                break;
//////
//////            case "adc2":
//////                pinDesignation[2] = FUNCTION_ALTERNATE_0;
//////                break;
//////
//////            case "dac1":
//////                pinDesignation[2] = FUNCTION_ALTERNATE_1;
//////                break;
//////
//////            default:
//////                break;
//////        }
////
////        // **************************
////        // GP3 functions
////        // **************************
////
////        // clear previous selection
////        spinnerSelection.setLength(0);
////        spinnerSelection.append(spinnerGp3Functions.getItemAtPosition(
////                spinnerGp3Functions.getSelectedItemPosition()).toString());
////
////        switch (spinnerSelection.toString().toLowerCase(Locale.US)) {
////            case "output":
////                pinDesignation[3] = FUNCTION_GPIO;
////                pinDirection[3] = OUTPUT;
////
////                if (spinnerGp3Levels.getItemAtPosition(spinnerGp3Levels.getSelectedItemPosition())
////                        .toString().equalsIgnoreCase("high")) {
////                    pinValue[3] = HIGH;
////                } else {
////                    pinValue[3] = LOW;
////                }
////                break;
////
////            case "input":
////                pinDesignation[3] = FUNCTION_GPIO;
////                pinDirection[3] = INPUT;
////                break;
////
////            case "led i2c":
////                pinDesignation[3] = FUNCTION_DEDICATED;
////                break;
////
////            case "adc3":
////                pinDesignation[3] = FUNCTION_ALTERNATE_0;
////                break;
////
////            case "dac2":
////                pinDesignation[3] = FUNCTION_ALTERNATE_1;
////                break;
////
////            default:
////                break;
////        }
//
//        // update the connection status before using the object
//        Mcp2221Comm mMcp2221Comm = mcp2221Comm;
//
//        if (mMcp2221Comm == null) {
//            sToast.setText("No MCP2221 Connected");
//            sToast.show();
//            return;
//        } else {
//
//            mMcp2221Config.setGpPinDesignations(pinDesignation);
//            mMcp2221Config.setGpPinDirections(pinDirection);
//            mMcp2221Config.setGpPinValues(pinValue);
//
//            // update the GP settings
//            if (mMcp2221Comm.setSRamSettings(mMcp2221Config, false, false, false, false, false,
//                    false, true) == Mcp2221Constants.ERROR_SUCCESSFUL) {
//                sToast.setText("MCP2221 Pin Configuration successfully updated.");
//                sToast.show();
//            } else {
//                sToast.setText("Could not update MCP2221 Pin Configuration.");
//                sToast.show();
//            }
//
//        }
//
//    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // implemented so the OnCreate isn't called each time the screen
        // orientation changes, thus avoiding repeated "MCP2221 Connected"
        // messages
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        this.mMenu = menu;

        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);

            if (mcp2221Comm != null) {
                // if a MCP2221 is connected, update the status icon to reflect this.
                menu.findItem(R.id.action_connection_status).setIcon(drawable.presence_online);
            }

            restoreActionBar();
            // return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        final int id = item.getItemId();
        return super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        // TODO Auto-generated method stub
        return super.onPrepareOptionsMenu(menu);

    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // show the license agreement on the first run
        // new EULA(this).show();
        eulaDialog = new EULA(this);
        eulaDialog.show();

        mNavigationDrawerFragment =
                (NavigationDrawerFragment) getFragmentManager().findFragmentById(
                        R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // set up the custom toast; This will appear in the center of the
        // screen and not the default position
        sToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        sToast.setGravity(Gravity.CENTER, 0, 0);

        mPermissionIntent =
                PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        final IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);

        registerReceiver(mUsbReceiver, filter);

        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        registerReceiver(mUsbReceiver, filter);

        Constants result = null;

        mcp2221 = new MCP2221(this);
        result = MicrochipUsb.getConnectedDevice(this);

        if (result == Constants.MCP2221) {
            // try to open a connection
            result = mcp2221.open();

            switch (result) {
                case SUCCESS:
                    sToast.setText("MCP2221 connected");
                    sToast.show();
                    mcp2221Comm = new Mcp2221Comm(mcp2221);
                    break;
                case CONNECTION_FAILED:
                    sToast.setText("Connection failed");
                    sToast.show();
                    break;
                case NO_USB_PERMISSION:
                    mcp2221.requestUsbPermission(mPermissionIntent);
                    break;
                default:
                    break;
            }
        }

        // provide a reference for the main activity, so we have access
        // to the UI elements from the Cmd class
        Cmd.setActivity(this);
        //getSample(1,null,false);
    }

    // ............................

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // close the connection and release all resources
        unregisterReceiver(mUsbReceiver);
        buttonStopClick(null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        buttonStopClick(null);
    }

    /*
      Experiments

     */
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment myFragment;

        switch (position) {
            case 1:

                // if the fragment has been displayed before, don't recreate it, just update the
                // view
                myFragment = fragmentManager.findFragmentByTag(I2C_TERMINAL);
                if (myFragment == null) {
                    transaction.replace(R.id.container,
                            I2cTerminalFragment.newInstance(position + 1), I2C_TERMINAL);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    transaction.addToBackStack(I2C_TERMINAL);
                    transaction.commit();
                } else if (myFragment.isVisible() == false) {

                    transaction.replace(R.id.container, myFragment, I2C_TERMINAL);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    transaction.addToBackStack(I2C_TERMINAL);
                    transaction.commit();
                    // update the title as well
                    mTitle = getString(R.string.title_section1_i2c_terminal);
                }

                break;
            case 0:
                try {
                    // if the fragment has been displayed before, don't recreate it, just update the
                    // view
                    myFragment = fragmentManager.findFragmentByTag(PIN_CONFIG_FRAGMENT);
                    if (myFragment == null) {
                        transaction.replace(R.id.container,
                                DefaultFragment.newInstance(position + 1), PIN_CONFIG_FRAGMENT);
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        transaction.addToBackStack(PIN_CONFIG_FRAGMENT);
                        transaction.commit();
                    } else if (myFragment.isVisible() == false) {

                        transaction.replace(R.id.container, myFragment, PIN_CONFIG_FRAGMENT);
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        transaction.addToBackStack(PIN_CONFIG_FRAGMENT);
                        transaction.commit();
                        // update the title as well
                        mTitle = getString(R.string.title_section2_pin_config);
                    }
                }catch (Exception e){
                    debug_text_view.setText(e.getMessage());
                }

                break;
            case 2:
                try {
                    // if the fragment has been displayed before, don't recreate it, just update the
                    // view
                    myFragment = fragmentManager.findFragmentByTag(PIN_FUNCTIONS_FRAGMENT);
                    if (myFragment == null) {
                        transaction.replace(R.id.container,
                                PinFunctionsFragment.newInstance(position + 1), PIN_FUNCTIONS_FRAGMENT);
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        transaction.addToBackStack(PIN_FUNCTIONS_FRAGMENT);
                        transaction.commit();

                    } else if (!myFragment.isVisible()) {

                        transaction.replace(R.id.container, myFragment, PIN_FUNCTIONS_FRAGMENT);
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        transaction.addToBackStack(PIN_FUNCTIONS_FRAGMENT);
                        transaction.commit();
                        // update the title as well
                        mTitle = getString(R.string.title_section3_pin_functions);
                    }
                }catch (Exception e){
                    debug_text_view.setText(e.getMessage());
                }
                break;
            case 3:
                // TODO add serial terminal fragment here
                myFragment = fragmentManager.findFragmentByTag(SERIAL_TERMINAL_FRAGMENT);
                if (myFragment == null) {
                    transaction.replace(R.id.container,
                            SerialTerminalFragment.newInstance(position + 1),
                            SERIAL_TERMINAL_FRAGMENT);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    transaction.addToBackStack(HELP_FRAGMENT);
                    transaction.commit();
                } else if (myFragment.isVisible() == false) {

                    transaction.replace(R.id.container, myFragment, SERIAL_TERMINAL_FRAGMENT);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    transaction.addToBackStack(SERIAL_TERMINAL_FRAGMENT);
                    transaction.commit();
                    // update the title as well
                    mTitle = getString(R.string.title_section4_serial_terminal);
                }
                break;

            case 4:
                myFragment = fragmentManager.findFragmentByTag(HELP_FRAGMENT);
                if (myFragment == null) {
                    transaction.replace(R.id.container, HelpFragment.newInstance(position + 1),
                            HELP_FRAGMENT);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    transaction.addToBackStack(HELP_FRAGMENT);
                    transaction.commit();
                } else if (myFragment.isVisible() == false) {

                    transaction.replace(R.id.container, myFragment, HELP_FRAGMENT);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    transaction.addToBackStack(HELP_FRAGMENT);
                    transaction.commit();
                    // update the title as well
                    mTitle = getString(R.string.title_section5_help);
                }
                break;

            default:
                break;
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 2:
                mTitle = getString(R.string.title_section1_i2c_terminal);
                break;
            case 1:
                mTitle = getString(R.string.title_section2_pin_config);
                break;
            case 3:
                mTitle = getString(R.string.title_section3_pin_functions);
                break;
            case 4:
                mTitle = getString(R.string.title_section4_serial_terminal);
                break;
            case 5:
                mTitle = getString(R.string.title_section5_help);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class I2cTerminalFragment extends Fragment implements TextWatcher {
        /**
         * The fragment argument representing the section number for this fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section number.
         */
        public static I2cTerminalFragment newInstance(int sectionNumber) {
            I2cTerminalFragment fragment = new I2cTerminalFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public I2cTerminalFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.main_show, container, false);

            result1_text_view = (TextView) rootView.findViewById(R.id.result1_tv);
            result2_text_view = (TextView) rootView.findViewById(R.id.result2_tv);

            // set spinner
            rate_spinner = (Spinner) rootView.findViewById(R.id.rate_spinner);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(container.getContext(),
                    R.array.rate_array, android.R.layout.simple_spinner_item);
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            rate_spinner.setAdapter(adapter);

            //set start button
            start_btn = (Button) rootView.findViewById(R.id.start_btn);
            stop_btn = (Button) rootView.findViewById(R.id.stop_btn);
            debug_text_view = (TextView) rootView.findViewById(R.id.debug_tv);
            desc_inp = (EditText) rootView.findViewById(R.id.desc_inp);

            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
        }

        // /*********************************************************
        // * Text changed handlers All the text fields are validated here out of range values will
        // trigger
        // * an error message.
        // *********************************************************/
        //
        @Override
        public void afterTextChanged(final Editable s) {
            int addr = 0;
            int regIndex = 0;

        }

        /**
         * Not used.
         */
        @Override
        public void beforeTextChanged(final CharSequence s, final int start, final int count,
                                      final int after) {
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onResume() {
            super.onResume();
            // Set title
            getActivity().getActionBar().setTitle(R.string.title_section1_i2c_terminal);
        }

    }

    public static class DefaultFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section number.
         */
        public static DefaultFragment newInstance(int sectionNumber) {
            DefaultFragment fragment = new DefaultFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public DefaultFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.default_page, container, false);

            result1_text_view = (TextView) rootView.findViewById(R.id.result1_tv);
            result2_text_view = (TextView) rootView.findViewById(R.id.result2_tv);
            debug_text_view = (TextView) rootView.findViewById(R.id.debug_tv);
            Button hold = (Button) rootView.findViewById(R.id.hold);

            if (((MainActivity) getActivity()).mcp2221Comm == null) {
                sToast.setText("No MCP2221 Connected");
                sToast.show();

            } else {
                ((MainActivity) getActivity()).buttonStopClick(null);
                ((MainActivity) getActivity()).getSample(1, null, false);
            }
            hold.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                    if (((MainActivity) getActivity()).mcp2221Comm != null) {
                        if (!isHold) {
                            ((MainActivity) getActivity()).buttonStopClick(null);
                            isHold = true;
                        }
                        else {
                            isHold = false;
                            ((MainActivity) getActivity()).getSample(1, null, false);
                        }
                    }
                    }catch (Exception e){
                        debug_text_view.setText(e.getMessage());
                    }
                }
            });


            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
        }

        @Override
        public void onResume() {
            super.onResume();
            // Set title
            getActivity().getActionBar().setTitle(R.string.title_section2_pin_config);
        }

        @Override
        public void onStop() {
            super.onStop();
            ((MainActivity) getActivity()).buttonStopClick(null);
        }

        @Override
        public void onDetach() {
            super.onDetach();
            ((MainActivity) getActivity()).buttonStopClick(null);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FragmentManager fm = getFragmentManager();
        String stackName = null;
        for (int entry = 0; entry < fm.getBackStackEntryCount(); entry++) {
            stackName = fm.getBackStackEntryAt(entry).getName();
        }

        if (stackName == null) {
            this.finish();
            return;
        }
        mDrawerList = (ListView) findViewById(R.id.navigation_drawer);
        switch (stackName) {
            case I2C_TERMINAL:
                mDrawerList.setItemChecked(0, true);
                break;
            case PIN_CONFIG_FRAGMENT:
                mDrawerList.setItemChecked(1, true);
                break;
            case PIN_FUNCTIONS_FRAGMENT:
                mDrawerList.setItemChecked(2, true);
                break;
            case SERIAL_TERMINAL_FRAGMENT:
                mDrawerList.setItemChecked(3, true);
                break;
            case HELP_FRAGMENT:
                mDrawerList.setItemChecked(4, true);
                break;
            default:
                break;
        }

    }

    private void updateFragments() {
        // if the Pin Configuration fragment is visible, read the chip's
        // settings and update the fragment
//        DefaultFragment pinConfigFragment =
//                (DefaultFragment) getFragmentManager().findFragmentByTag(PIN_CONFIG_FRAGMENT);
//        if (pinConfigFragment != null) {
//            pinConfigFragment.updateConnectionStatus();
//            if (pinConfigFragment.isVisible()) {
//                pinConfigFragment.updatePinSelections();
//            }
//        }
//
//        // if the Pin Functions fragment is visible, read the chip's
//        // settings and update the fragment
//        PinFunctionsFragment pinFunctionsFragment =
//                (PinFunctionsFragment) getFragmentManager().findFragmentByTag(
//                        PIN_FUNCTIONS_FRAGMENT);
//        if (pinFunctionsFragment != null) {
//            // update the connection status as well
//            pinFunctionsFragment.updateConnectionStatus();
//            if (pinFunctionsFragment.isVisible()) {
//                pinFunctionsFragment.updatePinFunctions();
//                // sToast.setText("MCP2221 Pin Configuration read from device.");
//                // sToast.show();
//            }
//        }
//
//        // if the Serial Terminal fragment is visible update the mcp2221
//        // connection status
//        //
//        SerialTerminalFragment serialTerminalFragment =
//                (SerialTerminalFragment) getFragmentManager().findFragmentByTag(
//                        SERIAL_TERMINAL_FRAGMENT);
//        if (serialTerminalFragment != null) {
//            serialTerminalFragment.updateConnectionStatus();
//        }
    }
}
