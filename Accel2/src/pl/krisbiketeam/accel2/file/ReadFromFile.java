package pl.krisbiketeam.accel2.file;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import pl.krisbiketeam.accel2.Accel2;
import pl.krisbiketeam.accel2.MySensor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class ReadFromFile extends AsyncTask<Object, Integer, ReadFromFileData> {
	// Debugging
	private static final String TAG = "SaveToFile";
	private static final boolean D = true;
	
	//Nazwa pliku do odczytania
	String mFileName;
	
	Context mContext;
	Messenger mMessenger;
	ProgressDialog progressDialog;
	
	
	
	public ReadFromFile(Context context, String fileName, Messenger messenger){
		mContext  = context;
		mFileName = fileName;
		mMessenger  = messenger;
	}
    
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute() {
		prepareProgressBar(100);//numOfPoints);			ustawiamy na procenty
		
	}

	@Override
	protected ReadFromFileData doInBackground(Object... arg0) {
		if (D) Log.w(TAG, "++ READ FILE THREAD doInBackground ++");
		String mFileName = (String)arg0[0];
		
		File myFile = new File(mContext.getExternalFilesDir(null),mFileName);
		DataInputStream dis = null;
		
		MySensor sensor;
		try {
			FileInputStream fis = new FileInputStream(myFile);
			//int numOfBytes = fis.available();
			int numOfBytes = (int) myFile.length();
			if(numOfBytes==0) return null;
			
			dis = new DataInputStream(fis);
			
			int type;
			float  maximumRange;  
			float  minimumRange;  
			float  resolution;  
			boolean internal;
			String  name;
			
			type = dis.readInt() + 100;					//4 bajty			dodajemy 100 bo to odczytany z ppliku
			maximumRange = dis.readFloat();			//4 bajty
			minimumRange = dis.readFloat();			//4 bajty
			resolution = dis.readFloat();			//4 bajty
			internal = dis.readBoolean();			//1 bajty
			int stringCharNumber = dis.readInt();	//4 bajty
			name = new String();
			for(int i = 0; i < stringCharNumber; i++){
				name += dis.readChar();				//stringCharNumber bajtów
			}
			name += " Read from File";
			sensor = new MySensor(type,  maximumRange,	 minimumRange,	resolution, name, internal);
			
			int sensorValueSize = dis.readInt();	// 4 bajty
			
			
			
			//odejmujemy liczbê bajtów nag³ówka
			numOfBytes -= (25 + stringCharNumber * 2);
			
			int dataValueSize = 4 * sensorValueSize + 8;
			
			int numOfPoints;
		    numOfPoints = numOfBytes / dataValueSize;				
			
		    //jeœli nie ma ¿adnych punktów zapisanych
		    if(numOfPoints==0) return null;
		  
		    //mHandler.sendMessage(mHandler.obtainMessage(BlueMSP430Activity.MESSAGE_PREPARE_PROGRESS, numOfBytes/20,-1,null));
			float progressDivider = (float)numOfPoints / 100f;
				
		    float[][] mReadFromFilePoints = new float[sensorValueSize][numOfPoints];
		    long[] mSensorTimeStampPoints = new long[numOfPoints];
		    
		    numOfPoints = 0;
		    int i;
			while(numOfBytes>0){
				mSensorTimeStampPoints[numOfPoints] = dis.readLong();	//8 bajtów	//mSensorTimeStamp
				
				for(i = 0; i < sensorValueSize; i++)
					mReadFromFilePoints[i][numOfPoints] = dis.readFloat();	//4 bajt
				
				numOfPoints ++;	
				numOfBytes -= dataValueSize;
				publishProgress((int)(numOfPoints/progressDivider));
			}
			ReadFromFileData mReadFromFileData = new ReadFromFileData(sensor, mReadFromFilePoints, mSensorTimeStampPoints);
			return mReadFromFileData;
			
		} catch (FileNotFoundException e) {
			if (D) Log.w(TAG, "File not found" + myFile, e);
			Toast.makeText(mContext.getApplicationContext(), "File not found", Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			if (D) Log.w(TAG, "Can't read from file" + myFile, e);
			try {dis.close();} 
			catch (IOException e1) {if (D) Log.w(TAG, "IOException ", e);}
		}
		
		return null;
	}

	
	
	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
	 */
	@Override
	protected void onProgressUpdate(Integer... progress) {
		progressDialog.setProgress(progress[0]);
	}

	@Override
	protected void onPostExecute(ReadFromFileData result) {
		if(result != null){
			//mReadData = result;
			try {
				mMessenger.send(Message.obtain(null,Accel2.MESSAGE_READ_FROM_FILE, result));
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		destroyProgressBar();
		if (D) Log.w(TAG, "-- READ FILE THREAD onPostExecute --");
	}
	
	private void prepareProgressBar(int count){
		progressDialog = new ProgressDialog(mContext);
		//progressDialog.setTitle("Progress");
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setProgress(0);
		progressDialog.setMax(count);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setOnCancelListener(new OnCancelListener()	{

			@Override
			public void onCancel(DialogInterface arg0) {
				cancelReadingFile();
			}
			
		});
		progressDialog.show();
	}
	
	private void destroyProgressBar(){
		
		((DialogInterface) progressDialog).dismiss();
	}
	/**
	 * zatrymuje wykonanie odczytywania danych z pliku
	 */
	private void cancelReadingFile(){
		this.cancel(true);
	}
	
	
 }