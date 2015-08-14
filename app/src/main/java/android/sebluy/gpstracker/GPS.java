package android.sebluy.gpstracker;

import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class GPS
        implements ConnectionCallbacks, OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private GPSListener mListener;

    public GPS(GPSListener listener) {
        mListener = listener;
        mGoogleApiClient = makeGoogleApiClient();
        mLocationRequest = makeLocationRequest();
        updateStatus("Idling");
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

    private GoogleApiClient makeGoogleApiClient() {
        return new GoogleApiClient.Builder(Application.getContext()).
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
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mListener);
    }

    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.
                requestLocationUpdates(mGoogleApiClient, mLocationRequest, mListener);
    }

}
