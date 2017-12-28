

package com.spreadtrum.reverse;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateUtils;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.vcard.VCardEntry;
import android.view.LayoutInflater;
import android.util.Log;
import android.text.TextUtils;
import java.util.ArrayList;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class PbapClientAdapter extends BaseAdapter {
	  private static final boolean DBG = true;
    private Context mContext;
    private ArrayList<VCardEntry> mEntryList;
    private LayoutInflater mInflater;
    private Bitmap mPhotoIcon,mGoCallIcon,mInCallIcon;
    private static final String TAG = "BluetoothPbapClientActivity";

    public PbapClientAdapter(Context context, ArrayList<VCardEntry> list) {
        mContext = context;
	 mEntryList = list;
	 this.mInflater = LayoutInflater.from(context);
	 mPhotoIcon = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.photo_icon);
	 mGoCallIcon=BitmapFactory.decodeResource(context.getResources(),
             R.drawable.go_call);
	 mInCallIcon=BitmapFactory.decodeResource(context.getResources(),
             R.drawable.in_call);
    }

       public int getCount() {
		return mEntryList.size();
	}
	
	public Object getItem(int position) {
	       if (mEntryList != null && position >= 0 && position < getCount()) {
	            return mEntryList.get(position);
	        }
	        return null;
	}

	public long getItemId(int position) {
		return position;
	}
	public void addItem(VCardEntry  vCard){
		mEntryList.add(vCard);
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		 if(mEntryList == null || (mEntryList != null && mEntryList.size() == 0)){
		 	return null;
		 }
               else if (position >= mEntryList.size()) {
                   Log.w(TAG, "Invalid view position:" + position + ", actual size is:"
                        + mEntryList.size());
                   return null;
              }

		VCardEntry ve = mEntryList.get(position);
		if(ve == null){
			return null;
		}
		
		ViewHolder holder = null;
		String nameStr = "";
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.bluetooth_pbap_item, null);
			holder = new ViewHolder();  
			holder.id_icon =(ImageView) convertView.findViewById(R.id.id_icon);
			holder.id_name = (TextView)convertView.findViewById(R.id.id_name);
			holder.id_number= (TextView)convertView.findViewById(R.id.id_number);
		
			convertView.setTag(holder);
		} else { 
			holder = (ViewHolder)convertView.getTag(); 
		}
		
		holder.id_icon.setImageBitmap(mPhotoIcon);

    if(DBG) Log.d(TAG, "TEST:getView() getFamily  ="  + ve.getNameData().getFamily());
		if(DBG) Log.d(TAG, "TEST:getView() getGiven   =" + ve.getNameData().getGiven());
		if(ve.getNameData() != null){
			if(ve.getNameData().getFamily() != null){
				nameStr += ve.getNameData().getFamily();
			}
			if(ve.getNameData().getGiven() != null){
				nameStr += ve.getNameData().getGiven();
			}
		}
		holder.id_name.setText(nameStr);
		
		if(ve.getPhoneList() != null 
			&& ve.getPhoneList().get(0) != null 
			&& ve.getPhoneList().get(0).getNumber() != null){
			
			if(DBG) Log.d(TAG, "TEST: getView()  ve.getPhoneList().get(0).getNumber() = " + ve.getPhoneList().get(0).getNumber());
			holder.id_number.setText(ve.getPhoneList().get(0).getNumber());
		}

 		return convertView;
	}
	
	class ViewHolder {
		ImageView id_icon;
		TextView id_name;
		TextView id_number;
	}
	
}
