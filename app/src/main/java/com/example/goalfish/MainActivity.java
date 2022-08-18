package com.example.goalfish;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    DBHelper DB;
    private Spinner spinnerGoal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialise database for changing word count and word goal on homepage
        DB = new DBHelper(this);

        Cursor res = DB.getGoals();
        int rows = res.getCount() + 1;
        String[] goalNames1 = new String[rows];
        int i = 0;
        while(res.moveToNext()){
            goalNames1[i] = res.getString(1);
            i += 1;
        }
        String[] goalNames = Arrays.copyOf(goalNames1, goalNames1.length - 1);
        Log.d("dataArray", Arrays.toString(goalNames));

        spinnerGoal = findViewById(R.id.goalSelector);

        spinnerGoal.setOnItemSelectedListener(this);

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, goalNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoal.setAdapter(adapter);




        TextView t = (TextView) findViewById(R.id.currWordCount);
        String c = (String.valueOf(DB.getCum("Default Goal")));
        t.setText(c);

        TextView t2 = (TextView) findViewById(R.id.goalWordCount);
        String c2 = (String.valueOf(DB.getGoal("Default Goal").get("Goal")));
        t2.setText(c2);
    }

    public void editGoal(View v){
        Intent i = new Intent(this, GoalActivity.class);
        startActivity(i);
    }

    public void addWords(View v){
        Intent i = new Intent(this, AddWordsActivity.class);
        startActivity(i);
    }

    public void editLogs(View v){
        Intent i = new Intent(this, editLogsActivity.class);
        startActivity(i);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        if (adapterView.getId() == R.id.goalSelector) {
            String valueFromSpinner = adapterView.getItemAtPosition(position).toString();

            TextView t = (TextView) findViewById(R.id.currWordCount);
            String c = (String.valueOf(DB.getCum(valueFromSpinner)));
            t.setText(c);

            TextView t2 = (TextView) findViewById(R.id.goalWordCount);
            String c2 = (String.valueOf(DB.getGoal(valueFromSpinner).get("Goal")));
            Log.d("goal value", c2);
            t2.setText(c2);
        }


    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Cursor cursor = DB.getGoals();
        cursor.moveToFirst();
        String name = cursor.getString(1);

        TextView t = (TextView) findViewById(R.id.currWordCount);
        String c = (String.valueOf(DB.getCum(name)));
        t.setText(c);

        TextView t2 = (TextView) findViewById(R.id.goalWordCount);
        String c2 = (String.valueOf(DB.getGoal(name).get("Goal")));
        Log.d("goal value", c2);
        t2.setText(c2);
    }
}