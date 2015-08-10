package android.sebluy.gpstracker;

import android.location.Location;

import com.google.android.gms.wallet.NotifyTransactionStatusRequest;

/**
 * Created by sebluy on 8/9/15.
 */
public interface GPSListener extends StatusListener {
    void onLocationChanged(Location location);
}
