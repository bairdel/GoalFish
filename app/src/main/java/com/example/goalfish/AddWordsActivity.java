package com.example.goalfish;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

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

        // create the dropdown menu with the list of goals
        Cursor res = DB.getGoals();
        int rows = res.getCount() + 1;
        String[] goalNames1 = new String[rows];
        int i = 0;
        while(res.moveToNext()){ // add goals to a list
            goalNames1[i] = res.getString(1);
            i += 1;
        }
        String[] goalNames = Arrays.copyOf(goalNames1, goalNames1.length - 1); // last item is null for some reason

        spinnerGoal = findViewById(R.id.goalSelectorWords);
        spinnerGoal.setOnItemSelectedListener(this);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, goalNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGoal.setAdapter(adapter);


    }

    public void submitWords(View v){
        // submit new words written to db - when button pressed

        // get submitted value
        TextView textView = findViewById(R.id.newWords);
        String s = textView.getText().toString();
        int t = Integer.parseInt(s);

        // get current date as string
        LocalDate myDateObj = LocalDate.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = myDateObj.format(myFormatObj);

        int cum;
        cum = DB.getCum(currentGoal) + t; // update cumulative total

        // insert data into wordLogs table
        Boolean checkInsertData = DB.insertLogsData(formattedDate, t, cum, currentGoal);

        // check if inserted - might not work
        if (checkInsertData==true) {
            Toast.makeText(AddWordsActivity.this, "New Entry Inserted", Toast.LENGTH_SHORT).show();
            Log.d("entryinserted", "success");
        }else{
            Toast.makeText(AddWordsActivity.this, "Entry Not Inserted", Toast.LENGTH_SHORT).show();
            Log.d("entryinserted", "fail");
        }


        // return to main activity
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    public void calculateWords (View view) {
        //Create a View object yourself through inflater
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.activity_word_calculator, null);

        //Specify the length and width through constants
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        //Make Inactive Items Outside Of PopupWindow
        boolean focusable = true;

        //Create a window with our parameters
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        //Set the location of the window on the screen
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        //Initialize the elements of our window, install the handler

        SharedPreferences sharedpreferences = getSharedPreferences("com.example.goalfish.oldwords", Context.MODE_PRIVATE);
        String oldWords = sharedpreferences.getString("oldWordCount", "");
        EditText leftCount = popupView.findViewById(R.id.oldWordCount);
        leftCount.setText(oldWords);



        Button buttonEdit = popupView.findViewById(R.id.messageButton);
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int newWordCount = Integer.parseInt(((TextView)popupView.findViewById(R.id.newWordCount)).getText().toString());
                int oldWordCount = Integer.parseInt(((TextView)popupView.findViewById(R.id.oldWordCount)).getText().toString());

                int newWords = newWordCount - oldWordCount;

                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString("oldWordCount", String.valueOf(newWordCount));
                editor.commit();

                EditText wordInput = findViewById(R.id.newWords);
                wordInput.setText(String.valueOf(newWords));

                popupWindow.dismiss();

            }
        });

        // close window when outside clicked
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //Close the window when clicked
                popupWindow.dismiss();
                return true;
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        // change associated goal when dropdown changes
        if (adapterView.getId() == R.id.goalSelectorWords) {
            String valueFromSpinner = adapterView.getItemAtPosition(position).toString();

            currentGoal = valueFromSpinner;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // default to most recently added goal if user does not select anything
        if (adapterView.getId() == R.id.goalSelectorWords) {
            Cursor cursor = DB.getGoals();
            cursor.moveToFirst();
            String name = cursor.getString(1);
            Log.d("default selection", name);
            currentGoal = name;
        }
    }
}