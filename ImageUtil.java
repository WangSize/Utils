
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ImageView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2016/4/27.
 */
public class ImageUtil {

    /**
     * 根据原图和变长绘制圆形图片
     *
     * @param source
     * @return
     */
    public static Bitmap createCircleImage(Bitmap source) {
        int min = source.getHeight() < source.getWidth() ? source.getHeight() : source.getWidth();
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        Bitmap target = Bitmap.createBitmap(min, min, Bitmap.Config.ARGB_8888);
        /**
         * 产生一个同样大小的画布
         */
        Canvas canvas = new Canvas(target);
        /**
         * 首先绘制圆形
         */
        canvas.drawCircle(min / 2, min / 2, min / 2, paint);
        /**
         * 使用SRC_IN
         */
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        /**
         * 绘制图片
         */
        canvas.drawBitmap(source, 0, 0, paint);
        return target;
    }

    public static Bitmap getBitmapFromView(View view) {
        Bitmap bitmap = null;
        try {
            int width = view.getWidth();
            int height = view.getHeight();
            if (width != 0 && height != 0) {
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                view.layout(0, 0, width, height);
                view.draw(canvas);
            }
        } catch (Exception e) {
            bitmap = null;
            e.getStackTrace();
        }
        return bitmap;
    }
	/**
     * 从View获取图片
     *
     * @param view
     * @return
     */
    public static Bitmap convertViewToBitmap(View view) {
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();

        return bitmap;
    }

