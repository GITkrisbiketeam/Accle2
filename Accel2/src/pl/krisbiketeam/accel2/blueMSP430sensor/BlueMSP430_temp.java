package pl.krisbiketeam.accel2.blueMSP430sensor;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class BlueMSP430_temp extends BlueMSP430_sensor implements Parcelable, Cloneable //extends BlueMSP430
{

	//TODO
	//temperatura = 6,25 x TEMP_DATA
	
	/******************************** Temperature *********************************/
	public static final int TEMP_ADDR     = 0x48;    // Temperature sensor address
	public static final String DESCRIPTION	= "Temperatura";    // Accelerometer name
	public static final String CLASS_NAME	= "BlueMSP430_temp";    // Accelerometer name
	
	// TMP106 registers
	public static final int TEMP_DATA     = 0x00;    // Temperature register (R)  2 Bytes
	public static final int CTRL		  = 0x01;    // Configuration register (RW)
	public static final int T_LOW         = 0x02;    // High Temp Threshold register (RW) 2 Bytes
	public static final int T_HIGH        = 0x03;    // Low Temp Threshold register (RW) 2 Bytes

	// Configuration Register options (CONFIG_TEMP 0x01)
	public static final int SHUTD_MODE    = 0x01;    // Shutdown mode, continuous conversion state otherwise
	public static final int CONT_MODE	  = 0x00;	 // continuous conversion state otherwise
	public static final int THERM_MODE    = 0x02;    // Interrupt mode, comparator mode otherwise
	public static final int COMP_MODE     = 0x00;    // comparator mode otherwise
	public static final int ALERT_POL_HIG = 0x04;    // Alert pin is active high, active low otherwise
	public static final int ALERT_POL_LOW = 0x00;    // Alert pin is active low 
	public static final int FAULT_C1      = 0x00;    // Fault Queue 1 consecutive faults
	public static final int FAULT_C2      = 0x08;    // Fault Queue 2 consecutive faults
	public static final int FAULT_C4      = 0x10;    // Fault Queue 4 consecutive faults
	public static final int FAULT_C6      = 0x18;    // Fault Queue 6 consecutive faults
	public static final int RES_9BITS     = 0x00;    // Converter resolution 9 Bits (0.5 Celsius)
	public static final int RES_10BITS    = 0x20;    // Converter resolution 10 Bits (0.25 Celsius)
	public static final int RES_11BITS    = 0x40;    // Converter resolution 11 Bits (0.125 Celsius)
	public static final int RES_12BITS    = 0x60;    // Converter resolution 12 Bits (0.0625 Celsius)
	public static final int ONE_SHOT      = 0x80;    // Single temperature conversion in Shutdown mode
	/***************************** End of Temperature *****************************/
	
	public static final int SHUTD    	= 0x101;    // Shutdown mode, continuous conversion state otherwise
	public static final int THERM    	= 0x102;    // Interrupt mode, comparator mode otherwise
	public static final int ALERT_POL	= 0x104;    // Alert pin is active high, active low otherwise
	public static final int FAULT    	= 0x108;    // Fault Queue x consecutive faults
	public static final int RES      	= 0x120;    // Converter resolution x Bits 
	public static final int ONE      	= 0x180;    // Single temperature conversion in Shutdown mode
	

	
	// TMP106 registers
	int mTEMP_DATA;     //0x00    // Temperature register (R)
	int mCTRL;   		//0x01    // Configuration register (RW)
	int mT_LOW;         //0x02    // High Temp Threshold register (RW)
	int mT_HIGH;        //0x03    // Low Temp Threshold register (RW)
	
	int mSHUTD;			
	int mTHERM;
	int mALERT_POL;
	int mFAULT;
	int mRES;
	int mONE;
	private long mSensorTimeStamp;
	private float  temperatura;					// temperatura w stopniach celcjusza	
	

	public ArrayList<BlueMSP430_register> registers = new ArrayList<BlueMSP430_register>();
	public ArrayList<BlueMSP430_register> control_registers = new ArrayList<BlueMSP430_register>();
	
	public BlueMSP430_temp(){
		mTEMP_DATA = mCTRL = mT_LOW = mT_HIGH = mSHUTD = mTHERM = mALERT_POL = mFAULT = mRES = mONE = 0;
		registers.add(new BlueMSP430_register("TEMP_DATA" 	, TEMP_DATA,"Temperature register (R)  2 Bytes",			mTEMP_DATA, true, 0x0FFF));
		registers.add(new BlueMSP430_register("CTRL"		, CTRL,    	"Configuration register (RW)",					mCTRL, 		false));
		registers.add(new BlueMSP430_register("T_LOW"		, T_LOW,    "Low Temp Threshold register (RW) 2 Bytes", 	mT_LOW,		false, 0x0FFF));
		registers.add(new BlueMSP430_register("T_HIGH"		, T_HIGH,   "High Temp Threshold register (RW) 2 Bytes",		mT_HIGH,	false, 0x0FFF));
		
		control_registers.add(new BlueMSP430_register("SHUTD",    	SHUTD,		"CTRL; " +
																				"\n1 - Shutdown mode;" +
																				"\n0 - Continuous conversion state otherwise", 	mSHUTD, false));
		control_registers.add(new BlueMSP430_register("THERM",    	THERM,		"CTRL; " +
																				"\n1 - Interrupt mode;" +
																				"\n0 - Comparator mode otherwise", 				mTHERM, false));
		control_registers.add(new BlueMSP430_register("ALERT_POL",	ALERT_POL,	"CTRL; " +
																				"\n1 - Alert pin is active high;" +
																				"\n0 - Alert pin is active low", 				mALERT_POL, false));
		control_registers.add(new BlueMSP430_register("FAULT",    	FAULT,		"CTRL; Fault Queue " +
																				"\n0 - 1 consecutive fault" +
																				"\n1 - 2 consecutive faults" +
																				"\n2 - 3 consecutive faults" +
																				"\n3 - 4 consecutive faults", 					mFAULT, false, 3));
		control_registers.add(new BlueMSP430_register("RES",      	RES,		"CTRL; Converter resolution; " +
																				"\n0 - 9 Bits (0.5 Celsius)" +
																				"\n1 - 10 Bits (0.25 Celsius)" +
																				"\n2 - 11 Bits (0.125 Celsius)" +
																				"\n3 - 12 Bits (0.0625 Celsius)",				mRES, false, 3)); 
		control_registers.add(new BlueMSP430_register("ONE",      	ONE,		"CTRL; " +
																				"\n1 - Single temperature conversion in Shutdown mode", mONE, false));
		
		
	}
	public BlueMSP430_temp(Parcel in){
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
	 * tworzy wiadomoœæ do wys³ania pod adres tego czujnika TEMP_ADDR do zapisu rejestru konfiguracyjnego
	 * @param val	wartoœæ rejestru CTRL do zapisania
	 * @return	byte array z wiadomoœci¹ do wys³ania
	 */
	public byte[] createMessageToSend (int val){
		return createMessageToSend(1, TEMP_ADDR, CTRL, val);
	}
	@Deprecated
	/**
	 * tworzy wiadomoœæ do wys³ania pod adres tego czujnika TEMP_ADDR do wykonania jakieœ czynnoœci na rejestrach
	 * @param what	co ma byæ zrobnione zapis odczyt ile bajtów?
	 * @param reg	rejestr do odczytania zapisania
	 * @param val	wartoœæ rejestru CTRL do zapisania
	 * @return	byte array z wiadomoœci¹ do wys³ania
	 */
	public byte[] createMessageToSend (int what, int reg, int val){
		return createMessageToSend(what, TEMP_ADDR, reg, val);
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
			else
				return "error byte 0 ";
			
			message+= Byte.toString(msg[i]);
			i++;
		}
		if(msg.length>i){
			message+= "Adres: ";
			message+= decodeRegister(convertRawByte(msg[i]));
			i++;
		}
		if(msg.length>i){
			message+= "Rejest: ";
			message+= decodeRegister(convertRawByte(msg[i]));
			i++;
		}
		if(msg.length>i){
			if(msg[0]==1 | msg[0] == 2){			// by³a komenda do zapisania do rejestru
				message+= "War. Rej.: ";
				message+= decodeControlRegister(convertRawByte(msg[i]));
				if(msg[0]==2){						//by³y dwa bajty do zapisania
					i++;
					message+= decodeControlRegister(convertRawByte(msg[i]));
				}
			}
			else if(msg[0]==3 | msg[0] == 4){		//by³a komenda do odczytania z rejestru
				message+= "War. odczytana: ";
				message+= convertRawByte(msg[i]);
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
				message+= "sukces";
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
		case TEMP_DATA:
			mTEMP_DATA = (char)val;  	// Identification register (R)
			return true;
		case CTRL:		
			mCTRL = (char)val;    		// Configuration (por, operation modes) (RW)
			this.decodeControlRegister(CTRL, val);
			return true;
		case T_LOW:				
			mT_LOW = (char)val;    	// Status (por, EEPROM parity) (R)
			return true;
		case T_HIGH:				
			mT_HIGH = (char)val;    		// Reset Register (RW)
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
			case TEMP_ADDR:
				return "TEMP_ADDR";  	// Temperature sensor I2C address (R)
			case TEMP_DATA:		
				return "TEMP_DATA";		// Temperature register (R)  two bytes
			case CTRL:		
				return "CTRL";   // Configuration register (RW)
			case T_LOW:				
				return "T_LOW";    		// High Temp Threshold register (RW)
			case T_HIGH:				
				return "T_HIGH";    	// Low Temp Threshold register (RW)
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
			if((val & SHUTD_MODE) > 0)			// bit [0]
			{
				mSHUTD = 1;
			}
			else
				mSHUTD = 0;
			if((val & THERM_MODE) > 0)			// bit [1]
			{
				mTHERM = 1;
			}
			else
				mTHERM = 0;
			if((val & ALERT_POL_HIG) > 0)			// bit [2]
			{
				mALERT_POL = 1;
			}
			else
				mALERT_POL = 0;
			switch ((val & 0x18)){				// bit [4:3]
				case FAULT_C1:       		
					mFAULT = 0;    // Fault Queue 1 consecutive faults
					break;
				case FAULT_C2:      		
					mFAULT = 1;    // Fault Queue 2 consecutive faults
					break;
				case FAULT_C4:      		
					mFAULT = 2;    // Fault Queue 4 consecutive faults
					break;
				case FAULT_C6:       		
					mFAULT = 3;    // Fault Queue 6 consecutive faults
					break;
			}
			switch ((val & 0x60)){				// bit [6:5]
				case RES_9BITS:       		
					mRES = 0;    // Converter resolution 9 Bits (0.5 Celsius)
					break;
				case RES_10BITS:      		
					mRES = 1;    // Converter resolution 10 Bits (0.25 Celsius)
					break;
				case RES_11BITS:      		
					mRES = 2;    // Converter resolution 11 Bits (0.125 Celsius)
					break;
				case RES_12BITS:       		
					mRES = 3;    // Converter resolution 12 Bits (0.0625 Celsius)
					break;
			}
			if((val & ONE_SHOT) > 0)			// bit [4]
			{
				mONE = 1;
			}
			else
				mONE = 0;
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
		if((val & SHUTD_MODE) > 0)			// bit [0]
		{
			ctrl_reg += "SHUTD_MODE";
		}
		else
			ctrl_reg += "CONT_MODE";
		ctrl_reg += " ";
		if((val & THERM_MODE) > 0)			// bit [1]
		{
			ctrl_reg += "COMP_MODE";
		}
		else
			ctrl_reg += "CONT_MODE";
		ctrl_reg += " ";
		if((val & ALERT_POL) > 0)			// bit [2]
		{
			ctrl_reg += "ALERT_POL_HIGH";
		}
		else
			ctrl_reg += "ALERT_POL_LOW";
		ctrl_reg += " ";
		switch ((val & 0x18)){				// bit [4:3]
			case FAULT_C1:       		
				ctrl_reg += "FAULT_C1";    // Fault Queue 1 consecutive faults
				break;
			case FAULT_C2:      		
				ctrl_reg += "FAULT_C2";    // Fault Queue 2 consecutive faults
				break;
			case FAULT_C4:      		
				ctrl_reg += "FAULT_C4";    // Fault Queue 4 consecutive faults
				break;
			case FAULT_C6:       		
				ctrl_reg += "FAULT_C6";    // Fault Queue 6 consecutive faults
				break;
		}
		ctrl_reg += " ";
		switch ((val & 0x60)){				// bit [6:5]
			case RES_9BITS:       		
				ctrl_reg += "RES_9BITS";    // Converter resolution 9 Bits (0.5 Celsius)
				break;
			case RES_10BITS:      		
				ctrl_reg += "RES_10BITS";    // Converter resolution 9 Bits (0.25 Celsius)
				break;
			case RES_11BITS:      		
				ctrl_reg += "RES_11BITS";    // Converter resolution 11 Bits (0.125 Celsius)
				break;
			case RES_12BITS:       		
				ctrl_reg += "RES_12BITS";    // Converter resolution 11 Bits (0.0625 Celsius)
				break;
		}
		ctrl_reg += " ";
		if((val & ONE_SHOT) > 0)			// bit [4]
		{
			ctrl_reg += "ONE_SHOT";
		}
		else
			ctrl_reg += "CONT_MODE";
		return ctrl_reg;

	}
 	
 	/**
 	 * aktualizuje wartoœci rejestrów w "ArrayList<Register> registers" na podstawie lokalnych oddzielnych rejestrów
 	 */
 	public void update_registers(){
 		for(BlueMSP430_register r : registers){
 			switch (r.getReg()){
 			case TEMP_DATA:
 				r.unsecureSetVal(mTEMP_DATA);  		// Identification register (R)
 				break;
 			case CTRL:		
 				r.unsecureSetVal(mCTRL);    		// Configuration (por, operation modes) (RW)
 				decodeControlRegister(CTRL, mCTRL);
 				update_control_registers();
 				break;
 			case T_LOW:				
 				r.unsecureSetVal(mT_LOW);    		// Status (por, EEPROM parity) (R)
 				break;
 			case T_HIGH:				
 				r.unsecureSetVal(mT_HIGH);    		// Reset Register (RW)
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
 			case ONE:			
 				r.unsecureSetVal(mONE);
 				break;
 			case RES:			
 				r.unsecureSetVal(mRES);
 				break;
 			case FAULT:			
 				r.unsecureSetVal(mFAULT);
 				break;
 			case ALERT_POL:			
 				r.unsecureSetVal(mALERT_POL); 
 				break;
 			case THERM:			
 				r.unsecureSetVal(mTHERM);
 				break;
 			case SHUTD:			
 				r.unsecureSetVal(mSHUTD);
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
 		mCTRL += (ONE & 0xff) * (mONE);  
 		mCTRL += mRES << 6;
 		mCTRL += mFAULT << 4;
 		mCTRL += (ALERT_POL & 0xff) * (mALERT_POL);
 		mCTRL += (THERM & 0xff) * (mTHERM);
 		mCTRL += (SHUTD) * (mSHUTD);
 	}
 	/**
 	 * aktualizuje wartoœæ lokalnych oddzielnych rejestrów wraz ze steruj¹cymi na podstawie danego rejestru z "ArrayList<Register>"
 	 * @param r		rejestr na podstawie którego ma byæ zaktualizowany lokalny oddzielny rejestr
 	 */
	public void update_any_register(BlueMSP430_register r){
		switch (r.getReg()){
			case TEMP_DATA:
				mTEMP_DATA = r.getVal();  		// Identification register (R)
				break;
			case CTRL:		
				mCTRL = r.getVal();    		// Configuration (por, operation modes) (RW)
				decodeControlRegister(CTRL, mCTRL);
 				update_control_registers();
 				break;
			case T_LOW:				
				mT_LOW = r.getVal();    		// Status (por, EEPROM parity) (R)
				break;
			case T_HIGH:				
				mT_HIGH = r.getVal();     		// Reset Register (RW)
				break;
						
			// control_register	
			
			case SHUTD:			
				mSHUTD = r.getVal();    		// Accelerometer I2C address (R)if(r.short_label ==
 				update_control_register();
 				update_register(CTRL, mCTRL);
 				break;
 			case THERM:			
 				mTHERM = r.getVal();    	// Accelerometer I2C address (R)if(r.short_label ==
 				update_control_register();
 				update_register(CTRL, mCTRL);
 				break;
 			case ALERT_POL:			
 				mALERT_POL = r.getVal();    		// Accelerometer I2C address (R)if(r.short_label ==
 				update_control_register();
 				update_register(CTRL, mCTRL);
 				break;
 			case FAULT:			
 				mFAULT = r.getVal();    			// Accelerometer I2C address (R)if(r.short_label ==
 				update_control_register();
 				update_register(CTRL, mCTRL);
 				break;
 			case RES:			
 				mRES = r.getVal();    		// Accelerometer I2C address (R)if(r.short_label ==
 				update_control_register();
 				update_register(CTRL, mCTRL);
 				break;
 			case ONE:			
 				mONE = r.getVal();    			// Accelerometer I2C address (R)if(r.short_label ==
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
		return ((0x000000FF & (b)));
	}

	public long getmSensorTimeStamp() {
		return mSensorTimeStamp;
	}
	public float  getTemperatura() {
		return temperatura;
	}
	/**
	 * @return the registers
	 */
	@Override
	public ArrayList<BlueMSP430_register> getRegisters() {
		return registers;
	}
	/**
	 * @param registers the registers to set
	 */
	@Override
	public void setRegisters(ArrayList<BlueMSP430_register> registers) {
		this.registers = registers;
	}		
	/**
	 * @return the control_registers
	 */
	@Override
	public ArrayList<BlueMSP430_register> getControl_registers() {
		return control_registers;
	}
	/**
	 * @param control_registers the control_registers to set
	 */
	@Override
	public void setControl_registers(ArrayList<BlueMSP430_register> control_registers) {
		this.control_registers = control_registers;
	}
	/**
	 * @return the ctrl
	 */
	@Override
	public int getCtrl() {
		return CTRL;
	}

	
	
	public static final Parcelable.Creator<BlueMSP430_temp> CREATOR = new Parcelable.Creator<BlueMSP430_temp>() {
        @Override
		public BlueMSP430_temp createFromParcel(Parcel in) {
            return new BlueMSP430_temp (in);
        }

        @Override
		public BlueMSP430_temp [] newArray(int size) {
            return new BlueMSP430_temp [size];
        }
    };
	
	/**
	 * zapisuje dane z tej klasy (zmienne i rejestry) do Parcel'a do przes³ania np. do innego Activity czy tam Intent'a 
	 * @param in	Parcel'a do której mamy zapisaæ dane z tej klasy (zmienne i rejestry)
	 */	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		
		dest.writeInt(mTEMP_DATA);     	//0x00    // Temperature register (R)
		dest.writeInt(mCTRL);          	//0x01    // Configuration register (RW)
		dest.writeInt(mT_LOW);        	//0x02    // High Temp Threshold register (RW)
		dest.writeInt(mT_HIGH);         //0x03    // Low Temp Threshold register (RW)
		dest.writeInt(mSHUTD);   
		dest.writeInt(mTHERM);         
		dest.writeInt(mALERT_POL);
		dest.writeInt(mFAULT);
		dest.writeInt(mRES);
		dest.writeInt(mONE);
		
		
		dest.writeLong(mSensorTimeStamp);
		
		dest.writeTypedList (registers);
		dest.writeTypedList (control_registers);
	}
	/**
	 * odczytuje dane z Parcel'a przes³anego np. z innego Activity czy tam Intent'a i zapisuje je do lokalnych zmiennych i rejestrami
	 * @param in	Parcel'a z danymi
	 */
	private void readFromParcel(Parcel in) {
		mTEMP_DATA = in.readInt();		//0x00    // Temperature register (R)
		mCTRL = in.readInt();			//0x01    // Configuration register (RW)
		mT_LOW = in.readInt();          //0x02    // High Temp Threshold register (RW)
		mT_HIGH = in.readInt();        	//0x03    // Low Temp Threshold register (RW)
		mSHUTD = in.readInt();
		mTHERM = in.readInt();
		mALERT_POL = in.readInt();
		mFAULT = in.readInt();
		mRES = in.readInt();
		mONE = in.readInt();
		
		mSensorTimeStamp = in.readLong();
		
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
				// TODO Auto-generated catch block
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
		BlueMSP430_temp temp = (BlueMSP430_temp) super.clone();
		temp.control_registers = cloneList(this.control_registers);			//to coœ nie daia³a
		temp.registers = cloneList(this.registers);
		
		return temp;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof BlueMSP430_temp)){
			return false;
		}
		BlueMSP430_temp temp = (BlueMSP430_temp) o;
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
	}
	/* (non-Javadoc)
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
