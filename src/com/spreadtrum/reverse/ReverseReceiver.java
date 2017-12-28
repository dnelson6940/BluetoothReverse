package com.spreadtrum.reverse;

import java.util.ArrayList;
import java.util.Arrays;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.bluetooth.BluetoothHeadsetClient;
import android.bluetooth.BluetoothHeadsetClientCall;

public class ReverseReceiver extends BroadcastReceiver {
	private static final String TAG = "TestReverseReceiver";
	private static final boolean DBG = true; // Constants.DEBUG;
	private int callState = -1;

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (DBG)
			Log.d(TAG, "onReceive() action = " + action);
		if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
			int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
					BluetoothAdapter.ERROR);
			if (BluetoothAdapter.STATE_ON == state) {
				synchronized (this) {
					if (PbapClientUtils.getPbapClientFlag()) {
						if (DBG)
							Log.d(TAG,
									"Received BLUETOOTH_STATE_CHANGED_ACTION, BLUETOOTH_STATE_ON");
						PbapClientUtils.setPbapClientFlag(false);

						Intent it = new Intent(LauncherActivity.ACTION_LAUNCH);
						it.putExtra(LauncherActivity.EXTRA_NEED_AUTH, false);
						it.putExtra(LauncherActivity.EXTRA_FILTER_TYPE,
								LauncherActivity.FILTER_TYPE_AUDIO);
						it.putExtra(LauncherActivity.EXTRA_LAUNCH_PACKAGE,
								"com.spreadtrum.reverse");
						it.putExtra(LauncherActivity.EXTRA_LAUNCH_CLASS,
								ReverseReceiver.class.getName());
						it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.startActivity(it);
					}
				}
			}
		} else if (action.equals(LauncherActivity.ACTION_DEVICE_SELECTED)) {
			Log.i(TAG, "设备选择成功");
			final BluetoothDevice remoteDevice = intent
					.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			if (DBG)
				Log.d(TAG, "设备选择成功 Device  = " + remoteDevice.getName());

			Intent it = new Intent(context, MainActivity.class);
			it.putExtra(BluetoothDevice.EXTRA_DEVICE, remoteDevice);
			it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TOP);
			context.startActivity(it);
		} else if (action.equals(LauncherActivity.ACTION_CALL_CHANGED)) {
			BluetoothHeadsetClientCall mCall = (BluetoothHeadsetClientCall) intent
					.getExtra(BluetoothHeadsetClient.EXTRA_CALL, null);
			if (mCall != null) {
				callState = mCall.getState();
				switch (callState) {
				case BluetoothHeadsetClientCall.CALL_STATE_INCOMING:
					Intent a = new Intent(context, CallActivity.class);
					a.putExtra("action", "incoming");
					a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(a);
					break;
//				case BluetoothHeadsetClientCall.CALL_STATE_DIALING :
//					Intent b = new Intent(context, CallActivity.class);
//					b.putExtra("action", "callout");
//					b.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//					context.startActivity(b);
//					break;
				}
			}
		}
	}
}