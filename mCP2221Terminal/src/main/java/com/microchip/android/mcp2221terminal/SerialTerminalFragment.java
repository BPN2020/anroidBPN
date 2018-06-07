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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.microchip.android.mcp2221comm.Mcp2221Comm;
import com.microchip.android.microchipusb.MCP2221;

import java.lang.Thread.State;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class containing the Serial Terminal.
 */
@SuppressLint("ShowToast")
public class SerialTerminalFragment extends Fragment {

    /**
     * Thread to parse incoming serial messages. Used to format ASCII/Hex values based on the
     * "RX format" selection.
     */
    public static MessageThread rxMessageParserThread;
    /**
     * Incoming message format(ASCII/Hex).
     */
    public static StringBuffer rxDataFormat = new StringBuffer("ASCII");
    /**
     * Custom green color that will be used to print messages.
     */
    public static final int DARK_GREEN = Color.rgb(100, 200, 100);
    /**
     * The fragment argument representing the section number for this fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private MCP2221 mMcp2221;
    private Mcp2221Comm mMcp2221Comm;

    /** Custom toast - displayed in the center of the screen. */
    private static Toast sToast;

    private Button btnOpenCloseCom;
    private ImageButton btnComSettings;
    private ImageButton btnComSendData;

    private static EditText txtComOutput;
    private EditText txtComInput;

    private ComSettingsDialog dialogComSetting;
    private byte rxByte;

    /** Customized dialog fragment to clear the Data field. */
    private ClearDialogFragment mClearOutputDialog;
    /** TAG to be used for logcat. */
    protected static final String TAG = "SerialTerminalFragment";

    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static SerialTerminalFragment newInstance(int sectionNumber) {
        SerialTerminalFragment fragment = new SerialTerminalFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public SerialTerminalFragment() {
    }

    @Override
    public View
            onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_serial_terminal, container, false);

        // initialize the COM setting dialog as well
        if (dialogComSetting == null) {
            dialogComSetting = new ComSettingsDialog(getActivity());
        }

        // set up the dialogs for clearing the output and data fields
        mClearOutputDialog =
                new ClearDialogFragment(rootView.findViewById(R.id.txtComOutput),
                        getString(R.string.dialog_clear_output));

        // update the connection status each time the fragment becomes visible
        mMcp2221 = ((MainActivity) getActivity()).mcp2221;

        // update the connection status each time the fragment becomes visible
        mMcp2221Comm = ((MainActivity) getActivity()).mcp2221Comm;

        // set up the custom toast; This will appear in the center of the
        // screen and not the default position
        sToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
        sToast.setGravity(Gravity.CENTER, 0, 0);

        txtComInput = (EditText) (rootView.findViewById(R.id.txtInput));
        txtComOutput = (EditText) (rootView.findViewById(R.id.txtComOutput));
        btnOpenCloseCom = (Button) (rootView.findViewById(R.id.btnOpenCloseCom));
        btnComSettings = (ImageButton) (rootView.findViewById(R.id.btnComSettings));
        btnComSendData = (ImageButton) (rootView.findViewById(R.id.btnComSendData));

        // the open/close operation will be selected based on the tag
        btnOpenCloseCom.setTag("open");

