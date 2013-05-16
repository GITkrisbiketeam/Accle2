package pl.krisbiketeam.accel2.blueMSP430;

import pl.krisbiketeam.accel2.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class DummyActivityDialog extends Activity{

	// Debugging
	private static final String TAG = "DummyActivityDialog";
	private static final boolean D = true;

	private String title;
	private String broadcast;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (D) Log.d(TAG, "+ ON CREATE +");
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.dummy_activity);
        
		CharSequence[] items = this.getIntent().getCharSequenceArrayExtra("Device_List");
		title = this.getIntent().getStringExtra("Title");
		broadcast = this.getIntent().getStringExtra("Broadcast");
		
		new AlertDialog.Builder(this)
		.setTitle(title)
		//.setView(editView)
		.setItems(items, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				sendMyBroadcast(which);
			}

		}).setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				finishMe();
			}
		})
				
		.show();
	}
	/**
	 * kończy to Activity
	 */
	private void finishMe(){
		this.finish();
	}

	/**
     * wysy�a Broadcasta z adresem modu�u z jaki zosta� wybrany z listy
     * @param data Intent z adresem Bluetooth urz�dzenia do pod��czenia si�
     */
    private void sendMyBroadcast(int which){
    	Intent broadcastIntent;
    	broadcastIntent = new Intent(broadcast);
    	broadcastIntent.putExtra("which", which);
    	//wysy�amy og�le powiadomienie
    	LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    	finishMe();
    }
	
}
