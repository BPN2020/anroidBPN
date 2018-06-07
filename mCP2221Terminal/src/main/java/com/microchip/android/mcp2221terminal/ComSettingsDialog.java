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

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.microchip.android.mcp2221comm.Mcp2221Comm;
import com.microchip.android.microchipusb.MCP2221;

import java.util.Arrays;
import java.util.List;

/**
 * Dialog containing the settings for the COM port and Serial Terminal fragment.
 * 
 */
public class ComSettingsDialog extends Dialog {

    private Spinner spinnerComOutputFormat;
    private Spinner spinnerBaudRate;
    private Button btnSave;
    private ImageButton btnGetBaudRate;
    private Switch swLocalEcho;

    private int mBaudRate = 9600;
    private boolean mLocalEcho = true;
    private StringBuffer mRxFormat = new StringBuffer("ASCII");
    private MCP2221 mMcp2221;
    private Mcp2221Comm mMcp2221Comm;

    /** Custom toast - displayed in the center of the screen. */
    private static Toast sToast;

    public ComSettingsDialog(Context context) {
        super(context);

        this.setContentView(R.layout.dialog_com_settings);
        this.setTitle("COM Settings");

        spinnerComOutputFormat = (Spinner) findViewById(R.id.spinnerComOutputFormat);
        spinnerBaudRate = (Spinner) findViewById(R.id.spinnerBaudRate);
        btnSave = (Button) findViewById(R.id.btnSaveComSettings);
        btnGetBaudRate = (ImageButton) findViewById(R.id.btnComSendData);
        swLocalEcho = (Switch) findViewById(R.id.swLocalEcho);

        String[] baudRatesArray = getContext().getResources().getStringArray(R.array.baudrate);
        List<String> baudRatesArrayList = Arrays.asList(baudRatesArray);

        // set up the custom toast; This will appear in the center of the
        // screen and not the default position
        sToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        sToast.setGravity(Gravity.CENTER, 0, 0);

        btnSave.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                mBaudRate = Integer.parseInt(spinnerBaudRate.getSelectedItem().toString());
                mLocalEcho = swLocalEcho.isChecked();
                // make sure we clear the string first
                mRxFormat.setLength(0);
                // get the new value
                mRxFormat.append(spinnerComOutputFormat.getSelectedItem().toString());
                sToast.setText("Settings Saved");
                sToast.show();
                dismiss();
            }
        });

        btnGetBaudRate.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                String[] baudRatesArray =
                        getContext().getResources().getStringArray(R.array.baudrate);
                List<String> baudRatesArrayList = Arrays.asList(baudRatesArray);
                int tempBaud = 9600;

                // try to read the baud rate from the connected mcp2221
                if (mMcp2221Comm == null) {
                    sToast.setText("No MCP2221 Connected.");
                    sToast.show();
                    return;
                }

                mBaudRate = mMcp2221Comm.getBaudRate();
                // check if the read was successful
                if (mBaudRate < 0) {
                    sToast.setText("Could not read Baud Rate from MCP2221. Default loaded.");
                    sToast.show();
                } else {
                    sToast.setText("Baud Rate read from MCP2221.");
                    sToast.show();
                }

                // the mcp2221 reports back slightly non standard baud rates, so check which is
                // the closest standard value
                for (int i = 0; i < baudRatesArrayList.size() - 1; i++) {
                    if (mBaudRate >= Integer.parseInt(baudRatesArrayList.get(i))
                            && mBaudRate < Integer.parseInt(baudRatesArrayList.get(i + 1))) {
                        tempBaud = Integer.parseInt(baudRatesArrayList.get(i));
                        break;
                    }
                    // the mcp2221 reports back a higher baud rate, so perform a final check to see
                    // if 115200 is used
                    if ((mBaudRate >= 115200) && (mBaudRate < 116000)) {
                        tempBaud = 115200;
                    }
                }
                // update the value. If a match wasn't found, use the default 9600 baud
                mBaudRate = tempBaud;
                spinnerBaudRate.setSelection(baudRatesArrayList.indexOf(String.valueOf(mBaudRate)));
            }
        });

    }

    // ******************************
    // Getters and Setters
    // ******************************

    /**
     * Gets the local echo selection.
     * 
     * @return (boolean) the localEcho status. True = the sent data will be printed in the "Output"
     *         area.
     */
    public boolean isLocalEchoEnabled() {
        return mLocalEcho;
    }

    /**
     * Gets the currently selected RX message formatting.
     * 
     * @return (StringBuffer) - the rxFormat. <br>
     *         "ASCII" or "Hex"
     */
    public StringBuffer getRxFormat() {
        return mRxFormat;
    }

    /**
     * @param rxFormat
     *            the rxFormat to set
     */
    public void setRxFormat(StringBuffer rxFormat) {
        this.mRxFormat = rxFormat;
    }

    /**
     * Gets the currently selected baud rate.
     * 
     * @return (int) - the selected baudRate
     */
    public int getBaudRate() {
        return mBaudRate;
    }

    /**
     * Show the COM settings dialog
     * 
     * @param mcp2221Comm
     *            - a MCP2221Comm object which will be used to set the new baud rate when the user
     *            saves the COM settings.
     */
    public void show(Mcp2221Comm mcp2221Comm) {
        this.mMcp2221Comm = mcp2221Comm;
        super.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        String[] rxFormatArray =
                getContext().getResources().getStringArray(R.array.comOutputFormat);
        List<String> rxFormatArrayList = Arrays.asList(rxFormatArray);

        String[] baudRatesArray = getContext().getResources().getStringArray(R.array.baudrate);
        List<String> baudRatesArrayList = Arrays.asList(baudRatesArray);

        // load the current values
        spinnerBaudRate.setSelection(baudRatesArrayList.indexOf(String.valueOf(mBaudRate)));
        spinnerComOutputFormat.setSelection(rxFormatArrayList.indexOf(mRxFormat.toString()));
        swLocalEcho.setChecked(mLocalEcho);

    }
}
