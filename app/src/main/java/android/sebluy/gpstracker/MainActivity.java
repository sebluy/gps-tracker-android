package android.sebluy.gpstracker;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity
        extends Activity
        implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private String mStatus;

    private ArrayList<Location> mPoints;

    private TextView mStatusView;
    private TextView mLatitudeView;
    private TextView mLongitudeView;
    private TextView mTimeView;
    private TextView mPointCountView;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private boolean mIsTracking = false;

    private void handleSendClick() {
        ConnectivityManager c = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo n = c.getActiveNetworkInfo();
        if (n != null && n.isConnected()) {
            mStatus = "Sending";
            updateStatusView();
            new SendPathTask().execute();
        } else {
            mStatus = "Network connection unavailable";
            updateStatusView();
        }
    }

    private void updateStatusView() {
        mStatusView.setText(mStatus);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mStatus = "Google API Connected";
        updateStatusView();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mStatus = "Google API Disconnected";
        updateStatusView();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        mStatus = "Google API Connection Failed";
        updateStatusView();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLatitudeView.setText(String.valueOf(location.getLatitude()));
        mLongitudeView.setText(String.valueOf(location.getLongitude()));
        mTimeView.setText(DateFormat.getTimeInstance().format(new Date()));
        mPoints.add(location);
        mPointCountView.setText(String.valueOf(mPoints.size()));
    }

    private class SendPathTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                sendPath();
            } catch (Exception e) {
                mStatus = "Error" + e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            mStatusView.setText(mStatus);
        }

        private JSONObject locationToJSON(Location location) throws Exception {
            JSONObject latLng = new JSONObject();
            latLng.put("latitude", location.getLatitude());
            latLng.put("longitude", location.getLongitude());
            return latLng;
        }

        private JSONArray makeJSONAPICall(String action, JSONArray args) {
            JSONArray apiCall = new JSONArray();
            apiCall.put(action);
            apiCall.put(args);
            return apiCall;
        }

        private void sendPath() throws Exception {
            URL url = null;
            HttpURLConnection c = null;
            OutputStream os = null;

            JSONArray path = new JSONArray();
            for (int i = 0 ; i < mPoints.size() ; i++) {
                path.put(locationToJSON(mPoints.get(i)));
            }

            JSONArray apiCall = makeJSONAPICall("add-path", path);
            JSONArray apiCallList = new JSONArray();
            apiCallList.put(apiCall);

            byte[] bytes = apiCallList.toString().getBytes();
            try {
                url = new URL("https://fierce-dawn-3931.herokuapp.com/api");
                c = (HttpURLConnection) url.openConnection();

                c.setChunkedStreamingMode(bytes.length);
                c.setDoOutput(true);
                c.setRequestProperty("Content-Type", "application/json");
                c.setRequestProperty("charset", "utf-8");
                c.setRequestProperty("Content-Length", Integer.toString(bytes.length));

                os = new BufferedOutputStream(c.getOutputStream());
                os.write(bytes);
                os.flush();

                int response = c.getResponseCode();
                if (response == 200) {
                    mStatus = "Success" ;
                } else {
                    mStatus = "Failure" + response ;
                }
            } finally {
                if (os != null) {
                    os.close();
                }
                if (c != null) {
                    c.disconnect();
                }
            }
        }
    }

    private void buildGoogleApiClient() {
        mStatus = "Connecting";
        updateStatusView();
        mGoogleApiClient = new GoogleApiClient.Builder(this).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).
                addApi(LocationServices.API).
                build();
        createLocationRequest();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStatusView = (TextView)findViewById(R.id.status_view);
        Button sendButton = (Button)findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (mIsTracking) {
                    stopLocationUpdates();
                    mIsTracking = false;
                }
                handleSendClick();
            }
        });
        Button startTrackingButton = (Button)findViewById(R.id.start_tracking_button);
        startTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mIsTracking && mGoogleApiClient.isConnected()) {
                    startLocationUpdates();
                    mIsTracking = true;
                }
            }
        });
        Button stopTrackingButton = (Button)findViewById(R.id.stop_tracking_button);
        stopTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsTracking) {
                    stopLocationUpdates();
                    mIsTracking = false;
                }
            }
        });
        mLatitudeView = (TextView)findViewById(R.id.latitude);
        mLongitudeView = (TextView)findViewById(R.id.longitude);
        mTimeView = (TextView)findViewById(R.id.time);
        mPointCountView = (TextView)findViewById(R.id.point_count);
        buildGoogleApiClient();
    }

    private void stopLocationUpdates() {
        mStatus = "Tracking stopped";
        updateStatusView();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    private void startLocationUpdates() {
        mStatus = "Tracking";
        mPoints = new ArrayList<>();
        updateStatusView();
        LocationServices.FusedLocationApi.
                requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        stopLocationUpdates();
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
