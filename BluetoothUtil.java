  /**
     * 蓝牙监听
     */
    private  BluetoothAdapter bluetoothAdapter;
    private BluetoothProfile mBluetoothProfile;
	private boolean mHasBlueTooth;
	private Context mContext;
	private AudioManager mAudioManager =  (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
	
    private IntentFilter makeFilter() {
        IntentFilter audioFilter = new IntentFilter();
		//必须注册监听两个ACTION，第一个是蓝牙连接状态（手机和蓝牙设备的连接状态），第二个是本地蓝牙开关的状态
		//如果只使用BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED 监听广播，则会接收不到“关闭本机蓝牙开关”的广播事件
		//但只是用BluetoothAdapter.ACTION_STATE_CHANGED 的话，很明显这时候蓝牙设备并未真正配对。
        audioFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        audioFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return audioFilter;
    }

    public static boolean getBlueTooth(Context context){
        return PreferenceUtils.getPrefBoolean(context, PrefenceConstant.BLUE_TOOCH, false);
    }

    private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(intent.getAction())) {    //蓝牙连接状态
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
                if (state == BluetoothAdapter.STATE_CONNECTED ) {
                    //连接或失联，切换音频输出（到蓝牙、或者强制仍然扬声器外放）
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device.getBluetoothClass().getMajorDeviceClass() != BluetoothClass.Device.Major.AUDIO_VIDEO)
                        return;

                    //mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    mAudioManager.startBluetoothSco();
                    mAudioManager.setBluetoothScoOn(true);
                    mAudioManager.setSpeakerphoneOn(false);
                  mHasBlueTooth = true;
                }else if (state == BluetoothAdapter.STATE_DISCONNECTED){
                    PreferenceUtils.setPrefBoolean(context.getApplicationContext(), PrefenceConstant.BLUE_TOOCH, false);
                }
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {    //本地蓝牙打开或关闭
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                if (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.STATE_TURNING_OFF) {
                    //断开，切换音频输出
                   mHasBlueTooth = false;
                }
            }
        }

    };

    /**
     * 检查在咔信启动前是否已经链接蓝牙
     * @param context
     */
    public void hasBluetoothConnected(Context context){
        bluetoothAdapter =  BluetoothAdapter.getDefaultAdapter();
        int a2dp = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP);
        int headset = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEADSET);
        int health = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.HEALTH);
        int flag = -1;
        if (a2dp == BluetoothProfile.STATE_CONNECTED) {
            flag = a2dp;
        } else if (headset == BluetoothProfile.STATE_CONNECTED) {
            flag = headset;
        } else if (health == BluetoothProfile.STATE_CONNECTED) {
            flag = health;
        }

        if (flag != -1) {
            bluetoothAdapter.getProfileProxy(context, new BluetoothProfile.ServiceListener() {
                @Override
                public void onServiceDisconnected(int profile) {

                }

                @Override
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    mBluetoothProfile = proxy;
                    List<BluetoothDevice> mDevices = proxy.getConnectedDevices();
                    if (mDevices != null && mDevices.size() > 0) {
                        for (BluetoothDevice device : mDevices) {
                            if (device.getBluetoothClass().getMajorDeviceClass() != BluetoothClass.Device.Major.AUDIO_VIDEO)
                                continue;
                            else {
                                 mHasBlueTooth = true;
                                break;
                            }
                        }
                    } else {
                        mHasBlueTooth = false;

                    }
                }
            }, flag);
        }

    }
	
	private void startBlueTooth(){
		//获取音频焦点
		//关于音频焦点 http://www.linuxidc.com/Linux/2012-04/57902.htm
		 mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
		  mAudioManager.startBluetoothSco();
          mAudioManager.setBluetoothScoOn(true);
          mAudioManager.setSpeakerphoneOn(false);
	}
	
	private void stopBlueTooth(){
		 if (mAudioManager != null && mHasBlueTooth && mAudioManager.isBluetoothScoOn()){
            mAudioManager.stopBluetoothSco();
			//释放音频焦点
            mAudioManager.abandonAudioFocus(null);
        }
	}