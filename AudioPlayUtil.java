import android.media.MediaPlayer;
import android.text.TextUtils;
import android.util.Log;
import android.media.MediaPlayer.*;
import java.io.File;
import java.io.FileInputStream;

/**
 * Created by Administrator on 2017/12/8.
 */

public class AudioPlayUtil {

    private final String TAG = "AudioPlayUtil";

    private static AudioPlayUtil mAudioPlayUtil;
    private MediaPlayer mMediaPlayer;

    public static AudioPlayUtil getInstance() {
        if (mAudioPlayUtil == null) {
            synchronized (AudioPlayUtil.class){
                if (mAudioPlayUtil == null)
                    mAudioPlayUtil = new AudioPlayUtil();
            }
        }
        return mAudioPlayUtil;
    }
    private AudioPlayUtil(){

    }

    public boolean isPlaying(String path) {
        if (mMediaPlayer != null)
            return mMediaPlayer.isPlaying();
        else
            return false;
    }

    public void play(String path){
        play(path, null, null);
    }

    public void play(String path, OnCompletionListener onCompletionListener){
        play(path, onCompletionListener, null);
    }
    public void play(String path, OnPreparedListener onPreparedListener){
        play(path, null, onPreparedListener);
    }

    /**
     * 开始播放如果传入onPreparedListener ，要在回调方法onPrepared()调用mp.start（）
     *
     * @param sourcePath
     * @param onCompletionListener
     * @param onPreparedListener
     */
    public void play(String sourcePath, OnCompletionListener onCompletionListener, OnPreparedListener onPreparedListener){
        if (sourcePath == null || TextUtils.isEmpty(sourcePath)){
            Log.e(TAG, "资源文件路径不能为空！");
            return;
        }
        try {
            if (mMediaPlayer != null ) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        File file = new File(sourcePath);
        try {
            if (mMediaPlayer == null)
                mMediaPlayer = new MediaPlayer();
            // 设置播放资源
            mMediaPlayer.setDataSource(new FileInputStream(file).getFD());
            // 准备播放
            mMediaPlayer.prepareAsync();
            if (onPreparedListener != null){
                mMediaPlayer.setOnPreparedListener(onPreparedListener);
            }else {
                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        mMediaPlayer.start();
                    }
                });
            }
            if (onCompletionListener != null){
                mMediaPlayer.setOnCompletionListener(onCompletionListener);
            }else {
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                        mMediaPlayer =null;
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 暂停播放
     */
    public void pause(){
        if (mMediaPlayer != null && mMediaPlayer.isPlaying())
            mMediaPlayer.pause();
    }

    /**
     * 继续播放
     */
    public void resume(){
        if (mMediaPlayer != null)
             mMediaPlayer.start();
    }

    /**
     * 停止播放
     */
    public void stop(){
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

    }

    /**
     * 获取当前进度
     * @return
     */
    public int getCurrentPosition(){
        if (mMediaPlayer != null)
            return mMediaPlayer.getCurrentPosition();
        else
            return 0;
    }

    /**
     * 获取音频长度
     * @return
     */
    public int getDuring(){
        if (mMediaPlayer != null)
            return mMediaPlayer.getDuration();
        else
            return 0;
    }

    /**
     * 销毁MediaPlay，建议在使用AudioPlayUtil的界面退出时，调用此方法
     * 由于AudioPlayUtil设置成单例模式，如果MediaPlay处于pause状态，此时退出界面，而未调用此方法
     * MediaPlay将会一直存在，占用大量资源
     */
    public void clear() {
        try {
            if (mMediaPlayer != null ) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }finally {
            if (mMediaPlayer != null)
                mMediaPlayer = null;
        }
    }

}
