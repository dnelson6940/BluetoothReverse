package com.spreadtrum.reverse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;
import android.app.TabActivity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.bluetooth.BluetoothUuid;

import com.android.vcard.VCardEntry;

public class MainActivity extends TabActivity {
	private static final String TAG = "TestMainActivity";
	private static final boolean DBG = true;

	private String[] mTabTitle = new String[] { "音乐控制","联系人" ,"最近通话"};
	private Class<?>[] m_tabIntent = new Class<?>[] { ControllerActivity.class,
			ContactActivity.class, RecentActivity.class };

	private static final int CONNECT_TIMEOUT = 101;
	private static final int CONNECT_TIMEOUT_DELAY = 30000;
	private static final int DONT_SUPPORT = 2;
	private final int SESSION_CONNECTED_MSG = 1;
	private PbapClientUtils mPbapClientUtils;
	private BluetoothDevice mRemoteDevice;
	private LinearLayout mLoadingLayout;
	private ImageView mLoadingView;
	private TabHost tabHost;
	int i=0,k=0;
	private List<VCardEntry> mPbList=null;;
    static final ParcelUuid[] UUIDS = {
        BluetoothUuid.HSP_AG,
        BluetoothUuid.Handsfree_AG,
    };
	 static final  ParcelUuid[] SINK_UUIDS = {
		 BluetoothUuid.AudioSource,
		 BluetoothUuid. AvrcpTarget,
	    };

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (DBG)
				Log.d(TAG, "TEST: handleMessage() msg.what = " + msg.what);
			switch (msg.what) {

			case SESSION_CONNECTED_MSG:
				int state = (int) msg.obj;
				k++;
				if (i==k){
					onDismissLoadingDialog();
					removeMessages(CONNECT_TIMEOUT);
				}
				if (state == PbapClientUtils.SESSION_CONNECTED) {
					Toast.makeText(getApplicationContext(), "Connect Success!",
							Toast.LENGTH_SHORT).show();
				} else if (state == PbapClientUtils.SESSION_DISCONNECTED) {
					Toast.makeText(getApplicationContext(), "Connect False!",
							Toast.LENGTH_SHORT).show();
				}
				break;
			case DONT_SUPPORT:
				int profile = (int) msg.obj;
				switch(profile) {
				case 1:
					Toast.makeText(MainActivity.this, "对方设备不支持HeadsetProfile", 0).show();
				    break;
				case 2:
					Toast.makeText(MainActivity.this, "对方设备不支持AudioSource", 0).show();
					break;
				case 3:
					Toast.makeText(MainActivity.this, "对方设备不支持PSE", 0).show();
					break;
				}
				break;
			case CONNECT_TIMEOUT:
				Toast.makeText(MainActivity.this, "连接超时", 1).show();
				break;
			default:
				break;
			}

		}
	};
	private HfpClientUtils mHfpClientUtils;
	private ParcelUuid[] uuids;
	private A2dpSinkUtils mSinkUtils;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		mLoadingLayout = (LinearLayout) findViewById(R.id.loading_layout);
		mLoadingView = (ImageView) findViewById(R.id.loading);
		Intent intent = getIntent();
		mRemoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		uuids = mRemoteDevice.getUuids();
		mHfpClientUtils = HfpClientUtils.getInstance();
		mPbapClientUtils =PbapClientUtils.getInstance();
		mSinkUtils=A2dpSinkUtils.getInstance();
		mPbapClientUtils.init(mHandler,mRemoteDevice);
		tabHost = getTabHost();
		new ConnectAsynTask().execute();
		
		for (int i = 0; i < this.mTabTitle.length; i++) {
			String title = this.mTabTitle[i];
			Intent a = new Intent(this, m_tabIntent[i]);
			TabSpec spec = tabHost.newTabSpec(title).setIndicator(title)
					.setContent(a.putExtra(BluetoothDevice.EXTRA_DEVICE, mRemoteDevice));
			tabHost.addTab(spec);
		}
	}
	

	private void onDismissLoadingDialog() {
		if (mLoadingLayout != null
				&& mLoadingLayout.getVisibility() == View.VISIBLE) {
			mLoadingLayout.setVisibility(View.GONE);
			tabHost.setVisibility(View.VISIBLE);
		}
	}

	private class ConnectAsynTask extends AsyncTask<Void, Integer, Boolean> {


		public ConnectAsynTask() {
			
		}

		protected void onPreExecute() {
			super.onPreExecute();
			if (mLoadingLayout != null)
				mLoadingLayout.setVisibility(View.VISIBLE);
			tabHost.setVisibility(View.GONE);
			Animation loadingAnimation = AnimationUtils.loadAnimation(
					MainActivity.this, R.anim.loading);
			mLoadingView.startAnimation(loadingAnimation);
		}

		@Override
		protected void onPostExecute(Boolean result) {

		}

		@Override
		protected void onProgressUpdate(Integer... progresses) {

		}

		@Override
		protected Boolean doInBackground(Void... paramArrayOfParams) {
			mHandler.sendMessageDelayed(mHandler.obtainMessage(CONNECT_TIMEOUT),CONNECT_TIMEOUT_DELAY);
			if (BluetoothUuid.containsAnyUuid(uuids, UUIDS)) {
				mHfpClientUtils.connect(MainActivity.this, mRemoteDevice,mHandler);
				i++;
			} else {
				mHandler.sendMessage(mHandler.obtainMessage(DONT_SUPPORT, 1));
			}
			if (BluetoothUuid.containsAnyUuid(uuids, SINK_UUIDS)) {
				mSinkUtils.connect(MainActivity.this,mRemoteDevice,mHandler);
				i++;
			} else {
				mHandler.sendMessage(mHandler.obtainMessage(DONT_SUPPORT, 2));
			}
			
			if (BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.PBAP_PSE)) {
				mPbapClientUtils.connect();
				i++;
				
			} else {
				mHandler.sendMessage(mHandler.obtainMessage(DONT_SUPPORT, 3));
			}
			
			return true;
		}
	}
}
