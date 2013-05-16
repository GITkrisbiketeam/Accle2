package pl.krisbiketeam.accel2.drawView;

import pl.krisbiketeam.accel2.MySensor;
import pl.krisbiketeam.accel2.MySensorEvent;
import pl.krisbiketeam.accel2.MySensorManager;
import pl.krisbiketeam.accel2.SensorService.OnMySensorChanegedListener;
import pl.krisbiketeam.accel2.settings.MySettings;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;

/**
 * Wy�wietla 1 wykres danych z czujnik�w 
 * @author Krzy�
 *
 */
public class DrawView extends View implements OnMySensorChanegedListener //implements SensorEventListener, OnBlueMSP430SensorChanegedListener
{

	// Debugging
	private static final String TAG = "DrawView";
	private static final boolean D = true;

	private float MIN_ZOOM = 1f;
	private float MAX_ZOOM = 20f;
	 

	protected int mViewWidth, mViewWidth2;
	protected int mViewHeight, mViewHeight2;
	
	protected float mMaximumRange;
    protected float mMinimumRange;
    protected float mResolution;
    protected long mSensorTimeStamp;
    protected int type;						//sensor type
    
    protected MySensor sensor;
    protected MySensorManager sensorManager;
       
    protected MyScale mMyScale;
    
    /**
     * parametr okre�laj�cy liczb� wykres�w zmiennych wy�wietlanych na tym Draw
     */
    protected int drawCount = 0;
    
    /**
     * kolorki napis�w
     */
    protected Paint paintTextBlack, paintTextBlackRight, paintTextRedRight;
    /**
     * kolorki wykres�w
     */
    protected Paint paintDraw, paintDrawRed;
    
    /**
     * tablica z punktami na bazie kt�rych onDraw b�dzie rysowa� lini�
     */
    protected float points[][];
    
    /**
     * liczba punkt�w wykresu
     */
    protected int numOfPointsNew;
    protected int timeAxisNew;
    
    protected float scaleFactor = 1.f;
    protected ScaleGestureDetector mScaleGestureDetector; 
    protected GestureDetector mGestureDetector; 
	
    protected boolean recorded = false;
    
    /**
     * odczytuje przechowuje i zapisuje ustawienia programu
     */
    MySettings mSetings;
	

	/**
     * unikalny numer klasy tego Draw to potrzebne do refreshHandler i updateTimeAxis
     */
    protected int classCode;
    
    /**
     * nazwa czujnika z kt�rego wy�wietlane s� dane
     */
    protected String sensorName = "";
    protected String scaleMax = "";
    protected String scaleMin = "";
    protected String scaleZero = "";
    
