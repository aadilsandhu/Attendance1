package com.example.android.attendance1;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Time;
import java.util.Date;

import static android.icu.util.Calendar.DAY_OF_MONTH;
import static android.icu.util.Calendar.DAY_OF_WEEK;
import static android.icu.util.Calendar.HOUR_OF_DAY;
import static android.icu.util.Calendar.MINUTE;
import static android.icu.util.Calendar.MONTH;
import static android.icu.util.Calendar.YEAR;
import static android.icu.util.Calendar.getInstance;

public class MainActivity extends AppCompatActivity {

    //String for User Identity
    private String mUserIdentity, mQueryString, mOffice;

    //SharedPreference to Get Identity
    private SharedPreferences mSharedPrefGet;

    //Location Manager & Location Listeners to get Location of User
    private LocationManager mlocationManager;
    private LocationListener mlocationListener;

//    private final double LOCATION_1_LATITUDE = 31.457623;
//    private final double LOCATION_1_LONGITUDE = 74.307990;

    //CMPAK
//    private final double LOCATION_1_LATITUDE = 31.432105;
//    private final double LOCATION_1_LONGITUDE = 74.322484;

    //Home
    private final double LOCATION_1_LATITUDE = 32.160017;
    private final double LOCATION_1_LONGITUDE = 74.199631;

    private final float LOCATION_DELTA = 100f;

    //Max Digits of variables
    private final int USER_ID_DIGITS= 6;
    private final int LOCATION_DIGITS= 4;
    private final int MONTH_DIGITS= 2;
    private final int DATE_DIGITS= 2;
    private final int TIME_DIGITS= 2;

    //Network Connectivity Instances
    ConnectivityManager mConnectivityManager;
    NetworkInfo mActiveNetwork;

    //TextView
    TextView mDisplayId, mStatusOfApp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Read Identity of User from SharedPreferences
        readPreferences();


