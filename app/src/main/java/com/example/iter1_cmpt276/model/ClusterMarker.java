package com.example.iter1_cmpt276.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

// Used for custom markers
public class ClusterMarker implements ClusterItem {

    private LatLng position;
    private String title;
    private String snippet;
    private int iconPicture;
    private Restaurant restaurant;

    public ClusterMarker(LatLng position, String title, String snippet, int iconPicture, Restaurant restaurant) {
        this.position = position;
        this.title = title;
        this.snippet = snippet;
        this.iconPicture = iconPicture;
        this.restaurant = restaurant;
    }

    public ClusterMarker(){}

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return snippet;
    }

    public int getIconPicture() {
        return iconPicture;
    }

    public Restaurant getRestaurant(){return restaurant;}
}

