package pl.krisbiketeam.accel2.file;

import pl.krisbiketeam.accel2.MySensor;

public class ReadFromFileData {

	
	
	public MySensor sensor;
	
	public float[][] mReadFromFilePoints;
	public long[] mSensorTimeStampPoints;
	public int numOfPoints;
	//public int mStartPoint;
	
	ReadFromFileData(MySensor sensor, float[][] mReadFromFilePoints){
		this.sensor = sensor;
		this.mReadFromFilePoints = mReadFromFilePoints;
		if(mReadFromFilePoints != null)
			this.numOfPoints = mReadFromFilePoints[0].length;
		//this.mStartPoint = 0;
	}
	ReadFromFileData(MySensor sensor, float[][] mReadFromFilePoints, long[] mSensorTimeStampPoints){
		this(sensor, mReadFromFilePoints);
		this.mSensorTimeStampPoints = mSensorTimeStampPoints;
		//this.mStartPoint = 0;
	}
		
}
