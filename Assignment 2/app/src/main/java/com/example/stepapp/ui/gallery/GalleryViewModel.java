package com.example.stepapp.ui.gallery;


import android.content.Context;
import android.content.ContentValues;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GalleryViewModel extends SQLiteOpenHelper  {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "stepapp";

    public static final String TABLE_NAME = "num_steps";
    public static final String KEY_ID = "id";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_DAY = "day";
    public static final String KEY_HOUR = "hour";


    // Default SQL for creating a table in a database
    public static final String CREATE_TABLE_SQL = "CREATE TABLE " +
            TABLE_NAME + " (" + KEY_ID + " INTEGER PRIMARY KEY, " +
            KEY_DAY + " TEXT, " + KEY_HOUR + " TEXT, " + KEY_TIMESTAMP + " TEXT);";

    // Add the constructor
    public GalleryViewModel(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Load all records in the database
    public static void loadRecords(Context context){
        List<String> dates = new LinkedList<String>();
        GalleryViewModel databaseHelper = new GalleryViewModel(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        String [] columns = new String [] {GalleryViewModel.KEY_TIMESTAMP};
        Cursor cursor = database.query(GalleryViewModel.TABLE_NAME, columns, null, null, GalleryViewModel.KEY_TIMESTAMP,
                null, null );

        // iterate over returned elements
        cursor.moveToFirst();
        for (int index=0; index < cursor.getCount(); index++){
            dates.add(cursor.getString(0));
            cursor.moveToNext();
        }
        database.close();

        Log.d("STORED TIMESTAMPS: ", String.valueOf(dates));
    }


    //Delete all records from the database
    public static void deleteRecords(Context context){
        GalleryViewModel databaseHelper = new GalleryViewModel(context);
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        int numberDeletedRecords =0;

        numberDeletedRecords = database.delete(GalleryViewModel.TABLE_NAME, null, null);
        database.close();

        // display the number of deleted records with a Toast message
        Toast.makeText(context,"Deleted " + String.valueOf(numberDeletedRecords) + " steps",Toast.LENGTH_LONG).show();
    }

    // load records from a single day
    public static Integer loadSingleRecord(Context context, String fDate) {
        List<String> steps = new LinkedList<String>();
        // Get the readable database
        GalleryViewModel databaseHelper = new GalleryViewModel(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        String where = GalleryViewModel.KEY_DAY + " = ?";
        String [] whereArgs = { fDate };

        Cursor cursor = database.query(GalleryViewModel.TABLE_NAME, null, where, whereArgs, null,
                null, null );

        // iterate over returned elements
        cursor.moveToFirst();
        for (int index=0; index < cursor.getCount(); index++){
            steps.add(cursor.getString(0));
            cursor.moveToNext();
        }
        database.close();

        Integer numSteps = steps.size();
        Log.d("STORED STEPS TODAY: ", String.valueOf(numSteps));
        return numSteps;
    }

    /**
     * Utility function to get the number of steps by hour for current date
     *
     * @param context: application context
     * @param date: today's date
     * @return map: map with key-value pairs hour->number of steps
     */
    //
    public static Map<Integer, Integer> loadStepsByHour(Context context, String date){
        // 1. Define a map to store the hour and number of steps as key-value pairs
        Map<Integer, Integer>  map = new HashMap<> ();

        // 2. Get the readable database
        GalleryViewModel databaseHelper = new GalleryViewModel(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        // 3. Define the query to get the data
        Cursor cursor = database.rawQuery("SELECT hour, COUNT(*)  FROM num_steps " +
                "WHERE day = ? GROUP BY hour ORDER BY  hour ASC ", new String [] {date});

        // 4. Iterate over returned elements on the cursor
        cursor.moveToFirst();
        for (int index=0; index < cursor.getCount(); index++){
            Integer tmpKey = Integer.parseInt(cursor.getString(0));
            Integer tmpValue = Integer.parseInt(cursor.getString(1));

            //2. Put the data from the database into the map
            map.put(tmpKey, tmpValue);


            cursor.moveToNext();
        }

        // 5. Close the cursor and database
        cursor.close();
        database.close();

        // 6. Return the map with hours and number of steps
        return map;
    }


    public static Map<Integer, Integer> loadStepsByDay(Context context, String date){
        // 1. Define a map to store the day and number of steps as key-value pairs
        Map<Integer, Integer>  map = new HashMap<> ();

        // 2. Get the readable database
        GalleryViewModel databaseHelper = new GalleryViewModel(context);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        // 3. Define the query to get the data
        Cursor cursor = database.rawQuery("SELECT day, COUNT(*)  FROM num_steps " +
                "WHERE day = ? GROUP BY day ORDER BY  day ASC ", new String [] {date});

        // 4. Iterate over returned elements on the cursor
        cursor.moveToFirst();
        for (int index=0; index < cursor.getCount(); index++){
            Integer tmpKey = Integer.parseInt(cursor.getString(0));
            Integer tmpValue = Integer.parseInt(cursor.getString(1));

            //2. Put the data from the database into the map
            map.put(tmpKey, tmpValue);


            cursor.moveToNext();
        }

        // 5. Close the cursor and database
        cursor.close();
        database.close();

        // 6. Return the map with hours and number of steps
        return map;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}