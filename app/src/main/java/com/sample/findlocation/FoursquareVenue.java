package com.sample.findlocation;

public class FoursquareVenue {

    private String name;
    private String city;
    private String category;
    private double lat;
    private double lng;

    public FoursquareVenue() {
        this.name = "";
        this.city = "";
        this.category = "";
        this.lat = 0.0;
        this.lng = 0.0;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
