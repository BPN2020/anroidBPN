package com.microchip.android.mcp2221terminal;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class ResulShowActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expand_item);

        String name = getIntent().getExtras().getString("name");
        ListView listView =(ListView) findViewById(R.id.expand_item_list);

        ArrayList<String> stringArrayList = new ArrayList<>();
        DBHelper dbHelper = new DBHelper(getApplicationContext());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor res = db.rawQuery( "select * from "+ DB.TestEntry.TABLE_NAME+ " WHERE "
                + DB.TestEntry.COLUMN_NAME_TITLE +" = \"" + name +"\"", null );
        res.moveToFirst();
        while(!res.isAfterLast()) {
            stringArrayList.add("Temp : "+res.getString(3) + "   RH :"+ res.getString(4)+"\n" +" Date:"+ res.getString(2));
            res.moveToNext();
        }
        ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(),
                R.layout.retrieve_list_item, R.id.label, stringArrayList);
        listView.setAdapter(adapter);
    }
}