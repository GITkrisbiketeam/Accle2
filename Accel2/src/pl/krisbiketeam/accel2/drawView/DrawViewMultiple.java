package pl.krisbiketeam.accel2.drawView;

import pl.krisbiketeam.accel2.MySensor;
import pl.krisbiketeam.accel2.MySensorManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;

public class DrawViewMultiple extends DrawView{

	// Debugging
	private static final String TAG = "DrawViewMultiple";
	private static final boolean D = true;

	
	/**
     * tablica z punktami na bazie kt�rych onDraw b�dzie rysowa� lini�
     * rozmiar zale�ny od ilo�ci wykres�w
     */
    //protected float points[][];
	/**
	 * liczba wykres�w do narysowania
	 */
    protected int drawCount = 0;
    
    /**
     * kolorki wykres�w
     */
    protected Paint paintDraw[];
   

	public DrawViewMultiple(MySensor sensor, MySensorManager sensorManage, Context context, AttributeSet attrs) {
		super(sensor, sensorManage, context, attrs);
	}
	@Deprecated
	public DrawViewMultiple(Context context) {
		super(context);
	}
	@Deprecated
	public DrawViewMultiple(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	@Deprecated
	public DrawViewMultiple(Context context, AttributeSet attrs, int defStyle) {
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
		//je�li nie zainicjowane to wyjd�
		if(points == null) return;
		
		for(byte i = 0; i < drawCount; i++){
			canvas.drawLines(points[i], 0, numOfPointsNew, paintDraw[i]);
		}
		canvas.drawText(scaleMax, 0, paintTextBlack.getTextSize(), paintTextBlack);
		canvas.drawText(scaleMin, 0, mViewHeight, paintTextBlack);
		
		canvas.drawText(sensorName, mViewWidth, paintTextBlack.getTextSize(), paintTextBlackRight);
		
		if(recorded){
			canvas.drawText("Recorded", mViewWidth, mViewHeight, paintTextRedRight);
			//TODO: Nie dzia�a co�
			canvas.drawCircle(mViewHeight-10, mViewWidth-10, 5, paintDrawRed);
		}
		
		// and make sure to redraw asap
        invalidate();
		
	}
	
		
	/**
	 * dodaje punkty do loklanej tablicy pointsX, pointsY, pointsZ z now� lini� do narysowania
	 * @param values	tablica z warto�ciami danych z czujnika
	 */
	@Override
	protected void addPointsToTable(float[] values){
		if(mViewWidth == 0 || mViewHeight == 0) return;
		
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
			//bie��cy punkt
			points[i][numOfPointsNew - 2] = timeAxisNew;
			points[i][numOfPointsNew - 1] = mMyScale.myScaledValue(values[i]);
			//punkt do nast�pnej linii
			points[i][numOfPointsNew] = timeAxisNew;
			points[i][numOfPointsNew + 1] = points[i][numOfPointsNew - 1];
			
		}
		
		invalidate();
	}
}
