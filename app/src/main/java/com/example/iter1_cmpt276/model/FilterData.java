package com.example.iter1_cmpt276.model;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.iter1_cmpt276.MapsActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;

import java.util.Date;
import java.util.List;
import java.util.Set;


public class FilterData {

    private Boolean isFavourite = false;

    private String hazardLevel;

    private int numberOfViolationsMoreThan;

    private String searchRestaurantByName;

    private List<Integer> restaurantPositionInFilterBox = new ArrayList<>();

    private RestaurantManager restaurantManager;

    private InspectionReportManager inspectionReportManager;


    private static FilterData instance;

    public FilterData() {
    }

    public static FilterData getInstance() {
        if (instance == null) {
            instance = new FilterData();
        }
        return instance;
    }

    public void setRestaurantManager(RestaurantManager restaurantManager) {
        this.restaurantManager = restaurantManager;
    }

    public RestaurantManager getRestaurantManager() {
        return restaurantManager;
    }

    public void setInspectionManager(InspectionReportManager inspectionManager) {
        this.inspectionReportManager = inspectionManager;
    }

    public InspectionReportManager getInspectionManager() {
        return inspectionReportManager;
    }

    public Boolean getFavourite() {
        return isFavourite;
    }

    public void setFavourite(Boolean favoriteFilterBox) {
        isFavourite = favoriteFilterBox;
    }

    public String getHazardLevel() {
        return hazardLevel;
    }

    public void setHazardLevel(String hazardLevel) {
        this.hazardLevel = hazardLevel;
    }

    public int getNumberOfViolationsMoreThan() {
        return numberOfViolationsMoreThan;
    }

    public void setNumberOfViolationsMoreThan(int numberOfViolationsMoreThan) {
        this.numberOfViolationsMoreThan = numberOfViolationsMoreThan;
    }

    public String getSearchRestaurantByName() {
        return searchRestaurantByName;
    }

    public void setSearchRestaurantByName(String s) {
        searchRestaurantByName = s.toLowerCase();
    }

    public List<Integer> getRestaurantPositionInFilterBox() {
        return restaurantPositionInFilterBox;
    }

    public void addRestaurantPosition(int position) {
        this.restaurantPositionInFilterBox.add(position);
    }

