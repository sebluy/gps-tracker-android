package android.sebluy.gpstracker;

import android.location.Location;
import android.text.format.DateFormat;

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

    public String toString() {
        return String.format("Start: %s\nDistance: %s\nSpeed: %s\n",
                mStartTime.toString(),
                Util.distanceString(mTotalDistance),
                Util.speedString(mAverageSpeed));
    }

    public JSONArray toJSON() throws JSONException {
        JSONArray JSONPath = new JSONArray();
        for (int i = 0 ; i < mPoints.size() ; i++) {
            JSONPath.put(locationToJSON(mPoints.get(i)));
        }
        return JSONPath;
    }

    private static JSONObject locationToJSON(Location location) throws JSONException {
        JSONObject point = new JSONObject();
        point.put("latitude", location.getLatitude());
        point.put("longitude", location.getLongitude());
        if (location.hasSpeed()) {
            point.put("speed", location.getSpeed());
        }
        if (location.hasAccuracy()) {
            point.put("accuracy", location.getAccuracy());
        }
        return point;
    }

    private void updateSpeed(Location location) {
        if (location.hasSpeed()) {
            mSpeedSum += location.getSpeed();
            mPointsWithSpeed += 1;
            mAverageSpeed = mSpeedSum/mPointsWithSpeed;
        }
    }

}

