

package com.spreadtrum.reverse;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.content.Context;
import android.bluetooth.BluetoothAdapter;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;

/**
 * This class is designed to show BT enable confirmation dialog;
 */
public class BtEnableActivity extends AlertActivity implements
        DialogInterface.OnClickListener {
    private WifiManager mWifiManager;
    private boolean supportBtWifiSoftApCoexit = true;
    private BluetoothAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = BluetoothAdapter.getDefaultAdapter();
        
        // Set up the "dialog"
        final AlertController.AlertParams p = mAlertParams;
        p.mIconAttrId = android.R.attr.alertDialogIcon;
        p.mTitle = getString(R.string.bt_enable_title);
        p.mView = createView();
        p.mPositiveButtonText = getString(R.string.bt_enable_ok);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(R.string.bt_enable_cancel);
        p.mNegativeButtonListener = this;
        setupAlert();
        /* SPRD : add for the softap and bt coexit @{ */
        if (SystemProperties.get("ro.btwifisoftap.coexist", "true").equals(
                "false")) {
            if (mWifiManager == null) {
                mWifiManager = (WifiManager) this
                        .getSystemService(Context.WIFI_SERVICE);
            }
            supportBtWifiSoftApCoexit = false;
        }
        /*@}  */
    }

    private View createView() {
        View view = getLayoutInflater().inflate(R.layout.confirm_dialog, null);
        TextView contentView = (TextView)view.findViewById(R.id.content);
        contentView.setText(getString(R.string.bt_enable_line1) + "\n\n"
                + getString(R.string.bt_enable_line2) + "\n");

        return view;
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                if (!supportBtWifiSoftApCoexit) {
                    if (mWifiManager.isSoftapEnablingOrEnabled()) {
                        Toast.makeText(this,R.string.bt_softap_cannot_coexist, Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                    }
                }
		 
		  if(mAdapter != null){
		  	 mAdapter.enable();
			           PbapClientUtils.setPbapClientFlag(true);
	               Toast.makeText(this, getString(R.string.enabling_progress_content),
	                        Toast.LENGTH_SHORT).show();

	               Intent in = new Intent(this, BtEnablingActivity.class);
	               in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	               this.startActivity(in);
		  }

                finish();
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                finish();
                break;
        }
    }
}
