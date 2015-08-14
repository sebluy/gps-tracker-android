package android.sebluy.gpstracker;

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

public class Path {

    private ArrayList<Location> mPoints;
    private double mTotalDistance;
    private Date mStartTime;
    private Date mFinishTime;
    private double mAverageSpeed;
    private double mSpeedSum;
    private int mPointsWithSpeed;

    public void addPoint(Location location) {
        if (mPoints == null) {
            mPoints = new ArrayList<>();
            mStartTime = new Date();
            mTotalDistance = 0.0;
            mSpeedSum = 0.0;
            mPointsWithSpeed = 0;
            mAverageSpeed = 0.0;
        } else {
            mTotalDistance += mPoints.get(mPoints.size() - 1).distanceTo(location);
        }
        mFinishTime = new Date();
        updateSpeed(location);
        mPoints.add(location);
    }

    public boolean isEmpty() {
        return mPoints.isEmpty();
    }

    public long getDuration() {
        return mFinishTime.getTime() - mStartTime.getTime();
    }

    public double getAverageSpeed() {
        return mAverageSpeed;
    }

    public double getTotalDistance() {
        return mTotalDistance;
    }

    public JSONArray toJSON() throws JSONException {
        JSONArray JSONPath = new JSONArray();
        for (int i = 0 ; i < mPoints.size() ; i++) {
            JSONPath.put(locationToJSON(mPoints.get(i)));
        }
        return JSONPath;
    }

    private static JSONObject locationToJSON(Location location) throws JSONException {
        JSONObject latLng = new JSONObject();
        latLng.put("latitude", location.getLatitude());
        latLng.put("longitude", location.getLongitude());
        return latLng;
    }

    private void updateSpeed(Location location) {
        if (location.hasSpeed()) {
            mSpeedSum += location.getSpeed();
            mPointsWithSpeed += 1;
            mAverageSpeed = mSpeedSum/mPointsWithSpeed;
        }
    }

}

