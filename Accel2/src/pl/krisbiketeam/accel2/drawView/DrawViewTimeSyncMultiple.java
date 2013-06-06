package pl.krisbiketeam.accel2.drawView;

import pl.krisbiketeam.accel2.MySensor;
import pl.krisbiketeam.accel2.MySensorManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;

public class DrawViewTimeSyncMultiple extends DrawViewTimeSync{

	// Debugging
	private static final String TAG = "DrawViewMultiple";
	private static final boolean D = true;

	
	/**
     * tablica z punktami na bazie których onDraw bêdzie rysowaæ liniê
     * rozmiar zale¿ny od iloœci wykresów
     */
    //protected float points[][];
	/**
	 * liczba wykresów do narysowania
	 */
    protected int drawCount = 0;
    
    /**
     * kolorki wykresów
     */
    protected Paint paintDraw[];
   

	public DrawViewTimeSyncMultiple(MySensor sensor, MySensorManager sensorManage, Context context, AttributeSet attrs) {
		super(sensor, sensorManage, context, attrs);
	}
	@Deprecated
	public DrawViewTimeSyncMultiple(Context context) {
		super(context);
	}
	@Deprecated
	public DrawViewTimeSyncMultiple(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	@Deprecated
	public DrawViewTimeSyncMultiple(Context context, AttributeSet attrs, int defStyle) {
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
		paintDraw = new Paint[6];
		for(byte i = 0; i < 6; i++){
			paintDraw[i] = new Paint(); 
		}
		paintDraw[0].setColor(Color.RED);
		paintDraw[1].setColor(Color.GREEN);
		paintDraw[2].setColor(Color.BLUE);
		paintDraw[3].setColor(Color.YELLOW);
		paintDraw[4].setColor(Color.MAGENTA);		
	}
	
	/* (non-Javadoc)
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		//super.onDraw(canvas);
		//jeœli nie zainicjowane to wyjdŸ
		if(points == null) return;
		
		for(byte i = 0; i < drawCount; i++){
			canvas.drawLines(points[i], 0, numOfPointsNew, paintDraw[i]);
		}
		canvas.drawText(scaleMaxText, 0, paintTextBlack.getTextSize(), paintTextBlack);
		canvas.drawText(scaleMinText, 0, mViewHeight, paintTextBlack);
		
		canvas.drawText(sensorNameText, mViewWidth, paintTextBlack.getTextSize(), paintTextBlackRight);
		
		if(recorded){
			canvas.drawText("Recorded", mViewWidth, mViewHeight, paintTextRedRight);
			//TODO: Nie dzia³a coœ
			canvas.drawCircle(mViewHeight-10, mViewWidth-10, 5, paintDrawRed);
		}
		
		// and make sure to redraw asap
        invalidate();
		
	}
	
		
	/**
	 * dodaje punkty do loklanej tablicy pointsX, pointsY, pointsZ z nowï¿½ liniï¿½ do narysowania
	 * @param values	tablica z wartoï¿½ciami danych z czujnika
	 * @param timeStamp	czas nano wyst¹pienia zda¿enia
	 */
	@Override
	protected void addPointsToTable(float[] values, long timeStamp){
		
		if(mViewWidth == 0 || mViewHeight == 0) return;
		
		if(points == null){
			drawCount = values.length;
			points = new float[drawCount][mViewWidth*4 + 2];		// zapasowe dwa punkty na pocz¹tek kolejnego punktu
			numOfPointsNew = 0;
			nanoTimeOffset = timeStamp;
			/*
			 * 100Hz -> 10ms -> 10 000us - 10 000 000ns
			 */
			nanoTimeScale = 5000000;
			rebuildScale();
			for(byte i = 0; i < drawCount; i++){
				//bieï¿½ï¿½cy punkt
				points[i][numOfPointsNew] = (timeStamp - nanoTimeOffset) / nanoTimeScale; //mo¿e zrobic rzutowanie/zamieniæ na inty to siê szybciej wykona
				points[i][numOfPointsNew + 1] = mMyScale.myScaledValue(values[i]);
			}
			return;
		}
		
		if(numOfPointsNew >= mViewWidth * 4 || (timeStamp - nanoTimeOffset) / nanoTimeScale >= mViewWidth){
       
        	numOfPointsNew = 0;
        	nanoTimeOffset = timeStamp;
        	for(byte i = 0; i < drawCount; i++){
        		points[i][numOfPointsNew] = (timeStamp - nanoTimeOffset) / nanoTimeScale; //mo¿e zrobic rzutowanie/zamieniæ na inty to siê szybciej wykona
        		points[i][numOfPointsNew + 1] = mMyScale.myScaledValue(values[i]);
        	}
			return;
        }
		
		for(byte i = 0; i < drawCount; i++){
			//bieï¿½ï¿½cy punkt
			points[i][numOfPointsNew + 2] = (timeStamp - nanoTimeOffset) / nanoTimeScale; //mo¿e zrobic rzutowanie/zamieniæ na inty to siê szybciej wykona;
			points[i][numOfPointsNew + 3] = mMyScale.myScaledValue(values[i]);
			//punkt do nastï¿½pnej linii
			points[i][numOfPointsNew + 4] = points[i][numOfPointsNew + 2];
			points[i][numOfPointsNew + 5] = points[i][numOfPointsNew + 3];
		}
		numOfPointsNew +=4;
        
		invalidate();
		/*
		if(points == null){
			drawCount = values.length;
			points = new float[values.length][mViewWidth*4];
			numOfPointsNew = 0;
			timeAxisNew = 0;
			rebuildScale();
			for(byte i = 0; i < drawCount; i++){
				points[i][numOfPointsNew] = timeAxisNew;
				points[i][numOfPointsNew + 1] = mMyScale.myScaledValue(values[i]);
			}
			
			return;
		}
		
		if(timeAxisNew < mViewWidth - 1){
        	timeAxisNew++;
        	numOfPointsNew +=4;
        }
        else{
        	timeAxisNew = 0;        	
        	numOfPointsNew = 0;
        	for(byte i = 0; i < drawCount; i++){
				points[i][numOfPointsNew] = timeAxisNew;
				points[i][numOfPointsNew + 1] = mMyScale.myScaledValue(values[i]);
			}
			return;
        } 
		
		for(byte i = 0; i < drawCount; i++){
			//bie¿¹cy punkt
			points[i][numOfPointsNew - 2] = timeAxisNew;
			points[i][numOfPointsNew - 1] = mMyScale.myScaledValue(values[i]);
			//punkt do nastêpnej linii
			points[i][numOfPointsNew] = timeAxisNew;
			points[i][numOfPointsNew + 1] = points[i][numOfPointsNew - 1];
			
		}
		
		invalidate();
		*/
	}
}
