package pl.krisbiketeam.accel2.blueMSP430;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.krisbiketeam.accel2.R;
import pl.krisbiketeam.accel2.blueMSP430sensor.BlueMSP430Sensor;
import pl.krisbiketeam.accel2.blueMSP430sensor.BlueMSP430SensorEvent;
import pl.krisbiketeam.accel2.blueMSP430sensor.BlueMSP430_acc;
import pl.krisbiketeam.accel2.blueMSP430sensor.BlueMSP430_register;
import pl.krisbiketeam.accel2.blueMSP430sensor.BlueMSP430_temp;
import pl.krisbiketeam.accel2.settings.MySettings;
import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

//TODO: doda� opcje �eby jak si� roz��czy to�eby ponownie si� ��czy�o ewentualnie licznik po��cze� i jak si� nie uda 5 razy to zako�cz t� us�ug�

@SuppressLint("HandlerLeak")
public class BlueMSP430Service extends Service {

	// Debugging
	public static final int BLUEMSP430_DATA_PACKET = 10;
	
	//StrictMode to do test�w aplikacji czy aplikacja nie bedzie si� da�a klikn�� jak co� sie bedzie robi�o za d�ugo
	private static final boolean L = false;
		
	// Debugging
    protected static final String TAG = "BlueMSP430Service";
    protected static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    
    public static final int MESSAGE_PREPARE_PROGRESS = 6;
    public static final int MESSAGE_PROGRESS = 7;
    public static final int MESSAGE_DESTROY_PROGRESS = 8;
    
    public static final int STATE_NOT_CONNECTED = 10;
    public static final int STATE_CONNECTED = 11;
    

    // Key names received from the BluetoothService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    
    
    
    private ReadAllRegisters tReadAllRegisters;
    private SendAllRegisters tSendAllRegisters;
    
    public List<BlueMSP430> mBlueMSP430 = new ArrayList<BlueMSP430>();
    
    // Local Bluetooth adapter
    protected BluetoothAdapter mBluetoothAdapter = null;
    
    protected Map<BluetoothDevice, BluetoothService> mBluetoothMap 
    			= new HashMap<BluetoothDevice, BluetoothService>();
    




	//public Map<BlueMSP430SensorEventListener, BlueMSP430Sensor> mBlueMSP430SensorEventListenerMap 
    //	= new HashMap<BlueMSP430SensorEventListener, BlueMSP430Sensor>();
    public Map<BlueMSP430SensorEventListener, List<BlueMSP430Sensor>> mBlueMSP430SensorEventListenerMap 
		= new HashMap<BlueMSP430SensorEventListener, List<BlueMSP430Sensor>>();

    
	
   /**
     * odczytuje przechowuje i zapisuje ustawienia programu
     */
    MySettings mSetings;
	
    
    //iterator do p�tli for
    protected int iter1, iter2, length;
    //w mMESSAGE_READ
	protected byte[] readBuf;
	
	//protected NotificationManager mgr=null;
	//protected static final int NOTIFY_ME_ID=1337;
	//protected Notification note = null;
	//protected Intent activityIntent;
	//private boolean isRunning=false;
	
	
	
    // Binder given to clients
    private final IBinder mBinder = new LocalBinderBlueMSP430Service();
    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinderBlueMSP430Service extends Binder {
    	public BlueMSP430Service getService() {
            // Return this instance of LocalService so clients can call public methods
            return BlueMSP430Service.this;
        }
    }
    
    
    
    
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate() {
        if(D) Log.d(TAG, "+++ ON CREATE +++");
        
        // to tylko do test�w p�niej mo�na wykasowa�/ zrzuca logi o naruszeniu polityki w�tk�w
        if (L) StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        		.detectAll()
        		.penaltyLog()
        		.build());
        
        //pobierz ustawienia programu 
        //mPrefs = new Preferences(this.getApplicationContext(),mHandler);
        mSetings = new MySettings(this.getApplicationContext());
                
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.no_bluetooth_adapter, Toast.LENGTH_LONG).show();
            return;
        }
        //registerBluetoothReceiver();
        //mgr=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        
    	
//		notifyMe();
        
            }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(D) Log.d(TAG, "+++ ON START +++");
        // Get the device MAC address
        //mBluetoothAddress = intent.getExtras().getString(BluetoothDeviceListActivity.EXTRA_DEVICE_ADDRESS);
        
        // Get the BLuetoothDevice object
        //mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(mBluetoothAddress);
        
        //wyślij BluetoothDevice do funkcji łączącej w celu połączenia się z tym urządzeniem 
        connectDevice(mBluetoothAdapter.getRemoteDevice(intent.getExtras()
                .getString(BluetoothDeviceListActivity.EXTRA_DEVICE_ADDRESS)));
        
        return START_NOT_STICKY;
    }

    public IBinder onBind(Intent arg0) {
		if (D) Log.d(TAG, "+ ON BIND +");
	
		return mBinder;		
	}
    
    
	public void onDestroy() {
        super.onDestroy();
        if(D) Log.d(TAG, "--- ON DESTROY ---");
        // Stop the Bluetooth chat services
        for(BluetoothDevice item: mBluetoothMap.keySet()){
        	mBluetoothMap.get(item).stop();
        }
        mBluetoothMap.clear();
        
        //Context mContext = getBaseContext();
        //mContext.unregisterReceiver(mReceiver);
        //mBluetoothAdapter.disable();
        //kasujemy ikonke status bar notification
//		clearNotification();
        
        
    }

	
