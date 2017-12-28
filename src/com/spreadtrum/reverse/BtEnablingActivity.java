
package com.spreadtrum.reverse;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

/**
 * This class is designed to show BT enabling progress.
 */
public class BtEnablingActivity extends AlertActivity {
    private static final String TAG = "BluetoothOppEnablingActivity";

    private static final boolean D = true;///Constants.DEBUG;

    private static final boolean V = true;//Constants.VERBOSE;

    private static final int BT_ENABLING_TIMEOUT = 0;

    private static final int BT_ENABLING_TIMEOUT_VALUE = 20000;

    private boolean mRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If BT is already enabled jus return.
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter.isEnabled()) {
            finish();
            return;
        }

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBluetoothReceiver, filter);
        mRegistered = true;

        // Set up the "dialog"
        final AlertController.AlertParams p = mAlertParams;
        p.mTitle = getString(R.string.enabling_progress_title);
        p.mView = createView();
        setupAlert();

        // Add timeout for enabling progress
        mTimeoutHandler.sendMessageDelayed(mTimeoutHandler.obtainMessage(BT_ENABLING_TIMEOUT),
                BT_ENABLING_TIMEOUT_VALUE);
    }

    private View createView() {
        View view = getLayoutInflater().inflate(R.layout.bt_enabling_progress, null);
        TextView contentView = (TextView)view.findViewById(R.id.progress_info);
        contentView.setText(getString(R.string.enabling_progress_content));

        return view;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (D) Log.d(TAG, "onKeyDown() called; Key: back key");
            mTimeoutHandler.removeMessages(BT_ENABLING_TIMEOUT);
            cancelPbapClientReq();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRegistered) {
            unregisterReceiver(mBluetoothReceiver);
        }
    }

    private final Handler mTimeoutHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BT_ENABLING_TIMEOUT:
                    if (V) Log.v(TAG, "Received BT_ENABLING_TIMEOUT msg.");
                    cancelPbapClientReq();
                    break;
                default:
                    break;
            }
        }
    };

    private final BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (V) Log.v(TAG, "TEST: Received intent: " + action) ;
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                    case BluetoothAdapter.STATE_ON:
                        mTimeoutHandler.removeMessages(BT_ENABLING_TIMEOUT);
                        finish();
                        break;
                    default:
                        break;
                }
            }
        }
    };

    private void cancelPbapClientReq() {
        if (PbapClientUtils.getPbapClientFlag()) {
            PbapClientUtils.setPbapClientFlag(false);
        }
        finish();
    }
}
