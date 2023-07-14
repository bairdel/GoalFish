package com.example.goalfish;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {

    public DBHelper(Context context) {
        super(context, "wordsTracker.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase DB) {
        // create tables
        DB.execSQL("create Table wordLogs(id INTEGER primary key AUTOINCREMENT NOT NULL, date TEXT NOT NULL, words INTEGER NOT NULL, cumulative INTEGER NOT NULL, goal_id INTEGER NOT NULL, totalCount INTEGER, FOREIGN KEY(goal_id) REFERENCES goalsTable(id))");
        DB.execSQL("create Table goalsTable(id INTEGER primary key AUTOINCREMENT, goalName TEXT UNIQUE, goal INTEGER, period INTEGER, startDate TEXT, reoccurring BOOL, limitReached BOOL)");
        // changed so startDate is effectively finishDate

        // get current date as string - might need to do something about making this work all the time
        LocalDate myDateObj = LocalDate.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = myDateObj.format(myFormatObj);

        // give starting values for both tables so they're not empty
        ContentValues contentValues = new ContentValues();
        contentValues.put("date", formattedDate);
        contentValues.put("words", 0);
        contentValues.put("cumulative", 0);
        contentValues.put("goal_id", 1);
        contentValues.put("totalCount", 0);
        long r = DB.insert("wordLogs", null, contentValues);

        ContentValues contentValues2 = new ContentValues();
        contentValues2.put("goalName", "Default Goal");
        contentValues2.put("goal", 500);
        contentValues2.put("period", 1);
        contentValues2.put("startDate", formattedDate);
        contentValues2.put("reoccurring", true);
        contentValues2.put("limitReached", true);
        long r2 = DB.insert("goalsTable", null, contentValues2);

        // insertlogsdata won't work here - calls database recursively


    }

    public int convertGoalToId(String goalName) {
        // return the id of a goal given the goalName
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("select * from goalsTable where goalName=? ORDER BY id DESC LIMIT 1", new String[] {goalName});
        cursor.moveToFirst();

        int c = cursor.getInt(0);
        cursor.close();
        return c;
    }



    public Boolean insertLogsData(String date, int words, int cumulative, String goalName){
        SQLiteDatabase DB = this.getWritableDatabase();

        // get the current running total
        Cursor cursor = DB.rawQuery("select * from wordLogs where goal_id=? ORDER BY id DESC LIMIT 1", new String[] {String.valueOf(convertGoalToId(goalName))});
        cursor.moveToFirst();
        int runningTotal = cursor.getInt(5);
        cursor.close();

        // create a new entry in wordLogs
        ContentValues contentValues = new ContentValues();
        contentValues.put("date", date);
        contentValues.put("words", words);
        contentValues.put("cumulative", cumulative);
        contentValues.put("goal_id", convertGoalToId(goalName));
        contentValues.put("totalCount", runningTotal + words);
        long result = DB.insert("wordLogs", null, contentValues);
    if (result==-1){
        return false;
    }else{
        return true;
    }
    }

    public Boolean insertGoalData(String goalName, int goal, int period, String startDate, boolean reoccurring){
        // create a new goal, and create initial entry in wordLogs

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
        LocalDate startDateDT = LocalDate.parse(startDate, formatter);
        LocalDate finishDateTemp = startDateDT.plusDays(period);
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String finishDate = finishDateTemp.format(myFormatObj);

        SQLiteDatabase DB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("goalName", goalName);
        contentValues.put("goal", goal);
        contentValues.put("period", period);
        contentValues.put("startDate", finishDate);
        contentValues.put("reoccurring", reoccurring);
        contentValues.put("limitReached", false);
        long result = DB.insert("goalsTable", null, contentValues);

        // get current date as string
        LocalDate myDateObj = LocalDate.now();
        String formattedDate = myDateObj.format(myFormatObj);

        ContentValues contentValues2 = new ContentValues();
        contentValues2.put("date", formattedDate);
        contentValues2.put("words", 0);
        contentValues2.put("cumulative", 0);
        contentValues2.put("goal_id", convertGoalToId(goalName));
        contentValues2.put("totalCount", 0);
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
        Cursor cursor1 = DB.rawQuery("Select * from wordLogs where goal_id=?", new String[] {String.valueOf(convertGoalToId(goalName))});
        int logsNum = cursor1.getCount();
        cursor1.close();

        // query most recent entry with specified goal
        Cursor cursor = DB.rawQuery("Select * from wordLogs where goal_id=? ORDER BY id DESC LIMIT 1", new String[] {String.valueOf(convertGoalToId(goalName))});
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
        Cursor cursor = DB.rawQuery("Select * from wordLogs where goal_id=? ORDER BY id DESC", new String[] {String.valueOf(convertGoalToId(goalName))});
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
        Cursor cursor = DB.rawQuery("select * from wordLogs where goal_id=? ORDER BY id DESC LIMIT 1", new String[] {String.valueOf(convertGoalToId(goalName))});
        cursor.moveToFirst();

        int c = cursor.getInt(3);
        //Log.d("cumulative", String.valueOf(c));
        //int cum = Integer.parseInt(c);
        cursor.close();
        return c;
    }

    public int getTotal(String goalName) {
        // get the most recent cumulative value for the record with the requested goalName
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("select * from wordLogs where goal_id=? ORDER BY id DESC LIMIT 1", new String[] {String.valueOf(convertGoalToId(goalName))});
        cursor.moveToFirst();

        int c = cursor.getInt(5);
        //Log.d("cumulative", String.valueOf(c));
        //int cum = Integer.parseInt(c);
        cursor.close();
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
        dict.put("limitReached", cursor.getInt(6));

        cursor.close();
        return dict;
    }

    public int getLimitReached(String goalName) {
        // return the value of limitReached for a goal to see if a blank entry has already been added
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("select * from goalsTable where goalName=? ORDER BY id DESC LIMIT 1", new String[] {goalName});
        cursor.moveToFirst();

        int c = cursor.getInt(6);
        //Log.d("cumulative", String.valueOf(c));
        //int cum = Integer.parseInt(c);
        cursor.close();
        return c;
    }

    public boolean setLimitReached(String goalName, boolean limitReached) {
        // change the limitReached boolean
        SQLiteDatabase DB = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("limitReached", limitReached);

        long result = DB.update("goalsTable", contentValues, "goalName=?", new String[] {goalName});
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public boolean changeGoalName(String goalName, Dictionary newValues) {
        //
        SQLiteDatabase DB = this.getWritableDatabase();

        int goalID = (int) newValues.get("Goal ID");

        ContentValues contentValues = new ContentValues();
        contentValues.put("goalName", (String) newValues.get("Goal Name"));
        contentValues.put("goal", (int) newValues.get("Goal"));
        contentValues.put("period", (int) newValues.get("Period"));


        long result = DB.update("goalsTable", contentValues, "id=?", new String[] {String.valueOf(goalID)});
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public boolean checkGoalUsed (String goalName) {
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from goalsTable ORDER BY id ASC", null);

        while (cursor.moveToNext()) {
            if (cursor.getString(1).equals(goalName)) {
                return true;
            }
        } return false;
    }

    public void changeFinishDate(String goalName, String newDate) {
        // change the limitReached boolean
        SQLiteDatabase DB = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("startDate", newDate);

        long result = DB.update("goalsTable", contentValues, "goalName=?", new String[] {goalName});
    }

//    public String[] calculateDates(String oldfinishDate, int period, int reoccurring){
//
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
//        LocalDate endDate = LocalDate.parse(oldfinishDate, formatter);
//
//        LocalDate currentDate = LocalDate.now();
//
//        String daysBetween = ChronoUnit.DAYS.between(endDate, currentDate); // date in future gives neg
//        //String daysBetween = String.valueOf(ChronoUnit.DAYS.between(currentDate, endDate)); // date in future gives pos
//
//        //Log.d("daysBetweeen", String.valueOf(daysBetween));
//        Log.d("daysBetweeen", String.valueOf(daysBetween));
//
//        String daysLeft;
//        String finishDate;
//        LocalDate base;
//        if (reoccurring == 1) {
//            int count;
//            String mid = String.valueOf(ChronoUnit.DAYS.between(endDate, currentDate));
//            if (Integer.parseInt(mid) <= 0) {
//                // update finishDate in database
//                // reset log
//            }
//            base = endDate;
//            while (Integer.parseInt(mid) <= 0) {
//                base = base.plusDays(period);
//                mid = String.valueOf(ChronoUnit.DAYS.between(currentDate, base));
//            }
//            daysLeft = mid;
//            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//            finishDate = base.format(myFormatObj);
//        } else {
//            daysLeft = daysBetween;
//            base = endDate;
//            base = base.plusDays(period);
//            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//            finishDate = base.format(myFormatObj);
//        }
//
//
//        String[] answer = new String[2];
//        answer[0] = daysLeft;
//        answer[1] = finishDate;
//        return answer;
//    }

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
