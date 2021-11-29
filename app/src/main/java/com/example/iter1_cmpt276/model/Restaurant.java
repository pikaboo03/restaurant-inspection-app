package com.example.iter1_cmpt276.model;

import com.example.iter1_cmpt276.R;

import java.util.Calendar;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;

/*
 * This class models the information about a restaurant.
 */
public class Restaurant implements Comparator<Restaurant> {
    private String trackingNumber;
    private String name;
    private String address;
    private String city;
    private String facType;
    private double latitude;
    private double longitude;
    private int inspections;
    private LinkedList<InspectionReport> inspectionReports;
    private String month[];
    private static long TIME_CON = 24*60*60*1000;
    private int iconID;
    private boolean isFavourite;


    private InspectionReport report;
    private InspectionReportManager reportManager;

    public Restaurant() {}

    public Restaurant(String trackingNumber, String restaurantName, String address, String City, double latitude, double longitude){
        this.trackingNumber = trackingNumber;
        this.name = restaurantName;
        this.address = address;
        this.city =City;
        this.latitude = latitude;
        this.longitude = longitude;
    }



    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getFacType() {
        return facType;
    }

    public void setFacType(String facType) {
        this.facType = facType;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public InspectionReport getReport() {
        return report;
    }

    public void setReport(InspectionReport report) {
        this.report = report;
    }

    public InspectionReportManager getReportManager() {
        return reportManager;
    }

    public void setReportManager(InspectionReportManager reportManager) {
        this.reportManager = reportManager;
    }

    public boolean getFavourite(){
        return isFavourite;
    }

    public void setFavourite(boolean favourite) {

        isFavourite = favourite;
    }

    public int getIconID (String restaurantName){
        String name = restaurantName.toLowerCase();
        if (name.contains("sushi")){
            this.iconID = R.drawable.sushi_co;
            //https://twitter.com/sushiandco
        } else if (name.contains("top in town pizza")){
            this.iconID = R.drawable.top_in_town;
            //https://www.sirved.com/restaurant/abbotsford-british_columbia-canada/top-in-town-pizza/557825/menus/3020896
        } else if (name.contains("seafood")){
            this.iconID = R.drawable.logo_lee_yuen;
            //https://www.brandrepstaging30.com/
        } else if (name.contains("burger") || name.contains("a&w")){
            this.iconID = R.drawable.a_and_w;
            //https://en.wikipedia.org/wiki/A%26W_(Canada)#/media/File:A&W_Canada_Logo.svg
        } else if (name.contains("chicken")){
            this.iconID = R.drawable.church_s_chicken;
            //https://seeklogo.com/vector-logo/30146/church-s-chicken
        }
        else if(name.contains("tim hortons")){
            this.iconID = R.drawable.tim_hortons;
            //https://www.pngwave.com/png-clip-art-jwcze
        }
        else if(name.contains("subway")){
            this.iconID = R.drawable.subway;
            //https://www.pngitem.com/middle/iohhRm_subway-14-0-0-0-icon-transparent-subway/
        }
        else if(name.contains("starbucks")){
            this.iconID = R.drawable.starbucks_logo;
            //https://1000logos.net/starbucks-logo/
        }
        else if(name.contains("d-plus pizza")){
            this.iconID = R.drawable.d_plus_pizza;
            //http://www.dhutpizza.ca/
        }
        else if(name.contains("boston pizza")){
            this.iconID = R.drawable.bp_logo;
            //https://bostonpizza.com/en/index.html
        }
        else if(name.contains("dairy queen")){
            this.iconID = R.drawable.dairy_queen_logo;
            //https://en.wikipedia.org/wiki/Dairy_Queen
        }
        else if(name.contains("kfc")){
            this.iconID = R.drawable.kfc;
            //https://www.pngwing.com/en/free-png-bcqgr
        }
        else if(name.contains("mcdonald's")){
            this.iconID = R.drawable.mcdonalds;
            //https://www.pngfuel.com/free-png/gfyjl
        }
        else if(name.contains("7-eleven")){
            this.iconID = R.drawable.seven_eleven_logo;
            //https://upload.wikimedia.org/wikipedia/commons/4/40/7-eleven_logo.svg
        }
        else if(name.contains("5 star catering")){
            this.iconID = R.drawable.logo_5_star;
            //https://fivestarcateringevents.com/
        }
        else if(name.contains("blenz coffee")){
            this.iconID = R.drawable.blenz_coffee;
            //https://www.pngfuel.com/free-png/jwzsb
        }
        else {
            this.iconID = R.drawable.restaurant_icon_general;
        }
        return iconID;
    }


    @Override
    public int compare(Restaurant o1, Restaurant o2) {
        return o1.name.compareTo(o2.name);
    }

}

