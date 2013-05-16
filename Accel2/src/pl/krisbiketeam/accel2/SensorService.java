package pl.krisbiketeam.accel2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.krisbiketeam.accel2.blueMSP430.BlueMSP430;
import pl.krisbiketeam.accel2.blueMSP430.BlueMSP430Activity;
import pl.krisbiketeam.accel2.blueMSP430.BlueMSP430Service;
import pl.krisbiketeam.accel2.blueMSP430.BlueMSP430Service.BlueMSP430SensorEventListener;
import pl.krisbiketeam.accel2.blueMSP430.BlueMSP430Service.LocalBinderBlueMSP430Service;
import pl.krisbiketeam.accel2.blueMSP430.BluetoothDeviceListActivity;
import pl.krisbiketeam.accel2.blueMSP430.DummyActivityDialog;
import pl.krisbiketeam.accel2.blueMSP430sensor.BlueMSP430Sensor;
import pl.krisbiketeam.accel2.blueMSP430sensor.BlueMSP430SensorEvent;
import pl.krisbiketeam.accel2.file.SaveToFile;
import pl.krisbiketeam.accel2.settings.MySettings;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Messenger;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class SensorService extends Service implements SensorEventListener, BlueMSP430SensorEventListener {

	// Debugging
	private static final String TAG = "SensorService";
	private static final boolean D = true;
	
	// Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    
	// Id powiadomienia na pasku stanu
	private static final int ONGOING_NOTIFICATION = 666;
	
	// indicates whether Service was already running fo onStartCommand()
	private boolean isRunning = false;
	private boolean isReceiving = false;
	
	
	/** 
	 * messanger to send messages to other intents
	 */
	public Messenger mSensorServiceMessenger = null;
	
	/**
	 *  do wewn�trznego czujnika przyspieszenia
	 */
    private SensorManager mSensorManager;

    /**
     * odczytuje przechowuje i zapisuje ustawienia programu
     */
    MySettings mSetings;
    
    /**
     * do zewn�trznego czujnika
     */
    BlueMSP430Service mBlueMSP430Service;
    Intent mBlueMSP430ServiceMyIntent;
    
    /**
	 * flaga przechowuj¹ca stan zarejestrowania Recivera Bluetoot
	 */
    private boolean registerBluetootReceiver = false;
    
    /**
     * lista dost�pnych czujnik�w
     * mog� w niej by� obiekty Sensor i ewentualnie BlueMSPSensor
     */
    public List<MySensor> mMySensorList;
    
    /**
     * lista nas�uchiwanych czujnik�w
     * mog� w niej by� obiekty Sensor i ewentualnie BlueMSPSensor
     */
    public List<MySensor> mMySensorRegisteredList;
    
    /**
     * lista zapisywanych czujnik�w
     * mog� w niej by� obiekty Sensor i ewentualnie BlueMSPSensor
     */
    public List<SaveToFile> mMySensorSaveList;
        
    /**
     * lista Listener�w zarejestrowanych do nas�uchiwania zda�e� z czujnik�w
     */
    public Map<OnMySensorChanegedListener, MySensor> mOnMySensorChanegedListenerMap 
				= new HashMap<OnMySensorChanegedListener, MySensor>();

    
	/**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
    	SensorService getService() {
            // Return this instance of LocalService so clients can call public methods
            return SensorService.this;
        }
    }
        
  
  /* (non-Javadoc)
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		if (D) Log.d(TAG, "+ ON CREATE +");
		
		mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		List<Sensor> internalSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
		
		mMySensorList = new ArrayList<MySensor>();
		for(Sensor item: internalSensors){
			mMySensorList.add(new MySensor(item));
		}
		mMySensorRegisteredList = new ArrayList<MySensor>();
		mMySensorSaveList = new ArrayList<SaveToFile>();
		
		//wczytujemy ustawienia programu
		mSetings = new MySettings(this.getApplicationContext());
		
		
		// TODO Auto-generated method stub
		super.onCreate();
	}

	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (D) Log.d(TAG, "+ ON START +");
  
		if(!isRunning){
			Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
			if (D) Log.i(TAG, "Starting service");
			isRunning = true;
		}
		else{
			Toast.makeText(this, "service already starting", Toast.LENGTH_SHORT).show();
			if (D) Log.i(TAG, "Service already running");
			
		}
		
	// If we get killed, after returning from here, restart
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		if (D) Log.d(TAG, "+ ON BIND +");
	
		return mBinder;		
	}
	
	
	/* (non-Javadoc)
	 * @see android.app.Service#onUnbind(android.content.Intent)
	 */
	@Override
	public boolean onUnbind(Intent intent) {
		if (D) Log.d(TAG, "- ON UNBIND -");
		
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		if (D) Log.d(TAG, "- ON DESTROY -");
	    Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show(); 
	 
	    // Unregister since the activity is about to be closed.
	 	LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
	 		
	    if(mBlueMSP430ServiceMyIntent != null){
	    	BluetoothDevice items[] = new BluetoothDevice[0];
	    	items = (BluetoothDevice[]) mBlueMSP430Service.getmBluetoothMap().keySet().toArray(items);
	    	int i = items.length;
	    	do{
	    		blueMSP430Disconnect(items[--i]);
	    	}
	    	while(i>0);
	    	
	    	//for(BluetoothDevice item: mBlueMSP430Service.getmBluetoothMap().keySet()){
	    		//TODO: wywala błąd przy wychodzeniu z aplikacji jak jest więcej niż dwa połączenia bluetooth
	    	//	blueMSP430Disconnect(item);
	    	//}
	    }
	    	
	    
	    super.onDestroy();
	  }
	
	/**
	 * je�li service przestaje odbiera� dane z czujnik�w to go zamknij
	 */
	public void stopMeRunning () {
		if(isReceiving){
			while(mMySensorRegisteredList.size() != 0)
				stopSensorListen(mMySensorRegisteredList.get(mMySensorRegisteredList.size() - 1));
			//for(MySensor item: mMySensorRegisteredList)
			//	stopSensorListen(item);			
		}
		isRunning = false;
		//stopSelf();
	}
	
		
	@Override
 	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent arg0) {
		MySensor mySensor = null;
		for(MySensor sensor: mMySensorList){
			if(sensor.getSensor() == arg0.sensor){
				mySensor = sensor;
				break;
			}
		}
		if(mOnMySensorChanegedListenerMap != null && mySensor != null){    	
    		for(OnMySensorChanegedListener item: mOnMySensorChanegedListenerMap.keySet()){
				item.onMySensorValueChaneged(new MySensorEvent(arg0, mySensor));
			}
    	}
	}
		
	@Override
	public void onBlueMSP430SensorValueChaneged(BlueMSP430SensorEvent arg0) {
		MySensor mySensor = null;
		for(MySensor sensor: mMySensorList){
			if(sensor.getBlueMSP430Sensor() == arg0.sensor){
				mySensor = sensor;
				break;
			}
		}
		if(mOnMySensorChanegedListenerMap != null && mySensor != null){
    		for(OnMySensorChanegedListener item: mOnMySensorChanegedListenerMap.keySet()){
				//item.onMySensorValueChaneged(new MySensorEvent(arg0));
    			item.onMySensorValueChaneged(new MySensorEvent(arg0, mySensor));
			}
    	}
	}
	
	@Override
	public void onBlueMSP430SensorRangeChaneged(BlueMSP430Sensor sensor) {
		// TODO Auto-generated method stub
		
	}

	
	/**
	 * ustawia service na pierwszoplanowy i w��cza powiadomienie na pasku stanu o tym
	 */
	public void startForegroundNotification(){
		if (D) Log.i(TAG, "startForegroundNotification");
		
		
		NotificationCompat.Builder mBuilder =
	        new NotificationCompat.Builder(this)
	        .setSmallIcon(R.drawable.status_bar_icon)
	        .setContentTitle("My notification")
	        .setContentText("Hello World!");
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(getApplicationContext(), Accel2.class);
		//resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		resultIntent.setAction("android.intent.action.MAIN");
		resultIntent.addCategory("android.intent.category.LAUNCHER"); 
		PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		startForeground(ONGOING_NOTIFICATION, mBuilder.build());
		
	}
	
	/**
	 * usuwa service z pierwszego planu i usuwa powiadomienia na pasku stanu
	 */
	public void stopForegroundNotification(){
		if (D) Log.i(TAG, "stopForegroundNotification");
		//usuwa tak�e Notification z paska powiadomie�
		stopForeground(true);
	}
	
	/**
	 * rozpoczyna nas�uchiwanie z danego czujnika
	 * @param sensor czujnik jaki ma by� ju� nas�uchiwany
	 */
	private void startSensorListen(MySensor sensor){
		//sprawdzamy czy przypadkiem ju� nie nas�uchujemy danego czujnika
		if(mMySensorRegisteredList.contains(sensor))
			return;
		//niech zarejestruje nas�uchiwanie z wewn�trznego czujnika
		if(sensor.isInternal()){
			mSensorManager.registerListener(this, sensor.getSensor(),//mSensorManager.getDefaultSensor(sensor.getType())
					SensorManager.SENSOR_DELAY_GAME);//.SENSOR_DELAY_FASTEST);//.SENSOR_DELAY_NORMAL);//.SENSOR_DELAY_UI);//
			
		}
		//niech zarejestruje nas�uchiwanie z zewn�trznego czujnika
		else{
			if(mBlueMSP430Service != null)
				mBlueMSP430Service.registerBlueMSP430Listener(this, sensor.getBlueMSP430Sensor());
		}
		
		isReceiving = true;
		mMySensorRegisteredList.add(sensor);
		startForegroundNotification();
	}
	
	/**
	 * ko�czy nas�uchiwanie z danego czujnika
	 * @param sensor czujnik jaki ma nie by� ju� nas�uchiwany
	 */
	private void stopSensorListen(MySensor sensor){
		//sprawdzamy czy nas�uchujemy danego czujnika
		if(!mMySensorRegisteredList.contains(sensor))
			return;
		
		//niech wyrejestruje nas�uchiwanie z wewn�trznego czujnika
		if(sensor.isInternal()){
			mSensorManager.unregisterListener(this, sensor.getSensor());//mSensorManager.getDefaultSensor(sensor.getType())
						
		}
		//niech wyrejestruje nas�uchiwanie z zewn�trznego czujnika
		else{
			if(mBlueMSP430Service != null)
				mBlueMSP430Service.unregisterOnBlueMSP430Listener(this, sensor.getBlueMSP430Sensor());
		}
		
		mMySensorRegisteredList.remove(sensor);			
		//je�li niczego ju� nie nas�uchujemy to wyzeruj flag� nas�uchiwania
		if(mMySensorRegisteredList.size() == 0){
			isReceiving = false;
			stopForegroundNotification();
		}
	}
	
		
	/**
	 * uruchom us�ug� BleMSP430
	 * @param data		intent z nazw� urz�dzenia bluetoothe do po��czenia si� z nim
	 */	
	public void blueMSP430Connect(Intent data){
				// Register to receive messages.
		// We are registering an observer (mMessageReceiver) to receive Intents
		// with actions named "custom-event-name".
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
				new IntentFilter("BlueMSP430Service_state_event"));

		//start SensorService
		mBlueMSP430ServiceMyIntent = new Intent(this, BlueMSP430Service.class);
		mBlueMSP430ServiceMyIntent.putExtras(data);
		startService(mBlueMSP430ServiceMyIntent);
		bindService(mBlueMSP430ServiceMyIntent, mBlueMSP430ServiceConnection, 0);//Context.BIND_AUTO_CREATE);
		
	}
	
	/**
	 * zako�cz us�ug� BleMSP430
	 */
	public void blueMSP430Disconnect(BluetoothDevice btDevice){
		//super.onDestroy();
		mBlueMSP430Service.getmBluetoothMap().get(btDevice).stop();
		mBlueMSP430Service.getmBluetoothMap().remove(btDevice);
		//je�li by�o poprawne po��czenie i utworzony zosta� obiekt BlueMSP430 to go usu�
		if(mBlueMSP430Service.getmBluetoothMap().isEmpty()){
			unbindService(mBlueMSP430ServiceConnection);
			//stop BlueMSP430ServiceMy
			stopService(mBlueMSP430ServiceMyIntent);
		}
	}
	
	/**
	 * pokaż okno z informacjami i opcjami do czujnika BlueMSP430
	 * @param blue BlueMSP430 jakiego informacje i parametry chcemy zmianić
	 */
	public void showBlueMSP430Activity(BlueMSP430 blue){
		
		Intent blueMSP430Activity = new Intent(this, BlueMSP430Activity.class);
		blueMSP430Activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK + Intent.FLAG_ACTIVITY_SINGLE_TOP); 
		blueMSP430Activity.putExtra("Number", mBlueMSP430Service.mBlueMSP430.indexOf(blue));
		startActivity(blueMSP430Activity);
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
			
			
            //mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        	if (D) Log.d(TAG, " ServiceDisconnected");
            
        	//mBound = false;
        }
    };
    
    
	// Allows the user to set an Listener and react to the event
	public void registerOnMySensorChanegedListener(OnMySensorChanegedListener listener, MySensor sensor) {
		if(!mOnMySensorChanegedListenerMap.containsKey(listener)){
			mOnMySensorChanegedListenerMap.put(listener, sensor);
			startSensorListen(sensor);
		}
	}
	// Allows the user to set an Listener and react to the event
	//TODO: trzeba bedzie wykasowa� z t�d MySensor sensor jak si� zlikwiduje mOnBlueMSP430SensorChanegedListenerList
	public void unregisterOnMySensorChanegedListener(OnMySensorChanegedListener listener, MySensor sensor) {
		MySensor tmpSensor = mOnMySensorChanegedListenerMap.get(listener);
		mOnMySensorChanegedListenerMap.remove(listener);
		
		//sprawdzamy czy jest kto� jeszcze kto nas�uchuje jeszcze danego czujnika je�li nie to przestajeym nas�uchiwa� tego czujnika
		if(!mOnMySensorChanegedListenerMap.containsValue(tmpSensor))
			stopSensorListen(tmpSensor);
		
	}
		
	// Define our custom Listener interface
    public interface OnMySensorChanegedListener {
    	/**
    	 * funkcja wywo�ywana po odebraniu danych z czujnika z warto�ciami z tego czujnika
    	 * @param event warto�ci z czujnika
    	 */
    	public abstract void onMySensorValueChaneged(MySensorEvent event);
	    //TODO: to b�dzie mo�na wywali� i zrobi� �eby poczatkowe dane pobiera�o z MySensor
	    //public abstract void onMySensorInitChaneged(MySensor sensor);	    
    }
    
    
    
