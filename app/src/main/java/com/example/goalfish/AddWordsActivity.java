package com.example.goalfish;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class AddWordsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    DBHelper DB;
    private Spinner spinnerGoal;
    String currentGoal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_words);

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

        spinnerGoal = findViewById(R.id.goalSelectorWords);

        spinnerGoal.setOnItemSelectedListener(this);

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, goalNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoal.setAdapter(adapter);

    }

    public void submitWords(View v){
        TextView textView = findViewById(R.id.newWords);
        String s = textView.getText().toString();
//        Log.d("newWords", s);

        int t = Integer.parseInt(s);

        LocalDate myDateObj = LocalDate.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = myDateObj.format(myFormatObj);

        int cum;
        cum = DB.getCum(currentGoal) + t;

        Log.d("editgoal", String.valueOf(formattedDate));
        Log.d("editgoal", String.valueOf(t));
        Log.d("editgoal", String.valueOf(cum));
        Log.d("editgoal", String.valueOf(currentGoal));

        Boolean checkInsertData = DB.insertLogsData(formattedDate, t, cum, currentGoal);
        if (checkInsertData==true) {
            Toast.makeText(AddWordsActivity.this, "New Entry Inserted", Toast.LENGTH_SHORT).show();
            Log.d("entryinserted", "success");
        }else{
            Toast.makeText(AddWordsActivity.this, "Entry Not Inserted", Toast.LENGTH_SHORT).show();
            Log.d("entryinserted", "fail");
        }

        Intent i = new Intent(this, MainActivity.class);
//        i.putExtra("newWords", s);
        startActivity(i);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        if (adapterView.getId() == R.id.goalSelectorWords) {
            String valueFromSpinner = adapterView.getItemAtPosition(position).toString();

            currentGoal = valueFromSpinner;
            Log.d("current goal", currentGoal);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        if (adapterView.getId() == R.id.goalSelectorWords) {
            Cursor cursor = DB.getGoals();
            cursor.moveToFirst();
            String name = cursor.getString(1);
            Log.d("default selection", name);
            currentGoal = name;
            Log.d("current goal", currentGoal);
        }
    }
}