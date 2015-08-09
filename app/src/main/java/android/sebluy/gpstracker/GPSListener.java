package android.sebluy.gpstracker;

import android.location.Location;

/**
 * Created by sebluy on 8/9/15.
 */
public interface GPSListener {
    void onLocationChanged(Location location);
    void onStatusChanged(String status);
}