/////////////////////////////////////////////             BLUETOOTH CONNECT INSTANCE          ///////////////////    
    /**
     * otwiera okno z wyborem urz¹dzeñ do po³¹czenia siê przez bluetooth
     */
    public void selectBluetoothDeviceActivity(){
    	if (BluetoothAdapter.getDefaultAdapter() != null){
			if(BluetoothAdapter.getDefaultAdapter().isEnabled()){
				LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
							new IntentFilter("BluetoothDeviceListActivity_Result"));
				Intent intent = new Intent(this, BluetoothDeviceListActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				
			}
			else{
				requestEnableBluetooth();
			}
		}
		else
		{
			Toast.makeText(this, R.string.no_bluetooth_adapter, Toast.LENGTH_LONG).show();
		}
    }
    /**
     * Wy�wietla okienko z wybodem urz�dze� Bluetooth kt�re chcemy od��czy�
     */
    public void disconnectBluetoothDeviceUI(){
		int i = 0;
		
		if(mBlueMSP430Service == null)
			return;
		
		CharSequence[] items = new CharSequence[mBlueMSP430Service.getmBluetoothMap().size()];
		//for(MySensor item: mSensorService.mMySensorRegisteredList)
		for(BluetoothDevice item: mBlueMSP430Service.getmBluetoothMap().keySet())
			items[i++] = "Name: " + item.getName() + "\nAddress: " + item.getAddress();
	
		//zarejestruj z nas�uchiwanie z DummyActivityDialog o wyborze kt�rego czujnika mamy od��czy�
		LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(mMessageReceiver,
					new IntentFilter("RemoveBluetoothDeviceListActivity_Result"));
		
		//Tworzymy puste Activity z dialogboxem do tyboru BluetoothDevice do roz��czenia
		Intent intent = new Intent(this, DummyActivityDialog.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK + Intent.FLAG_ACTIVITY_SINGLE_TOP); 
		intent.putExtra("Device_List", items);
		intent.putExtra("Title", "Which BT Device to disconnect");
		intent.putExtra("Broadcast", "RemoveBluetoothDeviceListActivity_Result");
    	startActivity(intent);
		
		
	}
    /**
     * Wy�wietla okienko z wybodem urz�dze� Bluetooth których parametry chcemy zmienić zobaczyć
     */
    public void msp430ActivityUI(){
		int i = 0;
		
		if(mBlueMSP430Service == null)
			return;
		if(mBlueMSP430Service.mBlueMSP430.size() == 1){
			showBlueMSP430Activity(mBlueMSP430Service.mBlueMSP430.get(0));
		}
		else{
			CharSequence[] items = new CharSequence[mBlueMSP430Service.mBlueMSP430.size()];
			//for(MySensor item: mSensorService.mMySensorRegisteredList)
			for(BlueMSP430 item: mBlueMSP430Service.mBlueMSP430)
				items[i++] = "Name: " + item.btDevice.getName() +"\nAddress: " + item.btDevice.getAddress();
		
			//zarejestruj z nas�uchiwanie z DummyActivityDialog o wyborze kt�rego czujnika mamy od��czy�
			LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(mMessageReceiver,
						new IntentFilter("MSP430ActivityUI_Result"));
			
			//Tworzymy puste Activity z dialogboxem do tyboru BluetoothDevice do roz��czenia
			Intent intent = new Intent(this, DummyActivityDialog.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK + Intent.FLAG_ACTIVITY_SINGLE_TOP); 
			intent.putExtra("Device_List", items);
			intent.putExtra("Title", "Which BT Device properties to show");
			intent.putExtra("Broadcast", "MSP430ActivityUI_Result");
	    	startActivity(intent);
		}
		
	}
    /**
     * shows popup to request enable bluetooth
     */
    private void requestEnableBluetooth(){
    	Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        enableIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(enableIntent);
     
        registerBluetoothReceiver();
    }
    /**
     * zarejestruj nas³uchiwanie czy w³¹czono Bluetooth
     */
    private void registerBluetoothReceiver(){
    	if(registerBluetootReceiver == false){
	    	// zarejestruj odbiornik do orbierania zmianu stanu BluetoothAdapter  powi¹zane z BroadcastReceiver
	        //Context mContext = getBaseContext();;
	        IntentFilter mIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
	        registerReceiver(mReceiver, mIntentFilter);
	        registerBluetootReceiver = true;
    	}
    }
    /**
     * odrejestruj nas³uchiwanie czy w³¹czono Bluetooth
     */
    private void unregisterBluetoothReceiver(){
    	if(registerBluetootReceiver == true){
	    	// odrejestruj odbiornik do orbierania zmianu stanu BluetoothAdapter  powi¹zane z BroadcastReceiver
	        //Context mContext = getBaseContext();;
	        unregisterReceiver(mReceiver);
	        registerBluetootReceiver = false;
    	}
    }   
    // odbiera wiadomoæ po zmianie stanu bluetooth (w³¹czeniu) bluetooth
 	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             
         	String action = intent.getAction();
         	if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                 int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                                                BluetoothAdapter.ERROR);
                 switch (state) {
                 case BluetoothAdapter.STATE_ON:
                 	unregisterBluetoothReceiver();
                 	selectBluetoothDeviceActivity();
                 	break;
                 case BluetoothAdapter.STATE_OFF:
                     //onBluetoothEnable();
                     break;
                 case BluetoothAdapter.STATE_TURNING_OFF:
                     //onBluetoothDisable();
                     break;
                 case BluetoothAdapter.STATE_TURNING_ON:
                     //onBluetoothDisable();
                     break;
                 }
             }
                 
             
         }
     };
    
    // Our handler for received Intents. This will be called whenever an Intent
 	// with an action named "BluetoothDeviceListActivity_Result" is broadcasted.
 	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
 	  @Override
 	  public void onReceive(Context context, Intent intent) {
 		  String action = intent.getAction();
 		  if (action.equals("BluetoothDeviceListActivity_Result")) { 	    
 			   // Unregister bo ju¿ nie oczekujemy na wynik z DeviceListActivity o wybranym BlueMSP430
 			  LocalBroadcastManager.getInstance(getBaseContext()).unregisterReceiver(mMessageReceiver);
 			  // "BluetoothDeviceListActivity_Result"
 			  blueMSP430Connect(intent);
 		  }
 		 
 		  if (action.equals("RemoveBluetoothDeviceListActivity_Result")) {
 			  // Unregister since the activity is about to be closed.
 			  LocalBroadcastManager.getInstance(getBaseContext()).unregisterReceiver(mMessageReceiver);
 			  //zarejestruj z powrotem nas�uchiwanie z BlueMSP430Service
 			  LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(mMessageReceiver,
						new IntentFilter("BlueMSP430Service_state_event"));
 			  
 			  int which = intent.getIntExtra("which",-1);
 			  if(which != -1){
 				  int ii = 0;
	 			  for(BluetoothDevice item: mBlueMSP430Service.getmBluetoothMap().keySet()){
					if(ii == which){
						blueMSP430Disconnect(item);
						break;
					}
					else
						ii++;
	 			  }
 			  }
 			  
 			 	
 		  }
 		 if (action.equals("MSP430ActivityUI_Result")) {
			  // Unregister since the activity is about to be closed.
			  LocalBroadcastManager.getInstance(getBaseContext()).unregisterReceiver(mMessageReceiver);
			  //zarejestruj z powrotem nas�uchiwanie z BlueMSP430Service
			  LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(mMessageReceiver,
						new IntentFilter("BlueMSP430Service_state_event"));
			  
			  int which = intent.getIntExtra("which",-1);
			  if(which != -1){
				  int ii = 0;
	 			  for(BlueMSP430 item: mBlueMSP430Service.mBlueMSP430){
					if(ii == which){
						showBlueMSP430Activity(item);
						break;
					}
					else
						ii++;
	 			  }
			  }
			  
			 	
		  }
		 

 		  if (action.equals("BlueMSP430Service_state_event")) { 
	 		  // "BlueMSP430ServiceMy_state_event"
	 		  // Get extra data included in the Intent
			  int state = intent.getIntExtra("state",BlueMSP430Service.STATE_NOT_CONNECTED);
			  String btAddress = intent.getStringExtra("device_address");
			  
			  switch (state) {
              case BlueMSP430Service.STATE_CONNECTED:
            	  
            	  for(BlueMSP430 blue: mBlueMSP430Service.mBlueMSP430){
            		  if(blue.btDevice.getAddress().equals(btAddress)){
            			  if(mMySensorList == null)
                    		  mMySensorList = new ArrayList<MySensor>();
        					
            			  for(BlueMSP430Sensor item: blue.mBlueMSP430SensorList){
            				  mMySensorList.add(new MySensor(item));
            			  }
            		  }
            	  }
            	  break;
              case BlueMSP430Service.STATE_NOT_CONNECTED:
            	  
            	  for(BlueMSP430 blue: mBlueMSP430Service.mBlueMSP430){
            		  if(blue.btDevice.getAddress().equals(btAddress)){
            			  
            			  for(BlueMSP430Sensor item: blue.mBlueMSP430SensorList){
            				  for(MySensor myItem: mMySensorList){
            					  if(myItem.getBlueMSP430Sensor() == item){
            						  mMySensorList.remove(myItem);
            						  break;
            					  }
            				  }
            				  
            			  }
            			  mBlueMSP430Service.mBlueMSP430.remove(blue);
            			  break;
            		  }
            	  }
            	  break;
		            
			  }  	
	    }
 	  }
 	};
 	
	    
    /**
	 * @return the isReceiving
	 */
	public boolean isReceiving() {
		return isReceiving;
	}

	/**
	 * @return the isRunning
	 */
	public boolean isRunning() {
		return isRunning;
	}



	
}

