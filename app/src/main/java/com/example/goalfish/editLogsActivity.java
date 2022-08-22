package com.example.goalfish;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Arrays;
import java.util.ArrayList;

import de.codecrafters.tableview.TableView;
import de.codecrafters.tableview.toolkit.SimpleTableDataAdapter;
import de.codecrafters.tableview.toolkit.SimpleTableHeaderAdapter;

public class editLogsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    DBHelper DB;
    private Spinner spinnerGoal;
    String currentGoal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_logs);

        DB = new DBHelper(this);
        currentGoal = "Default Goal";

        // getting current goal
        Cursor res2 = DB.getGoals();
        int rows2 = res2.getCount() + 1; // number of rows to get
        String[] goalNames1 = new String[rows2]; // new string array of length items found
        int i = 0;
        while(res2.moveToNext()){
            goalNames1[i] = res2.getString(1);
            i += 1;
        }
        String[] goalNames = Arrays.copyOf(goalNames1, goalNames1.length - 1); // last item seems to always end up as null
        Log.d("dataArray", Arrays.toString(goalNames));

        spinnerGoal = findViewById(R.id.logsDropdown);
        spinnerGoal.setOnItemSelectedListener(this);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, goalNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoal.setAdapter(adapter);

        refreshLogs();
    }

    public void refreshLogs(){
        // update the data in table based on currentGoal
        TableView tableView = findViewById(R.id.table_data_view);
        String[] headers={"Date", "Words", "Total"};


        // getting words written
        Log.d("current goal", currentGoal);
        Cursor res = DB.getSpecificData(currentGoal);
        int rows = res.getCount() + 1;
        String[][] data = new String[rows][3];
        int j = 0;
        while(res.moveToNext()){
//            data[i][0] = res.getString(1);
//            data[i][1] = res.getString(2);
//            data[i][2] = res.getString(3);

            String[] tempData = new String[3];
            tempData[0] = (res.getString(1));
            tempData[1] = (res.getString(2));
            tempData[2] = (res.getString(3));
            Log.d("dataArray", Arrays.toString(tempData));
            data[j] = tempData;
            j += 1;
        }
        String[][] newData = Arrays.copyOf(data, data.length - 1);
        // add data to table
        tableView.setHeaderAdapter(new SimpleTableHeaderAdapter(this, headers));
        tableView.setDataAdapter(new SimpleTableDataAdapter(this, newData));
    }

    public void deleteEntry(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure?");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // continue with delete

                // delete last log for selected goal on button click
                Boolean checkDeleteData = DB.deleteWords(currentGoal);
                if (checkDeleteData==true) {
                    Toast.makeText(editLogsActivity.this, "Entry Deleted", Toast.LENGTH_LONG).show();
                    Log.d("entryinserted", "success");
                }else{
                    Toast.makeText(editLogsActivity.this, "Entry Not Deleted", Toast.LENGTH_LONG).show();
                    Log.d("entryinserted", "fail");
                }

                // refresh page
                Intent i = getIntent();
                finish();
                startActivity(i);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // do nothing


            }
        });
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.show();



    }

    public void deleteGoal(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure?");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // continue with delete

                // delete last log for selected goal on button click
                Boolean checkDeleteData = DB.deleteGoal(currentGoal);
                if (checkDeleteData==true) {
                    Toast.makeText(editLogsActivity.this, "Goal Deleted", Toast.LENGTH_LONG).show();
                    Log.d("entryinserted", "success");
                }else{
                    Toast.makeText(editLogsActivity.this, "Goal Not Deleted", Toast.LENGTH_LONG).show();
                    Log.d("entryinserted", "fail");
                }

                // refresh page
                Intent i = getIntent();
                finish();
                startActivity(i);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // do nothing


            }
        });
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.show();



    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        if (adapterView.getId() == R.id.logsDropdown) {
            String valueFromSpinner = adapterView.getItemAtPosition(position).toString();

            currentGoal = valueFromSpinner;
            refreshLogs();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        if (adapterView.getId() == R.id.logsDropdown) {
            Cursor cursor = DB.getGoals();
            cursor.moveToFirst();
            String name = cursor.getString(1);
            currentGoal = name;
            refreshLogs();
        }
    }
}