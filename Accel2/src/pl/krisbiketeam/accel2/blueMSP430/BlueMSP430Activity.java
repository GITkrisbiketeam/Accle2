package pl.krisbiketeam.accel2.blueMSP430;

import pl.krisbiketeam.accel2.R;
import pl.krisbiketeam.accel2.blueMSP430.BlueMSP430Service.BlueMSP430SensorEventListener;
import pl.krisbiketeam.accel2.blueMSP430.BlueMSP430Service.LocalBinderBlueMSP430Service;
import pl.krisbiketeam.accel2.blueMSP430sensor.BlueMSP430Sensor;
import pl.krisbiketeam.accel2.blueMSP430sensor.BlueMSP430SensorEvent;
import pl.krisbiketeam.accel2.blueMSP430sensor.BlueMSP430_acc;
import pl.krisbiketeam.accel2.blueMSP430sensor.BlueMSP430_temp;
import pl.krisbiketeam.accel2.settings.MySettings;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class BlueMSP430Activity extends Activity implements BlueMSP430SensorEventListener{

	public static final int BLUEMSP430_SENSORSINFOTABACTIVITY = 0;
	
	// Debugging
	private static final String TAG = "BlueMSP430Activity";
	private static final boolean D = true;
	
	CheckBox mCheckBoxContSend;
	CheckBox mCheckBoxContSendNew;
	
	TextView mTextViewTemperature;
	TextView mTextViewCoreTemperature;
	TextView mTextViewSupplyVoltage;
	
	TextView mTextViewDevice;
	TextView mTextViewAddress;
	
	/**
     * do zewn�trznego czujnika
     */
    BlueMSP430Service mBlueMSP430Service;
    Intent mBlueMSP430ServiceIntent;
    
    private BlueMSP430 blueMSP430;
    
    private int blyeArrayIndexOf; 
    
    /**
     * wskazuje czy jeste�my powi�zani z BlueMSP430ServiceMy
     */
    boolean mBound;
	
	/**
     * odczytuje przechowuje i zapisuje ustawienia programu
     */
    MySettings mSetings;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (D) Log.d(TAG, "+ ON CREATE +");
        
        setContentView(R.layout.blue_msp430_activity);
      
        mCheckBoxContSend = (CheckBox)findViewById(R.id.blue_msp430_check_box_cont_send); 
		mCheckBoxContSendNew = (CheckBox)findViewById(R.id.blue_msp430_check_box_cont_send_new);
		
		mTextViewTemperature = (TextView) findViewById(R.id.blue_msp430_temperature);
		mTextViewCoreTemperature = (TextView) findViewById(R.id.blue_msp430_coreTemperature);
		mTextViewSupplyVoltage = (TextView) findViewById(R.id.blue_msp430_supplyVoltage);
		
		mTextViewDevice = (TextView) findViewById(R.id.blue_msp430_title_device_text);
		mTextViewAddress = (TextView) findViewById(R.id.blue_msp430_title_address_text);
		 
		//wczytujemy ustawienia programu
		mSetings = new MySettings(this.getApplicationContext());	
		
		mBlueMSP430ServiceIntent = new Intent(this, BlueMSP430Service.class);
		bindService(mBlueMSP430ServiceIntent, mBlueMSP430ServiceConnection, 0);//Context.BIND_AUTO_CREATE);
		
		blyeArrayIndexOf = this.getIntent().getIntExtra("Number", 0);
    }
		
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		if (D) Log.d(TAG, "+ ON START +");
			
        registerSensors();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		unregisterSensors();
		
		if (D) Log.d(TAG, "- ON STOP -");
        super.onStop();
	}

	@Override
	protected void onDestroy() {
		if (D) Log.d(TAG, "- ON CREATE -");
        
		// Unbind from the service
		if (mBound) {
			unbindService(mBlueMSP430ServiceConnection);
			mBound = false;
		}
		
		super.onDestroy();
	}

	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case BLUEMSP430_SENSORSINFOTABACTIVITY:
			// When BlueMSP430_sensorsInfoTabActivity returns new BlueMSP430_acc/temp data
			if (resultCode == Activity.RESULT_OK) {
				if(data.hasExtra(BlueMSP430_acc.CLASS_NAME))
					blueMSP430.mBlueMSP430_acc = (BlueMSP430_acc) data.getParcelableExtra(BlueMSP430_acc.CLASS_NAME);
				if(data.hasExtra(BlueMSP430_temp.CLASS_NAME))
					blueMSP430.mBlueMSP430_temp = (BlueMSP430_temp) data.getParcelableExtra(BlueMSP430_temp.CLASS_NAME);
				
				if(blueMSP430.getBluetoothConectionState() == BlueMSP430Service.STATE_CONNECTED){
					mBlueMSP430Service.send_all_registers(blueMSP430);
					Toast.makeText(this, "BlueMSP430_accInfoActivity written new data to sensors",
									Toast.LENGTH_SHORT).show();
				}
			}
			break;
		}
	}

	/**
	 * rejestrowuje nas�uchiwanie z czujnik�w temperatury napi�cia i drugiej temperatury
	 */
	private void registerSensors(){
		if(mBlueMSP430Service != null){
			mBlueMSP430Service.registerBlueMSP430Listener(this, 
					blueMSP430.mBlueMSP430SensorList.get(BlueMSP430Sensor.TYPE_AMBIENT_TEMPERATURE));
			mBlueMSP430Service.registerBlueMSP430Listener(this, 
					blueMSP430.mBlueMSP430SensorList.get(BlueMSP430Sensor.TYPE_CORE_TEMPERATURE));
			mBlueMSP430Service.registerBlueMSP430Listener(this, 
					blueMSP430.mBlueMSP430SensorList.get(BlueMSP430Sensor.TYPE_SUPPLY_VOLTAGE));
			
		}
	}
	/**
	 * wyrejestrowuje nas�uchiwanie z czujnik�w temperatury napi�cia i drugiej temperatury
	 */
	private void unregisterSensors(){
		if(mBlueMSP430Service != null){
			mBlueMSP430Service.unregisterOnBlueMSP430Listener(this, 
					blueMSP430.mBlueMSP430SensorList.get(BlueMSP430Sensor.TYPE_AMBIENT_TEMPERATURE));
			mBlueMSP430Service.unregisterOnBlueMSP430Listener(this, 
					blueMSP430.mBlueMSP430SensorList.get(BlueMSP430Sensor.TYPE_CORE_TEMPERATURE));
			mBlueMSP430Service.unregisterOnBlueMSP430Listener(this, 
					blueMSP430.mBlueMSP430SensorList.get(BlueMSP430Sensor.TYPE_SUPPLY_VOLTAGE));
		}
	}

	/** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mBlueMSP430ServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
        	if (D) Log.d(TAG, " ServiceConnected");
            
        	// We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinderBlueMSP430Service binder = (LocalBinderBlueMSP430Service) service;
            mBlueMSP430Service = binder.getService();
            
            blueMSP430 = mBlueMSP430Service.mBlueMSP430.get(blyeArrayIndexOf);
            
            mTextViewDevice.setText(blueMSP430.btDevice.getName());
            mTextViewAddress.setText(blueMSP430.btDevice.getAddress());
            
            if(blueMSP430.isCont_data_sending())
            	mCheckBoxContSend.setChecked(true);
            else
            	mCheckBoxContSend.setChecked(false);
            if(blueMSP430.isCont_data_sendingNew())
            	mCheckBoxContSendNew.setChecked(true);
            else
            	mCheckBoxContSendNew.setChecked(false);
            
            registerSensors();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        	if (D) Log.d(TAG, " ServiceDisconnected");
            
        	unregisterSensors();
        	mBound = false;
        }
    };
    
    @Override
	public void onBlueMSP430SensorValueChaneged(BlueMSP430SensorEvent arg0) {
    	if(arg0.sensor.getBtDevice() == this.blueMSP430.btDevice){
			switch (arg0.sensor.getType()){
				case BlueMSP430Sensor.TYPE_AMBIENT_TEMPERATURE:
					mTextViewTemperature.setText(Float.toString(arg0.values[0]));
					
					break;
				case BlueMSP430Sensor.TYPE_CORE_TEMPERATURE:
					mTextViewCoreTemperature.setText(Float.toString(arg0.values[0]));
					
					break;
				case BlueMSP430Sensor.TYPE_SUPPLY_VOLTAGE:
					mTextViewSupplyVoltage.setText(Float.toString(arg0.values[0]));
					
					break;
			}
    	}
	}


	@Override
	public void onBlueMSP430SensorRangeChaneged(BlueMSP430Sensor sensor) {
		// TODO Auto-generated method stub
		
	}
	
	
	public void blue_msp430_checkBoxContSend(View v){
		if(mBlueMSP430Service != null){
		//if(mCheckBoxContSend.isEnabled()){
			//w��czamy ci�g�e wysy�anie danych
			if(mCheckBoxContSend.isChecked()){
				mBlueMSP430Service.enableContSending(blueMSP430.btDevice);
			}
			//wy�aczamy nas�uchiwanie
			else{
				mBlueMSP430Service.disableContSending(blueMSP430.btDevice);
			}
		//}
		}
	}
	public void blue_msp430_checkBoxContSendNew(View v){
		if(mBlueMSP430Service != null){
		//if(mCheckBoxContSend.isEnabled()){
			//w��czamy ci�g�e wysy�anie danych
			if(mCheckBoxContSendNew.isChecked()){
				mBlueMSP430Service.enableContSendingNew(blueMSP430.btDevice);
			}
			//wy�aczamy nas�uchiwanie
			else{
				mBlueMSP430Service.disableContSendingNew(blueMSP430.btDevice);
			}
		//}
		}
	}
	
	
	public void blue_msp430_acctivity_button_start(View v){
		if(mBlueMSP430Service != null){
			mBlueMSP430Service.enableContSending(blueMSP430.btDevice);
			mCheckBoxContSendNew.setChecked(true);
		}		
	}
	public void blue_msp430_acctivity_button_stop(View v){
		if(mBlueMSP430Service != null){
			mBlueMSP430Service.disableContSending(blueMSP430.btDevice);
			mCheckBoxContSendNew.setChecked(false);
		}
	}
	public void blue_msp430_acctivity_button_start_new(View v){
		if(mBlueMSP430Service != null){
			mBlueMSP430Service.enableContSendingNew(blueMSP430.btDevice);
			mCheckBoxContSendNew.setChecked(true);
		}
	}
	public void blue_msp430_acctivity_button_stop_new(View v){
		if(mBlueMSP430Service != null){
			mBlueMSP430Service.disableContSendingNew(blueMSP430.btDevice);
			mCheckBoxContSendNew.setChecked(false);
		}
	}
	public void blue_msp430_acctivity_button_sensor_registers(View v){
		if(mBlueMSP430Service != null){
			Intent sensorsInfoTabActivity = new Intent(this, BlueMSP430SsensorsInfoTabActivity.class);
			sensorsInfoTabActivity.putExtra("CTRL", false);
			sensorsInfoTabActivity.putExtra(pl.krisbiketeam.accel2.blueMSP430sensor.BlueMSP430_acc.CLASS_NAME, blueMSP430.mBlueMSP430_acc);
			sensorsInfoTabActivity.putExtra(pl.krisbiketeam.accel2.blueMSP430sensor.BlueMSP430_temp.CLASS_NAME, blueMSP430.mBlueMSP430_temp);
			startActivityForResult(sensorsInfoTabActivity, BLUEMSP430_SENSORSINFOTABACTIVITY);
			
		}
	}



	
}
