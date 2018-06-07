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
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.microchip.android.mcp2221comm.Mcp2221Comm;
import com.microchip.android.mcp2221comm.Mcp2221Config;
import com.microchip.android.mcp2221comm.Mcp2221Constants;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Class containing the MCP2221 pin functions settings.
 * 
 */
@SuppressLint("ShowToast")
public class PinFunctionsFragment extends Fragment {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    private Mcp2221Comm mMcp2221Comm;
    private Mcp2221Config mMcp2221Config = new Mcp2221Config();

    /** Custom toast - displayed in the center of the screen. */
    private static Toast sToast;

    private ToggleButton toggleGP0;
    private ToggleButton toggleGP1;
    private ToggleButton toggleGP2;
    private ToggleButton toggleGP3;
    private Spinner spinnerGpReadWrite;
    private Button btnGpioReadWriteAll;
    private Button btnGpioGP0;
    private Button btnGpioGP1;
    private Button btnGpioGP2;
    private Button btnGpioGP3;

    private Spinner spinnerAdcVref;
    private Button btnGp1Adc0;
    private Button btnGp2Adc1;
    private Button btnGp3Adc2;
    private Button btnAdcReadAll;
    private Button btnAdcSetvref;
    private TextView txtAdcGp1;
    private TextView txtAdcGp2;
    private TextView txtAdcGp3;

    private Button btnClkSet;
    private Spinner spinnerClkFrequency;
    private Spinner spinnerClkDutyCycle;

    private Button btnSetDacValue;
    private SeekBar seekBarDacValue;
    private Spinner spinnerDacVref;
    private TextView lblDacValue;
    private Button btnDacSetVref;
    /**
     * The fragment argument representing the section number for this fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static PinFunctionsFragment newInstance(int sectionNumber) {
        PinFunctionsFragment fragment = new PinFunctionsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public PinFunctionsFragment() {
    }

    @Override
    public View
            onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_pin_functions, container, false);
        // update the connection status each time the fragment becomes visible
        mMcp2221Comm = ((MainActivity) getActivity()).mcp2221Comm;

        // set up the custom toast; This will appear in the center of the
        // screen and not the default position
        sToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
        sToast.setGravity(Gravity.CENTER, 0, 0);

        // get the listview
        expListView = (ExpandableListView) rootView.findViewById(R.id.listPinFunctions);

        // preparing list data
        prepareListData();

        listAdapter =
                new ExpandableListAdapter(rootView.getContext(), listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);
        return rootView;
    }

    /*
     * Preparing the list data
     */
    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        listDataHeader.add("ADC");
        listDataHeader.add("Clock Out");
        listDataHeader.add("DAC");
        listDataHeader.add("GPIO");

        // Adding child data
        List<String> childList = new ArrayList<String>();
        // we only need one child for each expandable list entry
        childList.add("");

