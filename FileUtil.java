import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;




public class FileUtil {

   /**
     * 创建文件
     * @param path 父路径
     * @param fileName 文件名
     * @param isDeleteOldFile 如果文件存在，是否删除文件并重新创建
     * @return File 创建失败返回null
     */
    public static File createFile(String path, String fileName, boolean isDeleteOldFile){
        if (fileName == null || path == null)
            return null;
        File file = new File(path,fileName);
        try {
            if (file.exists()){
                if (isDeleteOldFile){
                    file.delete();
                    file.createNewFile();
                }else {
                    return file;
                }
            }else {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return file;
    }

    /***
     * 文件复制操作
     *
     * @param oldFilePath
     * @param newFilePath
     */
    public static void copyFile(String oldFilePath, String newFilePath) {
        File oldfile = new File(oldFilePath);
        if (!oldfile.exists() || !oldfile.isFile() || !oldfile.canRead()) {
            return;
        }
        try {
            int bytesum = 0;
            int byteread = 0;
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldFilePath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newFilePath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            e.printStackTrace();

        }
    }

    //刪除文件
    public static boolean deleteFile(String path) {
        if (TextUtils.isEmpty(path))
            return false;
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        return true;
    }

    public static void copyFromRaw2SD(Context context, int resId, String path) {
        try {
            if (!(new File(path)).exists()) {

                InputStream is = context.getResources().openRawResource(
                        resId);
                FileOutputStream fos = new FileOutputStream(path);
                byte[] buffer = new byte[8192];
                int count = 0;
                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
                is.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 图+ 获取文件类型编号
     *
     * @param filepath
     * @return
     */
    public static int getFileTypeNum(String filepath) {
        ArrayList<String> typeString = new ArrayList<>();
        typeString.add("txt");
        typeString.add("doc");
        typeString.add("docx");
        typeString.add("ppt");
        typeString.add("pptx");
        typeString.add("xls");
        typeString.add("xlsx");
        typeString.add("pdf");
        typeString.add("zip");
        typeString.add("rar");
        typeString.add("cab");
        typeString.add("gzip");
        typeString.add("iso");
        typeString.add("3gp");
        typeString.add("mp4");
        typeString.add("avi");
        typeString.add("rmvb");
        typeString.add("rm");
        typeString.add("asf");
        typeString.add("divx");
        typeString.add("mpg");
        typeString.add("mpeg");
        typeString.add("wmv");
        typeString.add("mkv");
        typeString.add("vob");
        typeString.add("mp3");
        typeString.add("wav");
        typeString.add("mid");
        typeString.add("adf");
        typeString.add("tti");
        typeString.add("amr");
        String extensionName = getExtensionName(filepath);
        for (int i = 0; i < typeString.size(); i++) {
            if (extensionName.equals(typeString.get(i))) {
                //1-3是文本语音图片类型
                return i + 4;
            }
        }
        //没找到，返回未识别类型
        return 100;
    }

    /*
    * Java文件操作 获取文件扩展名
    *
    */
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    /**
     * 从文件Uri获取文件路径
     * @param context
     * @param uri
     * @return
     */
    public static String getPathFromUri(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= 19;

        // DocumentProvider
        if (isKitKat && isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String ACTION_OPEN_DOCUMENT = "android.intent.action.OPEN_DOCUMENT";
    public static int Build_VERSION_KITKAT = 19;
    private static final String PATH_DOCUMENT = "document";

    /**
     * Test if the given URI represents a {@link } backed by a
     * {@link }.
     */
    private static boolean isDocumentUri(Context context, Uri uri) {
        final List<String> paths = uri.getPathSegments();
        if (paths.size() < 2) {
            return false;
        }
        if (!PATH_DOCUMENT.equals(paths.get(0))) {
            return false;
        }

        return true;
    }

    private static String getDocumentId(Uri documentUri) {
        final List<String> paths = documentUri.getPathSegments();
        if (paths.size() < 2) {
            throw new IllegalArgumentException("Not a document: " + documentUri);
        }
        if (!PATH_DOCUMENT.equals(paths.get(0))) {
            throw new IllegalArgumentException("Not a document: " + documentUri);
        }
        return paths.get(1);
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     *                      [url=home.php?mod=space&uid=7300]@return[/url] The value of
     *                      the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
