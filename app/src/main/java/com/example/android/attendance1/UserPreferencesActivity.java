package com.example.android.attendance1;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.TestLooperManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class UserPreferencesActivity extends AppCompatActivity {


    //SharedPreference to save Identity
    private SharedPreferences mSharedPref;

    //Employees List as List Array
    private ArrayList<String> mEmployees = new ArrayList<String>();

    //Auto Complete Text View
    private AutoCompleteTextView mAutoEmployeesTextView;

    //Button to save Employee ID after validation
    private Button saveButton;

    //Back Button Pressed counter
    private int backCount = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_preferences);


        //Make List of Employees
        mEmployees.add("12411 ( David Ray )");
        mEmployees.add("12412 ( James Chandler )");
        mEmployees.add("12421 ( Arnold James )");
        mEmployees.add("12422 ( Harry Grey )");
        mEmployees.add("12531 ( Perry Tesla )");
        mEmployees.add("12532 ( Remond John )");
        mEmployees.add("12541 ( Silverster Blake )");
        mEmployees.add("12542 ( Jonny Joe )");
        mEmployees.add("13651 ( Herald Jonson )");
        mEmployees.add("13652 ( Ricky Mavea )");
        mEmployees.add("13661 ( Adam Grey )");
        mEmployees.add("13662 ( Ricky Robert )");
        mEmployees.add("13771 ( Jake Robert )");
        mEmployees.add("13772 ( Kate Sandler )");
        mEmployees.add("13781 ( Billy John )");


        //Get Identity from EditText & save in Global String Variable mIdentity
        mAutoEmployeesTextView = (AutoCompleteTextView) findViewById(R.id.list_of_employees);

        //Define Array Adapter for the list of Employees
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(UserPreferencesActivity.this, R.layout.support_simple_spinner_dropdown_item, mEmployees);

        mAutoEmployeesTextView.setAdapter(adapter);

        mAutoEmployeesTextView.setThreshold(1);




        //Define Button & set Listener
        saveButton = (Button) findViewById(R.id.save_button);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                //String to save Identity from EditText
                String identityString = mAutoEmployeesTextView.getText().toString();

                if(mEmployees.contains(identityString)){
                    //If Id is valid, save success prompt
                    Toast saveSuccess = Toast.makeText(UserPreferencesActivity.this, "Thank You, Identity Saved Successfully", Toast.LENGTH_SHORT);
                    saveSuccess.show();

                    //Initialize SharedPreferences instance
                    mSharedPref = getSharedPreferences("saveData", Context.MODE_PRIVATE);

                    //Set Editor for SharedPreference Instance & store Identity
                    SharedPreferences.Editor editorToStore = mSharedPref.edit();
                    editorToStore.putString("Identity", identityString + mEmployees.indexOf(identityString));

                    editorToStore.commit();

                    //send Identity to MainActivity
                    Intent callMain = new Intent(UserPreferencesActivity.this, MainActivity.class);
                    startActivity(callMain);

                }else{

                    //If Id is Blank, display Toast
                    Toast plzInput = Toast.makeText(UserPreferencesActivity.this, "Please Input Valid Identity to Proceed", Toast.LENGTH_SHORT);
                    plzInput.show();
                    mAutoEmployeesTextView.setText("");

                }

            }
        });

    }

    @Override
    public void onBackPressed() {
        if(backCount>0){
            finishAffinity();
        }
        else{
            Toast.makeText(this,"Press again to exit", Toast.LENGTH_SHORT).show();
            backCount++;

            new CountDownTimer(2000, 1000) {
                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    backCount = 0;
                }
            }.start();
        }

    }
}
