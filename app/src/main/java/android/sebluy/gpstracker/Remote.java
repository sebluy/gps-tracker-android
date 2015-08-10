package android.sebluy.gpstracker;

import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.google.android.gms.location.LocationListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Remote {

    private Context mContext;
    private StatusListener mListener;

    public Remote(Context context, StatusListener listener) {
        mContext = context;
        mListener = listener;
    }

    public void sendPath(ArrayList<Location> path) {
        ConnectivityManager c = (ConnectivityManager)mContext.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo n = c.getActiveNetworkInfo();
        if (n != null && n.isConnected()) {
            updateStatus("Sending");
            new SendPathTask().execute(path);
        } else {
            updateStatus("Network connection unavailable");
        }
    }

    private void updateStatus(String status) {
        mListener.onStatusChanged("Remote: " + status);
    }

    private class SendPathTask extends AsyncTask<ArrayList<Location>, Void, String> {

        @Override
        protected String doInBackground(ArrayList<Location>... params) {
            String status;
            try {
                status = HTTPPostPath(params[0]);
            } catch (Exception e) {
                status = "Error (" + e.getMessage() + ")";
            }
            return status;
        }

        @Override
        protected void onPostExecute(String status) {
            updateStatus(status);
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

        private String HTTPPostPath(ArrayList<Location> path) throws Exception {
            URL url = null;
            HttpURLConnection c = null;
            OutputStream os = null;

            JSONArray JSONPath = new JSONArray();
            for (int i = 0 ; i < path.size() ; i++) {
                JSONPath.put(locationToJSON(path.get(i)));
            }

            JSONArray apiCall = makeJSONAPICall("add-path", JSONPath);
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
                    return "Success";
                } else {
                    return "Failure";
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
}
