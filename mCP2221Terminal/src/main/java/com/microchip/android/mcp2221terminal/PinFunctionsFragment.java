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
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
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
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Class containing the MCP2221 pin functions settings.
 */
@SuppressLint("ShowToast")
public class PinFunctionsFragment extends Fragment {

    ArrayAdapter listAdapter;
    ListView expListView;
    List<String> listDataHeader =  new ArrayList<String>();
    HashMap<String, List<String>> listDataChild = new HashMap<String, List<String>>();
    ;;

    private Mcp2221Comm mMcp2221Comm;
    private Mcp2221Config mMcp2221Config = new Mcp2221Config();

    /**
     * Custom toast - displayed in the center of the screen.
     */
    private static Toast sToast;

    /**
     * The fragment argument representing the section number for this fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    ArrayList<View> childrenViw = new ArrayList<>(listDataHeader.size());

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
        final View rootView = inflater.inflate(R.layout.fragment_retrieve, container, false);
        // update the connection status each time the fragment becomes visible
        mMcp2221Comm = ((MainActivity) getActivity()).mcp2221Comm;

        // set up the custom toast; This will appear in the center of the
        // screen and not the default position
        sToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
        sToast.setGravity(Gravity.CENTER, 0, 0);

        Button clear = (Button) rootView.findViewById(R.id.clear_button);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DBHelper dbHelper = new DBHelper(getActivity().getApplicationContext());
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.execSQL("delete from "+ DB.TestEntry.TABLE_NAME);
                db.execSQL("delete from "+ DB.TestNameEntry.TABLE_NAME);
            }
        });
        //get the listview
        expListView = (ListView) rootView.findViewById(R.id.list_test_names);

        // preparing list data
        prepareListData();

        listAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(),
                R.layout.retrieve_list_item, R.id.label, listDataHeader);
        expListView.setAdapter(listAdapter);

        expListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(),ResulShowActivity.class);
                intent.putExtra("name",listDataHeader.get(i) );
                startActivity(intent);
            }
        });
//        listAdapter =
//                new ExpandableListAdapter(rootView.getContext(), listDataHeader, listDataChild);


        return rootView;
    }

    /*
     * Preparing the list data
     */
    private void prepareListData() {
        listDataHeader =  new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        DBHelper dbHelper = new DBHelper(getActivity().getApplicationContext());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.rawQuery("select " + DB.TestNameEntry.COLUMN_NAME_TITLE + " from "
                + DB.TestNameEntry.TABLE_NAME, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            listDataHeader.add(c.getString(c.getColumnIndex(DB.TestNameEntry.COLUMN_NAME_TITLE)));
            c.moveToNext();
        }
        List<String> childList = new ArrayList<String>();
        // we only need one child for each expandable list entry
        childList.add("");

        for (int i = 0; i < listDataHeader.size(); i++) {
            listDataChild.put(listDataHeader.get(i), childList);
        }
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
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setTitle(R.string.title_section3_pin_functions);
        }
    }

    public class ExpandableListAdapter extends BaseExpandableListAdapter {

        private Context _context;
        private List<String> _listDataHeader; // header titles
        // child data in format of header title, child title
        private HashMap<String, List<String>> _listDataChild;


        public ExpandableListAdapter(Context context, List<String> listDataHeader,
                                     HashMap<String, List<String>> listChildData) {
            this._context = context;
            this._listDataHeader = listDataHeader;
            this._listDataChild = listChildData;
        }

        @Override
        public void onGroupExpanded(int groupPosition) {
            updateList(groupPosition);
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
            return _listDataHeader.size();
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition, boolean isLastChild,
                                 View convertView, ViewGroup parent) {

            LayoutInflater infalInflater =
                    (LayoutInflater) this._context
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            while (groupPosition >= childrenViw.size())
                childrenViw.add(null);
            if (childrenViw.get(groupPosition) == null) {
                childrenViw.set(groupPosition, (View) infalInflater.inflate(R.layout.expand_item,
                        parent, false));
                ArrayList<String> stringArrayList = new ArrayList<>();
                DBHelper dbHelper = new DBHelper(getActivity().getApplicationContext());
                SQLiteDatabase db = dbHelper.getReadableDatabase();
                Cursor res = db.rawQuery( "select * from "+ DB.TestEntry.TABLE_NAME+ " WHERE "
                        + DB.TestEntry.COLUMN_NAME_TITLE +" = \"" + _listDataHeader.get(groupPosition)+"\"", null );
                res.moveToFirst();
                while(!res.isAfterLast()) {
                    stringArrayList.add("T1 : "+res.getString(3) + "   T2 :"+ res.getString(4)+"\n" +" Date:"+ res.getString(2));
                    res.moveToNext();
                }
                ArrayAdapter adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(),
                        R.layout.retrieve_list_item, stringArrayList);
                ListView listView = (ListView)childrenViw.get(groupPosition).findViewById(R.id.expand_item_list);
                listView.setAdapter(adapter);
                updateList(groupPosition);
            }
            convertView = childrenViw.get(groupPosition);
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

    // todo
    private void updateList(int groupPosition) {
        // update the connection status
        mMcp2221Comm = ((MainActivity) getActivity()).mcp2221Comm;
    }

    public void updatePinFunctions() {
        // update the connection status
        for (int i = 0; i < listDataHeader.size(); i++) {
            if (childrenViw.get(i) != null && childrenViw.get(i).getVisibility() == View.VISIBLE) {
                updateList(i);
            }
        }

    }
    public void updateConnectionStatus() {
        // update the connection status each time the fragment becomes visible
        mMcp2221Comm = ((MainActivity) getActivity()).mcp2221Comm;
    }
}
