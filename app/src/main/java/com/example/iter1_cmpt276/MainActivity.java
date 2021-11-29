package com.example.iter1_cmpt276;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.iter1_cmpt276.model.FilterData;
import com.example.iter1_cmpt276.model.InspectionReport;
import com.example.iter1_cmpt276.model.InspectionReportManager;
import com.example.iter1_cmpt276.model.Restaurant;
import com.example.iter1_cmpt276.model.RestaurantManager;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/*
  This class displays list of restaurants in Surrey, BC, Canada,
  and their most recent inspection result with its hazard rating and number of issues found.
  If user clicks on a restaurant, it launches a new activity by sending key of a restaurant,
  so a detailed information of the restaurant and inspection history is available in the next screen.
 */
public class MainActivity extends AppCompatActivity {
    FilterData filterData;
    SharedPreferences sharedPreferences;
    RestaurantManager restaurantManager;
    InspectionReportManager inspectionManager;

    List<Restaurant> restaurants = new ArrayList<>();
    List<Restaurant> favRestaurants = new ArrayList<>();
    ArrayList<String> removing = new ArrayList<>();
    ArrayList<String> adding = new ArrayList<>();
    ArrayList<Restaurant> originalList = new ArrayList<>();

    private final String HIGH = "High";
    private final String MODERATE = "Moderate";
    private final String LOW = "Low";

    private TextView dateTimeDisplay;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private String date = "";
    private int size = 0;

    private ImageView mAdvancedOptions;
    public static ArrayAdapter<Restaurant> adapter;
    public static ArrayAdapter<Restaurant> tempAdapter;

