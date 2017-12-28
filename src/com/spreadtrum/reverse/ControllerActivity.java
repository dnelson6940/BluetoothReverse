package com.spreadtrum.reverse;

import com.spreadtrum.reverse.A2dpSinkService.A2dpSinkServiceBinder;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothAvrcp;
import android.bluetooth.BluetoothA2dpSink;
public class ControllerActivity extends Activity {

	private static final String TAG = "ControllerActivity";
	private ImageView ivPlay;
	private ImageView ivPre;
	private ImageView ivNext;
	private A2dpSinkService mService;
	BluetoothDevice mDevice;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_controller);
		Intent intent=getIntent();
		mDevice = (BluetoothDevice) intent.getExtra(BluetoothDevice.EXTRA_DEVICE);
		initView();
		IntentFilter mIntentFilter =new IntentFilter(); 
		mIntentFilter.addAction(BluetoothA2dpSink.ACTION_CONNECTION_STATE_CHANGED);
		mIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(mReceiver, mIntentFilter);
	}

	private void initView() {
		// TODO Auto-generated method stub
		
		rlController = (RelativeLayout) findViewById(R.id.rl_controler);
		ivPlay = (ImageView) findViewById(R.id.iv_play);
		ivPre = (ImageView) findViewById(R.id.iv_pre);
		ivNext = (ImageView) findViewById(R.id.iv_next);
		tvError = (TextView) findViewById(R.id.tv_a2dp_error);

		ivPlay.setOnClickListener(mPauseListener);
		ivPre.setOnClickListener(mPrevListener);
		ivNext.setOnClickListener(mNextListener);

		}
//	@Override
//	protected void onResume() {
//		// TODO Auto-generated method stub
//		if (A2dpSinkUtils.getInstance().getConnectionState(mDevice)==BluetoothProfile.STATE_DISCONNECTED) {
//			rlController.setVisibility(View.GONE);
//		} else {
//			tvError.setVisibility(View.GONE);
//		}
//		super.onResume();
//	}
	
    private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			A2dpSinkServiceBinder binder = (A2dpSinkServiceBinder)service;
			mService = binder.getService();
			Log.d(TAG, "bind service sucess");
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			mService = null;
		}  
    }; 
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		   Intent intent = new Intent(this, A2dpSinkService.class);
	        intent.putExtra(BluetoothDevice.EXTRA_DEVICE, mDevice);
	        startService(intent);
	        if (!bindService(intent, mConnection, Context.BIND_AUTO_CREATE)) {
	            Log.d(TAG, "fail to bindToService");
	            mService = null;
	            return;
	        }
		super.onStart();
	}
	
	private View.OnClickListener mPauseListener = new View.OnClickListener() {
        public void onClick(View v) {
        	sendPlayPauseCmd();
        }
    };

    private View.OnClickListener mPrevListener = new View.OnClickListener() {
        public void onClick(View v) {
        	sendBackwardCmd();
        }
    };

    private View.OnClickListener mNextListener = new View.OnClickListener() {
        public void onClick(View v) {
        	sendForwardCmd();
        }
    };
	private TextView tvError;
	private RelativeLayout rlController;
    
    private void setPauseButtonImage() {
        if (isPlaying()) {
            ivPlay.setImageResource(R.drawable.dainji_20);
        } else {
        	ivPlay.setImageResource(R.drawable.dianji_19);
        }
    }
    
    private boolean isPlaying() {
		// TODO Auto-generated method stub
    	if(mService == null){
	    	Log.d(TAG,"isPlaying service is null");
	    	return false;
    	}
    	boolean isplay = mService.isPlaying();
    	Log.d(TAG,"Activity.isPlaying(): " + isplay);
    	return isplay;
	}
    
	private void sendPlayPauseCmd() {
		// TODO Auto-generated method stub
		if(isPlaying()){
			if(mService.sendCommand(BluetoothAvrcp.PASSTHROUGH_ID_PAUSE)){
				mService.audioPause();
			}
		}else{
			if(mService.sendCommand(BluetoothAvrcp.PASSTHROUGH_ID_PLAY)){
				mService.audioPlay();
			}
		}
		setPauseButtonImage();
	}
	private void sendForwardCmd() {
		// TODO Auto-generated method stub
		mService.sendCommand(BluetoothAvrcp.PASSTHROUGH_ID_FORWARD);
	}
	private void sendBackwardCmd() {
		mService.sendCommand(BluetoothAvrcp.PASSTHROUGH_ID_BACKWARD);
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			Log.d(TAG, "mReceiver action =" + action);

			 if (BluetoothA2dpSink.ACTION_CONNECTION_STATE_CHANGED.equals(action)){
    			int state =  intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED);
    			Toast.makeText(ControllerActivity.this,"a2dp"+state ,0 ).show();
    			if(state == BluetoothProfile.STATE_DISCONNECTED){
    				rlController.setVisibility(View.GONE);
    				tvError.setVisibility(View.VISIBLE);
    			}else if (state == BluetoothProfile.STATE_CONNECTED) {
    				rlController.setVisibility(View.VISIBLE);
    				tvError.setVisibility(View.GONE);
    			}
            }else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
						BluetoothAdapter.ERROR);
				Toast.makeText(ControllerActivity.this,"bluetooth"+state ,0 ).show();
				if (BluetoothAdapter.STATE_ON == state) {
					if (A2dpSinkUtils.getInstance().getConnectionState(mDevice)==BluetoothProfile.STATE_CONNECTED) {
						rlController.setVisibility(View.VISIBLE);
	    				tvError.setVisibility(View.GONE);
					}
				} else if (BluetoothAdapter.STATE_OFF == state) {
					rlController.setVisibility(View.GONE);
    				tvError.setVisibility(View.VISIBLE);
				}
			}
		}
	};

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.d(TAG, "onDestroy");
		if(mService != null){
			unbindService(mConnection);
			Intent intent = new Intent(this, A2dpSinkService.class);
			stopService(intent);
		}
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}
}
