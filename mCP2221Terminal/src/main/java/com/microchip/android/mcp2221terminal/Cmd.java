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
import android.widget.EditText;
import android.widget.Spinner;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides easy access to the parameters needed for I2C/SMBus transfer. <br>
 * All input fields are assumed to be in a valid state. No error checking is done before returning
 * the values.
 */
public final class Cmd {
    /** Reference to the main activity. Used to get the IDs for all input fields. */
    private static Activity sActivity;

    /**
     * Does nothing.
     */
    private Cmd() {

    }

    /**
     * Get the slave address from the "address" text field and return it in a format that will be
     * usable for the MCP2221 read/write functions. The input value is assumed to be a hex number.
     * 
     * @return (byte) - the slave address value
     */
    public static byte getAddress(String in) {
        byte address;
        //EditText txtAddress = (EditText) sActivity.findViewById(R.id.txtAddress);
        //Spinner spinnerAddrLength = (Spinner) sActivity.findViewById(R.id.SpinnerAddrLength);

        address = (byte) Integer.parseInt(in, 16);

        // 7 bit address needs to be shifted left by 1 byte
//        if (spinnerAddrLength.getSelectedItem().toString().contains("7")) {
//            address <<= 1;
//        }

        return address;
    }

    /**
     * Get the data from the "data" field and convert it to a byte array. All values are assumed to
     * be hex. Input values should be comma separated.
     * 
     * @return (ByteBuffer) - data bytes that will be used in the MCP2221 read/write methods.
     */
    public static ByteBuffer getData(String in) {
        ByteBuffer data;
        List<String> dataBytes = new ArrayList<String>();

        dataBytes.clear();
        //EditText txtData = (EditText) sActivity.findViewById(R.id.txtData);
        //Spinner spinnerOperation = (Spinner) sActivity.findViewById(R.id.spinnerOperation);

        String temp =in;
        String[] tempStringArray;
        // remove any line breaks
        temp = temp.replace("\n", "");
        // split if one or more commas are encountered
        tempStringArray = temp.split(",+");

        // remove any null entries
        for (String string : tempStringArray) {
            if (string.equals("") == false) {
                dataBytes.add(string);
            }
        }

        // if smbus is selected the first value in the data array is the
        // register index
        //if (spinnerOperation.getSelectedItem().toString().contains("SMBus")) {
        if(false){
            data = ByteBuffer.allocate(dataBytes.size() + 1);

            // place the register index in the first position of the array
            data.put(0, getRegIndex());

            // then add the rest of the data
            for (int i = 0; i < dataBytes.size(); i++) {
                data.put(i + 1, (byte) Integer.parseInt(dataBytes.get(i), 16));
            }
        } else { // i2c array contains only data
            data = ByteBuffer.allocate(dataBytes.size());

            for (int i = 0; i < dataBytes.size(); i++) {
                data.put((byte) Integer.parseInt(dataBytes.get(i), 16));
            }
        }

        return data;
    }

    /**
     * Get the speed selection from the "speed" drop down and return it in a format that will be
     * usable for the MCP2221 read/write functions.
     * 
     * @return (int) - the speed value
     */
    public static int getSpeed() {
        int speed = 100000;
        //Spinner spinnerSpeed = (Spinner) sActivity.findViewById(R.id.SpinnerSpeed);

//        switch (spinnerSpeed.getSelectedItemPosition()) {
//            case 0:
//                speed = 100000;
//                break;
//            case 1:
//                speed = 400000;
//                break;
//            default:
//                speed = 100000;
//                break;
//        }

        return speed;
    }

    /**
     * Get the PEC selection from the "PEC" drop down and return it in a format that will be usable
     * for the MCP2221 read/write functions.
     * 
     * @return (byte) - 0 = PEC off, 1 = PEC on
     */
    public static byte getUsesPec() {
        byte usesPec;
        Spinner spinnerPec = (Spinner) sActivity.findViewById(R.id.SpinnerPEC);

        // 0 = PEC off, 1 = PEC on
        usesPec = (byte) spinnerPec.getSelectedItemPosition();
        return usesPec;
    }

    /**
     * Get the register index value from the "register index" field and return it in a format that
     * will be usable for the MCP2221 read/write functions. The input is assumed to be a hex number
     * 
     * @return (byte) - register index value
     */
    public static byte getRegIndex() {
        byte regIndex;
        EditText txtRegIndex = (EditText) sActivity.findViewById(R.id.txtRegIndex);
        regIndex = (byte) Integer.parseInt(txtRegIndex.getText().toString(), 16);
        return regIndex;
    }

    /**
     * Pass a reference to the activity which contains the spinners and text boxes for the command
     * fields (address, speed etc).
     * 
     * @param mainActivity
     *            main activity reference
     */
    public static void setActivity(final Activity mainActivity) {
        sActivity = mainActivity;
    }
}
