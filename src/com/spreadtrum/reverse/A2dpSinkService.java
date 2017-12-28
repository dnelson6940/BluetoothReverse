package com.spreadtrum.reverse;

import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothA2dpSink;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAvrcp;
import android.bluetooth.BluetoothAvrcpController;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.RemoteViews;

public class A2dpSinkService extends Service {

    private static final String TAG = "liu ----A2dpSinkService";
    private Context mContext;
    private BluetoothAdapter mLocalAdapter;
    private AudioRecord recorder;
    private AudioTrack player;
    private int recorder_buf_size;
    private int player_buf_size;
    private boolean mThreadExitFlag = false;
    private RecordThread mRecordThread;
    private IntentFilter mIntentFilter;
    private final IBinder mBinder = new A2dpSinkServiceBinder(this);
    private RemoteViews mStatusBarViews = null;
    private static final String BLUETOOLS_VIEW= "com.spreadtrum.bluetoothtools.view";
    public static final int SINK_STATUS = 10;
    public BluetoothDevice mDevice;
    private SendCmdHandler mSendCmdHandler;
    private BluetoothAvrcpController mAvrcpCt;
    OnAudioFocusChangeListener mAudioFocusListener = null;
    private AudioManager mAudioManager = null;

    static final int SEND_COMMAND_RELEASE = 1;
    static final int  SEND_COMMAND_PRESS = 2;
    static final int SEND_COMMAND = 3;
    private List<BluetoothDevice> deviceList;


    
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "action: " + action);
            if (BluetoothA2dpSink.ACTION_PLAYING_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getExtra(BluetoothDevice.EXTRA_DEVICE);
                int playingState =  intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothA2dpSink.STATE_NOT_PLAYING);
                if(mDevice != device){
                mDevice = device;
             }
            if(playingState == BluetoothA2dpSink.STATE_PLAYING){
               if(!isPlaying()){
				   Log.d(TAG, " liu onReceive : audioPlay " + playingState );
                   audioPlay(); 
                  }
               }else if(playingState == BluetoothA2dpSink.STATE_NOT_PLAYING){
                     if(isPlaying()){
                        audioPause();
    				}
    			}
            }else if (BluetoothA2dpSink.ACTION_CONNECTION_STATE_CHANGED.equals(action)){
    			int state =  intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED);
    			BluetoothDevice device = (BluetoothDevice) intent.getExtra(BluetoothDevice.EXTRA_DEVICE);
    			if(mDevice != device){
    				mDevice = device;
    			}
    			if(state == BluetoothProfile.STATE_DISCONNECTED){
    				cleanAudioTrack();
    				stopNotification();
    				Log.d(TAG,"A2dpsink has Disconnected,cleanAudioTrack");
    			}
            }else if ("com.android.passthroughrsp".equals(action)){
            	int key =  intent.getIntExtra("key", 0);
            	int keystate =  intent.getIntExtra("keystate",BluetoothAvrcp.PASSTHROUGH_STATE_RELEASE);
            	if(keystate == BluetoothAvrcp.PASSTHROUGH_STATE_PRESS){
					Log.d(TAG,"liu SEND_COMMAND_RELEASE keystate = "+keystate );
                	mSendCmdHandler.sendMessage(mSendCmdHandler.obtainMessage(SEND_COMMAND_RELEASE, key));
            	}
            }else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
            	int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            	if(state == BluetoothAdapter.STATE_OFF){
            		stopSelf();
            	}
            	
            }
        }
    };

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.d(TAG, "onCreate");
		mContext = getApplicationContext();
		mLocalAdapter = BluetoothAdapter.getDefaultAdapter();

        recorder_buf_size = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        player_buf_size = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);

		mIntentFilter = new IntentFilter(BluetoothA2dpSink.ACTION_PLAYING_STATE_CHANGED);
		mIntentFilter.addAction(BluetoothA2dpSink.ACTION_CONNECTION_STATE_CHANGED);
		mIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		mIntentFilter.addAction("com.android.passthroughrsp");
        registerReceiver(mReceiver, mIntentFilter);
        
        mSendCmdHandler = new SendCmdHandler();
		if (mLocalAdapter != null) {
			Log.d(TAG, "getProfileProxy");
		    mLocalAdapter.getProfileProxy(mContext,
		    		mBluetoothProfileServiceListener,BluetoothProfile.AVRCP_CONTROLLER);
        }
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(SINK_STATUS);
        mAudioManager = ((AudioManager) getSystemService(Context.AUDIO_SERVICE));
		requestAudioFocus();
	}

	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		
        if(intent != null){
    		mDevice = (BluetoothDevice) intent.getExtra(BluetoothDevice.EXTRA_DEVICE);
    		Log.d(TAG,"Connected device: " + mDevice);
        }
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.d(TAG, "onDestroy");
		unregisterReceiver(mReceiver);
		cleanAudioTrack();
        if (mAudioFocusListener != null) {
            mAudioManager.abandonAudioFocus(mAudioFocusListener);
            mAudioFocusListener = null;
        }
		stopNotification();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	private void cleanAudioTrack() {
		audioPause();
		mThreadExitFlag = true;
		if (recorder != null) {
			recorder.release();
			recorder = null;
		}
		if (player != null) {
			player.release();
			player = null;
		}
		if (mRecordThread != null) {
			mRecordThread = null;
		}
	}

	private void initAudioTrack() {
        if (recorder == null) {
            recorder = new AudioRecord(
                    MediaRecorder.AudioSource.BLUETOOTH_A2DP, 
                    44100,
                    AudioFormat.CHANNEL_IN_STEREO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    recorder_buf_size);
            }

		if (player == null) {
			player = new AudioTrack(
					AudioManager.STREAM_MUSIC, 
					44100,
					AudioFormat.CHANNEL_OUT_STEREO,
					AudioFormat.ENCODING_PCM_16BIT,
					player_buf_size,
					AudioTrack.MODE_STREAM);
		}
	}

	public void audioPlay() {
		initAudioTrack();
		if (mRecordThread == null) {
			mThreadExitFlag = false;
			mRecordThread = new RecordThread();
			mRecordThread.start();
		}
		Log.d(TAG, "audioPlay");
		player.play();
		updateNotification();
		requestAudioFocus();
	}

	public void audioPause() {
		if (player != null) {
			Log.d(TAG, "audioPause");
			player.stop();
			updateNotification();
	        if (mAudioFocusListener != null) {
	            mAudioManager.abandonAudioFocus(mAudioFocusListener);
	            mAudioFocusListener = null;
	        }
		}
	}
	public boolean isPlaying(){
		if (player != null) {
			Log.d(TAG,"A2dpSinkService PlayState:" + player.getPlayState());
			if(player.getPlayState() == AudioTrack.PLAYSTATE_PLAYING){
				return true;
			}
		}
		return false;
	}
	
	public boolean sendCommand(int Command){
		
		if(mDevice == null){
			Log.d(TAG, "sendPlayPauseCmd, mDevice is null");
			return false;
		}
		if(mAvrcpCt == null){
			Log.d(TAG, "sendPlayPauseCmd, proxy is not connected");
			return false;
		}
		
		mSendCmdHandler.sendMessage(mSendCmdHandler.obtainMessage(SEND_COMMAND_PRESS, Command));
		return true;
	}
	
    private void updateNotification() {
        Log.i(TAG, "updateNotification()");
        mStatusBarViews = new RemoteViews(getPackageName(), R.layout.statusbar);
        mStatusBarViews.setImageViewResource(R.id.icon, R.drawable.stat_notify_musicplayer_icon);
        mStatusBarViews.setTextViewText(R.id.comment, getString(R.string.notification_comment));

        

        Notification status = new Notification();
        status.contentView = mStatusBarViews;
        status.flags |= Notification.FLAG_ONGOING_EVENT;
        status.icon = R.drawable.stat_notify_musicplayer;
        Intent intent = new Intent(BLUETOOLS_VIEW);
        intent.putExtra(BluetoothDevice.EXTRA_DEVICE, mDevice);
        Log.d(TAG,"updateNotification,device: " + mDevice);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        status.contentIntent = PendingIntent.getActivity(this, 0,intent, PendingIntent.FLAG_UPDATE_CURRENT);
        
        startForeground(SINK_STATUS, status);
    }
    protected void stopNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(SINK_STATUS);
        stopForeground(true);

    }
    
    private synchronized boolean requestAudioFocus() {
        if(mAudioFocusListener == null){
            mAudioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
                @Override
                public void onAudioFocusChange(int focusChange) {

                    Log.d(TAG, "AudioFocusChanged " + focusChange);
                    switch (focusChange) {
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        case AudioManager.AUDIOFOCUS_LOSS:
                        	if(sendCommand(BluetoothAvrcp.PASSTHROUGH_ID_PAUSE)){
                        		audioPause();
                        	}
                        break;
                        case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                        case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                        case AudioManager.AUDIOFOCUS_GAIN:
                        	if(sendCommand(BluetoothAvrcp.PASSTHROUGH_ID_PLAY)){
                        		audioPlay();
                        	}
                        break;
                        default:
                        break;
                    }
                }
            };
        }
        boolean success = false;
        int tryCount = 10;
        for (int i = 0; i < tryCount; i++) {
            success = mAudioManager.requestAudioFocus(mAudioFocusListener,
                    AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN) == AudioManager.AUDIOFOCUS_GAIN;//try it again
            if (!success && i < tryCount - 1) {
                Log.i(TAG, "audio focus request failed " + i);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            break;
        }

        return success;
    }


	class RecordThread extends Thread {
		@Override
		public void run() {
			byte[] buffer = new byte[recorder_buf_size];
			recorder.startRecording();
			while (true) {
				if (mThreadExitFlag == true) {
					break;
				}
				try {
					int res = recorder.read(buffer, 0, recorder_buf_size);
					if (res > 0) {
						byte[] tmpBuf = new byte[res];
						System.arraycopy(buffer, 0, tmpBuf, 0, res);
						player.write(tmpBuf, 0, tmpBuf.length);
					}
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
		}
	}
	
	private BluetoothProfile.ServiceListener mBluetoothProfileServiceListener =
			new BluetoothProfile.ServiceListener() {
		
		        @Override
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
		        	mAvrcpCt = (BluetoothAvrcpController) proxy;
		        	Log.d(TAG, "AvrcpController Profile Proxy Connected");
                }
		        
                @Override
                public void onServiceDisconnected(int profile) {
                	mAvrcpCt = null;
                	Log.d(TAG, "AvrcpController Profile Proxy Disconnected");
                }
    };
	
    private class SendCmdHandler extends Handler {

  

		@Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            	case SEND_COMMAND_PRESS:
            		int key_press = (Integer) msg.obj;
            		Log.d(TAG,"send press cmd : " + key_press);
				
            		
            		mAvrcpCt.sendPassThroughCmd(mDevice, key_press, BluetoothAvrcp.PASSTHROUGH_STATE_PRESS);
        		break;
            	case SEND_COMMAND_RELEASE:
            		//FIXME.  key no sync
            		int key_release = (Integer) msg.obj;
            		Log.d(TAG,"send release cmd : " + key_release);
            		mAvrcpCt.sendPassThroughCmd(mDevice, key_release, BluetoothAvrcp.PASSTHROUGH_STATE_RELEASE);
            	break;
            	case SEND_COMMAND:
            		//FIXME.  key no sync
            		int key = (Integer) msg.obj;
            		Log.d(TAG,"send  cmd : " + key);
            		mAvrcpCt.sendPassThroughCmd(mDevice, key, BluetoothAvrcp.PASSTHROUGH_STATE_PRESS);
            		mAvrcpCt.sendPassThroughCmd(mDevice, key, BluetoothAvrcp.PASSTHROUGH_STATE_RELEASE);
            	break;
            }
        }
    }

    public class A2dpSinkServiceBinder extends Binder{
        A2dpSinkService mService;
        A2dpSinkServiceBinder(A2dpSinkService service){
              mService = service;
        }
        public A2dpSinkService getService(){
			return mService;
		};
		public void isPlaying() throws RemoteException{
			mService.isPlaying();
		}
		public void audioPlay() throws RemoteException{
			Log.d(TAG, "audioPlay Binder");
			mService.audioPlay();
		}
		public void audioPause() throws RemoteException{
			Log.d(TAG, "audioPause Binder");
			mService.audioPause();
		}
		public boolean sendCommand(int cmd) throws RemoteException{
			Log.d(TAG, "audioPause Binder");
			return mService.sendCommand(cmd);
		}
		
	}
}
