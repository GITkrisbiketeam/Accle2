package pl.krisbiketeam.accel2.blueMSP430sensor;



public class BlueMSP430SensorEvent implements Cloneable{
	
	public int accuracy;
	public BlueMSP430Sensor sensor;
	public long mSensorTimeStamp;
	public float values[];
	
	@Deprecated
	public float temperature;
	@Deprecated
	public float coreTemperature;
	@Deprecated
	public float supplyVoltage;


	public BlueMSP430SensorEvent(){	}
	public BlueMSP430SensorEvent(BlueMSP430Sensor sensor){
		this.sensor = sensor;
		int numOfValues = 0;
		switch (sensor.getType()){
		case BlueMSP430Sensor.TYPE_ACCELEROMETER:
			numOfValues = 3;
			break;
		case BlueMSP430Sensor.TYPE_AMBIENT_TEMPERATURE:
			numOfValues = 1;
			break;
		case BlueMSP430Sensor.TYPE_CORE_TEMPERATURE:
			numOfValues = 1;
			break;
		case BlueMSP430Sensor.TYPE_SUPPLY_VOLTAGE:
			numOfValues = 1;
			break;
		}
		this.values = new float[numOfValues];
	}
	
	public BlueMSP430SensorEvent update(float values[], long mSensorTimeStamp){
		this.mSensorTimeStamp = mSensorTimeStamp;
		if(values.length == this.values.length){
			this.values = values;
			return this;
		}
		else{
			return null;
		}
	}
	
	
	@Deprecated
	public BlueMSP430SensorEvent update(int accX, int accY, int accZ, int temperature, int coreTemperature, int supplyVoltage, long mSensorTimeStamp){
		//this.accX = accX;
		//this.accY = accY;
		//this.accZ = accZ;
		this.temperature = temperature;
		this.coreTemperature = coreTemperature;
		this.supplyVoltage = supplyVoltage;
		this.mSensorTimeStamp = mSensorTimeStamp;
		this.values = new float[3];
		values[0] = accX;
		values[1] = accY;
		values[2] = accZ;
		return this;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}
	
}
