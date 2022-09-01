package com.example.goalfish;

import static com.example.goalfish.GoalWidgetConfiguration.KEY_BUTTON_TEXT;
import static com.example.goalfish.GoalWidgetConfiguration.SHARED_PRES;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.TextView;


public class WidgetProvider extends AppWidgetProvider {

    DBHelper DB;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {

            SharedPreferences prefs = context.getSharedPreferences(SHARED_PRES, Context.MODE_PRIVATE);
            String valueFromSpinner = prefs.getString(KEY_BUTTON_TEXT + appWidgetId, "Default Goal");

            DB = new DBHelper(context);
            //TextView t = (TextView) findViewById(R.id.widgetWord);
            String c = (String.valueOf(DB.getCum(valueFromSpinner)));
            //t.setText(c);

            //TextView t2 = (TextView) findViewById(R.id.widgetGoal);
            String c2 = (String.valueOf(DB.getGoal(valueFromSpinner).get("Goal")));
            //t2.setText(c2);

            String startDate = (String) (DB.getGoal(valueFromSpinner)).get("Start Date");
            int period = (int) (DB.getGoal(valueFromSpinner)).get("Period");
            int reoccurring = (int) (DB.getGoal(valueFromSpinner)).get("Reoccurring");

            String result[] = DB.calculateDates(startDate, period, reoccurring);

            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.progress_widget);
            views.setOnClickPendingIntent(R.id.widgetButton, pendingIntent);
            views.setProgressBar(R.id.progressBar, Integer.parseInt(c2), Integer.parseInt(c), false);
            views.setTextViewText(R.id.widgetWord, c);
            views.setTextViewText(R.id.widgetGoal, c2);
            views.setTextViewText(R.id.widgetDaysLeft, result[0]);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
