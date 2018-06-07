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
import android.app.Fragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.microchip.android.mcp2221comm.Mcp2221Comm;
import com.microchip.android.mcp2221comm.Mcp2221Config;
import com.microchip.android.mcp2221comm.Mcp2221Constants;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Class containing the MCP2221 pin configuration settings.
 * 
 */
public class PinConfigFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    private static final byte FUNCTION_GPIO = 0;
    private static final byte FUNCTION_DEDICATED = 1;
    private static final byte FUNCTION_ALTERNATE_0 = 2;
    private static final byte FUNCTION_ALTERNATE_1 = 3;
    private static final byte FUNCTION_ALTERNATE_2 = 4;
    private static final byte OUTPUT = 0;
    private static final byte INPUT = 1;
    private static final byte LOW = 0;
    private static final byte HIGH = 1;

    private Mcp2221Comm mMcp2221Comm;

    Spinner spinnerGp0Functions;
    Spinner spinnerGp1Functions;
    Spinner spinnerGp2Functions;
    Spinner spinnerGp3Functions;
    Spinner spinnerGp0Levels;
    Spinner spinnerGp1Levels;
    Spinner spinnerGp2Levels;
    Spinner spinnerGp3Levels;
    Button btnConfigure;

    /** Custom toast - displayed in the center of the screen. */
    private static Toast sToast;

    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static PinConfigFragment newInstance(int sectionNumber) {
        PinConfigFragment fragment = new PinConfigFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    private Mcp2221Config mMcp2221Config = new Mcp2221Config();

    public PinConfigFragment() {
    }

    @Override
    public View
            onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_pin_config, container, false);

        // update the connection status each time the fragment becomes visible
        mMcp2221Comm = ((MainActivity) getActivity()).mcp2221Comm;

        // set up the custom toast; This will appear in the center of the
        // screen and not the default position
        sToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
        sToast.setGravity(Gravity.CENTER, 0, 0);

        /**********************************************************
         * Pin function spinner on item selected listeners
         **********************************************************/
        spinnerGp0Functions = (Spinner) (rootView.findViewById(R.id.spinner_gp0_functions));
        spinnerGp0Levels = (Spinner) (rootView.findViewById(R.id.spinner_gp0_levels));
        spinnerGp1Functions = (Spinner) (rootView.findViewById(R.id.spinner_gp1_functions));
        spinnerGp1Levels = (Spinner) (rootView.findViewById(R.id.spinner_gp1_levels));
        spinnerGp2Functions = (Spinner) (rootView.findViewById(R.id.spinner_gp2_functions));
        spinnerGp2Levels = (Spinner) (rootView.findViewById(R.id.spinner_gp2_levels));
        spinnerGp3Functions = (Spinner) (rootView.findViewById(R.id.spinner_gp3_functions));
        spinnerGp3Levels = (Spinner) (rootView.findViewById(R.id.spinner_gp3_levels));

        btnConfigure = (Button) (rootView.findViewById(R.id.btn_configure));

        btnConfigure.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                setPinSelections();
            }
        });

        // GP0
        spinnerGp0Functions.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, final long id) {
                if (parent.getItemAtPosition(pos).toString().contains("Output")) {
                    spinnerGp0Levels.setVisibility(Spinner.VISIBLE);
                } else {
                    spinnerGp0Levels.setVisibility(Spinner.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        // GP1
        spinnerGp1Functions.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, final long id) {
                if (parent.getItemAtPosition(pos).toString().contains("Output")) {
                    spinnerGp1Levels.setVisibility(Spinner.VISIBLE);
                } else {
                    spinnerGp1Levels.setVisibility(Spinner.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        // GP2
        spinnerGp2Functions.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, final long id) {
                if (parent.getItemAtPosition(pos).toString().contains("Output")) {
                    spinnerGp2Levels.setVisibility(Spinner.VISIBLE);
                } else {
                    spinnerGp2Levels.setVisibility(Spinner.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        // GP3
        spinnerGp3Functions.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, final long id) {
                if (parent.getItemAtPosition(pos).toString().contains("Output")) {
                    spinnerGp3Levels.setVisibility(Spinner.VISIBLE);
                } else {
                    spinnerGp3Levels.setVisibility(Spinner.INVISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        updatePinSelections();

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

    /**
     * Try to read the pin configuration values from a connected MCP2221. If no devices is
     * connected, load the default values.
     */
    public void updatePinSelections() {
        byte[] pinDesignations = new byte[4];
        byte[] pinDirections = new byte[4];
        ByteBuffer pinValues = ByteBuffer.allocate(4);

        // update the connection status
        mMcp2221Comm = ((MainActivity) getActivity()).mcp2221Comm;

        String[] gpFunctionsArray = getResources().getStringArray(R.array.gp0_functions);
        List<String> gpFunctionsArrayList = Arrays.asList(gpFunctionsArray);

        String[] gpLevelsArray = getResources().getStringArray(R.array.output_levels);
        List<String> gpLevelsArrayList = Arrays.asList(gpLevelsArray);

        if (mMcp2221Comm == null) {
            // load default settings
            spinnerGp0Functions.setSelection(gpFunctionsArrayList.indexOf("SSPND"));
            spinnerGp1Functions.setSelection(gpFunctionsArrayList.indexOf("LED UTX"));
            spinnerGp2Functions.setSelection(gpFunctionsArrayList.indexOf("USBCFG"));
            spinnerGp3Functions.setSelection(gpFunctionsArrayList.indexOf("LED I2C"));

            sToast.setText("Default Pin Configuration Loaded.");
            sToast.show();
        } else {
            if (mMcp2221Comm.getSRamSettings(mMcp2221Config) == 0) {
                // if the read was successful then update the UI with the values read from the
                // MCP2221
                pinDesignations = mMcp2221Config.getGpPinDesignations();
                pinDirections = mMcp2221Config.getGpPinDirections();
                // pinValues = mMcp2221Config.getGpPinValues();
                mMcp2221Comm.getGpPinValue(pinValues);

                sToast.setText("MCP2221 Pin Configuration read from device.");
                sToast.show();
                // ******************************
                // update GP0 function spinner
                // *******************************
                switch (pinDesignations[0]) {
                // if it's configured as a GPIO
                    case 0:
                        // get the direction
                        switch (pinDirections[0]) {
                        // if it's an output, get the current level
                            case 0:
                                spinnerGp0Functions.setSelection(gpFunctionsArrayList
                                        .indexOf("Output"));
                                // update the value of the pin as well
                                switch (pinValues.get(0)) {
                                    case 0:
                                        spinnerGp0Levels.setSelection(gpLevelsArrayList
                                                .indexOf("Low"));
                                        break;
                                    case 1:
                                        spinnerGp0Levels.setSelection(gpLevelsArrayList
                                                .indexOf("High"));
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            // for inputs just update the function spinner
                            case 1:
                                spinnerGp0Functions.setSelection(gpFunctionsArrayList
                                        .indexOf("Input"));
                                break;

                            default:
                                break;
                        }
                        ;

                        break;
                    case 1:
                        spinnerGp0Functions.setSelection(gpFunctionsArrayList.indexOf("SSPND"));
                        break;
                    case 2:
                        spinnerGp0Functions.setSelection(gpFunctionsArrayList.indexOf("LED URX"));
                        break;
                    default:
                        break;
                }

                // ******************************
                // update GP1 function spinner
                // *******************************

                gpFunctionsArray = getResources().getStringArray(R.array.gp1_functions);
                gpFunctionsArrayList = Arrays.asList(gpFunctionsArray);
                switch (pinDesignations[1]) {
                // if it's configured as a GPIO
                    case 0:
                        // get the direction
                        switch (pinDirections[1]) {
                        // if it's an output, get the current level
                            case 0:
                                spinnerGp1Functions.setSelection(gpFunctionsArrayList
                                        .indexOf("Output"));
                                // update the value of the pin as well
                                switch (pinValues.get(1)) {
                                    case 0:
                                        spinnerGp1Levels.setSelection(gpLevelsArrayList
                                                .indexOf("Low"));
                                        break;
                                    case 1:
                                        spinnerGp1Levels.setSelection(gpLevelsArrayList
                                                .indexOf("High"));
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            // for inputs just update the function spinner
                            case 1:
                                spinnerGp1Functions.setSelection(gpFunctionsArrayList
                                        .indexOf("Input"));
                                break;

                            default:
                                break;
                        }
                        ;

                        break;
                    case 1:
                        spinnerGp1Functions.setSelection(gpFunctionsArrayList.indexOf("CLK OUT"));
                        break;
                    case 2:
                        spinnerGp1Functions.setSelection(gpFunctionsArrayList.indexOf("ADC1"));
                        break;
                    case 3:
                        spinnerGp1Functions.setSelection(gpFunctionsArrayList.indexOf("LED UTX"));
                        break;
                    case 4:
                        spinnerGp1Functions.setSelection(gpFunctionsArrayList.indexOf("IOC"));
                        break;
                    default:
                        break;
                }

                // ******************************
                // update GP2 function spinner
                // *******************************

                gpFunctionsArray = getResources().getStringArray(R.array.gp2_functions);
                gpFunctionsArrayList = Arrays.asList(gpFunctionsArray);
                switch (pinDesignations[2]) {
                // if it's configured as a GPIO
                    case 0:
                        // get the direction
                        switch (pinDirections[2]) {
                        // if it's an output, get the current level
                            case 0:
                                spinnerGp2Functions.setSelection(gpFunctionsArrayList
                                        .indexOf("Output"));
                                // update the value of the pin as well
                                switch (pinValues.get(2)) {
                                    case 0:
                                        spinnerGp2Levels.setSelection(gpLevelsArrayList
                                                .indexOf("Low"));
                                        break;
                                    case 1:
                                        spinnerGp2Levels.setSelection(gpLevelsArrayList
                                                .indexOf("High"));
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            // for inputs just update the function spinner
                            case 1:
                                spinnerGp2Functions.setSelection(gpFunctionsArrayList
                                        .indexOf("Input"));
                                break;

                            default:
                                break;
                        }
                        ;

                        break;
                    case 1:
                        spinnerGp2Functions.setSelection(gpFunctionsArrayList.indexOf("USBCFG"));
                        break;
                    case 2:
                        spinnerGp2Functions.setSelection(gpFunctionsArrayList.indexOf("ADC2"));
                        break;
                    case 3:
                        spinnerGp2Functions.setSelection(gpFunctionsArrayList.indexOf("DAC1"));
                        break;
                    default:
                        break;
                }

                // ******************************
                // update GP3 function spinner
                // *******************************

                gpFunctionsArray = getResources().getStringArray(R.array.gp3_functions);
                gpFunctionsArrayList = Arrays.asList(gpFunctionsArray);
                switch (pinDesignations[3]) {
                // if it's configured as a GPIO
                    case 0:
                        // get the direction
                        switch (pinDirections[3]) {
                        // if it's an output, get the current level
                            case 0:
                                spinnerGp3Functions.setSelection(gpFunctionsArrayList
                                        .indexOf("Output"));
                                // update the value of the pin as well
                                switch (pinValues.get(3)) {
                                    case 0:
                                        spinnerGp3Levels.setSelection(gpLevelsArrayList
                                                .indexOf("Low"));
                                        break;
                                    case 1:
                                        spinnerGp3Levels.setSelection(gpLevelsArrayList
                                                .indexOf("High"));
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            // for inputs just update the function spinner
                            case 1:
                                spinnerGp3Functions.setSelection(gpFunctionsArrayList
                                        .indexOf("Input"));
                                break;

                            default:
                                break;
                        }
                        ;

                        break;
                    case 1:
                        spinnerGp3Functions.setSelection(gpFunctionsArrayList.indexOf("LED I2C"));
                        break;
                    case 2:
                        spinnerGp3Functions.setSelection(gpFunctionsArrayList.indexOf("ADC3"));
                        break;
                    case 3:
                        spinnerGp3Functions.setSelection(gpFunctionsArrayList.indexOf("DAC2"));
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * get the currently selected values and try to write then to a connected MCP2221.
     */
    private void setPinSelections() {
        // will contain the spinner function selection
        StringBuilder spinnerSelection = new StringBuilder();

        byte[] pinDesignation = new byte[4];
        byte[] pinDirection = new byte[4];
        byte[] pinValue = new byte[4];

        if (mMcp2221Config == null) {
            mMcp2221Config = new Mcp2221Config();
        }

        // **************************
        // GP0 functions
        // **************************
        spinnerSelection.append(spinnerGp0Functions.getItemAtPosition(
                spinnerGp0Functions.getSelectedItemPosition()).toString());

        switch (spinnerSelection.toString().toLowerCase(Locale.US)) {
            case "output":
                pinDesignation[0] = FUNCTION_GPIO;
                pinDirection[0] = OUTPUT;

                if (spinnerGp0Levels.getItemAtPosition(spinnerGp0Levels.getSelectedItemPosition())
                        .toString().equalsIgnoreCase("high")) {
                    pinValue[0] = HIGH;
                } else {
                    pinValue[0] = LOW;
                }
                break;

            case "input":
                pinDesignation[0] = FUNCTION_GPIO;
                pinDirection[0] = INPUT;
                break;

            case "sspnd":
                pinDesignation[0] = FUNCTION_DEDICATED;
                break;

            case "led urx":
                pinDesignation[0] = FUNCTION_ALTERNATE_0;
                break;

            default:
                break;
        }

        // **************************
        // GP1 functions
        // **************************

        // clear previous selection
        spinnerSelection.setLength(0);
        spinnerSelection.append(spinnerGp1Functions.getItemAtPosition(
                spinnerGp1Functions.getSelectedItemPosition()).toString());

        switch (spinnerSelection.toString().toLowerCase(Locale.US)) {
            case "output":
                pinDesignation[1] = FUNCTION_GPIO;
                pinDirection[1] = OUTPUT;

                if (spinnerGp1Levels.getItemAtPosition(spinnerGp1Levels.getSelectedItemPosition())
                        .toString().equalsIgnoreCase("high")) {
                    pinValue[1] = HIGH;
                } else {
                    pinValue[1] = LOW;
                }
                break;

            case "input":
                pinDesignation[1] = FUNCTION_GPIO;
                pinDirection[1] = INPUT;
                break;

            case "clk out":
                pinDesignation[1] = FUNCTION_DEDICATED;
                break;

            case "adc1":
                pinDesignation[1] = FUNCTION_ALTERNATE_0;
                break;

            case "led utx":
                pinDesignation[1] = FUNCTION_ALTERNATE_1;
                break;

            case "ioc":
                pinDesignation[1] = FUNCTION_ALTERNATE_2;
                break;
            default:
                break;
        }

        // **************************
        // GP2 functions
        // **************************

        // clear previous selection
        spinnerSelection.setLength(0);
        spinnerSelection.append(spinnerGp2Functions.getItemAtPosition(
                spinnerGp2Functions.getSelectedItemPosition()).toString());

        switch (spinnerSelection.toString().toLowerCase(Locale.US)) {
            case "output":
                pinDesignation[2] = FUNCTION_GPIO;
                pinDirection[2] = OUTPUT;

                if (spinnerGp2Levels.getItemAtPosition(spinnerGp2Levels.getSelectedItemPosition())
                        .toString().equalsIgnoreCase("high")) {
                    pinValue[2] = HIGH;
                } else {
                    pinValue[2] = LOW;
                }
                break;

            case "input":
                pinDesignation[2] = FUNCTION_GPIO;
                pinDirection[2] = INPUT;
                break;

            case "usbcfg":
                pinDesignation[2] = FUNCTION_DEDICATED;
                break;

            case "adc2":
                pinDesignation[2] = FUNCTION_ALTERNATE_0;
                break;

            case "dac1":
                pinDesignation[2] = FUNCTION_ALTERNATE_1;
                break;

            default:
                break;
        }

        // **************************
        // GP3 functions
        // **************************

        // clear previous selection
        spinnerSelection.setLength(0);
        spinnerSelection.append(spinnerGp3Functions.getItemAtPosition(
                spinnerGp3Functions.getSelectedItemPosition()).toString());

        switch (spinnerSelection.toString().toLowerCase(Locale.US)) {
            case "output":
                pinDesignation[3] = FUNCTION_GPIO;
                pinDirection[3] = OUTPUT;

                if (spinnerGp3Levels.getItemAtPosition(spinnerGp3Levels.getSelectedItemPosition())
                        .toString().equalsIgnoreCase("high")) {
                    pinValue[3] = HIGH;
                } else {
                    pinValue[3] = LOW;
                }
                break;

            case "input":
                pinDesignation[3] = FUNCTION_GPIO;
                pinDirection[3] = INPUT;
                break;

            case "led i2c":
                pinDesignation[3] = FUNCTION_DEDICATED;
                break;

            case "adc3":
                pinDesignation[3] = FUNCTION_ALTERNATE_0;
                break;

            case "dac2":
                pinDesignation[3] = FUNCTION_ALTERNATE_1;
                break;

            default:
                break;
        }

        // update the connection status before using the object
        mMcp2221Comm = ((MainActivity) getActivity()).mcp2221Comm;

        if (mMcp2221Comm == null) {
            sToast.setText("No MCP2221 Connected");
            sToast.show();
            return;
        } else {

            mMcp2221Config.setGpPinDesignations(pinDesignation);
            mMcp2221Config.setGpPinDirections(pinDirection);
            mMcp2221Config.setGpPinValues(pinValue);

            // update the GP settings
            if (mMcp2221Comm.setSRamSettings(mMcp2221Config, false, false, false, false, false,
                    false, true) == Mcp2221Constants.ERROR_SUCCESSFUL) {
                sToast.setText("MCP2221 Pin Configuration successfully updated.");
                sToast.show();
            } else {
                sToast.setText("Could not update MCP2221 Pin Configuration.");
                sToast.show();
            }

        }

    }

    public void updateConnectionStatus() {
        // update the connection status each time the fragment becomes visible
        mMcp2221Comm = ((MainActivity) getActivity()).mcp2221Comm;
    }
}
