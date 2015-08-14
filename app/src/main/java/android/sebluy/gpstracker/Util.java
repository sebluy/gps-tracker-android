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

public class Util {

    public static double mpsToMph(double mps) {
        return mps * 2.23694;
    }

    public static Object metersToMiles(double totalDistance) {
        return totalDistance / 1609.34;
    }

    public static String timeString(long duration) {
        long seconds = (duration / 1000) % 60;
        long minutes = (duration / (60 * 1000)) % 60;
        long hours = (duration / (60 * 60 * 1000)) % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }


    public static String distanceString(double totalDistance) {
        return String.format("%.3f Miles", metersToMiles(totalDistance));
    }


    public static String speedString(double averageSpeed) {
        return String.format("%.2f MPH", mpsToMph(averageSpeed));
    }

}

