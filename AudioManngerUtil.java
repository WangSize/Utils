
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;



public class AudioManngerUtil {

    private Context mContext;

    public AudioManngerUtil(Context context){
        this.mContext = context;
        initAudioManager();
    }

    /*************************耳机监听***************************************/
    private AudioManager mAudioManager;
    public static boolean mHasHeadSet = false; //是否插入耳机
    private BroadcastReceiver headsetPlugReceiver;
    private static HeadsetListener mHeadsetListener = null;

    /**
     * 初始化音频管理器和耳机状态
     */
    private void initAudioManager(){
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        mHasHeadSet = mAudioManager.isWiredHeadsetOn();
    }

    /**
     * 注册耳机监听
     */
    public void registerHeadsetPlugReceiver() {
        headsetPlugReceiver = new HeadsetPlugReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.HEADSET_PLUG");
        mContext.registerReceiver(headsetPlugReceiver, intentFilter);
    }

    /**
     * 注销耳机监听
     */
    public void unRegisterHeadsetPlugReceiver(){
        if (headsetPlugReceiver != null ){
            mContext.unregisterReceiver(headsetPlugReceiver);
            headsetPlugReceiver = null;
        }
    }

    /**
     * 监听耳机是否插入
     * @param headsetListener
     */
    public static void setHeadsetListener(HeadsetListener headsetListener){
        mHeadsetListener = headsetListener;
    }

    public interface HeadsetListener{
        void isWiredHeadsetOn(boolean on);
    }

    /**
     * 实时监听耳机插入拔出状态
     */
    class HeadsetPlugReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("state")){
                if (intent.getIntExtra("state", 0) == 0){
                    //耳机拔出
                    mHasHeadSet = false;
                    if (mAudioManager != null)
                        mAudioManager.setSpeakerphoneOn(true);
                } else if (intent.getIntExtra("state", 0) == 1){
                    //耳机插入
                    mHasHeadSet = true;
                    if (mAudioManager != null)
                        mAudioManager.setSpeakerphoneOn(false);
                    if(mHeadsetListener != null){
                        mHeadsetListener.isWiredHeadsetOn(true);
                    }
                }

            }
        }
    }
}
