package pl.krisbiketeam.accel2.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;

public class MySettings {
	
	private static SharedPreferences prefs;
	
	private static boolean settingsLoaded = false;
	
	private static boolean senseOnNotActive;
	public static boolean logScale;
	private static boolean changeAllScales;
	private static boolean writeToFlash;			//czy mamy zapis do pamiêci Flash czy w przeciwnym przypadku RAM
	private static boolean memoryWriteEnable;			//czy mamy zapisywaæ dane w trybie przesy³¹nia przez bluetooth
	private static boolean allowZoom;				
		
	public static final String SENSE_ON_NOT_ACTIVE = "senseOnNotActive";
	public static final String LOG_SCALE = "logScale";
	public static final String CHANGE_ALL_SCALES = "changeAllScales";
	public static final String WRITE_TO_FLASH = "writeToFlash";			//czy mamy zapis do pamiêci Flash czy w przeciwnym przypadku RAM
	public static final String MEMORY_WRITE_ENABLE = "memoryWriteEnable";			//czy mamy zapisywaæ dane w trybie przesy³¹nia przez bluetooth
	public static final String ALLOW_ZOOM = "allowZoom";
	

	//handler do g³ównej aplikacji
	//@Deprecated
	//private Handler mHandler;
	
	public MySettings(Context context){
		if(!settingsLoaded){
			prefs=PreferenceManager.getDefaultSharedPreferences(context);
			updatePrefs();
			settingsLoaded = true;
			//new ReadPreferences().execute(context);
		}	
			
	}
	@Deprecated
	public MySettings(Context context, Handler handle){
		//mHandler = handle;
		new ReadPreferences().execute(context);
	}
	
	/**
	 * aktualizuje ustawienia(preferences)
	 */
	public void updatePrefs(){
		senseOnNotActive = prefs.getBoolean(SENSE_ON_NOT_ACTIVE, false);
		logScale = prefs.getBoolean(LOG_SCALE, false);
		changeAllScales = prefs.getBoolean(CHANGE_ALL_SCALES, false);
		writeToFlash = prefs.getBoolean(WRITE_TO_FLASH, false);
		memoryWriteEnable = prefs.getBoolean(MEMORY_WRITE_ENABLE, false);
		allowZoom = prefs.getBoolean(ALLOW_ZOOM, false);
	}
	
	/**
	 * czy ma dalej odbieraæ dane z czujnika jak okno nie jest aktywne
	 * @return the senseOnNotActive
	 */
	public boolean isSenseOnNotActive() {
		return senseOnNotActive;
	}	
	/**
	 * sprawdza czy skala ma byæ logarytmiczna
	 * @return the logScale
	 */
	public boolean isLogScale() {
		return logScale;
	}
	/**
	 * sprawdza czy am zmieniaæ wszystkie skale na raz czy tylko t¹ wybran¹ do edycji
	 * @return the changeAllScales
	 */
	public boolean isChangeAllScales() {
		return changeAllScales;
	}
	/**
	 * sprawdza czy czy ma byæ zapis do FLASH czy RAM
	 * @return the logScale
	 */
	public boolean isWriteToFlash() {
		return writeToFlash;
	}
	/**
	 * sprawdza czy ma byæ zapis danych w trakcie wysy³ania danych przez bluetoot
	 * @return the logScale
	 */
	public boolean isMemoryWriteEnable() {
		return memoryWriteEnable;
	}
	/**
	 * sprawdza czy mo¿liwe jest zoomowanie wykresu
	 * @return the allowZoom
	 */
	public static boolean isAllowZoom() {
		return allowZoom;
	}
	
	
	
	//klasa do sprawdzenia wartoœæ w Preferences odnoœnie typu skali wykresu
	@Deprecated
	private class ReadPreferences extends AsyncTask<Context, Void, SharedPreferences> {
	     
		@Override
		protected SharedPreferences doInBackground(Context... context) {
			SharedPreferences mPrefs=PreferenceManager.getDefaultSharedPreferences(context[0]);
			return mPrefs;
		}
		@Override
		protected void onPostExecute(SharedPreferences mPrefs) {
			prefs = mPrefs;
			updatePrefs();
			settingsLoaded = true;
			//powiadamiamy g³ówn¹ aplikacjê ¿e odczytaliœmy preferencje
			//mHandler.sendEmptyMessage(BlueMSP430Activity.MESSAGE_PREFERENCES);
	    }

	 }
	
	
	
	/**
	 * ustawia skalê logarytmiczn¹
	 * @param logScale the logScale to set
	 */
	public void setLogScale(boolean mlogScale) {
		logScale = mlogScale;
		prefs.edit().putBoolean("logScale",logScale).apply();
	}
	/**
	 * ustawia czy zmieniaæ wszystkie skale na raz czy tylko t¹ wybran¹ do edycji
	 * @param changeAllScales the changeAllScales to set
	 */
	public void setChangeAllScales(boolean mchangeAllScales) {
		changeAllScales = mchangeAllScales;
		prefs.edit().putBoolean("changeAllScales",changeAllScales).apply();
	}
	/**
	 * ustawia zapis do FLASH lub RAM
	 * @param writeToFlash the writeToFlash to set
	 */
	public void setWriteToFlash(boolean mwriteToFlash) {
		writeToFlash = mwriteToFlash;
		prefs.edit().putBoolean("writeToFlash",writeToFlash).apply();
	}
	/**
	 * ustawia czy ma byæ zapis danych w trakcie wysy³ania danych przez bluetoot
	 * @param memoryWriteEnable the memoryWriteEnable to set
	 */
	public void setMemoryWriteEnable(boolean mmemoryWriteEnable) {
		memoryWriteEnable = mmemoryWriteEnable;
		prefs.edit().putBoolean("memoryWriteEnable",memoryWriteEnable).apply();
	}
	
}
