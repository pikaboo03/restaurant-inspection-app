package com.example.iter1_cmpt276;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.iter1_cmpt276.model.InspectionReport;
import com.example.iter1_cmpt276.model.InspectionReportManager;
import com.example.iter1_cmpt276.model.Restaurant;
import com.example.iter1_cmpt276.model.RestaurantManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/*
  This class displays data of the selected restaurant in the MainActivity.
  The data includes name, address, and GPS coordinates of the restaurant, and all of the inspection report.
  If user clicks a report, it launches a new activity by sending key of a report,
  the detailed info regarding the report is available in the next screen.
 */
public class  SingleRestaurantActivity extends AppCompatActivity {
    public static final String TRACKING_NUMBER = "Tracking Number";
    public static final String RESTAURANT_INDEX = "Restaurant List Index";
    private RestaurantManager restaurantManager;
    private InspectionReportManager inspectionManager;
    private Restaurant CurrRestaurant;
    private List<InspectionReport> inspectionReports = new ArrayList<>();
    private List<InspectionReport> reportsCalled;
    private String trackingNumber;
    private int position;
    private TextView restaurantName;
    private TextView restaurantAddress;
    private TextView restaurantCoords;
    private final String HIGH = "High";
    private final String MODERATE = "Moderate";
    private final String LOW = "Low";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_restaurant);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        inspectionManager = InspectionReportManager.getInstance(this);

        if(inspectionManager.getNumReports() == 0) {
            readReportsData();
        }

        extractDataFromIntent();
        setUIRestaurantData();
        populateReportList();
        registerClickCall();

        Button gpsBtn = (Button) findViewById(R.id.text_coord);
        gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SingleRestaurantActivity.this, MapsActivity.class);
                finish();
                startActivity(i);
            }
        });
    }

    private void extractDataFromIntent() {
        Intent intent = getIntent();
        this.trackingNumber = intent.getStringExtra(TRACKING_NUMBER);
        this.position = intent.getIntExtra(RESTAURANT_INDEX, 0);

        restaurantManager = RestaurantManager.getInstance(this);
        CurrRestaurant = restaurantManager.getRestaurantByTrackingNum(trackingNumber);
        reportsCalled = inspectionManager.getReportListByTrackingNum(trackingNumber);
    }

    public static Intent makeLaunchIntent(Context context, int position, String trackingNumber) {
        Intent intent = new Intent(context, SingleRestaurantActivity.class);
        intent.putExtra(TRACKING_NUMBER, trackingNumber);
        intent.putExtra(RESTAURANT_INDEX, position);
        return intent;
    }

    private void setUIRestaurantData() {
        restaurantName = findViewById(R.id.text_name);
        restaurantAddress = findViewById(R.id.text_address);
        restaurantCoords = findViewById(R.id.text_coord);

        String name = CurrRestaurant.getName();
        String physAddress = CurrRestaurant.getAddress();
        String city = CurrRestaurant.getCity();
        double latitude = CurrRestaurant.getLatitude();
        double longitude = CurrRestaurant.getLongitude();

        restaurantName.setText(getString(R.string.restaurantName, name));
        restaurantAddress.setText(getString(R.string.address, physAddress, city));
        restaurantCoords.setText(getString(R.string.coords, formatM(latitude), formatM(longitude)));
    }

    private void populateReportList() {
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                // do nothing
            }
        }, 1000);

        ArrayAdapter<InspectionReport> adapter = new MyAdapter();
        ListView reportList = findViewById(R.id.list_inspection_reports);
        reportList.setAdapter(adapter);
    }

    private class MyAdapter extends ArrayAdapter<InspectionReport> {
        MyAdapter() {
            super(SingleRestaurantActivity.this,
                    R.layout.inspection_report_row, reportsCalled);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View reportView = convertView;
            System.out.println(position);

            if (reportView == null) {
                reportView = getLayoutInflater().inflate(R.layout.item_view_report_list,
                        parent, false);
            }

            InspectionReport currentReport = reportsCalled.get(position);

            // Set hazard icon, label, and color by hazard rating
            String hazardRating = currentReport.getHazardRating();
            int hazardIconID = currentReport.getHazardIconID(hazardRating);

            ImageView reportHazard = (ImageView) reportView.findViewById(R.id.icon_hazard_rating);
            TextView hazardLev = (TextView) reportView.findViewById(R.id.txt_hazard_level);
            reportHazard.setImageResource(hazardIconID);

            if (hazardRating.equalsIgnoreCase(HIGH)) {
                hazardLev.setText(getString(R.string.hazardHigh));
                hazardLev.setTextColor(Color.RED);
            } else if (hazardRating.equalsIgnoreCase(MODERATE)) {
                hazardLev.setText(getString(R.string.hazardMod));
                hazardLev.setTextColor(getResources().getColor(R.color.yellow));
            } else if (hazardRating.equalsIgnoreCase(LOW)) {
                hazardLev.setText(getString(R.string.hazardLow));
                hazardLev.setTextColor(Color.GREEN);
            }

            TextView reportInfo = (TextView) reportView.findViewById(R.id.txt_report_descrip);
            String inspDate = currentReport.getInspectionDate();
            String formattedDate = currentReport.getFormattedDate(inspDate);
            int critIssue = currentReport.getNumCritical();
            int nonCritIssue = currentReport.getNumNonCritical();
            reportInfo.setText(getString(R.string.report_list_fill, formattedDate, critIssue, nonCritIssue));

            return reportView;
        }
    }

    private void registerClickCall() {
        ListView list = (ListView) findViewById(R.id.list_inspection_reports);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                Intent intent = ViolationReportActivity
                        .makeLaunchIntent(SingleRestaurantActivity.this, position, trackingNumber);
                startActivity(intent);
            }
        });
    }

    private String formatM(double distanceInM) {
        DecimalFormat df = new DecimalFormat("0.000");
        return df.format(distanceInM);
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
            case R.id.home:
                finish();
                return true;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        //https://www.vhv.rs/viewpic/wmmohT_favorite-icon-png-image-free-download-searchpng-heart/
        final Drawable favIcon = getDrawable(R.drawable.favourite_icon);
        final Drawable notFavIcon =getDrawable(R.drawable.non_favourite_icon);
        getMenuInflater().inflate(R.menu.menu_at_single_restaurant_activity, menu);
        MenuItem icon = menu.findItem(R.id.favourite_icon);
        icon.setActionView(R.layout.favourite_layout);
        RelativeLayout layout = (RelativeLayout) icon.getActionView();
        final ImageView favImage = (ImageView) layout.getChildAt(0);
        favImage.setBackground(notFavIcon);

        if (restaurantManager.isFavourite(CurrRestaurant.getTrackingNumber())) {
            favImage.setBackground(favIcon);
        }

        favImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean favouriteRestaurant = restaurantManager.isFavourite(CurrRestaurant.getTrackingNumber());
                if (!favouriteRestaurant) {
                    favImage.setBackground(favIcon);
                    restaurantManager.updateFavRestData(CurrRestaurant, true);
                } else {
                    favImage.setBackground(notFavIcon);
                    restaurantManager.updateFavRestData(CurrRestaurant, false);
                }
            }
        });

        return true;
    }

    // Code based on the Dr.Fraser's Youtube tutorial regarding reading CSV file
    private void readReportsData() {
        InputStream is = getResources().openRawResource(R.raw.inspectionreports_itr1);
        BufferedReader reader = new BufferedReader (
                new InputStreamReader(is, Charset.forName("UTF-8"))
        );
        String line = "";
        try {
            reader.readLine();
            while ( (line = reader.readLine()) != null) {
                Log.d("My Activity", "Line: " + line);

                // Separate by comma but not commas contained in quotation marks;
                String[] tokens = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                // Code found at [https://stackoverflow.com/questions/1757065/java-splitting-a-comma-separated-string-but-ignoring-commas-in-quotes]
                InspectionReport report = new InspectionReport();
                report.setTrackingNumber(tokens[0]);
                report.setInspectionDate(tokens[1]);
                report.setInspType(tokens[2]);
                report.setNumCritical(Integer.parseInt(tokens[3]));
                report.setNumNonCritical(Integer.parseInt(tokens[4]));
                report.setHazardRating(tokens[5]);
                report.setViolLump(tokens[6]);

                inspectionReports.add(report);
                Log.d("MyActivity", "Just created: " + report);
            }
        } catch (IOException e) {
            Log.wtf("MyActivity", "Error reading data file on line " + line, e);
            e.printStackTrace();
        }
        storeData();
    }

    private void storeData() {
        inspectionManager.setList(inspectionReports);
    }

    @Override
    public void finish() {

        super.finish();

        restaurantManager.updateFavRestData(CurrRestaurant, restaurantManager.isFavourite(CurrRestaurant.getTrackingNumber()));

        return;

    }
}