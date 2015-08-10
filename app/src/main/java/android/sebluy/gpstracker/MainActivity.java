package android.sebluy.gpstracker;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

public class MainActivity extends Activity implements GPSListener {

    private TextView mStatusView;
    private TextView mLatitudeView;
    private TextView mLongitudeView;
    private TextView mSpeedView;
    private TextView mDistanceView;
    private TextView mTimeView;
    private TextView mPointCountView;

    private GPS mGPS;

    private float mpsToMph(float mps) {
        return mps * 2.23694f;
    }

    private float distanceTo(Location location) {
        float[] results = new float[1];
        Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                43.694011, -70.588709, results);
        return results[0];
    }

    @Override
    public void onLocationChanged(Location location) {
        mLatitudeView.setText(String.valueOf(location.getLatitude()));
        mLongitudeView.setText(String.valueOf(location.getLongitude()));
        mSpeedView.setText(String.valueOf(mpsToMph(location.getSpeed()))) ;
        mDistanceView.setText(String.valueOf(distanceTo(location)));
        mTimeView.setText(DateFormat.getTimeInstance().format(new Date()));
        mPointCountView.setText(String.valueOf(mGPS.getPoints().size()));
    }

    @Override
    public void onStatusChanged(String status) {
        mStatusView.setText(status);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        mStatusView = (TextView)findViewById(R.id.status_view);
        mLatitudeView = (TextView)findViewById(R.id.latitude);
        mLongitudeView = (TextView)findViewById(R.id.longitude);
        mSpeedView = (TextView)findViewById(R.id.speed);
        mDistanceView = (TextView)findViewById(R.id.distance);
        mTimeView = (TextView)findViewById(R.id.time);
        mPointCountView = (TextView)findViewById(R.id.point_count);

        mGPS = new GPS(getApplicationContext(), this);

        Button sendButton = (Button)findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mGPS.stop();
                new Remote(getApplicationContext(), MainActivity.this).sendPath(mGPS.getPoints());
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
