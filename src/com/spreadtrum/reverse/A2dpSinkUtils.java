package com.spreadtrum.reverse;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.bluetooth.BluetoothA2dpSink;


public class A2dpSinkUtils {

	public static final String TAG = "A2dpSinkUtils";
	private static A2dpSinkUtils instance = null;
	private BluetoothDevice mDevice = null;
    private static boolean mIsProfileReady = false;
	private BluetoothAdapter mAdapter = null;
	private BluetoothA2dpSink mSink;
	
	private Handler mHandler;
	public static A2dpSinkUtils getInstance() {
		if (instance == null) {
			instance = new A2dpSinkUtils();
		}
		return instance;
	}

	public void connect(Context context, BluetoothDevice device,Handler handler) {
		mDevice = device;
		mHandler=handler;
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mAdapter.getProfileProxy(context, new A2dpSinkServiceListener(),
				BluetoothProfile.A2DP_SINK);
		   IntentFilter mIntentFilter = new IntentFilter(BluetoothA2dpSink.ACTION_CONNECTION_STATE_CHANGED);
	        context.registerReceiver(mReceiver, mIntentFilter);
	}
	
	public int getConnectionState(BluetoothDevice device){
		return mSink.getConnectionState(device);
		
	}
	
	public BluetoothA2dpSink getA2dpSink(){
		return mSink;
	}

	public static boolean IsProfileReady(){
		return mIsProfileReady;
	}
	
	public static void setProfileReady(boolean is){
		mIsProfileReady=is;
	}
	
	private final class A2dpSinkServiceListener implements BluetoothProfile.ServiceListener {

		public void onServiceConnected(int profile, BluetoothProfile proxy) {
			Log.d(TAG, "Bluetooth service connected");
			mSink = (BluetoothA2dpSink) proxy;
			Log.i(TAG, "mSink="+mSink+";mDevice="+mDevice);
			if (getConnectionState(mDevice)==BluetoothProfile.STATE_DISCONNECTED) {
				mSink.connect(mDevice);
			} else {
            	mHandler.sendMessage(mHandler.obtainMessage(1, 2));
            	mIsProfileReady = true;
            }
           
		}

		public void onServiceDisconnected(int profile) {
			Log.d(TAG, "Bluetooth service disconnected");
		}
	}
	
	 private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			  String action = intent.getAction();
	          Log.d(TAG, "mReceiver action =" + action);
			
			 if (action.equals(BluetoothA2dpSink.ACTION_CONNECTION_STATE_CHANGED)) {
	                int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, 
	                                               BluetoothProfile.STATE_DISCONNECTED);
	                if (state == BluetoothProfile.STATE_DISCONNECTED) {
	                	mIsProfileReady = false;
	                	mHandler.sendMessage(mHandler.obtainMessage(1, 4));
	                	
	                } else if (state == BluetoothProfile.STATE_CONNECTED) {
	                     // TODO: we can get the remote feature and connected device information in this state
	                	mIsProfileReady = true;
	                	mHandler.sendMessage(mHandler.obtainMessage(1, 2));
	                	
	                }
	            } 
		}
		 
	 };

}
