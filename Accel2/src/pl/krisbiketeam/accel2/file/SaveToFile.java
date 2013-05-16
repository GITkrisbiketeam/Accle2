/**
 * 
 */
package pl.krisbiketeam.accel2.file;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import pl.krisbiketeam.accel2.MySensor;
import pl.krisbiketeam.accel2.MySensorEvent;
import pl.krisbiketeam.accel2.MySensorManager;
import pl.krisbiketeam.accel2.SensorService.OnMySensorChanegedListener;

import android.util.Log;
import android.content.Context;

/**
 * @author Krzy�
 *	klasa zapisuj�ca dane z czujnika w pliku
 *	
 */
public class SaveToFile extends Thread implements OnMySensorChanegedListener{
	// Debugging
	private static final String TAG = "SaveToFile";
	private static final boolean D = true;
	
	
	//Nazwa pliku do zapisania
	private String mFileName;
	
	private Context mContext;
	
	private OutputStream fos;
	private DataOutputStream dos = null;
	
	//obiekt z danymi z czujnika
	private Object data;
	int count = 0;
	
	private MySensor mMySensor; 
	private MySensorManager mMySensorManager; 
	
	
	
	private AtomicBoolean isRunning=new AtomicBoolean(false);
	
	SaveToFile(Context context, String fileName){
		mContext  = context;
		mFileName = fileName;
	}
	/**
	 * Tworzy obiekt zapisuj�cy dane z danego czujnika w pliku
	 * @param context	kontekst programu gdzie b�dzie tworzony plik
	 * @param fileName	nazwa zapisywanego pliku
	 * @param sensor	czujnik jaki b�dzie zapisywany b�dzie on rejestrowany do nas�uchiwnia
	 * @param sensorManager	sensorManager czujnika jaki b�dzie nas�uchiwany i zapisywany
	 */
	public SaveToFile(Context context, String fileName, MySensor sensor, MySensorManager sensorManager){
		this(context, fileName);
		mMySensor = sensor;
		mMySensorManager = sensorManager;
	}
	
	@Override
	public void run() {
		if (D) Log.w(TAG, "++ SAVE FILE THREAD RUN ++");
		// to tylko do test�w p�niej mo�na wykasowa�/ zrzuca logi o naruszeniu polityki w�tk�w
        
		createFile();	
		
	}
	synchronized private void wakeThisThread(){
		notify();
	}
	synchronized private void createFile(){
		boolean firstData = true;
		mFileName = mFileName.replace(":", ".");
		File myFile = new File(mContext.getExternalFilesDir(null),mFileName);
		//RandomAccessFile myFile = new RandomAccessFile(mContext.getExternalFilesDir(null),mFileName);
		
		try {
			fos = new FileOutputStream(myFile);
			dos = new DataOutputStream(fos);
												// nag��wek ??? bajt�w
			dos.writeInt(mMySensor.getType());					//4 bajty
			dos.writeFloat(mMySensor.getMaximumRange());		//4 bajty
			dos.writeFloat(mMySensor.getMinimumRange());		//4 bajty
			dos.writeFloat(mMySensor.getResolution());			//4 bajty
			dos.writeBoolean(mMySensor.isInternal());			//1 bajty
			dos.writeInt(mMySensor.getName().length());			//4 bajty
			dos.writeChars(mMySensor.getName());				//mMySensor.getName().length() bajt�w
			
			isRunning.set(true);	
			
			
		} catch (FileNotFoundException e) {
			if (D) Log.w(TAG, "ExternalStorage - Error writing " + myFile, e);
			isRunning.set(false);
			fos = null;
			return;
		} catch (IOException e) {
			if (D) Log.w(TAG, "IOException ", e);
			try {
				dos.close();
				return;
			} catch (IOException e1) {
				if (D) Log.w(TAG, "IOException ", e1);
				return;
			}
		}
		startMySensorListen();
		
		while(isRunning.get()){
			try {
				if(data!= null){
					if(data instanceof MySensorEvent){			// 20 bajt�w
						MySensorEvent newdata = (MySensorEvent)data;
						if(firstData){
							dos.writeInt(newdata.values.length);
							firstData = false;
						}
							
						dos.writeLong(newdata.timestamp);				//8 bajt�w
						dos.writeFloat(newdata.values[0]);				//4 bajty
						dos.writeFloat(newdata.values[1]);				//4 bajty
						dos.writeFloat(newdata.values[2]);				//4 bajty
					}
					count ++;
				}
			} catch (IOException e) {
				if (D) Log.w(TAG, "IOException ", e);
				try {dos.close();} 
				catch (IOException e1) {if (D) Log.w(TAG, "IOException ", e);}
			}
			//i usypiamy w�tek do odebrania nast�pnej warto�ci z czujnika
			try {
				wait();
			} catch (InterruptedException e) {
				if (D) Log.w(TAG, "wait() ", e);
			}
		}
	}
	synchronized private void closeFile(){
		if (D) Log.w(TAG, "-- SAVE FILE THREAD CLOSE --");
		stopMySensorListen();
		isRunning.set(false);
		
		if(dos != null){
			try {
				dos.close();
				dos = null;
			} catch (IOException e1) {
				if (D) Log.w(TAG, "IOException ", e1);
				dos = null;
			}
		}
		if(fos != null){
			try {
				fos.close();
				fos = null;
			} catch (IOException e) {
				if (D) Log.w(TAG, "Error flushing ora closing file ", e);
				fos = null;
			}
		}
	}
	
	public void stopSaveToFile(){
		//handlerSaveToFile.sendMessage(handlerSaveToFile.obtainMessage(CLOSE_FILE));
		closeFile();
	}
	
	//rozpocznij nas�uchiwanie zdarze� od ZEWN�TRZNEGO czujnika przyspieszenia
	private void startMySensorListen(){//) {
		if(mMySensorManager != null){
			mMySensorManager.registerMyListener(this, mMySensor);
		}			
	}
	//zako�cz nas�uchiwanie zdarze� od ZEWN�TRZNEGO czujnika przyspieszenia
	private void stopMySensorListen() {
		if(mMySensorManager != null){
			mMySensorManager.unregisterMyListener(this, mMySensor);
		}
	}
	
	
	@Override
	public void onMySensorValueChaneged(MySensorEvent event) {
		// TODO Auto-generated method stub
		if(event.sensor.getType() == this.mMySensor.getType()){
			data = event;
			wakeThisThread();
		}
	}
	
	/**
	 * @return the mMySensor
	 */
	public MySensor getmMySensor() {
		return mMySensor;
	}
	/**
	 * @return the isRunning
	 */
	public AtomicBoolean getIsRunning() {
		return isRunning;
	}
	
}
