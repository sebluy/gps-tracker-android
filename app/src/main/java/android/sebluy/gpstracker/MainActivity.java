package android.sebluy.gpstracker;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.LocationListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends Activity implements GPSListener {

    private TextView mStatusView;
    private TextView mLatitudeView;
    private TextView mLongitudeView;
    private TextView mTimeView;
    private TextView mPointCountView;

    private GPS mGPS;

    private void handleSendClick() {
        ConnectivityManager c = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo n = c.getActiveNetworkInfo();
        if (n != null && n.isConnected()) {
//            mStatus = "Sending";
            new SendPathTask().execute();
        } else {
//            mStatus = "Network connection unavailable";
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLatitudeView.setText(String.valueOf(location.getLatitude()));
        mLongitudeView.setText(String.valueOf(location.getLongitude()));
        mTimeView.setText(DateFormat.getTimeInstance().format(new Date()));
        mPointCountView.setText(String.valueOf(mGPS.getPoints().size()));
    }

    @Override
    public void onStatusChanged(String status) {
        mStatusView.setText(status);
    }

    private class SendPathTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                sendPath();
            } catch (Exception e) {
//                mStatus = "Error" + e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
//            mStatusView.setText(mStatus);
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
            for (int i = 0 ; i < mGPS.getPoints().size() ; i++) {
                path.put(locationToJSON(mGPS.getPoints().get(i)));
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
//                    mStatus = "Success" ;
                } else {
//                    mStatus = "Failure" + response ;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        mStatusView = (TextView)findViewById(R.id.status_view);
        mLatitudeView = (TextView)findViewById(R.id.latitude);
        mLongitudeView = (TextView)findViewById(R.id.longitude);
        mTimeView = (TextView)findViewById(R.id.time);
        mPointCountView = (TextView)findViewById(R.id.point_count);

        mGPS = new GPS(getApplicationContext(), this);

        Button sendButton = (Button)findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mGPS.stop();
                handleSendClick();
            }
        });
        Button startTrackingButton = (Button)findViewById(R.id.start_tracking_button);
        startTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGPS.start();
            }
        });
        Button stopTrackingButton = (Button)findViewById(R.id.stop_tracking_button);
        stopTrackingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGPS.stop();
            }
        });
        Button clearPathButton = (Button)findViewById(R.id.clear_path_button);
        clearPathButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGPS.clear();
            }
        });
    }

    @Override
    public void onStop() {
        mGPS.stop();
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
