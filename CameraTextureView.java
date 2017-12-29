import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.TextureView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.List;

/**
 * Created by Administrator on 2017/12/27.
 * 获取CameraTextureView后，应先调用{@link #initCamera(Activity)}
 * 初始化
 */

public class CameraTextureView extends TextureView {

    private final String TAG = "CameraTextureView";

    private Activity mActivity;
    private IOrientationEventListener mOrientationEventListener ;


    private MediaRecorder mMediaRecorder;
    private Camera.Size preSize;
    private Camera mCamera; // 相机
    private Camera.Parameters mParameters;
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;// 1代表前置摄像头，0代表后置摄像头
    private String mVideoAbsolutePath;
    private RecordVideoCallback mRecordVideoCallback;
    private TakePhotoCallback mTakePhotoCallback;
    public  boolean mIsRecordingVideo;
    private boolean isFlashOpen = false;
    private String mPicOutputPath;

    public CameraTextureView(Context context) {
        super(context);

    }

    public CameraTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public CameraTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }


    private SurfaceTextureListener mTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera(surface, mCameraId);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            releaseCamera();

            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    /**
     * 初始化Camera，在activity获取CameraTextureView后调用
     *
     * @param activity
     */
    public void initCamera(Activity activity) {
        mActivity = activity;
        setSurfaceTextureListener(mTextureListener);
    }

    private void openCamera(SurfaceTexture surface, int cameraId) {
        try {
            mOrientationEventListener = new IOrientationEventListener(mActivity);
            mOrientationEventListener.enable();
            mCamera = Camera.open(cameraId);
            mParameters = mCamera.getParameters();

            setCameraDisplayOrientation(mActivity, mCameraId, mCamera);
            preSize = getCloselyPreSize(getWidth(), getHeight(), mParameters.getSupportedPreviewSizes());
            ;

            mParameters.setPreviewSize(preSize.width, preSize.height);
            //设置照片分辨率
            List<Camera.Size> sizes = mParameters.getSupportedPictureSizes();
            Camera.Size size = sizes.get(sizes.size() / 2);
            mParameters.setPictureSize(size.width, size.height);
            setFlash(isFlashOpen);
            mCamera.setParameters(mParameters);
            mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

            mCamera.setPreviewTexture(surface);// 设置相机预览
            mCamera.startPreview();// 开始预览

        } catch (RuntimeException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setFlash(boolean open) {
        Camera.Parameters parameters = mCamera.getParameters();
        if (open) {
            isFlashOpen = true;
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        } else {
            isFlashOpen = false;
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        }
        mCamera.setParameters(parameters);

    }

    public void takePicture(String outputPath, TakePhotoCallback takePhotoCallback) {
        // 分别对应原始图像，压缩图像，jpg
        mPicOutputPath = outputPath;
        mTakePhotoCallback = takePhotoCallback;
        mCamera.takePicture(null, null, pictureCallback);
    }

    public void startRecordingVideo(String outputPath, RecordVideoCallback recordVideoCallback) {
        //mOrientationEventListener.disable();
        mRecordVideoCallback = recordVideoCallback;
        if (outputPath == null || TextUtils.isEmpty(outputPath)) {
            Log.e(TAG, "the output path can not be null");
            return;
        }
        mVideoAbsolutePath = checkVideoOutputPath(outputPath);
        if (!isAvailable() || null == preSize || mCamera == null) {
            return;
        }
        try {
            setUpMediaRecorder();
            mIsRecordingVideo = true;
            mMediaRecorder.start();
            mRecordVideoCallback.onStartRecord();
        } catch (IOException e) {
            mRecordVideoCallback.onRecordError(e);
            e.printStackTrace();
        }

    }

    public void stopRecordingVideo() {
        mIsRecordingVideo = false;
        // Stop recording
        try {
            mMediaRecorder.stop();
        } catch (IllegalStateException e) {
            mMediaRecorder = null;
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.stop();
        }

        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder = null;

        if (mRecordVideoCallback != null)
            mRecordVideoCallback.onRecordSuccess(mVideoAbsolutePath);
        mRecordVideoCallback = null;
        mCamera.lock();

    }

    private Camera.Size getPictureSize() {
        Camera.Size mSize = null;
        Camera.Size maxSize = null;
        //参考SDK中的API，获取相机的参数：
        Camera.Parameters parameters = mCamera.getParameters();
        //获取摄像头支持的各种分辨率
        List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
        for (int i = 0; i < supportedPictureSizes.size(); i++) {
            float w = supportedPictureSizes.get(i).width;
            float h = supportedPictureSizes.get(i).height;
            if (h / w == 0.75) {
                if (mSize == null)
                    mSize = supportedPictureSizes.get(i);
                else {
                    if (mSize.height < supportedPictureSizes.get(i).height)
                        mSize = supportedPictureSizes.get(i);
                }
            }
            if (maxSize == null)
                maxSize = supportedPictureSizes.get(i);
            else {
                if (maxSize.height < supportedPictureSizes.get(i).height)
                    maxSize = supportedPictureSizes.get(i);
            }
        }
        if (mSize == null)
            return maxSize;
        else
            return mSize;
    }

    /**
     * 图片回调函数
     */
    Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile(mPicOutputPath);
            if (pictureFile == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                camera.startPreview();
                mTakePhotoCallback.takePhotoSuccess(pictureFile.getAbsolutePath());
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
                mTakePhotoCallback.takePhotoError(e);
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
                mTakePhotoCallback.takePhotoError(e);
            }
        }

    };


    private void releaseCamera() {
        if (mCamera != null) {
            try {
                mCamera.stopPreview();// 停掉原来摄像头的预览
                mCamera.release();// 释放资源
                mCamera = null;// 取消原来摄像头
            } catch (Exception e) {

            }
        }
        if (mOrientationEventListener != null)
            mOrientationEventListener.disable();
    }

    /**
     * 切换摄像头
     */
    public void shiftCamera() {
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();// 得到摄像头的个数

        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);// 得到每一个摄像头的信息
            if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                // 现在是后置，变更为前置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {// 代表摄像头的方位，CAMERA_FACING_FRONT前置
                    releaseCamera();
                    mCameraId = cameraInfo.facing;
                   openCamera(getSurfaceTexture(), mCameraId);
                    break;
                }
            } else {
                // 现在是前置， 变更为后置
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {// 代表摄像头的方位，CAMERA_FACING_FRONT前置
                    releaseCamera();
                    mCameraId = cameraInfo.facing;
                    openCamera(getSurfaceTexture(), mCameraId);
                    break;
                }
            }
        }
    }


    /**
     * 通过对比得到与宽高比最接近的预览尺寸（如果有相同尺寸，优先选择）
     *
     * @param surfaceWidth  需要被进行对比的原宽
     * @param surfaceHeight 需要被进行对比的原高
     * @param preSizeList   需要对比的预览尺寸列表
     * @return 得到与原宽高比例最接近的尺寸
     */
    public Camera.Size getCloselyPreSize(int surfaceWidth, int surfaceHeight, List<Camera.Size> preSizeList) {
        int reqTmpWidth;
        int reqTmpHeight;

        reqTmpWidth = surfaceHeight;
        reqTmpHeight = surfaceWidth;
        //先查找preview中是否存在与surfaceview相同宽高的尺寸
        for (Camera.Size size : preSizeList) {
            if ((size.width == reqTmpWidth) && (size.height == reqTmpHeight)) {
                return size;
            }
        }

        // 得到与传入的宽高比最接近的size
        float reqRatio = ((float) reqTmpWidth) / reqTmpHeight;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;
        Camera.Size retSize = null;
        for (Camera.Size size : preSizeList) {
            curRatio = ((float) size.width) / size.height;
            deltaRatio = Math.abs(reqRatio - curRatio);
            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio;
                retSize = size;
            }
        }

        return retSize;
    }

    /**
     * 此方法设置MediaRecord，可以通过修改MediaRecord参数
     * 改变录制视频的质量
     *
     * @throws IOException
     */
    private void setUpMediaRecorder() throws IOException {
        if (null == mActivity) {
            return;
        }

        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        //通过获取手机自带的关于摄像头的配置信息配置MediaRecord
        // 可以避免因为手动设置参数导致无法录制的情况
        // 一般情况使用CamcorderProfile.QUALITY_480P,这种配置录制480P的视频10s大概4-5m，视频质量与大小兼顾
        //如果想要视频更小，可以再手动设置视频比特率为较小的值
        // QUALITY_QVGA清晰度较差，不过视频很小，一般10S才几百K
        // 判断有没有这个手机有没有这个参数
        if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P)) {
            CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
            // 下面这行注释掉的代码手动设置较小的比特率，录制出来10S的视频，大概1.2M，清晰度有些差，
//            profile.videoBitRate = mPreviewSize.getWidth() * mPreviewSize.getHeight();
            mMediaRecorder.setProfile(profile);
        } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P)) {
            CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_720P);
            profile.videoBitRate = preSize.width * preSize.height;
            mMediaRecorder.setProfile(profile);
        } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_QVGA)) {
            mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_QVGA));
        } else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_CIF)) {
            mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_CIF));
        } else {
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setVideoEncodingBitRate(2500000);
            mMediaRecorder.setVideoFrameRate(20);
            mMediaRecorder.setVideoSize(preSize.width, preSize.height);
        }

        mMediaRecorder.setOutputFile(mVideoAbsolutePath);
        mMediaRecorder.setPreviewDisplay(new Surface(getSurfaceTexture()));
        if (mCameraId ==  Camera.CameraInfo.CAMERA_FACING_BACK)
            mMediaRecorder.setOrientationHint(90);
        else
            mMediaRecorder.setOrientationHint(270);
        mMediaRecorder.prepare();
    }


    /**
     * 检查视频的路径
     *
     * @param outputPath
     * @return
     */
    private String checkVideoOutputPath(String outputPath) {
        File file = new File(outputPath).getParentFile();
        if (file.exists()){
            if (!outputPath.endsWith(".mp4"))
                return outputPath+".mp4";
        } else {
            file.mkdirs();
            if (!outputPath.endsWith(".mp4"))
               outputPath = outputPath+".mp4";
        }

        File file1 = new File(outputPath);
        if (!file1.exists())
            try {
                file1.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

        return outputPath;
    }


    private File getOutputMediaFile(String outputPath) {

    File file = new File(outputPath).getParentFile();
    if (file.exists()){
        if (!outputPath.endsWith(".jpg"))
            outputPath = outputPath+".jpg";
    } else {
        file.mkdirs();
        if (!outputPath.endsWith(".jpg"))
            outputPath = outputPath+".jpg";
    }

    File file1 = new File(outputPath);
    if (!file1.exists())
            try {
        file1.createNewFile();
    } catch (IOException e) {
        e.printStackTrace();
    }

    return file1;
    }

    /**
     * 调整预览方向
     *
     * @param activity
     * @param cameraId
     * @param camera
     */
    public void setCameraDisplayOrientation(Activity activity,
                                            int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    /**
     * 调整照片旋转角度
     */
    class IOrientationEventListener extends OrientationEventListener {

        public IOrientationEventListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (ORIENTATION_UNKNOWN == orientation) {
                return;
            }
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraId, info);
            orientation = (orientation + 45) / 90 * 90;
            int rotation = 0;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                rotation = (info.orientation - orientation + 360) % 360;
            } else {
                rotation = (info.orientation + orientation) % 360;
            }
            if (null != mCamera ) {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setRotation(rotation);
                mCamera.setParameters(parameters);
            }
        }
    }

    public interface RecordVideoCallback {
        void onStartRecord();

        void onRecordSuccess(String outputPath);

        void onRecordError(Exception e);
    }

    public interface TakePhotoCallback {
        void takePhotoSuccess(String photoPath);

        void takePhotoError(Exception e);
    }

}
