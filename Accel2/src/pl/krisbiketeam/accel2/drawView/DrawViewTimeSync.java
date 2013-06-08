package pl.krisbiketeam.accel2.drawView;

import pl.krisbiketeam.accel2.MySensor;
import pl.krisbiketeam.accel2.MySensorManager;
import pl.krisbiketeam.accel2.SensorService.OnMySensorChanegedListener;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;

/**
 * Wy�wietla 1 wykres danych z czujnik�w 
 * @author Krzy�
 *
 */
public class DrawViewTimeSync extends DrawView implements OnMySensorChanegedListener //implements SensorEventListener, OnBlueMSP430SensorChanegedListener
{

	// Debugging
	private static final String TAG = "DrawViewTimeSync";
	private static final boolean D = true;
	
	/**
	 * minimalna warto�� zoomowania osi czasu, osi X
	 */
	protected float MIN_ZOOM_X = 2500000f;			//400Hz
	/**
	 * maksymalna warto�� zoomowania osi czasu, osi X
	 */
	protected float MAX_ZOOM_X = 100000000f;		//10Hz
	

	/**
     * offset czasu nano wyst�pienia pierwszego punktu na wykresie
     */
    protected long nanoTimeOffset;
    /**
     * skala czasu nano do przeskalownia czas�w wyst�pienia kolejnych zda�e� czujnika
     * 100Hz -> 10ms -> 10 000us - 10 000 000ns
	*/
    protected float nanoTimeScale = 10000000;
    
    protected String scaleTimeText = "";
    
    /**
     * zmienna potrzebna do wyliczania pocz�tkowego nanoTimeScale
     */
    protected boolean init = false;
   
    
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
	
	private class MyScaleGestureDetector extends SimpleOnScaleGestureListener {
		
		
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			
			//skalowanie w osi amplitudy Y zoomowanie
			if(mSetings.isAllowZoom()){
				//float temp = detector.getScaleFactor();
				float scaleY = detector.getCurrentSpanY()/detector.getPreviousSpanY();		//custopm getStaleFactorY()
				scaleFactor *= scaleY;	//temp;
				scaleFactor = Math.max(MIN_ZOOM_Y, Math.min(scaleFactor, MAX_ZOOM_Y));
				
				mMaximumRange = sensor.getMaximumRange()/scaleFactor;
				mMinimumRange = -sensor.getMaximumRange()/scaleFactor;//getMinimumRange();
				
				scaleMaxText = Float.toString(mMaximumRange);
				scaleMinText = Float.toString(mMinimumRange);
				
				rebuildScale();
			}
			
			//skalowanie w osi czasu X	
			if(mSetings.isAllowTimeZoom()){
				float scaleX = detector.getCurrentSpanX()/detector.getPreviousSpanX();		//custopm getStaleFactorX()
				
				nanoTimeScale /= scaleX;	//temp;			//jest "/" zamiast "*" �eby odwr�ci� pinch to zoom
				nanoTimeScale = Math.max(MIN_ZOOM_X, Math.min(nanoTimeScale, MAX_ZOOM_X));
				
				scaleTimeText = Float.toString(1/nanoTimeScale * 1000000000) + "Hz";				
			}
			//TODO: to chyba nie potrzebne jest sprawdzi� czy dzia�a bez tego
			//invalidate();
			
			return true;
		}

		
	}
	
	/* (non-Javadoc)
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		//super.onDraw(canvas);
		//je�li nie zainicjowane to wyjd�
		if(points == null) return;
		
		canvas.drawLines(points[0], 0, numOfPointsNew, paintDraw);
		canvas.drawText(scaleMaxText, 0, paintTextBlack.getTextSize(), paintTextBlack);
		canvas.drawText(scaleMinText, 0, mViewHeight, paintTextBlack);
		
		canvas.drawText(sensorNameText, mViewWidth, paintTextBlack.getTextSize(), paintTextRedRight);
		canvas.drawText(scaleTimeText, mViewWidth, mViewHeight, paintTextRedRight);
		
		if(recorded)
			canvas.drawCircle(mViewHeight, mViewWidth, 5, paintDrawRed);
		
		// and make sure to redraw asap
        invalidate();
	}
	
	/**
	 * inicjalizuje parametry okna z wykresem
	 * @param context parametr pobierany z kontruktora DrawView
	 */
	@Override
	protected void initDrawView(Context context){
		super.initDrawView(context);
		if (D) Log.d(TAG, "+ initDrawView +");
		mScaleGestureDetector = new ScaleGestureDetector(context, new MyScaleGestureDetector());
		
	}
	
	/**
	 * dodaje punkty do loklanej tablicy pointsX, pointsY, pointsZ z now� lini� do narysowania
	 * @param values	tablica z warto�ciami danych z czujnika
	 * @param timeStamp	czas nano wyst�pienia zda�enia
	 */
	protected void addPointsToTable(float[] value, long timeStamp){
		if(mViewWidth == 0 || mViewHeight == 0) return;
		
		if(points == null){
			points = new float[1][mViewWidth*4 + 2];		// zapasowe dwa punkty na pocz�tek kolejnego punktu
			numOfPointsNew = 0;
			nanoTimeOffset = timeStamp;
			rebuildScale();
			//bie��cy punkt
			points[0][numOfPointsNew] = (timeStamp - nanoTimeOffset) / nanoTimeScale; //mo�e zrobic rzutowanie/zamieni� na inty to si� szybciej wykona
			points[0][numOfPointsNew + 1] = mMyScale.myScaledValue(value[0]);
			init = true;
			return;
		}
		//obliczamy nanoTimeScale
		if(init){
			nanoTimeScale= timeStamp - nanoTimeOffset;
			scaleTimeText = Float.toString(1/nanoTimeScale * 1000000000) + "Hz";
			init = false;
		}
		
		if(numOfPointsNew >= mViewWidth * 4){
       
        	numOfPointsNew = 0;
        	nanoTimeOffset = timeStamp;
        	points[0][numOfPointsNew] = (timeStamp - nanoTimeOffset) / nanoTimeScale; //mo�e zrobic rzutowanie/zamieni� na inty to si� szybciej wykona
			points[0][numOfPointsNew + 1] = mMyScale.myScaledValue(value[0]);
        	return;
        } 
		
		//bie��cy punkt
		points[0][numOfPointsNew + 2] = (timeStamp - nanoTimeOffset) / nanoTimeScale; //mo�e zrobic rzutowanie/zamieni� na inty to si� szybciej wykona;
		points[0][numOfPointsNew + 3] = mMyScale.myScaledValue(value[0]);
		
		numOfPointsNew +=4;
        //punkt do nast�pnej linii
		points[0][numOfPointsNew    ] = points[0][numOfPointsNew - 2];
		points[0][numOfPointsNew + 1] = points[0][numOfPointsNew - 1];
		
		invalidate();
		
	}
	
	
}
