package com.spreadtrum.reverse;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.client.pbap.BluetoothPbapCard;
import android.bluetooth.client.pbap.BluetoothPbapClient;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.os.Looper;
import android.os.Process;
import android.os.HandlerThread;
import com.android.vcard.VCardEntry;

public class PbapClientUtils {
	public static final String TAG = "TestPbapClientUtils";
	private static final boolean DBG = true;
	private static  BluetoothDevice mRemoteDevice;
	private BluetoothPbapClient mPbapClient;
	private String mCurCallNumber;
	private HandlerThread mHandlerThread;
	private  BluetoothPbapClientHandler mPbapClientHandler;
	private static PbapClientUtils INSTANCE;
	private static Object INSTANCE_LOCK = new Object();
       private static boolean mPbapClientFlag;
	private ArrayDeque<String> mSetPathQueen = null;
	private int mSessionState;
	private static  Handler mHandler;
	 boolean isFirst=true;
	
	public static final int SESSION_CONNECTING = 1;
       public static final int SESSION_CONNECTED = 2;
	public static final int SESSION_DISCONNECTING = 3;
       public static final int SESSION_DISCONNECTED = 4;

       
	public   void init(Handler handler,BluetoothDevice remoteDevice) {
		mSessionState= SESSION_DISCONNECTED;
		mHandlerThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
	       mHandlerThread.start();
	       mRemoteDevice=remoteDevice;
	       mPbapClientHandler = new BluetoothPbapClientHandler(mHandlerThread.getLooper());
	       mPbapClient = new BluetoothPbapClient(mRemoteDevice, mPbapClientHandler);
	       mHandler=handler;
	}
	public static PbapClientUtils getInstance( ) {
          synchronized (INSTANCE_LOCK) {
             if (INSTANCE == null) {
                 INSTANCE = new PbapClientUtils();
             }
             return INSTANCE;
         }
      }

      @Override
    public void finalize() {
        cleanUp();
    }
      
	public static void setPbapClientFlag(boolean isFlag) {
              mPbapClientFlag = isFlag;
      }

      public static boolean getPbapClientFlag() {
              return mPbapClientFlag;
      }

	private boolean isConnect() {
		return mSessionState == SESSION_CONNECTED || mSessionState == SESSION_CONNECTING;
	}

	private boolean isDisconcted() {
		return mSessionState == SESSION_DISCONNECTED;
	}
	private void setConnected(int  senState){
		mSessionState = senState;
	}

	public  BluetoothPbapClient.ConnectionState  getState(){
		return mPbapClient.getState();
	}
	
	public int getCntedState() {
		return mSessionState;
	}
	private void cleanUp(){
		Log.d(TAG, "TEST: cleanUp() isConnected = " + isDisconcted());
		if(isDisconcted()){
			mPbapClientFlag = false;
			if(mPbapClient != null){
				mPbapClient = null;
			}
			if(mSetPathQueen != null){
				mSetPathQueen.clear();
				mSetPathQueen = null;
			}
			if(mHandlerThread != null) {
	                  mHandlerThread.quitSafely();
			    mHandlerThread = null;
			} 
			if(mPbapClientHandler != null){
				mPbapClientHandler = null;
			}

			if(mRemoteDevice != null){
				mRemoteDevice = null;
			}
		}
	} 

	private void notifyCntedStateChange(int senState) {
		Log.i(TAG, "notfi "+senState);
		setConnected(senState);
		  if(mHandler != null){
			  Message m  = mHandler.obtainMessage(1, senState);
			  mHandler.sendMessage(m);
		    }
	}

	public void connect() {
		if (isDisconcted()) {
			Log.d(TAG, "TEST: connect() ==> connect()");
			if(mPbapClient != null){
				setConnected(SESSION_CONNECTING);
				mPbapClient.connect();
			}
		}
	}

	public void disconnect() {
		if (isConnect()) {
			setConnected(SESSION_DISCONNECTING);
			mPbapClient.disconnect();
		}else if(isDisconcted()){
		       cleanUp();
		}
	}

	public void pullPhonebook(String pbName,Handler handler) {
			this.mHandler=handler;
			if(mPbapClient != null)
				mPbapClient.pullPhoneBook(pbName);
	}
	
//	public void pullPhonebook(String pbName,Handler handler,long filter,byte format) {
//			this.mHandler=handler;
//			if(mPbapClient != null)
//				mPbapClient.pullPhoneBook( pbName,  filter, format);
//	}

	public void getCallNumberName(String number) {
		mCurCallNumber = number;
		setPhonebook(BluetoothPbapClient.PB_PATH);
	}

