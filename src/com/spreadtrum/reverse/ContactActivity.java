package com.spreadtrum.reverse;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothHeadsetClient;
import com.android.vcard.VCardEntry;
import android.bluetooth.client.pbap.BluetoothPbapClient;

public class ContactActivity extends Activity implements OnItemClickListener{
	protected static final String TAG = "TestContactActivity";
	private Button btBottomCall;
	private ListView lvContact;
	private  ArrayList<VCardEntry> mList;
    private PbapClientAdapter mPbapClientAdapter;
	private PbapClientUtils mPbapClientUtils;
	private BluetoothDevice mRemoteDevice;
	private LinearLayout mLoadingLayout;
	private ImageView mLoadingView;
	private TextView tvError;

	private Handler handler =new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			Log.i(TAG, "handleMessage msg="+msg.what);
			switch (msg.what) {
			case 0:
				showList();
				mList=(ArrayList<VCardEntry>) msg.obj;
				mPbapClientAdapter = new PbapClientAdapter(
						ContactActivity.this,
						mList
						);
				lvContact.setAdapter(mPbapClientAdapter);
				mPbapClientAdapter.notifyDataSetChanged();
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			Log.d(TAG, "mReceiver action =" + action);

			if (action
					.equals(BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED)) {
				int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE,
						BluetoothProfile.STATE_DISCONNECTED);

				if (state == BluetoothProfile.STATE_DISCONNECTED) {
					btBottomCall.setBackgroundColor(getResources().getColor(
							R.color.ok_text_color_gray));
				} else if (state == BluetoothProfile.STATE_CONNECTED) {
					// TODO: we can get the remote feature and connected device
					// information in this state
					btBottomCall.setBackgroundColor(getResources().getColor(
							R.color.background));
				}
			} else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
						BluetoothAdapter.ERROR);
				Log.i(TAG, "state="+mPbapClientUtils.getState());
				
				if (BluetoothAdapter.STATE_ON == state) {
					Log.i(TAG, "on"+mPbapClientUtils.getCntedState());
					if (mPbapClientUtils.getCntedState() == PbapClientUtils.SESSION_CONNECTED) {
						showList();
					}
					if (HfpClientUtils.getInstance().getConnectionState(mRemoteDevice)==BluetoothProfile.STATE_CONNECTED) {
						btBottomCall.setBackgroundColor(getResources().getColor(
								R.color.background));
					}
				} else if (BluetoothAdapter.STATE_OFF == state) {
					Log.i(TAG, "off "+mPbapClientUtils.getCntedState());
					showError();
					btBottomCall.setBackgroundColor(getResources().getColor(
							R.color.ok_text_color_gray));
					
				}
			}
		}

	};
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_contact);
		Intent intent=getIntent();
		mRemoteDevice = (BluetoothDevice) intent.getExtra(BluetoothDevice.EXTRA_DEVICE);
		initView();
		showLoading();
		mPbapClientUtils	=	PbapClientUtils.getInstance();
		mList=new ArrayList<VCardEntry>();
		IntentFilter mIntentFilter = new IntentFilter(
				BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED);
		mIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		this.registerReceiver(mReceiver, mIntentFilter);
		
	}

	private void initView() {
		// TODO Auto-generated method stub
		lvContact = (ListView) findViewById(R.id.lv_contact);
		btBottomCall = (Button) findViewById(R.id.bt_bottom_call);
		lvContact.setOnItemClickListener(this);
		mLoadingLayout = (LinearLayout) findViewById(R.id.loading_layout);
		mLoadingView = (ImageView) findViewById(R.id.loading);
		tvError = (TextView) findViewById(R.id.tv_pbap_error);
		btBottomCall.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				if (HfpClientUtils.getInstance().getConnectionState(mRemoteDevice)==BluetoothProfile.STATE_CONNECTED) {
					Intent intent=new Intent(ContactActivity.this,DialerActivity.class);
					startActivity(intent);
				} else {
					Toast.makeText(ContactActivity.this, R.string.hfp_error, 0).show();
				}
			}
		});
		
	}

	private void showList() {
		// TODO Auto-generated method stub
			mLoadingLayout.setVisibility(View.GONE);
			tvError.setVisibility(View.GONE);
			lvContact.setVisibility(View.VISIBLE);
	}

	private void showLoading() {
		// TODO Auto-generated method stub
		if (mLoadingLayout != null) {
			mLoadingLayout.setVisibility(View.VISIBLE);
			tvError.setVisibility(View.GONE);
			Animation loadingAnimation = AnimationUtils.loadAnimation(
					ContactActivity.this, R.anim.loading);
			mLoadingView.startAnimation(loadingAnimation);
		}
	}
	
	private void showError() {
		// TODO Auto-generated method stub

		mLoadingLayout.setVisibility(View.GONE);
		lvContact.setVisibility(View.GONE);
		tvError.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		if (mPbapClientUtils.getCntedState()==PbapClientUtils.SESSION_CONNECTED) {
			mPbapClientUtils.pullPhonebook(BluetoothPbapClient.PB_PATH, handler);
		}else {
			showError();
		}
		if (HfpClientUtils.getInstance().getConnectionState(mRemoteDevice)==BluetoothProfile.STATE_DISCONNECTED) {
			btBottomCall.setBackgroundColor(getResources().getColor(
					R.color.ok_text_color_gray));
		} 
		super.onResume();
	}
	

	
	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
		// TODO Auto-generated method stub
		String number=mList.get(position).getPhoneList().get(0).getNumber();
		if (number!=null){
			if (HfpClientUtils.getInstance().getConnectionState(mRemoteDevice)==BluetoothProfile.STATE_CONNECTED) {
				Intent intent=new Intent(ContactActivity.this,CallActivity.class);
				intent.putExtra("number", number);
				intent.putExtra("action", "callout");
				startActivity(intent);
			}else {
				Toast.makeText(ContactActivity.this, R.string.hfp_error, 0).show();
			}
		}
	
	}

}
