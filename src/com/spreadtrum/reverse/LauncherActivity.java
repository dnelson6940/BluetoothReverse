package com.spreadtrum.reverse;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class LauncherActivity extends Activity {
	private static final String TAG = "LauncherActivity";
	private static final boolean DBG = true;// Constants.DEBUG;
	public static final String ACTION_LAUNCH = "android.bluetooth.devicepicker.action.LAUNCH";
	public static final String EXTRA_LAUNCH_CLASS = "android.bluetooth.devicepicker.extra.DEVICE_PICKER_LAUNCH_CLASS";
	public static final String EXTRA_LAUNCH_PACKAGE = "android.bluetooth.devicepicker.extra.LAUNCH_PACKAGE";
	public static final int FILTER_TYPE_AUDIO = 1;
	public static final String EXTRA_FILTER_TYPE = "android.bluetooth.devicepicker.extra.FILTER_TYPE";
	public static final String EXTRA_NEED_AUTH = "android.bluetooth.devicepicker.extra.NEED_AUTH";
	public static final String ACTION_DEVICE_SELECTED = "android.bluetooth.devicepicker.action.DEVICE_SELECTED";
	public static final String ACTION_CALL_CHANGED = "android.bluetooth.headsetclient.profile.action.AG_CALL_CHANGED";
	private BluetoothAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		if (DBG)
			Log.d(TAG, "TEST: onCreate() mAdapter = " + mAdapter);
		if (!isBluetoothAllowed()) {
			Toast.makeText(this, this.getString(R.string.airplane_error_msg), 0)
					.show();
			finish();
			return;
		}

		new Thread(new Runnable() {
			public void run() {
				launchDevicePicker();
				finish();
			}
		}).start();
		return;

	}

	private final boolean isBluetoothAllowed() {
		final ContentResolver resolver = this.getContentResolver();

		// Check if airplane mode is on
		final boolean isAirplaneModeOn = Settings.System.getInt(resolver,
				Settings.System.AIRPLANE_MODE_ON, 0) == 1;
		if (!isAirplaneModeOn) {
			return true;
		}

		// Check if airplane mode matters
		final String airplaneModeRadios = Settings.System.getString(resolver,
				Settings.System.AIRPLANE_MODE_RADIOS);
		final boolean isAirplaneSensitive = airplaneModeRadios == null ? true
				: airplaneModeRadios.contains(Settings.System.RADIO_BLUETOOTH);
		if (!isAirplaneSensitive) {
			return true;
		}

		// Check if Bluetooth may be enabled in airplane mode
		final String airplaneModeToggleableRadios = Settings.System.getString(
				resolver, Settings.System.AIRPLANE_MODE_TOGGLEABLE_RADIOS);
		final boolean isAirplaneToggleable = airplaneModeToggleableRadios == null ? false
				: airplaneModeToggleableRadios
						.contains(Settings.System.RADIO_BLUETOOTH);
		if (isAirplaneToggleable) {
			return true;
		}

		// If we get here we're not allowed to use Bluetooth right now
		return false;
	}

	private final void launchDevicePicker() {
		if (mAdapter == null) {
			if (DBG)
				Log.d(TAG, "TEST: mAdapter == null");
			return;
		} else if (!(mAdapter.isEnabled())) {
			if (DBG)
				Log.v(TAG, "Prepare Enable BT!! ");
			 	Intent in = new Intent(this, BtEnableActivity.class);
	            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            startActivity(in);
		} else {
			Log.d(TAG, "TEST:BT already enabled!! ");
			Intent it = new Intent(ACTION_LAUNCH);
			it.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			it.putExtra(EXTRA_NEED_AUTH, false);
			it.putExtra(EXTRA_FILTER_TYPE, FILTER_TYPE_AUDIO);
			it.putExtra(EXTRA_LAUNCH_PACKAGE, "com.spreadtrum.reverse");
			it.putExtra(EXTRA_LAUNCH_CLASS, ReverseReceiver.class.getName());
			if (DBG) {
				Log.d(TAG, "TEST: Launching " + ACTION_LAUNCH);
			}
			startActivity(it);
		}
	}
}
