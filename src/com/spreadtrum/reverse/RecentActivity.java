package com.spreadtrum.reverse;

import java.util.ArrayList;

import android.app.Activity;
import android.app.TabActivity;
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
import android.widget.TabHost.TabSpec;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothHeadsetClient;
import com.android.vcard.VCardEntry;
import android.bluetooth.client.pbap.BluetoothPbapClient;
import android.bluetooth.BluetoothPbap;

public class RecentActivity extends TabActivity {

	protected static final String TAG = "TestRecentActivity";
	private Button btBottomCall;
	// private ListView lvRecent;
	// private ArrayList<VCardEntry> mList;
	// private PbapClientAdapter mPbapClientAdapter;
	private PbapClientUtils mPbapClientUtils;
	private BluetoothDevice mRemoteDevice;
	// private LinearLayout mLoadingLayout;
	// private ImageView mLoadingView;
	private TextView tvError;
	private String[] mTabTitle = new String[] { "呼出", "呼入", "未接" };
	private Class<?>[] m_tabIntent = new Class<?>[] { OutCallActivity.class,
			InCallActivity.class, MissCallActivity.class };

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
				if (BluetoothAdapter.STATE_ON == state) {
					if (mPbapClientUtils.getCntedState() == PbapClientUtils.SESSION_CONNECTED) {
						showList();
					}
					if (HfpClientUtils.getInstance().getConnectionState(
							mRemoteDevice) == BluetoothProfile.STATE_CONNECTED) {
						btBottomCall.setBackgroundColor(getResources()
								.getColor(R.color.background));
					}
				} else if (BluetoothAdapter.STATE_OFF == state) {
					showError();

					btBottomCall.setBackgroundColor(getResources().getColor(
							R.color.ok_text_color_gray));

				}
			}
		}
	};
	private TabHost tabHost;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_recent);
		Intent intent = getIntent();
		mRemoteDevice = (BluetoothDevice) intent
				.getExtra(BluetoothDevice.EXTRA_DEVICE);
		initView();
		// showLoading();
		mPbapClientUtils = PbapClientUtils.getInstance();
		// mList = new ArrayList<VCardEntry>();
		IntentFilter mIntentFilter = new IntentFilter(
				BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED);
		mIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		this.registerReceiver(mReceiver, mIntentFilter);
	}

	private void initView() {
		// TODO Auto-generated method stub
		btBottomCall = (Button) findViewById(R.id.bt_bottom_call);
		tabHost = getTabHost();
		for (int i = 0; i < this.mTabTitle.length; i++) {
			String title = this.mTabTitle[i];
			Intent a = new Intent(this, m_tabIntent[i]);
			TabSpec spec = tabHost
					.newTabSpec(title)
					.setIndicator(title)
					.setContent(
							a.putExtra(BluetoothDevice.EXTRA_DEVICE,
									mRemoteDevice));
			tabHost.addTab(spec);
		}

		tvError = (TextView) findViewById(R.id.tv_pbap_error);
		btBottomCall.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				if (HfpClientUtils.getInstance().getConnectionState(
						mRemoteDevice) == BluetoothProfile.STATE_CONNECTED) {
					Intent intent = new Intent(RecentActivity.this,
							DialerActivity.class);
					startActivity(intent);
				} else {
					Toast.makeText(RecentActivity.this, R.string.hfp_error, 0)
							.show();
				}
			}
		});

	}

	private void showList() {
		// TODO Auto-generated method stub
		// mLoadingLayout.setVisibility(View.GONE);

		tvError.setVisibility(View.GONE);
		tabHost.setVisibility(View.VISIBLE);
	}

	private void showError() {
		// TODO Auto-generated method stub

		tabHost.setVisibility(View.GONE);
		tvError.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.i(TAG, "PbapState=" + mPbapClientUtils.getCntedState());
		if (mPbapClientUtils.getCntedState() == PbapClientUtils.SESSION_CONNECTED) {
			showList();
		} else {
			showError();
		}
		if (HfpClientUtils.getInstance().getConnectionState(mRemoteDevice) == BluetoothProfile.STATE_DISCONNECTED) {
			btBottomCall.setBackgroundColor(getResources().getColor(
					R.color.ok_text_color_gray));
		}
		super.onResume();
	}

}
