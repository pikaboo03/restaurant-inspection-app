package com.example.iter1_cmpt276.model;

import android.content.Context;

import com.example.iter1_cmpt276.R;
import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/*
 * This class manages handling of inspection report objects in the android activity
 * by static instance method.
 */
public class InspectionReportManager implements Iterable<InspectionReport> {
    private List<InspectionReport> inspectionReports = new ArrayList<>();
    List<InspectionReport> reportsCalled;
    private static InspectionReportManager instance;
    private static Context context;

    private String localFileLocation = "inspectionreports_itr1.csv";
    private String localTempFileLocation = "temp_inspectionreports_itr1.csv";
    public static InspectionReportManager getInstance(Context c) {
        context = c;
        if (instance == null) {
            instance = new InspectionReportManager();
        }
        return instance;
    }

    private InspectionReportManager() {
        //readData();
    }

    public void readData() {
        CSVReader csvReader = new CSVReader(new InputStreamReader(context.getResources().openRawResource(R.raw.inspectionreports_itr1)));

        try {
            String[] iRow = csvReader.readNext(); //skip first line (titles);
            while ((iRow = csvReader.readNext()) != null) {
                InspectionReport inspection = new InspectionReport();
                inspection.setTrackingNumber(iRow[0]);
                inspection.setInspectionDate(iRow[1]);
                inspection.date(Integer.parseInt(iRow[1]));
                inspection.setInspType(iRow[2]);
                inspection.setNumCritical(Integer.parseInt(iRow[3]));
                inspection.setNumNonCritical(Integer.parseInt(iRow[4]));
                inspection.setHazardRating(iRow[5]);
                inspection.setViolLump(iRow[6]);
                inspectionReports.add(inspection);
            }
            csvReader.close();
        } catch (Exception e) {
            // do nothing
        }
    }

    public void readInData(){
        InputStream fileInputStream;
        InputStream inputStream;
        BufferedReader bufferedReader;

        try {
            fileInputStream = context.openFileInput(localFileLocation);
            bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
            inputStream = context.getResources().openRawResource(R.raw.inspectionreports_itr1);
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        }
        String dataLine = null;
        String[] tokens;

        try {
            while (dataLine != null) {
                // The below regular pattern ignores commas when in double quotes
                tokens = dataLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                for (int i = 0; i < tokens.length; i++) {
                    tokens[i] = tokens[i].trim();
                }

                //InspectionReport currentInspection;
                try {
                    InspectionReport inspection = new InspectionReport();
                    inspection.setTrackingNumber(tokens[0]);
                    inspection.setInspectionDate(tokens[1]);
                    inspection.date(Integer.parseInt(tokens[1]));
                    inspection.setInspType(tokens[2]);
                    inspection.setNumCritical(Integer.parseInt(tokens[3]));
                    inspection.setNumNonCritical(Integer.parseInt(tokens[4]));
                    inspection.setHazardRating(tokens[5]);
                    inspection.setViolLump(tokens[6]);
                    inspectionReports.add(inspection);
                } catch (Exception e) {
                    // do nothing
                }
                dataLine = bufferedReader.readLine();
            }
        }catch (
                IOException e) {
            e.printStackTrace();
        }
        return;
    }

    public void add(InspectionReport report) {
        inspectionReports.add(report);
    }

    public void remove(InspectionReport report) {
        inspectionReports.remove(report);
    }

    public InspectionReport get(int i) {
        return inspectionReports.get(i);
    }

    @Override
    public Iterator<InspectionReport> iterator() {
        return inspectionReports.iterator();
    }

    public int getNumReports() {
        return inspectionReports.size();
    }

    // set the list in the singleton class from external data
    public void setList(List<InspectionReport> inspectionReports){
        this.inspectionReports = inspectionReports;
    }

    public ArrayList<InspectionReport> returnList(){
        return (ArrayList<InspectionReport>) inspectionReports;
    }

    public ArrayList<InspectionReport> getInspectionByRestaurant(String inputID) {
        ArrayList<InspectionReport> temp = new ArrayList<>();
        for (int i = 0; i < inspectionReports.size(); i++) {
            if (inspectionReports.get(i).getTrackingNumber().equals(inputID)) {
                temp.add(inspectionReports.get(i));
            }
        }
        return temp;
    }

    public InspectionReport getLatestByTrackingID(String inputID) {
        ArrayList<InspectionReport> temp = this.getInspectionByRestaurant(inputID);
        InspectionReport latest = temp.get(0);
        for (int i = 1; i < temp.size(); i++) {
            if (inspectionReports.get(i).getLastInspection().compareTo(latest.getLastInspection()) > 0) {
                latest = temp.get(i);
            }
        }
        return latest;
    }

    public List<InspectionReport> getReportListByTrackingNum (String trackingNumber){
        reportsCalled = new ArrayList<>();
        for (InspectionReport report : inspectionReports){
            if (trackingNumber.equals(report.getTrackingNumber())){
                reportsCalled.add(report);
            }
        }
        Collections.sort(reportsCalled, Collections.<InspectionReport>reverseOrder());
        return reportsCalled;
    }


    public String getLocalFileLocation() {
        return this.localFileLocation;
    }

    public String getLocalTempFileLocation() {
        return this.localTempFileLocation;
    }

    public List<InspectionReport> getInspections(){
        return inspectionReports;
    }


}