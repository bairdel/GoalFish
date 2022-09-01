package com.example.goalfish;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    DBHelper DB;
    private Spinner spinnerGoal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialise database for changing word count and word goal on homepage
        DB = new DBHelper(this);

        // create the dropdown with the created goals
        Cursor res = DB.getGoals();
        int rows = res.getCount() + 1;
        String[] goalNames1 = new String[rows];
        int i = 0;
        while(res.moveToNext()){
            goalNames1[i] = res.getString(1);
            i += 1;
        }

        String[] goalNames = Arrays.copyOf(goalNames1, goalNames1.length - 1);
        spinnerGoal = findViewById(R.id.goalSelector);
        spinnerGoal.setOnItemSelectedListener(this);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, goalNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoal.setAdapter(adapter);



        updateHome("Default Goal");
//        // set the numbers to something - this might be unnecessary
//        TextView t = (TextView) findViewById(R.id.currWordCount);
//        String c = (String.valueOf(DB.getCum("Default Goal")));
//        t.setText(c);
//
//        TextView t2 = (TextView) findViewById(R.id.goalWordCount);
//        String c2 = (String.valueOf(DB.getGoal("Default Goal").get("Goal")));
//        t2.setText(c2);

    }

    public void editGoal(View v){
        // move to GoalActivity when button pressed
        Intent i = new Intent(this, GoalActivity.class);
        startActivity(i);
    }

    public void addWords(View v){
        // move to AddWordsActivity when button pressed
        Intent i = new Intent(this, AddWordsActivity.class);
        startActivity(i);
    }

    public void editLogs(View v){
        // move to editLogsActivity when button pressed
        Intent i = new Intent(this, editLogsActivity.class);
        startActivity(i);
    }



    public void updateHome (String valueFromSpinner) {

        TextView t = (TextView) findViewById(R.id.currWordCount);
        String c = (String.valueOf(DB.getCum(valueFromSpinner)));
        t.setText(c);

        TextView t2 = (TextView) findViewById(R.id.goalWordCount);
        String c2 = (String.valueOf(DB.getGoal(valueFromSpinner).get("Goal")));
        t2.setText(c2);

        ProgressBar simpleProgressBar=(ProgressBar) findViewById(R.id.mainProgressBar); // initiate the progress bar
        simpleProgressBar.setMax(Integer.parseInt(c2));
        simpleProgressBar.setProgress(Integer.parseInt(c));

        TextView daysLeft = (TextView) findViewById((R.id.daysLeft));
        TextView finishDate = (TextView) findViewById((R.id.finishDate));
        TextView runningCount = (TextView) findViewById((R.id.runningTotal));

        String startDate = (String) (DB.getGoal(valueFromSpinner)).get("Start Date");
        int period = (int) (DB.getGoal(valueFromSpinner)).get("Period");
        int reoccurring = (int) (DB.getGoal(valueFromSpinner)).get("Reoccurring");

        String result[] = DB.calculateDates(startDate, period, reoccurring);

        daysLeft.setText(result[0]);
        finishDate.setText(result[1]);
        runningCount.setText(String.valueOf(DB.getTotal(valueFromSpinner)));

        if ((Integer.parseInt(result[0]) == period) && (DB.getLimitReached(valueFromSpinner) == 0)) {
            // daysLeft = 0 and hasn't been updated today

            // get current date as string
            LocalDate myDateObj = LocalDate.now();
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedDate = myDateObj.format(myFormatObj);

            // insert data into wordLogs table
            Boolean checkInsertData = DB.insertLogsData(formattedDate, 0, 0, valueFromSpinner);

            // check if inserted - might not work
            if (checkInsertData==true) {
                Toast.makeText(MainActivity.this, "Word Count Updated", Toast.LENGTH_SHORT).show();
                Log.d("entryinserted", "success");
            }else{
                Toast.makeText(MainActivity.this, "Goal Not Updated", Toast.LENGTH_SHORT).show();
                Log.d("entryinserted", "fail");
            }

            DB.setLimitReached(valueFromSpinner, true);


        } else if (Integer.parseInt(result[0]) != period) {
            DB.setLimitReached(valueFromSpinner, false);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // refresh page
        Intent i = getIntent();
        finish();
        startActivity(i);    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        // update values when something is selected on the dropdown
        if (adapterView.getId() == R.id.goalSelector) {
            String valueFromSpinner = adapterView.getItemAtPosition(position).toString();

            updateHome(valueFromSpinner);



        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // default to most recent goal for values
        Cursor cursor = DB.getGoals();
        cursor.moveToFirst();
        String name = cursor.getString(1);

        //updateHome(name);

        // refresh page
//        Intent i = getIntent();
//        finish();
//        startActivity(i);

//        TextView t = (TextView) findViewById(R.id.currWordCount);
//        String c = (String.valueOf(DB.getCum(name)));
//        t.setText(c);
//
//        TextView t2 = (TextView) findViewById(R.id.goalWordCount);
//        String c2 = (String.valueOf(DB.getGoal(name).get("Goal")));
//        t2.setText(c2);
    }
}