package pl.krisbiketeam.accel2.drawView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import pl.krisbiketeam.accel2.MySensorManager;
import pl.krisbiketeam.accel2.file.ReadFromFileData;
import pl.krisbiketeam.accel2.settings.MySettings;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;

public class DrawViewMultipleFromFile extends DrawViewMultiple{

	// Debugging
	private static final String TAG = "DrawViewMultipleFromFile";
	private static final boolean D = true;

	ReadFromFileData mReadFromFileData;
	
	protected int offset = 0;
    
	SimpleDateFormat formatter;
	Calendar calendar;
	
	public DrawViewMultipleFromFile(ReadFromFileData mReadFromFileData, MySensorManager sensorManage, Context context, AttributeSet attrs) {
		super(mReadFromFileData.sensor, sensorManage, context, attrs);
		this.mReadFromFileData = mReadFromFileData;
	}
	@Deprecated
	public DrawViewMultipleFromFile(Context context) {
		super(context);
	}
	@Deprecated
	public DrawViewMultipleFromFile(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	@Deprecated
	public DrawViewMultipleFromFile(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * inicjalizuje parametry okna z wykresem
	 * @param context parametr pobierany z kontruktora DrawView
	 */
	@Override
	protected void initDrawView(Context context){
		super.initDrawView(context);
		mGestureDetector = new GestureDetector(context, new MyGestureDetector());
		
		offset = 0;
		
		formatter = new SimpleDateFormat("HH:mm:ss.SSS",Locale.FRANCE);
		calendar = Calendar.getInstance();
		
	}
	
	private class MyGestureDetector extends SimpleOnGestureListener {

		/* (non-Javadoc)
		 * @see android.view.GestureDetector.SimpleOnGestureListener#onScroll(android.view.MotionEvent, android.view.MotionEvent, float, float)
		 */
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			if(!mScaleGestureDetector.isInProgress())				
				if(mReadFromFileData != null){
					int sP;
					int dX = (int)distanceX;
					sP = mReadFromFileData.numOfPoints - mViewWidth;
					if(sP > 0){
						offset = offset + dX;
						if(offset >= sP)
							offset = sP;
						else if(offset < 0)
							offset = 0;
						//setOffset(offset);
						//TODO: dodaæ opcje ¿eby tworzy³o w onDraw ca³y wykres ze wszystkimi punktami a przy wyœwietlaniu odpowiednio przycina³o do rozmiaru okna a ewentualnie przesuwa³o je
						//mBlueMSP430DrawViewReadFromFile.offsetLeftAndRight(- offset/4);//.setOffset(offset);
					}
					
					float maxDrawRange = mMaximumRange - mMinimumRange;
					
					mMaximumRange = mMaximumRange - distanceY/10;
					mMinimumRange = mMinimumRange - distanceY/10;
					
					if(mMaximumRange > sensor.getMaximumRange()){
						mMaximumRange = sensor.getMaximumRange();
						mMinimumRange = mMaximumRange - maxDrawRange;
					}
					else if(mMinimumRange < sensor.getMinimumRange()){
						mMinimumRange = sensor.getMinimumRange();
						mMaximumRange = mMinimumRange + maxDrawRange;
					}
						
					scaleMaxText = Float.toString((int) mMaximumRange);
					scaleMinText = Float.toString((int) mMinimumRange);
					
					rebuildScale();						
					
					return true;
				}
			return super.onScroll(e1, e2, distanceX, distanceY);
		}
		
		/* (non-Javadoc)
		 * @see android.view.GestureDetector.SimpleOnGestureListener#onLongPress(android.view.MotionEvent)
		 */
		@Override
		public void onLongPress(MotionEvent e) {
			//super.onLongPress(e);
			longPressHandOver(e);
		}

		
	}
	
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		calendar.setTimeInMillis(mReadFromFileData.mSensorTimeStampPoints[offset/4]);///1000000);
		canvas.drawText(formatter.format(calendar.getTime()), 0, mViewHeight/2, paintTextBlack);
		//canvas.drawText(sensorNameText, mViewWidth, paintTextBlack.getTextSize(), paintBlackRight);
		
	}
	
	/* (non-Javadoc)
	 * @see pl.krisbiketeam.krzys_project_accel_2.drawView.DrawView#rebuildScale()
	 */
	@Override
	public void rebuildScale() {
		// TODO Auto-generated method stub
		super.rebuildScale();
		fillDrawViewPointsOffsetNew();
	}
	
	/**
	 * ustawia przesuniêcie offset do wyœwietlania danych przesunietych o offset
	 * @param offset the offset to set
	 */
	protected void setOffset(int offset) {
		this.offset = offset;
		moveDrawViewPoints();		
	}
	
