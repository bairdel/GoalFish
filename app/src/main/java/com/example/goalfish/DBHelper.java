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
import java.util.Arrays;
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
        DB.execSQL("create Table goalsTable(id INTEGER primary key AUTOINCREMENT, goalName TEXT UNIQUE, goal INTEGER, period INTEGER, endDate TEXT, reoccurring BOOL, isDefault BOOL)");
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
        contentValues2.put("endDate", formattedDate);
        contentValues2.put("reoccurring", true);
        contentValues2.put("isDefault", true);
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


    // updating db stuff - daily
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

    public Boolean insertGoalData(String goalName, int goal, int period, String startDate, boolean reoccurring, boolean isDefault){
        // create a new goal, and create initial entry in wordLogs

        SQLiteDatabase DB = this.getWritableDatabase();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH);
        LocalDate startDateDT = LocalDate.parse(startDate, formatter);
        LocalDate finishDateTemp = startDateDT.plusDays(period);
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String finishDate = finishDateTemp.format(myFormatObj);

        if (isDefault == true){
            resetDefaults();

        }

        ContentValues contentValues = new ContentValues();
        contentValues.put("goalName", goalName);
        contentValues.put("goal", goal);
        contentValues.put("period", period);
        contentValues.put("endDate", finishDate);
        contentValues.put("reoccurring", reoccurring);
        contentValues.put("isDefault", isDefault);
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

    public void resetDefaults(){
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from goalsTable ORDER BY id DESC", null);
        while(cursor.moveToNext()){ // set isDefault to 0

            int goalID = (int) cursor.getInt(0);

            ContentValues contentValues = new ContentValues();
            contentValues.put("isDefault", 0);

            long result = DB.update("goalsTable", contentValues, "id=?", new String[] {String.valueOf(goalID)});
        }

    }

    public void changeFinishDate(String goalName, String newDate) {
        // change the limitReached boolean
        SQLiteDatabase DB = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("endDate", newDate);

        long result = DB.update("goalsTable", contentValues, "goalName=?", new String[] {goalName});
    }

    // how the main program gets data from the db
    public Dictionary getGoal(String goalName) {
        // get all information about a goal given a goalName
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("select * from goalsTable where goalName=? ORDER BY id DESC LIMIT 1", new String[] {goalName});
        cursor.moveToFirst();

        Dictionary dict = new Hashtable();
        dict.put("Goal Name", cursor.getString(1));
        dict.put("Goal", cursor.getInt(2));
        dict.put("Period", cursor.getInt(3));
        dict.put("End Date", cursor.getString(4));
        dict.put("Reoccurring", cursor.getInt(5));
        dict.put("isDefault", cursor.getInt(6));

        cursor.close();
        return dict;
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

    public Dictionary getGoals () {
        // return a cursor of all records in goalsTable descending, includes null record at end
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from goalsTable ORDER BY id DESC", null);

        int defaultSpinnerIndex = 0;
        int rows = cursor.getCount() + 1;
        String[] goalNames1 = new String[rows];
        int i = 0;
        while(cursor.moveToNext()){
            goalNames1[i] = cursor.getString(1);
            Log.d("ifDefault", cursor.getString(6));
            if (cursor.getInt(6) == 1){
                defaultSpinnerIndex = i;
                Log.d("defaultSpinnerIndex", String.valueOf(defaultSpinnerIndex));
            }
            i += 1;
        }


        String[] goalNames = Arrays.copyOf(goalNames1, goalNames1.length - 1);

        Dictionary dict = new Hashtable();;
        dict.put("goalNames", goalNames);
        dict.put("defaultSpinnerIndex", defaultSpinnerIndex);

        return dict;
    }

    public String getDefaultGoal(){
        SQLiteDatabase DB = this.getWritableDatabase();
        String name = null;


        Cursor cursor = DB.rawQuery("select * from goalsTable where isDefault=? ORDER BY id DESC LIMIT 1", new String[]{"0"});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            name = cursor.getString(1);
        } else if (cursor.getCount() == 0){
            Cursor res = DB.rawQuery("Select * from goalsTable ORDER BY id DESC", null);
            res.moveToFirst();
            name = res.getString(1);
            res.close();
        }
        cursor.close();



        return name;
    }

    // logs interactions and user control over database
    public Cursor getSpecificData (String goalName) {
        // return a cursor of all records in wordLogs descending, includes null record at end, specific to a goal
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from wordLogs where goal_id=? ORDER BY id DESC", new String[] {String.valueOf(convertGoalToId(goalName))});
        return cursor;
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

    public boolean changeGoalName(String goalName, Dictionary newValues) {
        //
        SQLiteDatabase DB = this.getWritableDatabase();

        int goalID = (int) newValues.get("Goal ID");

        ContentValues contentValues = new ContentValues();
        contentValues.put("goalName", (String) newValues.get("Goal Name"));
        contentValues.put("goal", (int) newValues.get("Goal"));
        contentValues.put("period", (int) newValues.get("Period"));
        contentValues.put("reoccurring", (Boolean) newValues.get("Reoccurring"));
        contentValues.put("isDefault", (Boolean) newValues.get("isDefault"));

        if ((Boolean) (newValues.get("isDefault")) == true){
            resetDefaults();

        }

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



    ///////////////////////////////////////

    public Cursor getData () {
        // return a cursor of all records in wordLogs descending, includes null record at end
        SQLiteDatabase DB = this.getWritableDatabase();
        Cursor cursor = DB.rawQuery("Select * from wordLogs ORDER BY id DESC", null);
        return cursor;
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

//    public boolean setLimitReached(String goalName, boolean limitReached) {
//        // change the limitReached boolean
//        SQLiteDatabase DB = this.getWritableDatabase();
//
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("limitReached", limitReached);
//
//        long result = DB.update("goalsTable", contentValues, "goalName=?", new String[] {goalName});
//        if (result == -1) {
//            return false;
//        } else {
//            return true;
//        }
//    }




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


//    public Boolean updateData(int id, String date, int words, int cumulative){
//        SQLiteDatabase DB = this.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("words", words);
//        contentValues.put("cumulative", cumulative);
//        Cursor cursor = DB.rawQuery("Select * from wordLogs where id = ?", new String[] {String.valueOf(id)});
//        if (cursor.getCount()>0) {
//            long result = DB.update("wordLogs", contentValues, "id=?", new String[] {String.valueOf(id)});
//            if (result == -1) {
//                return false;
//            } else {
//                return true;
//            }
//        }else{
//            return false;
//        }}
//
//    public Boolean deleteData(int id, int words, int cumulative){
//        SQLiteDatabase DB = this.getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("words", words);
//        contentValues.put("cumulative", cumulative);
//        Cursor cursor = DB.rawQuery("Select * from wordLogs where id = ?", new String[] {String.valueOf(id)});
//        if (cursor.getCount()>0) {
//            long result = DB.update("wordLogs", contentValues, "id=?", new String[]{String.valueOf(id)});
//            if (result == -1) {
//                return false;
//            } else {
//                return true;
//            }
//        }else{
//            return false;
//        }}
}
