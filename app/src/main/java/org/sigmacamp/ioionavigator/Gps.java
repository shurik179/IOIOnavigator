package org.sigmacamp.ioionavigator;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;


/***********  Create class and implements with LocationListener **************/
public  class Gps implements LocationListener {
    Context parent;
    private LocationManager locationManager;
    private  Location location;
    //constructor function - needs context from the parent
    public Gps(Context mContext){
        this.parent = mContext;
        this.locationManager=(LocationManager) parent.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER,
                1000,   // 1 sec
                0, // do not use minimal distance - only minimal time
                this);

    }


    /************* Called after each 1 sec **********/
    @Override
    public void onLocationChanged(Location newLocation) {
        this.location=newLocation;
    }

    @Override
    public void onProviderDisabled(String provider) {

        /******** Called when User off Gps *********/
        location=null;
    }

    @Override
    public void onProviderEnabled(String provider) {

        /******** Called when User on Gps  *********/

    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    /*************** */
    public  double getLongitude(){
        if (location!=null) {
            return location.getLongitude();
        } else {
            return (0.0);
        }
    }
    public  double getLatitude(){
        if (location!=null) {
            return location.getLatitude();
        } else {
            return (0.0);
        }
    }
    public  Location getLocation(){
        return location;
    }
    public float distanceTo(Location l){
        return location.distanceTo(l);
    }
    public float bearingTo(Location l) {
        return location.bearingTo(l);
    }
    public boolean hasLocation() {
        return (location != null);
    }
    public void pause (){
        locationManager.removeUpdates(this);
        location=null;
    }
    public void resume(){
        locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER,
                1000,   // request updates every 1 sec
                0, // do not use minimal distance - only minimal time
                this);

    }

}