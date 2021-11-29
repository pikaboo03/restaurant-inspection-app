package com.example.iter1_cmpt276.model;

import com.example.iter1_cmpt276.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/*
 * This class models data of inspection report objects of restaurants.
 */
public class InspectionReport implements Comparable<InspectionReport>{
    private String trackingNumber;
    private String inspectionDate;
    private String InspType;
    private int numCritical;
    private int numNonCritical;
    private String HazardRating;
    private String violLump;
    private Calendar lastInspection;
    private String formattedDate;
    private int hazardIconID;

    public InspectionReport() {
        numCritical = 0;
        numNonCritical =0;
        HazardRating = "";
        violLump = "";
        lastInspection = Calendar.getInstance();
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getInspectionDate() {
        return inspectionDate;
    }

    public void setInspectionDate(String inspectionDate) {
        this.inspectionDate = inspectionDate;
    }

    public Calendar getLastInspection(){
        return lastInspection;
    }

    public void setLastInspection(Calendar inspection){
        lastInspection = inspection;
    }

    public String getInspType() {
        return InspType;
    }

    public void setInspType(String inspType) {
        this.InspType = inspType;
    }

    public int getNumCritical() {
        return numCritical;
    }

    public void setNumCritical(int numCritical) {
        this.numCritical = numCritical;
    }

    public int getNumNonCritical() {
        return numNonCritical;
    }

    public void setNumNonCritical(int numNonCritical) {
        this.numNonCritical = numNonCritical;
    }

    public String getHazardRating() {
        return HazardRating;
    }

    public void setHazardRating(String hazardRating) {
        HazardRating = hazardRating;
    }

    public String getViolLump() {
        return violLump;
    }

    public void setViolLump(String violLump) {
        this.violLump = violLump;
    }
    public void addViolLump(String string) {
        violLump = violLump;
    }

    //Icons found at [https://www.iconsdb.com/guacamole-green-icons/checkmark-icon.html]
    public int getHazardIconID(String hazardRating){
        String hazard = hazardRating.toLowerCase();
        if (hazard.equalsIgnoreCase("high")){
            this.hazardIconID = R.drawable.haz_high;
        } else if (hazard.equalsIgnoreCase("moderate")){
            this.hazardIconID = R.drawable.haz_mod;
        } else {
            this.hazardIconID = R.drawable.haz_low;
        }
        return hazardIconID;
    }

    public String toString(){
        return  " Date : " + getFormattedDate(inspectionDate) + "\n" +
                "# critical issues : " + numCritical + "\n" +
                "# non-critical issues: " + numNonCritical;
    }

    public int compareTo(InspectionReport o){
        int compare = inspectionDate.compareTo(o.inspectionDate);
        return compare;
    }

    public void date(int d){
        int thatDay = d%100;
        d = d/100;
        int thatMonth =(d)%100;
        d = d/100;
        int thatYear = d;
        lastInspection.set(thatYear, thatMonth-1, thatDay);
    }

    // Format inspection date as instructed
    public String getFormattedDate(String inspectionDate) {
        formattedDate = "";
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String inspYear = "";
        String inspMonth = "";
        String inspDay = "";

        String currDate = date;
        String currYear = currDate.substring(0, 4);
        String currMonth = currDate.substring(5, 7);
        String currDay = currDate.substring(8, 10);

        inspYear = inspectionDate.substring(0, 4);
        inspMonth = inspectionDate.substring(4, 6);
        inspDay = inspectionDate.substring(6, 8);

        int inspYearInt = Integer.parseInt(inspYear);
        int inspMonthInt = Integer.parseInt(inspMonth);
        int inspDayInt = Integer.parseInt(inspDay);

        int currYearInt = Integer.parseInt(currYear);
        int currMonthInt = Integer.parseInt(currMonth);
        int currDayInt = Integer.parseInt(currDay);

        String[] months = {"January", "February", "March", "April", "May", "June", "July",
                "August", "September", "October", "November", "December"};
        int temp = inspMonthInt;
        temp--;
        inspMonth = months[temp]; // Get the month in words

        if ((currYearInt - inspYearInt) <= 1) // Inspection was potentially under a year ago
        {
            if ((currMonthInt - inspMonthInt) >= 1 && currYearInt != inspYearInt) // Inspection was more than a year ago (by months)
            {
                formattedDate = inspMonth + ", " + inspYear;
            } else {
                if (currYearInt == inspYearInt && (currMonthInt - inspMonthInt) <= 1) // Same year and within one month
                {
                    int diff = 0;
                    if ((currMonthInt == inspMonthInt)) {
                        diff = currDayInt - inspDayInt;
                        formattedDate = diff + " day(s) ago";
                    } else {
                        diff = (30 - inspDayInt);
                        diff = diff + currDayInt;
                        if (diff >= 30) {
                            formattedDate = inspMonth + ", " + inspDay;
                        } else {
                            formattedDate = diff + " day(s) ago";
                        }
                    }
                } else {
                    formattedDate = inspMonth + ", " + inspDay;
                }
            }
        } else { // Inspection was more than a year ago (by years)
            formattedDate = inspMonth + ", " + inspYear;
        }
        this.formattedDate = formattedDate;
        return formattedDate;
    }
}
