package android.sebluy.gpstracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.HashMap;
import java.util.UUID;

public class ReceiverFragment extends DialogFragment {

    private static final String TAG = ReceiverFragment.class.getSimpleName();

    private static final long SCAN_PERIOD = 10000;

    private static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID TX_CHARA_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mAdapter;
    private HashMap<String, BluetoothDevice> mDevices;
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

        mDevices = new HashMap<>();
        mAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1);

        startScan();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Scanning").
                setAdapter(mAdapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                stopScan();
                                startLoading(mAdapter.getItem(which));
                                ProgressDialog.show(getActivity(), "Loading", "Please wait",
                                        true, true);
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

    private void startLoading(String deviceName) {
        BluetoothDevice device = mDevices.get(deviceName);
        Log.d(TAG, "Connecting " + deviceName);
        BluetoothGatt gatt = device.connectGatt(getActivity(), false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                Log.d(TAG, "Connected");
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices();
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                Log.d(TAG, "Services discovered");
                enableTxNotifications(gatt);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                try {
                    Log.d(TAG, new String(characteristic.getValue(), "UTF-8"));
                } catch (Exception e){
                    Log.d(TAG, "Decoding failed");
                }
            }
        });
        gatt.getServices();
    }

    private void enableTxNotifications(BluetoothGatt gatt) {
        BluetoothGattCharacteristic tx = gatt.getService(RX_SERVICE_UUID).getCharacteristic(TX_CHARA_UUID);
        gatt.setCharacteristicNotification(tx, true);
        BluetoothGattDescriptor descriptor = tx.getDescriptor(CCCD);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(descriptor);
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
            mDevices.put(device.getName(), device);
            mAdapter.add(device.getName());
            mAdapter.notifyDataSetChanged();
        }
    };

    private boolean isBluetoothEnabled() {
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }
}