        // *********************************************
        // COM settings on dismissed listener
        // *********************************************
        dialogComSetting.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                // make sure we clear the string first
                rxDataFormat.setLength(0);
                // get the new value
                rxDataFormat.append(dialogComSetting.getRxFormat());
            }
        });

        // *********************************************
        // Open/Close COM on click
        // *********************************************
        btnOpenCloseCom.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                // update the connection status
                mMcp2221Comm = ((MainActivity) getActivity()).mcp2221Comm;
                if (mMcp2221Comm == null) {
                    sToast.setText("No MCP2221 Connected");
                    sToast.show();
                    return;
                }

                if (btnOpenCloseCom.getTag().equals("open")) {
                    if (mMcp2221Comm.isComOpen() == false) {
                        // set the handler for the RX messages.
                        mMcp2221Comm.setHandler(rxHandler);
                        // try to set the baud rate
                        if (mMcp2221Comm.setBaudRate(dialogComSetting.getBaudRate()) == false) {
                            sToast.setText("Could not set Baud Rate. COM not opened.");
                            sToast.show();
                            return;
                        }
                        if (mMcp2221Comm.openCOM()) {
                            // start the message parsing thread as well

                            rxMessageParserThread = new MessageThread(messageParserHandler);

                            sToast.setText("COM opened.\n" + "Baud Rate: "
                                    + dialogComSetting.getBaudRate());
                            sToast.show();
                            // update the text and tag
                            btnOpenCloseCom.setTag("close");
                            btnOpenCloseCom.setText("Close COM");
                            return;

                        } else {
                            sToast.setText("Could not open COM.");
                            sToast.show();
                            return;
                        }
                    } else {
                        sToast.setText("COM already opened.");
                        sToast.show();
                    }
                }

                // Close COM operation
                if (btnOpenCloseCom.getTag().equals("close")) {
                    if (mMcp2221Comm.isComOpen()) {
                        mMcp2221Comm.closeCOM();

                        rxMessageParserThread.destroy();

                        sToast.setText("COM closed.");
                        sToast.show();
                        // update the text and tag
                        btnOpenCloseCom.setTag("open");
                        btnOpenCloseCom.setText("Open COM");
                    }
                }
            }
        });

        // *************************************
        // Send Data on click
        // ************************************
        btnComSendData.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                // check if there's a MCP2221 connected
                mMcp2221Comm = ((MainActivity) getActivity()).mcp2221Comm;
                if (mMcp2221Comm == null) {
                    sToast.setText("No MCP2221 Connected");
                    sToast.show();
                    return;
                }

                // don't do anything if there's no data in the input field
                if (txtComInput.getText().toString().isEmpty()) {
                    sToast.setText("No data to send.");
                    sToast.show();
                    return;
                }

                if (mMcp2221Comm.isComOpen()) {
                    mMcp2221Comm.sendCdcData(txtComInput.getText().toString());

                    // if local echo is enabled, add the sent message to the output field, in blue
                    if (dialogComSetting.isLocalEchoEnabled()) {
                        txtComOutput.append(Output.formatText(txtComInput.getText().toString(),
                                DARK_GREEN));
                    }

                    txtComInput.setText("");

                } else {
                    sToast.setText("COM not opened.");
                    sToast.show();
                }
            }
        });

        // *************************************
        // COM Settings on click
        // ************************************
        btnComSettings.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialogComSetting.show(mMcp2221Comm);
            }
        });

        // *****************************************
        // Output field long click
        // *****************************************
        // on a long click, show a dialog to clear the data field
        txtComOutput.setOnLongClickListener(new View.OnLongClickListener() {
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

    @Override
    public void onResume() {
        super.onResume();
        // Set title
        getActivity().getActionBar().setTitle(R.string.title_section4_serial_terminal);
    }

    /**
     * Handler for receiving messages from the USB Manager thread
     */
    private static Handler rxHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            rxMessageParserThread.parseRxMessage(msg.obj.toString());
        } // handleMessage
    }; // handler

    public void updateConnectionStatus() {
        // update the connection status each time the fragment becomes visible
        mMcp2221Comm = ((MainActivity) getActivity()).mcp2221Comm;
        // Close COM operation if needed
        if (btnOpenCloseCom.getTag().equals("close")) {
            if (mMcp2221Comm != null) {
                mMcp2221Comm.closeCOM();
                rxMessageParserThread.destroy();
            }
            // make sure the text on the button changes
            sToast.setText("COM closed.");
            sToast.show();
            // update the text and tag
            btnOpenCloseCom.setTag("open");
            btnOpenCloseCom.setText("Open COM");
        }

        mMcp2221 = ((MainActivity) getActivity()).mcp2221;
    }

    /**
     * handler for parsing the incoming RX message.
     */
    private static Handler messageParserHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            SerialTerminalFragment.txtComOutput.append(msg.obj.toString());
        };
    };

    @Override
    public void onPause() {
        super.onPause();
        // update the connection and close the COM before leaving the fragment
        updateConnectionStatus();
    }
}

class MessageThread implements Runnable {
    private Thread messageThread;
    private StringBuffer msg = new StringBuffer();
    StringBuffer tempString = new StringBuffer();
    /** Handler to pass CDC messages to the calling activity. */
    private Handler mHandler;
    StringBuffer tempStringBuffer = new StringBuffer();

    @Override
    public void run() {
        while (true) {

            if (this.messageThread == null) {
                return;
            }

            if (this.messageThread.isInterrupted()) {
                this.messageThread = null;
                return;
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (msg.length() != 0) {
                synchronized (tempString) {
                    if (SerialTerminalFragment.rxDataFormat.toString().equals("ASCII")) {
                        tempString.append(msg.toString());

                    } else {

                        // Direct use of Pattern:
                        Pattern p = Pattern.compile("(\\p{Cc})");
                        Matcher m = p.matcher(msg.toString());

                        while (m.find()) {
                            m.appendReplacement(tempStringBuffer,
                                    String.format("{%02X} ", (byte) m.group().charAt(0)));
                        }
                        m.appendTail(tempStringBuffer);
                    }

                    tempString.append(tempStringBuffer.toString());
                    mHandler.obtainMessage(0, tempString.toString()).sendToTarget();
                    tempString.setLength(0);
                    tempStringBuffer.setLength(0);
                    msg.setLength(0);
                }
            }
        }
    }

    /**
     * create and start the RX message processing thread.
     * 
     * @param messageHandler
     *            - the handler that will receive the parsed string
     */
    MessageThread(Handler messageHandler) {
        this.mHandler = messageHandler;

        if (this.messageThread == null) {
            this.messageThread = new Thread(this);
            this.messageThread.start();
        }

        if (this.messageThread.getState() == State.NEW) {
            this.messageThread.start();
        }
    }

    public void parseRxMessage(String inputMessage) {
        msg.append(inputMessage);
    }

    /**
     * Interrupt the message parsing thread and release all its resources.
     */
    public void destroy() {
        if (this.messageThread.isAlive()) {
            this.messageThread.interrupt();
            this.messageThread = null;

        }
    }

}
