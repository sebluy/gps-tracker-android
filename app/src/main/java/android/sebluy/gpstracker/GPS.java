package android.sebluy.gpstracker;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public class GPS
        implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private String mStatus;
    private ArrayList<Location> mPoints;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private GPSListener mListener;
    private Context mContext;

    public GPS(Context context, GPSListener listener) {
        mContext = context;
        mListener = listener;
        mPoints = new ArrayList<>();
        mGoogleApiClient = makeGoogleApiClient();
        mLocationRequest = makeLocationRequest();
        updateStatus("Idling");
    }

    public boolean isTracking() {
        return mGoogleApiClient.isConnected();
    }

    public void start() {
        if (!mGoogleApiClient.isConnected()) {
            updateStatus("Connecting");
            mGoogleApiClient.connect();
        }
    }

    public void stop() {
        if (mGoogleApiClient.isConnected()) {
            updateStatus("Disconnecting");
            mGoogleApiClient.disconnect();
        }
    }

    public void clear() {
        mPoints.clear();
    }

    public ArrayList<Location> getPoints() {
        return mPoints;
    }

    private void updateStatus(String status) {
        mListener.onStatusChanged("GPS: " + status);
    }

    @Override
    public void onConnected(Bundle bundle) {
        updateStatus("Tracking");
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        updateStatus("Idling");
        stopLocationUpdates();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        updateStatus("Google API Connection Failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        mPoints.add(location);
        mListener.onLocationChanged(location);
    }

    private GoogleApiClient makeGoogleApiClient() {
        return new GoogleApiClient.Builder(mContext).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).
                addApi(LocationServices.API).
                build();
    }

    private LocationRequest makeLocationRequest() {
        return new LocationRequest().
                setInterval(10000).
                setFastestInterval(5000).
                setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.
                requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

}
