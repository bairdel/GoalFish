package com.example.goalfish;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Dictionary;
import java.util.Hashtable;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, "wordsTracker.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase DB) {
        // create tables
        DB.execSQL("create Table wordLogs(id INTEGER primary key AUTOINCREMENT NOT NULL, date TEXT NOT NULL, words INTEGER NOT NULL, cumulative INTEGER NOT NULL, goal_id TEXT NOT NULL, FOREIGN KEY(goal_id) REFERENCES goalsTable(goalName))");
        DB.execSQL("create Table goalsTable(id INTEGER primary key AUTOINCREMENT, goalName TEXT UNIQUE, goal INTEGER, period INTEGER, startDate TEXT, reoccurring BOOL)");

        // get current date as string - might need to do something about making this work all the time
        LocalDate myDateObj = LocalDate.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = myDateObj.format(myFormatObj);

        // give starting values for both tables so they're not empty
        ContentValues contentValues = new ContentValues();
        contentValues.put("date", formattedDate);
        contentValues.put("words", 0);
        contentValues.put("cumulative", 0);
        contentValues.put("goal_id", "Default Goal");
        long r = DB.insert("wordLogs", null, contentValues);

        ContentValues contentValues2 = new ContentValues();
        contentValues2.put("goalName", "Default Goal");
        contentValues2.put("goal", 500);
        contentValues2.put("period", 1);
        contentValues2.put("startDate", formattedDate);
        contentValues2.put("reoccurring", true);
        long r2 = DB.insert("goalsTable", null, contentValues2);

        // insertlogsdata won't work here - calls database recursively


    }



    public Boolean insertLogsData(String date, int words, int cumulative, String goalName){
        // create a new entry in wordLogs
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("date", date);
        contentValues.put("words", words);
        contentValues.put("cumulative", cumulative);
        contentValues.put("goal_id", goalName);
        long result = DB.insert("wordLogs", null, contentValues);
    if (result==-1){
        return false;
    }else{
        return true;
    }
    }

    public Boolean insertGoalData(String goalName, int goal, int period, String startDate, boolean reoccurring){
        // create a new goal, and create initial entry in wordLogs

        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("goalName", goalName);
        contentValues.put("goal", goal);
        contentValues.put("period", period);
        contentValues.put("startDate", startDate);
        contentValues.put("reoccurring", reoccurring);
        long result = DB.insert("goalsTable", null, contentValues);

        // get current date as string
        LocalDate myDateObj = LocalDate.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = myDateObj.format(myFormatObj);

        ContentValues contentValues2 = new ContentValues();
        contentValues2.put("date", formattedDate);
        contentValues2.put("words", 0);
        contentValues2.put("cumulative", 0);
        contentValues2.put("goal_id", goalName);
        long r = DB.insert("wordLogs", null, contentValues2);

        if (result==-1){
            return false;
        }else{
            return true;
        }
    }


    public Boolean deleteWords(String goalName){
        // delete the last record entered into wordLogs based on a goal
        SQLiteDatabase DB = this.getWritableDatabase();

        // check there are more than one entries before deleting
        Cursor cursor1 = DB.rawQuery("Select * from wordLogs where goal_id=?", new String[] {goalName});
        int logsNum = cursor1.getCount();
        cursor1.close();

        // query most recent entry with specified goal
        Cursor cursor = DB.rawQuery("Select * from wordLogs where goal_id=? ORDER BY id DESC LIMIT 1", new String[] {goalName});
        cursor.moveToFirst();
        int id = cursor.getInt(0); // gets id of record so we know what to delete
        cursor.close();
        if (logsNum != 1) { // don't delete first value
            long result = DB.delete("wordLogs", "id=?", new String[]{String.valueOf(id)});
            if (result == -1) {
                return false;
            } else {
                return true;
            }
        }else{
            return false;
        }}

    public Boolean deleteGoal(String goalName){
        // delete the last record entered into goalsTable based on a goal
        SQLiteDatabase DB = this.getWritableDatabase();

        // check there are more than one entries before deleting
        Cursor cursor1 = DB.rawQuery("Select * from goalsTable", null);
        int logsNum = cursor1.getCount();
        cursor1.close();

        // query most recent entry with specified goal
        Cursor cursor = DB.rawQuery("Select * from goalsTable where goalName=? ORDER BY id DESC LIMIT 1", new String[] {goalName});
        cursor.moveToFirst();
        int id = cursor.getInt(0); // gets id of record so we know what to delete
        cursor.close();
        if (logsNum != 1) { // don't delete first value
            long result = DB.delete("goalsTable", "id=?", new String[]{String.valueOf(id)});
            if (result == -1) {
                return false;
            } else {
                return true;
            }
        }else{
            return false;
        }}


    public Cursor getData () {
        // return a cursor of all records in wordLogs descending, includes null record at end
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from wordLogs ORDER BY id DESC", null);
        return cursor;
    }

    public Cursor getSpecificData (String goalName) {
        // return a cursor of all records in wordLogs descending, includes null record at end, specific to a goal
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from wordLogs where goal_id=? ORDER BY id DESC", new String[] {goalName});
        return cursor;
    }

    public Cursor getGoals () {
        // return a cursor of all records in goalsTable descending, includes null record at end
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from goalsTable ORDER BY id DESC", null);

        return cursor;
    }

    public int getCum(String goalName) {
        // get the most recent cumulative value for the record with the requested goalName
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("select * from wordLogs where goal_id=? ORDER BY id DESC LIMIT 1", new String[] {goalName});
        cursor.moveToFirst();

        int c = cursor.getInt(3);
        //Log.d("cumulative", String.valueOf(c));
        //int cum = Integer.parseInt(c);
        return c;
    }

    public Dictionary getGoal(String goalName) {
        // get all information about a goal given a goalName
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("select * from goalsTable where goalName=? ORDER BY id DESC LIMIT 1", new String[] {goalName});
        cursor.moveToFirst();

        Dictionary dict = new Hashtable();
        dict.put("Goal Name", cursor.getString(1));
        dict.put("Goal", cursor.getInt(2));
        dict.put("Period", cursor.getInt(3));
        dict.put("Start Date", cursor.getString(4));
        dict.put("Reoccurring", cursor.getInt(5));

        return dict;
    }


    // unused methods ////////////


    @Override
    public void onUpgrade(SQLiteDatabase DB, int i, int i1) {

    }


    public Boolean updateData(int id, String date, int words, int cumulative){
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("words", words);
        contentValues.put("cumulative", cumulative);
        Cursor cursor = DB.rawQuery("Select * from wordLogs where id = ?", new String[] {String.valueOf(id)});
        if (cursor.getCount()>0) {
            long result = DB.update("wordLogs", contentValues, "id=?", new String[] {String.valueOf(id)});
            if (result == -1) {
                return false;
            } else {
                return true;
            }
        }else{
            return false;
        }}

    public Boolean deleteData(int id, int words, int cumulative){
        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("words", words);
        contentValues.put("cumulative", cumulative);
        Cursor cursor = DB.rawQuery("Select * from wordLogs where id = ?", new String[] {String.valueOf(id)});
        if (cursor.getCount()>0) {
            long result = DB.update("wordLogs", contentValues, "id=?", new String[]{String.valueOf(id)});
            if (result == -1) {
                return false;
            } else {
                return true;
            }
        }else{
            return false;
        }}
}
