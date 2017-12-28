package com.spreadtrum.reverse;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.android.vcard.VCardEntry;
import android.bluetooth.client.pbap.BluetoothPbapClient;
public class InCallActivity extends Activity {
	protected static final String TAG = "TestMissCallActivity";
	private ArrayList<VCardEntry> mList;
	private PbapClientAdapter mPbapClientAdapter;
	private LinearLayout mLoadingLayout;
	private ImageView mLoadingView;
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			Log.i(TAG, "handleMessage msg=" + msg.what);
			switch (msg.what) {
			case 0:
				showList();
				mList = (ArrayList<VCardEntry>) msg.obj;
				for (VCardEntry ve : mList) {
					Log.d(TAG, "TEST: handleMessage()  ve = " + ve.toString());
				}

				mPbapClientAdapter = new PbapClientAdapter(InCallActivity.this,
						mList);
				lvOut.setAdapter(mPbapClientAdapter);
				mPbapClientAdapter.notifyDataSetChanged();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};
	private ListView lvOut;;
	private PbapClientUtils mPbapClientUtils;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_out_call);
		mLoadingLayout = (LinearLayout) findViewById(R.id.loading_layout);
		mLoadingView = (ImageView) findViewById(R.id.loading);
		lvOut = (ListView) findViewById(R.id.lv_out);
		mList = new ArrayList<VCardEntry>();
		mPbapClientUtils = PbapClientUtils.getInstance();
		 showLoading() ;
	}
	
	protected void onResume() {
		// TODO Auto-generated method stub
//		 showList();
		if (mPbapClientUtils.getCntedState() == PbapClientUtils.SESSION_CONNECTED) {
			mPbapClientUtils.pullPhonebook(BluetoothPbapClient.ICH_PATH,
					handler
					);
		} 
		super.onResume();
	}
	
	private void showLoading() {
	// TODO Auto-generated method stub
	if (mLoadingLayout != null) {
		mLoadingLayout.setVisibility(View.VISIBLE);
		Animation loadingAnimation = AnimationUtils.loadAnimation(
				InCallActivity.this, R.anim.loading);
		mLoadingView.startAnimation(loadingAnimation);
	}
}
	
	private void showList() {
		// TODO Auto-generated method stub
		mLoadingLayout.setVisibility(View.GONE);
		lvOut.setVisibility(View.VISIBLE);
	}
}
