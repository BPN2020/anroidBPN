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
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.microchip.android.mcp2221comm.Mcp2221Comm;
import com.microchip.android.mcp2221comm.Mcp2221Constants;
import com.microchip.android.microchipusb.Constants;
import com.microchip.android.microchipusb.MCP2221;
import com.microchip.android.microchipusb.MicrochipUsb;

import java.nio.ByteBuffer;
import java.util.Locale;

/**
 * MCP2221 Terminal App main.
 * 
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

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    /** Microchip Product ID. */
    protected static final int MCP2221_PID = 0xDD;
    /** Microchip Vendor ID. */
    protected static final int MCP2221_VID = 0x4D8;
    /** TAG to be used for logcat. */
    protected static final String TAG = "MainActivity";
    /** Custom toast - displayed in the center of the screen. */
    private static Toast sToast;
    /** USB permission action for the USB broadcast receiver. */
    private static final String ACTION_USB_PERMISSION = "com.microchip.android.USB_PERMISSION";
    /** public member to be used in the test project. */
    public MCP2221 mcp2221;
    /** public member to be used in the test project. */
    public Mcp2221Comm mcp2221Comm;
    /** Customized dialog fragment to clear the Data field. */
    private static ClearDialogFragment mClearDataDialog;
    /** Customized dialog fragment to clear the Output field. */
    private static ClearDialogFragment mClearOutputDialog;
    /** Pending Intent for requesting USB permission. */
    private PendingIntent mPermissionIntent;
    /** Menu item. Used to update the connection status icon when a MCP2221 is attached/dettached */
    private Menu mMenu;
    /** Operation selection drop down (i2c/smbus read/write). */
    private static Spinner mSpinnerOperation;
    /** 7 or 8bit slave address length selection. */
    private static Spinner mSpinnerAddrLength;
    /** PEC on/off selection. */
    private static Spinner mSpinnerPEC;
    /** Slave address field. */
    private static EditText mTxtAddress;
    /** Data field. */
    private static EditText mTxtData;
    /** SMBus register index field. */
    private static EditText mTxtRegIndex;
    /** Output field. */
    private static EditText mTxtOutput;

    private static EditText mDebugOutput;


    /*********************************************************
     * USB actions broadcast receiver.
     *********************************************************/
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
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

        }
    };

    /*********************************************************
     * Send Button Press event handler.
     *********************************************************/

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

        if (mSpinnerOperation.getSelectedItem().toString().contains("I2C")) {
            // check that the needed fields aren't empty
            if (mTxtAddress.getText().toString().isEmpty()
                    || mTxtData.getText().toString().isEmpty()) {

                if (mTxtAddress.getText().toString().isEmpty()) {
                    mTxtAddress.setError("Address cannot be empty");
                }

                if (mTxtData.getText().toString().isEmpty()) {
                    mTxtData.setError("Data cannot be empty");
                }
                sToast.setText("Error: Empty fields");
                sToast.show();
                return;
            } else if (mTxtAddress.getError() != null || mTxtData.getError() != null) {
                // if any of the fields have an error, don't send
                // anything
                sToast.setText("Error: Invalid values detected.");
                sToast.show();
                return;
            }
        } else if (mTxtAddress.getText().toString().isEmpty()
                || mTxtData.getText().toString().isEmpty()
                || mTxtRegIndex.getText().toString().isEmpty()) {

            // if we have an smbus operation
            // check that the needed fields aren't empty

            if (mTxtAddress.getText().toString().isEmpty()) {
                mTxtAddress.setError("Address cannot be empty");
            }

            if (mTxtData.getText().toString().isEmpty()) {
                mTxtData.setError("Data cannot be empty");
            }

            if (mTxtRegIndex.getText().toString().isEmpty()) {
                mTxtRegIndex.setError("Reg. index cannot be empty");
            }
            sToast.setText("Error: Empty fields");
            sToast.show();
            return;
        } else if (mTxtAddress.getError() != null || mTxtData.getError() != null
                || mTxtRegIndex.getError() != null) {
            // / if any of the fields have an error, don't send
            // anything
            sToast.setText("Error: Invalid values detected.");
            sToast.show();
            return;
        }

        // check that we have a connection open to a device
        if (mcp2221Comm == null) {
            sToast.setText("No MCP2221 Connected");
            sToast.show();
            return;
        }

        // if we got here, all input fields are valid
        // get the data to be transmitted
        address = Cmd.getAddress();
        txData = Cmd.getData();
        speed = Cmd.getSpeed();

        // there's a chance that the data field will contain only "," and no
        // valid data
        // so perform a final check here
        if (txData.capacity() == 0) {
            mTxtData.setError("Invalid Data");
            return;
        }

        if (mSpinnerOperation.getSelectedItem().toString().contains("I2C")) {
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

        switch (mSpinnerOperation.getSelectedItem().toString().toLowerCase(Locale.US)) {

            case "i2c write":

                // make sure the address is even
                address &= 0xFE;

                result = mcp2221Comm.writeI2cdata(address, txData, txData.limit(), speed);

                mTxtOutput.append(Output.formatText("> I2C write, Address = "
                        + mTxtAddress.getText().toString() + " Data: " + dataString.toString()
                        + "\n", Color.BLACK));

                // display the result of the operation
                mTxtOutput.append(Output.processErrorMessage(result));
                break;

            case "i2c read":

                // the value in the data field = the number of bytes to read
                // allocate the readData buffer based on that size
                bytesToRead = Integer.parseInt(mTxtData.getText().toString(), 16);

                readData = ByteBuffer.allocate(bytesToRead);

                // make sure the address is odd
                address |= 0x01;

                result = mcp2221Comm.readI2cData(address, readData, bytesToRead, speed);

                mTxtOutput.append(Output.formatText("> I2C read (" + bytesToRead
                        + " bytes), Address = " + mTxtAddress.getText().toString() + "\n",
                        Color.BLACK));

                mTxtOutput.append(Output.processErrorMessage(result));

                // if the read was successful, display the data
                if (result == Mcp2221Constants.ERROR_SUCCESSFUL) {
                    mTxtOutput.append("< " + Output.formatText(readData, Output.DARK_GREEN));
                }
                break;

            case " smbus write":

                // for smbus the first entry in the data array is the register
                // index value
                usesPec = Cmd.getUsesPec();

                result = mcp2221Comm.smbWriteBlock(address, txData, txData.limit(), speed, usesPec);

                mTxtOutput.append(Output.formatText("> SMBus write, Address = "
                        + mTxtAddress.getText().toString() + ", Reg. Index: "
                        + mTxtRegIndex.getText().toString() + ", Data: " + dataString.toString()
                        + "\n", Color.BLACK));

                mTxtOutput.append(Output.processErrorMessage(result));

                break;

            case "smbus read":
                regIndex = Cmd.getRegIndex();
                usesPec = Cmd.getUsesPec();
                // first byte of the data is the register index
                // second item is the number of bytes to read back
                bytesToRead = Integer.parseInt(mTxtData.getText().toString(), 16);
                readData = ByteBuffer.allocate(bytesToRead);

                result =
                        mcp2221Comm.smbReadBlock(address, readData, bytesToRead, speed, usesPec,
                                regIndex);

                mTxtOutput.append(Output.formatText("> SMBus read (" + bytesToRead + " bytes), "
                        + "Address = " + mTxtAddress.getText().toString() + ", Reg. Index: "
                        + mTxtRegIndex.getText().toString() + "\n", Color.BLACK));

                mTxtOutput.append(Output.processErrorMessage(result));

                if (result == 0) {
                    mTxtOutput.append("< " + Output.formatText(readData, Output.DARK_GREEN));
                }
                break;

            default:
                break;
        }

    }

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

    }

    // ............................

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // close the connection and release all resources
        unregisterReceiver(mUsbReceiver);
    }

    /**
     * Experiments
     * 
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
            case 0:

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
            case 1:
                // if the fragment has been displayed before, don't recreate it, just update the
                // view
                myFragment = fragmentManager.findFragmentByTag(PIN_CONFIG_FRAGMENT);
                if (myFragment == null) {
                    transaction.replace(R.id.container,
                            PinConfigFragment.newInstance(position + 1), PIN_CONFIG_FRAGMENT);
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

                break;
            case 2:
                // if the fragment has been displayed before, don't recreate it, just update the
                // view
                myFragment = fragmentManager.findFragmentByTag(PIN_FUNCTIONS_FRAGMENT);
                if (myFragment == null) {
                    transaction.replace(R.id.container,
                            PinFunctionsFragment.newInstance(position + 1), PIN_FUNCTIONS_FRAGMENT);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    transaction.addToBackStack(PIN_FUNCTIONS_FRAGMENT);
                    transaction.commit();
                } else if (myFragment.isVisible() == false) {

                    transaction.replace(R.id.container, myFragment, PIN_FUNCTIONS_FRAGMENT);
                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    transaction.addToBackStack(PIN_FUNCTIONS_FRAGMENT);
                    transaction.commit();
                    // update the title as well
                    mTitle = getString(R.string.title_section3_pin_functions);
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
            case 1:
                mTitle = getString(R.string.title_section1_i2c_terminal);
                break;
            case 2:
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
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            // set up the dialogs for clearing the output and data fields
            mClearOutputDialog =
                    new ClearDialogFragment(rootView.findViewById(R.id.txtOutput),
                            getString(R.string.dialog_clear_output));
            mClearDataDialog =
                    new ClearDialogFragment(rootView.findViewById(R.id.txtData),
                            getString(R.string.dialog_clear_data));

            // get the IDs for UI elements and add event listeners
            mTxtAddress = (EditText) rootView.findViewById(R.id.txtAddress);
            mTxtAddress.addTextChangedListener(this);

            mTxtData = (EditText) rootView.findViewById(R.id.txtData);
            mTxtData.addTextChangedListener(this);
            // on a long click, show a dialog to clear the data field
            mTxtData.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
                    // show the dialog to clear the output window
                    mClearDataDialog.show(getFragmentManager(), TAG);
                    return false;
                }
            });

            mTxtRegIndex = (EditText) rootView.findViewById(R.id.txtRegIndex);
            mTxtRegIndex.addTextChangedListener(this);

            mSpinnerOperation = (Spinner) rootView.findViewById(R.id.spinnerOperation);
            mSpinnerOperation.setOnItemSelectedListener(new CustomOnItemSelectedListener());

            mSpinnerAddrLength = (Spinner) rootView.findViewById(R.id.SpinnerAddrLength);
            mSpinnerAddrLength.setOnItemSelectedListener(new CustomOnItemSelectedListener());

            mSpinnerPEC = (Spinner) rootView.findViewById(R.id.SpinnerPEC);
            mSpinnerPEC.setOnItemSelectedListener(new CustomOnItemSelectedListener());

            mTxtOutput = (EditText) rootView.findViewById(R.id.txtOutput);

            mDebugOutput = (EditText) rootView.findViewById(R.id.debugOutput);

            // on a long click, show a dialog to clear the output
            mTxtOutput.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
                    // show the dialog to clear the output window
                    mClearOutputDialog.show(getFragmentManager(), TAG);
                    return false;
                }
            });
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
            // Each editbox has a unique hash code, so if the editable
            // hash value matches it, we know which of the editboxes was
            // typed into

            // first make sure we have some data in the field we want to validate
            if (s.toString().isEmpty() == false) {
                // Address check
                if (mTxtAddress.getText().hashCode() == s.hashCode()) {

                    // only allow 2 chars in the textbox
                    if (mTxtAddress.getText().toString().length() > 2) {
                        s.delete(2, mTxtAddress.getText().toString().length());
                        sToast.setText("Address cannot exceed two characters");
                        sToast.show();
                    }

                    // verify that the address value is valid:
                    // <= 0x7F for 7bit and <= 0xFF for 8bit
                    addr = Integer.parseInt(mTxtAddress.getText().toString(), 16);

                    // 7bit address check
                    if (mSpinnerAddrLength.getSelectedItem().toString().contains("7")) {
                        if (addr > 0x7F) {
                            mTxtAddress.setError("Address must be between 0 and 7F");
                        } else {
                            // make sure the error is cleared
                            mTxtAddress.setError(null);
                        }
                    } else {// 8bit address check
                        if (addr > 0xFF) {
                            mTxtAddress.setError("Address must be between 0 and FF");
                        } else {
                            // value is in range, make sure the error is cleared
                            mTxtAddress.setError(null);
                        }
                    }
                } else if (mTxtRegIndex.getText().hashCode() == s.hashCode()) {
                    // Register index check

                    // only allow 2 chars in the textbox
                    if (mTxtRegIndex.getText().toString().length() > 2) {
                        s.delete(2, mTxtRegIndex.getText().toString().length());
                        sToast.setText("Register cannot exceed two characters");
                        sToast.show();
                    }

                    regIndex = Integer.parseInt(mTxtRegIndex.getText().toString(), 16);

                    if (regIndex > 0xFF) {
                        mTxtRegIndex.setError("Register must be between 0 and FF");
                    } else {
                        // make sure the error is cleared
                        mTxtRegIndex.setError(null);
                    }
                } else if (mTxtData.getText().hashCode() == s.hashCode()) {
                    // Data check
                    final int commaPos = s.toString().lastIndexOf(",");
                    String data;

                    if (mSpinnerOperation.getSelectedItem().toString().contains("Write")) {
                        // make sure that the value after a "," isn't over 0xFF

                        // if a comma was found
                        if (commaPos != -1) {
                            // and it's not in the last position
                            if (commaPos < s.length() - 1) {

                                // check if the value after the comma isn't larger
                                // than FF
                                data = s.toString().substring(commaPos + 1);
                                // remove any line breaks
                                data = data.replace("\n", "");

                                // check that the string has some data
                                if (data.isEmpty()) {
                                    return;
                                }

                                // only allow 2 character bytes
                                if (data.length() > 2) {
                                    // delete whatever the user tried to add
                                    s.delete(s.length() - 1, s.length());
                                    sToast.setText("Data byte cannot exceed two characters");
                                    sToast.show();
                                    return;
                                }

                                if (Integer.parseInt(data, 16) > 0xFF) {
                                    mTxtData.setError("Values must be between 0 and FF");
                                } else {
                                    // clear any errors
                                    mTxtData.setError(null);
                                }
                            } else if (commaPos == 0) {
                                // if the comma is the first item in the text box
                                // and no values follow it; clear any lingering error messages
                                // and return if we only have a ","
                                mTxtData.setError(null);
                                return;
                            }
                        } else {
                            // if not comma is present we can still have a value
                            // so check it's in range as well

                            // remove any line breaks
                            data = s.toString();
                            data = data.replace("\n", "");

                            // check that the string has some data
                            if (data.isEmpty()) {
                                return;
                            }

                            // only allow 2 character bytes
                            if (data.length() > 2) {
                                // delete whatever the user tried to add
                                s.delete(s.length() - 1, s.length());
                                sToast.setText("Data byte cannot exceed two characters");
                                sToast.show();
                                return;
                            }

                            if (Integer.parseInt(data, 16) > 0xFF) {
                                mTxtData.setError("Values must be between 0 and FF");
                            } else {
                                // clear any errors
                                mTxtData.setError(null);
                            }

                        } // comma not found
                    } else {
                        // check for read operations
                        // don't allow commas
                        if (commaPos != -1) {
                            s.delete(commaPos, commaPos + 1);
                            return;
                        }
                        // get the data
                        data = s.toString();

                        // for smbus read with PEC on the max value is 65534
                        if (mSpinnerOperation.getSelectedItem().toString().contains("SMB")
                                && mSpinnerPEC.getSelectedItem().toString().contains("On")) {
                            if (Long.parseLong(data, 16) > 65534) {
                                // delete whatever the user tried to add
                                s.delete(s.length() - 1, s.length());
                                sToast.setText("Cannot read more than 65534(0xFFFE) bytes with PEC On");
                                sToast.show();
                                return;
                            }
                        }

                        if (Long.parseLong(data, 16) > 65535) {
                            // delete whatever the user tried to add
                            s.delete(s.length() - 1, s.length());
                            sToast.setText("Cannot read more than 65535(0xFFFF) bytes");
                            sToast.show();
                        }
                    } // read data check
                } // txtData field check
            } // input field not empty
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
        PinConfigFragment pinConfigFragment =
                (PinConfigFragment) getFragmentManager().findFragmentByTag(PIN_CONFIG_FRAGMENT);
        if (pinConfigFragment != null) {
            pinConfigFragment.updateConnectionStatus();
            if (pinConfigFragment.isVisible()) {
                pinConfigFragment.updatePinSelections();
            }
        }

        // if the Pin Functions fragment is visible, read the chip's
        // settings and update the fragment
        PinFunctionsFragment pinFunctionsFragment =
                (PinFunctionsFragment) getFragmentManager().findFragmentByTag(
                        PIN_FUNCTIONS_FRAGMENT);
        if (pinFunctionsFragment != null) {
            // update the connection status as well
            pinFunctionsFragment.updateConnectionStatus();
            if (pinFunctionsFragment.isVisible()) {
                pinFunctionsFragment.updatePinFunctions();
                // sToast.setText("MCP2221 Pin Configuration read from device.");
                // sToast.show();
            }
        }

        // if the Serial Terminal fragment is visible update the mcp2221
        // connection status
        //
        SerialTerminalFragment serialTerminalFragment =
                (SerialTerminalFragment) getFragmentManager().findFragmentByTag(
                        SERIAL_TERMINAL_FRAGMENT);
        if (serialTerminalFragment != null) {
            serialTerminalFragment.updateConnectionStatus();
        }
    }
}
