package pl.krisbiketeam.accel2.blueMSP430sensor;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class BlueMSP430_acc extends BlueMSP430_sensor implements Parcelable, Cloneable //extends BlueMSP430
{
	
	private final static int PACKET_LENGTH = 10;
	
	
	/******************************* Accelerometer ********************************/
	public static final int ACC_ADDR	= 0x1C;    // Accelerometer address
	public static final String DESCRIPTION	= "Akcelerometr";    // Accelerometer name
	public static final String CLASS_NAME	= "BlueMSP430_acc";    // Accelerometer name
	
	
	
	// CMA3000 registers
	public static final int WHO_AM_I 	= 0x00;    // Identification register (R)
	public static final int REVID		= 0x01;    // ASIC revision ID, fixed in metal (R)
	public static final int CTRL		= 0x02;    // Configuration (por, operation modes) (RW)
	public static final int STATUS		= 0x03;    // Status (por, EEPROM parity) (R)
	public static final int RSTR		= 0x04;    // Reset Register (RW)
	public static final int INT_STATUS	= 0x05;    // Interrupt status register (R)
	public static final int DOUTX		= 0x06;    // X channel output data register (R)
	public static final int DOUTY		= 0x07;    // Y channel output data register (R)
	public static final int DOUTZ		= 0x08;    // Z channel output data register (R)
	public static final int MDTHR		= 0x09;    // Motion detection threshold value register (RW)
	public static final int MDFFTMR		= 0x0A;    // Free fall and motion detection time register (RW)
	public static final int FFTHR		= 0x0B;    // Free fall threshold value register (RW)
	public static final int I2C_ADDR	= 0x0C;	   // Accelerometer I2C address (R)
	
	
	// Control Register options (CTRL 0x02)
	public static final int G_RANGE_2     = 0x80;    // 2g range, 8g range otherwise
	public static final int G_RANGE_8     = 0x00;    // 8g range otherwise
	public static final int INT_LEVEL_LOW = 0x40;    // INT pin is active high, active low otherwise
	public static final int INT_LEVEL_HIGH= 0x00;    // INT pin is active low
	public static final int MDET_NO_EXIT  = 0x20;    // Remain in motion detection mode, goes to measurement mode otherwise
	public static final int MDET_EXIT	  = 0x00;    // Device goes to measurement mode after motion is detected (400Hz ODR)
	public static final int I2C_DIS       = 0x10;    // I2C disabled, I2C enabled otherwise
	public static final int I2C_ENA       = 0x00;    // I2C enabled otherwise
	public static final int MODE_PD       = 0x00;    // Power Down - default mode
	public static final int MODE_100      = 0x02;    // Measurement mode 100 Hz ODR
	public static final int MODE_400      = 0x04;    // Measurement mode 400 Hz ODR
	public static final int MODE_40       = 0x06;    // Measurement mode 40 Hz ODR
	public static final int MODE_MD_10    = 0x08;    // Motion detection mode 10 Hz ODR
	public static final int MODE_FF_100   = 0x0A;    // Free fall detection mode 100 Hz ODR
	public static final int MODE_FF_400   = 0x0C;    // Free fall detection mode 400 Hz ODR
	public static final int INT_DIS       = 0x01;    // Interrupts disabled, interrupts enabled otherwise
	public static final int INT_ENA       = 0x00;    // Interrupts enabled otherwise
	/*************************** End of Accelerometer *****************************/
	
	public static final int G_RANGE		= 0x280;
	public static final int INT_LEVEL	= 0x240;
	public static final int MDET		= 0x220;
	public static final int I2C			= 0x210;
	public static final int MODE		= 0x202;
	public static final int INT			= 0x201;
	
	
	public int mWHO_AM_I;      //0x00    // Identification register (R)
	public int mREVID;         //0x01    // ASIC revision ID, fixed in metal (R)
	public int mCTRL;          //0x02    // Configuration (por, operation modes) (RW)
	public int mSTATUS;        //0x03    // Status (por, EEPROM parity) (R)
	public int mRSTR;          //0x04    // Reset Register (RW)
	public int mINT_STATUS;    //0x05    // Interrupt status register (R)
	public int mDOUTX;         //0x06    // X channel output data register (R)
	public int mDOUTY;         //0x07    // Y channel output data register (R)
	public int mDOUTZ;         //0x08    // Z channel output data register (R)
	public int mMDTHR;         //0x09    // Motion detection threshold value register (RW)
	public int mMDFFTMR;       //0x0A    // Free fall and motion detection time register (RW)
	public int mFFTHR;         //0x0B    // Free fall threshold value register (RW)
	public int mI2C_ADDR;      //0x1C    // Accelerometer I2C address (R)
	
	public int mG_RANGE;
	public int mINT_LEVEL;
	public int mMDET;
	public int mI2C;
	public int mMODE;
	public int mINT;
	
	private long mSensorTimeStamp;
	
	@Deprecated
	private int accX;
	@Deprecated
	private int accY;
	@Deprecated
	private int accZ;
	@Deprecated
	private int temp;
	@Deprecated
	private boolean cont_data_sending;				//czy mamy ci¹g³ê przesy³anie danych z accelerometra i temp w trybie 13
	
	
	
	public ArrayList<BlueMSP430_register> registers = new ArrayList<BlueMSP430_register>();
	public ArrayList<BlueMSP430_register> control_registers = new ArrayList<BlueMSP430_register>();
	
	
	
	public BlueMSP430_acc(){
		//mWHO_AM_I = mREVID = mCTRL = mSTATUS = mRSTR = mINT_STATUS = mDOUTX = mDOUTY = mDOUTZ = mMDTHR = mMDFFTMR = mFFTHR = mACC_ADDR = 0;
		accX = accY = accZ = temp = 0;	
		cont_data_sending = false;
		
		registers.add(new BlueMSP430_register("WHO_AM_I" 	, WHO_AM_I,    	"Identification register (R)", 						mWHO_AM_I, true));
		registers.add(new BlueMSP430_register("REVID"		, REVID,    	"ASIC revision ID, fixed in metal (R)",				mREVID, true));
		registers.add(new BlueMSP430_register("CTRL"		, CTRL,    		"Configuration (por, operation modes) (RW)", 		mCTRL, false));
		registers.add(new BlueMSP430_register("STATUS"		, STATUS,    	"Status (por, EEPROM parity) (R)",					mSTATUS, true));
		registers.add(new BlueMSP430_register("RSTR"		, RSTR,    		"Reset Register (RW)", 								mRSTR, false));
		registers.add(new BlueMSP430_register("INT_STATUS"	, INT_STATUS,   "Interrupt status register (R)",					mINT_STATUS, true));
		registers.add(new BlueMSP430_register("DOUTX"		, DOUTX,    	"X channel output data register (R)",				mDOUTX, true, 0x80));
		registers.add(new BlueMSP430_register("DOUTY"		, DOUTY,    	"Y channel output data register (R)",				mDOUTY, true, 0x80));
		registers.add(new BlueMSP430_register("DOUTZ"		, DOUTZ,    	"Z channel output data register (R)",				mDOUTZ, true, 0x80));
		registers.add(new BlueMSP430_register("MDTHR"		, MDTHR,    	"Motion detection threshold value register (RW)",	mMDTHR, false));
		registers.add(new BlueMSP430_register("MDFFTMR"		, MDFFTMR,    	"Free fall and motion detection time register (RW)",mMDFFTMR, false));
		registers.add(new BlueMSP430_register("FFTHR"		, FFTHR,    	"Free fall threshold value register (RW)",			mFFTHR, false));
		registers.add(new BlueMSP430_register("I2C_ADDR"	, I2C_ADDR,    	"Accelerometer I2C address (R)",					mI2C_ADDR, true));
		
		control_registers.add(new BlueMSP430_register("G_RANGE"		, G_RANGE, 	"CTRL (RW)" +
																				"\n0 - 2G range; " +
																				"\n1 - 8G range ",								mG_RANGE, false, 1));
		control_registers.add(new BlueMSP430_register("INT_LEVEL"	, INT_LEVEL,"CTRL (RW)" +
																				"\n0 - INT active low; " +
																				"\n1 - INT active high ",						mINT_LEVEL, false, 1));
		control_registers.add(new BlueMSP430_register("MDET"		, MDET,    	"CTRL (RW)" +
																				"\n0 - Remain in motion detection mode; " +
																				"\n1 - goes to measurement mode otherwise ",	mMDET, false, 1));
		control_registers.add(new BlueMSP430_register("I2C"			, I2C,    	"CTRL (RW)" +
																				"\n0 - I2C disabled; " +
																				"\n1 - I2C enabled  ",							mI2C, false, 1));
		control_registers.add(new BlueMSP430_register("MODE"		, MODE,    	"CTRL Mode (RW)" +
																				"\n0 - Power Down - default mode; " +
																				"\n1 - Measurement mode 100 Hz ODR; " +
																				"\n2 - Measurement mode 400 Hz ODR; " +
																				"\n3 - Measurement mode 40 Hz ODR; " +
																				"\n4 - Motion detection mode 10 Hz ODR; " +
																				"\n5 - Free fall detection mode 100 Hz ODR; " +
																				"\n6 - Free fall detection mode 400 Hz ODR; " +
																				"\n7 - Power Down; ",							mMODE, false, 7));
		control_registers.add(new BlueMSP430_register("INT"			, INT,    	"CTRL (RW)" +
																				"\n0 - interrupts disabled; " +
																				"\n1 - interrupts enabled ",					mINT, false, 1));
	}
	public BlueMSP430_acc(Parcel in){
		this();
		readFromParcel(in);
		//update_registers();
		//update_control_registers();
	}
	
	// struktura tablicy wartoœci do wys³ania do zdalnego urz¹dzenia BT
	// 	[]	- 1 - zapisz 1 bajt do rejestru; 2 - zapisz 2 bajty do rejestru; 3 - odczytaj 1 bajt z rejestru; 4 - odczytaj 2 bajty z rejestru
	//		- 10 - rozpocznij ci¹g³ê wysy³anie danych; 11 - przerwij ci¹g³ê wysy³anie danych; 13 - ci¹g³e wysy³anie danych z czujnik accel i temp
	//		- 20 - sprawdŸ stan ci¹g³ego wysy³ania danych
	//	[]	- adres czyjnika w urz¹dzeniu BT : ACC_ADDR, TEMP_ADDR
	//	[]	- rejestr do zapisania odczytania
	//	[]	- ewentualna wartoœæ do zapisania do rejestru
	
	
	@Deprecated
	/**
	 * tworzy wiadomoœæ do wys³ania pod adres tego czujnika ACC_ADDR do zapisu rejestru konfiguracyjnego
	 * @param val	wartoœæ rejestru CTRL do zapisania
	 * @return	byte array z wiadomoœci¹ do wys³ania
	 */
	public byte[] createMessageToSend (int val){
		return createMessageToSend(1, ACC_ADDR, CTRL, val);
	}
	@Deprecated
	/**
	 * tworzy wiadomoœæ do wys³ania pod adres tego czujnika ACC_ADDR do wykonania jakieœ czynnoœci na rejestrach
	 * @param what	co ma byæ zrobnione zapis odczyt ile bajtów?
	 * @param reg	rejestr do odczytania zapisania
	 * @param val	wartoœæ rejestru CTRL do zapisania
	 * @return	byte array z wiadomoœci¹ do wys³ania
	 */
	public byte[] createMessageToSend (int what, int reg, int val){
		return createMessageToSend(what, ACC_ADDR, reg, val);
	}
	@Deprecated
	/**
	 * tworzy wiadomoœæ do wys³ania do wykonania jakieœ czynnoœci na rejestrach
	 * 
	 * @param what	co ma byæ zrobnione zapis odczyt ile bajtów?
	 * @param add	adres czujnika
	 * @param reg	rejestr do odczytania zapisania
	 * @param ctrl_reg	wartoœæ rejestru CTRL do zapisania
	 * @return	byte array z wiadomoœci¹ do wys³ania
	 */
	public byte[] createMessageToSend (int what, int add, int reg, int val){
		byte[] byteMsg = new byte[4];
		byteMsg[0] = (byte)what;
		byteMsg[1] = (byte)add;
		byteMsg[2] = (byte)reg;
		byteMsg[3] = (byte)val;
		return byteMsg;
	}
		
	@Deprecated
	/**
	 * dekoduje odebran¹ wiadomoœæ i zapisuje do lokalnych rejestrów tej klasy
	 * @param msg	byte array z danymi odebranymi z modu³u MSP430
	 * @return	true jeœli poprawnie zdekodowani, false jeœ³i b³¹d w dekodowaniu
	 */
	public boolean decodeMessage(byte[] msg){
		if(msg.length>=4)
			switch(msg[0]){
			case 1:				//zapisz 1 bajt do rejestru;
				if (ACC_ADDR == convertRawByte(msg[1])){
					if(msg[4]==1)
					{
						boolean b = decodeRegister(convertRawByte(msg[2]),convertRawByte(msg[3]));
						//update_registers();
						return b;
					}
					else
						return false;
				}
				else
					return false;
			case 2:				//zapisz 2 bajty do rejestru;
				if (ACC_ADDR == convertRawByte(msg[1])){
					if(msg.length == 5)
						if(msg[5]==1)							////jeszcze nie zaimplementowane
							return true;
						else
							return false;
					else
						return false;
				}
				else
					return false;
			case 3:				//odczytaj 1 bajt z rejestru;
				if (ACC_ADDR == convertRawByte(msg[1])){
					if(msg[4]==1)
					{
						boolean b = decodeRegister(convertRawByte(msg[2]),convertRawByte(msg[3]));
						//update_registers();
						return b;
					}
					else
						return false;
				}
				else
					return false;
			case 4:				//odczytaj 2 bajty z rejestru;
				if (ACC_ADDR == convertRawByte(msg[1])){
					if(msg.length == 5)
						if(msg[5]==1)					////jeszcze nie zaimplementowane
							return true;
						else
							return false;
					else
						return false;
				}
				else
					return false;
			case 10:				//Rozpocznij ci¹g³e wysy³anie danych
				if(msg[4]==1){
					cont_data_sending = true;
					return true;
				}
				else
					return false;
			case 11:				//Przerwij ci¹g³e wysy³anie danych
				if(msg[4]==1){
					cont_data_sending = false;
					return true;
				}
				else
					return false;
			
			case 13:	//valid data packet
				if(calculate_check_sum(msg,PACKET_LENGTH) == msg[9])
				{
					mSensorTimeStamp = System.nanoTime();
					//mamy w³aczone ci¹g³e wysy³anie danych
					cont_data_sending = true;		
					
					//konwersja z signed na unsigned, dane wysy³ane s¹ w unsigned a java ma tylko signed
		    		accX = (0x000000FF & ((int)msg[2]));
		    		accX<<=8;
		    		accX |= (0x000000FF & ((int)msg[1]));
		    		accX = (short)accX;
		    		accY = (0x000000FF & ((int)msg[4]));
		    		accY<<=8;
		    		accY |= (0x000000FF & ((int)msg[3])); 
		    		accY = (short)accY;
		    		accZ = (0x000000FF & ((int)msg[6]));
		    		accZ<<=8;
		    		accZ |= (0x000000FF & ((int)msg[5]));
		    		accZ = (short)accZ;
		    		temp = (0x000000FF & ((int)msg[8]));
		    		temp<<=8;
		    		temp |= (0x000000FF & ((int)msg[7]));
		    		temp = (short)temp;
		    		
		    		return true;
				}
				else
					return false;
			case 20:				//sprawdz stan ci¹g³ego wysy³ania danych
				if(msg[1]==1)
					cont_data_sending = true;
				else
					cont_data_sending = false;
				return true;
			default:
				return false;
		}
		return false;
		
	}
	@Deprecated
	/**
	 * dekoduje odebran¹ wiadomoœæ i tworzy z niej stringa z t¹ wiadomoœci¹
	 * @param msg	byte array z danymi odebranymi z modu³u MSP430
	 * @return	String z wypisanymi parametrami odebranej wiadomoœci
	 */
	public String decodeReceivedMessage(byte[] msg){
		String message = "error";
		int i = 0;
		if(msg.length>i){
			if(msg[i]==1 | msg[i] == 2){
				message= "Akcja: zapisz ";
			}
			else if(msg[i]==3 | msg[i] == 4){
				message= "Akcja: odczytaj ";
			}
			else if(msg[i]== 10)
			{
				if(msg[4]==1)
					return "Rozpocznij ci¹g³e wysy³anie danych";
				else
					return "ERROR rozpocznij ci¹g³ê wysy³anie danych";
			}
			else if(msg[i]== 11)
			{
				if(msg[4]==1)
					return "Przerwij ci¹g³e wysy³anie danych";
				else
					return "ERROR przerwij ci¹g³ê wysy³anie danych";
			}
			else if(msg[i]== 13)
			{
				return "odbieranie skumulowanych danych przyspieszeniomierza i temperatury w 10 bajtowej tablicy";
			}
			else if(msg[i]== 20)
			{
				if(msg[1]==1)
					return "Ci¹g³e wysy³anie danych w³¹czone ON";
				else
					return "Ci¹g³e wysy³anie danych wy³¹czone OFF";
			}
			else
				return "error byte 0 ";
			
			message+= Byte.toString(msg[i]);
			i++;
		}
		if(msg.length>i){
			message+= " Adres: ";
			message+= decodeRegister(convertRawByte(msg[i]));
			i++;
		}
		if(msg.length>i){
			message+= " Rejest: ";
			message+= decodeRegister(convertRawByte(msg[i]));
			i++;
		}
		if(msg.length>i){
			if(msg[0]==1 | msg[0] == 2){			// by³a komenda do zapisania do rejestru
				message+= " War. zapisana: ";
				message+= convertRawByte(msg[i]);
				message+= " ";
				if(convertRawByte(msg[i-1]) == CTRL)
					message+= decodeControlRegister(convertRawByte(msg[i]));
				if(msg[0]==2){						//by³y dwa bajty do zapisania
					i++;
					if(convertRawByte(msg[i-2]) == CTRL)
						message+= decodeControlRegister(convertRawByte(msg[i]));
					
				}
			}
			else if(msg[0]==3 | msg[0] == 4){		//by³a komenda do odczytania z rejestru
				message+= " War. odczytana: ";
				message+= convertRawByte(msg[i]);
				message+= " ";
				if(convertRawByte(msg[i-1]) == CTRL)
					message+= decodeControlRegister(convertRawByte(msg[i]));
				if(msg[0]==4){						//by³y dwa bajty do odczytania
					i++;
					message+= convertRawByte(msg[i]);
				}
			}
			else									//tutaj nigdy nie wejdziemy mo¿e assretion zrobiæ :)
				return "error byte 2";
			i++;
		}
		if (msg.length>i){
			if(msg[i] == 1)
				message+= " sukces";
		}
		return message;
	}
	
	/**
 	 * dekoduje wartoœæ podanego rejestru i zapisuje wartoœæ do lokalnej zmiennej pod tym adresem
 	 * @param reg		rejestr
 	 * @param val		wartoœæ do zapisania do rejestru
 	 */
 	public boolean decodeRegister(int reg, int val){
 		update_register(reg, val);
		switch (reg){
		case WHO_AM_I:
			mWHO_AM_I = val;  	// Identification register (R)
			return true;
		case REVID:		
			mREVID = val;			// ASIC revision ID, fixed in metal (R)
			return true;
		case CTRL:		
			mCTRL = val;    		// Configuration (por, operation modes) (RW)
			this.decodeControlRegister(CTRL, val);
			return true;
		case STATUS:				
			mSTATUS = val;    	// Status (por, EEPROM parity) (R)
			return true;
		case RSTR:				
			mRSTR = val;    		// Reset Register (RW)
			return true;
		case INT_STATUS:		
			mINT_STATUS = val;   	// Interrupt status register (R)
			return true;
		case DOUTX:				
			mDOUTX = val;    		// X channel output data register (R)
			return true;
		case DOUTY:				
			mDOUTY = val;    		// Y channel output data register (R)
			return true;
		case DOUTZ:				
			mDOUTZ = val;    		// Z channel output data register (R)
			return true;
		case MDTHR:				
			mMDTHR = val;    		// Motion detection threshold value register (RW)
			return true;
		case MDFFTMR:				
			mMDFFTMR = val;    	// Free fall and motion detection time register (RW)
			return true;
		case FFTHR:				
			mFFTHR = val;    		// Free fall threshold value register (RW)
			return true;
		case I2C_ADDR:			
			mI2C_ADDR = val;    	// Accelerometer I2C address (R)
			return true;
		default:
			return false;
 		}
 	}
 	/**
 	 * dekoduje wartoœæ podanego rejestru i zwraca stringa ze skrótem nazwy rejestru
 	 * @param reg	rejestr do zdekodowania
 	 * @return	string ze skrócon¹ nazw¹ przekazanego rejestru
 	 */
 	public String decodeRegister(int reg){
 		switch (reg){
			case WHO_AM_I:
				return "Who_AM_I";  // Identification register (R)
			case REVID:		
				return "REVID";		// ASIC revision ID, fixed in metal (R)
			case CTRL:		
				return "CTRL";    	// Configuration (por, operation modes) (RW)
			case STATUS:				
				return "STATUS";    	// Status (por, EEPROM parity) (R)
			case RSTR:				
				return "RSTR";    	// Reset Register (RW)
			case INT_STATUS:		
				return "INT_STATUS";   	// Interrupt status register (R)
			case DOUTX:				
				return "DOUTX";    	// X channel output data register (R)
			case DOUTY:				
				return "DOUTY";    	// Y channel output data register (R)
			case DOUTZ:				
				return "DOUTZ";    	// Z channel output data register (R)
			case MDTHR:				
				return "MDTHR";    	// Motion detection threshold value register (RW)
			case MDFFTMR:				
				return "MDFFTMR";    	// Free fall and motion detection time register (RW)
			case FFTHR:				
				return "FFTHR";    	// Free fall threshold value register (RW)
			case I2C_ADDR:			
				return "I2C_ADDR";    	// Accelerometer I2C address (R)
			case ACC_ADDR:			
				return "ACC_ADDR";    	// Accelerometer I2C address (R)
			default:
				return "unknown";
		}
	}
 	
	/**
	 * dekoduje wartoœæ rejestru steruj¹cego CTRL i zapisuje w lokalnych rejestrach zdekodowan¹ wartoœæ
	 * @param reg	rejestr do zapsania powinien byæ CTRL
	 * @param val	wartoœæ rejestru CTRL do zdekodowania
	 * @return		jeœl podany by³ parametr rejestru CTRL to true jak inny rejestr to false b³¹d
	 */
 	public boolean decodeControlRegister(int reg, int val){
		if(reg == CTRL){
			if((val & INT_DIS) > 0)//INT_DIS)		// bit [0]
			{
				mINT = 0;
			}
			else
				mINT = 1;
			switch ((val & 0x0E)){				// bit [3:1]
				case MODE_PD:       		
					mMODE = 0;    // Power Down - default mode
					break;
				case MODE_100:      		
					mMODE = 1;    // Measurement mode 100 Hz ODR
					break;
				case MODE_400:      		
					mMODE = 2;    // Measurement mode 400 Hz ODR
					break;
				case MODE_40:       		
					mMODE = 3;    // Measurement mode 40 Hz ODR
					break;
				case MODE_MD_10:    		
					mMODE = 4;    // Motion detection mode 10 Hz ODR
					break;
				case MODE_FF_100:   		
					mMODE = 5;    // Free fall detection mode 100 Hz ODR
					break;
				case MODE_FF_400:   		
					mMODE = 6;    // Free fall detection mode 400 Hz ODR
					break;
				default:			// Power Down
					mMODE = 7;
			}
			if((val & I2C_DIS) > 0)			// bit [4]
			{
				mI2C = 0;
			}
			else
				mI2C = 1;
			if((val & MDET_NO_EXIT) > 0)			// bit [5]
			{
				mMDET = 0;
			}
			else
				mMDET = 1;
			if((val & INT_LEVEL_LOW) > 0)			// bit [6]
			{
				mINT_LEVEL = 0;
			}
			else
				mINT_LEVEL = 1;
			if((val & G_RANGE_2) > 0)			// bit [7]
			{
				mG_RANGE = 0;
			}
			else
				mG_RANGE = 1;
			update_control_registers();
		
			return true;
		}
		else
			return false;
	}
	/**
	 * dekoduje wartoœæ rejestru steruj¹cego CTRL i zwraca zdekodowan¹ wartoœæ w postaci stringa
	 * @param val	wartoœæ rejestru CTRL do zdekodowania
	 * @return		string ze zdekodowanym tekstem rejestru CTRL
	 */
 	public String decodeControlRegister(int val){
		String ctrl_reg = "";
		if((val & INT_DIS) > 0)//INT_DIS)		// bit [0]
		{
			ctrl_reg += "INT_DIS ";
		}
		else
			ctrl_reg += "INT_ENA ";
		ctrl_reg += " ";
		switch ((val & 0x0E)){				// bit [3:1]
			case MODE_PD:       		
				ctrl_reg += "MODE_PD ";    // Power Down - default mode
				break;
			case MODE_100:      		
				ctrl_reg += "MODE_100 ";    // Measurement mode 100 Hz ODR
				break;
			case MODE_400:      		
				ctrl_reg += "MODE_400 ";    // Measurement mode 400 Hz ODR
				break;
			case MODE_40:       		
				ctrl_reg += "MODE_40 ";    // Measurement mode 40 Hz ODR
				break;
			case MODE_MD_10:    		
				ctrl_reg += "MODE_MD_10 ";    // Motion detection mode 10 Hz ODR
				break;
			case MODE_FF_100:   		
				ctrl_reg += "MODE_FF_100 ";    // Free fall detection mode 100 Hz ODR
				break;
			case MODE_FF_400:   		
				ctrl_reg += "MODE_FF_400 ";    // Free fall detection mode 400 Hz ODR
				break;
			default:
				ctrl_reg += "MODE_PD ";    // Power Down - default mode
				break;
		}
		ctrl_reg += " ";
		if((val & I2C_DIS) > 0)			// bit [4]
		{
			ctrl_reg += "I2C_DIS ";
		}
		else
			ctrl_reg += "I2C_ENA ";
		ctrl_reg += " ";
		if((val & MDET_NO_EXIT) > 0)			// bit [5]
		{
			ctrl_reg += "MDET_NO_EXIT ";
		}
		else
			ctrl_reg += "MDET_EXIT ";
		ctrl_reg += " ";
		if((val & INT_LEVEL_LOW) > 0)			// bit [6]
		{
			ctrl_reg += "INT_LEVEL_LOW ";
		}
		else
			ctrl_reg += "INT_LEVEL_HIGH ";
		ctrl_reg += " ";
		if((val & G_RANGE_2) > 0)			// bit [7]
		{
			ctrl_reg += "G_RANGE_2 ";
		}
		else
			ctrl_reg += "G_RANGE_8 ";
		return ctrl_reg;
	}
	
 	/**
 	 * aktualizuje wartoœci rejestrów w "ArrayList<Register> registers" na podstawie lokalnych oddzielnych rejestrów
 	 */
 	public void update_registers(){
 		for(BlueMSP430_register r : registers){
 			switch (r.getReg()){
 			case WHO_AM_I:
 				r.unsecureSetVal(mWHO_AM_I);  		// Identification register (R)
 				break;
 			case REVID:		
 				r.unsecureSetVal(mREVID);			// ASIC revision ID, fixed in metal (R)
 				break;
 			case CTRL:		
 				r.unsecureSetVal(mCTRL);    		// Configuration (por, operation modes) (RW)
 				decodeControlRegister(CTRL, mCTRL);
 				update_control_registers();
 				break;
 			case STATUS:				
 				r.unsecureSetVal(mSTATUS);    		// Status (por, EEPROM parity) (R)
 				break;
 			case RSTR:				
 				r.unsecureSetVal(mRSTR);    		// Reset Register (RW)
 				break;
 			case INT_STATUS:		
 				r.unsecureSetVal(mINT_STATUS);   	// Interrupt status register (R)
 				break;
 			case DOUTX:				
 				r.unsecureSetVal(mDOUTX);    		// X channel output data register (R)
 				break;
 			case DOUTY:				
 				r.unsecureSetVal(mDOUTY);    		// Y channel output data register (R)
 				break;
 			case DOUTZ:				
 				r.unsecureSetVal(mDOUTZ);    		// Z channel output data register (R)
 				break;
 			case MDTHR:				
 				r.unsecureSetVal(mMDTHR);    		// Motion detection threshold value register (RW)
 				break;
 			case MDFFTMR:				
 				r.unsecureSetVal(mMDFFTMR); 	   	// Free fall and motion detection time register (RW)
 				break;
 			case FFTHR:				
 				r.unsecureSetVal(mFFTHR);    		// Free fall threshold value register (RW)
 				break; 
 			case I2C_ADDR:			
 				r.unsecureSetVal(mI2C_ADDR);    	// Accelerometer I2C address (R)if(r.short_label ==
 				break;
 			}
 		}
 	}
 	/**
 	 * aktualizuje jeden rejestr z "ArrayList<Register> registers" wpisuj¹c do wartoœci danego rejestru dan¹ wartoœæ
 	 * @param reg	rejestr do zaktualizowania
 	 * @param val	wartoœæ do wpisania do danego rejestru
 	 */
 	public void update_register(int reg, int val){
 		for(BlueMSP430_register r : registers){
 			if(r.getReg() == reg)
 				r.unsecureSetVal(val);
 		}
 	}
 	
 	/**
 	 * aktualizuje wartoœci poszczególnych rejestrów steruj¹cych w "ArrayList<Register> control_registers" na podstawie lokalnych oddzielnych rejestrów steruj¹cych
 	 */
 	public void update_control_registers(){
 		for(BlueMSP430_register r : control_registers){
 			switch (r.getReg()){
 			case G_RANGE:			
 				r.unsecureSetVal(mG_RANGE);    		// Accelerometer I2C address (R)if(r.short_label ==
 				break;
 			case INT_LEVEL:			
 				r.unsecureSetVal(mINT_LEVEL);    	// Accelerometer I2C address (R)if(r.short_label ==
 				break;
 			case MDET:			
 				r.unsecureSetVal(mMDET);    		// Accelerometer I2C address (R)if(r.short_label ==
 				break;
 			case I2C:			
 				r.unsecureSetVal(mI2C);    			// Accelerometer I2C address (R)if(r.short_label ==
 				break;
 			case MODE:			
 				r.unsecureSetVal(mMODE);    		// Accelerometer I2C address (R)if(r.short_label ==
 				break;
 			case INT:			
 				r.unsecureSetVal(mINT);    			// Accelerometer I2C address (R)if(r.short_label ==
 				break;
 			}
 		}
 	}
 	/**
 	 * aktualizuje jeden rejestr steruj¹cy z "ArrayList<Register> control_registers" wpisuj¹c do wartoœci danego rejestru dan¹ wartoœæ
 	 * @param reg	rejestr sterujacy do zaktualizowania
 	 * @param val	wartoœæ do wpisania do danego rejestru steruj¹cego
 	 */
 	public void update_control_register(int reg, int val){
 		for(BlueMSP430_register r : control_registers){
 			if(r.getReg() == reg)
 				r.unsecureSetVal(val);
 		}
 	}
	/**
	 * aktualizuje wartoœæ rejestru steruj¹cego mCTRL na podstawie oddzielnych wartoœci danych rejestrów sterujacych
	 */
 	public void update_control_register(){
 		mCTRL = 0;
 		mCTRL += (G_RANGE & 0xff) * (mG_RANGE^1);  
 		mCTRL += (INT_LEVEL & 0xff) * (mINT_LEVEL^1);
 		mCTRL += (MDET & 0xff) * (mMDET^1);
 		mCTRL += (I2C & 0xff) * (mI2C^1);
 		mCTRL += mMODE<<1;
 		mCTRL += (INT & 0xff) * (mINT^1);
 	}
 	/**
 	 * aktualizuje wartoœæ lokalnych oddzielnych rejestrów wraz ze steruj¹cymi na podstawie danego rejestru z "ArrayList<Register>"
 	 * @param r		rejestr na podstawie którego ma byæ zaktualizowany lokalny oddzielny rejestr
 	 */
	public void update_any_register(BlueMSP430_register r){
 		switch (r.getReg()){
			case WHO_AM_I:
				mWHO_AM_I = r.getVal();  		// Identification register (R)
				break;
			case REVID:		
				mREVID = r.getVal();			// ASIC revision ID, fixed in metal (R)
				break;
			case CTRL:		
				mCTRL = r.getVal();    		// Configuration (por, operation modes) (RW)
				decodeControlRegister(CTRL, mCTRL);
 				update_control_registers();
 				break;
			case STATUS:				
				mSTATUS = r.getVal();    		// Status (por, EEPROM parity) (R)
				break;
			case RSTR:				
				mRSTR = r.getVal();     		// Reset Register (RW)
				break;
			case INT_STATUS:		
				mINT_STATUS = r.getVal();   	// Interrupt status register (R)
				break;
			case DOUTX:				
				mDOUTX = r.getVal();    		// X channel output data register (R)
				break;
			case DOUTY:				
				mDOUTY = r.getVal();    		// Y channel output data register (R)
				break;
			case DOUTZ:				
				mDOUTZ = r.getVal();    		// Z channel output data register (R)
				break;
			case MDTHR:				
				mMDTHR = r.getVal();    		// Motion detection threshold value register (RW)
				break;
			case MDFFTMR:				
				mMDFFTMR = r.getVal(); 	   	// Free fall and motion detection time register (RW)
				break;
			case FFTHR:				
				mFFTHR = r.getVal();    		// Free fall threshold value register (RW)
				break; 
			case I2C_ADDR:			
				mI2C_ADDR = r.getVal();    	// Accelerometer I2C address (R)
				break;
			
			// control_register	
			
			case G_RANGE:			
 				mG_RANGE = r.getVal();    		// Accelerometer I2C address (R)if(r.short_label ==
 				update_control_register();
 				update_register(CTRL, mCTRL);
 				break;
 			case INT_LEVEL:			
 				mINT_LEVEL = r.getVal();    	// Accelerometer I2C address (R)if(r.short_label ==
 				update_control_register();
 				update_register(CTRL, mCTRL);
 				break;
 			case MDET:			
 				mMDET = r.getVal();    		// Accelerometer I2C address (R)if(r.short_label ==
 				update_control_register();
 				update_register(CTRL, mCTRL);
 				break;
 			case I2C:			
 				mI2C = r.getVal();    			// Accelerometer I2C address (R)if(r.short_label ==
 				update_control_register();
 				update_register(CTRL, mCTRL);
 				break;
 			case MODE:			
 				mMODE = r.getVal();    		// Accelerometer I2C address (R)if(r.short_label ==
 				update_control_register();
 				update_register(CTRL, mCTRL);
 				break;
 			case INT:			
 				mINT = r.getVal();    			// Accelerometer I2C address (R)if(r.short_label ==
 				update_control_register();
 				update_register(CTRL, mCTRL);
 				break;
			}
 	}
	

	@Deprecated
	/**
	 * przekszta³ca wartoœ signed byte na unsigned byte i wpisuje j¹ do inta
	 * @param b		bajte ze znakiem do przekszta³cenia
	 * @return		int z wartoœci¹ przekszta³conego bajtu w werrsji bez znaku
	 */
	public int convertRawByte(byte b){
		return ((0x000000FF & ((int)b)));
	}
	
	@Deprecated
	/**
	 * oblicza sume kontroln¹ z danych z tabliczy data i zwraca j¹
	 * @param data		tablica z bajtami danych
	 * @param data_length	d³ugoœæ tablicy danych
	 * @return	obliczona suma kontrolan zaokr¹glona do byte 
	 */
	private byte calculate_check_sum(byte[] data, int data_length)
	{
		byte check_sum = 0;
		for(int i =0; i < data_length - 1; i++)			//nie liczymy ostatniego bajtu tam gdzie jest zapisana sama suma kontrolna
			check_sum += data[i];
		return check_sum;
	}
	
	@Deprecated
	public int getaccX(){
		return accX;
	}
	@Deprecated
	public int getaccY(){
		return accY;
	}
	@Deprecated
	public int getaccZ(){
		return accZ;
	}
	@Deprecated
	public int gettemp(){
		return temp;
	}
	public long getmSensorTimeStamp() {
		return mSensorTimeStamp;
	}
	@Deprecated
	public boolean getcont_data_sending() {
		return cont_data_sending;
	}
	/**
	 * @return the registers
	 */
	public ArrayList<BlueMSP430_register> getRegisters() {
		return registers;
	}
	/**
	 * @param registers the registers to set
	 */
	public void setRegisters(ArrayList<BlueMSP430_register> registers) {
		this.registers = registers;
	}		
	/**
	 * @return the control_registers
	 */
	public ArrayList<BlueMSP430_register> getControl_registers() {
		return control_registers;
	}
	/**
	 * @param control_registers the control_registers to set
	 */
	public void setControl_registers(ArrayList<BlueMSP430_register> control_registers) {
		this.control_registers = control_registers;
	}
	/**
	 * @return the ctrl
	 */
	public int getCtrl() {
		return CTRL;
	}

	
	
	
	
	
	public static final Parcelable.Creator<BlueMSP430_acc> CREATOR = new Parcelable.Creator<BlueMSP430_acc>() {
        public BlueMSP430_acc createFromParcel(Parcel in) {
            return new BlueMSP430_acc (in);
        }

        public BlueMSP430_acc [] newArray(int size) {
            return new BlueMSP430_acc [size];
        }
    };
	

    /**
	 * zapisuje dane z tej klasy (zmienne i rejestry) do Parcel'a do przes³ania np. do innego Activity czy tam Intent'a 
	 * @param in	Parcel'a do której mamy zapisaæ dane z tej klasy (zmienne i rejestry)
	 */	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mWHO_AM_I);      //0x00    // Identification register (R)
		dest.writeInt(mREVID);         //0x01    // ASIC revision ID, fixed in metal (R)
		dest.writeInt(mCTRL);          //0x02    // Configuration (por, operation modes) (RW)
		dest.writeInt(mSTATUS);        //0x03    // Status (por, EEPROM parity) (R)
		dest.writeInt(mRSTR);          //0x04    // Reset Register (RW)
		dest.writeInt(mINT_STATUS);    //0x05    // Interrupt status register (R)
		dest.writeInt(mDOUTX);         //0x06    // X channel output data register (R)
		dest.writeInt(mDOUTY);         //0x07    // Y channel output data register (R)
		dest.writeInt(mDOUTZ);         //0x08    // Z channel output data register (R)
		dest.writeInt(mMDTHR);         //0x09    // Motion detection threshold value register (RW)
		dest.writeInt(mMDFFTMR);       //0x0A    // Free fall and motion detection time register (RW)
		dest.writeInt(mFFTHR);         //0x0B    // Free fall threshold value register (RW)
		dest.writeInt(mI2C_ADDR);      //0x1C    // Accelerometer I2C address (R)
		
		dest.writeInt(mG_RANGE);
		dest.writeInt(mINT_LEVEL);
		dest.writeInt(mMDET);
		dest.writeInt(mI2C);
		dest.writeInt(mMODE);
		dest.writeInt(mINT);
		
		dest.writeInt(accX);
		dest.writeInt(accY);
		dest.writeInt(accZ);
		dest.writeInt(temp);
		dest.writeLong(mSensorTimeStamp);
		dest.writeValue(Boolean.valueOf(cont_data_sending));
		
		dest.writeTypedList (registers);
		dest.writeTypedList (control_registers);
		
	}
	
	/**
	 * odczytuje dane z Parcel'a przes³anego np. z innego Activity czy tam Intent'a i zapisuje je do lokalnych zmiennych i rejestrami
	 * @param in	Parcel'a z danymi
	 */
	private void readFromParcel(Parcel in) {
		mWHO_AM_I = in.readInt();      //0x00    // Identification register (R)
		mREVID = in.readInt();         //0x01    // ASIC revision ID, fixed in metal (R)
		mCTRL = in.readInt();          //0x02    // Configuration (por, operation modes) (RW)
		mSTATUS = in.readInt();        //0x03    // Status (por, EEPROM parity) (R)
		mRSTR = in.readInt();          //0x04    // Reset Register (RW)
		mINT_STATUS = in.readInt();    //0x05    // Interrupt status register (R)
		mDOUTX = in.readInt();         //0x06    // X channel output data register (R)
		mDOUTY = in.readInt();         //0x07    // Y channel output data register (R)
		mDOUTZ = in.readInt();         //0x08    // Z channel output data register (R)
		mMDTHR = in.readInt();         //0x09    // Motion detection threshold value register (RW)
		mMDFFTMR = in.readInt();       //0x0A    // Free fall and motion detection time register (RW)
		mFFTHR = in.readInt();         //0x0B    // Free fall threshold value register (RW)
		mI2C_ADDR = in.readInt();      //0x1C    // Accelerometer I2C address (R)
		
		mG_RANGE = in.readInt();
		mINT_LEVEL = in.readInt();
		mMDET = in.readInt();
		mI2C = in.readInt();
		mMODE = in.readInt();
		mINT = in.readInt();
		
		accX = in.readInt();
		accY = in.readInt();
		accZ = in.readInt();
		temp = in.readInt();
		mSensorTimeStamp = in.readLong();
		cont_data_sending = (Boolean) in.readValue(null);
		
		in.readTypedList(registers, BlueMSP430_register.CREATOR);
		in.readTypedList(control_registers, BlueMSP430_register.CREATOR);
				
	}
	
	
	
	/**
	 * deep cloneing, kopiuje zawartoœæ ArrayList'a zapodanego na wejœcie tej funkcji
	 * @param list ArrayList do sklonowania
	 * @return sklonowana ArrayList'a 
	 */
	public static ArrayList<BlueMSP430_register> cloneList(ArrayList<BlueMSP430_register> list) {
		ArrayList<BlueMSP430_register> clone = new ArrayList<BlueMSP430_register>(list.size());
	    for(BlueMSP430_register item: list)
			try {
				clone.add((BlueMSP430_register) item.clone());
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
	    return clone;
	}
	
	@Deprecated
	/**
	 * porównuje ArrayList podane jako parametr z odpowiednim ArrayList z tej klasy
	 * @param list	ArrayList które ma byæ porównane z arrayList z tej klasy
	 * @return
	 */
	public  boolean equalsList(ArrayList<BlueMSP430_register> list) {
		ArrayList<BlueMSP430_register> tempList;
		//TODO to w zasadzie niepotrzebne bo i tak jak coœ zapisujemy do control_registers to siê w registers aktualizuje
		//sprawdzamy któr¹ liste podano jako argument tej funkcji
		if(list.size() == registers.size()){
			tempList = registers;
		}
		else if(list.size() == control_registers.size()){
			tempList = control_registers;
		}
		else return false;
		for(BlueMSP430_register item: list){
			if(item.getVal() == tempList.get(list.indexOf(item)).getVal())
				continue;
			else
				return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		BlueMSP430_acc temp = (BlueMSP430_acc) super.clone();
		temp.control_registers = cloneList(this.control_registers);			//to coœ nie daia³a
		temp.registers = cloneList(this.registers);
		
		return temp;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof BlueMSP430_acc)){
			return false;
		}
		BlueMSP430_acc temp = (BlueMSP430_acc) o;
		//TODO sprawdzamy tylko "registers"
		for(BlueMSP430_register item: temp.registers){
			if(item.getVal() == registers.get(temp.registers.indexOf(item)).getVal())
				continue;
			else
				return false;
		}
		return true;
		//return super.equals(o);
	}	
	/* (non-Javadoc)
	 * @see java.lang.Object#describeContents()
	 */
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return CLASS_NAME;
	}
	@Override
	public String describeSensor() {
		return DESCRIPTION;
	}
	
}
