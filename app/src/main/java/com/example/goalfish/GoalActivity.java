package com.example.goalfish;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.example.goalfish.databinding.ActivityGoalBinding;

import java.util.Calendar;
import java.util.Date;

public class GoalActivity extends AppCompatActivity {

    DBHelper DB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal);

        DB = new DBHelper(this);

        EditText startDate;
        DatePickerDialog.OnDateSetListener setListener;
        startDate = (findViewById(R.id.startDate));
        Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        GoalActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int day) {
                        month = month+1;
                        String date;
                        if (month < 10){
                            date = year + "-0" + month + "-" + day;
                        } else {
                            date = year + "-" + month + "-" + day;
                        }
                        startDate.setText(date);
                    }
                },year,month,day);
                datePickerDialog.show();

            }
        });
    }

    public void submitGoalChange(View v){

        String goalName, startDate;
        int wordGoal, daysGoal;
        Boolean reoccurring;

        // get values submitted in form
        goalName = ((TextView)findViewById(R.id.goalName)).getText().toString();
        wordGoal = Integer.parseInt(((TextView)findViewById(R.id.wordGoal)).getText().toString());
        daysGoal = Integer.parseInt(((TextView)findViewById(R.id.daysGoal)).getText().toString());
        startDate = ((TextView)findViewById(R.id.startDate)).getText().toString();
        reoccurring = ((Switch)findViewById(R.id.reoccuring)).isChecked();


        // insert data into database - goalsTable
        Boolean checkInsertData = DB.insertGoalData(goalName, wordGoal, daysGoal, startDate, reoccurring);

        // check if insert worked - don't know if this works
        if (checkInsertData==true) {
            Toast.makeText(GoalActivity.this, "New Entry Inserted", Toast.LENGTH_LONG).show();
            Log.d("entryinserted", "success");
        }else{
            Toast.makeText(GoalActivity.this, "Entry Not Inserted", Toast.LENGTH_LONG).show();
            Log.d("entryinserted", "fail");
        }

        // switch to main activity
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

}