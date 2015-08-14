package android.sebluy.gpstracker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Remote {

    private StatusListener mListener;

    public Remote(StatusListener listener) {
        mListener = listener;
    }

    public void postAPI(String body) {
        ConnectivityManager c = (ConnectivityManager)Application.getContext().
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo n = c.getActiveNetworkInfo();
        if (n != null && n.isConnected()) {
            updateStatus("Sending");
            new SendPathTask().execute(body);
        } else {
            updateStatus("Network connection unavailable");
        }
    }

    private void updateStatus(String status) {
        mListener.onStatusChanged("Remote: " + status);
    }

    private class SendPathTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String status;
            try {
                status = HTTPPost(params[0]);
            } catch (Exception e) {
                status = "Error (" + e.getMessage() + ")";
            }
            return status;
        }

        @Override
        protected void onPostExecute(String status) {
            updateStatus(status);
        }

        private String HTTPPost(String body) throws Exception {
            URL url = null;
            HttpURLConnection c = null;
            OutputStream os = null;

            byte[] bytes = body.getBytes();
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