	public void setPhonebook(String dts) {
		dts = dts.replaceFirst("\\.vcf$", "");

		if(DBG) Log.d(TAG, "TEST: setPhonebook() dts = " + dts);
		mSetPathQueen = new ArrayDeque<String>(Arrays.asList(dts.split("/")));
		if(mPbapClient != null)
			mPbapClient.setPhoneBookFolderRoot();
	}

	private void setPhonebookFloder(String fld) {
		if(mPbapClient != null)
			mPbapClient.setPhoneBookFolderDown(fld);

	}

	private void queryPhoneBookSize(String pbName){
		if(DBG) Log.d(TAG, "TEST: queryPhoneBookSize() pbName = " + pbName);
		if(mPbapClient != null)
              	mPbapClient.pullPhoneBookSize(pbName);
	}

       private void queryVcardListingSize(String folder){
		if(DBG) Log.d(TAG, "TEST: queryVcardListingSize() folder = " + folder);
		if(mPbapClient != null)
			mPbapClient.pullVcardListingSize(folder);
	}

	public void pullVcardListing(String folder,String strValue,int maxListCnt) {
		if(mPbapClient != null){
			mPbapClient.pullVcardListing(folder,
				BluetoothPbapClient.ORDER_BY_DEFAULT,
				BluetoothPbapClient.SEARCH_ATTR_NUMBER, strValue, maxListCnt, 0);
		}
	}

	private final class BluetoothPbapClientHandler extends Handler{
			BluetoothPbapClientHandler(Looper looper) {
	            super(looper);
	       }
		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg) {
			if(DBG) Log.d(TAG, "TEST: handleMessage()  msg.what = "+ msg.what);
			switch (msg.what) {
			case BluetoothPbapClient.EVENT_SESSION_CONNECTED:
				notifyCntedStateChange(SESSION_CONNECTED);
//				pullPhonebook(BluetoothPbapClient.PB_PATH);
				break;
			case BluetoothPbapClient.EVENT_SESSION_DISCONNECTED:
				notifyCntedStateChange(SESSION_DISCONNECTED);
				break;
			case BluetoothPbapClient.EVENT_PULL_PHONE_BOOK_DONE:
				ArrayList<VCardEntry> pbList = (ArrayList<VCardEntry>) msg.obj;
				
				  if(mHandler != null){
					  Message m  = mHandler.obtainMessage(0, pbList);
					  mHandler.sendMessage(m);
				    }
				  for(VCardEntry ve : pbList){
						
						 Log.d(TAG, "TEST: handleMessage()  ve = " + ve.toString());
				  }
				break;

			case BluetoothPbapClient.EVENT_PULL_VCARD_LISTING_DONE:

				break;
			case BluetoothPbapClient.EVENT_SET_PHONE_BOOK_DONE:
				Log.d(TAG, "handleMessage() set phone book success  = EVENT_SET_PHONE_BOOK_DONE");					

				break;
      case BluetoothPbapClient.EVENT_PULL_PHONE_BOOK_SIZE_DONE:
			       if(DBG) Log.d(TAG, "TEST: handleMessage()  = EVENT_PULL_PHONE_BOOK_SIZE_DONE msg.arg1 =  " + msg.arg1);
             break;
			case BluetoothPbapClient.EVENT_PULL_VCARD_LISTING_SIZE_DONE:
				if(DBG) Log.d(TAG, "TEST: handleMessage()  = EVENT_PULL_VCARD_LISTING_SIZE_DONE msg.arg1 =  " + msg.arg1);
				break;
		       case BluetoothPbapClient.EVENT_SESSION_AUTH_TIMEOUT:
			       break;
			case BluetoothPbapClient.EVENT_SESSION_AUTH_REQUESTED:
			       break;
		       case BluetoothPbapClient.EVENT_SET_PHONE_BOOK_ERROR:
			   break;
			case BluetoothPbapClient.EVENT_PULL_PHONE_BOOK_ERROR:
			   break;
			case BluetoothPbapClient.EVENT_PULL_VCARD_LISTING_ERROR:
			   break;
			case BluetoothPbapClient.EVENT_PULL_VCARD_ENTRY_ERROR:
			   break;
			case BluetoothPbapClient.EVENT_PULL_PHONE_BOOK_SIZE_ERROR:
			   break;
			case BluetoothPbapClient.EVENT_PULL_VCARD_LISTING_SIZE_ERROR:
			   break;
			default:
				Log.d(TAG, "TEST: handleMessage()  default msg.what =  " + msg.what);
				break;
			}
		}
	};
}
