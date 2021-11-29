package com.example.iter1_cmpt276.model;

import android.content.Context;
import android.util.Log;

import com.example.iter1_cmpt276.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/*
 * This class manages handling of restaurant objects in the anodroid activity
 * by static instance method.
 */
public class RestaurantManager implements Iterable<Restaurant> {

    private LinkedList<Restaurant> restaurants;
    private static RestaurantManager instance;
    private int count;
    private Context context;
    private HashMap<Integer, String> vioType;
    Restaurant selectedRestaurant;

    private ArrayList<Restaurant> restaurantManager = new ArrayList<>();
    private Set<String> favRestaurantTrackingNum;


    public Set<String> getFavRestaurantTrackingNum(){
        return favRestaurantTrackingNum;
    }


    private RestaurantManager(Context context) {
        restaurants = new LinkedList<Restaurant>();
        favRestaurantTrackingNum = new HashSet<>();
        count = 0;
        this.context = context;
    }

    public RestaurantManager(){
        //ensure singleton
    }

    private static void fillRestaurant(RestaurantManager instance, Restaurant restaurant) {
        instance.restaurants.addLast(restaurant);
        Collections.sort(instance.restaurants, rComp);
    }

    public void add(Restaurant restaurant) {
        restaurants.add(restaurant);
    }
    public void remove(Restaurant restaurant) {
        restaurants.remove(restaurant);
    }
    public Restaurant get(int index) {
        return restaurants.get(index);
    }

    public Restaurant getRestaurantByTrackingNum(String trackingNum){
        Restaurant selectedRestaurant = null;
        for (Restaurant restaurant : restaurants){
            if (trackingNum.equals(restaurant.getTrackingNumber())){
                this.selectedRestaurant = restaurant;
            }
        }
        return this.selectedRestaurant;
    }

    public void readFavData() {
        // taken help from https://www.codota.com/web/assistant/code/rs/5c7d2d35ac38dc0001e44da9#L67
        File locFile;
        locFile = new File(context.getFilesDir(), "favourites_itr1.csv");
        if (!locFile.exists()) {
           Log.d("MyActivity", "Error Occurred");
            try {
                context.openFileOutput("favourites_itr1.csv", Context.MODE_PRIVATE);
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String line = " ";

        try {

            InputStream inputStream = context.openFileInput("favourites_itr1.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            line = reader.readLine();

            while (line != null) {
                Log.d("MyActivity", "line" + line);
                favRestaurantTrackingNum.add(line);
                line = reader.readLine();
            }
            Log.d("MyActivity", "just created" + favRestaurantTrackingNum);

            reader.close();
            System.out.println();
        } catch (IOException e) {
            Log.wtf("MyActivity", "Error reading data file on line " + line , e);
            e.printStackTrace();
        }
    }
    public static RestaurantManager getInstance(Context c) {
        if (instance == null) {
            instance = new RestaurantManager(c);
        }
        return instance;
    }
    public static RestaurantManager getInstance() {
        if(instance == null) {
            instance = new RestaurantManager();
        }
        return instance;
    }

    public void updateFavRestData(Restaurant restaurant, boolean favourite) {
        String trackingNum = restaurant.getTrackingNumber();
        if (!favourite) {
            favRestaurantTrackingNum.remove(trackingNum);
        }
        else {
            favRestaurantTrackingNum.add(trackingNum);
        }


        FileOutputStream output;
        //taken help from https://www.programiz.com/java-programming/bufferedwriter
        BufferedWriter writer;
        try {
            output = context.openFileOutput("favourites_itr1.csv", Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(output));
            for (String trackingNumber : favRestaurantTrackingNum) {
                writer.write(trackingNumber);
                writer.newLine();
            }
            Log.d("MyActivity", "just created" + favRestaurantTrackingNum);


            writer.close();
            System.out.println();

        } catch (IOException e) {
            Log.wtf("MyActivity", "Error reading data file on line " , e);
            e.printStackTrace();
        }
    }

    @Override
    public Iterator<Restaurant> iterator() {
        return restaurants.iterator();
    }

    public int getNumReports() {
        return restaurants.size();
    }

    public LinkedList<Restaurant> getList() {
        return (LinkedList<Restaurant>)restaurants;
    }

    public int getCount() {
        return count;
    }

    public static Comparator<Restaurant> rComp= new Comparator<Restaurant>() {
        @Override
        public int compare(Restaurant o1, Restaurant o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };


    public boolean isFavourite(String trackingNumber) {
        return favRestaurantTrackingNum.contains(trackingNumber);
    }

}
