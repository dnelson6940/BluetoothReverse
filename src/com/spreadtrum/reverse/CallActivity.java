package com.spreadtrum.reverse;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.bluetooth.BluetoothHeadsetClient;
import android.bluetooth.BluetoothHeadsetClientCall;
import android.widget.TextView;

public class CallActivity extends Activity implements OnClickListener{

	private Button btReject,btAccept,btHangUp,btSpeaker;
	private TextView tvCallNumber;
	private TextView tvCallTitle;
	private String number;
	private String action;
	private ImageView ivBack;
	private AudioManager mAudioManager;
    private int callState =-1;
    private static final String TAG = "CallActivity";
	private HfpClientUtils mHfpClientUtils;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.activity_call);
		
		btReject=(Button) findViewById(R.id.bt_reject);
		btAccept=(Button) findViewById(R.id.bt_accept);
		btHangUp=(Button) findViewById(R.id.bt_hangup);
		btSpeaker=(Button) findViewById(R.id.bt_speaker);
		tvCallNumber=(TextView) findViewById(R.id.tv_call_number);
		tvCallTitle=(TextView) findViewById(R.id.tv_call_title);
		ivBack=(ImageView) findViewById(R.id.iv_back);
		mHfpClientUtils = HfpClientUtils.getInstance();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    	Intent intent= getIntent();
		number = intent.getStringExtra("number");
		action = intent.getStringExtra("action");
		tvCallNumber.setText(number);
		
		btHangUp.setOnClickListener(this);
		btAccept.setOnClickListener(this);
		btReject.setOnClickListener(this);
		btSpeaker.setOnClickListener(this);
		ivBack.setOnClickListener(this);
		
		if (action.equals("callout")) {
			btAccept.setVisibility(View.GONE);
			btSpeaker.setVisibility(View.GONE);
			btReject.setVisibility(View.GONE);
			btHangUp.setVisibility(View.VISIBLE);
			tvCallTitle.setText("拨打电话");
			mHfpClientUtils.dial(number);
		}else if (action.equals("incoming")) {
			btAccept.setVisibility(View.VISIBLE);
			btSpeaker.setVisibility(View.GONE);
			btReject.setVisibility(View.VISIBLE);
			btHangUp.setVisibility(View.GONE);
			tvCallTitle.setText("来电");
		}
	
        IntentFilter mIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        mIntentFilter.addAction(BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED);
        mIntentFilter.addAction(BluetoothHeadsetClient.ACTION_AUDIO_STATE_CHANGED);
        mIntentFilter.addAction(BluetoothHeadsetClient.ACTION_AG_EVENT);
        mIntentFilter.addAction(BluetoothHeadsetClient.ACTION_CALL_CHANGED);
        registerReceiver(mReceiver, mIntentFilter);
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.bt_hangup:
				mHfpClientUtils.terminateCall();
				finish();
			break;
		case R.id.bt_reject:
			mHfpClientUtils.rejectCall();
			finish();
			break;
		case R.id.bt_accept:
			mHfpClientUtils.acceptCall();
			break;
		case R.id.iv_back:
			mHfpClientUtils.terminateCall();
			finish();
			break;
		case R.id.bt_speaker:
			if (btSpeaker.getText().equals("Speaker")) {
				btSpeaker.setText("Phone");
				mAudioManager.setParameter("BT_HeadSet_Audio", "0");
			} else {
				btSpeaker.setText("Speaker");
				mAudioManager.setParameter("BT_HeadSet_Audio", "1");
			}
			break;
		default:
			break;
		}

	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {



		@Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            Log.d(TAG, "received intent, action:" + action);
            
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                                               BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_TURNING_OFF) {
                    Log.d(TAG, "STATE_TURNING_OFF");
                } else if (state == BluetoothAdapter.STATE_ON) {
                    Log.d(TAG, "STATE_ON");
                }
            } else if (action.equals(BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, 
                                               BluetoothProfile.STATE_DISCONNECTED);
                if (state == BluetoothProfile.STATE_DISCONNECTED) {
                     Toast.makeText(getApplicationContext(), "Headset is disconnected, please connect a device before using this app.", Toast.LENGTH_SHORT).show();
                } else if (state == BluetoothProfile.STATE_CONNECTED) {
                     // TODO: we can get the remote feature and connected device information in this state
                     Toast.makeText(getApplicationContext(), "Headset is Connected", Toast.LENGTH_SHORT).show();
                }

            } else if (action.equals(BluetoothHeadsetClient.ACTION_AUDIO_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, 
                                                BluetoothHeadsetClient.STATE_AUDIO_DISCONNECTED);

                if (state == BluetoothHeadsetClient.STATE_AUDIO_CONNECTED) {
				     mAudioManager.setParameter("BT_HeadSet_Audio", "1");
                     Toast.makeText(getApplicationContext(), "Audio is Connected", Toast.LENGTH_SHORT).show();
                } else if (state == BluetoothHeadsetClient.STATE_AUDIO_DISCONNECTED) {
				     mAudioManager.setParameter("BT_HeadSet_Audio", "0");
                     Toast.makeText(getApplicationContext(), "Audio is Disconnected", Toast.LENGTH_SHORT).show();
                }
                
            } else if (action.equals(BluetoothHeadsetClient.ACTION_AG_EVENT)) {
            	Toast.makeText(getApplicationContext(), "ACTION_AG_EVENT", Toast.LENGTH_SHORT).show();
                
            } else if (action.equals(BluetoothHeadsetClient.ACTION_CALL_CHANGED)) {
                BluetoothHeadsetClientCall mCall = (BluetoothHeadsetClientCall) intent.getExtra(BluetoothHeadsetClient.EXTRA_CALL, null);
                if (mCall != null) {
                     callState = mCall.getState();

                     switch (callState) {
                        case BluetoothHeadsetClientCall.CALL_STATE_ACTIVE:
                			btAccept.setVisibility(View.GONE);
                			btSpeaker.setVisibility(View.VISIBLE);
                			btReject.setVisibility(View.GONE);
                			btHangUp.setVisibility(View.VISIBLE);
                            tvCallTitle.setText("正在通话中");
                            break;
                        case BluetoothHeadsetClientCall.CALL_STATE_DIALING:
                        	btAccept.setVisibility(View.GONE);
                			btSpeaker.setVisibility(View.GONE);
                			btReject.setVisibility(View.GONE);
                			btHangUp.setVisibility(View.VISIBLE);
                        	tvCallTitle.setText("拨打电话");
                            break;
                        case BluetoothHeadsetClientCall.CALL_STATE_ALERTING:
                        	tvCallTitle.setText("Alerting");
                            break;

                        case BluetoothHeadsetClientCall.CALL_STATE_WAITING:
                        	tvCallTitle.setText("Waiting");
                            break;
                        case BluetoothHeadsetClientCall.CALL_STATE_HELD_BY_RESPONSE_AND_HOLD:
                        	tvCallTitle.setText("Reponse and Held");
                            break;
                        case BluetoothHeadsetClientCall.CALL_STATE_TERMINATED:
                        	tvCallTitle.setText("Terminated");
                        	finish();
                            break;
                        case BluetoothHeadsetClientCall.CALL_STATE_HELD:
                        	tvCallTitle.setText("Held");
                            break;

                      }
                }
            }
        }
    };
    
}
