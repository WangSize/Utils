import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 全选没获取到的提示全在父类做显示了 所以只需要做权限的调用就好，如要改提示内容下面的switch里改就行
 *类中用到BuildConfig类是gradle生成的类，在使用时导入即可
 */

public class PermissionUtils {

    private static final String TAG = "PermissionUtils";
    public static final int NORMAL = 10000; // 不关心回调的话用这个
    public static final int WRITE_EXTERNAL_STORAGE = 1;
    public static final int READ_PHONE_STATE = 1000;
    public static final int ACCESS_FINE_LOCATION = 3;
    public static final int RECORD_AUDIO = 4;
    public static final int CAMERA = 5;
    public static final int SYSTEM_ALERT_WINDOW = 101;
    public static final int ACTION_MANAGE_WRITE_SETTINGS = 102;



    /**
     * @param activity    activity
     * @param permissions 权限数组
     * @param requestCode 申请码
     * @return true 有权限  false 无权限
     */
    public static boolean checkAndApplyfPermissionActivity(Activity activity, String[] permissions, int requestCode) {
        int sdk = Build.VERSION.SDK_INT;
        if (sdk >= Build.VERSION_CODES.M) {
            permissions = checkPermissions(activity, permissions);
            if (permissions != null && permissions.length > 0) {
                ActivityCompat.requestPermissions(activity, permissions, requestCode);
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    /**
     * @param mFragment   fragment
     * @param permissions 权限数组
     * @param requestCode 申请码
     * @return true 有权限  false 无权限
     */
    public static boolean checkAndApplyfPermissionFragment(Fragment mFragment, String[] permissions, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissions = checkPermissions(mFragment.getActivity(), permissions);
            if (permissions != null && permissions.length > 0) {
                if (mFragment.getActivity() != null) {
                    mFragment.requestPermissions(permissions, requestCode);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    /**
     * @param context     上下文
     * @param permissions 权限数组
     * @return 还需要申请的权限
     */
    private static String[] checkPermissions(Context context, String[] permissions) {
        if (permissions == null || permissions.length == 0) {
            return new String[0];
        }
        ArrayList<String> permissionLists = new ArrayList<>();
        permissionLists.addAll(Arrays.asList(permissions));
        for (int i = permissionLists.size() - 1; i >= 0; i--) {
            if (ContextCompat.checkSelfPermission(context, permissionLists.get(i)) == PackageManager.PERMISSION_GRANTED) {
                permissionLists.remove(i);
            }
        }

        String[] temps = new String[permissionLists.size()];
        for (int i = 0; i < permissionLists.size(); i++) {
            temps[i] = permissionLists.get(i);
        }
        return temps;
    }


    /**
     * 检查申请的权限是否全部允许
     */
    public static boolean checkPermission(int[] grantResults) {
        if (grantResults == null || grantResults.length == 0) {
            return true;
        } else {
            int temp = 0;
            for (int i : grantResults) {
                if (i == PackageManager.PERMISSION_GRANTED) {
                    temp++;
                }
            }
            return temp == grantResults.length;
        }
    }

    /**
     * 没有获取到权限的提示
     *
     * @param permissions 权限名字数组
     */
    public static void showPermissionsToast(Activity activity, @NonNull String[] permissions) {
            permissions = checkPermissions(activity, permissions);
            for (String permission : permissions) {
                L.d(TAG, permissions.toString());
                showPermissionToast(activity, permission);
            }
    }

    /**
     * 没有获取到权限的提示
     *
     * @param permission 权限名字
     */
    private static void showPermissionToast(Activity activity, @NonNull String permission) {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            //用户勾选了不再询问,提示用户手动打开权限
            switch (permission) {
                case Manifest.permission.CAMERA:
                    toast(activity, "相机权限已被禁止，请在应用管理中打开权限");
                    break;
                case Manifest.permission.READ_EXTERNAL_STORAGE:
                case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    toast(activity, "文件权限已被禁止，请在应用管理中打开权限");
                    break;
                case Manifest.permission.RECORD_AUDIO:
                    toast(activity, "录制音频权限已被禁止，请在应用管理中打开权限");
                    break;
                case Manifest.permission.ACCESS_FINE_LOCATION:
                    toast(activity, "位置权限已被禁止，请在应用管理中打开权限");
                    break;
                case Manifest.permission.READ_PHONE_STATE:
                    toast(activity, "识别码权限被禁止，请在应用管理中打开权限，否则将无法使用大卫兵");
                    break;
                case Manifest.permission.SYSTEM_ALERT_WINDOW:
                    toast(activity, "悬浮窗权限已被禁止，请在应用管理中打开权限");
                    break;
                case Manifest.permission.BODY_SENSORS:
                    toast(activity, "传感器权限已被禁止，请在应用管理中打开权限");
                    break;
                case Manifest.permission.READ_CONTACTS:
                case Manifest.permission.WRITE_CONTACTS:
                    toast(activity, "读取联系人权限被禁止,请在应用管理中打开权限");
                    break;
            }
        } else {
            //用户没有勾选了不再询问,拒绝了权限申请
            switch (permission) {
                case Manifest.permission.CAMERA:
                    toast(activity, "没有相机权限");
                    break;
                case Manifest.permission.READ_EXTERNAL_STORAGE:
                case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    toast(activity, "没有文件读取权限");
                    break;
                case Manifest.permission.RECORD_AUDIO:
                    toast(activity, "没有录制音频权限");
                    break;
                case Manifest.permission.ACCESS_FINE_LOCATION:
                    toast(activity, "没有位置权限");
                    break;
                case Manifest.permission.READ_PHONE_STATE:
                    toast(activity, "没有手机识别码权限,请允许，否则将无法使用大卫兵");
                    activity.sendBroadcast(new Intent(PreferenceContant.LOCK_CLOSE_APP_BROADCAST));
                    break;
                case Manifest.permission.SYSTEM_ALERT_WINDOW:
                    toast(activity, "没有悬浮窗权限");
                    break;
                case Manifest.permission.BODY_SENSORS:
                    toast(activity, "没有传感器权限");
                    break;
                case Manifest.permission.READ_CONTACTS:
                case Manifest.permission.WRITE_CONTACTS:
                    toast(activity, "没有读取联系人权限");
                    break;
            }
        }
    }

    private static void toast(Activity activity, String msg){
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 去应用权限管理界面
     */
    public static void gotoPermissionManager(Context context) {
        Intent intent;
        ComponentName comp;
        //防止刷机出现的问题
        try {
            switch (Build.MANUFACTURER) {
                case "Huawei":
                    intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
                    comp = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");
                    intent.setComponent(comp);
                    context.startActivity(intent);
                    break;
                case "Meizu":
                    intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
                    context.startActivity(intent);
                    break;
                case "Xiaomi":
                    String rom = getSystemProperty("ro.miui.ui.version.name");
                    if ("v5".equals(rom)) {
                        Uri packageURI = Uri.parse("package:" + context.getApplicationInfo().packageName);
                        intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                    } else {//if ("v6".equals(rom) || "v7".equals(rom)) {
                        intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
                        intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
                        intent.putExtra("extra_pkgname", context.getPackageName());
                    }
                    context.startActivity(intent);
                    break;
                case "Sony":
                    intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
                    comp = new ComponentName("com.sonymobile.cta", "com.sonymobile.cta.SomcCTAMainActivity");
                    intent.setComponent(comp);
                    context.startActivity(intent);
                    break;
                case "OPPO":
                    intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
                    comp = new ComponentName("com.color.safecenter", "com.color.safecenter.permission.PermissionManagerActivity");
                    intent.setComponent(comp);
                    context.startActivity(intent);
                    break;
                case "LG":
                    intent = new Intent("android.intent.action.MAIN");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
                    comp = new ComponentName("com.android.settings", "com.android.settings.Settings$AccessLockSummaryActivity");
                    intent.setComponent(comp);
                    context.startActivity(intent);
                    break;
                case "Letv":
                    intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
                    comp = new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.PermissionAndApps");
                    intent.setComponent(comp);
                    context.startActivity(intent);
                    break;
                default:
                    getAppDetailSettingIntent(context);
                    break;
            }
        } catch (Exception e) {
            getAppDetailSettingIntent(context);
        }
    }

    /**
     * 检测系统弹出权限
     * @param cxt
     * @param req
     * @return
     */
    @TargetApi(23)
    public static boolean checkSettingAlertPermission(Object cxt, int req) {
        if (cxt instanceof Activity) {
            Activity activity = (Activity) cxt;
            if (!Settings.canDrawOverlays(activity.getBaseContext())) {
                L.i(TAG, "Setting not permission");

                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, req);
                return false;
            }
        } else if (cxt instanceof Fragment) {
            Fragment fragment = (Fragment) cxt;
            if (!Settings.canDrawOverlays(fragment.getActivity())) {
                L.i(TAG, "Setting not permission");

                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + fragment.getActivity().getPackageName()));
                fragment.startActivityForResult(intent, req);
                return false;
            }
        } else {
            throw new RuntimeException("cxt is net a activity or fragment");
        }

        return true;
    }

    /**
     * WRITE_SETTINGS 权限
     * @param cxt
     * @param req
     * @return
     */
    @TargetApi(23)
    public static boolean checkSettingSystemPermission(Object cxt, int req) {
        if (cxt instanceof Activity) {
            Activity activity = (Activity) cxt;
            if (!Settings.System.canWrite(activity)) {
                L.i(TAG, "Setting not permission");

                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, req);
                return false;
            }
        } else if (cxt instanceof Fragment) {
            Fragment fragment = (Fragment) cxt;
            if (!Settings.System.canWrite(fragment.getContext())) {
                L.i(TAG, "Setting not permission");

                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + fragment.getContext().getPackageName()));
                fragment.startActivityForResult(intent, req);
                return false;
            }
        } else {
            throw new RuntimeException("cxt is net a activity or fragment");
        }

        return true;
    }