        listDataChild.put(listDataHeader.get(0), childList);
        listDataChild.put(listDataHeader.get(1), childList);
        listDataChild.put(listDataHeader.get(2), childList);
        listDataChild.put(listDataHeader.get(3), childList);
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
        getActivity().getActionBar().setTitle(R.string.title_section3_pin_functions);
    }

    public class SimplerExpandableListAdapter extends BaseExpandableListAdapter {
        private Context mContext;
        private String[][] mContents;
        private String[] mTitles;

        public SimplerExpandableListAdapter(Context context, String[] titles, String[][] contents) {
            super();
            if (titles.length != contents.length) {
                throw new IllegalArgumentException("Titles and Contents must be the same size.");
            }

            mContext = context;
            mContents = contents;
            mTitles = titles;
        }

        @Override
        public String getChild(int groupPosition, int childPosition) {
            return mContents[groupPosition][childPosition];
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                View convertView, ViewGroup parent) {
            TextView row = (TextView) convertView;
            if (row == null) {
                row = new TextView(mContext);
            }
            row.setText(mContents[groupPosition][childPosition]);
            return row;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mContents[groupPosition].length;
        }

        @Override
        public String[] getGroup(int groupPosition) {
            return mContents[groupPosition];
        }

        @Override
        public int getGroupCount() {
            return mContents.length;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                ViewGroup parent) {
            TextView row = (TextView) convertView;
            if (row == null) {
                row = new TextView(mContext);
            }
            row.setTypeface(Typeface.DEFAULT_BOLD);
            row.setText(mTitles[groupPosition]);
            return row;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }

    public class ExpandableListAdapter extends BaseExpandableListAdapter {

        private View gpioView = null;
        private View adcView = null;
        private View clkView = null;
        private View dacView = null;

        private Context _context;
        private List<String> _listDataHeader; // header titles
        // child data in format of header title, child title
        private HashMap<String, List<String>> _listDataChild;

        public boolean isAdcVisible() {
            if (adcView == null) {
                return false;
            }
            if (adcView.getVisibility() == View.VISIBLE) {
                return true;
            } else {
                return false;
            }
        }

        public boolean isClkVisible() {
            if (clkView == null) {
                return false;
            }
            if (clkView.getVisibility() == View.VISIBLE) {
                return true;
            } else {
                return false;
            }
        }

        public boolean isDacVisible() {
            if (dacView == null) {
                return false;
            }
            if (dacView.getVisibility() == View.VISIBLE) {
                return true;
            } else {
                return false;
            }
        }

        public boolean isGpioVisible() {
            if (gpioView == null) {
                return false;
            }
            if (gpioView.getVisibility() == View.VISIBLE) {
                return true;
            } else {
                return false;
            }
        }

        public ExpandableListAdapter(Context context, List<String> listDataHeader,
                HashMap<String, List<String>> listChildData) {
            this._context = context;
            this._listDataHeader = listDataHeader;
            this._listDataChild = listChildData;
        }

        @Override
        public void onGroupExpanded(int groupPosition) {
            switch (groupPosition) {
                case 0:
                    if (isAdcVisible()) {
                        updateAdc();
                    }
                    break;

                case 1:
                    if (isClkVisible()) {
                        updateClock();
                    }
                    break;

                case 2:
                    if (isDacVisible()) {
                        updateDac();
                    }
                    break;

                case 3:
                    if (isGpioVisible()) {
                        updateGpio();
                    }
                    break;

                default:
                    break;
            }
            super.onGroupExpanded(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosititon) {
            return this._listDataChild.get(this._listDataHeader.get(groupPosition)).get(
                    childPosititon);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        // mv
        @Override
        public int getChildType(int groupPosition, int childPosition) {
            return groupPosition;
        }

        @Override
        public int getChildTypeCount() {
            // forced it to 4 so it matches the number of items in the list.
            return 4;
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition, boolean isLastChild,
                View convertView, ViewGroup parent) {

            LayoutInflater infalInflater =
                    (LayoutInflater) this._context
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            switch (groupPosition) {
            // ADC
                case 0:

                    if (adcView == null) {
                        adcView = infalInflater.inflate(R.layout.list_item_adc, parent, false);

                        spinnerAdcVref = (Spinner) adcView.findViewById(R.id.spinnerVrefAdc);
                        btnGp1Adc0 = (Button) adcView.findViewById(R.id.btn_GP1_ADC0);
                        btnGp2Adc1 = (Button) adcView.findViewById(R.id.btn_GP2_ADC1);
                        btnGp3Adc2 = (Button) adcView.findViewById(R.id.btn_GP3_ADC2);
                        btnAdcReadAll = (Button) adcView.findViewById(R.id.btnAdcReadAll);
                        btnAdcSetvref = (Button) adcView.findViewById(R.id.btnSetVrefAdc);
                        txtAdcGp1 = (TextView) adcView.findViewById(R.id.txtAdcGp1);
                        txtAdcGp2 = (TextView) adcView.findViewById(R.id.txtAdcGp2);
                        txtAdcGp3 = (TextView) adcView.findViewById(R.id.txtAdcGp3);

                        updateAdc();
                    }
                    convertView = adcView;

                    // **************************
                    // btnAdcSetVref on click
                    // **************************
                    btnAdcSetvref.setOnClickListener(new Button.OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            if (mMcp2221Comm == null) {
                                sToast.setText("No MCP2221 Connected");
                                sToast.show();
                                return;
                            } else {
                                StringBuilder vrefSpinnerValue = new StringBuilder();
                                int result;

                                vrefSpinnerValue.append(spinnerAdcVref.getItemAtPosition(
                                        spinnerAdcVref.getSelectedItemPosition()).toString());

                                switch (vrefSpinnerValue.toString()) {
                                    case "Vdd":
                                        result =
                                                mMcp2221Comm
                                                        .setAdcVoltageReference(Mcp2221Comm.VRefValue.VREF_VDD);
                                        break;
                                    case "1.024V":
                                        result =
                                                mMcp2221Comm
                                                        .setAdcVoltageReference(Mcp2221Comm.VRefValue.VREF_1024MV);
                                        break;
                                    case "2.048V":
                                        result =
                                                mMcp2221Comm
                                                        .setAdcVoltageReference(Mcp2221Comm.VRefValue.VREF_2048MV);
                                        break;
                                    case "4.096V":
                                        result =
                                                mMcp2221Comm
                                                        .setAdcVoltageReference(Mcp2221Comm.VRefValue.VREF_4096MV);
                                        break;

                                    default:
                                        result =
                                                mMcp2221Comm
                                                        .setAdcVoltageReference(Mcp2221Comm.VRefValue.VREF_1024MV);
                                        break;
                                }

                                if (result == Mcp2221Constants.ERROR_SUCCESSFUL) {
                                    sToast.setText("ADC Vref Set.");
                                    sToast.show();
                                } else {
                                    sToast.setText("Could not set ADC Vref.");
                                    sToast.show();
                                }
                            }

                        }
                    });

                    // *************************
                    // btn ADC Read GP1 on click
                    // *************************
                    btnGp1Adc0.setOnClickListener(new Button.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if (mMcp2221Comm == null) {
                                sToast.setText("No MCP2221 Connected");
                                sToast.show();
                                return;
                            } else {
                                IntBuffer adcData = IntBuffer.allocate(3);

                                if (mMcp2221Comm.getAdcData(adcData) == Mcp2221Constants.ERROR_SUCCESSFUL) {
                                    sToast.setText("Read GP1.");
                                    sToast.show();
                                    txtAdcGp1.setText(String.format("0x%03X", adcData.get(0)));
                                }
                            }
                        }
                    });

                    // *************************
                    // btn ADC Read GP2 on click
                    // *************************
                    btnGp2Adc1.setOnClickListener(new Button.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if (mMcp2221Comm == null) {
                                sToast.setText("No MCP2221 Connected");
                                sToast.show();
                                return;
                            } else {
                                IntBuffer adcData = IntBuffer.allocate(3);

                                if (mMcp2221Comm.getAdcData(adcData) == Mcp2221Constants.ERROR_SUCCESSFUL) {
                                    sToast.setText("Read GP2.");
                                    sToast.show();
                                    txtAdcGp2.setText(String.format("0x%03X", adcData.get(1)));
                                }
                            }
                        }
                    });

                    // *************************
                    // btn ADC Read GP3 on click
                    // *************************
                    btnGp3Adc2.setOnClickListener(new Button.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if (mMcp2221Comm == null) {
                                sToast.setText("No MCP2221 Connected");
                                sToast.show();
                                return;
                            } else {
                                IntBuffer adcData = IntBuffer.allocate(3);

                                if (mMcp2221Comm.getAdcData(adcData) == Mcp2221Constants.ERROR_SUCCESSFUL) {
                                    sToast.setText("Read GP3.");
                                    sToast.show();
                                    txtAdcGp3.setText(String.format("0x%03X", adcData.get(2)));
                                }
                            }
                        }
                    });

                    // *************************
                    // btn ADC Read All on click
                    // *************************
                    btnAdcReadAll.setOnClickListener(new Button.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if (mMcp2221Comm == null) {
                                sToast.setText("No MCP2221 Connected");
                                sToast.show();
                                return;
                            } else {
                                IntBuffer adcData = IntBuffer.allocate(3);

                                if (mMcp2221Comm.getAdcData(adcData) == Mcp2221Constants.ERROR_SUCCESSFUL) {
                                    sToast.setText("Read ADC pins.");
                                    sToast.show();
                                    // if the button is enabled then the pin is configured as an ADC
                                    // input
                                    if (btnGp1Adc0.isEnabled()) {
                                        txtAdcGp1.setText(String.format("0x%03X", adcData.get(0)));
                                    }
                                    if (btnGp2Adc1.isEnabled()) {
                                        txtAdcGp2.setText(String.format("0x%03X", adcData.get(1)));
                                    }
                                    if (btnGp3Adc2.isEnabled()) {
                                        txtAdcGp3.setText(String.format("0x%03X", adcData.get(2)));
                                    }
                                }
                            }
                        }
                    });
                    break;

                // Clock out
                case 1:
                    if (clkView == null) {
                        clkView = infalInflater.inflate(R.layout.list_item_clock, null);

                        btnClkSet = (Button) clkView.findViewById(R.id.btnClkSet);
                        spinnerClkFrequency = (Spinner) clkView.findViewById(R.id.spinnerFrequency);
                        spinnerClkDutyCycle = (Spinner) clkView.findViewById(R.id.spinnerDutyCycle);
                        updateClock();

                    }

                    convertView = clkView;

                    // **************************
                    // btn ClkSet on click
                    // **************************
                    btnClkSet.setOnClickListener(new Button.OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            if (mMcp2221Comm == null) {
                                sToast.setText("No MCP2221 Connected");
                                sToast.show();
                                return;
                            } else {
                                StringBuilder dutyCycleSelection = new StringBuilder();
                                StringBuilder frequencySelection = new StringBuilder();

                                byte dutyCycle = 2;
                                byte clockDivider = 2;

                                dutyCycleSelection.append(spinnerClkDutyCycle.getItemAtPosition(
                                        spinnerClkDutyCycle.getSelectedItemPosition()).toString());

                                frequencySelection.append(spinnerClkFrequency.getItemAtPosition(
                                        spinnerClkFrequency.getSelectedItemPosition()).toString());

                                switch (dutyCycleSelection.toString()) {
                                    case "0 %":
                                        dutyCycle = 0;
                                        break;

                                    case "25 %":
                                        dutyCycle = 1;
                                        break;

                                    case "50 %":
                                        dutyCycle = 2;
                                        break;

                                    case "75 %":
                                        dutyCycle = 3;
                                        break;

                                    default:
                                        break;
                                }

                                switch (frequencySelection.toString()) {
                                    case "24 MHz":
                                        clockDivider = 1;
                                        break;

                                    case "12 MHz":
                                        clockDivider = 2;
                                        break;

                                    case "6 MHz":
                                        clockDivider = 3;
                                        break;

                                    case "3 MHz":
                                        clockDivider = 4;
                                        break;

                                    case "1.5 MHz":
                                        clockDivider = 5;
                                        break;

                                    case "750 kHz":
                                        clockDivider = 6;
                                        break;

                                    case "375 kHz":
                                        clockDivider = 7;
                                        break;

                                    default:
                                        break;
                                }

                                if (mMcp2221Comm.setClockPinConfiguration(clockDivider, dutyCycle) == Mcp2221Constants.ERROR_SUCCESSFUL) {
                                    sToast.setText("Clock Out Settings updated.");
                                    sToast.show();
                                } else {
                                    sToast.setText("Could not update Clock Out Settings.");
                                    sToast.show();
                                }

                            }
                        }
                    });

                    break;

                // DAC
                case 2:
                    if (dacView == null) {
                        dacView = infalInflater.inflate(R.layout.list_item_dac, null);

                        btnSetDacValue = (Button) dacView.findViewById(R.id.btnSetDac);
                        btnDacSetVref = (Button) dacView.findViewById(R.id.btnSetVrefDac);
                        seekBarDacValue = (SeekBar) dacView.findViewById(R.id.seekBarDacValue);
                        spinnerDacVref = (Spinner) dacView.findViewById(R.id.spinnerVrefDac);
                        lblDacValue = (TextView) dacView.findViewById(R.id.lblDacValue);
                        updateDac();
                    }

                    convertView = dacView;

                    // ****************************
                    // seek Bar DAC VAlue changed
                    // ****************************
                    seekBarDacValue.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress,
                                boolean fromUser) {
                            // limit the min value to 1
                            if (progress < 1) {
                                progress = 1;
                            }
                            lblDacValue.setText("Value: " + progress);

                        }
                    });

                    // ****************************
                    // btn Set Dac Vref on click
                    // ****************************

                    btnDacSetVref.setOnClickListener(new Button.OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            if (mMcp2221Comm == null) {
                                sToast.setText("No MCP2221 Connected");
                                sToast.show();
                                return;
                            } else {
                                StringBuilder vrefSpinnerValue = new StringBuilder();
                                int result;

                                vrefSpinnerValue.append(spinnerDacVref.getItemAtPosition(
                                        spinnerDacVref.getSelectedItemPosition()).toString());

                                switch (vrefSpinnerValue.toString()) {
                                    case "Vdd":
                                        result =
                                                mMcp2221Comm
                                                        .setDacVoltageReference(Mcp2221Comm.VRefValue.VREF_VDD);
                                        break;
                                    case "1.024V":
                                        result =
                                                mMcp2221Comm
                                                        .setDacVoltageReference(Mcp2221Comm.VRefValue.VREF_1024MV);
                                        break;
                                    case "2.048V":
                                        result =
                                                mMcp2221Comm
                                                        .setDacVoltageReference(Mcp2221Comm.VRefValue.VREF_2048MV);
                                        break;
                                    case "4.096V":
                                        result =
                                                mMcp2221Comm
                                                        .setDacVoltageReference(Mcp2221Comm.VRefValue.VREF_4096MV);
                                        break;

                                    default:
                                        result =
                                                mMcp2221Comm
                                                        .setDacVoltageReference(Mcp2221Comm.VRefValue.VREF_VDD);
                                        break;
                                }

                                if (result == Mcp2221Constants.ERROR_SUCCESSFUL) {
                                    sToast.setText("DAC Vref Set.");
                                    sToast.show();
                                } else {
                                    sToast.setText("Could not set DAC Vref.");
                                    sToast.show();
                                }
                            }
                        }
                    });

                    // *****************************
                    // btn set DAC value on click
                    // ******************************
                    btnSetDacValue.setOnClickListener(new Button.OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            if (mMcp2221Comm == null) {
                                sToast.setText("No MCP2221 Connected");
                                sToast.show();
                                return;
                            } else {
                                byte dacValue = (byte) seekBarDacValue.getProgress();
                                if (mMcp2221Comm.setDacValue(dacValue) == Mcp2221Constants.ERROR_SUCCESSFUL) {
                                    sToast.setText("DAC Value Set.");
                                    sToast.show();
                                } else {
                                    sToast.setText("Could not set DAC Value.");
                                    sToast.show();
                                }
                            }
                        }
                    });

                    break;

                // GPIO
                case 3:
                    if (gpioView == null) {
                        gpioView = infalInflater.inflate(R.layout.list_item_gpio, null);

                        spinnerGpReadWrite =
                                (Spinner) (gpioView.findViewById(R.id.spinner_gp_read_write));
                        toggleGP0 = (ToggleButton) (gpioView.findViewById(R.id.toggleGP0));
                        toggleGP1 = (ToggleButton) (gpioView.findViewById(R.id.toggleGP1));
                        toggleGP2 = (ToggleButton) (gpioView.findViewById(R.id.toggleGP2));
                        toggleGP3 = (ToggleButton) (gpioView.findViewById(R.id.toggleGP3));

                        btnGpioReadWriteAll = (Button) (gpioView.findViewById(R.id.btnReadAll));
                        btnGpioGP0 = (Button) (gpioView.findViewById(R.id.btn_GP0));
                        btnGpioGP1 = (Button) (gpioView.findViewById(R.id.btn_GP1));
                        btnGpioGP2 = (Button) (gpioView.findViewById(R.id.btn_GP2));
                        btnGpioGP3 = (Button) (gpioView.findViewById(R.id.btn_GP3));

                        updateGpio();
                    }

                    convertView = gpioView;

                    spinnerGpReadWrite.setOnItemSelectedListener(new OnItemSelectedListener() {
                        @SuppressLint("NewApi")
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                final long id) {

                            if (parent.getItemAtPosition(pos).toString().contains("Read")) {
                                updateGpio();

                                spinnerGpReadWrite.setVisibility(Spinner.VISIBLE);
                                btnGpioReadWriteAll.setText("Read All");

                                btnGpioReadWriteAll.setTag("read");
                                btnGpioGP0.setTag("read");
                                btnGpioGP1.setTag("read");
                                btnGpioGP2.setTag("read");
                                btnGpioGP3.setTag("read");

                            } else {
                                updateGpio();
                                // change the button text to "Write all"
                                btnGpioReadWriteAll.setText("Write All");

                                btnGpioReadWriteAll.setTag("write");
                                btnGpioGP0.setTag("write");
                                btnGpioGP1.setTag("write");
                                btnGpioGP2.setTag("write");
                                btnGpioGP3.setTag("write");
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> arg0) {
                            // TODO Auto-generated method stub
                        }
                    });

                    // *****************************
                    // btn GP0 on click
                    // ******************************
                    btnGpioGP0.setOnClickListener(new Button.OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            if (mMcp2221Comm == null) {
                                sToast.setText("No MCP2221 Connected");
                                sToast.show();
                                return;
                            } else {
                                if (btnGpioGP0.getTag().toString().equals("read")) {
                                    switch (mMcp2221Comm.getGpPinValue((byte) 0)) {
                                        case 0:
                                            toggleGP0.setChecked(false);
                                            sToast.setText("Read GP0.");
                                            sToast.show();
                                            break;
                                        case 1:
                                            toggleGP0.setChecked(true);
                                            sToast.setText("Read GP0.");
                                            sToast.show();
                                            break;
                                        default:
                                            sToast.setText("GP0 not configured as GPIO.");
                                            sToast.show();
                                            break;
                                    }
                                } else {
                                    int result = -1;
                                    if (toggleGP0.isChecked()) {
                                        // set GP0 high
                                        result = mMcp2221Comm.setGpPinValue((byte) 0, (byte) 1);
                                    } else {
                                        // set GP0 low
                                        result = mMcp2221Comm.setGpPinValue((byte) 0, (byte) 0);
                                    }

                                    if (result == Mcp2221Constants.ERROR_SUCCESSFUL) {
                                        sToast.setText("GP0 updated.");
                                        sToast.show();
                                    } else {
                                        sToast.setText("Could not write GP0");
                                        sToast.show();
                                    }
                                }
                            }
                        }
                    });

                    // *****************************
                    // btn GP1 on click
                    // ******************************
                    btnGpioGP1.setOnClickListener(new Button.OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            if (mMcp2221Comm == null) {
                                sToast.setText("No MCP2221 Connected");
                                sToast.show();
                                return;
                            } else {
                                if (btnGpioGP1.getTag().toString().equals("read")) {
                                    switch (mMcp2221Comm.getGpPinValue((byte) 1)) {
                                        case 0:
                                            toggleGP1.setChecked(false);
                                            sToast.setText("Read GP1.");
                                            sToast.show();
                                            break;
                                        case 1:
                                            toggleGP1.setChecked(true);
                                            sToast.setText("Read GP1.");
                                            sToast.show();
                                            break;
                                        default:
                                            sToast.setText("GP1 not configured as GPIO.");
                                            sToast.show();
                                            break;
                                    }
                                } else {
                                    int result = -1;
                                    if (toggleGP1.isChecked()) {
                                        // set GP1 high
                                        result = mMcp2221Comm.setGpPinValue((byte) 1, (byte) 1);
                                    } else {
                                        // set GP0 low
                                        result = mMcp2221Comm.setGpPinValue((byte) 1, (byte) 0);
                                    }

                                    if (result == Mcp2221Constants.ERROR_SUCCESSFUL) {
                                        sToast.setText("GP1 updated.");
                                        sToast.show();
                                    } else {
                                        sToast.setText("Could not write GP1");
                                        sToast.show();
                                    }
                                }
                            }
                        }
                    });

                    // *****************************
                    // btn GP2 on click
                    // ******************************
                    btnGpioGP2.setOnClickListener(new Button.OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            if (mMcp2221Comm == null) {
                                sToast.setText("No MCP2221 Connected");
                                sToast.show();
                                return;
                            } else {
                                if (btnGpioGP2.getTag().toString().equals("read")) {
                                    switch (mMcp2221Comm.getGpPinValue((byte) 2)) {
                                        case 0:
                                            toggleGP2.setChecked(false);
                                            sToast.setText("Read GP2.");
                                            sToast.show();
                                            break;
                                        case 1:
                                            toggleGP2.setChecked(true);
                                            sToast.setText("Read GP2.");
                                            sToast.show();
                                            break;
                                        default:
                                            sToast.setText("GP2 not configured as GPIO.");
                                            sToast.show();
                                            break;
                                    }
                                } else {
                                    int result = -1;
                                    if (toggleGP2.isChecked()) {
                                        // set GP2 high
                                        result = mMcp2221Comm.setGpPinValue((byte) 2, (byte) 1);
                                    } else {
                                        // set GP0 low
                                        result = mMcp2221Comm.setGpPinValue((byte) 2, (byte) 0);
                                    }

                                    if (result == Mcp2221Constants.ERROR_SUCCESSFUL) {
                                        sToast.setText("GP2 updated.");
                                        sToast.show();
                                    } else {
                                        sToast.setText("Could not write GP2");
                                        sToast.show();
                                    }
                                }
                            }
                        }
                    });

                    // *****************************
                    // btn GP3 on click
                    // ******************************
                    btnGpioGP3.setOnClickListener(new Button.OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            if (mMcp2221Comm == null) {
                                sToast.setText("No MCP2221 Connected");
                                sToast.show();
                                return;
                            } else {
                                if (btnGpioGP3.getTag().toString().equals("read")) {
                                    switch (mMcp2221Comm.getGpPinValue((byte) 3)) {
                                        case 0:
                                            toggleGP3.setChecked(false);
                                            sToast.setText("Read GP3.");
                                            sToast.show();
                                            break;
                                        case 1:
                                            toggleGP3.setChecked(true);
                                            sToast.setText("Read GP3.");
                                            sToast.show();
                                            break;
                                        default:
                                            sToast.setText("GP3 not configured as GPIO.");
                                            sToast.show();
                                            break;
                                    }
                                } else {
                                    int result = -1;
                                    if (toggleGP3.isChecked()) {
                                        // set GP1 high
                                        result = mMcp2221Comm.setGpPinValue((byte) 3, (byte) 1);
                                    } else {
                                        // set GP0 low
                                        result = mMcp2221Comm.setGpPinValue((byte) 3, (byte) 0);
                                    }

                                    if (result == Mcp2221Constants.ERROR_SUCCESSFUL) {
                                        sToast.setText("GP3 updated.");
                                        sToast.show();
                                    } else {
                                        sToast.setText("Could not write GP3");
                                        sToast.show();
                                    }
                                }
                            }
                        }
                    });

                    // *****************************
                    // btn Read/Write All on click
                    // ******************************
                    btnGpioReadWriteAll.setOnClickListener(new Button.OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            if (mMcp2221Comm == null) {
                                sToast.setText("No MCP2221 Connected");
                                sToast.show();
                                return;
                            } else {
                                if (btnGpioReadWriteAll.getTag().equals("read")) {
                                    updateGpio();
                                } else {
                                    int result = -1;
                                    ByteBuffer pinValues = ByteBuffer.allocate(4);

                                    if (toggleGP0.isChecked()) {
                                        // set GP0 high
                                        pinValues.put(0, (byte) 1);
                                    } else {
                                        // set GP0 low
                                        pinValues.put(0, (byte) 0);
                                    }

                                    if (toggleGP1.isChecked()) {
                                        // set GP1 high
                                        pinValues.put(1, (byte) 1);
                                    } else {
                                        // set GP1 low
                                        pinValues.put(1, (byte) 0);
                                    }

                                    if (toggleGP2.isChecked()) {
                                        // set GP2 high
                                        pinValues.put(2, (byte) 1);
                                    } else {
                                        // set GP2 low
                                        pinValues.put(2, (byte) 0);
                                    }

                                    if (toggleGP3.isChecked()) {
                                        // set GP3 high
                                        pinValues.put(3, (byte) 1);
                                    } else {
                                        // set GP3 low
                                        pinValues.put(3, (byte) 0);
                                    }

                                    result = mMcp2221Comm.setGpPinValue(pinValues);

                                    if (result == Mcp2221Constants.ERROR_SUCCESSFUL) {
                                        sToast.setText("GPIO pins updated.");
                                        sToast.show();
                                    } else {
                                        sToast.setText("Could not write GPIO pins");
                                        sToast.show();
                                    }
                                }
                            }
                        }
                    });
                    break;

                default:
                    break;
            }
            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return this._listDataChild.get(this._listDataHeader.get(groupPosition)).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return this._listDataHeader.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return this._listDataHeader.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                ViewGroup parent) {
            String headerTitle = (String) getGroup(groupPosition);
            if (convertView == null) {
                LayoutInflater infalInflater =
                        (LayoutInflater) this._context
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.list_group, null);
            }

            TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
            lblListHeader.setTypeface(null, Typeface.BOLD);
            lblListHeader.setText(headerTitle);

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

    }

    public void updatePinFunctions() {
        // update the connection status
        if (listAdapter.isAdcVisible()) {
            updateAdc();
        }
        if (listAdapter.isClkVisible()) {
            updateClock();
        }
        if (listAdapter.isDacVisible()) {
            updateDac();
        }

        if (listAdapter.isGpioVisible()) {
            updateGpio();
        }

    }

    private void updateAdc() {
        byte[] pinDesignations = new byte[4];
        byte adcVref;

        String[] adcVrefArray = getResources().getStringArray(R.array.vref);
        List<String> adcVrefArrayList = Arrays.asList(adcVrefArray);

        // update the connection status
        mMcp2221Comm = ((MainActivity) getActivity()).mcp2221Comm;
        if (mMcp2221Comm == null) {
            // load default settings
            spinnerAdcVref.setSelection(adcVrefArrayList.indexOf("1.024V"));
            btnGp1Adc0.setEnabled(false);
            btnGp2Adc1.setEnabled(false);
            btnGp3Adc2.setEnabled(false);
            btnAdcReadAll.setEnabled(false);
        } else {
            if (mMcp2221Comm.getSRamSettings(mMcp2221Config) == 0) {

                sToast.setText("Read ADC Settings");
                sToast.show();

                pinDesignations = mMcp2221Config.getGpPinDesignations();
                adcVref = mMcp2221Config.getAdcVoltageReference();

                // update vref
                switch (adcVref) {
                // Vdd
                    case 0:
                        spinnerAdcVref.setSelection(adcVrefArrayList.indexOf("Vdd"));
                        break;
                    // 1.024V
                    case 0x3:
                        spinnerAdcVref.setSelection(adcVrefArrayList.indexOf("1.024V"));
                        break;
                    // 2.048V
                    case 0x5:
                        spinnerAdcVref.setSelection(adcVrefArrayList.indexOf("2.048V"));
                        break;
                    // 4.096V
                    case 0x7:
                        spinnerAdcVref.setSelection(adcVrefArrayList.indexOf("4.096V"));
                        break;

                    default:
                        break;
                }

                // disable the read buttons if pins aren't configured for ADC function

                // GP1
                if (pinDesignations[1] == 2) {
                    btnGp1Adc0.setEnabled(true);
                } else {
                    btnGp1Adc0.setEnabled(false);
                }

                // GP2
                if (pinDesignations[2] == 2) {
                    btnGp2Adc1.setEnabled(true);
                } else {
                    btnGp2Adc1.setEnabled(false);
                }

                // GP3
                if (pinDesignations[3] == 2) {
                    btnGp3Adc2.setEnabled(true);
                } else {
                    btnGp3Adc2.setEnabled(false);
                }

                // if none of the pins are configured for ADC, let the user know
                if (btnGp1Adc0.isEnabled() == false && btnGp2Adc1.isEnabled() == false
                        && btnGp3Adc2.isEnabled() == false) {
                    sToast.setText("No pin configured for ADC.");
                    sToast.show();
                    // disable read all as well
                    btnAdcReadAll.setEnabled(false);
                } else {
                    // make sure the read all button is enabled
                    btnAdcReadAll.setEnabled(true);
                }

            }

        }
    }

    private void updateClock() {
        byte[] pinDesignations = new byte[4];
        byte dutyCycle;
        byte frequency;

        String[] dutyCycleArray = getResources().getStringArray(R.array.dutyCycle);
        List<String> dutyCycleArrayList = Arrays.asList(dutyCycleArray);

        String[] frequencyArray = getResources().getStringArray(R.array.frequency);
        List<String> frequencyArrayList = Arrays.asList(frequencyArray);

        // update the connection status
        mMcp2221Comm = ((MainActivity) getActivity()).mcp2221Comm;
        if (mMcp2221Comm == null) {
            // load the default settings
            btnClkSet.setEnabled(false);
            spinnerClkDutyCycle.setSelection(dutyCycleArrayList.indexOf("50 %"));
            spinnerClkFrequency.setSelection(frequencyArrayList.indexOf("12 MHz"));
        } else {
            if (mMcp2221Comm.getSRamSettings(mMcp2221Config) == 0) {
                pinDesignations = mMcp2221Config.getGpPinDesignations();
                frequency = mMcp2221Config.getGpClockDividerValue();
                dutyCycle = mMcp2221Config.getGpClockDutyCycle();

                sToast.setText("Read Clock Out Settings.");
                sToast.show();

                // if GP1 = clock out
                if (pinDesignations[1] == 1) {
                    // let the user change the clock settings
                    btnClkSet.setEnabled(true);
                    // update the frequency
                    switch (frequency) {
                        case 1:
                            spinnerClkFrequency.setSelection(frequencyArrayList.indexOf("24 MHz"));
                            break;
                        case 2:
                            spinnerClkFrequency.setSelection(frequencyArrayList.indexOf("12 MHz"));
                            break;
                        case 3:
                            spinnerClkFrequency.setSelection(frequencyArrayList.indexOf("6 MHz"));
                            break;
                        case 4:
                            spinnerClkFrequency.setSelection(frequencyArrayList.indexOf("3 MHz"));
                            break;
                        case 5:
                            spinnerClkFrequency.setSelection(frequencyArrayList.indexOf("1.5 MHz"));
                            break;
                        case 6:
                            spinnerClkFrequency.setSelection(frequencyArrayList.indexOf("750 kHz"));
                            break;
                        case 7:
                            spinnerClkFrequency.setSelection(frequencyArrayList.indexOf("375 kHz"));
                            break;
                        default:
                            break;
                    }

                    // update the duty cycle
                    switch (dutyCycle) {
                        case 0:
                            spinnerClkDutyCycle.setSelection(dutyCycleArrayList.indexOf("0 %"));
                            break;
                        case 1:
                            spinnerClkDutyCycle.setSelection(dutyCycleArrayList.indexOf("25 %"));
                            break;
                        case 2:
                            spinnerClkDutyCycle.setSelection(dutyCycleArrayList.indexOf("50 %"));
                            break;
                        case 3:
                            spinnerClkDutyCycle.setSelection(dutyCycleArrayList.indexOf("75 %"));
                            break;

                        default:
                            break;
                    }
                } else {
                    // let the user know that GP1 isn't configured for clock out and don't let them
                    // reconfigure the clock values
                    btnClkSet.setEnabled(false);
                    sToast.setText("GP1 not configured for clock out.");
                    sToast.show();
                }
            }
        }
    }

    private void updateDac() {
        byte[] pinDesignations = new byte[4];
        byte dacVref;
        byte dacValue;

        String[] dacVrefArray = getResources().getStringArray(R.array.vref);
        List<String> dacVrefArrayList = Arrays.asList(dacVrefArray);

        // update the connection status
        mMcp2221Comm = ((MainActivity) getActivity()).mcp2221Comm;
        if (mMcp2221Comm == null) {
            // load the default settings
            btnSetDacValue.setEnabled(false);
            spinnerDacVref.setSelection(dacVrefArrayList.indexOf("Vdd"));
            seekBarDacValue.setProgress(8);

        } else {
            if (mMcp2221Comm.getSRamSettings(mMcp2221Config) == 0) {
                pinDesignations = mMcp2221Config.getGpPinDesignations();
                dacVref = mMcp2221Config.getDacVoltageReference();
                dacValue = mMcp2221Config.getDacValue();

                sToast.setText("Read DAC Settings.");
                sToast.show();

                // if GP2 or GP3 = DAC pins
                if (pinDesignations[2] == 3 || pinDesignations[3] == 3) {
                    // let the user change the DAC value
                    btnSetDacValue.setEnabled(true);

                    // update vref
                    switch (dacVref) {
                    // Vdd
                        case 0:
                            spinnerDacVref.setSelection(dacVrefArrayList.indexOf("Vdd"));
                            break;
                        // 1.024V
                        case 0x3:
                            spinnerDacVref.setSelection(dacVrefArrayList.indexOf("1.024V"));
                            break;
                        // 2.048V
                        case 0x5:
                            spinnerDacVref.setSelection(dacVrefArrayList.indexOf("2.048V"));
                            break;
                        // 4.096V
                        case 0x7:
                            spinnerDacVref.setSelection(dacVrefArrayList.indexOf("4.096V"));
                            break;

                        default:
                            break;
                    }

                    // update the DAC value
                    seekBarDacValue.setProgress(dacValue);
                    lblDacValue.setText("Value: " + dacValue);

                } else {
                    // let the user know that no pin is configured for DAC and don't let them
                    // reconfigure the value
                    btnSetDacValue.setEnabled(false);
                    sToast.setText("No pin configured for DAC.");
                    sToast.show();
                }
            }
        }
    }

    private void updateGpio() {
        byte[] pinDesignations = new byte[4];
        byte[] pinDirections = new byte[4];
        ByteBuffer pinValues = ByteBuffer.allocate(4);

        // load defaults
        btnGpioGP0.setEnabled(false);
        btnGpioGP1.setEnabled(false);
        btnGpioGP2.setEnabled(false);
        btnGpioGP3.setEnabled(false);
        btnGpioReadWriteAll.setEnabled(false);
        toggleGP0.setEnabled(false);
        toggleGP1.setEnabled(false);
        toggleGP2.setEnabled(false);
        toggleGP3.setEnabled(false);

        // update the connection status
        mMcp2221Comm = ((MainActivity) getActivity()).mcp2221Comm;
        if (mMcp2221Comm == null) {
            // defaults were loaded, so just exit here
            return;
        } else {
            if (mMcp2221Comm.getSRamSettings(mMcp2221Config) == 0) {
                pinDesignations = mMcp2221Config.getGpPinDesignations();
                pinDirections = mMcp2221Config.getGpPinDirections();
                mMcp2221Comm.getGpPinValue(pinValues);

                if (pinDesignations[0] != 0 && pinDesignations[1] != 0 && pinDesignations[2] != 0
                        && pinDesignations[3] != 0) {
                    sToast.setText("No pin configured as GPIO.");
                    sToast.show();
                    return;
                }

                Button[] gpButtons = new Button[4];
                gpButtons[0] = btnGpioGP0;
                gpButtons[1] = btnGpioGP1;
                gpButtons[2] = btnGpioGP2;
                gpButtons[3] = btnGpioGP3;

                ToggleButton[] toggleButtons = new ToggleButton[4];
                toggleButtons[0] = toggleGP0;
                toggleButtons[1] = toggleGP1;
                toggleButtons[2] = toggleGP2;
                toggleButtons[3] = toggleGP3;

                for (int i = 0; i < pinDesignations.length; i++) {
                    if (pinDesignations[i] == 0) {
                        // if the pin is configured for GPIO
                        if (spinnerGpReadWrite.getSelectedItem().toString()
                                .equalsIgnoreCase("read")) {
                            // and we have a read operation
                            // enable the GP and toggle button for that pin
                            gpButtons[i].setEnabled(true);
                            toggleButtons[i].setEnabled(true);
                            // make the toggle not clickable and change its alpha level
                            // so the user can still read the value
                            toggleButtons[i].setClickable(false);
                            toggleButtons[i].setAlpha((float) 0.7);
                        } else {
                            // write operation is selected
                            if (pinDirections[i] == 0) {
                                // only allow writing the pin if its an output
                                gpButtons[i].setEnabled(true);
                                toggleButtons[i].setEnabled(true);
                                // let the user click the pin level toggle button
                                toggleButtons[i].setClickable(true);
                                toggleButtons[i].setAlpha((float) 1);
                            } else {
                                // the pin is configured as an input
                                // don't allow writing to it
                                gpButtons[i].setEnabled(false);
                                toggleButtons[i].setEnabled(false);
                            }
                        }
                        // update the pin level values
                        if (pinValues.get(i) == 1) {
                            toggleButtons[i].setChecked(true);
                        } else {
                            toggleButtons[i].setChecked(false);
                        }
                    } else {
                        // pin not configured as GPIO
                        // disable the buttons for it
                        gpButtons[i].setEnabled(false);
                        toggleButtons[i].setEnabled(false);
                    }
                }

                // if at least one pin is readable/writable, enable the read/write all button
                for (int i = 0; i < gpButtons.length; i++) {
                    if (gpButtons[i].isEnabled()) {
                        btnGpioReadWriteAll.setEnabled(true);
                        break;
                    }
                }

                if (spinnerGpReadWrite.getSelectedItem().equals("Write")
                        && btnGpioGP0.isEnabled() == false && btnGpioGP1.isEnabled() == false
                        && btnGpioGP2.isEnabled() == false && btnGpioGP3.isEnabled() == false) {

                    sToast.setText("No pin configured as Output.");
                    sToast.show();
                    return;
                }

                sToast.setText("Read MCP2221 GPIO pins.");
                sToast.show();
            }
        }
    }

    public void updateConnectionStatus() {
        // update the connection status each time the fragment becomes visible
        mMcp2221Comm = ((MainActivity) getActivity()).mcp2221Comm;
    }
}