    public static Intent makeIntent(Context inputContext) {

        return new Intent(inputContext, MainActivity.class);

    }
   @Override
    public void onResume() {
        ArrayAdapter<Restaurant> adapter= new MyAdapter();
        ListView list= findViewById(R.id.list);
        list.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        super.onResume();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeClassDataMembers();

        if(restaurantManager.getNumReports() == 0) {
            readReportsData();
            inspectionManager.readData();
        }

        populateListAndSearch();
        advancedOptions();
        checkForFavUpdatedRestaurants();
        registerOnClick();

        // Create array list of original restaurant list
        for (int i = 0; i < restaurantManager.getNumReports(); i++) {
            originalList.add(restaurantManager.get(i));
        }

        // Reset button to repopulate list with original restaurant list
        Button resetBtn = (Button) findViewById(R.id.reset);
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListView list = findViewById(R.id.list);
                list.setAdapter(null);
                adapter.clear();
                adapter.addAll(originalList);
                list.setAdapter(adapter);
            }
        });


        // Map button to navigate back to the map activity
        Button mapBtn = (Button) findViewById(R.id.backtoMap);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.clear();
                adapter.addAll(originalList);

                Intent i = new Intent(MainActivity.this, MapsActivity.class);
                finish();
                startActivity(i);
            }
        });
    }

    private void initializeClassDataMembers() {
        this.restaurantManager = RestaurantManager.getInstance(this);
        this.inspectionManager = InspectionReportManager.getInstance(this);
        this.filterData = FilterData.getInstance();
    }

    private void populateListAndSearch() {
        final Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                // do nothing
            }
        }, 1000);

        adapter = new MyAdapter();
        ListView list = findViewById(R.id.list);
        list.setAdapter(adapter);

        // Creates search bar for restaurant list
        SearchView searchView = (SearchView) findViewById(R.id.inputSearch);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String text) {
                adapter.getFilter().filter(text);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String text) {
                adapter.getFilter().filter(text);
                return true;
            }
        });
    }

    // Creates advanced options button
    private void advancedOptions()
    {
        mAdvancedOptions = (ImageView)findViewById(R.id.advancedOptions);
        mAdvancedOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
    }

    private void populateFavUpdatedRestaurants() {
        final Button okButton = findViewById(R.id.main_ok);
        okButton.setVisibility(View.VISIBLE);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){
                okButton.setVisibility(View.INVISIBLE);

                Intent i = new Intent(MainActivity.this, MapsActivity.class);
                finish();
                startActivity(i);
            }
        });
        TextView textView = findViewById(R.id.main_text);
        textView.setText(getString(R.string.update_fav_text));
        restaurants = favRestaurants;
        ArrayAdapter<Restaurant> adapter = new MyAdapter();
        ListView restaurantList = findViewById(R.id.list);
        restaurantList.setAdapter(adapter);

    }

    private class MyAdapter extends ArrayAdapter<Restaurant> {
        MyAdapter() {
            super(MainActivity.this,
                    R.layout.individual_restaurant, restaurantManager.getList());
        }

        private final List<Restaurant> mList = new ArrayList<>(restaurantManager.getList());

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View restaurantView= convertView;
            System.out.println(position);
            if(restaurantView == null) {
                restaurantView= getLayoutInflater()
                        .inflate(R.layout.individual_restaurant, parent, false);
            }

            Restaurant currentRestaurant = restaurantManager.get(position);
            if (restaurantManager.isFavourite(currentRestaurant.getTrackingNumber())) {
                //https://www.pinterest.ca/pin/434667801538669943/
                restaurantView.setBackgroundResource(R.drawable.light_bck);
            } else {
                restaurantView.setBackgroundColor(getColor(R.color.blank_background));
            }

            // Set restaurant name, address, and icon
            TextView restaurantName = (TextView) restaurantView.findViewById(R.id.text_rName);
            String name = currentRestaurant.getName();
            restaurantName.setText(name);
            restaurantName.setSelected(true);

            TextView restaurantAddress = (TextView) restaurantView.findViewById(R.id.text_rAddress);
            restaurantAddress.setText(currentRestaurant.getAddress());

            ImageView restaurantIcon = (ImageView) restaurantView.findViewById(R.id.icon_restaurant);
            int iconID = currentRestaurant.getIconID(name);
            restaurantIcon.setImageResource(iconID);

            List<InspectionReport> inspections = inspectionManager
                    .getInspectionByRestaurant(currentRestaurant.getTrackingNumber());

            // set # of issues and inspection date of the recent inspection
            int numProblems= 0;
            String inspDate = "";
            String formattedDate = "";
            if (inspections.size() != 0) {
                InspectionReport latest = inspectionManager
                        .getLatestByTrackingID(currentRestaurant.getTrackingNumber());
                numProblems = latest.getNumNonCritical() + latest.getNumCritical();
                inspDate = latest.getInspectionDate();
                formattedDate = latest.getFormattedDate(inspDate);
            }

            TextView lastInspection =  (TextView) restaurantView
                    .findViewById(R.id.text_lastInspected);
            lastInspection.setText(getString(R.string.last_inspection, formattedDate));

            TextView currentRestaurantIssues = (TextView) restaurantView
                    .findViewById(R.id.text_rIssuesfound);
            currentRestaurantIssues.setText(getString(R.string.issued_found, numProblems));

            // change hazard level image by getting iconID
            ImageView restaurantHazard= (ImageView) restaurantView.findViewById(R.id.image_rHazard);
            TextView restaurantTextHazard = (TextView) restaurantView.findViewById(R.id.haz_text);

            // Adds the text for the hazard level of each restaurant
            if (inspections.size() != 0) {
                InspectionReport latest = inspectionManager.getLatestByTrackingID(currentRestaurant.getTrackingNumber());
                String hazardRating = latest.getHazardRating();

                if(hazardRating.equals("High")){
                    restaurantTextHazard.setText(hazardRating);
                    restaurantTextHazard.setTextColor(Color.RED);
                }
                else if(hazardRating.equals("Moderate")){
                    restaurantTextHazard.setText(hazardRating);
                    restaurantTextHazard.setTextColor(getResources().getColor(R.color.yellow));
                }
                else{
                    restaurantTextHazard.setText(hazardRating);
                    restaurantTextHazard.setTextColor(Color.GREEN);
                }

                int hazardIcon = latest.getHazardIconID(hazardRating);
                restaurantHazard.setImageResource(hazardIcon);
            } else {
                restaurantHazard.setImageResource(android.R.color.transparent);
                currentRestaurantIssues.setText(getString(R.string.no_inspection));
                lastInspection.setText(getString(R.string.none));
            }
            return restaurantView;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    FilterResults result = new FilterResults();
                    String constraint = charSequence.toString().toLowerCase();

                    if (constraint == null || constraint.isEmpty()) {
                        result.values = mList;
                        result.count = mList.size();
                    } else {
                        List<Restaurant> list = new ArrayList<>();
                        int max = mList.size();
                        for (int cont = 0; cont < max; cont++) {
                            Restaurant res = mList.get(cont);
                            boolean contains = res.getName().toLowerCase().contains(constraint);
                            if (contains) {
                                list.add(mList.get(cont));
                            }
                        }
                        result.values = list;
                        result.count = list.size();
                    }
                    return result;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    clear();
                    addAll((ArrayList<Restaurant>) results.values);
                    notifyDataSetChanged();
                }
            };
        }
    }

    public void showDialog() {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.search_dialog_box);

        Button search = dialog.findViewById(R.id.as_search);
        Button cancel = dialog. findViewById(R.id.as_cancel);
        final EditText name = dialog.findViewById(R.id.as_name);
        final EditText hazardLvl = dialog.findViewById(R.id.as_hazard_level);
        final EditText criticalViolations = dialog.findViewById(R.id.as_critical_violations);
        final CheckBox checkBox = dialog.findViewById(R.id.as_favorited);
        search.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                // Reset the list
                ListView list = findViewById(R.id.list);
                list.setAdapter(null);
                list.setAdapter(adapter);


                String stringName = name.getText().toString();
                String stringhazardLvl = hazardLvl.getText().toString();
                int intCriticalViolation;
                if(criticalViolations.getText().toString().equals("")){
                    intCriticalViolation = -1;
                }
                else{
                    intCriticalViolation = Integer.parseInt(criticalViolations.getText().toString());
                }
                boolean isChecked = checkBox.isChecked();

                //do something with these string
                filterData.setRestaurantManager(restaurantManager);
                filterData.setInspectionManager(inspectionManager);
                filterData.setSearchRestaurantByName(stringName);
                filterData.setHazardLevel(stringhazardLvl);
                filterData.setNumberOfViolationsMoreThan(intCriticalViolation);
                filterData.setFavourite(isChecked);
                ArrayList<Restaurant> filteredRestaurants = null;
                try {
                    filteredRestaurants = filterData.getFilteredRestaurants();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                // Checks if there was something in the search
                if (stringName.isEmpty() && stringhazardLvl.isEmpty() && intCriticalViolation == -1 && !isChecked) {
                    list.setAdapter(null);
                    list.setAdapter(adapter);
                }
                else
                {
                    list.setAdapter(null);
                    adapter.clear();
                    adapter.addAll(filteredRestaurants);
                    list.setAdapter(adapter);
                }
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void registerOnClick()
    {
        ListView list = (ListView) findViewById(R.id.list);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                String trackingNumber = restaurantManager.get(position).getTrackingNumber();
                Intent intent = SingleRestaurantActivity
                        .makeLaunchIntent(MainActivity.this, position, trackingNumber);
                startActivity(intent);
            }
        });
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
            case R.id.back:
                finishAndRemoveTask();
                return true;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_at_main, menu);
        return true;
    }

    private void checkForFavUpdatedRestaurants() {
        restaurantManager= RestaurantManager.getInstance(this);

        SharedPreferences sharedPreferences;
        sharedPreferences = getSharedPreferences("Cmpt276_group16", Context.MODE_PRIVATE);

        Set<String> fav = new HashSet<String>(Objects.requireNonNull(sharedPreferences.getStringSet("Favourite_Restaurants", new HashSet<String>())));
        String old;
        Restaurant x;

        for (String oldFile : fav) {
            for (Restaurant favRes : restaurantManager) {
                if (favRes.getFavourite()) {
                    Gson gson_file = new Gson();
                    Restaurant current_rest = gson_file.fromJson(oldFile, Restaurant.class);
                    String newFile = new Gson().toJson(favRes);
                    if (current_rest.getTrackingNumber().equals(favRes.getTrackingNumber())) {
                        if (!oldFile.equals(newFile)) {
                            favRestaurants.add(favRes);
                            removing.add(oldFile);
                            adding.add(newFile);
                        }
                    }
                }
            }
        }

        System.out.println();
        fav.removeAll(removing);
        fav.addAll(adding);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("Favourite_Restaurants", fav);
        editor.apply();

        if (favRestaurants.isEmpty()) {
            populateListAndSearch();
        } else {
            populateFavUpdatedRestaurants();
        }
    }

    // Reads restaurants_itr1
    private void readReportsData()
    {
        InputStream is = getResources().openRawResource(R.raw.restaurants_itr1);
        BufferedReader reader = new BufferedReader (
                new InputStreamReader(is, StandardCharsets.UTF_8)
        );
        String line = "";
        try {
            reader.readLine();
            while ( (line = reader.readLine()) != null) {

                String[] tokens = line.split(",");

                Restaurant restaurant = new Restaurant();
                restaurant.setTrackingNumber(tokens[0]);
                restaurant.setName(tokens[1]);
                restaurant.setAddress(tokens[2]);
                restaurant.setCity(tokens[3]);
                restaurant.setLatitude(Double.parseDouble(tokens[5]));
                restaurant.setLongitude(Double.parseDouble(tokens[6]));

                restaurantManager.add(restaurant);
            }
        } catch (IOException e) {
            Log.wtf("MyActivity", "Error reading data file on line " + line, e);
            e.printStackTrace();
        }
    }
}