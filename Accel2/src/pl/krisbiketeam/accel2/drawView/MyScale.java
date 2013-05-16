package pl.krisbiketeam.accel2.drawView;

public abstract class MyScale {

	abstract public void creatScaleTable(float minVal,float maxVal, int window);
	abstract public void creatScaleTable(float minVal,float maxVal, int window, float resolution);
	
	abstract public int myScaledValue(float val);
}