    public DrawView(MySensor sensor, MySensorManager sensorManager, Context context, AttributeSet attrs) {
		super(context, attrs);
		initDrawView(context);
		sensorInit(sensor, sensorManager);
	}
    @Deprecated
    public DrawView(Context context) {
		super(context);
		initDrawView(context);
	}
	@Deprecated
	public DrawView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initDrawView(context);
	}
	@Deprecated
	public DrawView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initDrawView(context);
	}
	
	
	
	/**
	 * inicjalizuje parametry okna z wykresem
	 * @param context parametr pobierany z kontruktora DrawView
	 */
	protected void initDrawView(Context context){
		if (D) Log.d(TAG, "+ initDrawView +");
		
		paintTextBlack = new Paint();
		paintTextBlack.setColor(Color.BLACK);
		paintTextBlack.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 
                (float) 20, getResources().getDisplayMetrics()));
		paintTextBlackRight = new Paint();
		paintTextBlackRight.setColor(Color.BLACK);
		paintTextBlackRight.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 
                (float) 20, getResources().getDisplayMetrics()));
		paintTextBlackRight.setTextAlign(Paint.Align.RIGHT);
		
		paintTextRedRight = new Paint();
		paintTextRedRight.setColor(Color.RED);
		paintTextRedRight.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 
                (float) 20, getResources().getDisplayMetrics()));
		paintTextRedRight.setTextAlign(Paint.Align.RIGHT);
		
		this.setBackgroundColor(Color.WHITE);
		
		paintDraw = new Paint();
		paintDraw.setColor(Color.BLACK);
		paintDrawRed = new Paint();
		paintDrawRed.setColor(Color.RED);
		paintDrawRed.setStyle(Style.FILL);
		
		mSetings = new MySettings(context.getApplicationContext());
		
		if(mSetings.isLogScale())
			mMyScale = new MyLog();
		else
			mMyScale = new MyLin();
		
		classCode = this.hashCode();
		
		mScaleGestureDetector = new ScaleGestureDetector(context, new MyScaleGestureDetector());
		mGestureDetector = new GestureDetector(context, new MyGestureDetector());
		
				
		//this.setHorizontalScrollBarEnabled(true);
	}
	
	
	
	/* (non-Javadoc)
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Let the ScaleGestureDetector inspect all events.
		mScaleGestureDetector.onTouchEvent(event);
		mGestureDetector.onTouchEvent(event);
		//super.onTouchEvent(event);
		
        return true;
        		
	}
	
	public void longPressHandOver(MotionEvent event){
		super.onTouchEvent(event);
		
	}
	
	private class MyGestureDetector extends SimpleOnGestureListener {

		
		/* (non-Javadoc)
		 * @see android.view.GestureDetector.SimpleOnGestureListener#onLongPress(android.view.MotionEvent)
		 */
		@Override
		public void onLongPress(MotionEvent e) {
			// TODO Auto-generated method stub
			//super.onLongPress(e);
			longPressHandOver(e);
		}

		
	}
		
	private class MyScaleGestureDetector extends SimpleOnScaleGestureListener {
		
		
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			//contextMenuBlock = true;
			
			float temp = detector.getScaleFactor();
			scaleFactor *= temp;
			scaleFactor = Math.max(MIN_ZOOM, Math.min(scaleFactor, MAX_ZOOM));
			
			mMaximumRange = sensor.getMaximumRange()/scaleFactor;
			mMinimumRange = -sensor.getMaximumRange()/scaleFactor;//getMinimumRange();
			
			scaleMax = Float.toString(mMaximumRange);
			scaleMin = Float.toString(mMinimumRange);
			
			rebuildScale();
			
			//TODO: to chyba nie potrzebne jest sprawdzi� czy dzia�a bez tego
			invalidate();
			
			return true;
		}

		
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if (D) Log.d(TAG, "+ ON SIZE CHANGED +");
		
		mViewWidth = w;
		mViewHeight = h;
		mViewWidth2 = w/2;
		mViewHeight2 = h/2;
		
		timeAxisNew = 0;
		numOfPointsNew = 0;
		points = null;
		
		rebuildScale();
		//super.onSizeChanged(w, h, oldw, oldh);
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
		canvas.drawText(scaleMax, 0, paintTextBlack.getTextSize(), paintTextBlack);
		canvas.drawText(scaleMin, 0, mViewHeight, paintTextBlack);
		
		canvas.drawText(sensorName, mViewWidth, paintTextBlack.getTextSize(), paintTextRedRight);
		
		if(recorded)
			canvas.drawCircle(mViewHeight, mViewWidth, 5, paintDrawRed);
		
		// and make sure to redraw asap
        invalidate();
		
	}
	
		
	/**
	 * funkcja wywo�ywana z Activity po zmianie dok�adno�ci wewn�trznego czujnika
	 * inicjalizuje parametry skalowania dla wykresu 
	 * mMaximumRange, mMinimumRange, mResolution 
	 * odbudowywuje skal� rebuildScale();
	 * oraz wraca na pocz�tekwykresu
	 * @param sensor czujnik
	 * @param accuracy dok�adno��
	 */
	public void sensorInit(MySensor sensor, MySensorManager sensorManager) {
		//TODO: co� tu dziwnie dzia�a
		mMaximumRange = sensor.getMaximumRange();
		mMinimumRange = -sensor.getMaximumRange();//getMinimumRange();
		mResolution = sensor.getResolution();
		sensorName = sensor.getName();
		type = sensor.getType();
		this.sensor = sensor;
		this.sensorManager = sensorManager;
		
		MAX_ZOOM = mMaximumRange/mResolution;
		
		scaleMax = Float.toString((int) mMaximumRange);
		scaleMin = Float.toString((int) mMinimumRange);
		//rebuildScale();
		
		registerMySensorListener();
	}
	
	/**
	 * rejestruje nas�uchiwanie zdarze� z czujnika sensor
	 * @param sensorManager
	 */
	public void registerMySensorListener(){
		if(sensorManager != null)
			sensorManager.registerMyListener(this, sensor);
		
	}
	/**
	 * wyrejestrowuje nas�uchiwanie zdarze� z czujnika sensor
	 * @param sensorManager
	 */
	public void unRegisterMySensorListener(){
		if(sensorManager != null)
			sensorManager.unregisterMyListener(this, sensor);
		
	}
	
	/**
	 * funkcja wywo�ywana z Activity po odebraniu danych z WN�TRZNEGO czujnika z warto�ciami z tego czujnika
	 * @param event warto�ci z czujnika
	 */
	@Deprecated
	public void sensorChanged(MySensorEvent event) {
		//sprawdza czy event jest przeznaczony dla wykresu tego czujnika
		if(type == event.sensor.getType()){
			mSensorTimeStamp = event.timestamp;
		    
			addPointsToTable(event.values);
			
		}
		
	}
	
	@Override
	public void onMySensorValueChaneged(MySensorEvent event) {
		//sprawdza czy event jest przeznaczony dla wykresu tego czujnika
		if(event.sensor == this.sensor)
			if(type == event.sensor.getType()){
				mSensorTimeStamp = event.timestamp;
			    
				addPointsToTable(event.values);
				//if (D) Log.d(TAG, event.sensor.getBlueMSP430Sensor().getBtDevice().getAddress() + " :" + mSensorTimeStamp);
			}
		
	}
 	
	
	/**
	 * dodaje punkty do loklanej tablicy pointsX, pointsY, pointsZ z now� lini� do narysowania
	 * @param values	tablica z warto�ciami danych z czujnika
	 */
	protected void addPointsToTable(float[] value){
		if(mViewWidth == 0 || mViewHeight == 0) return;
		
		if(points == null){
			points = new float[1][mViewWidth*4];
			numOfPointsNew = 0;
			timeAxisNew = 0;
			rebuildScale();
			//bie��cy punkt
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
		
		//bie��cy punkt
		points[0][numOfPointsNew - 2] = timeAxisNew;
		points[0][numOfPointsNew - 1] = mMyScale.myScaledValue(value[0]);
		//punkt do nast�pnej linii
		points[0][numOfPointsNew] = timeAxisNew;
		points[0][numOfPointsNew + 1] = points[0][numOfPointsNew - 1];
		
		invalidate();
	}
	
	/**
	 *  tworzy tablice z warto�ciami do mapowania warto�ci przyspieszenia na warto�� na wykresie 
	 *  bazuje na mMaximumRange, mViewHeight/3
	 */
	public void rebuildScale(){
		//mMyScale.creatScaleTable(mMaximumRange, mViewHeight/3, (float)0);//, mResolution);//
		mMyScale.creatScaleTable(mMinimumRange, mMaximumRange, mViewHeight);
	}
	//inicjuje liniow� skal� wykresu
	public void scaleLin(){
		mMyScale = null;
		mMyScale = new MyLin();
		rebuildScale();
	}
	//inicjuje logarytmiczn� skal� wykresu
	public void scaleLog(){
		mMyScale = null;
		mMyScale = new MyLog();
		rebuildScale();
	}

	
	/**
	 * @return the myScale
	 */
 	public MyScale getmMyScale() {
		return mMyScale;
	}
	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}
	/**
	 * @return the sensor
	 */
	public MySensor getMySensor() {
		return sensor;
	}
	/**
	 * @return the recorded
	 */
	public boolean isRecorded() {
		return recorded;
	}
	/**
	 * @param recorded the recorded to set
	 */
	public void setRecorded(boolean recorded) {
		this.recorded = recorded;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		return TAG;
	}
	
 	
}
