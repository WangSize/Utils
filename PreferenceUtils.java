import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * 偏好保存配置
 * 
 */

public class PreferenceUtils {

	/**
	 * API:hasKey
	 * 
	 * @param context
	 * @param key
	 * @return
	 */
	public static boolean hasKey(Context context, final String key) {
		return PreferenceManager.getDefaultSharedPreferences(context).contains(
				key);
	}

	/**
	 * API:clearPreference
	 * 
	 * @param context
	 * @param p
	 */
	public static void clearPreference(Context context,
			final SharedPreferences p) {
		final Editor editor = p.edit();
		editor.clear();
		editor.commit();
	}

	/**
	 * API:getPrefString
	 * 
	 * @param context
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static String getPrefString(Context context, String key,
			final String defaultValue) {
		final SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		return settings.getString(key, defaultValue);
	}

	/**
	 * API:setPrefString
	 * 
	 * @param context
	 * @param key
	 * @param value
	 */
	public static void setPrefString(Context context, final String key,
			final String value) {
		final SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		settings.edit().putString(key, value).commit();
	}

	/**
	 * API:getPrefBoolean
	 * 
	 * @param context
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static boolean getPrefBoolean(Context context, final String key,
			final boolean defaultValue) {
		final SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		return settings.getBoolean(key, defaultValue);
	}

	/**
	 * API:setPrefBoolean
	 * 
	 * @param context
	 * @param key
	 * @param value
	 */
	public static void setPrefBoolean(Context context, final String key,
			final boolean value) {
		final SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		settings.edit().putBoolean(key, value).commit();
	}

	/**
	 * API:setPrefInt
	 * 
	 * @param context
	 * @param key
	 * @param value
	 */
	public static void setPrefInt(Context context, final String key,
			final int value) {
		final SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		settings.edit().putInt(key, value).commit();
	}

	/**
	 * API:getPrefInt
	 * 
	 * @param context
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static int getPrefInt(Context context, final String key,
			final int defaultValue) {
		final SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		return settings.getInt(key, defaultValue);
	}

	/**
	 * API:setPrefFloat
	 * 
	 * @param context
	 * @param key
	 * @param value
	 */
	public static void setPrefFloat(Context context, final String key,
			final float value) {
		final SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		settings.edit().putFloat(key, value).commit();
	}

	/**
	 * API:getPrefFloat
	 * 
	 * @param context
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static float getPrefFloat(Context context, final String key,
			final float defaultValue) {
		final SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		return settings.getFloat(key, defaultValue);
	}

	/**
	 * API:setSettingLong
	 * 
	 * @param context
	 * @param key
	 * @param value
	 */
	public static void setSettingLong(Context context, final String key,
			final long value) {
		final SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		settings.edit().putLong(key, value).commit();
	}

	/**
	 * API:getSettingLong
	 * 
	 * @param context
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static long getPrefLong(Context context, final String key,
			final long defaultValue) {
		final SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(context);
		return settings.getLong(key, defaultValue);
	}

}
