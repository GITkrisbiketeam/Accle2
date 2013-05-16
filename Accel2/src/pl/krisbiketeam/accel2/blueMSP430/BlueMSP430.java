package pl.krisbiketeam.accel2.blueMSP430;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

import pl.krisbiketeam.accel2.blueMSP430sensor.BlueMSP430Sensor;
import pl.krisbiketeam.accel2.blueMSP430sensor.BlueMSP430SensorEvent;
import pl.krisbiketeam.accel2.blueMSP430sensor.BlueMSP430_acc;
import pl.krisbiketeam.accel2.blueMSP430sensor.BlueMSP430_temp;
import pl.krisbiketeam.accel2.settings.MySettings;

public class BlueMSP430 {

	private final static int PACKET_LENGTH = 10;
	
	
	private int tmp;
	public float accX;
	public float accY;
	public float accZ;
	public float mMaximumRange;
	public float mMinimumRange;
	public float mResolution;;
	public float temperature;
	public float coreTemperature;
	public float supplyVoltage;
	public long mSensorTimeStamp;
	private boolean cont_data_sending;				//czy mamy ci�g�� przesy�anie danych z accelerometra i temp w trybie 93
	private boolean cont_data_sendingNew;			//czy mamy ci�g�� przesy�anie danych z accelerometra i temp w trybie przerwania
	private boolean memoryWriteEnable;			//czy mamy zapisywa� dane w trybie przesy��nia przez bluetooth
	private boolean writeToFlash;			//czy mamy zapis do pami�ci Flash czy w przeciwnym przypadku RAM
	
	private int bluetoothConectionState;
	
	
	/**
     * odczytuje przechowuje i zapisuje ustawienia programu
     */
    MySettings mSetings;
	

	private Date dataTimeOfReadData;
//	private SaveToFileNew mSaveToFile; 
	
	public BlueMSP430_acc mBlueMSP430_acc;
    public BlueMSP430_temp mBlueMSP430_temp;
    
    public BluetoothDevice btDevice;
	
    
    /**
     * lista przechowuj�ca czujniki z BlueMSP430 
     */
    public List<BlueMSP430Sensor> mBlueMSP430SensorList = new MyArrayList<BlueMSP430Sensor>();
	/**
     * lista przechowuj�ca obiekty z danymi z czujnik�w z BlueMSP430 
     */
    private List<BlueMSP430SensorEvent> mBlueMSP430SensorEventList = new MyArrayList<BlueMSP430SensorEvent>();
	
    /**
     * Klasa przesuwaj�ca o offset zwracany obiek zArrayListy tak aby czujniki z blue MSP430 nie pkrywa�y sie 
     * wewn�trznymi cxzujnikami przesuni�cie o pierwszy element z listy czujnik�w  - BlueMSP430Sensor.TYPE_ALL
     * @author Krzys
     *
     */
    class MyArrayList<E> extends ArrayList<E>{
    	private static final long serialVersionUID = -7198884773428058756L;

		/**
		 * zwraca obiektr z listy przesuni�ty o offset czyli pierwszy obiekt z listy czujnik�w w BlueMSP430 to jest  - BlueMSP430Sensor.TYPE_ALL
		 * @param index		numer ideksu dla zwracanego czujnika przesuni�ty o BlueMSP430Sensor.TYPE_ALL
		 * @return 		obiekt z lisy czujnik�wo indeksie przesuni�tym o BlueMSP430Sensor.TYPE_ALL
     	*/
		public E get(int index) {
			// TODO Auto-generated method stub
			return super.get(index - BlueMSP430Sensor.TYPE_ALL);
		}
    }
	
	

    
    BlueMSP430(Context context, BluetoothDevice btDevice){
    	//pobierz ustawienia programu 
        //mPrefs = new Preferences(this.getApplicationContext(),mHandler);
        mSetings = new MySettings(context.getApplicationContext());
        
    	
    	mBlueMSP430_acc = new BlueMSP430_acc();
        mBlueMSP430_temp = new BlueMSP430_temp();
        this.btDevice = btDevice;
        
        //tworzymy list� czujnik�w BlueMSP430Sensor
        initBlueMSP430Sensors();
        
    }
    