    /**
     * 获取系统属性值
     */
    public static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            L.e(TAG, "Unable to read sysprop " + propName + ex);
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    L.e(TAG, "Exception while closing InputStream" + e);
                }
            }
        }
        return line;
    }

    /**
     *
     * @param context
     * @param fileName
     * @return
     */
    public static Intent getIntent(Context context, String fileName){
        if(TextUtils.isEmpty(fileName)){
            return null;
        }
        File file = new File(fileName);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
                takePictureIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri fileUri = FileProvider.getUriForFile(context, "com.norcatech.guards.fileprovider", file);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
//                takePictureIntent.setDataAndType(fileUri, "application/vnd.android.package-archive");
                return takePictureIntent;
            } else {
                return null;
            }
        } else {
            Uri u = Uri.fromFile(file);
            takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, u);
            return takePictureIntent;
        }
    }

    /**
     * 获取uri的办法
     * @param context
     * @param file
     * @return
     */
    public static Uri getUriForFile(Context context, File file) {
        Uri fileUri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            fileUri = getUriForFile24(context, file);
        } else {
            fileUri = Uri.fromFile(file);
        }
        return fileUri;
    }

    public static Uri getUriForFile24(Context context, File file) {
        Uri fileUri = android.support.v4.content.FileProvider.getUriForFile(context,
                context.getPackageName() + ".fileprovider",
                file);
        return fileUri;
    }

    /**
     * 安裝apk记得调用geturiforfile得到uri 和这个方法得到intentdataandtype
     * @param context
     * @param intent
     * @param type
     * @param file
     * @param writeAble
     */
    public static void setIntentDataAndType(Context context,
                                            Intent intent,
                                            String type,
                                            File file,
                                            boolean writeAble) {
        if (Build.VERSION.SDK_INT >= 24) {
            intent.setDataAndType(getUriForFile(context, file), type);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (writeAble) {
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        } else {
            intent.setDataAndType(Uri.fromFile(file), type);
        }
    }

    //以下代码可以跳转到应用详情，可以通过应用详情跳转到权限界面(6.0系统测试可用)
    public static void getAppDetailSettingIntent(Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }
        launchApp(context, localIntent);
    }

    /**
     * 安全的启动APP
     */
    public static boolean launchApp(Context ctx, Intent intent) {
        if (ctx == null)
            throw new NullPointerException("ctx is null");
        try {
            ctx.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }


}
