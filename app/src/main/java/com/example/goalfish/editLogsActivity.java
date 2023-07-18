package com.example.goalfish;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

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

        currentGoal = DB.getDefaultGoal();
//        currentGoal = "Default Goal";

        Dictionary goalsDictionary = DB.getGoals();
        String[] goalNames = (String[]) goalsDictionary.get("goalNames");
        int defaultSpinnerIndex = (int) goalsDictionary.get("defaultSpinnerIndex");

        spinnerGoal = findViewById(R.id.logsDropdown);
        spinnerGoal.setOnItemSelectedListener(this);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, goalNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoal.setAdapter(adapter);
        spinnerGoal.setSelection(defaultSpinnerIndex);


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
        String[][] data = new String[rows][3]; // empty array no. entries down 3 across
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
        String[][] newData = Arrays.copyOf(data, data.length - 1); // removing last entry
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

                // update widget
                String cum = (String.valueOf(DB.getCum(currentGoal)));
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(editLogsActivity.this);
                RemoteViews remoteViews = new RemoteViews(editLogsActivity.this.getPackageName(), R.layout.progress_widget);
                remoteViews.setTextViewText(R.id.widgetWord, cum);
                String c2 = (String.valueOf(DB.getGoal(currentGoal).get("Goal")));
                remoteViews.setProgressBar(R.id.progressBar, Integer.parseInt(c2), Integer.parseInt(cum), false);
                final int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(editLogsActivity.this, WidgetProvider.class));
                appWidgetManager.partiallyUpdateAppWidget(appWidgetIds, remoteViews);


                // refresh page
//                Intent i = getIntent();
//                finish();
//                startActivity(i);
                refreshLogs();

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

    public void changeName(View v) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Edit Goal");

        // Set an EditText view to get user input
//        final EditText input = new EditText(this);
//        alert.setView(input);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20,20,20, 20);

        final TextView goalNameMessage = new TextView(this);
        goalNameMessage.setText("Goal Name: ");
        layout.addView(goalNameMessage);

        final EditText goalName = new EditText(this);
        goalName.setHint("Goal Name");
        goalName.setText((String) DB.getGoal(currentGoal).get("Goal Name"));
        layout.addView(goalName);

        final TextView wordGoalMessage = new TextView(this);
        wordGoalMessage.setText("Word Goal: ");
        layout.addView(wordGoalMessage);

        final EditText wordGoal = new EditText(this);
        wordGoal.setHint("Words Goal");
        wordGoal.setText(String.valueOf((int) DB.getGoal(currentGoal).get("Goal")));
        wordGoal.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(wordGoal);

        final TextView periodMessage = new TextView(this);
        periodMessage.setText("Period of Time: ");
        layout.addView(periodMessage);

        final EditText period = new EditText(this);
        period.setHint("Period");
        period.setText(String.valueOf((int) DB.getGoal(currentGoal).get("Period")));
        period.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(period);

        LinearLayout layoutReoccurring = new LinearLayout(this);
        layoutReoccurring.setOrientation(LinearLayout.HORIZONTAL);

        final TextView reoccurringMessage = new TextView(this);
        reoccurringMessage.setText("Reoccurring: ");
        layoutReoccurring.addView(reoccurringMessage);

        final Switch reoccurring = new Switch(this);
        int checkValue = (int) DB.getGoal(currentGoal).get("Reoccurring");
        if (checkValue == 0) {
            reoccurring.setChecked(false);
        } else {
            reoccurring.setChecked(true);
        }
        //reoccurring.setGravity(Gravity.LEFT);
        layoutReoccurring.addView(reoccurring);

        layout.addView(layoutReoccurring);

        LinearLayout layoutDefault = new LinearLayout(this);
        layoutDefault.setOrientation(LinearLayout.HORIZONTAL);

        final TextView defaultMessage = new TextView(this);
        defaultMessage.setText("Default Goal: ");
        layoutDefault.addView(defaultMessage);

        final Switch isDefault = new Switch(this);
        int checkValue2 = (int) DB.getGoal(currentGoal).get("isDefault");
        if (checkValue2 == 0) {
            isDefault.setChecked(false);
        } else {
            isDefault.setChecked(true);
        }
        isDefault.setGravity(Gravity.LEFT);
        layoutDefault.addView(isDefault);

        layout.addView(layoutDefault);


//        final EditText wordGoal = new EditText(this);
//        wordGoal.setHint("Words Goal");
//        layout.addView(wordGoal);

        alert.setView(layout); // Again this is a set method, not add


        int goalId = DB.convertGoalToId((String) DB.getGoal(currentGoal).get("Goal Name"));

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
//                String newName = input.getText().toString();
                Dictionary dict = new Hashtable();
                dict.put("Goal ID", goalId);
                dict.put("Goal Name", goalName.getText().toString());
                dict.put("Goal", Integer.parseInt(wordGoal.getText().toString()));
                dict.put("Period", Integer.parseInt(period.getText().toString()));
//                dict.put("Start Date", );
                dict.put("Reoccurring", reoccurring.isChecked());
                dict.put("isDefault", isDefault.isChecked());

                Log.d("change goal", dict.toString());
                Boolean checkChangeName = DB.changeGoalName(currentGoal, dict);
                if (checkChangeName == true) {
                    Toast.makeText(editLogsActivity.this, "Goal Changed", Toast.LENGTH_LONG).show();
                    Log.d("entryinserted", "success");
                } else {
                    Toast.makeText(editLogsActivity.this, "Goal Not Changed", Toast.LENGTH_LONG).show();
                    Log.d("entryinserted", "fail");
                }
                // refresh page
                Intent i = getIntent();
                finish();
                startActivity(i);

            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();




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
            currentGoal = DB.getDefaultGoal();
            refreshLogs();
        }
    }
}