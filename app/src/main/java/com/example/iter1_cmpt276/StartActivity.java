package com.example.iter1_cmpt276;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.iter1_cmpt276.model.InspectionReport;
import com.example.iter1_cmpt276.model.InspectionReportManager;
import com.example.iter1_cmpt276.model.Restaurant;
import com.example.iter1_cmpt276.model.RestaurantManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.net.ssl.HttpsURLConnection;

/*
Activity is used to prompt the user for updates if updates are available
or it has been more than 20 hours since the last update
 */
public class StartActivity extends AppCompatActivity {
    RestaurantManager restaurantManager;
    InspectionReportManager inspectionManager;

    private String restaurantLastModified = "";
    private String inspectionLastModified = "";
    private String restaurantURL = "";
    private String inspectionURL = "";
    private String restDir = "";
    private String inspDir = "";

    public static final String SHARED_TIME = "sharedtime";
    public static final String SHARED_PREF = "sharedpref";
    public static final String TEXT_DATE = "textdate";
    public static final String TEXT_TIME = "texttime";
    public static final String TEXT_RES = "textres";
    public static final String TEXT_INSP = "textinsp";
    private String resModified;
    private String inspModified;
    private String lastUpdateDate;
    private String lastUpdateTime;
    private static long currentTime = 0;

    private Dialog dialog;
    private AsyncTask asyncTask;
    private long WAITING_TIME = 72000000;

    private boolean RestaurantFlag;

    private static final String FILE_NAME_RESTAURANT = "updatedRestaurant.csv";
    private static final String FILE_NAME_INSPECTION = "updatedInspection.csv";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        initializeClassDataMembers();

        // Retrieves url and lastModifiedTime
        jsonParseRestaurant();
        jsonParseInspections();

        // Stores last modified date in resModified & inspModified
        getLastModified();

        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();


        // Check if there has ever been any updates or downloads
        if(resModified.isEmpty() || inspModified.isEmpty()) {
            dialogBoxUpdate();
        }

