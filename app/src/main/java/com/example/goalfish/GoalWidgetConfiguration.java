package com.example.goalfish;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.Spinner;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

public class GoalWidgetConfiguration extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    public static final String SHARED_PRES = "prefs";
    public static final String KEY_BUTTON_TEXT = "keyButtonText";

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private EditText editTextButton;
    DBHelper DB;
    private Spinner spinnerGoal;
    private String valueFromSpinner;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goal_widget_configuration);

        DB = new DBHelper(this);

        Intent configIntent = getIntent();
        Bundle extras = configIntent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_CANCELED, resultValue);

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        //editTextButton = findViewById(R.id.configureEntry);

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
        spinnerGoal = findViewById(R.id.configureEntry);
        spinnerGoal.setOnItemSelectedListener(this);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, goalNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoal.setAdapter(adapter);

        valueFromSpinner = DB.getDefaultGoal();
    }

    public void confirmConfiguration(View v) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        //String valueFromSpinner = editTextButton.getText().toString();

        String c = (String.valueOf(DB.getCum(valueFromSpinner)));
        String c2 = (String.valueOf(DB.getGoal(valueFromSpinner).get("Goal")));

        String startDate = (String) (DB.getGoal(valueFromSpinner)).get("End Date");
        int period = (int) (DB.getGoal(valueFromSpinner)).get("Period");
        int reoccurring = (int) (DB.getGoal(valueFromSpinner)).get("Reoccurring");
//        String result[] = DB.calculateDates(startDate, period, reoccurring);

        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate currentFinishDateDT = LocalDate.parse(startDate, myFormatObj);
        String daysBetween = String.valueOf(ChronoUnit.DAYS.between(currentDate, currentFinishDateDT));


        RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.progress_widget);
        views.setOnClickPendingIntent(R.id.widgetButton, pendingIntent);
        views.setProgressBar(R.id.progressBar, Integer.parseInt(c2), Integer.parseInt(c), false);
        views.setTextViewText(R.id.widgetWord, c);
        views.setTextViewText(R.id.widgetGoal, c2);
        views.setTextViewText(R.id.widgetDaysLeft, daysBetween);
        //views.setInt(R.id.widgetButton, "setBackgroundColor", COLOR.RED);

        appWidgetManager.updateAppWidget(appWidgetId, views);

        SharedPreferences prefs = getSharedPreferences(SHARED_PRES, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_BUTTON_TEXT + appWidgetId, valueFromSpinner);
        editor.apply();

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        // update values when something is selected on the dropdown
        if (adapterView.getId() == R.id.configureEntry) {
            valueFromSpinner = adapterView.getItemAtPosition(position).toString();

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // default to most recent goal for values
        Cursor cursor = DB.getGoals();
        cursor.moveToFirst();
        valueFromSpinner = cursor.getString(1);    }
}






















