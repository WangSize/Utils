import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.norcatech.guards.R;
import com.norcatech.guards.app.Config;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzoneShare;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXImageObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/5/8.
 */

public class ShareUtils {

	//以下三个字段都要先去注册获取
    public static final String WX_APP_ID = "wx7086a24a64a1abda";
    public static final String APP_SECRET = "d0b91370c0fbb84c51b42fa06f96e0f7";
    public static final String QQ_APP_ID = "1105398377";

    private IWXAPI iwxapi;
    private Tencent tencent;
	
	//分享类型 
	private int type;
    public static final int WECHAT = 0;
    public static final int WECHAT_GROUP = 1;
    public static final int QQ = 2;
    public static final int QQ_GROUP = 3;

    private Context mContext;

    private String title;
    private String msgs;
    private String url;
   

    public ShareUtils() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**要分享必须先注册(微信)*/
    public void regToWX(Context context) {

        iwxapi = WXAPIFactory.createWXAPI(context, WX_APP_ID);
        iwxapi.registerApp(WX_APP_ID);

    }

    /**要分享必须先注册(QQ)*/
    public void regToQQ(Context context) {
        tencent = Tencent.createInstance(QQ_APP_ID, context);
    }

    public IWXAPI getIwxapi() {
        return iwxapi;
    }

    public void setIwxapi(IWXAPI iwxapi) {
        this.iwxapi = iwxapi;
    }

    public Tencent getTencent() {
        return tencent;
    }

    public void setTencent(Tencent tencent) {
        this.tencent = tencent;
    }

    public String getWxAppId() {
        return WX_APP_ID;
    }

    public String getQqAppId() {
        return QQ_APP_ID;
    }
	
	/**
     * 初始化分享工具
     * @param context
     * @param title 分享的缩略信息的标题
     * @param msgs 分享的缩略信息的信息
     * @param url  分享的url
     * @param type 分享类型，qq、qq群、微信、微信群
     * @param listener
     */
    public void initShare(Context context, String title, String msgs, String url, int type, IUiListener listener) {
        this.title = title;
        this.msgs = msgs;
        this.url = url;
        this.type = type;
        this.mContext = context;
        switch (type) {
            case WECHAT:
            case WECHAT_GROUP:
                if(iwxapi.isWXAppInstalled()) {
                    shareWeChat();
                } else {
                    T.showShort(mContext, "未安装微信客户端"));
                }
                break;
            case QQ:
            case QQ_GROUP:
                if(isQQClientAvailable(mContext)) {
                    shareToQzone(listener);
                } else {
                    T.showShort(mContext, "未安装qq客户端");
                }
                break;
        }

    }

    /**
     * 分享url并根据type判断是朋友群还是好友
     */
    private void shareWeChat(Bitmap bmp) {
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = url;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;
        msg.description = msgs;
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 150, 150, true);
        bmp.recycle();
        msg.thumbData = WechatAndQQUtil.bmpToByteArray(thumbBmp, true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;

        req.scene = type == 0 ? SendMessageToWX.Req.WXSceneSession : SendMessageToWX.Req.WXSceneTimeline;
        iwxapi.sendReq(req);
    }

    /**
     * 微信分享图片
     *
     * @param context
     * @param path 图片完整路径
     */
    public void shareWeChatPic(Context context, String path) {
        Bitmap bmp = BitmapFactory.decodeFile(path);

        WXImageObject imgObj = new WXImageObject(bmp);
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;

        // 设置缩略图
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, 100, 100, true);
        bmp.recycle();
        msg.thumbData = bitmap2Bytes(thumbBmp,20);
        //msg.setThumbImage(thumbBmp);

        // 构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("img"); //transaction 字段用于唯一标识一个请求
        req.message = msg;
        req.scene = SendMessageToWX.Req.WXSceneSession;

        iwxapi.sendReq(req);
    }
    /**
     * Bitmap转换成byte[]并且进行压缩,压缩到不大于maxkb
     * @param bitmap
     * @param
     * @return
     */
    public  byte[] bitmap2Bytes(Bitmap bitmap, int maxkb) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
        int options = 100;
        while (output.toByteArray().length > maxkb&& options != 10) {
            output.reset(); //清空output
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, output);//这里压缩options%，把压缩后的数据存放到output中
            options -= 10;
        }
        return output.toByteArray();
    }


    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    /*QQ部分*/

    /**
     * 分享到QQ
     * @param activity
     * @param title
     * @param msg
     * @param url
     * @param iUiListener
     * @return
     */
    public void shareToQQ(Activity activity, String title, String msg, String url, IUiListener iUiListener) {
        Bundle qqParams = new Bundle();
        qqParams.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        qqParams.putString(QQShare.SHARE_TO_QQ_TITLE, title);
        qqParams.putString(QQShare.SHARE_TO_QQ_SUMMARY,  msg);
        qqParams.putString(QQShare.SHARE_TO_QQ_TARGET_URL,  url);
        qqParams.putString(QQShare.SHARE_TO_QQ_APP_NAME,  mContext.getString(R.string.app_name));
        tencent.shareToQQ(activity, qqParams, iUiListener);
    }

    /**分享到QQ空间*/
    public void shareToQzone(IUiListener uiListener){
        Bundle qzoneParams = new Bundle();
        qzoneParams.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        qzoneParams.putString(QzoneShare.SHARE_TO_QQ_TITLE, title);//必填
        qzoneParams.putString(QzoneShare.SHARE_TO_QQ_SUMMARY,  msgs);
        qzoneParams.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, url);//必填
        qzoneParams.putString(QQShare.SHARE_TO_QQ_APP_NAME,  mContext.getString(R.string.app_name));
        ArrayList<String> imageUrlList =new ArrayList<String>();
//        Uri uri = PermissionUtils.getUriForFile(mContext, new File(Config.IC_SHARE));
        imageUrlList.add(Config.IC_SHARE);
        qzoneParams.putString(QzoneShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, Config.IC_SHARE);
        if(type == QQ){
            tencent.shareToQQ((Activity)mContext, qzoneParams, uiListener);
        }else{
            tencent.shareToQzone((Activity)mContext, qzoneParams, uiListener);
        }


    }

    /**
     * 分享纯图片到QQ
     *
     * @param path 图片本地路径
     */
    public void shareImgToQQ(Context context, String path, IUiListener listener) {
        File file = new File(path);
        if ( !file.exists() ){
            return;
        }

        Bundle params = new Bundle();
        params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);// 设置分享类型为纯图片分享
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, path);// 需要分享的本地图片URL
        tencent.shareToQQ((Activity)context, params, listener );
    }

    /**
     * 系统自带的分享功能，分享图片
     * @param context
     * @param imagePath
     */
    public static void shareImage(Context context,String imagePath){
        Intent intent2=new Intent(Intent.ACTION_SEND);
        Uri uri=PermissionUtils.getUriForFile(context, new File(imagePath));
        intent2.putExtra(Intent.EXTRA_STREAM,uri);
        intent2.setType("image/*");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            intent2.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        context.startActivity(Intent.createChooser(intent2,"share"));
    }


    /**
     * 判断qq是否可用
     *
     * @param context
     * @return
     */
    public static boolean isQQClientAvailable(Context context) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals("com.tencent.mobileqq")) {
                    return true;
                }
            }
        }
        return false;
    }

}
