
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

/**
 *
 */
public class MainActivity extends AppCompatActivity {

    private AudioManager mAudioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        initHasHeadset();
        registerHeadsetPlugReceiver(this);
        hasBluetoothConnected(this);
        registerReceiver(mBluetoothReceiver, makeFilter());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterSensorListner();
        unRegisterHeadsetPlugReceiver(this);
        unregisterReceiver(mBluetoothReceiver);
    }

    private MediaPlayer media;
    private boolean isPlaying;
    public void play(final View view){
        view.setClickable(false);



        try {
            // 设置播放资源
            // 实例化MediaPlayer对象
            media = MediaPlayer.create(this, R.raw.wind);
//            if(!mCurModeNormal)
          //  media.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);

            // 设置播放结束时监听
            media.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                   isPlaying = false;
                    view.setClickable(true);
                    unregisterSensorListner();
                    mAudioManager.abandonAudioFocus(null);
                }
            });

            media.start();
            isPlaying = true;
            registerSensorListner();
            mAudioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }



    }


    /**
     * 蓝牙监听
     */
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothProfile mBluetoothProfile;
    private BluetoothA2dp mA2dp;
    private boolean mHasBluetooth;

    private IntentFilter makeFilter() {
        IntentFilter audioFilter = new IntentFilter();
        audioFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        audioFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        return audioFilter;
    }


    private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(intent.getAction())) {    //蓝牙连接状态
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
                if (state == BluetoothAdapter.STATE_CONNECTED ) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device.getBluetoothClass().getMajorDeviceClass() != BluetoothClass.Device.Major.AUDIO_VIDEO) {
                        return;
                    }
                    //不休眠100ms的话，mix2前两次语音播放无法从蓝牙声
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mAudioManager.setSpeakerphoneOn(false);
                   mHasBluetooth = true;
                } else if (state == BluetoothAdapter.STATE_DISCONNECTED) {
                    mHasBluetooth = false;
                }
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {    //本地蓝牙打开或关闭
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                if (state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.STATE_TURNING_OFF) {
                    //保存蓝牙耳机状态
                    mHasBluetooth = false;
                }
            }
        }
    };

    /**
     * 检查启动前是否已经链接蓝牙
     * @param context
     */
    public void hasBluetoothConnected(Context context) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        final int a2dp = bluetoothAdapter.getProfileConnectionState(BluetoothProfile.A2DP);
        int flag = -1;
        if (a2dp == BluetoothProfile.STATE_CONNECTED) {
            flag = a2dp;
        }
        if (flag != -1) {
            bluetoothAdapter.getProfileProxy(context, new BluetoothProfile.ServiceListener() {
                @Override
                public void onServiceDisconnected(int profile) {

                }

                @Override
                public void onServiceConnected(int profile, BluetoothProfile proxy) {
                    mBluetoothProfile = proxy;
                    if(profile == BluetoothProfile.A2DP){
                        mA2dp = (BluetoothA2dp) mBluetoothProfile; //转换
                    }
                    List<BluetoothDevice> mDevices = proxy.getConnectedDevices();
                    if (mDevices != null && mDevices.size() > 0) {
                        for (BluetoothDevice device : mDevices) {
                            if (device.getBluetoothClass().getMajorDeviceClass()
                                    != BluetoothClass.Device.Major.AUDIO_VIDEO)
                                continue;
                            else {
                                connectA2dp(device);//连接a2dp
                                mHasBluetooth = true;
                                break;
                            }
                        }
                    } else {
                        mHasBluetooth = false;
                    }
                }
            }, flag);
        }

    }

    /**
     * 连接a2dp
     * @param device
     */
    private void connectA2dp(BluetoothDevice device){
        try {
            if (mA2dp.getConnectionState(device) != BluetoothProfile.STATE_CONNECTED){
                //通过反射获取BluetoothA2dp中connect方法（hide的），进行连接。
                Method connectMethod =BluetoothA2dp.class.getMethod("connect",
                        BluetoothDevice.class);
                connectMethod.invoke(mA2dp, device);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /*************************耳机监听***************************************/

    public static boolean mHasHeadset = false; //是否插入耳机
    private BroadcastReceiver headsetPlugReceiver;

    private void registerHeadsetPlugReceiver(Context context) {
        headsetPlugReceiver = new HeadsetPlugReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        context.registerReceiver(headsetPlugReceiver, intentFilter);
    }

    private void unRegisterHeadsetPlugReceiver(Context context){
        if (headsetPlugReceiver != null ){
            context.unregisterReceiver(headsetPlugReceiver);
            headsetPlugReceiver = null;
        }
    }

    private void initHasHeadset(){
        mHasHeadset = mAudioManager.isWiredHeadsetOn();
    }


    /**
     * 实时监听耳机插入拔出状态
     */
    class HeadsetPlugReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("state")){
                if (intent.getIntExtra("state", 0) == 0){
                    mHasHeadset = false;
                    if (mAudioManager != null) {
                        mAudioManager.setSpeakerphoneOn(true);
                    }
                } else if (intent.getIntExtra("state", 0) == 1) {
                    mHasHeadset = true;
                    if (mAudioManager != null) {
                        mAudioManager.setSpeakerphoneOn(false);
                    }

                }

            }
        }
    }


    /**
     *
     * 距离传感器监听
     *
     */
    private SensorManager mSensorManager;
    private Sensor mSensor;

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float range = event.values[0];
            if (!mHasHeadset &&
                    !mHasBluetooth) {
                //没有有线耳机与蓝牙
                Log.i("range", "range :"+ range);
                if (range > 1 ) {
                    //距离大于阈值，外放
                    mAudioManager.setMode(AudioManager.MODE_NORMAL);
                    if (!mAudioManager.isSpeakerphoneOn()) {
                        mAudioManager.setSpeakerphoneOn(true);
                        Log.i("range", "isSpeakerphontOn："+ mAudioManager.isSpeakerphoneOn());
                    }
                } else {
                    //距离小于阈值，声音从听筒输出
                    mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    if (mAudioManager.isSpeakerphoneOn()) {
                        mAudioManager.setSpeakerphoneOn(false);
                        Log.i("range", "isSpeakerphontOn："+ mAudioManager.isSpeakerphoneOn());
                    }

                }
            } else if (!mHasBluetooth){
                if (mAudioManager.isSpeakerphoneOn()) {
                    //有耳机插入，关闭外放，声音从耳机输出
                    mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                    mAudioManager.setSpeakerphoneOn(false);
                }
            }else {
                //有蓝牙耳机连接
                mAudioManager.setMode(AudioManager.MODE_NORMAL);
                mAudioManager.setSpeakerphoneOn(false);
                if (mAudioManager.isBluetoothScoOn())
                    mAudioManager.setBluetoothScoOn(false);

            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private void registerSensorListner(){
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mSensorManager.registerListener(sensorEventListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregisterSensorListner(){
        mSensorManager.unregisterListener(sensorEventListener, mSensor);
    }

}


