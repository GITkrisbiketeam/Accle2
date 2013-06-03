package pl.krisbiketeam.accel2;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import pl.krisbiketeam.accel2.SensorService.LocalBinder;
import pl.krisbiketeam.accel2.drawView.DrawView;
import pl.krisbiketeam.accel2.drawView.DrawViewMultiple;
import pl.krisbiketeam.accel2.drawView.DrawViewMultipleFromFile;
import pl.krisbiketeam.accel2.drawView.DrawViewTimeSyncMultiple;
import pl.krisbiketeam.accel2.drawView.MyLog;
import pl.krisbiketeam.accel2.drawView.MyLin;
import pl.krisbiketeam.accel2.file.ReadFromFile;
import pl.krisbiketeam.accel2.file.ReadFromFileData;
import pl.krisbiketeam.accel2.file.SaveToFile;
import pl.krisbiketeam.accel2.settings.MySettings;
import pl.krisbiketeam.accel2.settings.MySettingsActivity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Accel2 extends Activity {

	// Debugging
	private static final String TAG = "MainActivity";
	private static final boolean D = true;
	
	// Intent request codes
	//private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int SAVED_FILES_LIST = 2;
	private static final int EDIT_PREFERENCES = 3;
	
	
	// Messages from SensorService
	public static final int SENSOR_DATA_RECEIVED = 1;
	public static final int SENSOR_INIT_CHANGE = 2;
	
	public static final int MESSAGE_READ_FROM_FILE = 10;
	
	
	SensorService mSensorService;
	MySensorManager mMySensorManager;
	boolean mBound = false;
	Intent intent;
	
	//TODO:tymczasowe
	static TextView editText;
	//TODO:tymczasowe
	static MySensorEvent mMySensorEvent;
	//TODO:tymczasowe
	static MySensor mSensor;
	
	
	//klasa z danymi odczytanymi z pliku przechowuje takï¿½e pierwszy punkt wyï¿½witlany na wykresie
	ReadFromFileData mReadFromFileData = null;
	
	ReadFromFile mReadFromFile;
	
	ProgressDialog progressDialog;
	

    /**
     * odczytuje przechowuje i zapisuje ustawienia programu
     */
    MySettings mSetings;
	
	/**
	 * lista wykresï¿½w
	 */
	static List<DrawView> mDrawViews = new ArrayList<DrawView>();
	
	/**
	 * LinearLayout z wykresami
	 */
	LinearLayout mLinearView;									//LinearLayout
	
	/**
	 * Selected DrawView in CreateContextMenu
	 */			
	DrawView mContextMenuDrawView;
	
	/**
	 * flaga przechowujï¿½ca stan zarejestrowania Recivera Bluetoot
	 */
	//private boolean registerBluetootReceiver = false;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (D) Log.d(TAG, "+ ON CREATE +");
        
        setContentView(R.layout.activity_main);
      
        //start SensorService
        intent = new Intent(this, SensorService.class);
		startService(intent);
		
		editText = (TextView) findViewById(R.id.text_view);	
		 
		
		//tworzy layout z wykresami
		mLinearView = (LinearLayout) findViewById(R.id.drawing_planes);		//LinearLayout
		
		// zarejestruj przechwytywanie dï¿½ugiego klikniï¿½cia do utweorzenia contextMenu
        //registerForContextMenu(mLinearView);
		
		//wczytujemy ustawienia programu
		mSetings = new MySettings(this.getApplicationContext());	
    }
    
    
    /* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		if (D) Log.d(TAG, "+ ON START +");
        
		if (!mBound) {
			bindService(intent, mConnection, 0);//Context.BIND_AUTO_CREATE);
			if(mSensorService!= null)
				if(mSensorService.isRunning())
					mBound = true;
        }
		super.onStart();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		if (D) Log.d(TAG, "- ON STOP -");
        
		// Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        super.onStop();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		if (D) Log.d(TAG, "- ON DESTROY -");
        
		if(mSensorService!= null)
			if(!mSensorService.isReceiving())
				//if(!mSensorService.isRunning())
					//mSensorService.stopMe();
					stopService(intent);
			
		
        super.onDestroy();
	}

	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		if(mSensorService!= null)
			if(mSensorService.isReceiving()){
				new AlertDialog.Builder(this)
				.setTitle("Stop listen to sensors and exit?")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						mSensorService.stopMeRunning();
						closeThisAcctivity();
					}
				})	
				.setNegativeButton("No",new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						closeThisAcctivity();
					}
				})	
				.show();
				return;
			}
			
		super.onBackPressed();		
	}
	
	private void closeThisAcctivity(){
		super.finish();
	}
	/***************************************             OPTIONS MENU            **********************************************/
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_options, menu);
        return true;
    }
	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int tmp = item.getItemId();
		switch (tmp){//item.getItemId()) {
		case R.id.menu_add_draw:
			adDrawPlaneUI();
			return true;
		case R.id.menu_remove_draw:
			removeDrawPlaneUI();
			return true;
		case R.id.menu_read_from_file:
			readFileSensorsUI();
			return true;
		case R.id.menu_save_to_file:
			saveMultipleSensorsUI();
			return true;
		case R.id.menu_bt_connect:
			if(mSensorService != null)
				mSensorService.selectBluetoothDeviceActivity();
			return true;
			
		case R.id.menu_bt_disconnect:
			if(mSensorService != null)
				mSensorService.disconnectBluetoothDeviceUI();
			return false;
			
		case R.id.menu_settings:
			startActivityForResult(new Intent(this, MySettingsActivity.class),EDIT_PREFERENCES);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
    
	/***************************************             CONTEXT MENU            **********************************************/
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main_context, menu);
		
		if(v instanceof DrawView){
			mContextMenuDrawView = (DrawView)v;
			
			if(mContextMenuDrawView.getmMyScale() instanceof MyLog)
				menu.findItem(R.id.contextMenuScaleLog).setChecked(true);
			else if(mContextMenuDrawView.getmMyScale() instanceof MyLin)
				menu.findItem(R.id.contextMenuScaleLin).setChecked(true);
			if(mContextMenuDrawView.isRecorded()){
				menu.findItem(R.id.contextMenuRecord).setVisible(false);
				menu.findItem(R.id.contextMenuStopRecord).setVisible(true);
			}
			else{
				menu.findItem(R.id.contextMenuRecord).setVisible(true);
				menu.findItem(R.id.contextMenuStopRecord).setVisible(false);
			}
		}
		//super.onCreateContextMenu(menu, v, menuInfo);
	}
	/* (non-Javadoc)
	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onContextItemSelected(MenuItem menuItem) {
		
		switch (menuItem.getItemId()) {
		case R.id.contextMenuRecord:
			//TODO: dodaï¿½ opcje co by pokazywaï¿½a jakiï¿½ znaczek ï¿½e dany wykres jest zapisywany jakieï¿½ mrugajï¿½ce kï¿½ko
			saveSensor(mContextMenuDrawView.getMySensor());
			return true;
		case R.id.contextMenuStopRecord:
			//TODO: nie wiem czy to dziaï¿½a
			for(SaveToFile item: mSensorService.mMySensorSaveList){
				if(item.getmMySensor().getName() == mContextMenuDrawView.getMySensor().getName()){
					stopSaveSensor(item);
					break;
					}
			}
			return true;
		case R.id.contextMenuScaleLin:
			//Zmieniamy skale tylko wybranego View
			mContextMenuDrawView.scaleLin();
			return true;
		case R.id.contextMenuScaleLog:
			//Zmieniamy skale tylko wybranego View
			mContextMenuDrawView.scaleLog();
			return true;
		case R.id.contextMenuAvaregeDraw:
			//((BlueMSP430DrawSensorsReadFromFile) mBlueMSP430DrawViews.get(info.position)).avaregeDraw();
			return true;
		case R.id.contextMenuDeleteDraw:
			//kasuje wybrany wykres
			//removeDrawSensor(mContextMenuDrawView.getMySensor());			//ta funkcja kasuje pierwsze wyst¹pienie czujnika na liœcie czasem Ÿle dzia³a
			removeDrawSensor(mLinearView.indexOfChild(mContextMenuDrawView));
			
			return true;
		default:
			//return false;
			return super.onContextItemSelected(menuItem);
		}
		
	}

	/**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

	/**
	 * The Handler that gets information back from the SensorService
	 **/
	@SuppressLint("HandlerLeak")
	class IncomingHandler extends Handler {
	    @Override
		public void handleMessage(Message msg) {
			//if (D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
			switch (msg.what) {
			case SENSOR_DATA_RECEIVED:
				/*if(msg.obj instanceof MySensorEvent){
					mMySensorEvent = (MySensorEvent) msg.obj;
					editText.setText(mMySensorEvent.values[0]+"");
					for(DrawView item: mDrawViews){
						item.sensorChanged(mMySensorEvent);
					}
				}*/
				break;
			case SENSOR_INIT_CHANGE:
				/*if(msg.obj instanceof MySensor){
					mSensor = (MySensor) msg.obj;
					for(DrawView item: mDrawViews){
						item.sensorInit(mSensor);
					}
				}*/
				break;
			case MESSAGE_READ_FROM_FILE:
				/*
				 * if(findBlueMSP430DrawSensorsReadFromFile() == null){
					mBlueMSP430DrawViews.add(new BlueMSP430DrawSensorsReadFromFile(getBaseContext(), null));
					adapter.notifyDataSetChanged();
					for(BlueMSP430DrawSensors item: mBlueMSP430DrawViews){
						item.requestLayout();
					}
				}
				*/
				
				addDrawSensorFromFile((ReadFromFileData)msg.obj);
				
				/*
				BlueMSP430DrawSensorsReadFromFile mBlueMSP430DrawViewReadFromFile = (BlueMSP430DrawSensorsReadFromFile) findBlueMSP430DrawSensorsReadFromFile();
				//nie odnaleziono DrawView z odczytanymi danymi z pliku
				if(mBlueMSP430DrawViewReadFromFile == null) break;
				
				//ustawiamy maksymelne zakresy resolution i odbudowujemy skale
				mBlueMSP430DrawViewReadFromFile.initDrawView(mReadFromFileData);
				mReadFromFile = null;
				*/
				break;
			default:
                super.handleMessage(msg);

			}			
		}
	};

	    
	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		/*case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				mSensorService.blueMSP430Connect(data);
				//if(mBlueMSP430ServiceMy.mBluetoothService == null)
				//	mBlueMSP430ServiceMy.setupBluetooth();
				//mBlueMSP430ServiceMy.connectDevice(data, false);
			}
			break;*/
		case SAVED_FILES_LIST:
			if (resultCode == Activity.RESULT_OK) {
				mReadFromFile = (ReadFromFile) new ReadFromFile(this, data.getStringExtra("fileName"), mMessenger).execute(data.getStringExtra("fileName"));
			}
			break;
		
		}
		
	}

	/** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
        	if (D) Log.d(TAG, " ServiceConnected");
            
        	// We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            mSensorService = binder.getService();
            mMySensorManager = new MySensorManager(mSensorService);
            mBound = true;
            
            mSensorService.mSensorServiceMessenger = mMessenger;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        	if (D) Log.d(TAG, " ServiceDisconnected");
            
        	mBound = false;
        }
    };
    /** Called when the user touches the button1 */
	public void main_acctivity_button(View view) {
		switch (view.getId()){
		case R.id.button1:
			if(mSensorService != null)
				mSensorService.msp430ActivityUI();
			
			//Intent blueMSP430Activity = new Intent(this, BlueMSP430Activity.class);
			//startActivity(blueMSP430Activity);
			
			//if (mBound)
			//	if(mSensorService!=null)
			//		mSensorService.startForegroundNotification();
			//bindService(intent, mConnection, 0);//ontext.BIND_AUTO_CREATE);
			
			break;
		case R.id.button2:
			// No to odczytujemy dane
			if(externalStorageAvailable()){
				Intent sensorsInfoTabActivity = new Intent(this, pl.krisbiketeam.accel2.file.SavedFilesListActivity.class);
				startActivityForResult(sensorsInfoTabActivity, SAVED_FILES_LIST);
			}
			else{
				Toast.makeText(getApplicationContext(), "External storage not available", Toast.LENGTH_SHORT).show();
			}
			
			//if (mBound)
			//	if(mSensorService!=null)
			//		mSensorService.stopForegroundNotification();
			
			// Unbind from the service
	        //if (mBound) {
	        //    unbindService(mConnection);
	        //    mBound = false;
	        //}
			break;
		case R.id.button3:
			addRemoveMultipleDrawPlanesUI();
			break;
		case R.id.button4:
			saveMultipleSensorsUI();
			break;
		}
	}
	
	/** Called when the user touches the button1 */
	public void main_acctivity_button1(View view) {
		
		if(mSensorService != null)
			mSensorService.msp430ActivityUI();
		
		//Intent blueMSP430Activity = new Intent(this, BlueMSP430Activity.class);
		//startActivity(blueMSP430Activity);
		
		//if (mBound)
		//	if(mSensorService!=null)
		//		mSensorService.startForegroundNotification();
		//bindService(intent, mConnection, 0);//ontext.BIND_AUTO_CREATE);
		
	}
	/** Called when the user touches the button2 */
	public void main_acctivity_button2(View view) {
		
		// No to odczytujemy dane
		if(externalStorageAvailable()){
			Intent sensorsInfoTabActivity = new Intent(this, pl.krisbiketeam.accel2.file.SavedFilesListActivity.class);
			startActivityForResult(sensorsInfoTabActivity, SAVED_FILES_LIST);
		}
		else{
			Toast.makeText(getApplicationContext(), "External storage not available", Toast.LENGTH_SHORT).show();
		}
		
		//if (mBound)
		//	if(mSensorService!=null)
		//		mSensorService.stopForegroundNotification();
		
		// Unbind from the service
        //if (mBound) {
        //    unbindService(mConnection);
        //    mBound = false;
        //}
	}
	
	/** Called when the user touches the button4 */
	public void main_acctivity_button3(View view) {
		addRemoveMultipleDrawPlanesUI();
	}
	/** Called when the user touches the button4 */
	public void main_acctivity_button4(View view) {
		saveMultipleSensorsUI();
	}

	/**
	 * tworzy pojawiajï¿½ce siï¿½ okienko z czujnikami jakie moga byï¿½ nasï¿½uchiwane
	 */
	public void adDrawPlaneUI(){
		CharSequence[] items = new CharSequence[mSensorService.mMySensorList.size()];
		int i = 0;
		for(MySensor item: mSensorService.mMySensorList)
			items[i++] = item.getName();
		
		new AlertDialog.Builder(this)
		.setTitle("Which Draw to add")
		//.setView(editView)
		.setItems(items, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				addDrawSensor(mSensorService.mMySensorList.get(which));
				
			}
		})
		.show();
	}	
	/**
	 * tworzy pojawiajï¿½ce siï¿½ okienko z nasï¿½uchiwanymi czujnikami jakie moï¿½ï¿½a skasowaï¿½ z wyï¿½wietlania
	 */
	public void removeDrawPlaneUI(){
		int i = 0;
		//CharSequence[] items = new CharSequence[mSensorService.mMySensorRegisteredList.size()];
		CharSequence[] items = new CharSequence[mDrawViews.size()];
		//for(MySensor item: mSensorService.mMySensorRegisteredList)
		for(DrawView item: mDrawViews)
			items[i++] = item.getMySensor().getName();
	
		new AlertDialog.Builder(this)
		.setTitle("Which Draw to remove")
		//.setView(editView)
		.setItems(items, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				//removeDrawSensor(mSensorService.mMySensorRegisteredList.get(which));
				//removeDrawSensor(mDrawViews.get(which).getSensor());
				removeDrawSensor(which);
				
			}
		})
		.show();
	}
	/**
	 * tworzy pojawiajï¿½ce siï¿½ okienko z checklistï¿½ z czujnikami jakie moga byï¿½ nasï¿½uchiwane 
	 * moï¿½liwoï¿½ï¿½ wybrania kilku naraz i usuniï¿½cia
	 */
	public void addRemoveMultipleDrawPlanesUI(){
		CharSequence[] items = new CharSequence[mSensorService.mMySensorList.size()];
		boolean[] checkedItems = new boolean[mSensorService.mMySensorList.size()];
		int i = 0;
		for(MySensor item: mSensorService.mMySensorList)
			items[i++] = item.getName();
		
		for(DrawView item: mDrawViews){
		//for(MySensor item: mSensorService.mMySensorRegisteredList){
			i = 0;
			for(MySensor itemList: mSensorService.mMySensorList){
				if(itemList.getName() == item.getMySensor().getName())
					checkedItems[i] = true;
				i++;				
			}
			
		}
		
		new AlertDialog.Builder(this)
		.setTitle("Which Draw to add")
		.setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				if(isChecked)
					addDrawSensor(mSensorService.mMySensorList.get(which));
				else
				{
					for(MySensor item: mSensorService.mMySensorRegisteredList){
						if(item.getName() == mSensorService.mMySensorList.get(which).getName()){
							removeDrawSensor(item);
							break;
							}
					}
				}
			}
			
		})
		.show();
	}
	
	/**
	 * tworzy pojawiajï¿½ce siï¿½ okienko z checklistï¿½ z czujnikami jakie moga byï¿½ zapisane 
	 * moï¿½liwoï¿½ï¿½ wybrania kilku naraz i usuniï¿½cia
	 */
	public void saveMultipleSensorsUI(){
		CharSequence[] items = new CharSequence[mSensorService.mMySensorList.size()];
		boolean[] checkedItems = new boolean[mSensorService.mMySensorList.size()];
		int i = 0;
		for(MySensor item: mSensorService.mMySensorList)
			items[i++] = item.getName();
		
		for(SaveToFile item: mSensorService.mMySensorSaveList){
			i = 0;
			for(MySensor itemList: mSensorService.mMySensorList){
				if(itemList.getName() == item.getmMySensor().getName())
					checkedItems[i] = true;
				i++;				
			}
			
		}
		
		new AlertDialog.Builder(this)
		.setTitle("Which Sensor to save")
		.setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				if(isChecked)
					saveSensor(mSensorService.mMySensorList.get(which));
				else
				{
					for(SaveToFile item: mSensorService.mMySensorSaveList){
						if(item.getmMySensor().getName() == mSensorService.mMySensorList.get(which).getName()){
							stopSaveSensor(item);
							break;
							}
					}
				}
			}
			
		})
		.show();
	}
	
	/**
	 * tworzy pojawiajï¿½ce siï¿½ okienko z plikami z zapisanymi danymi z czujnika
	 */
	public void readFileSensorsUI(){
		// No to odczytujemy dane
		if(externalStorageAvailable()){
			Intent sensorsInfoTabActivity = new Intent(this, pl.krisbiketeam.accel2.file.SavedFilesListActivity.class);
			startActivityForResult(sensorsInfoTabActivity, SAVED_FILES_LIST);
		}
		else{
			Toast.makeText(getApplicationContext(), "External storage not available", Toast.LENGTH_SHORT).show();
		}
	
	}
	
	/**
	 * dodaje wykres z danego czujnika do listy z wykresami 
	 * @param sensor	czujnik jaki ma byï¿½ dodany do listy wykresï¿½w
	 */
	public void addDrawSensor(MySensor sensor){

		mDrawViews.add(new DrawViewTimeSyncMultiple(sensor, mMySensorManager, this, null));
		//mSensorService.startSensorListen(sensor);
		
		// zarejestruj przechwytywanie dï¿½ugiego klikniï¿½cia do utweorzenia contextMenu
        registerForContextMenu(mDrawViews.get(mDrawViews.size()-1));
        
		
		mLinearView.addView(mDrawViews.get(mDrawViews.size()-1));		//LinearLayout
		
		//adapter.notifyDataSetChanged();								//ListView
		refershLinearView();											//LinearLayout
		
		//ustaw ï¿½aby nie wygasaï¿½ ekran jeï¿½li sï¿½ wykresy
		keepScreenOnUpdate();
		
		//odï¿½wierz rozmiary okienek z wykresami
		for(DrawView item: mDrawViews){
			item.requestLayout();
		}
	}	
	
	/**
	 * dodaje wykres z danych czujnika odczytanych z pliku do listy z wykresami 
	 * @param sensor	czujnik jaki ma byï¿½ dodany do listy wykresï¿½w
	 */
	public void addDrawSensorFromFile (ReadFromFileData mReadFromFileData){
		
		mDrawViews.add(new DrawViewMultipleFromFile(mReadFromFileData, null, this, null));
		//mSensorService.startSensorListen(sensor);
		
		// zarejestruj przechwytywanie dï¿½ugiego klikniï¿½cia do utweorzenia contextMenu
        registerForContextMenu(mDrawViews.get(mDrawViews.size()-1));
        
		
		mLinearView.addView(mDrawViews.get(mDrawViews.size()-1));		//LinearLayout
		
		//adapter.notifyDataSetChanged();								//ListView
		refershLinearView();											//LinearLayout
		
		//ustaw ï¿½aby nie wygasaï¿½ ekran jeï¿½li sï¿½ wykresy
		keepScreenOnUpdate();
		
		//odï¿½wierz rozmiary okienek z wykresami
		for(DrawView item: mDrawViews){
			item.requestLayout();
		}
	}	
	
	/**
	 * usuwa wykres z danego czujnika do listy z wykresami 
	 * kasuje pierwsze pojawienie siê czujnika na liœcie, 
	 * nie wykasuje kolejnego z kolei wyst¹pienia czujnika jeœli jest ich kilka
	 * @param sensor	czujnik jaki ma byï¿½ usuniï¿½ty z listy wykresï¿½w
	 */
	public void removeDrawSensor(MySensor sensor){
		
		for(DrawView item: mDrawViews){
			if(item.getType() == sensor.getType()){
				item.unRegisterMySensorListener();
				mLinearView.removeView(item);			//LinearLayout
				mDrawViews.remove(item);
				break;
			}
			
		}
		//mSensorService.stopSensorListen(sensor);
		
		//adapter.notifyDataSetChanged();				//ListView
		refershLinearView();											//LinearLayout
		
		//ustaw ï¿½aby nie wygasaï¿½ ekran jeï¿½li sï¿½ wykresy
		keepScreenOnUpdate();
		
		for(DrawView item: mDrawViews){
			item.requestLayout();
		}
	}
	/**
	 * usuwa wykres z danego czujnika do listy z wykresami 
	 * @param sensor	czujnik jaki ma byï¿½ usuniï¿½ty z listy wykresï¿½w
	 */
	public void removeDrawSensor(int which){
		
		mDrawViews.get(which).unRegisterMySensorListener();
		//sprawdzamy czy przypadkiem sÄ… jakieÅ› View
		if(mLinearView.getChildCount()>which)
			mLinearView.removeView(mDrawViews.get(which));
		mDrawViews.remove(which);
		
		//mSensorService.stopSensorListen(sensor);
		
		//adapter.notifyDataSetChanged();				//ListView
		refershLinearView();											//LinearLayout
		
		//ustaw ï¿½aby nie wygasaï¿½ ekran jeï¿½li sï¿½ wykresy
		keepScreenOnUpdate();
		
		for(DrawView item: mDrawViews){
			item.requestLayout();
		}
	}
	
	/**
	 * rozpoczyna zapisywanie danych z danego czujnika w pliku 
	 * @param sensor	czujnik jaki ma byï¿½ zapisany
	 */
	public void saveSensor(MySensor sensor){
		
		if(externalStorageAvailable()){
		
			Calendar c = Calendar.getInstance();
			
			
			mSensorService.mMySensorSaveList.add(
					new SaveToFile(this, sensor.getName() + c.getTime(), sensor, mMySensorManager));
			mSensorService.mMySensorSaveList.get(mSensorService.mMySensorSaveList.size()-1)
						.setName(sensor.getName()+ "SaveToFileThread");
			mSensorService.mMySensorSaveList.get(mSensorService.mMySensorSaveList.size()-1)
						.start();
			for(DrawView item: mDrawViews){
				if(sensor.getName() == item.getMySensor().getName())
					item.setRecorded(true);
			}
		
		}
		else{
			Toast.makeText(getApplicationContext(), "External storage not available", Toast.LENGTH_SHORT).show();
		}
	}
	/**
	 * koï¿½czy zapisywanie danych z danego czujnika w pliku 
	 * @param saveToFile	czujnik jaki nie ma byï¿½ juï¿½ zapisany
	 */
	public void stopSaveSensor(SaveToFile saveToFile){
		
		saveToFile.stopSaveToFile();
		mSensorService.mMySensorSaveList.remove(saveToFile);
		for(DrawView item: mDrawViews){
			if(saveToFile.getmMySensor().getName() == item.getMySensor().getName())
				item.setRecorded(false);
		}
	
	}
	
	/**
	 * wï¿½ï¿½cza blokade ï¿½wiecenia ekranu, bï¿½dzie siï¿½ saï¿½y czas ï¿½wieciï¿½ jeï¿½li sa jakieï¿½ wykresy i odbierajï¿½ dane
	 */
	private void keepScreenOnUpdate(){
		if(!mDrawViews.isEmpty())
			this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		else		
			this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			

	}

	/**
	 * //LinearLayout
	 * zmienia rozmiary okienek z wykresami na podstawie iloï¿½ci okienek do wyï¿½wietlania
	 */
	private void refershLinearView (){					//LinearLayout
		int parentViewHeigth = mLinearView.getHeight();
		
		for(DrawView item: mDrawViews){
			item.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, parentViewHeigth/mLinearView.getChildCount()));//.setMinimumHeight(parentViewHeigth/mLinearView.getChildCount());
		}
		
	}

	/**
	 * sprawdza czy moï¿½na zapisywaï¿½ na zewnï¿½trznej pamiï¿½ci i czy tak jest w ogï¿½le obecna
	 * @return	czy moï¿½ï¿½a zapisywaï¿½ na zewnï¿½trznej pamieci?
	 */
	public static boolean externalStorageAvailable(){
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
		return (mExternalStorageWriteable & mExternalStorageAvailable);
	}
}
