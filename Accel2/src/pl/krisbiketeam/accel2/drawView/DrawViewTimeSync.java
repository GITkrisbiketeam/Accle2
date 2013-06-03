package pl.krisbiketeam.accel2.drawView;

import pl.krisbiketeam.accel2.MySensor;
import pl.krisbiketeam.accel2.MySensorManager;
import pl.krisbiketeam.accel2.SensorService.OnMySensorChanegedListener;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Wyï¿½wietla 1 wykres danych z czujnikï¿½w 
 * @author Krzyï¿½
 *
 */
public class DrawViewTimeSync extends DrawView implements OnMySensorChanegedListener //implements SensorEventListener, OnBlueMSP430SensorChanegedListener
{

	// Debugging
	private static final String TAG = "DrawViewTimeSync";
	private static final boolean D = true;

	/**
     * offset czasu nano wyst¹pienia pierwszego punktu na wykresie
     */
    protected long nanoTimeOffset;
    /**
     * skala czasu nano do przeskalownia czasów wyst¹pienia kolejnych zda¿eñ czujnika
     */
    protected float nanoTimeScale;
    
    
    public DrawViewTimeSync(MySensor sensor, MySensorManager sensorManager, Context context, AttributeSet attrs) {
    	super(sensor, sensorManager, context, attrs);
	}
    @Deprecated
    public DrawViewTimeSync(Context context) {
		super(context);
	}
	@Deprecated
	public DrawViewTimeSync(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	@Deprecated
	public DrawViewTimeSync(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	/**
	 * inicjalizuje parametry okna z wykresem
	 * @param context parametr pobierany z kontruktora DrawView
	 */
	@Override
	protected void initDrawView(Context context){
		super.initDrawView(context);
		if (D) Log.d(TAG, "+ initDrawView +");
		
	}
	
	/**
	 * dodaje punkty do loklanej tablicy pointsX, pointsY, pointsZ z nowï¿½ liniï¿½ do narysowania
	 * @param values	tablica z wartoï¿½ciami danych z czujnika
	 * @param timeStamp	czas nano wyst¹pienia zda¿enia
	 */
	protected void addPointsToTable(float[] value, long timeStamp){
		if(mViewWidth == 0 || mViewHeight == 0) return;
		
		if(points == null){
			points = new float[1][mViewWidth*4 + 2];		// zapasowe dwa punkty na pocz¹tek kolejnego punktu
			numOfPointsNew = 0;
			nanoTimeOffset = timeStamp;
			/*
			 * 100Hz -> 10ms -> 10 000us - 10 000 000ns
			 */
			nanoTimeScale = 10000000;
			rebuildScale();
			//bieï¿½ï¿½cy punkt
			points[0][numOfPointsNew] = (timeStamp - nanoTimeOffset) / nanoTimeScale; //mo¿e zrobic rzutowanie/zamieniæ na inty to siê szybciej wykona
			points[0][numOfPointsNew + 1] = mMyScale.myScaledValue(value[0]);
			return;
		}
		
		if(numOfPointsNew >= mViewWidth * 4){
       
        	numOfPointsNew = 0;
        	nanoTimeOffset = timeStamp;
        	points[0][numOfPointsNew] = (timeStamp - nanoTimeOffset) / nanoTimeScale; //mo¿e zrobic rzutowanie/zamieniæ na inty to siê szybciej wykona
			points[0][numOfPointsNew + 1] = mMyScale.myScaledValue(value[0]);
        	return;
        } 
		
		//bieï¿½ï¿½cy punkt
		points[0][numOfPointsNew + 2] = (timeStamp - nanoTimeOffset) / nanoTimeScale; //mo¿e zrobic rzutowanie/zamieniæ na inty to siê szybciej wykona;
		points[0][numOfPointsNew + 3] = mMyScale.myScaledValue(value[0]);
		
		numOfPointsNew +=4;
        //punkt do nastï¿½pnej linii
		points[0][numOfPointsNew    ] = points[0][numOfPointsNew - 2];
		points[0][numOfPointsNew + 1] = points[0][numOfPointsNew - 1];
		
		invalidate();
		/*
		if(points == null){
			points = new float[1][mViewWidth*4];
			numOfPointsNew = 0;
			timeAxisNew = 0;
			rebuildScale();
			//bieï¿½ï¿½cy punkt
			points[0][numOfPointsNew] = timeAxisNew;
			points[0][numOfPointsNew + 1] = mMyScale.myScaledValue(value[0]);
			return;
		}
		
		if(timeAxisNew < mViewWidth - 1){
        	timeAxisNew++;
        	numOfPointsNew +=4;
        }
        else{
        	timeAxisNew = 0;        	
        	numOfPointsNew = 0;
        	points[0][numOfPointsNew] = timeAxisNew;
			points[0][numOfPointsNew + 1] = mMyScale.myScaledValue(value[0]);
        	return;
        } 
		
		//bieï¿½ï¿½cy punkt
		points[0][numOfPointsNew - 2] = timeAxisNew;
		points[0][numOfPointsNew - 1] = mMyScale.myScaledValue(value[0]);
		//punkt do nastï¿½pnej linii
		points[0][numOfPointsNew] = timeAxisNew;
		points[0][numOfPointsNew + 1] = points[0][numOfPointsNew - 1];
		
		invalidate();
		*/
	}
	
	
}