	/**
	 * wype³nia wartoœci tablic pointsXYZ w oparciu o dane odczytane z pliku i odpowiednie wymiary okna
	 */
	public void fillDrawViewPointsOffsetNew(){
		
		if (D) Log.d(TAG, "updateDrawViewPoints()");
        //jeœli nie ma jeszcze danych odczytanych z pliku to wyjdz
		if(mReadFromFileData == null) return;
		if(points == null){
			
			int size = mReadFromFileData.numOfPoints;
			//jeœli w danych odczytanych z pliku nie ma punktów to wyjdŸ
			if(size < 2) return;
			
			if(size <= mViewWidth)
				numOfPointsNew = size * 4 - 4;
			else
				numOfPointsNew = mViewWidth * 4 - 4;
		
			//ile bêdzie wykresów
			drawCount = mReadFromFileData.mReadFromFilePoints.length;
			
			points = new float[drawCount][mViewWidth*4];
		}
		int zoom;
		if(MySettings.logScale)
			zoom = 1;
		else
			zoom = 3;
		
		
		for(int j = 0; j < drawCount; j++){
			points[j][0] = 0;
			points[j][1] = getmMyScale().myScaledValue(mReadFromFileData.mReadFromFilePoints[j][offset]);// + mViewHeight * j / drawCount;
			points[j][2] = 1 * zoom;
			points[j][3] = getmMyScale().myScaledValue(mReadFromFileData.mReadFromFilePoints[j][offset + 1]);// + mViewHeight * j / drawCount;
			
			for (int i = 1; i<numOfPointsNew / 4; i++){
				points[j][i * 4 + 0] = i * zoom;
				points[j][i * 4 + 1] = points[j][i * 4 - 1];
				points[j][i * 4 + 2] = (i + 1) * zoom;
				points[j][i * 4 + 3] = getmMyScale().myScaledValue(mReadFromFileData.mReadFromFilePoints[j][offset + i + 1]);// + mViewHeight * j / drawCount;
			}
			
		}
		
		
		this.invalidate();
	}
	
	/**
	 * wype³nia wartoœci tablic pointsXYZ w oparciu o dane odczytane z pliku i odpowiednie wymiary okna
	 */
	@Deprecated	
	public void fillDrawViewPointsOffset(){
		
		if (D) Log.d(TAG, "fillDrawViewPoints()");
        
		//jeœli nie ma jeszcze danych odczytanych z pliku to wyjdz
		if(mReadFromFileData == null) return;
		
		int size = mReadFromFileData.numOfPoints - 1;
		//jeœli w danych odczytanych z pliku nie ma punktów to wyjdŸ
		if(size == 0) return;
		
		//ile bêdzie wykresów
		drawCount = mReadFromFileData.mReadFromFilePoints.length;
		
		points = new float[drawCount][size*4];
		
		int tmpOffset = offset/4;
		
		for(byte j = 0; j < drawCount; j++){
			points[j][0] = 0 - tmpOffset;
			points[j][1] = getmMyScale().myScaledValue(mReadFromFileData.mReadFromFilePoints[j][0]);// + mViewHeight * j / drawCount;
			points[j][2] = 1 - tmpOffset;
			points[j][3] = getmMyScale().myScaledValue(mReadFromFileData.mReadFromFilePoints[j][1]);// + mViewHeight * j / drawCount;
			
			for (int i = 1; i<size; i++){
				points[j][i * 4 + 0] = i - tmpOffset;
				points[j][i * 4 + 1] = points[j][i * 4 - 1];
				points[j][i * 4 + 2] = i + 1 - tmpOffset;
				points[j][i * 4 + 3] = getmMyScale().myScaledValue(mReadFromFileData.mReadFromFilePoints[j][i + 1]);// + mViewHeight * j / drawCount;
			}
			
		}
		
		if(size <= mViewWidth)
			numOfPointsNew = size * 4;
		else
			numOfPointsNew = mViewWidth * 4 - 4;
		
		this.invalidate();
	}

	/**
	 * wype³nia wartoœci tablic pointsXYZ w oparciu o dane odczytane z pliku i odpowiednie wymiary okna
	 */
	@Deprecated	
	public void fillDrawViewPoints(){
		
		if (D) Log.d(TAG, "fillDrawViewPointsNew()");
        
		//jeœli nie ma jeszcze danych odczytanych z pliku to wyjdz
		if(mReadFromFileData == null) return;
		
		int size = mReadFromFileData.numOfPoints - 1;
		//jeœli w danych odczytanych z pliku nie ma punktów to wyjdŸ
		if(size == 0) return;
		
		//ile bêdzie wykresów
		drawCount = mReadFromFileData.mReadFromFilePoints.length;
		
		points = new float[drawCount][size*4];
		
		for(byte j = 0; j < drawCount; j++){
			points[j][0] = 0;
			points[j][1] = getmMyScale().myScaledValue(mReadFromFileData.mReadFromFilePoints[j][0]);// + mViewHeight * j / drawCount;
			points[j][2] = 1;
			points[j][3] = getmMyScale().myScaledValue(mReadFromFileData.mReadFromFilePoints[j][1]);// + mViewHeight * j / drawCount;
			
			for (int i = 1; i<size; i++){
				points[j][i * 4 + 0] = i;
				points[j][i * 4 + 1] = points[j][i * 4 - 1];
				points[j][i * 4 + 2] = i + 1;
				points[j][i * 4 + 3] = getmMyScale().myScaledValue(mReadFromFileData.mReadFromFilePoints[j][i + 1]);// + mViewHeight * j / drawCount;
			}
			
		}
		
		if(size <= mViewWidth)
			numOfPointsNew = size * 4;
		else
			numOfPointsNew = mViewWidth * 4 - 4;
		
		this.invalidate();
	}
	
	@Deprecated
	public void moveDrawViewPoints(){
		
		for(byte j = 0; j < drawCount; j++){
        	
			points[j][0] = - offset;
			
			for(int i = 1; i < mReadFromFileData.numOfPoints - 1; i++){
				points[j][i * 4 - 2] = i - offset;
				points[j][i * 4] = i - offset;
			}
		}
	}

	public void avaregeDraw(){
		int window = 10;
		for(byte j = 0; j < drawCount; j++){
        	
			
			for(int i = 0; i < mReadFromFileData.numOfPoints - window * 4; i++){
				for(byte z = 1; z < window; z++){
					points[j][i * 4 + 1] += points[j][(i + z) * 4 + 1];
					points[j][i * 4 + 3] += points[j][(i + z) * 4 + 3];
				}
				points[j][i * 4 + 1] /= window;
				points[j][i * 4 + 3] /= window;
			}
		}
	}
}
