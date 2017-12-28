package com.spreadtrum.reverse;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DialerActivity extends Activity implements OnClickListener{
    private Button Btu0 = null;
    private Button Btu1 = null;
    private Button Btu2 = null;
    private  Button Btu3 = null;
    private  Button Btu4 = null;
    private Button Btu5 = null;
    private Button Btu6 = null;
    private Button Btu7 = null;
    private   Button Btu8 = null;
    private   Button Btu9 = null;
    private   Button Btux = null;
    private  Button Btuj = null;
    private  Button BtuDel = null;
    private   Button BtuCall = null;
    private TextView tvNumber;
	private StringBuffer sb;
	private ImageView ivBack;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.activity_dialer);
		
        Btu0 = (Button) findViewById(R.id.num_0);
        Btu1 = (Button) findViewById(R.id.num_1);
        Btu2 = (Button) findViewById(R.id.num_2);
        Btu3 = (Button) findViewById(R.id.num_3);
        Btu4 = (Button) findViewById(R.id.num_4);
        Btu5 = (Button) findViewById(R.id.num_5);
        Btu6 = (Button) findViewById(R.id.num_6);
        Btu7 = (Button) findViewById(R.id.num_7);
        Btu8 = (Button) findViewById(R.id.num_8);
        Btu9 = (Button) findViewById(R.id.num_9);
        Btux = (Button) findViewById(R.id.num_x);
        Btuj = (Button) findViewById(R.id.num_j);
        BtuDel = (Button) findViewById(R.id.num_del);
        BtuCall = (Button) findViewById(R.id.num_call);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        tvNumber=(TextView) findViewById(R.id.tv_number);
        
        Btu0.setOnClickListener(this);
        Btu1.setOnClickListener(this);
        Btu2.setOnClickListener(this);
        Btu3.setOnClickListener(this);
        Btu4.setOnClickListener(this);
        Btu5.setOnClickListener(this);
        Btu6.setOnClickListener(this);
        Btu7.setOnClickListener(this);
        Btu8.setOnClickListener(this);
        Btu9.setOnClickListener(this);
        Btux.setOnClickListener(this);
        Btuj.setOnClickListener(this);
        BtuCall.setOnClickListener(this);
        BtuDel.setOnClickListener(this);
        ivBack.setOnClickListener(this);
        sb = new StringBuffer();
	}
	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		switch (view.getId()) {
		case R.id.num_0:
			sb.append(Btu0.getText());
			break;
		case R.id.num_1:
			sb.append(Btu1.getText());
			break;
		case R.id.num_2:
			sb.append(Btu2.getText());
			break;
		case R.id.num_3:
			sb.append(Btu3.getText());
			break;
		case R.id.num_4:
			sb.append(Btu4.getText());
			break;
		case R.id.num_5:
			sb.append(Btu5.getText());
			break;
		case R.id.num_6:
			sb.append(Btu6.getText());
			break;
		case R.id.num_7:
			sb.append(Btu7.getText());
			break;
		case R.id.num_8:
			sb.append(Btu8.getText());
			break;
		case R.id.num_9:
			sb.append(Btu9.getText());
			break;
		case R.id.num_j:
			sb.append(Btuj.getText());
			break;
		case R.id.num_x:
			sb.append(Btux.getText());
			break;
		case R.id.num_call:
			Intent intent =new Intent(DialerActivity.this,CallActivity.class);
			intent.putExtra("action", "callout");
			intent.putExtra("number", sb.toString());
			startActivity(intent);
			break;
		case R.id.num_del:
			if (sb.length()>0) {
				sb.deleteCharAt(sb.length()-1);
			}
			break;
		case R.id.iv_back:
			this.finish();
			break;
		default:
			break;
		}
		tvNumber.setText(sb.toString());
		
	}
}
