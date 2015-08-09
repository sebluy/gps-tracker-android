package android.sebluy.gpstracker;

import android.content.Context;
import android.content.DialogInterface;
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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends Activity {

    private String mStatus;
    private TextView mStatusView;

    private void handleSendClick() {
        ConnectivityManager c = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo n = c.getActiveNetworkInfo();
        if (n != null && n.isConnected()) {
            mStatusView.setText("Sending");
            new SendPathTask().execute();
        } else {
            mStatusView.setText("Network connection unavailable");
        }
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

        private void sendPath() throws Exception {
            URL url = null;
            HttpURLConnection c = null;
            OutputStream os = null;
            String apiCall = "[[\"add-path\", [{\"latitude\": 43.2, \"longitude\": -70.0}," +
                        "{\"latitude\": 43.3, \"longitude\": -70.0}]]]";
            byte[] bytes = apiCall.getBytes();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStatusView = (TextView)findViewById(R.id.status_view);
        Button sendButton = (Button)findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                handleSendClick();
            }
        });
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
