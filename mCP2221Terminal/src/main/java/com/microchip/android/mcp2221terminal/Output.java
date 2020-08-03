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

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import com.microchip.android.mcp2221comm.Mcp2221Constants;

import java.nio.ByteBuffer;

/**
 * Used to format and apply color to the text that will be printed in the output field.
 */
public final class Output {

    /**
     * Custom green color that will be used to print messages.
     */
    public static final int DARK_GREEN = Color.rgb(100, 200, 100);
    /** SpannanbleStringBuilder that will contain the formatted output text. */
    private static SpannableStringBuilder sOutText = new SpannableStringBuilder();

    /**
     * Sets the color for the input text and makes it uppercase.
     * 
     * @param inputBuffer
     *            (ByteBuffer) - array of hex values
     * @param color
     *            (int) - the color of the string
     * @return (SpannableStringBuilder) - the text with the desired color and formatting
     */
    public static SpannableStringBuilder formatText(final ByteBuffer inputBuffer, final int color,String address) {
        sOutText.clear();
        for (int i = 0; i < inputBuffer.limit(); i++) {
            sOutText.append(String.format("%02X", inputBuffer.get(i)));
        }
        String s = sOutText.toString();
        sOutText.clear();
        if(address.equals("90"))
        sOutText.append(String.format("%.2f",Integer.parseInt(s,16)* 0.0078125));
        else{
            double a = Integer.parseInt(s,16);
            a /= 265;
            a /= 265;
            a *= 100;
            sOutText.append(String.format("%.2f",a));
        }
//            sOutText.append(String.format("%.2f",(float)((Integer.parseInt(s,16)/265.00)/256)*100));
        // if we only receive one hex value then don't append a new line just add a blank space
        if (inputBuffer.limit() > 1) {
            sOutText.append("\n");
        } else {
            sOutText.append(" ");
        }
        sOutText.setSpan(new ForegroundColorSpan(color), 0, sOutText.length(), 0);
        return sOutText;
    }

    /**
     * Sets the color for the input text.
     * 
     * @param inputText
     *            (String) - the string that will be colored
     * @param color
     *            (int) - the color of the string
     * @return (SpannableStringBuilder) - the text with the desired color set for the span of the
     *         input string
     */
    public static SpannableStringBuilder formatText(final String inputText, final int color) {
        sOutText.clear();
        sOutText.append(inputText);
        sOutText.setSpan(new ForegroundColorSpan(color), 0, sOutText.length(), 0);
        return sOutText;
    }

    /**
     * Sets the color for the input text and makes it uppercase.
     * 
     * @param inputByte
     *            (ByteBuffer) - array of hex values
     * @param color
     *            (int) - the color of the string
     * @return (SpannableStringBuilder) - the text with the desired color and formatting
     */
    public static SpannableStringBuilder formatText(final byte inputByte, final int color) {
        sOutText.clear();
        sOutText.append(String.format("%02X", inputByte) + " ");
        sOutText.setSpan(new ForegroundColorSpan(color), 0, sOutText.length(), 0);
        return sOutText;
    }

    /**
     * Interpret the read/write operation result and determine what message to print.
     * 
     * @param opResult
     *            (int) - the result that was received from one of the MCP2221 read/write methods
     * @return (SpannableStringBuilder) - color coded message based on the result code received
     */
    public static SpannableStringBuilder processErrorMessage(final int opResult) {
        // error codes that can be received from the MCP2221
        switch (opResult) {
            case Mcp2221Constants.ERROR_SUCCESSFUL:
                // if OK, switch to green color
                printText("OK", DARK_GREEN);
                break;

            // any errors are shown in red

            case Mcp2221Constants.ERROR_DEV_WRITE_FAILED:
                printText("Device Write Failed. Error code: " + opResult, Color.RED);
                break;

            case Mcp2221Constants.ERROR_INVALID_DATA_LEN:
                printText("Invalid data length. Error code: " + opResult, Color.RED);
                break;

            case Mcp2221Constants.ERROR_TIMEOUT:
                printText("Timeout. Error code: " + opResult, Color.RED);
                break;
            case Mcp2221Constants.ERROR_I2C_SEND_ERR:
                printText("I2C send error. Error code: " + opResult, Color.RED);
                break;

            case Mcp2221Constants.ERROR_I2C_SETSPEED:
                printText("Error setting I2C speed. Error code: " + opResult, Color.RED);
                break;

            case Mcp2221Constants.ERROR_I2C_STATUS:
                printText("Invalid I2C status. Error code: " + opResult, Color.RED);
                break;

            case Mcp2221Constants.ERROR_I2C_ADDRNACK:
                printText("Address NACK received. Error code: " + opResult, Color.RED);
                break;

            case Mcp2221Constants.ERROR_I2C_SLAVE_DATA_NACK:
                printText("Slave data NACK received. Error code: " + opResult, Color.RED);
                break;

            case Mcp2221Constants.ERROR_I2C_READ001:
                printText("Read error. Transfer was not possible. Error code: " + opResult,
                        Color.RED);
                break;

            case Mcp2221Constants.ERROR_I2C_READ002:
                printText("Read error. USB operation not successful. Error code:  " + opResult,
                        Color.RED);
                break;

            case Mcp2221Constants.ERROR_I2C_READ003:
                printText("Could not read data. Error code:  " + opResult, Color.RED);
                break;

            case Mcp2221Constants.ERROR_I2C_READ004:
                printText("Too many bytes received from slave. Error code: " + opResult, Color.RED);
                break;

            case Mcp2221Constants.ERROR_INVALID_PARAMETER_2:
                printText("Invalid sent/received data(null value). Error code: " + opResult,
                        Color.RED);
                break;
            case Mcp2221Constants.ERROR_INVALID_PARAMETER_3:
                printText("Number of bytes to read/write is too large. Error code: " + opResult,
                        Color.RED);
                break;

            case Mcp2221Constants.ERROR_WRONG_PEC:
                printText("Wrong PEC value. Error code: " + opResult, Color.RED);
                break;

            default:
                printText("Unknow error. Error code:  " + opResult, Color.RED);
                break;
        }
        return sOutText;
    }

    /**
     * Sets the color for the input text.
     * 
     * @param inputText
     *            (String)
     * @param color
     *            (int)
     */
    private static void printText(final String inputText, final int color) {
        sOutText.clear();
        sOutText.append(inputText + "\n");
        sOutText.setSpan(new ForegroundColorSpan(color), 0, sOutText.length(), 0);
    }

    /** Constructor. Does nothing */
    private Output() {
    }
}
