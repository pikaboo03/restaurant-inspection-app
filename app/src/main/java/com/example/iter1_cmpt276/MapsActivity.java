package com.example.iter1_cmpt276;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.iter1_cmpt276.model.ClusterMarker;

import com.example.iter1_cmpt276.model.FilterData;
import com.example.iter1_cmpt276.model.InspectionReport;
import com.example.iter1_cmpt276.model.InspectionReportManager;
import com.example.iter1_cmpt276.model.MyClusterManagerRenderer;
import com.example.iter1_cmpt276.model.Restaurant;
import com.example.iter1_cmpt276.model.RestaurantManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Integer.parseInt;

/*
Activity is used to display all the restaurants with their corresponding hazard levels on google maps.
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "MapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15;
    private static final float SURREY_ZOOM = 10;

    private RestaurantManager restaurantManager;
    private InspectionReportManager inspectionManager;

    //widgets
    private EditText mSearchText;
    private ImageView mGps;
    private ImageView mZoomIn;
    private ImageView mZoomOut;
    private ImageView mAdvancedOptions;

    //vars
    private Boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private ClusterManager mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();

    private boolean foundMultipleFlag;

    private ArrayList<Restaurant> copyRestaurant;
    private List<InspectionReport> copyInspection;

    private ArrayList<ClusterMarker> mTripMarkers = new ArrayList<>();

    private FilterData filterData;

    private boolean usedAdvancedSearched;

    private ClusterManager copyOrginalClusterManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mSearchText = (EditText) findViewById(R.id.as_name);
        mGps = (ImageView) findViewById(R.id.ic_gps);
        mZoomIn = (ImageView) findViewById(R.id.ic_zoomIn);
        mZoomOut = (ImageView) findViewById(R.id.ic_zoomOut);
        mAdvancedOptions = (ImageView)findViewById(R.id.advancedOptions);

        this.restaurantManager = RestaurantManager.getInstance(this);
        this.inspectionManager = InspectionReportManager.getInstance(this);
        this.filterData =FilterData.getInstance();

        getLocationPermission();

        Button listBtn = (Button) findViewById(R.id.listButton);
        Button resetBtn = (Button) findViewById(R.id.resetButton);
        listBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent maps = MainActivity.makeIntent(MapsActivity.this);
                finish();
                startActivity(maps);
            }
        });
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mClusterManager.clearItems();
                for(int i = 0; i < mClusterMarkers.size(); i++){
                    mClusterManager.addItem(mClusterMarkers.get(i));
                }
                mClusterManager.cluster();


            }


        });
    }

    private void init() {
        Log.d(TAG, "init: initalzing");

        //override enter key
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {
                    //execute our method for searching
                    String searchString = mSearchText.getText().toString();
                    ArrayList<Restaurant> foundinSearch = new ArrayList<>();
                    for (int i = 0; i < restaurantManager.getNumReports(); i++) {
                        if (restaurantManager.get(i).getName().toLowerCase().contains(searchString.toLowerCase())) {
                            foundinSearch.add(restaurantManager.get(i));
                        }
                    }
                    if (foundinSearch.size() == 0) {
                        geoLocate();
                    } else { //if we search pizza for example we get 145 markers
                        geoLocate(foundinSearch);
                    }


                }
                return false;
            }
        });
        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeviceLocation();
            }
        });
        mZoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });
        mZoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });
        mAdvancedOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showDialog();
            }
        });

        hideSoftKeyboard();
    }

    public void showDialog(){
        final Dialog dialog = new Dialog(MapsActivity.this);
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
                geoLocate(filteredRestaurants);
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

    private void geoLocate() {
        Log.d(TAG, "geoLocated: geolocating");
        String searchString = mSearchText.getText().toString();


        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException " + e.getMessage());
        }
        if (list.size() > 0) {
            Address address = list.get(0);
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));
        }

    }
    private void geoLocate(ArrayList<Restaurant> searchList) {

        String searchString = "surrey bc";
        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException " + e.getMessage());
        }
        if (list.size() > 0) {
            Address address = list.get(0);
            foundMultipleFlag = true;
            showOnlySearchedIcons(searchList);
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), SURREY_ZOOM, address.getAddressLine(0));
        }

        // foundMultipleFlag = false;

    }

    private void showOnlySearchedIcons(ArrayList<Restaurant> list) {
        List<InspectionReport> inspectionReportList = new ArrayList<>();

        ArrayList<Integer> ints = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < restaurantManager.getNumReports(); j++) {
                if ((list.get(i).getName().equals(mClusterMarkers.get(j).getRestaurant().getName()))) {
                    ints.add(j);
                }
            }
        }
        for(int i = 0; i < restaurantManager.getNumReports();i++){
            boolean check = false;
            for(int j =0 ; j< ints.size(); j++){
                if(i == ints.get(j)){
                    check = true;
                    break;
                }
            }
            if(!check){
                mClusterManager.removeItem(mClusterMarkers.get(i));
            }

        }
        mClusterManager.cluster();
    }




    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        if (mLocationPermissionGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);

            init();
            copyRestaurant = new ArrayList<>();
            for(int i = 0; i < restaurantManager.getNumReports();i++){
                copyRestaurant.add(restaurantManager.get(i));

            }
            copyInspection = inspectionManager.getInspections();
            addMapMarkers(copyRestaurant,copyInspection,false);
        }
    }


    private void getDeviceLocation() {
        Log.d(TAG, "getLocationPermission: getting location permissions");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted) {
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location!");
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM, "My Location");
                        } else {
                            Log.d(TAG, "onCOmplete: current location is null");
                            Toast.makeText(MapsActivity.this, R.string.unable, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {

        }
    }

    private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
    }

    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if (!(title.equals("My Location"))) {
            MarkerOptions options = new MarkerOptions().position(latLng).title(title);
            if(!foundMultipleFlag) {
                mMap.addMarker(options);
            }
            foundMultipleFlag = false;
        }
        hideSoftKeyboard();

    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void addMapMarkers(final ArrayList<Restaurant> restaurants, List<InspectionReport> searchInspections, boolean makeNull) {
        if (mMap != null) {
            if(makeNull){
                mClusterManager = null;
                mClusterManagerRenderer = null;
                mClusterMarkers = new ArrayList<>();
            }
            if (mClusterManager == null) {
                mClusterManager = new ClusterManager<ClusterMarker>(this.getApplicationContext(), mMap);

                mMap.setOnCameraIdleListener(mClusterManager);

                mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener() {
                    @Override
                    public boolean onClusterItemClick(ClusterItem item) {

                        //add alert dialogue here
                        Restaurant clickedRestaurant = ((ClusterMarker)item).getRestaurant();
                        int iconID = clickedRestaurant.getIconID(clickedRestaurant.getName());
                        final String trackingNumber = clickedRestaurant.getTrackingNumber();
                        int position = 0;
                        for(int i = 0; i < restaurants.size();i++){
                            if(clickedRestaurant.getAddress().equals(restaurants.get(i).getAddress())){
                                position = i;
                                break;
                            }
                        }

                        String hazardRating = "no";
                        List<InspectionReport> inspections = inspectionManager
                                .getInspectionByRestaurant(clickedRestaurant.getTrackingNumber());
                        if (inspections.size() != 0) {
                            InspectionReport latest = inspectionManager
                                    .getLatestByTrackingID(clickedRestaurant.getTrackingNumber());
                            hazardRating = latest.getHazardRating();
                        }


                        final int finalPosition = position;
                        new AlertDialog.Builder(MapsActivity.this)
                                .setTitle(clickedRestaurant.getName())
                                .setMessage("" + clickedRestaurant.getAddress() + ", " + clickedRestaurant.getCity() + ".\n" +
                                       getString(R.string.hazard_description) + " " + hazardRating)

                                .setPositiveButton(R.string.see_info, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = SingleRestaurantActivity
                                                .makeLaunchIntent(MapsActivity.this, finalPosition, trackingNumber);
                                        startActivity(intent);
                                    }
                                })


                                // A null listener allows the button to dismiss the dialog and take no further action.
                                .setNegativeButton(android.R.string.no, null)
                                .setIcon(iconID)
                                .show();
                        return false;
                    }
                });
            }
            if (mClusterManagerRenderer == null) {
                mClusterManagerRenderer = new MyClusterManagerRenderer(
                        this, mMap, mClusterManager
                );

                mClusterManager.setRenderer(mClusterManagerRenderer);
            }
            for (int i = 0; i < restaurants.size(); i++) {
                double longitude = restaurants.get(i).getLongitude();
                double latitude = restaurants.get(i).getLatitude();
                LatLng latLng = new LatLng(latitude, longitude);
                String name = restaurants.get(i).getName();
                String location = restaurants.get(i).getAddress();
                //need to change this to default for the no name restaurant
                int avatar = R.drawable.noinspections;
                Restaurant currentRestaurant = restaurants.get(i);
                List<InspectionReport> inspections = new ArrayList<>();
                InspectionReport latest;

                String hazardRating;
                if(!makeNull) {
                    inspections = inspectionManager
                            .getInspectionByRestaurant(currentRestaurant.getTrackingNumber());
                }
                else{
                    for (int j = 0; j < searchInspections.size(); j++) {
                        if (searchInspections.get(j).getTrackingNumber().equals(currentRestaurant.getTrackingNumber())) {
                            inspections.add(searchInspections.get(j));
                        }
                    }
                }
                if(!makeNull) {
                    if (inspections.size() != 0) {
                        latest = inspectionManager
                                .getLatestByTrackingID(currentRestaurant.getTrackingNumber());
                        hazardRating = latest.getHazardRating();
                        avatar = latest.getHazardIconID(hazardRating);
                    }
                }
                else{
                    if (inspections.size() != 0) {
                        latest = inspections.get(0);
                        for (int j = 1; j < inspections.size(); j++) {
                            if (searchInspections.get(j).getLastInspection().compareTo(latest.getLastInspection()) > 0) {
                                latest = inspections.get(j);
                            }
                        }
                        hazardRating = latest.getHazardRating();
                        avatar = latest.getHazardIconID(hazardRating);
                    }
                }

                try {
                    ClusterMarker newClusterMarker = new ClusterMarker(
                            latLng, location, name, avatar,restaurants.get(i)
                    );
                    mClusterManager.addItem(newClusterMarker);
                    mClusterMarkers.add(newClusterMarker);
                } catch (NullPointerException e) {
                    Log.e(TAG, "addMapMarkers: NullPointerException: " + e.getMessage());
                }
            }
            copyOrginalClusterManager = mClusterManager;

            mClusterManager.cluster();

        }
    }
}