/*
	public void notifyMe() {
		note=new Notification(R.drawable.status_bar_icon, "BlueMSP430", System.currentTimeMillis());
		Intent i=new Intent(this, MainActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pi=PendingIntent.getActivity(this, 0, i, 0);
		note.setLatestEventInfo(this, "BlueMSP430Service Running", "Lounch BlueMSP430Activity", pi);
		//note.vibrate=new long[] {500L, 200L, 200L, 500L};
		//note.flags|=Notification.FLAG_AUTO_CANCEL;			// kasuje status bar notification after clicking it
		note.flags|=Notification.FLAG_NO_CLEAR;
		//mgr.notify(NOTIFY_ME_ID, note);
		startForeground(NOTIFY_ME_ID, note);
	}
	public void clearNotification() {
		//mgr.cancel(NOTIFY_ME_ID);
		stopForeground(true);
	}
	public void notifyAdd(){
		note.number += 1;
		note.ledOnMS = 1;//000;
		note.ledOffMS = 0;//1000;
		note.ledARGB = 0x080ff0000;  
		note.flags = Notification.FLAG_SHOW_LIGHTS;
		mgr.notify(NOTIFY_ME_ID, note);
	}
	public void notifyRemove(){
		note.number -= 1;
		note.flags = Notification.DEFAULT_ALL;
		
		mgr.notify(NOTIFY_ME_ID, note);
	}
	*/
//////////////////////////////////////////////////////	
	
	/**
     * Connects to a device
     * @param address	Address urządzenia z którym chcemu się połączyć
     */
    public void connectDevice(BluetoothDevice btDevice) {
    	Log.d(TAG, "connectDevice()");
		
    	if (mBluetoothAdapter.isEnabled()){
    		if(!mBluetoothMap.containsKey(btDevice)){
    			// Initialize the BluetoothService to perform bluetooth connections
    			mBluetoothMap.put(btDevice, new BluetoothService(this, mHandler));
    		}
    		
    		if(mBluetoothMap.get(btDevice).getState() == BluetoothService.STATE_CONNECTED){
    			if(D) Log.d(TAG, "ALREADY connect to device" + btDevice.getAddress() + " " + btDevice.getName());    	        
    		}
    		else{
	    		// Attempt to connect to the device
		        mBluetoothMap.get(btDevice).connect(btDevice, false);
		        //if(D) Log.d(TAG, "connect to device" + mBluetoothAddress + " " + mBluetoothDevice.getName());
		        if(D) Log.d(TAG, "connect to device" + btDevice.getAddress() + " " + btDevice.getName());
	    		}
        }
    	else{
    		stopSelf();
    		//zarz�daj w��czenia bluetooth
			//requestEnableBluetooth();
    	}
    }
	
    /**
     * Sends a byte array of messages.
     * @param message  A byte array with message to send.
     */
    protected void sendByteMessage(BluetoothDevice btDevice, byte[] message) {
        // Check that we're actually connected before trying anything
    	if ( mBluetoothMap.get(btDevice).getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.title_not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            mBluetoothMap.get(btDevice).write(message);
        }
        //if(D) Log.d(TAG, "Send byte" + message[0]);
        
    }
    /**
     * tworzy wiadomo�� do wys�ania do wykonania jakie� czynno�ci na rejestrach
     * 
     * @param what	co ma by� zrobnione zapis odczyt ile bajt�w?
     * @param add	adres czujnika
     * @param reg	rejestr do odczytania/zapisania
     * @param val	warto�� rejestru do odczytania/zapisania
     * @return	byte array z wiadomo�ci� do wys�ania
     */
    public byte[] createMessageToSend (int what, int add, int reg, int val){
    	byte[] byteMsg = new byte[4];
    	byteMsg[0] = (byte)what;
    	byteMsg[1] = (byte)add;
    	byteMsg[2] = (byte)reg;
    	byteMsg[3] = (byte)val;
    	return byteMsg;
    }

         
	
	/**
	 * wy��cza opcje ci�g�ego wysy�ania danych z czujnika
	 */
	public void enableContSending(BluetoothDevice btDevice){
		sendByteMessage(btDevice, createMessageToSend(11, 0, 0, 0));			//wy��cz ci�g�e wysy�anie
	}
	/**
	 * w��cza opcje ci�g�ego wysy�ania danych z czujnika
	 */
	public void disableContSending(BluetoothDevice btDevice){
		sendByteMessage(btDevice, createMessageToSend(12, 0, 0, 0));			//w��cz ci�g�e wysy�anie
	}
	/**
	 * wy��cza opcje ci�g�ego wysy�ania danych z czujnika
	 */
	public void enableContSendingNew(BluetoothDevice btDevice){
		sendByteMessage(btDevice, createMessageToSend(13, 0, 0, 0));			//wy��cz ci�g�e wysy�anie przez przerwania
	}
	/**
	 * w��cza opcje ci�g�ego wysy�ania danych z czujnika
	 */
	public void disableContSendingNew(BluetoothDevice btDevice){
		sendByteMessage(btDevice, createMessageToSend(14, 0, 0, 0));			//w��cz ci�g�e wysy�anie przez przerwania
	}
	
	
		
	/**
	 * wczytaj zawarto�� wszystkich rejestr�w z czujnika
	 */
	public void read_all_registers(BlueMSP430 blue)
	{
		if(tReadAllRegisters == null){
			tReadAllRegisters = new ReadAllRegisters(this, blue);
			tReadAllRegisters.setName("tReadAllRegisters");
			tReadAllRegisters.start();
		}
		else{
			tReadAllRegisters = null;
			tReadAllRegisters = new ReadAllRegisters(this, blue);
			tReadAllRegisters.setName("tReadAllRegisters");
			tReadAllRegisters.start();
		}
	}
	/**
	 * zapisz zawarto�� wszystkich rejestr�w do czujnika
	 */
	public void send_all_registers(BlueMSP430 blue)
	{
		if(tSendAllRegisters == null){
			tSendAllRegisters = new SendAllRegisters(this, blue);
			tSendAllRegisters.setName("tSendAllRegisters");
	    	tSendAllRegisters.start();
		}
		else{
			tSendAllRegisters = null;
			tSendAllRegisters = new SendAllRegisters(this, blue);
			tSendAllRegisters.setName("tSendAllRegisters");
	    	tSendAllRegisters.start();
		}
	}


     
    public String getBluetoothDeviceAddress(BluetoothDevice btDevice){
    	return btDevice.getAddress();
    }
    
   
	
    
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////    
    