        if (mUserIdentity.isEmpty()) {
            Intent userPreferences = new Intent(MainActivity.this, UserPreferencesActivity.class);
            startActivity(userPreferences);
        } else {
            setContentView(R.layout.activity_main);
            mDisplayId = (TextView) findViewById(R.id.display_id);
            mDisplayId.setText("Hi, " + mUserIdentity.substring(mUserIdentity.indexOf("(" ) + 1, mUserIdentity.indexOf(")" ))
                                + "\n\nPress \"Locate Me\" When you are in office Premises");


            mStatusOfApp = (TextView) findViewById(R.id.status_of_app);
            mStatusOfApp.setText("Please Ensure Internet Connectivity...");


            //Button to Locate
            Button locateButton = (Button) findViewById(R.id.locate_button);
            locateButton.setVisibility(View.VISIBLE);
            locateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    locateMe();
                }
            });


        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mlocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mlocationListener);
                mlocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocationListener);

                //Enable GPS
                enableGPS(mlocationManager);
            }
        }
    }

    private void readPreferences() {

        //Initialize SharedPreferences instance
        mSharedPrefGet = getSharedPreferences("saveData", Context.MODE_PRIVATE);

        //Read Preferences & update UserId
        mUserIdentity = mSharedPrefGet.getString("Identity", "");


    }

    private Boolean checkNetwork(){
        //Internet Connection Check
        //Network Connectivity Instances
        mConnectivityManager =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        mActiveNetwork = mConnectivityManager.getActiveNetworkInfo();

        return mActiveNetwork != null && mActiveNetwork.isConnected();

    }

    private void enableGPS(LocationManager locationManager){
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Toast enableGPSToast = Toast.makeText(this, "Please enable Location through GPS",Toast.LENGTH_SHORT);
            enableGPSToast.show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }else
            mStatusOfApp.setText("Locating...");

    }

    private void locateMe(){

        //Internet Connectivity Check
        mConnectivityManager =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        mActiveNetwork = mConnectivityManager.getActiveNetworkInfo();

        //Proceed with GPS enable after verification of Internet connectivity
        if(mActiveNetwork != null && mActiveNetwork.isConnected()) {

            //Load Office Location
            final Location officeLocation = new Location("Load Manually");
            officeLocation.setLatitude(LOCATION_1_LATITUDE);
            officeLocation.setLongitude(LOCATION_1_LONGITUDE);
            mOffice = "42";

            //Locate User
            mlocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);

            mlocationListener = new LocationListener() {

                @Override
                public void onLocationChanged(Location location) {

                    //Remove Listener
                    mlocationManager.removeUpdates(mlocationListener);
                    if (location.distanceTo(officeLocation) <= LOCATION_DELTA) {



                        //Make QueryString
                        mQueryString = makeQueryString(location);



                        //Open weChat with Link
                        try {



                            Intent sendIntent = new Intent(Intent.ACTION_SEND);
                            sendIntent.setPackage("com.tencent.mm");
                            sendIntent.putExtra(Intent.EXTRA_TEXT, "https://aadilsandhu.github.io/?" + mQueryString);
                            sendIntent.setType("text/plain");
                            startActivity(sendIntent);

                        } catch (Exception e) {
                            //App not found
                            e.printStackTrace();
                        }



                    } else {
                        //keep Listening to Location Updates
                        mStatusOfApp.setText("Waiting to be in Office");

                    }

                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };

            if (Build.VERSION.SDK_INT < 21) {
                mlocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mlocationListener);
                mlocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocationListener);
                //Enable GPS
                enableGPS(mlocationManager);

            } else {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.

                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    return;
                } else {
                    mlocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mlocationListener);
                    mlocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocationListener);
                    //Enable GPS
                    enableGPS(mlocationManager);
                }
            }
        } else {
            Toast enableInternet = Toast.makeText(this, "You are not Connected to Internet\n" +
                    "Please Ensure Connectivity to Proceed",Toast.LENGTH_SHORT);
            enableInternet.show();
        }

    }

    //Function to make Query String
    private String makeQueryString (Location location){

        String idNo, officeNo, year, month, date, day, hour, minute;


        Calendar dateTime = Calendar.getInstance(TimeZone.getTimeZone("GMT+05:00"));
        dateTime.setTimeInMillis(location.getTime());

        //check on validity of string values

        //User ID Number 0-99999
        idNo = validString(mUserIdentity.substring(mUserIdentity.indexOf(")") + 1, mUserIdentity.length()), USER_ID_DIGITS);

        //Office Number 0-9999
        officeNo = validString(mOffice, LOCATION_DIGITS);

        //Year
        year = String.valueOf(dateTime.get(YEAR));

        //Month 0-11
        month = validString(String.valueOf(dateTime.get(MONTH)), MONTH_DIGITS);

        //Date 0-31
        date = validString(String.valueOf(dateTime.get(DAY_OF_MONTH)), DATE_DIGITS);

        //Day 1-7
        day = String.valueOf(dateTime.get(DAY_OF_WEEK));

        //Hour 0-24
        hour = validString(String.valueOf(dateTime.get(HOUR_OF_DAY)), TIME_DIGITS);

        //Hour 0-24
        minute = validString(String.valueOf(dateTime.get(MINUTE)), TIME_DIGITS);

        String mQueryString1, mQueryString2, mTempString, mCode;

        mQueryString1 = mUserIdentity.replace(" ", "");
        mQueryString1 = mQueryString1.substring(0, mQueryString1.indexOf(")") + 1)
                +  date + getMonth(dateTime.get(MONTH)) + year + "_" + hour + ":" + minute + "_";

        mCode = getCode(mQueryString1, hour + minute, date);

        mTempString = idNo + officeNo + year + month + date + day + hour + minute;

        mQueryString2 = modifyString(mTempString, mCode);

        int displaceIndex = Integer.valueOf(minute) % 23;

        mQueryString2 = mQueryString2.substring(displaceIndex) + mQueryString2.substring(0, displaceIndex);

        return mQueryString1 + mQueryString2;
    }


    //add 0 at left to get desired String Length
    private String validString (String validate, int maxLength){
        for (int i = validate.length(); i < maxLength; i++)
                validate = "0" + validate;
        return validate;
    }

    //resolve month Name
    private String getMonth(int monthNo){
        String monthName;
        switch (monthNo) {
            case 0:
                monthName = "Jan";
                break;

            case 1:
                monthName = "Feb";
                break;

            case 2:
                monthName = "Mar";
                break;

            case 3:
                monthName = "Apr";
                break;

            case 4:
                monthName = "May";
                break;

            case 5:
                monthName = "Jun";
                break;

            case 6:
                monthName = "Jul";
                break;

            case 7:
                monthName = "Aug";
                break;

            case 8:
                monthName = "Sep";
                break;

            case 9:
                monthName = "Oct";
                break;

            case 10:
                monthName = "Nov";
                break;

            case 11:
                monthName = "Dec";
                break;

            default:
                monthName = "";
                break;

        }
        return monthName;
    }

    //make code as seed
    private String getCode(String qString1, String hourMinute, String date){

        int index = qString1.indexOf('(');
        int calculatedValue = (Integer.valueOf(qString1.substring(index - 3, index)) + Integer.valueOf(hourMinute))
                * Integer.valueOf(date) * 7;

        return String.valueOf(calculatedValue);
    }

    //modify String
    private String modifyString(String tempString, String code){

        char[][] modification = {{'+', 'w', 's', 'c', 'u', 'o', '-', 'n', 'y', 'v'}, {'-', 'd', 't', 'v', 'n', '+', 'a', 'q', 'o', 'g'}, {'p', 'i', 'r', 'q', 'e', 'w', 'x', '+', 's', '-'}, {'d', 'g', '+', 't', '-', 'y', 'z', 'e', 'i', 'j'}, {'j', 'q', 'f', 'x', 'b', 't', 'w', 'p', '+', 'r'}, {'l', 'b', '-', 'j', 'r', 's', '+', 'c', 'a', 't'}, {'v', 'e', 'y', 'g', 'k', 'j', 'm', '-', 'f', 'u'}, {'z', 'o', 'w', '-', '+', 'd', 'p', 'b', 'e', 'f'}, {'c', 'k', 'n', 'h', 'f', '-', 'o', 't', 'q', '+'}, {'q', 'm', 'd', '+', 'a', 'x', 'l', 'k', '-', 'e'}};
        String strModified = "";
        String index1, index2;
        for(int stringIndex = 0; stringIndex < tempString.length(); )
            for(int codeIndex = 0; codeIndex < code.length(); codeIndex++){
                index1 = String.valueOf(tempString.charAt(stringIndex));
                index2 = String.valueOf(code.charAt(codeIndex));
                strModified = strModified + modification[Integer.valueOf(index1)][Integer.valueOf(index2)];
                stringIndex++;
                if(stringIndex == tempString.length())
                    break;
            }

        return strModified;
    }

}
