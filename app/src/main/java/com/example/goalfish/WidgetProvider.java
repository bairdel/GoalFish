package com.example.goalfish;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.TextView;

public class WidgetProvider extends AppWidgetProvider {

    DBHelper DB;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {

            DB = new DBHelper(context);
            //TextView t = (TextView) findViewById(R.id.widgetWord);
            String c = (String.valueOf(DB.getCum("Default Goal")));
            //t.setText(c);

            //TextView t2 = (TextView) findViewById(R.id.widgetGoal);
            String c2 = (String.valueOf(DB.getGoal("Default Goal").get("Goal")));
            //t2.setText(c2);



            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.progress_widget);
            views.setOnClickPendingIntent(R.id.widgetButton, pendingIntent);
            views.setProgressBar(R.id.progressBar, Integer.parseInt(c2), Integer.parseInt(c), false);
            views.setTextViewText(R.id.widgetWord, c);
            views.setTextViewText(R.id.widgetGoal, c2);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
