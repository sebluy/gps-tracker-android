package android.sebluy.gpstracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class TrackingDialogFragment extends DialogFragment implements GPSListener {

    private Path mPath;
    private GPS mGPS;
    private AlertDialog mDialog;
    private TextView mCurrentSpeedView;
    private TextView mAverageSpeedView;
    private TextView mDistanceView;
    private TextView mDurationView;
    private TextView mProvider;

    @Override
    public void onLocationChanged(Location location) {
        mPath.addPoint(location);
        if (location.hasSpeed()) {
            mCurrentSpeedView.setText(Util.speedString(location.getSpeed()));
        } else {
            mCurrentSpeedView.setText("Unavailable");
        }
        mProvider.setText(location.getProvider());
        mAverageSpeedView.setText(Util.speedString(mPath.getAverageSpeed()));
        mDistanceView.setText(Util.distanceString(mPath.getTotalDistance()));
        mDurationView.setText(Util.timeString(mPath.getDuration()));
    }

    @Override
    public void onStatusChanged(String status) {
        mDialog.setTitle(status);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.tracking_table, null);

        mProvider = (TextView)view.findViewById(R.id.provider);
        mCurrentSpeedView = (TextView)view.findViewById(R.id.current_speed);
        mAverageSpeedView = (TextView)view.findViewById(R.id.average_speed);
        mDistanceView = (TextView)view.findViewById(R.id.total_distance);
        mDurationView = (TextView)view.findViewById(R.id.duration);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setNeutralButton("Pause", null)
                .setPositiveButton("Finish", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((MainActivity)getActivity()).updateCurrentPathView();
                    }
                });

        mDialog = builder.create();

        mPath = PathHolder.getPath();
        mGPS = new GPS(this);

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                resumeTracking();
            }
        });

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return mDialog;
    }

    private void resumeTracking() {
        Button resumeButton = mDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        mGPS.start();
        resumeButton.setText("Pause");
        resumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseTracking();
            }
        });
    }

    private void pauseTracking() {
        Button resumeButton = mDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        mGPS.stop();
        resumeButton.setText("Resume");
        resumeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resumeTracking();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mGPS.start();
    }

    @Override
    public void onStop() {
        mGPS.stop();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onStop();
    }

}
