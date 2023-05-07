package com.example.goalfish;

import static com.example.goalfish.GoalWidgetConfiguration.KEY_BUTTON_TEXT;
import static com.example.goalfish.GoalWidgetConfiguration.SHARED_PRES;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TextView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;


public class WidgetProvider extends AppWidgetProvider {

    DBHelper DB;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {

            // get the goal name for this widget
            SharedPreferences prefs = context.getSharedPreferences(SHARED_PRES, Context.MODE_PRIVATE);
            String valueFromSpinner = prefs.getString(KEY_BUTTON_TEXT + appWidgetId, "Default Goal");

            DB = new DBHelper(context);

            String c = (String.valueOf(DB.getCum(valueFromSpinner)));
            String c2 = (String.valueOf(DB.getGoal(valueFromSpinner).get("Goal")));

            String startDate = (String) (DB.getGoal(valueFromSpinner)).get("Start Date");
            int period = (int) (DB.getGoal(valueFromSpinner)).get("Period");
            int reoccurring = (int) (DB.getGoal(valueFromSpinner)).get("Reoccurring");
//            String result[] = DB.calculateDates(startDate, period, reoccurring);
            LocalDate currentDate = LocalDate.now();
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate currentFinishDateDT = LocalDate.parse(startDate, myFormatObj);
            String daysBetween = String.valueOf(ChronoUnit.DAYS.between(currentDate, currentFinishDateDT));

            // opens main activity on button press
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.progress_widget);
            views.setOnClickPendingIntent(R.id.widgetButton, pendingIntent);
            views.setProgressBar(R.id.progressBar, Integer.parseInt(c2), Integer.parseInt(c), false);
            views.setTextViewText(R.id.widgetWord, c);
            views.setTextViewText(R.id.widgetGoal, c2);
            views.setTextViewText(R.id.widgetDaysLeft, daysBetween);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.progress_widget);

        int minWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int maxWidth = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
        int minHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
        int maxHeight = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT);

        if (maxWidth > 150) {
            views.setInt(R.id.widgetButton, "width", 140);
        }



    }
}


