package com.spreadtrum.reverse;

import java.util.List;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.bluetooth.BluetoothHeadsetClient;
import android.bluetooth.BluetoothHeadsetClientCall;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

@SuppressLint("ResourceAsColor")
public class HfpClientUtils {
    private BluetoothAdapter mAdapter = null;
    private static boolean mIsProfileReady = false;
    private  BluetoothHeadsetClient mHeadset = null;
    private static HfpClientUtils instance =null;
	private   BluetoothDevice mDevice=null;
	private Handler mHandler;
	private static final String TAG = "TestHfpClientUtils";
	
	public static HfpClientUtils getInstance(){
		if (instance==null){
			instance=new HfpClientUtils();
		}
		return instance;
	}
	
	public  void connect (Context context,BluetoothDevice device ,Handler handler){
		mDevice=device;
		mHandler=handler;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mAdapter.getProfileProxy(context, new HeadsetClientServiceListener(), BluetoothProfile.HEADSET_CLIENT);
        IntentFilter mIntentFilter = new IntentFilter(BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED);
        context.registerReceiver(mReceiver, mIntentFilter);
        
	}
	
	public int getConnectionState(BluetoothDevice device){
		return mHeadset.getConnectionState(device);
	}
	
	public BluetoothHeadsetClient getHeadset(){
		return mHeadset;
	}
	public BluetoothDevice getDevice(){
		return mDevice;
	}
	public static  boolean IsProfileReady(){
		return mIsProfileReady;
	}
	
	public static void  setProfileReady(boolean is){
		mIsProfileReady=is;
	}
	
	
	public void dial(String number) {
		mHeadset.dial(mDevice, number);
	}
	
	public void terminateCall(){
		mHeadset.terminateCall(mDevice, 0);
	}
	public void acceptCall (){
		mHeadset.acceptCall(mDevice, BluetoothHeadsetClient.CALL_ACCEPT_NONE);
	}
	public void rejectCall() {
		mHeadset.rejectCall(mDevice);
	}
	
	 private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			  String action = intent.getAction();
	          Log.d(TAG, "mReceiver action =" + action);
			
			 if (action.equals(BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED)) {
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
	
	private final class HeadsetClientServiceListener implements BluetoothProfile.ServiceListener {

		public void onServiceConnected(int profile, BluetoothProfile proxy) {
            Log.d(TAG,"Bluetooth service connected");
            mHeadset = (BluetoothHeadsetClient) proxy;
            if (getConnectionState(mDevice)==BluetoothProfile.STATE_DISCONNECTED) {
            	 mHeadset.connect(mDevice);
            }else {
            	mHandler.sendMessage(mHandler.obtainMessage(1, 2));
            	mIsProfileReady = true;
            }
            
        }

        public void onServiceDisconnected(int profile) {
            Log.d(TAG,"Bluetooth service disconnected");
            
        }
        
    }

}
