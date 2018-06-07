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

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * On Item Selected Listener for all the spinners in the i2c terminal part of the app. Used to
 * update the UI and perform error checks based on the selected items.
 */
public class CustomOnItemSelectedListener implements OnItemSelectedListener {

    // set the default value.
    // The app starts up with I2C write selected for the operation
    /** The value for the previously selected operation (read/write). */
    private String mPrevOperation = "write";
    /** The value for the currently selected operation (read/write). */
    private String mCurrentOperation;

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, final long id) {

        Context context = parent.getContext();

        EditText txtRegIndex = (EditText) ((Activity) context).findViewById(R.id.txtRegIndex);
        EditText txtAddr = (EditText) ((Activity) context).findViewById(R.id.txtAddress);
        EditText txtData = (EditText) ((Activity) context).findViewById(R.id.txtData);
        Spinner spinnerPec = (Spinner) ((Activity) context).findViewById(R.id.SpinnerPEC);
        Spinner spinnerOperation =
                (Spinner) ((Activity) context).findViewById(R.id.spinnerOperation);

        switch (parent.getId()) {
            case R.id.spinnerOperation:

                // see if we have a read or write operation
                if (parent.getItemAtPosition(pos).toString().contains("Write")) {
                    mCurrentOperation = "write";
                } else {
                    mCurrentOperation = "read";
                }

                // if the operation changed from read to write or vice versa,
                // clear the data field
                if (mPrevOperation.equals(mCurrentOperation) == false) {
                    txtData.setText("");
                }
                // update the value
                mPrevOperation = mCurrentOperation;

                // hide the register index and PEC fields if I2C is selected
                if (parent.getItemAtPosition(pos).toString().contains("I2C")) {
                    txtRegIndex.setVisibility(EditText.INVISIBLE);
                    spinnerPec.setVisibility(Spinner.INVISIBLE);
                } else {
                    txtRegIndex.setVisibility(EditText.VISIBLE);
                    spinnerPec.setVisibility(Spinner.VISIBLE);
                }

                // update the Hint for the Data field for read/write operations
                if (parent.getItemAtPosition(pos).toString().contains("Write")) {
                    txtData.setHint("Data bytes (ex: AA,12)");
                } else {
                    // read operations
                    if ((spinnerOperation.getSelectedItem().toString().contains("SMB"))
                            && (spinnerPec.getSelectedItem().toString().contains("On"))) {
                        txtData.setHint("Number of bytes to read (1 to 0xFFFE)");
                        if (txtData.getText().toString().isEmpty() == false) {
                            if (Integer.parseInt(txtData.getText().toString(), 16) > 0xFFFE) {
                                txtData.setError("Cannot read more than 0xFFFE bytes with PEC On");
                            }
                        }
                    } else {
                        txtData.setHint("Number of bytes to read (1 to 0xFFFF)");
                        // clear any errors
                        txtData.setError(null);
                    }
                }

                break;

            case R.id.SpinnerPEC:
                if (spinnerOperation.getSelectedItem().toString().contains("SMBus Read")) {
                    {
                        if (spinnerPec.getSelectedItem().toString().contains("On")) {
                            txtData.setHint("Number of bytes to read (1 to 0xFFFE)");
                            if (txtData.getText().toString().isEmpty() == false) {
                                if (Integer.parseInt(txtData.getText().toString(), 16) > 0xFFFE) {
                                    txtData.setError("Cannot read more than 0xFFFE bytes with PEC On");
                                }
                            }
                        } else {
                            txtData.setHint("Number of bytes to read (1 to 0xFFFF)");
                            // clear any errors
                            txtData.setError(null);
                        }
                    }
                }
                break;
            case R.id.SpinnerAddrLength:
                // if 7 bit is selected, check that the address isn't > 0x7F
                if ((txtAddr.getText().toString().isEmpty() == false)) {

                    // 7bit address check
                    if (parent.getItemAtPosition(pos).toString().contains("7")) {
                        if (Integer.parseInt(txtAddr.getText().toString(), 16) > 0x7F) {
                            txtAddr.setError("Address must be between 0 and 7F");
                        }
                    } else if (Integer.parseInt(txtAddr.getText().toString(), 16) > 0xFF) {
                        // 8bit address check
                        txtAddr.setError("Address must be between 0 and FF");
                    } else {
                        // remove any existing errors
                        txtAddr.setError(null);
                    }
                }
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

}
