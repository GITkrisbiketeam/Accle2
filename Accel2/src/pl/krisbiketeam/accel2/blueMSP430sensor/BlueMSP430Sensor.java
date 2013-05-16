package pl.krisbiketeam.accel2.blueMSP430sensor;

import android.bluetooth.BluetoothDevice;

public class BlueMSP430Sensor {
	
	public final static int TYPE_ALL = 100;
	public final static int TYPE_ACCELEROMETER = 101;
	public final static int TYPE_AMBIENT_TEMPERATURE = 102;
	public final static int TYPE_CORE_TEMPERATURE = 103;
	public final static int TYPE_SUPPLY_VOLTAGE = 104;
	private static final String[] sensorName = {"BlueMSP430 All"
												, "BlueMSP430 ACCELEROMETER"
												, "BlueMSP430 AMBIENT_TEMPERATURE"
												, "BlueMSP430 CORE_TEMPERATURE"
												, "BlueMSP430 SUPPLY_VOLTAGE"};
	
	
	private int type;
	private float  maximumRange;  
	private float  minimumRange;  
	private float  resolution;  
	private String  name;
	
	
	private int  minDelay;  
	private float  power;  
	private String  vendor;  
	private int  version; 
	
	private BluetoothDevice btDevice;

	public BlueMSP430Sensor(BlueMSP430Sensor sensor){
		this.type = sensor.type;
		this.maximumRange = sensor.maximumRange;
		this.minimumRange = sensor.minimumRange;
		this.resolution = sensor.resolution;
		this.name = sensor.name;
		this.btDevice = sensor.btDevice;
	}
	public BlueMSP430Sensor(int type, float  maximumRange,	float  resolution){
		this.type = type;
		this.maximumRange = maximumRange;
		this.minimumRange = -maximumRange;
		this.resolution = resolution;
		this.name = sensorName[type - 100];				//przesuwamy o ofset przypisany do typu czujnika
	}
	public BlueMSP430Sensor(int type, float  maximumRange,	float  minimumRange, float  resolution){
		this.type = type;
		this.maximumRange = maximumRange;
		this.minimumRange = minimumRange;
		this.resolution = resolution;
		this.name = sensorName[type - 100];				//przesuwamy o ofset przypisany do typu czujnika
	}
	public BlueMSP430Sensor(int type, float  maximumRange,	float  resolution, String  name){
		this(type, maximumRange, resolution);
		this.name = name;
	}
	public BlueMSP430Sensor(int type, float  maximumRange,	float  minimumRange,	float  resolution, String  name){
		this(type, maximumRange, minimumRange, resolution);
		this.name = name;
	}
	
	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}
	/**
	 * @return the maximumRange
	 */
	public float getMaximumRange() {
		return maximumRange;
	}
	/**
	 * @return the minimumRange
	 */
	public float getMinimumRange() {
		return minimumRange;
	}
	/**
	 * @return the resolution
	 */
	public float getResolution() {
		return resolution;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return the minDelay
	 */
	public int getMinDelay() {
		return minDelay;
	}
	/**
	 * @return the power
	 */
	public float getPower() {
		return power;
	}
	/**
	 * @return the vendor
	 */
	public String getVendor() {
		return vendor;
	}
	/**
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}
	
	/**
	 * @param maximumRange the maximumRange to set
	 */
	public void setMaximumRange(float maximumRange) {
		this.maximumRange = maximumRange;
	}
	/**
	 * @param minimumRange the minimumRange to set
	 */
	public void setMinimumRange(float minimumRange) {
		this.minimumRange = minimumRange;
	}
	
	/**
	 * @return the btAddress
	 */
	public BluetoothDevice getBtDevice() {
		return btDevice;
	}
	/**
	 * @param btAddress the btAddress to set
	 */
	public void setBtDevice(BluetoothDevice btDevice) {
		this.btDevice = btDevice;
	}
	
}
