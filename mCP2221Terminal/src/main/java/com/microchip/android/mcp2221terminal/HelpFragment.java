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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Help Fragment.
 * 
 */
public class HelpFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    // constants to keep track of each item's help page index
    private static final int I2C_TERMINAL = 0;
    private static final int PIN_CONFIGURATION = 1;
    private static final int PIN_FUNCTIONS = 2;
    private static final int SERIAL_TERMINAL = 3;
    private static final int KNOWN_ISSUES = 4;

    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static HelpFragment newInstance(int sectionNumber) {
        HelpFragment fragment = new HelpFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public HelpFragment() {
    }

    @Override
    public View
            onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_help, container, false);

        final TextView txtHelp = (TextView) rootView.findViewById(R.id.txtHelp);
        txtHelp.setText(R.string.help_txt);

//        final EditText txtHelpItem1 = (EditText) rootView.findViewById(R.id.txtHelpItem1);
//        final EditText txtHelpItem2 = (EditText) rootView.findViewById(R.id.txtHelpItem2);
//        final EditText txtHelpItem3 = (EditText) rootView.findViewById(R.id.txtHelpItem3);
//
//        Spinner spinnnerHelpItems = (Spinner) rootView.findViewById(R.id.spinnerHelpItens);
//        spinnnerHelpItems.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
//
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int pos, final long id) {
//
//                Drawable tempDrawable;
//                // update the help image text based on the selected index.
//                switch (pos) {
//                    case I2C_TERMINAL:
//                        tempDrawable =
//                                parent.getContext().getResources()
//                                        .getDrawable(R.drawable.help_i2c_terminal);
//                        if (tempDrawable.getIntrinsicWidth() > txtHelp.getWidth()) {
//                            // on some devices the drawable is wider than the screen,
//                            // so resize the drawable so it has a bit of white space around it
//                            int dif = tempDrawable.getIntrinsicWidth() - txtHelp.getWidth() + 20;
//
//                            tempDrawable.setBounds(0, 0, tempDrawable.getIntrinsicWidth() - dif,
//                                    tempDrawable.getIntrinsicHeight() - dif);
//
//                            txtHelp.setCompoundDrawables(null, tempDrawable, null, null);
//                        } else {
//                            txtHelp.setCompoundDrawablesWithIntrinsicBounds(null, tempDrawable,
//                                    null, null);
//                        }
//                        // update the help text
//                        txtHelp.setText(R.string.help_i2c_terminal);
//                        // Highlight any links in the help text
//                        Linkify.addLinks(txtHelp, Linkify.WEB_URLS);
//
//                        // hide any items that shouldn't be visible
//                        txtHelpItem1.setVisibility(View.INVISIBLE);
//                        txtHelpItem2.setVisibility(View.INVISIBLE);
//                        txtHelpItem3.setVisibility(View.INVISIBLE);
//                        break;
//
//                    case PIN_CONFIGURATION:
//                        txtHelp.setText(R.string.help_pin_configuration);
//
//                        tempDrawable =
//                                parent.getContext().getResources()
//                                        .getDrawable(R.drawable.help_pin_configuration);
//                        if (tempDrawable.getIntrinsicWidth() > txtHelp.getWidth()) {
//                            // on some devices the drawable is wider than the screen,
//                            // so resize the drawable so it has a bit of white space around it
//                            int dif = tempDrawable.getIntrinsicWidth() - txtHelp.getWidth() + 20;
//
//                            tempDrawable.setBounds(0, 0, tempDrawable.getIntrinsicWidth() - dif,
//                                    tempDrawable.getIntrinsicHeight() - dif);
//
//                            txtHelp.setCompoundDrawables(null, tempDrawable, null, null);
//                        } else {
//                            txtHelp.setCompoundDrawablesWithIntrinsicBounds(null, tempDrawable,
//                                    null, null);
//                        }
//                        // hide any items that shouldn't be visible
//                        txtHelpItem1.setVisibility(View.INVISIBLE);
//                        txtHelpItem2.setVisibility(View.INVISIBLE);
//                        txtHelpItem3.setVisibility(View.INVISIBLE);
//                        break;
//                    case PIN_FUNCTIONS:
//                        // ADC help section
//                        tempDrawable =
//                                parent.getContext().getResources()
//                                        .getDrawable(R.drawable.help_pin_functions_adc);
//                        if (tempDrawable.getIntrinsicWidth() > txtHelp.getWidth()) {
//                            // on some devices the drawable is wider than the screen,
//                            // so resize the drawable so it has a bit of white space around it
//                            int dif = tempDrawable.getIntrinsicWidth() - txtHelp.getWidth() + 20;
//
//                            tempDrawable.setBounds(0, 0, tempDrawable.getIntrinsicWidth() - dif,
//                                    tempDrawable.getIntrinsicHeight() - dif);
//
//                            txtHelp.setCompoundDrawables(null, tempDrawable, null, null);
//                        } else {
//                            txtHelp.setCompoundDrawablesWithIntrinsicBounds(null, tempDrawable,
//                                    null, null);
//                        }
//                        txtHelp.setText(R.string.help_pin_functions_adc);
//
//                        // Clock out help section
//                        tempDrawable =
//                                parent.getContext().getResources()
//                                        .getDrawable(R.drawable.help_pin_functions_clock);
//                        if (tempDrawable.getIntrinsicWidth() > txtHelpItem1.getWidth()) {
//                            // on some devices the drawable is wider than the screen,
//                            // so resize the drawable so it has a bit of white space around it
//                            int dif =
//                                    tempDrawable.getIntrinsicWidth() - txtHelpItem1.getWidth() + 20;
//
//                            tempDrawable.setBounds(0, 0, tempDrawable.getIntrinsicWidth() - dif,
//                                    tempDrawable.getIntrinsicHeight() - dif);
//
//                            txtHelpItem1.setCompoundDrawables(null, tempDrawable, null, null);
//                        } else {
//                            txtHelpItem1.setCompoundDrawablesWithIntrinsicBounds(null,
//                                    tempDrawable, null, null);
//                        }
//                        txtHelpItem1.setText(R.string.help_pin_functions_clock);
//                        txtHelpItem1.setVisibility(View.VISIBLE);
//
//                        // DAC help section
//                        tempDrawable =
//                                parent.getContext().getResources()
//                                        .getDrawable(R.drawable.help_pin_functions_dac);
//                        if (tempDrawable.getIntrinsicWidth() > txtHelpItem2.getWidth()) {
//                            // on some devices the drawable is wider than the screen,
//                            // so resize the drawable so it has a bit of white space around it
//                            int dif =
//                                    tempDrawable.getIntrinsicWidth() - txtHelpItem2.getWidth() + 20;
//
//                            tempDrawable.setBounds(0, 0, tempDrawable.getIntrinsicWidth() - dif,
//                                    tempDrawable.getIntrinsicHeight() - dif);
//
//                            txtHelpItem2.setCompoundDrawables(null, tempDrawable, null, null);
//                        } else {
//                            txtHelpItem2.setCompoundDrawablesWithIntrinsicBounds(null,
//                                    tempDrawable, null, null);
//                        }
//                        txtHelpItem2.setText(R.string.help_pin_functions_dac);
//                        txtHelpItem2.setVisibility(View.VISIBLE);
//
//                        // GPIO help section
//                        tempDrawable =
//                                parent.getContext().getResources()
//                                        .getDrawable(R.drawable.help_pin_functions_gpio);
//                        if (tempDrawable.getIntrinsicWidth() > txtHelpItem3.getWidth()) {
//                            // on some devices the drawable is wider than the screen,
//                            // so resize the drawable so it has a bit of white space around it
//                            int dif =
//                                    tempDrawable.getIntrinsicWidth() - txtHelpItem3.getWidth() + 20;
//
//                            tempDrawable.setBounds(0, 0, tempDrawable.getIntrinsicWidth() - dif,
//                                    tempDrawable.getIntrinsicHeight() - dif);
//
//                            txtHelpItem3.setCompoundDrawables(null, tempDrawable, null, null);
//                        } else {
//                            txtHelpItem3.setCompoundDrawablesWithIntrinsicBounds(null,
//                                    tempDrawable, null, null);
//                        }
//                        txtHelpItem3.setText(R.string.help_pin_functions_gpio);
//                        txtHelpItem3.setVisibility(View.VISIBLE);
//
//                        break;
//                    case SERIAL_TERMINAL:
//                        tempDrawable =
//                                parent.getContext().getResources()
//                                        .getDrawable(R.drawable.help_serial_terminal);
//                        if (tempDrawable.getIntrinsicWidth() > txtHelp.getWidth()) {
//                            // on some devices the drawable is wider than the screen,
//                            // so resize the drawable so it has a bit of white space around it
//                            int dif = tempDrawable.getIntrinsicWidth() - txtHelp.getWidth() + 20;
//
//                            tempDrawable.setBounds(0, 0, tempDrawable.getIntrinsicWidth() - dif,
//                                    tempDrawable.getIntrinsicHeight() - dif);
//
//                            txtHelp.setCompoundDrawables(null, tempDrawable, null, null);
//                        } else {
//                            txtHelp.setCompoundDrawablesWithIntrinsicBounds(null, tempDrawable,
//                                    null, null);
//                        }
//
//                        // update the help text
//                        txtHelp.setText(R.string.help_serial_terminal);
//
//                        tempDrawable =
//                                parent.getContext().getResources()
//                                        .getDrawable(R.drawable.help_serial_terminal_settings);
//                        if (tempDrawable.getIntrinsicWidth() > txtHelpItem1.getWidth()) {
//                            // on some devices the drawable is wider than the screen,
//                            // so resize the drawable so it has a bit of white space around it
//                            int dif =
//                                    tempDrawable.getIntrinsicWidth() - txtHelpItem1.getWidth() + 20;
//
//                            tempDrawable.setBounds(0, 0, tempDrawable.getIntrinsicWidth() - dif,
//                                    tempDrawable.getIntrinsicHeight() - dif);
//
//                            txtHelpItem1.setCompoundDrawables(null, tempDrawable, null, null);
//                        } else {
//                            txtHelpItem1.setCompoundDrawablesWithIntrinsicBounds(null,
//                                    tempDrawable, null, null);
//                        }
//
//                        // update the help text
//                        txtHelpItem1.setText(R.string.help_serial_terminal_settings);
//                        txtHelpItem1.setVisibility(View.VISIBLE);
//
//                        // hide any items that shouldn't be visible
//                        txtHelpItem2.setVisibility(View.INVISIBLE);
//                        txtHelpItem3.setVisibility(View.INVISIBLE);
//                        break;
//
//                    case KNOWN_ISSUES:
//
//                        txtHelp.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
//                        // update the help text
//                        txtHelp.setText(R.string.help_known_issues);
//
//                        // hide any items that shouldn't be visible
//                        txtHelpItem1.setVisibility(View.INVISIBLE);
//                        txtHelpItem2.setVisibility(View.INVISIBLE);
//                        txtHelpItem3.setVisibility(View.INVISIBLE);
//                        break;
//
//                    default:
//                        break;
//                }
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> arg0) {
//                // TODO Auto-generated method stub
//
//            }
//        });
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
        getActivity().getActionBar().setTitle(R.string.title_section5_help);
    }
}
