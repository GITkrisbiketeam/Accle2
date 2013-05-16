package pl.krisbiketeam.accel2.file;

import java.io.File;

import pl.krisbiketeam.accel2.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SavedFilesListActivity extends ListActivity {
	
	// Debugging
	private static final String TAG = "SavedFilesListActivity";
	private static final boolean D = true;
	
	private String [] mFilesList;
	File myFile;

	ArrayAdapter<String> adapter;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (D) Log.d(TAG, "++++ ON CREATE ++++");
	    super.onCreate(savedInstanceState);
		
		setContentView(R.layout.files_list);
		
		
		myFile = new File(this.getExternalFilesDir(null).getPath());
		mFilesList = myFile.list();
		
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mFilesList);
		setListAdapter(adapter);
		
		registerForContextMenu(getListView());
	}

	/* (non-Javadoc)
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		//super.onListItemClick(l, v, position, id);
		Intent intent = new Intent();
        intent.putExtra("fileName", mFilesList[position]);
        setResult(Activity.RESULT_OK, intent);
        finish();
	}
    
	//do menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "Delete All");
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		File[] allFiles = myFile.listFiles();
		for(File f : allFiles){
			f.delete();
		}
		finish();
		
		return super.onOptionsItemSelected(item);
	}
	
	// do edycji wartoœci rejestru
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		
		menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "Delete File");
		
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	private void refreshItSelf(){
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mFilesList);
		setListAdapter(adapter);
		//adapter.notifyDataSetChanged();
    	        	   
		
	}
	
	// do edycji vartoœci rejestru
    @Override
	public boolean onContextItemSelected(MenuItem item) {
    	final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
    	final File[] allFiles = myFile.listFiles();
		
    	
		    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage("Delete selected file?")
    	       .setCancelable(false)
    	       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    	           @Override
				public void onClick(DialogInterface dialog, int id) {
    	        	   //String s = allFiles[info.position].getName();
    	        	   //adapter.remove(s);
    	        	   allFiles[info.position].delete();
    	        	   mFilesList = myFile.list();
    	        	   refreshItSelf();
    	           }
    	       })
    	       .setNegativeButton("No", new DialogInterface.OnClickListener() {
    	           @Override
				public void onClick(DialogInterface dialog, int id) {
    	                dialog.cancel();
    	           }
    	       })
    	       .show();
    	//AlertDialog alert = builder.create();
		
    	return(super.onContextItemSelected(item));
	}

	
}
