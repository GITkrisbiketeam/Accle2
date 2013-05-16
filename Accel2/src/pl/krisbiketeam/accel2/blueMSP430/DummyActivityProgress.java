package pl.krisbiketeam.accel2.blueMSP430;

import pl.krisbiketeam.accel2.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.util.Log;

public class DummyActivityProgress extends Activity{

	// Debugging
		private static final String TAG = "DummyActivityProgress";
		private static final boolean D = true;
		
		public static final String PROGRESS_MAX = "Progress Max";
		public static final String PROGRESS_UPDATE = "Progress Update";
		public static final String PROGRESS_CLOSE = "Progress Close";
		
		
		ProgressDialog progressDialog;
    	
		
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D) Log.d(TAG, "+ ON CREATE +");
        
        setContentView(R.layout.dummy_activity);
        
        int progressMax = getIntent().getIntExtra(PROGRESS_MAX, 10);
		
        
        progressDialog = new ProgressDialog(this);
		//progressDialog.setTitle("Reading BlueMSP430 sensors registers");
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setProgress(0);
		progressDialog.setMax(progressMax);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setCancelable(true);
		progressDialog.setOnCancelListener(new OnCancelListener()	{

			@Override
			public void onCancel(DialogInterface arg0) {
				closeThisActivityProgress();
			}
			
		});
		
		progressDialog.show();
		
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (D) Log.d(TAG, "+ ON CREATE +");
        
		// TODO Auto-generated method stub
		//super.onNewIntent(intent);
		if(intent.hasExtra(PROGRESS_CLOSE)){
			closeThisActivityProgress();			
		}
		else{
			progressDialog.incrementProgressBy(1);
		}
	}
   
	private void closeThisActivityProgress(){
		progressDialog.dismiss();
		this.finish();
	}
	
}
