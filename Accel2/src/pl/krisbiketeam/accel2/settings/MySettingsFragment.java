package pl.krisbiketeam.accel2.settings;

import pl.krisbiketeam.accel2.R;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;


public class MySettingsFragment extends PreferenceFragment  implements OnSharedPreferenceChangeListener {//implements OnSharedPreferenceChangeListener{

	// Debugging
	private static final String TAG = "SensorService";
	private static final boolean D = true;
			
	private MySettings mySettings;
	/* (non-Javadoc)
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (D) Log.d(TAG, "+ ON CREATE +");
		
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		mySettings = new MySettings(this.getActivity().getApplicationContext());
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (D) Log.d(TAG, "+ onSharedPreferenceChanged +");
		mySettings.updatePrefs();
		
		if (key.equals(MySettings.LOG_SCALE)) {
		        //Preference connectionPref = findPreference(key);
		        // Set summary to be the user-description for the selected value
		        //connectionPref.setSummary(sharedPreferences.getString(key, ""));
		}
		
	}
	
	@Override
	public void onResume() {
		if (D) Log.d(TAG, "+ ON RESUME +");
		
		super.onResume();
	    getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		if (D) Log.d(TAG, "- ON PAUSE -");
		
		super.onPause();
	    getPreferenceScreen().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(this);
	}
	
	
	
	/*
	CheckBoxPreference writeToFlashCheckBox;
	CheckBoxPreference memoryWriteEnableCheckBox;
	CheckBoxPreference changeAllScalesCheckBox;
	CheckBoxPreference senseOnNotActiveCheckBox;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.preferences);
		Context context = getApplicationContext();
	    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
	    settings.registerOnSharedPreferenceChangeListener(this);
	    writeToFlashCheckBox = (CheckBoxPreference)this.findPreference("writeToFlash");
	    memoryWriteEnableCheckBox = (CheckBoxPreference)this.findPreference("memoryWriteEnable");
	    changeAllScalesCheckBox = (CheckBoxPreference)this.findPreference("changeAllScales");
	    senseOnNotActiveCheckBox = (CheckBoxPreference)this.findPreference("senseOnNotActive");
	    
	    updateCeckBox();
	    
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		updateCeckBox();
	}
	
	private void updateCeckBox(){
		if(changeAllScalesCheckBox.isChecked())
	    	changeAllScalesCheckBox.setSummary(R.string.change_all_scales);
	    else
	    	changeAllScalesCheckBox.setSummary(R.string.change_one_scale);
	    
	    if(senseOnNotActiveCheckBox.isChecked())
	    	senseOnNotActiveCheckBox.setSummary(R.string.sense_on_not_active);
	    else
	    	senseOnNotActiveCheckBox.setSummary(R.string.sense_on_active);
	    
		if(writeToFlashCheckBox.isChecked())
	    	writeToFlashCheckBox.setSummary(R.string.write_to_flash);
	    else
	    	writeToFlashCheckBox.setSummary(R.string.write_to_ram);
	    
	    if(memoryWriteEnableCheckBox.isChecked())
	    	memoryWriteEnableCheckBox.setSummary(R.string.memory_write_enable);
	    else
	    	memoryWriteEnableCheckBox.setSummary(R.string.memory_write_disable);
	}
	*/
}