    /**
	 * inicjalizuje list�  z czujnikami "BlueMSP430Sensor"
	 */
	private void initBlueMSP430Sensors(){
		
		mBlueMSP430SensorList.clear();
		mBlueMSP430SensorList.add(new BlueMSP430Sensor(BlueMSP430Sensor.TYPE_ALL, 128, 1));
		mBlueMSP430SensorList.add(new BlueMSP430Sensor(BlueMSP430Sensor.TYPE_ACCELEROMETER, 2, (float)(0.015625)));
		mBlueMSP430SensorList.add(new BlueMSP430Sensor(BlueMSP430Sensor.TYPE_AMBIENT_TEMPERATURE, 50, 0, (float)(0.01)));
		mBlueMSP430SensorList.add(new BlueMSP430Sensor(BlueMSP430Sensor.TYPE_CORE_TEMPERATURE, 50, 0, (float)(0.3)));
		mBlueMSP430SensorList.add(new BlueMSP430Sensor(BlueMSP430Sensor.TYPE_SUPPLY_VOLTAGE, 4, 2, (float)(0.01)));

		mBlueMSP430SensorEventList.clear();
		for(BlueMSP430Sensor item: mBlueMSP430SensorList){
			item.setBtDevice(btDevice);
			mBlueMSP430SensorEventList.add(new BlueMSP430SensorEvent(item));
		}
	}
	
	// struktura tablicy warto�ci do wys�ania do zdalnego urz�dzenia BT
			// 	[]	- 1 - zapisz 1 bajt do rejestru; 2 - zapisz 2 bajty do rejestru; 3 - odczytaj 1 bajt z rejestru; 4 - odczytaj 2 bajty z rejestru
			//		- 10 - sprawd� stan ci�g�ego wysy�ania danych
			//		- 11 - rozpocznij ci�g�� wysy�anie danych; 12 - przerwij ci�g�� wysy�anie danych; 
			//		- 93 - ci�g�e wysy�anie danych z czujnik accel i temp
			//	[]	- adres czyjnika w urz�dzeniu BT : ACC_ADDR, TEMP_ADDR
			//	[]	- rejestr do zapisania odczytania
			//	[]	- ewentualna warto�� do zapisania do rejestru
		    
