package com.example.iter1_cmpt276;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.iter1_cmpt276.model.InspectionReport;
import com.example.iter1_cmpt276.model.InspectionReportManager;
import com.example.iter1_cmpt276.model.ViolationReport;
import com.example.iter1_cmpt276.model.ViolationReportManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
      This class displays data of the selected inspection report in the SingleRestaurantActivity.
      The data includes date, inspection type, critical issues found, non-critical issues found, hazard rating,
      and violations of the restaurant's inspection and all of the violation reports.
      If user clicks a violation, it launches a new toast message displaying the entire violation in detail.
 */
public class ViolationReportActivity extends AppCompatActivity {

    private ViolationReportManager violationManager;
    private InspectionReportManager inspectionManager;

    private static final String INDEX_POSITION = "Extra - Position";
    public static final String TRACKING_NUMBER = "Tracking Number";

    private int inspectionReportIndex;
    private String trackingNumber;
    private final String HIGH = "High";
    private final String MODERATE = "Moderate";
    private final String LOW = "Low";

    private List<ViolationReport> list = new ArrayList<>();
    private List<String> violations = new ArrayList<>();
    private ListView violList;
    private ArrayList<String> arrayList = new ArrayList<>();
    private List<InspectionReport> reportChunk;
    private InspectionReport reportSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_violation_reportactivity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        violationManager = ViolationReportManager.getInstance();
        inspectionManager = InspectionReportManager.getInstance(this);

