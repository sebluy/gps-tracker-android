package android.sebluy.gpstracker;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements StatusListener {

    private TextView mCurrentPathView;

    public void updateCurrentPathView() {
        Path path = PathHolder.getPath();
        if (path == null) {
            mCurrentPathView.setText("No existing path");
        } else if (path.isEmpty()) {
            mCurrentPathView.setText("Path is empty");
        } else {
            mCurrentPathView.setText(path.toString());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCurrentPathView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCurrentPathView = (TextView)findViewById(R.id.current_path);
        findViewById(R.id.record_path_button).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PathHolder.setPath(new Path());
                        new TrackingDialogFragment().show(getFragmentManager(), "tracking");
                    }
                });
        findViewById(R.id.upload_path_button).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        JSONAPI.addPath(PathHolder.getPath(), MainActivity.this);
                    }
                });
        findViewById(R.id.recieve_path_button).
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new ReceiverFragment().show(getFragmentManager(), "recieving");
                    }
                });
    }

    @Override
    public void onStatusChanged(String status) {
        mCurrentPathView.setText(status);
    }
}