// The Handler that gets information back from the BluetoothChatService
    @SuppressLint("HandlerLeak")
	public final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	
        	switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
            	mESSAGE_STATE_CHANGE(msg);
                break;
            case MESSAGE_WRITE:
            	mESSAGE_WRITE(msg);
            	break;
            case MESSAGE_READ:
            	//sprawdzamy czy nei ma przypadkiem wi�cej pakiet�w do zdekodowania
            	length = msg.arg2;
            	if(length == BLUEMSP430_DATA_PACKET){
            		mESSAGE_READ(msg);
            	}
            	else if(length > BLUEMSP430_DATA_PACKET){
            		byte[] readBuf = ((byte[]) msg.obj);
            		for(int i = 0; i < length; i+=BLUEMSP430_DATA_PACKET){
        				byte[] temp = new byte[BLUEMSP430_DATA_PACKET];
            			for(int j = 0; j < BLUEMSP430_DATA_PACKET; j++){
            				temp[j] = readBuf[j + i];
	            		}
            			mESSAGE_READ(obtainMessage(MESSAGE_READ, msg.arg1, BLUEMSP430_DATA_PACKET, temp));
                		
            		}
            		readBuf = null;
            	}
                break;
            case MESSAGE_DEVICE_NAME:
            	mESSAGE_DEVICE_NAME(msg);
                break;
            case MESSAGE_TOAST:
            	mESSAGE_TOAST(msg);
                break;
            }
        }
    };

    
    

    void mESSAGE_STATE_CHANGE(Message msg){
    	
    	Intent broadcastIntent;
    	broadcastIntent = new Intent("BlueMSP430Service_state_event");
    	
    	switch (msg.arg2) {
        case BluetoothService.STATE_CONNECTED:
        	Toast.makeText(getApplicationContext(), R.string.title_connected_to + "...", Toast.LENGTH_SHORT).show();
        	//sprawdzamy czy nie ma już takiego urządzenia na liście 
        	//jeśli jest to powiadamiamy i wychodzimy jęsli nie ma to tworzymy nowy
        	for(BlueMSP430 blue: mBlueMSP430){
        		if(blue.btDevice.getAddress().hashCode() == msg.arg1){
        			if(blue.getBluetoothConectionState() != STATE_CONNECTED){
	        			blue.setBluetoothConectionState(STATE_CONNECTED);
	                	
	        			read_all_registers(blue);
	                		        			
	                	//wysy�amy og�le powiadomienie o zmianie stanu po��czenia
	                	broadcastIntent.putExtra("state", STATE_CONNECTED);
	                	broadcastIntent.putExtra("device_address", blue.btDevice.getAddress());
	        	        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);	
	        	        return;
        			}
        			else{
        				return;
        			}
        		}
        	}
        	//jeśli nie było urzadzenia na liście to tworzymy nowe i inicjujemy sekwencję nowego połączenia
        	for(BluetoothDevice device: mBluetoothMap.keySet()){
        		if(device.getAddress().hashCode() == msg.arg1){
        			mBlueMSP430.add(new BlueMSP430(this, device));
        			//TODO: a mo�e zrobi� nowy w�tek AsyncTask i pod koniec wykonywania w��cza� ContSending()
                	mBlueMSP430.get(mBlueMSP430.size() - 1).setCont_data_sending(false);
                	mBlueMSP430.get(mBlueMSP430.size() - 1).setCont_data_sendingNew(false);

                	read_all_registers(mBlueMSP430.get(mBlueMSP430.size() - 1));
                	            	 
                	
                	mBlueMSP430.get(mBlueMSP430.size() - 1).setBluetoothConectionState(STATE_CONNECTED);
                	
                	//wysy�amy og�le powiadomienie o zmianie stanu po��czenia
                	broadcastIntent.putExtra("state", STATE_CONNECTED);
                	broadcastIntent.putExtra("device_address", mBlueMSP430.get(mBlueMSP430.size() - 1).btDevice.getAddress());
        	        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        	        
        		}
        	}
        	
	        break;
        case BluetoothService.STATE_CONNECTING:
        	Toast.makeText(getApplicationContext(), R.string.title_connecting, Toast.LENGTH_SHORT).show();
        	for(BlueMSP430 blue: mBlueMSP430){
        		if(blue.btDevice.getAddress().hashCode() == msg.arg1){
        			blue.setBluetoothConectionState(STATE_NOT_CONNECTED);
                	
                	//wysy�amy og�le powiadomienie o zmianie stanu po��czenia
                	broadcastIntent.putExtra("state", STATE_NOT_CONNECTED);
                	broadcastIntent.putExtra("device_address", blue.btDevice.getAddress());
        	        //TODO: to tu w zasadzi nie potrzebne jest
                	//LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
                	
        		}
        	}
	        break;
        case BluetoothService.STATE_NONE:
        	Toast.makeText(getApplicationContext(), R.string.title_not_connected, Toast.LENGTH_SHORT).show();
        	for(BlueMSP430 blue: mBlueMSP430){
        		if(blue.btDevice.getAddress().hashCode() == msg.arg1){
        			blue.setBluetoothConectionState(STATE_NOT_CONNECTED);
                	
                	//wysy�amy og�le powiadomienie o zmianie stanu po��czenia
                	broadcastIntent.putExtra("state", STATE_NOT_CONNECTED);
                	broadcastIntent.putExtra("device_address", blue.btDevice.getAddress());
        	        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
                	
        		}
        	}
	        break;
        }
    }
    
    void mESSAGE_READ(Message msg){
    	readBuf = (byte[]) msg.obj;
    	
    	BlueMSP430 blue = null;
    	for(BlueMSP430 item: mBlueMSP430){
    		if(item.btDevice.getAddress().hashCode() == msg.arg1){
    			blue = item;
    		}
    	}
    	if(blue == null)
    		return;
    	//dekodujemy dane z czujnika BlueMSP430
    	if(!blue.decodeMessage(readBuf))
    		Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
    	
    	if(tReadAllRegisters!=null){
	    	if(!tReadAllRegisters.finished){
	    		if(readBuf[0] == 11 | readBuf[0] == 12 | readBuf[0] == 13 | readBuf[0] == 14 | readBuf[0] == 3 | readBuf[0] == 4 | readBuf[0] == 39  | readBuf[0] == 50){
	    			tReadAllRegisters.unlock_this_thread();
	    			return;
	    		}
	    	}
	    	//else
	    		//tReadAllRegisters=null;
    	}
    	if(tSendAllRegisters!=null){
	    	if(!tSendAllRegisters.finished){
	    		if(readBuf[0] == 11 | readBuf[0] == 12 | readBuf[0] == 13 | readBuf[0] == 14 | readBuf[0] == 1 | readBuf[0] == 2 | readBuf[0] == 50){
	    			tSendAllRegisters.unlock_this_thread();
	    			return;
	    		}
	    	}
	    	//TODO: tu co� nie dzia�a
	    	//else
	    		//tSendAllRegisters=null;
    	}
    	
    	
    	if(readBuf[0] != 93 && readBuf[0] != 95){
    		//Toast.makeText(getApplicationContext(), decodeReceivedMessage(readBuf), Toast.LENGTH_SHORT).show();
    		if(D) Log.d(TAG, "mESSAGE_READ:" + msg.arg2 + readBuf[0]);
    	}
    		
    	if(msg.arg2 > 10){
    		if(D) Log.d(TAG, "OVERLOAD mESSAGE_READ:" + msg.arg2);
    	}
    	
    	//if(D) Log.d(TAG, "mESSAGE_READ");
    	
    	//odebrano nowe dane z czujnika przyspieszenia i temperatury
    	if(readBuf[0] == 93 || readBuf[0] == 95){
	    	
	    	float[] values = new float[3];
	    	values[0] = blue.accX;
	    	values[1] = blue.accY;
	    	values[2] = blue.accZ;
	    	blue.getmBlueMSP430SensorEventList().get(BlueMSP430Sensor.TYPE_ACCELEROMETER)
	    			.update(values, blue.mSensorTimeStamp);
	    	values = new float[1];
	    	values[0] = blue.temperature;//((float)temperature) / 100;
	    	blue.getmBlueMSP430SensorEventList().get(BlueMSP430Sensor.TYPE_AMBIENT_TEMPERATURE)
	    			.update(values, blue.mSensorTimeStamp);
	    	values = new float[1];
	    	values[0] = blue.coreTemperature;//((float)coreTemperature) / 10;
	    	blue.getmBlueMSP430SensorEventList().get(BlueMSP430Sensor.TYPE_CORE_TEMPERATURE)
	    			.update(values, blue.mSensorTimeStamp);
	    	values = new float[1];
	    	values[0] = blue.supplyVoltage;//((float)supplyVoltage) / 1000;
	    	blue.getmBlueMSP430SensorEventList().get(BlueMSP430Sensor.TYPE_SUPPLY_VOLTAGE)
	    			.update(values, blue.mSensorTimeStamp);
	    	
	    	if(mBlueMSP430SensorEventListenerMap != null){
	    		for(BlueMSP430SensorEventListener listener: mBlueMSP430SensorEventListenerMap.keySet()){
	    			//listener.onBlueMSP430SensorValueChaneged(mBlueMSP430SensorList.get(mBlueMSP430SensorEventListenerMap.get(item).getType()));
					for(BlueMSP430Sensor sensor: mBlueMSP430SensorEventListenerMap.get(listener)){
						if(sensor.getBtDevice() == blue.btDevice)
							listener.onBlueMSP430SensorValueChaneged(blue.getmBlueMSP430SensorEventList().get(sensor.getType()));
	    			}
				}
	    	}
	    	
    	}
    	    	
    	readBuf = null;
    }
    
    void mESSAGE_WRITE(Message msg){
    	
    }
    
    void mESSAGE_DEVICE_NAME(Message msg){
    	
    }
    
    void mESSAGE_TOAST(Message msg){
    	Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
    }

    

    /**
     * klasa do w�tku do odczytania pocz�tkowych warto�ci rejestr�w w czujniku
     * @author Krzy�
     *
     */
    private class ReadAllRegisters extends Thread{
    	
    	volatile boolean finished = false;
    	volatile boolean contDataSend = false;
    	volatile boolean contDataSendNew = false;
    	Context context;
    	Intent intent;
    	BlueMSP430 blue;
    	
    	ReadAllRegisters(Context context, BlueMSP430 blue){
    		this.context = context;
    		//znajdź odbowiedni moduł BlueMSP430 na podstawie BluetoothDevice
    		this.blue = blue;
    		
    	}
    	@Override
    	public void run()
    	{
    		if(D) Log.d("ReadAllRegisters Thread", "run()" + " addres: " + blue.btDevice.getAddress() + "end");
    		
    		contDataSend = blue.isCont_data_sending();
        	contDataSendNew = blue.isCont_data_sendingNew();
        	
        	//zawiadom activity powi�zane z t� us�ug� o rozpocz�ciu odczytywania pocz�tkowych wartoci rejestr�w;
    		//do uruchomienia PROGRESBARa 
        	intent = new Intent(context, DummyActivityProgress.class);
    		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK + Intent.FLAG_ACTIVITY_SINGLE_TOP); 
    		intent.putExtra(DummyActivityProgress.PROGRESS_MAX, 
    				blue.mBlueMSP430_acc.registers.size() + blue.mBlueMSP430_temp.registers.size());
    		startActivity(intent);
    		
        	read_all_registers();
    	}
    	synchronized void unlock_this_thread(){
    		if(D) Log.d("ReadAllRegisters Thread", "notify()");
        	notify();
    	}

		
    	synchronized void read_all_registers(){
    		finished = false;
    		if(contDataSend){
	    		if(D) Log.d("ReadAllRegisters Thread", "wy��cz ci�g�e wysy�anie");
	    		blue.setCont_data_sending(false);
	        	sendByteMessage(blue.btDevice, 
	        			createMessageToSend(12, 0, 0, 0));			//wy��cz ci�g�e wysy�anie
	        	try {
	        		if(D) Log.d("ReadAllRegisters Thread", "wait() wy��cz ponowne ci�g�e wysy�anie");
	            	wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    	}
    		if(contDataSendNew){
	    		if(D) Log.d("ReadAllRegisters Thread", "wy��cz ci�g�e wysy�anie New");
	    		blue.setCont_data_sendingNew(false);
	        	sendByteMessage(blue.btDevice, 
	        			createMessageToSend(14, 0, 0, 0));			//wy��cz ci�g�e wysy�anie
	        	try {
	        		if(D) Log.d("ReadAllRegisters Thread", "wait() wy��cz ponowne ci�g�e wysy�anie New");
	            	wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    	}
    		if(true){
	    		if(D) Log.d("ReadAllRegisters Thread", "sprawdz flagi w MSP430");
	    		sendByteMessage(blue.btDevice, 
	    				createMessageToSend(39, 0, 0, 0));			//wy��cz ci�g�e wysy�anie
	        	try {
	        		if(D) Log.d("ReadAllRegisters Thread", "wait() sprawdz flagi w MSP430");
	            	wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    	}
    		if(true){
	    		if(D) Log.d("ReadAllRegisters Thread", "wy�lij aktualny czas do czujnika");
	    		byte[] byteMsg = new byte[7];
	    		Calendar c = Calendar.getInstance();
				//Date d = c.getTime();
				
				byteMsg[0] = 50;
	    		byteMsg[1] = (byte)(c.get(Calendar.YEAR) - 100);//d.getYear()-100);			//year 0 i 1900 for us i 0 is 2000
	    		byteMsg[2] = (byte)(c.get(Calendar.MONTH) + 1);//d.getMonth() + 1);			//0-11	dla tego + 1
	    		byteMsg[3] = (byte)(c.get(Calendar.DATE));//d.getDate());
	    		byteMsg[4] = (byte)(c.get(Calendar.HOUR_OF_DAY));//d.getHours());
	    		byteMsg[5] = (byte)(c.get(Calendar.MINUTE));//d.getMinutes());
	    		byteMsg[6] = (byte)(c.get(Calendar.SECOND));//d.getSeconds());
	    		sendByteMessage(blue.btDevice, byteMsg);			// wy�lij aktualn� dat� i godzin�
	        	try {
	        		if(D) Log.d("ReadAllRegisters Thread", "wait() wy�lij aktualny czas do czujnika");
	            	wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    	}
			for(BlueMSP430_register r : blue.mBlueMSP430_acc.registers){
    			if(D) Log.d("ReadAllRegisters Thread", "for(BlueMSP430Register r : mBlueMSP430_acc.registers)" + r.getReg());
            	sendByteMessage(blue.btDevice, 
            			createMessageToSend(3, BlueMSP430_acc.ACC_ADDR, r.getReg(), 0));
            	//zawiadom activity powi�zane z t� us�ug� o rozpocz�ciu odczytywania pocz�tkowych wartoci rejestr�w;
            	//do zaktualizowania PROGRESBARa 
            	intent.putExtra(DummyActivityProgress.PROGRESS_UPDATE, true);		// to tu w zasadzie nie potrzebne
    			startActivity(intent);
        		
            	try {
            		if(D) Log.d("ReadAllRegisters Thread", "wait()");
                	wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}           	
    		}
			for(BlueMSP430_register r : blue.mBlueMSP430_temp.registers){
    			if(D) Log.d("ReadAllRegisters Thread", "for(Register r : mBlueMSP430_temp.registers)" + r.getReg());
            	if(r.getMaxVal()<=0x0FF){		//odczytanie tylko jednego rejestru
            		sendByteMessage(blue.btDevice, 
            				createMessageToSend(3, BlueMSP430_temp.TEMP_ADDR, r.getReg(),0));
            	}
            	else{
            		sendByteMessage(blue.btDevice, 
            				createMessageToSend(4, BlueMSP430_temp.TEMP_ADDR, r.getReg(),0));
            	}
            	//zawiadom activity powi�zane z t� us�ug� o rozpocz�ciu odczytywania pocz�tkowych wartoci rejestr�w;
        		//do zaktualizowania PROGRESBARa 
            	intent.putExtra(DummyActivityProgress.PROGRESS_UPDATE, true);		// to tu w zasadzie nie potrzebne
    			startActivity(intent);
        		try {
            		if(D) Log.d("ReadAllRegisters Thread", "wait()");
                	wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}           	
    		}
			
			//Sprawdzamy czy nie zmieniły się parametry zaktresów czujnika jeśli tak to wysyłamy powiadomienie
			if(blue.updateSensorsParameters()){
				//TODO: g�upie bo wysy�a do wszystkich powiadomienie
				if(mBlueMSP430SensorEventListenerMap != null){
		    		for(BlueMSP430SensorEventListener listener: mBlueMSP430SensorEventListenerMap.keySet()){
		    			//listener.onBlueMSP430SensorRangeChaneged(mBlueMSP430SensorList.get(mBlueMSP430SensorEventListenerMap.get(item).getType()));
						for(BlueMSP430Sensor sensor: mBlueMSP430SensorEventListenerMap.get(listener)){
		    				listener.onBlueMSP430SensorRangeChaneged(
		    						blue.mBlueMSP430SensorList.get(sensor.getType()));
		    			}
					}
		    	}
				
			}
			
			if(contDataSend){
				if(D) Log.d("ReadAllRegisters Thread", "w��cz ponowne ci�g�e wysy�anie");
	        	sendByteMessage(blue.btDevice, 
	        			createMessageToSend(11, 0, 0, 0));				//w��cz ponowne ci�g�e wysy�anie
	        	blue.setCont_data_sending(true);
	        	try {
					if(D) Log.d("ReadAllRegisters Thread", "wait() w��cz ponowne ci�g�e wysy�anie");
	            	wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if(contDataSendNew){
				if(D) Log.d("ReadAllRegisters Thread", "w��cz ponowne ci�g�e wysy�anie New");
	        	sendByteMessage(blue.btDevice, 
	        			createMessageToSend(13, 0, 0, 0));				//w��cz ponowne ci�g�e wysy�anie
	        	blue.setCont_data_sendingNew(true);
	        	try {
					if(D) Log.d("ReadAllRegisters Thread", "wait() w��cz ponowne ci�g�e wysy�anie New");
	            	wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			finished = true;
			//zawiadom activity powi�zane z t� us�ug� o zako�czeniu odczytywania pocz�tkowych wartoci rejestr�w; 
			//do skasowania PROGRESBARa 
        	intent.putExtra(DummyActivityProgress.PROGRESS_CLOSE, true);
			startActivity(intent);
			
        	
		}
    }
    /**
     * klasa do w�tku do zapisania warto�ci rejestr�w w czujniku
     * @author Krzy�
     *
     */
    private class SendAllRegisters extends Thread{
    	
    	volatile boolean finished = false;
    	volatile boolean contDataSend = false;
    	volatile boolean contDataSendNew = false;
    	Context context;
    	Intent intent;    	
    	BlueMSP430 blue;
    	
    	SendAllRegisters(Context context, BlueMSP430 blue){
    		this.context = context;
    		//znajdź odbowiedni moduł BlueMSP430 na podstawie BluetoothDevice
    		this.blue = blue;
    	}
    	
    	@Override
    	public void run()
    	{
    		if(D) Log.d("SendAllRegisters Thread", "run()");
    		contDataSend = blue.isCont_data_sending();
        	contDataSendNew = blue.isCont_data_sendingNew();
        	
        	//zawiadom activity powi�zane z t� us�ug� o rozpocz�ciu odczytywania pocz�tkowych wartoci rejestr�w;
    		//do uruchomienia PROGRESBARa 
        	intent = new Intent(context, DummyActivityProgress.class);
    		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK + Intent.FLAG_ACTIVITY_SINGLE_TOP); 
    		intent.putExtra(DummyActivityProgress.PROGRESS_MAX, 
    				blue.mBlueMSP430_acc.registers.size() + blue.mBlueMSP430_temp.registers.size());
    		startActivity(intent);
    		
    		send_all_registers();
    	}
    	synchronized void unlock_this_thread(){
    		if(D) Log.d("SendAllRegisters Thread", "notify()");
        	notify();
    	}
    	
    	synchronized void send_all_registers(){
    		finished = false;
    		if(contDataSend){
	    		if(D) Log.d("SendAllRegisters Thread", "wy��cz ci�g�e wysy�anie");
	    		blue.setCont_data_sending(false);
	        	sendByteMessage(blue.btDevice, 
	        			createMessageToSend(12, 0, 0, 0));			//wy��cz ci�g�e wysy�anie
	        	try {
	        		if(D) Log.d("SendAllRegisters Thread", "wait() wy��cz ponowne ci�g�e wysy�anie");
	            	wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    	}
    		if(contDataSendNew){
	    		if(D) Log.d("SendAllRegisters Thread", "wy��cz ci�g�e wysy�anie New");
	    		blue.setCont_data_sendingNew(false);
	        	sendByteMessage(blue.btDevice, 
	        			createMessageToSend(14, 0, 0, 0));			//wy��cz ci�g�e wysy�anie
	        	try {
	        		if(D) Log.d("SendAllRegisters Thread", "wait() wy��cz ponowne ci�g�e wysy�anie New");
	            	wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    	}
    		if(true){
	    		if(D) Log.d("SendAllRegisters Thread", "wy�lij aktualny czas do czujnika");
	    		byte[] byteMsg = new byte[7];
	    		Calendar c = Calendar.getInstance();
				//Date d = c.getTime();
				
				byteMsg[0] = 50;
	    		byteMsg[1] = (byte)(c.get(Calendar.YEAR) - 100);//d.getYear()-100);			//year 0 i 1900 for us i 0 is 2000
	    		byteMsg[2] = (byte)(c.get(Calendar.MONTH) + 1);//d.getMonth() + 1);			//0-11	dla tego + 1
	    		byteMsg[3] = (byte)(c.get(Calendar.DATE));//d.getDate());
	    		byteMsg[4] = (byte)(c.get(Calendar.HOUR_OF_DAY));//d.getHours());
	    		byteMsg[5] = (byte)(c.get(Calendar.MINUTE));//d.getMinutes());
	    		byteMsg[6] = (byte)(c.get(Calendar.SECOND));//d.getSeconds());
	    		sendByteMessage(blue.btDevice, byteMsg);			// wy�lij aktualn� dat� i godzin�
	        	try {
	        		if(D) Log.d("SendAllRegisters Thread", "wait() wy�lij aktualny czas do czujnika");
	            	wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	    	}
			for(BlueMSP430_register r : blue.mBlueMSP430_acc.registers){
    			if(D) Log.d("SendAllRegisters Thread", "for(Register r : mBlueMSP430_acc.registers)" + r.getReg());
            	sendByteMessage(blue.btDevice, 
            			createMessageToSend(1, BlueMSP430_acc.ACC_ADDR, r.getReg(), r.getVal()));
            	
            	//zawiadom activity powi�zane z t� us�ug� o rozpocz�ciu odczytywania pocz�tkowych wartoci rejestr�w;
            	//do zaktualizowania PROGRESBARa 
            	intent.putExtra(DummyActivityProgress.PROGRESS_UPDATE, true);		// to tu w zasadzie nie potrzebne
    			startActivity(intent);
        		
    			try {
            		if(D) Log.d("SendAllRegisters Thread", "wait()");
                	wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}           	
    		}
			for(BlueMSP430_register r : blue.mBlueMSP430_temp.registers){
    			if(D) Log.d("SendAllRegisters Thread", "for(Register r : mBlueMSP430_temp.registers)" + r.getReg());
            	sendByteMessage(blue.btDevice, 
            			createMessageToSend(1, BlueMSP430_temp.TEMP_ADDR, r.getReg(), r.getVal()));

            	//zawiadom activity powi�zane z t� us�ug� o rozpocz�ciu odczytywania pocz�tkowych wartoci rejestr�w;
            	//do zaktualizowania PROGRESBARa 
            	intent.putExtra(DummyActivityProgress.PROGRESS_UPDATE, true);		// to tu w zasadzie nie potrzebne
    			startActivity(intent);
        		
            	try {
            		if(D) Log.d("SendAllRegisters Thread", "wait()");
                	wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}           	
    		}
			
			//Sprawdzamy czy nie zmieniły się parametry zaktresów czujnika jeśli tak to wysyłamy powiadomienie
			if(blue.updateSensorsParameters()){
				//TODO: g�upie bo wysy�a do wszystkich powiadomienie
				if(mBlueMSP430SensorEventListenerMap != null){
		    		for(BlueMSP430SensorEventListener listener: mBlueMSP430SensorEventListenerMap.keySet()){
		    			//listener.onBlueMSP430SensorRangeChaneged(mBlueMSP430SensorList.get(mBlueMSP430SensorEventListenerMap.get(item).getType()));
						for(BlueMSP430Sensor sensor: mBlueMSP430SensorEventListenerMap.get(listener)){
		    				listener.onBlueMSP430SensorRangeChaneged(
		    						blue.mBlueMSP430SensorList.get(sensor.getType()));
		    			}
					}
		    	}
				
			}
			if(contDataSend){
				if(D) Log.d("SendAllRegisters Thread", "w��cz ponowne ci�g�e wysy�anie");
	        	sendByteMessage(blue.btDevice, 
	        			createMessageToSend(11, 0, 0, 0));				//w��cz ponowne ci�g�e wysy�anie
	        	blue.setCont_data_sending(true);
	        	try {
					if(D) Log.d("SendAllRegisters Thread", "wait() w��cz ponowne ci�g�e wysy�anie");
	            	wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if(contDataSendNew){
				if(D) Log.d("SendAllRegisters Thread", "w��cz ponowne ci�g�e wysy�anie New");
	        	sendByteMessage(blue.btDevice, 
	        			createMessageToSend(13, 0, 0, 0));				//w��cz ponowne ci�g�e wysy�anie
	        	blue.setCont_data_sendingNew(true);
	        	try {
					if(D) Log.d("SendAllRegisters Thread", "wait() w��cz ponowne ci�g�e wysy�anie New");
	            	wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			finished = true;
			//zawiadom activity powi�zane z t� us�ug� o zako�czeniu odczytywania pocz�tkowych wartoci rejestr�w; 
			//do skasowania PROGRESBARa 
        	intent.putExtra(DummyActivityProgress.PROGRESS_CLOSE, true);
			startActivity(intent);
    		
    	}
    	
    }
//////////////////////////////////////////////////////


	// Allows the user to set an Listener and react to the event
	public void registerBlueMSP430Listener(BlueMSP430SensorEventListener listener, BlueMSP430Sensor sensor) {
		/*if(!mBlueMSP430SensorEventListenerMap.containsKey(listener)){
			mBlueMSP430SensorEventListenerMap.put(listener, sensor);
		}*/
		
		if(!mBlueMSP430SensorEventListenerMap.containsKey(listener)){
			mBlueMSP430SensorEventListenerMap.put(listener, new ArrayList<BlueMSP430Sensor>());
			mBlueMSP430SensorEventListenerMap.get(listener).add(sensor);
		}
		else{
			mBlueMSP430SensorEventListenerMap.get(listener).add(sensor);
		}			
	}
	// Allows the user to set an Listener and react to the event
	//TODO: trzeba bedzie wykasowa� z t�d BlueMSP430Sensor sensor jak si� zlikwiduje mOnBlueMSP430SensorChanegedListenerList
	public void unregisterOnBlueMSP430Listener(BlueMSP430SensorEventListener listener, BlueMSP430Sensor sensor) {
		/*mBlueMSP430SensorEventListenerMap.remove(listener);*/
		if(mBlueMSP430SensorEventListenerMap.containsKey(listener)){
			if(mBlueMSP430SensorEventListenerMap.get(listener).contains(sensor)){
				mBlueMSP430SensorEventListenerMap.get(listener).remove(sensor);
				if(mBlueMSP430SensorEventListenerMap.get(listener).size() == 0)
					mBlueMSP430SensorEventListenerMap.remove(listener);
			}
		}
	}
		
	
	// Define our custom Listener interface
    public interface BlueMSP430SensorEventListener {
    	public abstract void onBlueMSP430SensorValueChaneged(BlueMSP430SensorEvent arg0);
	    
    	public abstract void onBlueMSP430SensorRangeChaneged(BlueMSP430Sensor sensor);
	    
	    
    }

    /**
  	 * @return the mBluetoothMap
  	 */
  	public Map<BluetoothDevice, BluetoothService> getmBluetoothMap() {
  		return mBluetoothMap;
  	}

}
