package pl.krisbiketeam.accel2;

import pl.krisbiketeam.accel2.blueMSP430sensor.BlueMSP430Sensor;
import android.hardware.Sensor;

public class MySensor {
	
	public final static int TYPE_ALL = 0;
	public final static int TYPE_ACCELEROMETER = 1;
	public final static int TYPE_AMBIENT_TEMPERATURE = 2;
	public final static int TYPE_CORE_TEMPERATURE = 3;
	public final static int TYPE_SUPPLY_VOLTAGE = 4;
	
	/*private static final String[] sensorName = {"BlueMSP430 All"
												, "BlueMSP430 ACCELEROMETER"
												, "BlueMSP430 AMBIENT_TEMPERATURE"
												, "BlueMSP430 CORE_TEMPERATURE"
												, "BlueMSP430 SUPPLY_VOLTAGE"};
	*/
	
	private int type;
	private float  maximumRange;  
	private float  minimumRange;  
	private float  resolution;  
	private String  name;
	
	private int  minDelay;  
	private float  power;  
	private String  vendor;  
	private int  version; 
	
	private boolean internal;
	
	//TODO: moze jednak zostawiæ te czujniki
	@Deprecated 
	private Sensor sensor;
	@Deprecated 
	private BlueMSP430Sensor blueMSP430Sensor;
	
	MySensor(Sensor sensor){
		this.type = sensor.getType();
		this.maximumRange = sensor.getMaximumRange();
		this.minimumRange = -sensor.getMaximumRange();
		this.resolution = sensor.getResolution();
		this.name = sensor.getName();
		
		this.minDelay = sensor.getMinDelay();
		this.power = sensor.getPower();
		this.vendor = sensor.getVendor();
		this.version = sensor.getVersion();
		
		this.internal = true;
		this.sensor = sensor;
	}
	public MySensor(BlueMSP430Sensor sensor){
		this.type = sensor.getType();
		this.maximumRange = sensor.getMaximumRange();
		this.minimumRange = -sensor.getMaximumRange();
		this.resolution = sensor.getResolution();
		this.name = sensor.getName();
		
		//this.minDelay = sensor.getMinDelay();
		//this.power = sensor.getPower();
		//this.vendor = sensor.getVendor();
		//this.version = sensor.getVersion();
		
		this.internal = false;
		this.blueMSP430Sensor = sensor;
	}
	
	MySensor(int type, float  maximumRange,	float  resolution){
		this.type = type;
		this.maximumRange = maximumRange;
		this.minimumRange = -maximumRange;
		this.resolution = resolution;
		//this.name = sensorName[type];
		//this.internal = false;		
	}
	MySensor(int type, float  maximumRange,	float  minimumRange, float  resolution){
		this(type, maximumRange, resolution);
		this.minimumRange = minimumRange;
		//this.name = sensorName[type];
		//this.internal = false;
	}
	MySensor(int type, float  maximumRange,	float  resolution, String  name){
		this(type, maximumRange, resolution);
		this.name = name;
	}
	MySensor(int type, float  maximumRange,	float  minimumRange,	float  resolution, String  name){
		this(type, maximumRange, minimumRange, resolution);
		this.name = name;
	}
	public MySensor(int type, float  maximumRange,	float  minimumRange,	float  resolution, String  name, boolean internal){
		this(type, maximumRange, minimumRange, resolution, name);
		this.internal = internal;
	}
	/**
	 * @return the type
	 */
	public int getType() {
		//TODO: nie dzia³a przy wczytywaniu pliku
		if(internal){
			if(sensor!=null)
				return sensor.getType();
		}
		else{
			if(blueMSP430Sensor!=null)
				return blueMSP430Sensor.getType();
		}
		
		return type;
	}
	/**
	 * @return the maximumRange
	 */
	public float getMaximumRange() {
		if(internal){
			if(sensor!=null)
				return sensor.getMaximumRange();
		}
		else{
			if(blueMSP430Sensor!=null)
				return blueMSP430Sensor.getMaximumRange();
		}
		return maximumRange;
	}
	/**
	 * @return the minimumRange
	 */
	public float getMinimumRange() {
		if(internal){
			if(sensor!=null)
				return -sensor.getMaximumRange();
		}
		else{
			if(blueMSP430Sensor!=null)
				return blueMSP430Sensor.getMinimumRange();
		}
		return minimumRange;
	}
	/**
	 * @return the resolution
	 */
	public float getResolution() {
		if(internal){
			if(sensor!=null)
				return sensor.getResolution();
		}
		else{
			if(blueMSP430Sensor!=null)
				return blueMSP430Sensor.getResolution();
		}
		return resolution;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		if(internal){
			if(sensor!=null)
				return sensor.getName();
		}
		else{
			if(blueMSP430Sensor!=null)
				return blueMSP430Sensor.getName();
		}
		return name;
	}
	
	/**
	 * @return the minDelay
	 */
	public int getMinDelay() {
		if(internal){
			if(sensor!=null)
				return sensor.getMinDelay();
		}
		else{
			if(blueMSP430Sensor!=null)
				return blueMSP430Sensor.getMinDelay();
		}
		return minDelay;
	}
	/**
	 * @return the power
	 */
	public float getPower() {
		if(internal){
			if(sensor!=null)
				return sensor.getPower();
		}
		else{
			if(blueMSP430Sensor!=null)
				return blueMSP430Sensor.getPower();
		}
		return power;
	}
	/**
	 * @return the vendor
	 */
	public String getVendor() {
		if(internal){
			if(sensor!=null)
				return sensor.getVendor();
		}
		else{
			if(blueMSP430Sensor!=null)
				return blueMSP430Sensor.getVendor();
		}
		return vendor;
	}
	/**
	 * @return the version
	 */
	public int getVersion() {
		if(internal){
			if(sensor!=null)
				return sensor.getVersion();
		}
		else{
			if(blueMSP430Sensor!=null)
				return blueMSP430Sensor.getVersion();
		}
		return version;
	}
	
	
	/**
	 * @return the internal
	 */
	public boolean isInternal() {
		return internal;
	}
	
	/**
	 * @return the sensor
	 */
	public Sensor getSensor() {
		return sensor;
	}
	/**
	 * @return the blueMSP430Sensor
	 */
	public BlueMSP430Sensor getBlueMSP430Sensor() {
		return blueMSP430Sensor;
	}

	
	
}