		/**
		 * dekoduje odebran� wiadomo�� i zapisuje do lokalnych rejestr�w tej klasy
		 * @param msg	byte array z danymi odebranymi z modu�u MSP430
		 * @return	true je�li poprawnie zdekodowani, false je��i b��d w dekodowaniu
		 */
		public boolean decodeMessage(byte[] msg){
			if(msg.length>=4)
				switch(msg[0]){
				case 1:						//zapisz 1 bajt do rejestru;
					if(msg[4]==1){									//je�li zapis powi�d� si�
						switch(convertRawByte(msg[1])){
						case BlueMSP430_acc.ACC_ADDR:				//wiadomo�� od czujnika przyspieszenia
							return mBlueMSP430_acc.decodeRegister(convertRawByte(msg[2]),convertRawByte(msg[3]));
						case BlueMSP430_temp.TEMP_ADDR:				//wiadomo�� od czujnika temperatury
							return mBlueMSP430_temp.decodeRegister(convertRawByte(msg[2]),convertRawByte(msg[3]));
						default:
							return false;
						}
					}
					else return false;
				case 2:						//zapisz 2 bajty do rejestru;
					if(msg[5] == 1){							//je�li zapis powi�d� si�
						switch(convertRawByte(msg[1])){
						case BlueMSP430_acc.ACC_ADDR:				//wiadomo�� od czujnika przyspieszenia
							// tu nie ma dwubajtowych rejestr�w
							return false;
						case BlueMSP430_temp.TEMP_ADDR:				//wiadomo�� od czujnika temperatury
							//TODO co� zrobi� zeby kozysta�o z maxVal kt�re jest podniesione dla dwubajtowych warto�ci np do 0x0FFF
							int temp_val = convertRawByte(msg[3]) << 4;
							temp_val += convertRawByte(msg[3]) >> 4;
							return mBlueMSP430_temp.decodeRegister(convertRawByte(msg[2]),temp_val);
						default:
							return false;
						}
					}
					else return false;
				case 3:						//odczytaj 1 bajt z rejestru;
					if(msg[4]==1){									//je�li odczyt powi�d� si�
						switch(convertRawByte(msg[1])){
						case BlueMSP430_acc.ACC_ADDR:				//wiadomo�� od czujnika przyspieszenia
							return mBlueMSP430_acc.decodeRegister(convertRawByte(msg[2]),convertRawByte(msg[3]));
						case BlueMSP430_temp.TEMP_ADDR:				//wiadomo�� od czujnika temperatury
							return mBlueMSP430_temp.decodeRegister(convertRawByte(msg[2]),convertRawByte(msg[3]));
						default:
							return false;
						}
					}
					else return false;
				case 4:				//odczytaj 2 bajty z rejestru;
					if(msg[5] == 1){							//je�li odczyt powi�d� si� 
						switch(convertRawByte(msg[1])){
						case BlueMSP430_acc.ACC_ADDR:				//wiadomo�� od czujnika przyspieszenia
							// tu nie ma dwubajtowych rejestr�w
							return false;
						case BlueMSP430_temp.TEMP_ADDR:				//wiadomo�� od czujnika temperatury
							//TODO co� zrobi� zeby kozysta�o z maxVal kt�re jest podniesione dla dwubajtowych warto�ci np do 0x0FFF
							int temp_val = convertRawByte(msg[3]) >> 4;				//najpierw dolny bajt
							temp_val += convertRawByte(msg[4]) << 4;				//potem g�rny bajt
							return mBlueMSP430_temp.decodeRegister(convertRawByte(msg[2]),temp_val);
						default:
							return false;
						}
					}
					else return false;
				case 10:				//sprawdz stan ci�g�ego wysy�ania danych
					if(msg[1]==1)
						cont_data_sending = true;
					else
						cont_data_sending = false;
					return true;
				case 11:				//Rozpocznij ci�g�e wysy�anie danych
					if(msg[4]==1){
						cont_data_sending = true;
						return true;
					}
					else
						return false;
				case 12:				//Przerwij ci�g�e wysy�anie danych
					if(msg[4]==1){
						cont_data_sending = false;
						return true;
					}
					else
						return false;
				case 13:				//Rozpocznij ci�g�e wysy�anie danych
					if(msg[4]==1){
						cont_data_sendingNew = true;
						return true;
					}
					else
						return false;
				case 14:				//Przerwij ci�g�e wysy�anie danych
					if(msg[4]==1){
						cont_data_sendingNew = false;
						return true;
					}
					else
						return false;
				
				
				
				case 30:				//potwierdzenie komendy odczytania danych z pami�ci BlueMSP430
					if(msg[0] == 30)
					{
						
						return true;
					}
					else
						return false;
				case 31:				//w��cz zapis do pamieci  danych
					if(msg[4]==1){
						memoryWriteEnable = true;
						mSetings.setMemoryWriteEnable(memoryWriteEnable);
						return true;
					}
					else
						return false;
				case 32:				//wy��cz zapis do pamieci  danych
					if(msg[4]==1){
						memoryWriteEnable = false;
						mSetings.setMemoryWriteEnable(memoryWriteEnable);
						return true;
					}
					else
						return false;
				case 33:				//zapis do pami�ci Flash
					if(msg[4]==1){
						writeToFlash = true;
						mSetings.setWriteToFlash(writeToFlash);
						return true;
					}
					else
						return false;
				case 34:				//zapis do pami�ci RAM
					if(msg[4]==1){
						writeToFlash = false;
						mSetings.setWriteToFlash(writeToFlash);
						return true;
					}
					else
						return false;
				case 35:				//wyczyszczono pami�� flash
					if(msg[4]==1){
						return true;
					}
					else
						return false;
				case 39:				//sprawd� flagi
					if(msg[4]==1){
						if((msg[1] & 0x01) > 0){
							writeToFlash = true;
						}
						else{
							writeToFlash = false;
						}
						if((msg[1] & 0x02) > 0){
							memoryWriteEnable = true;
						}
						else{
							memoryWriteEnable = false;
						}
						mSetings.setMemoryWriteEnable(memoryWriteEnable);
						mSetings.setWriteToFlash(writeToFlash);
						
						return true;
					}
					else
						return false;
				case 50:				//sukces w zapisaniu czasu i daty
					if(msg[7]==1){
						
						return true;
					}
					else
						return false;
				case 51:				/** odczytano dat� rozpocz�cia zapisu danych **/
					if(dataTimeOfReadData == null){					//wys�ano dat� pocz�tku zapisu danych
						dataTimeOfReadData = new Date(0,0,0,0,0,0);
					}
					else{											//wys�ano dat� ko�ca zapisu danych
						
					}
					dataTimeOfReadData.setYear(msg[1] + 100);		// bo od 1900 liczy
					dataTimeOfReadData.setMonth(msg[2] - 1);		// bo od 0 liczy
					dataTimeOfReadData.setDate(msg[3]);
					
					return true;
				case 52:				/** odczytano czas rozpocz�cia zapisu danych **/
					if(dataTimeOfReadData == null) return false;					//jaki� b��d wyst�pi� nie odebrano najpierw 51
					
					if(dataTimeOfReadData.getHours() == 0){			//wys�ano dat� pocz�tku zapisu danych		
						dataTimeOfReadData.setHours(msg[1]);
						dataTimeOfReadData.setMinutes(msg[2]);
						dataTimeOfReadData.setSeconds(msg[3]);
						
						//odczytano pe�n� dat� wi�c tworzymy nowy plik do zapisu danych wys�anych zdalnie a przechowywanych w pami�ci BlueMSP430
					/*
						if(mSaveToFile == null){
							mSaveToFile = new SaveToFileNew(this, BlueMSP430Activity.DATA_FILE_NAME + " " + dataTimeOfReadData.toGMTString(), 
										128, 1, this);
								mSaveToFile.setName("SaveToFileThread");
								mSaveToFile.start();
								//notifyAdd();			//powiadomienie �e si� nagrywa
						}
						else{
							Toast.makeText(getApplicationContext(), "External storage not available", Toast.LENGTH_SHORT).show();
						}
					*/
					}
					else{											//wys�ano dat� ko�ca zapisu danych
						dataTimeOfReadData.setHours(msg[1]);
						dataTimeOfReadData.setMinutes(msg[2]);
						dataTimeOfReadData.setSeconds(msg[3]);
						/*
						// gdzie� dopisa� �eby zapisywa�o te� dat� i czas zako�czenia rejestrowania danych
						if(mSaveToFile !=null){
							mSaveToFile.stopSaveToFile();
							mSaveToFile = null;
							//notifyRemove();				//wy��czamy powiadomienie �e si� nagrywa
						}
						*/
						dataTimeOfReadData = null;
					}
					return true;				
				case 93:	/** valid data packet **/
					
					mSensorTimeStamp = System.nanoTime();
					//mamy w�aczone ci�g�e wysy�anie danych
					cont_data_sending = false;
					cont_data_sendingNew = true;
					
					//konwersja z signed na unsigned, dane wysy�ane s� w unsigned a java ma tylko signed
		    		accX = (float)msg[1] * mResolution;//(0x000000FF & ((int)msg[1]));
		    		accY = (float)msg[2] * mResolution;//(0x000000FF & ((int)msg[2]));
		    		accZ = (float)msg[3] * mResolution;//(0x000000FF & ((int)msg[3]));
		    		
		    		tmp = (0x000000FF & ((int)msg[5]));
		    		tmp<<=8;
		    		tmp |= (0x000000FF & ((int)msg[4]));
		    		tmp = (short)tmp;
		    		temperature = (float)tmp / 100;
		    		
		    		tmp = (0x000000FF & ((int)msg[7]));
		    		tmp<<=8;
		    		tmp |= (0x000000FF & ((int)msg[6]));
		    		tmp = (short)tmp;
		    		coreTemperature = (float)tmp / 10;
		    		
		    		tmp = (0x000000FF & ((int)msg[9]));
		    		tmp<<=8;
		    		tmp |= (0x000000FF & ((int)msg[8]));
		    		tmp = (short)tmp;
		    		supplyVoltage = (float)tmp / 1000;
		    		
		    		return true;
				case 94:		/** valid data packet STARA FUNKCJA **/
					if(calculate_check_sum(msg,PACKET_LENGTH) == msg[9])
					{
						mSensorTimeStamp = System.nanoTime();
						//mamy w�aczone ci�g�e wysy�anie danych
						cont_data_sending = true;		
						cont_data_sendingNew = false;
						
						//konwersja z signed na unsigned, dane wysy�ane s� w unsigned a java ma tylko signed
			    		tmp = (0x000000FF & ((int)msg[2]));
			    		tmp<<=8;
			    		tmp |= (0x000000FF & ((int)msg[1]));
			    		tmp = (short)tmp;
			    		accX = (float)tmp * mResolution;
			    		
			    		tmp = (0x000000FF & ((int)msg[4]));
			    		tmp<<=8;
			    		tmp |= (0x000000FF & ((int)msg[3])); 
			    		tmp = (short)tmp;
			    		accY = (float)tmp * mResolution;
			    		
			    		tmp = (0x000000FF & ((int)msg[6]));
			    		tmp<<=8;
			    		tmp |= (0x000000FF & ((int)msg[5]));
			    		tmp = (short)tmp;
			    		accZ = (float)tmp * mResolution;
			    		
			    		tmp = (0x000000FF & ((int)msg[8]));
			    		tmp<<=8;
			    		tmp |= (0x000000FF & ((int)msg[7]));
			    		tmp = (short)tmp;
			    		temperature = (float)tmp /100;
			    		
			    		return true;
					}
					else
						return false;
				case 95:		/** dane odczytane z pamieci Flash **/
					
					mSensorTimeStamp = (short)(0x000000FF & ((int)msg[4]));
					//mamy w�aczone ci�g�e wysy�anie danych
					cont_data_sending = false;		
					cont_data_sendingNew = false;
					
					accX = msg[1];
		    		accY = msg[2];
		    		accZ = msg[3];
		    		return true;
				default:
					return false;
			}
			return false;
			
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
		 * tworzy stringa z danych accX, accY, accZ i temp
		 * @return	zwraca wiadomo��
		 */
		@SuppressWarnings("unused")
		private String accelTempValueToString()
		{
			String msg_to_display;
			msg_to_display = Float.toString(accX) + "\t\t "+ Float.toString(accY) + "\t\t "+ Float.toString(accZ) + "\t\t "+ Float.toString(temperature);
	    	return msg_to_display;
		}
		
		/**
		 * oblicza sume kontroln� z danych z tabliczy data i zwraca j�
		 * @param data		tablica z bajtami danych
		 * @param data_length	d�ugo�� tablicy danych
		 * @return	obliczona suma kontrolan zaokr�glona do byte 
		 */
		private byte calculate_check_sum(byte[] data, int data_length)
		{
			byte check_sum = 0;
			for(int i =0; i < data_length - 1; i++)			//nie liczymy ostatniego bajtu tam gdzie jest zapisana sama suma kontrolna
				check_sum += data[i];
			return check_sum;
		}
		
    
		/**
		 * przekszta�ca warto� signed byte na unsigned byte i wpisuje j� do inta
		 * @param b		bajte ze znakiem do przekszta�cenia
		 * @return		int z warto�ci� przekszta�conego bajtu w werrsji bez znaku
		 */
		public int convertRawByte(byte b){
			return ((0x000000FF & ((int)b)));
		}
	    
		/**
	     * Zwraca domy�lny czujnik z listy czujnik�w tej klasy
	     * @param sensorType typ czujnika dozwr�cenia
	     * @return domy�lny czujnik wybrany z listy czujnik�w
	     */
		public BlueMSP430Sensor getDefaultSensor(int sensorType){
			return (mBlueMSP430SensorList.get(sensorType));
		}

		/**
		 * akualizuje parametry BlueMSP430Sensor
		 * @return	true jeśli była zmiana; false jeśli nie było zmiany
		 */
		public boolean updateSensorsParameters(){
			float temporary = mMaximumRange;
			if(mBlueMSP430_acc.mG_RANGE == 0){
				mMaximumRange = 2;		
			}
			else{
				mMaximumRange = 8;
			}
			mMinimumRange = -mMaximumRange;
			mResolution = mMaximumRange/128;
			
			mBlueMSP430SensorList.get(BlueMSP430Sensor.TYPE_ACCELEROMETER).setMaximumRange(mMaximumRange);
			mBlueMSP430SensorList.get(BlueMSP430Sensor.TYPE_ACCELEROMETER).setMinimumRange(mMinimumRange);
			if(temporary != mMaximumRange){
				
				/*
				//TODO: g�upie bo wysy�a do wszystkich powiadomienie
				if(mBlueMSP430SensorEventListenerMap != null){
		    		for(BlueMSP430SensorEventListener listener: mBlueMSP430SensorEventListenerMap.keySet()){
		    			//listener.onBlueMSP430SensorRangeChaneged(mBlueMSP430SensorList.get(mBlueMSP430SensorEventListenerMap.get(item).getType()));
						for(BlueMSP430Sensor sensor: mBlueMSP430SensorEventListenerMap.get(listener)){
		    				listener.onBlueMSP430SensorRangeChaneged(mBlueMSP430SensorList.get(sensor.getType()));
		    			}
					}
		    	}*/
				return true;
			}
			return false;
				
				
		}
		
		/**
		 * @return the cont_data_sending
		 */
		public boolean isCont_data_sending() {
			return cont_data_sending;
		}
		public void setCont_data_sending(boolean cont_data_sending) {
			this.cont_data_sending = cont_data_sending;
		}
		/**
		 * @return the cont_data_sending
		 */
		public boolean isCont_data_sendingNew() {
			return cont_data_sendingNew;
		}
		public void setCont_data_sendingNew(boolean cont_data_sendingNew) {
			this.cont_data_sendingNew = cont_data_sendingNew;
		}
		/**
		 * @return the mBlueMSP430SensorList
		 */
		public List<BlueMSP430Sensor> getmBlueMSP430SensorList() {
			return mBlueMSP430SensorList;
		}
		/**
		 * @return the mBlueMSP430SensorEventList
		 */
		public List<BlueMSP430SensorEvent> getmBlueMSP430SensorEventList() {
			return mBlueMSP430SensorEventList;
		}
		/**
		 * @return the bluetoothConectionCtate
		 */
		public int getBluetoothConectionState() {
			return bluetoothConectionState;
		}

		public void setBluetoothConectionState(int bluetoothConectionState) {
			this.bluetoothConectionState = bluetoothConectionState;
		}
		
}
