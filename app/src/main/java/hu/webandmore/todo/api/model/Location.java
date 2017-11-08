package hu.webandmore.todo.api.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Location {

    private double longitude;
    private double latitude;

    Location(){}

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
