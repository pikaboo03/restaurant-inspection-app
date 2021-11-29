package com.example.iter1_cmpt276.model;

import android.content.res.Resources;

import com.example.iter1_cmpt276.R;

/*
 * This class models data of violations that belong to single inspection report of a restaurant.
 */
public class ViolationReport {
    private int violationNum;
    private String violationStatus;
    private String violationDescription;
    private String violationFinalString;
    private int shortViolationDescription;
    private int iconId;


    public void setViolationStatus(String violationStatus) {
        this.violationStatus = violationStatus;
    }

    public void setViolationDescription(String violationDescription) {
        this.violationDescription = violationDescription;
    }

    public void setViolationNum(int violationNum) {
        this.violationNum = violationNum;
    }

    public void setViolationFinalString(String violationFinalString) {
        this.violationFinalString = violationFinalString;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public int getViolationNum() {
        return violationNum;
    }

    public String getViolationDescription() {
        return violationDescription;
    }

    public String getViolationFinalString() {
        return violationFinalString;
    }

    public String getViolationStatus() {
        return violationStatus;
    }

    public int getIconId() {
        return iconId;
    }

    // get short description based on the violationDescription
    public int getShortViolationDescription (int violationNum) {
        int shortdescrip = 0;
        if (violationNum >= 100 && violationNum < 200 || violationNum == 311) {
            shortdescrip = R.string.regulation;
        } else if (violationNum >= 200 && violationNum < 300) {
            shortdescrip = R.string.hazard_food;
        } else if (violationNum >= 300 && violationNum < 304 || violationNum == 306 || violationNum == 314) {
            shortdescrip = R.string.unsanitary;
        } else if (violationNum >= 304 && violationNum <= 305) {
            shortdescrip = R.string.pest;
        } else if (violationNum >= 307 && violationNum <= 308 || violationNum == 310 || violationNum == 315) {
            shortdescrip = R.string.equipment;
        } else if (violationNum == 309) {
            shortdescrip = R.string.chemical;
        } else if (violationNum >= 311 && violationNum < 313) {
            shortdescrip = R.string.premise;
        } else if (violationNum == 313) {
            shortdescrip = R.string.animal;
        } else if (violationNum >= 400 && violationNum < 500) {
            shortdescrip = R.string.employee;
        } else if (violationNum >= 500) {
            shortdescrip = R.string.foodsafe;
        }
        this.shortViolationDescription = shortdescrip;
        return shortdescrip;
    }
}