    /**
     * 加载本地图片
     * @param url
     * @return
     */
    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);  ///把流转化为Bitmap图片

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 保存Bitmap
     */
    public static String saveBitmap(Bitmap bitmap, String outputpath) {
        saveBitmap(bitmap,outputpath,90);
        return outputpath;
    }

    public static String saveBitmap(Bitmap bitmap, String outputpath,int quality){
        FileOutputStream b = null;
        if (bitmap == null)
            return null;
        try {
            b = new FileOutputStream(outputpath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, b);// 把数据写入文件
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                b.flush();
                b.close();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return outputpath;
    }


    private static int MAXWIDTH = 1080;

    public static Bitmap circleImage(String srcPath, String outputPath) throws OutOfMemoryError {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        int be = 1;// be=1表示不缩放
        if (w >= h && w > MAXWIDTH) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / MAXWIDTH);
        } else if (w < h && h > MAXWIDTH) {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / MAXWIDTH);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        try {
            bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
            saveBitmap(bitmap, outputPath);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return bitmap;// 压缩好比例大小后再进行质量压缩
    }

    /**
     * 图片打水印
     *
     * @param path  载体图片
     * @param c     上下文
     * @param title 文字-在左上角嵌入文字，为空则不嵌入
     * @return Bitmap
     */
    public static String watermarkBitmap(String path, Context c, String title) {
        try{
            Bitmap src = BitmapFactory.decodeFile(path);
            if (src == null) {
                return null;
            }
            int w = src.getWidth();
            int h = src.getHeight();
//        Bitmap watermark = BitmapFactory.decodeResource(c.getResources(),
//                R.drawable.warning);
//        int ww = watermark.getWidth();
//        int hh = watermark.getHeight();
//        // 计算缩放比例
//        float scaleWidth = ((float) w / SCALE) / ww;
//        // float scaleHeight = ((float) w/SCALE) / hh;
//        // 取得想要缩放的matrix参数
//        Matrix matrix = new Matrix();
//        matrix.postScale(scaleWidth, scaleWidth);
//        // 得到新的图片
//        Bitmap newwatermark = Bitmap.createBitmap(watermark, 0, 0, ww, hh,
//                matrix, true);
            Bitmap newb = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);// 创建一个新的和SRC长度宽度一样的位图
            Canvas cv = new Canvas(newb);
            cv.drawBitmap(src, 0, 0, null);// 在 0，0坐标开始画入src
            // 加入图片
//        if (newwatermark != null) {
//            int nw = newwatermark.getWidth();
//            int nh = newwatermark.getHeight();
//            Log.d("cut", "w=" + w + ";nw=" + nw + ";h=" + h + ";nh=" + nh);
//            paint.setAlpha(70);
//            cv.drawBitmap(newwatermark, w - nw, h - nh, paint);// 在src的右下角画入水印
//        }
            //加入文字矩形背景


            // 加入文字
            if (title != null) {
                String familyName = "宋体";
                Typeface font = Typeface.create(familyName, Typeface.BOLD);
                TextPaint textPaint = new TextPaint();
                textPaint.setColor(Color.WHITE);
                textPaint.setTypeface(font);
                textPaint.setTextSize(24);
                // 这里是自动换行的
                StaticLayout layout = new StaticLayout(title, textPaint, w,
                        Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, true);
                Rect rect = new Rect();
                //返回包围整个字符串的最小的一个Rect区域
                textPaint.getTextBounds(title, 0, title.length(), rect);
                //画圆角矩形
                Paint p = new Paint();
                p.setARGB(77, 0, 0, 0);
//            p.setColor(Color.BLUE);// 设置颜色
                p.setStyle(Paint.Style.FILL);//充满
                p.setAntiAlias(true);// 设置画笔的锯齿效果
                cv.drawRect(w - rect.width()-10, h - rect.height()-10, w, h, p);//第二个参数是x半径，第三个参数是y半径
                //画文字
//            cv.translate(15,0);
//            layout.draw(cv);
                // 文字就加左上角算了

//            strwid = rect.width();
//            strhei = rect.height();
                cv.drawText(title, w - rect.width()-5, h-5, textPaint);
            }
            cv.save(Canvas.ALL_SAVE_FLAG);// 保存
            cv.restore();// 存储
            saveBitmap(newb, path);
            newb.recycle();
        }catch (Exception e){

        }
        return path;
    }

    public static void compressionImage(String path, String outputpath,int maxWidth,int maxHeight) {

        int degree = getBitmapDegree(path);
        L.e(ImageUtil.class, "Degree = " + degree);
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, newOpts);

        float width = newOpts.outWidth;
        float heigh = newOpts.outHeight;
        float x = 1;
        if (width > maxWidth || heigh > maxHeight) {
            if (width / heigh > maxWidth / maxHeight) {
                x = width / maxWidth;
            } else {
                x = heigh / maxHeight;
            }
        }
        Bitmap newbitmap = getImage(path, heigh / x, width / x);
        saveBitmapWithRecyle(rotateBitmapByDegree(newbitmap,degree), outputpath);
    }

    /**
     * 读取图片的旋转的角度
     *
     * @param path 图片绝对路径
     * @return 图片的旋转角度
     */
    public static int getBitmapDegree(String path) {
        int degree = 0;
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            ExifInterface exifInterface = new ExifInterface(path);
            // 获取图片的旋转信息
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * @Description 旋转图片一定角度
     * @param bitmap 要旋转的图片
     * @param degree 要旋转的角度
     * @return 旋转后的图片
     */
    public static Bitmap rotatePicture(final Bitmap bitmap, final int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizeBitmap;
    }

    /**
     * @Description 保存图片到指定路径
     * @param bitmap 要保存的图片
     * @param filePath 目标路径
     * @return 是否成功
     */
    public static boolean saveBmpToPath(final Bitmap bitmap, final String filePath) {
        if (bitmap == null || filePath == null) {
            return false;
        }
        boolean result = false; //默认结果
        File file = new File(filePath);
        OutputStream outputStream = null; //文件输出流
        try {
            outputStream = new FileOutputStream(file);
            result = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream); //将图片压缩为JPEG格式写到文件输出流，100是最大的质量程度
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close(); //关闭输出流
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 个人新增图片处理方法
     *
     * @author zhx
     * @d2015年7月3日
     */
    // 少占用内存方式直接获取压缩的图片
    // 该方法按比例缩放,输入宽高只是保证完整的图片放入输入的大小中
    public static Bitmap getImage(String srcPath, float hh, float ww) throws OutOfMemoryError {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        // 由于设置inJustDecodeBounds为true，因此执行下面代码后bitmap为空，只是获取到图片的宽高
        // 按照字面意思翻译,应该就是只获取轮廓
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        L.i("imageCompression", "w=" + w + "\n h=" + h + "\n ww=" + ww
                + "\n hh=" + hh);
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w >= h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        try{
            bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        }catch (OutOfMemoryError e){
            e.printStackTrace();
        }

        return bitmap;// 压缩好比例大小后再进行质量压缩
    }
    /**
     * 将图片按照某个角度进行旋转
     *
     * @param bm     需要旋转的图片
     * @param degree 旋转角度
     * @return 旋转后的图片
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;

        // 根据旋转角度，生成旋转矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
            L.e(ImageUtil.class,e.toString());
        } catch (Exception e){
            L.e(ImageUtil.class,e.toString());
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }

    public static void saveBitmapWithRecyle(Bitmap bitmap,String outputPath){
        saveBitmap(bitmap,outputPath,80);
        if(bitmap!=null && !bitmap.isRecycled())
            bitmap.recycle();
    }

    /**
     * 将彩色图转换为黑白图
     * @return 返回转换好的位图
     */
    public static Bitmap convertToBlackWhite(Bitmap bmp) {
        int width = bmp.getWidth(); // 获取位图的宽
        int height = bmp.getHeight(); // 获取位图的高
        int[] pixels = new int[width * height]; // 通过位图的大小创建像素点数组
        if(bmp == null)
            return null;
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);

                grey = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
            }
        }
        Bitmap newBmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        newBmp.setPixels(pixels, 0, width, 0, 0, width, height);

        Bitmap resizeBmp = ThumbnailUtils.extractThumbnail(newBmp, width,
                height);
        return resizeBmp;
    }

    /**
     * <br>功能简述:4.4及以上获取图片的方法
     * <br>功能详细描述:
     * <br>注意:
     * @param context
     * @param uri
     * @return
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPathFromUri(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
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
                final String[] selectionArgs = new String[] { split[1] };

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

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
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

    /**
     * @description 计算图片的压缩比率
     *
     * @param options 参数
     * @param reqWidth 目标的宽度
     * @param reqHeight 目标的高度
     * @return
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * @description 通过传入的bitmap，进行压缩，得到符合标准的bitmap
     *
     * @param src
     * @param dstWidth
     * @param dstHeight
     * @return
     */
    private static Bitmap createScaleBitmap(Bitmap src, int dstWidth, int dstHeight, int inSampleSize) {
         // 如果是放大图片，filter决定是否平滑，如果是缩小图片，filter无影响，我们这里是缩小图片，所以直接设置为false
        Bitmap dst = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false);
        if (src != dst) { // 如果没有缩放，那么不回收
            src.recycle(); // 释放Bitmap的native像素数组
        }
        return dst;
    }

    /**
     * @description 从Resources中加载图片
     *
     * @param res
     * @param resId
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 设置成了true,不占用内存，只获取bitmap宽高
        BitmapFactory.decodeResource(res, resId, options); // 读取图片长宽，目的是得到图片的宽高
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight); // 调用上面定义的方法计算inSampleSize值
        // 使用获取到的inSampleSize值再次解析图片
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeResource(res, resId, options); // 载入一个稍大的缩略图
        return createScaleBitmap(src, reqWidth, reqHeight, options.inSampleSize); // 通过得到的bitmap，进一步得到目标大小的缩略图
    }

    /**
     * @description 从SD卡上加载图片
     *
     * @param pathName
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampledBitmapFromFile(String pathName, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeFile(pathName, options);
        return createScaleBitmap(src, reqWidth, reqHeight, options.inSampleSize);
    }

    /**
     * 读取图片属性：旋转的角度 后置摄像头
     *
     * @param path
     *            图片绝对路径
     * @return degree 旋转的角度
     * @throws IOException
     */
    public static int gainPictureDegree(String path) throws Exception {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (Exception e) {
            throw (e);
        }

        return degree;
    }

    /**
     * 读取图片属性：旋转的角度 前置摄像头
     *
     * @param path
     *            图片绝对路径
     * @return degree 旋转的角度
     * @throws IOException
     */
    public static int gainPictureDegree1(String path) throws Exception {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_NORMAL:
                    degree = 270;
                    break;
            }
        } catch (Exception e) {
            throw (e);
        }

        return degree;
    }

    /**
     * 旋转图片
     *
     * @param angle
     *            角度
     * @param bitmap
     *            源bitmap
     * @return Bitmap 旋转角度之后的bitmap
     */
    public static Bitmap rotaingBitmap(int angle, Bitmap bitmap) {
        // 旋转图片 动作
        Matrix matrix = new Matrix();
        ;
        matrix.postRotate(angle);
        // 重新构建Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

}
