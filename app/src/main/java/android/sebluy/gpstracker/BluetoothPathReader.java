package android.sebluy.gpstracker;

import android.location.Location;

public class BluetoothPathReader {

    private enum Field {
        LATITUDE, LONGITUDE, SPEED
    }

    public enum Status {
        UNSTARTED, STARTED, FINISHED, ERROR;
    }

    private Field mField;
    private Status mStatus;
    private Location mLocation;
    private Path mPath;

    public BluetoothPathReader() {
        mField = Field.LATITUDE;
        mStatus = Status.UNSTARTED;
    }

    public Status getStatus() {
        return mStatus;
    }

    public Path getPath() {
        if (mStatus == Status.FINISHED) {
            return mPath;
        } else {
            return null;
        }
    }

    public void add(String value) {
        if (mStatus == Status.ERROR || mStatus == Status.FINISHED) {
            return;
        }
        if (mStatus == Status.UNSTARTED) {
            if (value.equals("start")) {
                mPath = new Path();
                mStatus = Status.STARTED;
            } else {
                mStatus = Status.ERROR;
            }
        } else {
            if (value.equals("finish")) {
                mStatus = Status.FINISHED;
            } else {
                addField(mField, value);
            }
        }
    }

    private void addField(Field field, String value) {
        switch (field) {
            case LATITUDE:
                mLocation = new Location("Arduino");
                mLocation.setLatitude(Double.valueOf(value));
                mField = Field.LONGITUDE;
                break;
            case LONGITUDE:
                mLocation.setLongitude(Double.valueOf(value));
                mField = Field.SPEED;
                break;
            case SPEED:
                mLocation.setSpeed(Float.valueOf(value));
                mPath.addPoint(mLocation);
                mField = Field.LATITUDE;
                break;
            default:
                mStatus = Status.ERROR;
                break;
        }
    }
}