        readViolationData();
        extractDataFromIntent();
        setUI();
        createPrintableViolation();
        registerClick();
    }

    private void setUI(){
        String date = reportSelected.getInspectionDate();
        String[] showToTextview = {printInspectionReportDetails(date), reportSelected.getInspType(),
                " " + reportSelected.getNumCritical(), " " + reportSelected.getNumNonCritical(),
                reportSelected.getHazardRating()};
        populateScreen(showToTextview);
    }

    public static Intent makeLaunchIntent(Context context, int position, String trackingNumber)
    {
        Intent intent = new Intent(context, ViolationReportActivity.class);
        intent.putExtra(INDEX_POSITION, position);
        intent.putExtra(TRACKING_NUMBER, trackingNumber);
        return intent;
    }

    public void extractDataFromIntent()
    {
        Intent intent = getIntent();
        inspectionReportIndex = intent.getIntExtra(INDEX_POSITION, 0);
        trackingNumber = intent.getStringExtra(TRACKING_NUMBER);
        reportChunk = inspectionManager.getReportListByTrackingNum(trackingNumber);
        reportSelected = reportChunk.get(inspectionReportIndex);
    }

    private void populateScreen(String[] showToTextView)
    {
        String date = showToTextView[0];
        String routine = getString(R.string.inspection_type, showToTextView[1]);
        String criticalNum = getString(R.string.critical_issue, showToTextView[2]);
        String nonCriticalNum = getString(R.string.non_critical_issue, showToTextView[3]);
        String hazardRating = showToTextView[4];
        String hazardCheck = showToTextView[4];

        TextView dateText = (TextView) findViewById(R.id.dateOfViolation);
        dateText.setText(date);

        TextView routineText = (TextView) findViewById(R.id.routineViolation);
        routineText.setText(routine);

        TextView criticalNumText = (TextView) findViewById(R.id.criticalViolation);
        criticalNumText.setText(criticalNum);

        TextView nonCriticalNumText = (TextView) findViewById(R.id.noncriticalViolation);
        nonCriticalNumText.setText(nonCriticalNum);

        TextView hazardRatingText = (TextView) findViewById(R.id.hazardViolation);
        hazardRatingText.setText(hazardRating);
        if(hazardRating.equals(HIGH)){
            hazardRatingText.setTextColor(Color.RED);
        }
        else if(hazardRating.equals(MODERATE)){
            hazardRatingText.setTextColor(getResources().getColor(R.color.yellow));
        }
        else{
            hazardRatingText.setTextColor(Color.GREEN);
        }

        ImageView hazardImage = (ImageView) findViewById(R.id.hazardPicture);

        if (hazardCheck.equals(LOW))
        {
            hazardImage.setImageResource(R.drawable.haz_low);
        }
        else if (hazardCheck.equals(MODERATE))
        {
            hazardImage.setImageResource(R.drawable.haz_mod);
        }
        else if (hazardCheck.equals(HIGH))
        {
            hazardImage.setImageResource(R.drawable.haz_high);
        }
    }

    private void readViolationData()
    {
        if(violationManager.size() == 0)
        { //checks to see if it was already read or not before
            InputStream is = getResources().openRawResource(R.raw.allviolations);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, Charset.forName("UTF-8"))
            );

            String line = "";
            try
            {
                //Step over header
                reader.readLine();
                //Step over blank second line
                reader.readLine();

                int i = 0;

                while ((line = reader.readLine()) != null) {
                    // Split by ','
                    String[] tokens = line.split(",");

                    // Read the data
                    ViolationReport report = new ViolationReport();
                    report.setViolationNum(Integer.parseInt(tokens[0]));
                    report.setViolationStatus(tokens[1]);
                    report.setViolationDescription(tokens[2]);
                    report.setViolationFinalString(tokens[3]);
                    report.setIconId(getIconImage(Integer.parseInt(tokens[0])));

                    list.add(report);
                }
            } catch (IOException e) {
                Log.wtf("MyActivity", "Error reading data file on line " + line, e);
                e.printStackTrace();
            }
        }
    }

    private String printInspectionReportDetails(String inspDate)
    {
        String date = inspDate;
        String year = date.substring(0, 4);
        String month = date.substring(4, 6);
        String day = date.substring(6, 8);

        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

        int tempMonth = Integer.parseInt(month);
        tempMonth--;
        month = months[tempMonth];

        return month + " " + day + ", " + year;
    }

    private void createPrintableViolation()
    {
        int violationNum = 0;
        String violationDesc = "";

        // Get the string of violation(s)
        String allViol = reportSelected.getViolLump();
        String newAllViol;
        if(allViol.length() == 0){
            newAllViol = "";
        }
        else {
            newAllViol = allViol.substring(1, allViol.length() - 1);
        }

        if(!newAllViol.isEmpty())//ink
        {
            // Create list of all the violations in the report
            violations = Arrays.asList(newAllViol.split("\\|"));

            for (int i = 0; i < violations.size(); i++) {
                // Retrieves violation number
                violationNum = Integer.parseInt(violations.get(i).substring(0,3));
                // Loops through list of all violations

                for (int j = 0; j < list.size(); j++) {
                    if (violationNum == list.get(j).getViolationNum()) {
                        // Gets the description of violation description by comparing violation number
                        String desc = getString(list.get(j).getShortViolationDescription(violationNum));
                        arrayList.add(desc);
                    }
                }
            }
            violList = (ListView) findViewById(R.id.violList);
            ArrayAdapter<String> adapter = new myListAdapter();
            violList.setAdapter(adapter);
        }
    }

    private class myListAdapter extends ArrayAdapter<String> {

        public myListAdapter() {
            super(ViolationReportActivity.this,R.layout.item_view,violations);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View itemView = convertView;
            if( itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.item_view, parent, false);

            }
            String violation = violations.get(position);
            int violationNum = 0;
            for (int i = 0; i < violations.size(); i++) {
                // Retrieves violation number
                violationNum = Integer.parseInt(violation.substring(0,3));
                // Loops through list of all violations

                for (int j = 0; j < list.size(); j++) {
                    if (violationNum == list.get(j).getViolationNum()) {
                        ImageView image = (ImageView)itemView.findViewById(R.id.itemIcon);
                        image.setImageResource(list.get(j).getIconId());

                        String violationDesc = "";
                        TextView text = (TextView)itemView.findViewById(R.id.itemDescription);
                        violationDesc = getString(list.get(j).getShortViolationDescription(violationNum));
                        text.setText(violationDesc);

                        ImageView statusIcon = (ImageView)itemView.findViewById(R.id.itemSeverityIcon);
                        if(list.get(j).getViolationStatus().equals("Critical")){
                            statusIcon.setImageResource(R.drawable.critical);
                        }
                        else{
                            statusIcon.setImageResource(R.drawable.not_critical);
                        }
                        TextView statusText = (TextView)itemView.findViewById(R.id.itemStatus);
                        statusText.setText(list.get(j).getViolationStatus());
                        if(list.get(j).getViolationStatus().equals("Critical")) {
                            statusText.setTextColor(Color.RED);
                        }
                        else{
                            statusText.setTextColor(getResources().getColor(R.color.yellow));
                        }
                    }
                }
            }
            return itemView;
        }
    }

    public void registerClick()
    {
        ListView violList = (ListView) findViewById(R.id.violList);
        violList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String message = "" + violations.get(position);
                Toast.makeText(ViolationReportActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private int getIconImage(int violationNum){
        // Get the violation number and set image accordingly
        if (violationNum >= 100 && violationNum < 200)
        {
            return R.drawable.permit;
        }
        else if (violationNum >= 200 && violationNum < 300)
        {
            return R.drawable.hazardous_food;
        }
        else if (violationNum >= 300 && violationNum < 304 || violationNum == 306 || violationNum == 314)
        {
            return R.drawable.unsanitary;
        }
        else if (violationNum >= 304 && violationNum <= 305)
        {
            return R.drawable.pest;
        }
        else if (violationNum >= 307 && violationNum <= 308 || violationNum == 310 || violationNum == 315)
        {
            return R.drawable.equipment;
        }
        else if (violationNum == 309)
        {
            return R.drawable.dangerous_chemical;
        }
        else if (violationNum >= 311 && violationNum <= 313)
        {
            return R.drawable.premisis;
        }
        else if (violationNum >= 400 && violationNum < 500)
        {
            return R.drawable.employee;
        }
        else if (violationNum >= 500)
        {
            return R.drawable.no_food_safe;
        }
        return 0;
    }

    // For back button on the tool bar
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
            case R.id.back:
                finish();
                return true;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_at_violation_activity, menu);
        return true;
    }
}