    public void clearRestaurantPosition() {
        this.restaurantPositionInFilterBox.clear();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public ArrayList<Restaurant> getFilteredRestaurants() throws ParseException {
        ArrayList<Restaurant> listRestaurant1 = new ArrayList<>();
        boolean nameUsed = false;
        ArrayList<Restaurant> listRestaurant2 = new ArrayList<>();
        boolean hazardRatingUsed = false;
        ArrayList<Restaurant> listRestaurant3 = new ArrayList<>();
        boolean violationsUsed = false;
        ArrayList<Restaurant> listRestaurant4 = new ArrayList<>();
        List<InspectionReport> listInspection = new ArrayList<>();


        if (!(searchRestaurantByName.equals(""))) {
            nameUsed = true;
            for (int i = 0; i < restaurantManager.getNumReports(); i++) {
                if (restaurantManager.get(i).getName().toLowerCase().contains(searchRestaurantByName.toLowerCase())) {
                    listRestaurant1.add(restaurantManager.get(i));
                }
            }
            for (int i = 0; i < listRestaurant1.size(); i++) {
                Restaurant currentRestaurant = listRestaurant1.get(i);

                for (int j = 0; j < inspectionReportManager.getNumReports(); j++) {
                    if (inspectionReportManager.get(j).getTrackingNumber().equals(currentRestaurant.getTrackingNumber())) {
                        listInspection.add(inspectionReportManager.get(j));
                    }
                }
            }
        }

        if (!(hazardLevel.equals(""))) {
            hazardRatingUsed = true;
            if(listRestaurant1.size() == 0) {
                for (int i = 0; i < restaurantManager.getNumReports(); i++) {
                    Restaurant currentRestaurant = restaurantManager.get(i);
                    List<InspectionReport> inspections = new ArrayList<>();

                    for (int j = 0; j < inspectionReportManager.getNumReports(); j++) {
                        if (inspectionReportManager.get(j).getTrackingNumber().equals(currentRestaurant.getTrackingNumber())) {
                            inspections.add(inspectionReportManager.get(j));
                        }
                    }

                    if (inspections.size() != 0) {
                        InspectionReport latest = inspections.get(0);
                        for (int j = 1; j < inspections.size(); j++) {

                            if (inspectionReportManager.getInspections().get(j).getLastInspection().compareTo(latest.getLastInspection()) > 0) {
                                latest = inspections.get(j);
                            }
                        }

                        if (latest.getHazardRating().toLowerCase().equals(hazardLevel.toLowerCase())) {

                            listRestaurant2.add(currentRestaurant);
                        }
                    }
                }
            }
            else{
                for (int i = 0; i < listRestaurant1.size(); i++) {
                    Restaurant currentRestaurant = listRestaurant1.get(i);
                    List<InspectionReport> inspections = new ArrayList<>();

                    for (int j = 0; j < inspectionReportManager.getNumReports(); j++) {
                        if (inspectionReportManager.get(j).getTrackingNumber().equals(currentRestaurant.getTrackingNumber())) {
                            inspections.add(inspectionReportManager.get(j));
                        }
                    }

                    if (inspections.size() != 0) {
                        InspectionReport latest = inspections.get(0);
                        for (int j = 1; j < inspections.size(); j++) {

                            if (inspectionReportManager.getInspections().get(j).getLastInspection().compareTo(latest.getLastInspection()) > 0) {
                                latest = inspections.get(j);
                            }
                        }

                        if (latest.getHazardRating().toLowerCase().equals(hazardLevel.toLowerCase())) {

                            listRestaurant2.add(currentRestaurant);
                        }
                    }
                }
            }


        }
        if(numberOfViolationsMoreThan != -1) {
            violationsUsed = true;
            String currentDate = LocalDate.now() + "";
            int year = Integer.parseInt(currentDate.substring(0, 4));
            year--;
            String lastYearDate = year + currentDate.substring(4);
            Date time1 = new SimpleDateFormat("yyyy-MM-dd").parse(currentDate);
            Date time2 = new SimpleDateFormat("yyyy-MM-dd").parse(lastYearDate);


            if(listRestaurant1.size() == 0 && listRestaurant2.size() == 0) {

                for (int i = 0; i < restaurantManager.getNumReports(); i++) {
                    Restaurant currentRestaurant = restaurantManager.get(i);
                    List<InspectionReport> inspections = new ArrayList<>();
                    int criticalViolations = 0;


                    for (int j = 0; j < inspectionReportManager.getNumReports(); j++) {
                        if (inspectionReportManager.get(j).getTrackingNumber().equals(currentRestaurant.getTrackingNumber())) {
                            inspections.add(inspectionReportManager.get(j));
                        }
                    }

                    for (int k = 0; k < inspections.size(); k++) {
                        String inspecDate = inspections.get(k).getInspectionDate();
                        String date = inspecDate.substring(0, 4) + "-" + inspecDate.substring(4, 6) + "-" + inspecDate.substring(6);
                        Date dateInspection = new SimpleDateFormat("yyyy-MM-dd").parse(date);
                        if (dateInspection.after(time2) && dateInspection.before(time1)) {
                            criticalViolations = criticalViolations + inspections.get(k).getNumCritical();
                        }
                    }

                    if (criticalViolations >= numberOfViolationsMoreThan) {
                        listRestaurant3.add(currentRestaurant);
                    }


                }
            }

            else if(listRestaurant1.size() != 0 && listRestaurant2.size() == 0){
                for (int i = 0; i < listRestaurant1.size(); i++) {
                    Restaurant currentRestaurant = listRestaurant1.get(i);
                    List<InspectionReport> inspections = new ArrayList<>();
                    int criticalViolations = 0;


                    for (int j = 0; j < inspectionReportManager.getNumReports(); j++) {
                        if (inspectionReportManager.get(j).getTrackingNumber().equals(currentRestaurant.getTrackingNumber())) {
                            inspections.add(inspectionReportManager.get(j));
                        }
                    }

                    for (int k = 0; k < inspections.size(); k++) {
                        String inspecDate = inspections.get(k).getInspectionDate();
                        String date = inspecDate.substring(0, 4) + "-" + inspecDate.substring(4, 6) + "-" + inspecDate.substring(6);
                        Date dateInspection = new SimpleDateFormat("yyyy-MM-dd").parse(date);
                        if (dateInspection.after(time2) && dateInspection.before(time1)) {
                            criticalViolations = criticalViolations + inspections.get(k).getNumCritical();
                        }
                    }

                    if (criticalViolations >= numberOfViolationsMoreThan) {
                        listRestaurant3.add(currentRestaurant);
                    }
                }
            }

            else{
                for (int i = 0; i < listRestaurant2.size(); i++) {
                    Restaurant currentRestaurant = listRestaurant2.get(i);
                    List<InspectionReport> inspections = new ArrayList<>();
                    int criticalViolations = 0;


                    for (int j = 0; j < inspectionReportManager.getNumReports(); j++) {
                        if (inspectionReportManager.get(j).getTrackingNumber().equals(currentRestaurant.getTrackingNumber())) {
                            inspections.add(inspectionReportManager.get(j));
                        }
                    }

                    for (int k = 0; k < inspections.size(); k++) {
                        String inspecDate = inspections.get(k).getInspectionDate();
                        String date = inspecDate.substring(0, 4) + "-" + inspecDate.substring(4, 6) + "-" + inspecDate.substring(6);
                        Date dateInspection = new SimpleDateFormat("yyyy-MM-dd").parse(date);
                        if (dateInspection.after(time2) && dateInspection.before(time1)) {
                            criticalViolations = criticalViolations + inspections.get(k).getNumCritical();
                        }
                    }

                    if (criticalViolations >= numberOfViolationsMoreThan) {
                        listRestaurant3.add(currentRestaurant);
                    }
                }
            }
        }

        if(isFavourite){
            if(listRestaurant1.size() == 0 && listRestaurant2.size() == 0 && listRestaurant3.size() == 0) {
                Set<String> favorites = restaurantManager.getFavRestaurantTrackingNum();
                for (int i = 0; i < restaurantManager.getNumReports(); i++) {
                    Restaurant currentRestaurant = restaurantManager.get(i);

                    for (String temp : favorites) {
                        if(temp.equals(currentRestaurant.getTrackingNumber())){
                            listRestaurant4.add(currentRestaurant);
                        }
                    }

                }
            }
            else if(nameUsed && !hazardRatingUsed && !violationsUsed) {
                Set<String> favorites = restaurantManager.getFavRestaurantTrackingNum();
                for (int i = 0; i < listRestaurant1.size(); i++) {
                    Restaurant currentRestaurant = listRestaurant1.get(i);

                    for (String temp : favorites) {
                        if(temp.equals(currentRestaurant.getTrackingNumber())){
                            listRestaurant4.add(currentRestaurant);
                        }
                    }

                }
            }
            else if( hazardRatingUsed && !violationsUsed) {

                Set<String> favorites = restaurantManager.getFavRestaurantTrackingNum();
                for (int i = 0; i < listRestaurant2.size(); i++) {
                    Restaurant currentRestaurant = listRestaurant2.get(i);

                    for (String temp : favorites) {
                        if(temp.equals(currentRestaurant.getTrackingNumber())){
                            listRestaurant4.add(currentRestaurant);
                        }
                    }

                }
            }
            else if(violationsUsed) {
                Set<String> favorites = restaurantManager.getFavRestaurantTrackingNum();
                for (int i = 0; i < listRestaurant3.size(); i++) {
                    Restaurant currentRestaurant = listRestaurant3.get(i);

                    for (String temp : favorites) {
                        if(temp.equals(currentRestaurant.getTrackingNumber())){
                            listRestaurant4.add(currentRestaurant);
                        }
                    }

                }
            }

        }





        if(nameUsed && !hazardRatingUsed && !violationsUsed && !isFavourite){
            return listRestaurant1;
        }
        else if( hazardRatingUsed && !violationsUsed  && !isFavourite){
            return listRestaurant2;
        }
        else if (violationsUsed && !isFavourite){
            return listRestaurant3;
        }
        else{
            return listRestaurant4;
        }



    }
}