        else
        {
            // Stores last updated time & date in lastUpdateTime & lastUpdateDate
            getLastUpdateDateTime();

            LocalDate updateDate = LocalDate.parse(lastUpdateDate);
            LocalTime updateTime = LocalTime.parse(lastUpdateTime);

            // Gets the month number
            int currentMonth = currentDate.getMonthValue();
            int updateMonth = updateDate.getMonthValue();

            // Gets the day of the month
            int currentDay = currentDate.getDayOfMonth();
            int updateDay = updateDate.getDayOfMonth();

            // Gets the time in hours
            int currentHour = currentTime.getHour();
            int updateHour = updateTime.getHour();

            // Checks if its within the same month
            if (currentMonth == updateMonth)
            {
                if (currentDay == updateDay)
                {
                    if (currentHour - updateHour >= 20)
                    {
                        dialogBoxUpdate();
                    }
                    goToMap();
                }
                else
                {
                    if (currentDay - updateDay > 1)
                    {
                        dialogBoxUpdate();
                    }
                    goToMap();
                }
            }
            else
            {
                dialogBoxUpdate();
            }
        }
    }

    private void dialogBoxUpdate()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);

        builder.setTitle(R.string.update_title);

        builder.setMessage(R.string.update_message);

        builder.setPositiveButton(R.string.update_yes, new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Saves the current date and time when we update
                saveUpdateTime();
                saveLastModified();

                new JsonTask().execute(restaurantURL);
                new JsonTask().execute(inspectionURL);
            }
        });

        builder.setNegativeButton(R.string.update_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    readRestaurantData(FILE_NAME_RESTAURANT);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    readInspectionData(FILE_NAME_INSPECTION);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Intent i = new Intent(StartActivity.this, MapsActivity.class);
                finish();
                startActivity(i);
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void initializeClassDataMembers() {
        this.restaurantManager = RestaurantManager.getInstance(this);
        this.inspectionManager = InspectionReportManager.getInstance(this);
    }


    private void goToMap()
    {
        try {
            readRestaurantData(FILE_NAME_RESTAURANT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            readInspectionData(FILE_NAME_INSPECTION);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent i = new Intent(StartActivity.this, MapsActivity.class);
        finish();
        startActivity(i);
    }


    public void save(String filename, String text) throws IOException {
        try (FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE)) {
            fos.write(text.getBytes());
            restDir = getFilesDir() + "/" + filename;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readRestaurantData(String filename) throws IOException {
        restaurantManager.readFavData();
        try (FileInputStream fis = openFileInput(filename)) {
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String line;

            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

                Restaurant restaurant = new Restaurant();

                restaurant.setTrackingNumber(tokens[0]);

                // Checks if restaurant name starts with quotation mark
                String restaurantName = tokens[1];
                if(restaurantName.substring(0, 1).equals("\""))
                {
                    // Modify restaurantName
                    restaurantName = restaurantName.substring(1,restaurantName.length()-1);
                }
                restaurant.setName(restaurantName);

                restaurant.setAddress(tokens[2]);
                restaurant.setCity(tokens[3]);
                restaurant.setLatitude(Double.parseDouble(tokens[5]));
                restaurant.setLongitude(Double.parseDouble(tokens[6]));

                restaurantManager.add(restaurant);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(restaurantManager.getList(), new Restaurant());
    }

    public void readInspectionData(String filename) throws IOException {
        FileInputStream fis = null;
        try{
            fis = openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder  sb = new StringBuilder();
            String line = "";

            while( (line = br.readLine() )!= null) {
                String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);


                if(tokens[0].equals("") && tokens[1].equals("")){
                    continue;
                }
                InspectionReport report = new InspectionReport();
                report.setTrackingNumber(tokens[0]);
                report.setInspectionDate(tokens[1]);
                report.setInspType(tokens[2]);
                report.setNumCritical(Integer.parseInt(tokens[3]));
                report.setNumNonCritical(Integer.parseInt(tokens[4]));
                report.setViolLump(tokens[5]);
                report.setHazardRating(tokens[6]);

                inspectionManager.add(report);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if( fis != null){
                fis.close();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private class JsonTask extends AsyncTask<String, String, String> {

        @Override
        protected void onCancelled() {
            dialog.dismiss();
            goToMap();

            super.onCancelled();
            asyncTask.cancel(true);

            return;
        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            TextView text = findViewById(R.id.wait);
            text.setText(R.string.update_message_wait);

            try {
                URL url = new URL(params[0]);
                Thread.sleep(100);

                connection = (HttpURLConnection) url.openConnection();
                Thread.sleep(100);

                connection.connect();
                Thread.sleep(100);

                InputStream stream = connection.getInputStream();
                Thread.sleep(100);

                reader = new BufferedReader(new InputStreamReader(stream));
                Thread.sleep(100);

                StringBuffer buffer = new StringBuffer();
                Thread.sleep(100);

                String line = "";
                Thread.sleep(100);

                reader.readLine();
                Thread.sleep(100);

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                    Thread.sleep(10);
                   // Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)
                }
                return buffer.toString();

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (!RestaurantFlag) {
                try {
                    save(FILE_NAME_RESTAURANT, result); //takes around a minute to do
                    try {
                        readRestaurantData(FILE_NAME_RESTAURANT);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                RestaurantFlag = true;
            } else {
                try {
                    save(FILE_NAME_INSPECTION, result); //in total takes around 8 minutes 20 seconds
                    try {
                        readInspectionData(FILE_NAME_INSPECTION);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Intent i = new Intent(StartActivity.this, MapsActivity.class);
                    finish();
                    startActivity(i);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void jsonParseRestaurant() {
        StrictMode.ThreadPolicy threadPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(threadPolicy);

        try {
            URL remoteURL = new URL("https://data.surrey.ca/api/3/action/package_show?id=restaurants");
            HttpsURLConnection connection = (HttpsURLConnection) remoteURL.openConnection();
            InputStream response = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(response, StandardCharsets.UTF_8);
            HashMap<String, Object> map = new ObjectMapper().readValue(reader, HashMap.class);
            HashMap<String, Object> level1 = (HashMap<String, Object>) map.get("result");
            ArrayList<Object> level2 = (ArrayList) level1.get("resources");
            LinkedHashMap<String, String> level3 = (LinkedHashMap<String, String>) level2.get(0);

            // URL for restaurant CSV file
            restaurantURL = level3.get("url");

            // Last modified date
            restaurantLastModified = level3.get("last_modified");

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jsonParseInspections() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            URL remoteURL = new URL("https://data.surrey.ca/api/3/action/package_show?id=fraser-health-restaurant-inspection-reports");
            HttpsURLConnection connection = (HttpsURLConnection) remoteURL.openConnection();
            InputStream response = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(response, StandardCharsets.UTF_8);
            HashMap<String, Object> map = new ObjectMapper().readValue(reader, HashMap.class);
            HashMap<String, Object> level1 = (HashMap<String, Object>) map.get("result");
            ArrayList<Object> level2 = (ArrayList) level1.get("resources");
            LinkedHashMap<String, String> level3 = (LinkedHashMap<String, String>) level2.get(0);

            // Get URL for inspection report
            inspectionURL = level3.get("url");

            // Get last modified date
            inspectionLastModified = level3.get("last_modified");

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveLastModified()
    {
        SharedPreferences sharedModification = getApplicationContext().getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedModification.edit();
        editor.putString(TEXT_RES, restaurantLastModified);
        editor.putString(TEXT_INSP, inspectionLastModified);
        editor.apply();
    }

    private void getLastModified()
    {
        SharedPreferences sharedLastModified = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        resModified = sharedLastModified.getString(TEXT_RES, "");
        inspModified = sharedLastModified.getString(TEXT_INSP,"");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void saveUpdateTime()
    {
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        String editDate = currentDate + "";
        String editTime = currentTime + "";

        SharedPreferences sharedUpdateTime = getApplicationContext().getSharedPreferences(SHARED_TIME, MODE_PRIVATE);
        SharedPreferences.Editor editorTime = sharedUpdateTime.edit();
        editorTime.putString(TEXT_DATE, editDate);
        editorTime.putString(TEXT_TIME, editTime);
        editorTime.apply();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getLastUpdateDateTime()
    {
        SharedPreferences sharedUpdateDateTime = getSharedPreferences(SHARED_TIME, MODE_PRIVATE);
        lastUpdateTime = sharedUpdateDateTime.getString(TEXT_TIME, "");
        lastUpdateDate = sharedUpdateDateTime.getString(TEXT_DATE,"");
        assert lastUpdateDate != null;
        if(lastUpdateDate.equals("")) {
            lastUpdateDate = LocalDate.now() + "";
            lastUpdateTime = LocalTime.now() + "";
        }
    }

}