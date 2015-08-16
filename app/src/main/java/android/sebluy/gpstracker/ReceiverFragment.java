package android.sebluy.gpstracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class ReceiverFragment extends DialogFragment {

    private static final long SCAN_PERIOD = 10000;

    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> mDevices;
    private Handler mHandler;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        mBluetoothAdapter = ((BluetoothManager)getActivity().
                getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();

        if (!isBluetoothEnabled()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 0);
        }

        mHandler = new Handler();

        mDevices = new ArrayList<>();
        mAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, mDevices);

        startScan();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Scanning").
                setAdapter(mAdapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                stopScan();
                            }
                        }).
                setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        stopScan();
                    }
                });

        return builder.create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            getFragmentManager().beginTransaction().remove(this).commit();
        }
    }

    private void startScan() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
            }
        }, SCAN_PERIOD);
        mBluetoothAdapter.startLeScan(mScanCallback);
    }

    private void stopScan() {
        mBluetoothAdapter.stopLeScan(mScanCallback);
    }

    private LeScanCallback mScanCallback = new LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            mAdapter.add(device.getName());
            mAdapter.notifyDataSetChanged();
        }
    };

    private boolean isBluetoothEnabled() {
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }
}
