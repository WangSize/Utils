import android.annotation.SuppressLint;
import android.content.Context;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间工具类
 */

@SuppressLint("SimpleDateFormat")
public class TimeUtil {

    private static SimpleDateFormat formatBuilder;

    /**
     * API:getDate
     *
     * @param format 格式 ，例：MM--dd hh...
     * @return
     */
    public static String getDate(String format) {
        formatBuilder = new SimpleDateFormat(format);
        return formatBuilder.format(new Date());
    }

    /**
     * API:getdate
     *
     * @return
     */
    public static String getDate() {
        return getDate("MM-dd  hh:mm:ss");
    }

    /**
     * @param time
     * @param format
     * @return
     */
    public static String getDate(String time, String format) {
        if (time == null || time.isEmpty())
            return "";
        SimpleDateFormat format2 = new SimpleDateFormat(format);
        return format2.format(new Date(Long.parseLong(time)));
    }

    /**
     * API:getTime
     *
     * @param time
     * @return
     */
    public static String getTime(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd HH:mm");
        return format.format(new Date(time));
    }

    /**
     * API:getTime
     *
     * @return
     */
    public static String getTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(new Date());
    }

    public static String getTimeInAlubum(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        return format.format(new Date(time));
    }

    public static String getTimeDay() {
        long time = System.currentTimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("yy-MM-dd");
        return format.format(new Date(time));
    }

    public static String getTimeSecond() {
        long time = System.currentTimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("yy_MM_dd_HH_mm_ss");
        return format.format(new Date(time));
    }

    /**
     * API:getTime
     * yy_MM_dd_HH_mm
     *
     * @param time
     * @return
     */
    public static String getTime1(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yy_MM_dd_HH_mmss");
        return format.format(new Date(time));
    }

    /**
     * API:getHourAndMin
     *
     * @param time
     * @return
     */
    public static String getHourAndMin(long time) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(new Date(time));
    }

    /**
     * API : getMinAndSec
     *
     * @param time
     * @return
     */
    public static String getMinAndSec(long time) {
        SimpleDateFormat format = new SimpleDateFormat("mm:ss");
        return format.format(new Date(time));
    }

    /**
     * API:getChatTime
     *
     * @param timesamp
     * @return
     */
    public final static long MAX_LIMIT_TIME = 3 * 24 * 60 * 60 * 1000;

    public static String getChatTime(long timesamp, Context context) {
        String result = "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd");
        Date today = new Date(System.currentTimeMillis());
        Date otherDay = new Date(timesamp);
        int temp = 4;
        if (System.currentTimeMillis() - timesamp < MAX_LIMIT_TIME)
            temp = Integer.parseInt(sdf.format(today))
                    - Integer.parseInt(sdf.format(otherDay));

        switch (temp) {
            case 0:
                result = getHourAndMin(timesamp);
                break;
            case 1:
                result = "昨天" + getHourAndMin(timesamp);
                break;
            case 2:
                result = "前天" + getHourAndMin(timesamp);
                break;

            default:
                // result = temp + "天前 ";
                result = getTime(timesamp);
                break;
        }

        return result;
    }

    /**
     * 返回时间 格式为 x分钟前，X小时前
     *
     * @param btime
     * @return
     */
    public static String getTimeOfNews(long btime, Context context) {
        String result = "";
        //获取时间差
        long time_difference = System.currentTimeMillis() - btime;
        //转成秒
        time_difference = time_difference / 1000;
        if (time_difference < 60) {
            //小于60秒
            result = "刚刚";
        } else if (time_difference >= 60 && time_difference < 60 * 60) {
            //大于1分钟，小于1小时
            result = time_difference / 60 + "分钟前";
        } else if (time_difference >= 60 * 60 && time_difference < 60 * 60 * 24) {
            //大于1小时，小于24小时
            result = time_difference / 60 / 60 + "小时前";
        } else {
            result = getTime(btime, "MM-dd  hh:mm");
        }
        return result;
    }

    public static String getTime(long time, String format) {
        SimpleDateFormat format2 = new SimpleDateFormat(format);
        return format2.format(new Date(time));
    }

    /**
     * String日期转换为Long     * @param formatDate("yy-MM-dd HH:mm")
     *
     * @param date("2016-12-13 10:38")
     * @return
     * @throws ParseException
     */
    public static Long transferStringDateToLong(String formatDate, String date){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(formatDate);
            Date dt = sdf.parse(date);
            return dt.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return System.currentTimeMillis();
    }

    public static String secondTomin(long second) {
        long s = second % 60;
        long m = second / 60;
        String ss;
        String sm;
        if (s < 10) {
            ss = "0" + s;
        } else {
            ss = "" + s;
        }
        if (m < 10) {
            sm = "0" + m;
        } else {
            sm = "" + m;
        }
        return sm + ":" + ss;
    }


    public static String secondToHour(long second) {
        long h = second / 3600;
        long m = second % 3600 /60;
        long s = second % 60;
        String sh;
        String ss;
        String sm;
        if(h < 10){
            sh = "0" + h;
        }else{
           sh = h+"";
        }
        if (s < 10) {
            ss = "0" + s;
        } else {
            ss = "" + s;
        }
        if (m < 10) {
            sm = "0" + m;
        } else {
            sm = "" + m;
        }
        return sh + ":" + sm + ":" + ss;
    }
}
