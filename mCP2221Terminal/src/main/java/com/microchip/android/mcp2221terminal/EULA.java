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

/**
 * This file provides simple End User License Agreement It shows a simple dialog with the license
 * text, and two buttons. If user clicks on 'cancel' button, app closes and user will not be granted
 * access to app. If user clicks on 'accept' button, app access is allowed and this choice is saved
 * in preferences so next time this will not show, until next upgrade.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

/**
 * EULA class. Prompts the user to either accept or cancel the license agreement.
 */
public class EULA {
    private final String EULA_PREFIX;
    private final Activity mContext;

    public EULA(final Activity context) {
        mContext = context;
        EULA_PREFIX = mContext.getString(R.string.app_name);
    }

    private PackageInfo getPackageInfo() {
        PackageInfo info = null;
        try {
            info =
                    mContext.getPackageManager().getPackageInfo(mContext.getPackageName(),
                            PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return info;
    }

    public void show() {
        PackageInfo versionInfo = getPackageInfo();

        // The eulaKey changes every time you increment the version number in
        // the AndroidManifest.xml
        final String eulaKey = EULA_PREFIX + " " + versionInfo.versionCode;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        boolean bAlreadyAccepted = prefs.getBoolean(eulaKey, false);
        if (bAlreadyAccepted == false) {

            String title = mContext.getString(R.string.app_name) + " " + versionInfo.versionName;

            AlertDialog.Builder builder =
                    new AlertDialog.Builder(mContext)
                            .setTitle(title)
                            .setMessage(mContext.getText(R.string.eula_text))
                            .setOnCancelListener(new Dialog.OnCancelListener() {

                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    // if the window is dismissed (via a back button press) without
                                    // accepting, close the app
                                    mContext.finish();
                                }
                            })
                            .setPositiveButton(R.string.accept, new Dialog.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    // Mark this version as read.
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.putBoolean(eulaKey, true);
                                    editor.commit();

                                    // Close dialog
                                    dialogInterface.dismiss();

                                    // Enable orientation changes based on
                                    // device's sensor
                                    mContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                                }
                            })
                            .setNegativeButton(android.R.string.cancel,
                                    new Dialog.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Close the activity as they have declined
                                            // the EULA
                                            mContext.finish();
                                            mContext.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                                        }
                                    });
            builder.create().show();

        }
    }
}
