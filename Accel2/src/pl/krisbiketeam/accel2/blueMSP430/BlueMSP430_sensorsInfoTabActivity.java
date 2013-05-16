package pl.krisbiketeam.accel2.blueMSP430;


import java.util.ArrayList;

import pl.krisbiketeam.accel2.R;
import pl.krisbiketeam.accel2.blueMSP430sensor.BlueMSP430_acc;
import pl.krisbiketeam.accel2.blueMSP430sensor.BlueMSP430_register;
import pl.krisbiketeam.accel2.blueMSP430sensor.BlueMSP430_sensor;
import pl.krisbiketeam.accel2.blueMSP430sensor.BlueMSP430_temp;
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
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class BlueMSP430_sensorsInfoTabActivity extends ListActivity{

	//TODO zrobiæ ¿eby to by³o tylko w g³ównym Activity tylko raz zadeklarowane
	final static int BLUEMSP430_SENSORSINFOTABACTIVITY = 5;
	
	// Debugging
	private static final String TAG = "BlueMSP430_sensorsInfoTabActivity";
	private static final boolean D = true;
	
	private TextView selection;
	
	BlueMSP430_acc mBlueMSP430_acc, mBlueMSP430_acc_org;
	BlueMSP430_temp mBlueMSP430_temp, mBlueMSP430_temp_org;
	
	public ArrayList<BlueMSP430_register> local_registers;
	
	
	//ContextMenu
	public static final int MENU_SAVE = Menu.FIRST+1;
	public static final int MENU_RESET = Menu.FIRST+2;
	public static final int CONTEXT_MENU_EDIT = Menu.FIRST+3;
	
	
	
	private TabHost tabs=null;
	
	//private RawAdapter mRawAdapter; 
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (D) Log.d(TAG, "++++ ON CREATE ++++");
        super.onCreate(savedInstanceState);
        
		setContentView(R.layout.blue_sensors_info_tab);
		selection=(TextView)findViewById(R.id.tab_selection);
        
		if(getIntent().hasExtra(BlueMSP430_acc.CLASS_NAME))
			mBlueMSP430_acc = (BlueMSP430_acc) getIntent().getParcelableExtra(BlueMSP430_acc.CLASS_NAME);
		else
			mBlueMSP430_acc = null;
		
		if(getIntent().hasExtra(BlueMSP430_temp.CLASS_NAME))
			mBlueMSP430_temp = (BlueMSP430_temp) getIntent().getParcelableExtra(BlueMSP430_temp.CLASS_NAME);
		else
			mBlueMSP430_temp = null;
        
        try {
        	if(mBlueMSP430_acc != null)
        		mBlueMSP430_acc_org = (BlueMSP430_acc) mBlueMSP430_acc.clone();
        	else
        		mBlueMSP430_acc_org = null;
        	
        	if(mBlueMSP430_temp != null)
            	mBlueMSP430_temp_org = (BlueMSP430_temp) mBlueMSP430_temp.clone();
        	else
        		mBlueMSP430_temp_org = null;
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}        
        
		
		/*
		//ustawiamy pierwszy dowolny TabHost
		if(mBlueMSP430_acc != null)
			setCurrentLocalRegisters(mBlueMSP430_acc);
		else if(mBlueMSP430_temp != null)
			setCurrentLocalRegisters(mBlueMSP430_temp);
		else
			setCurrentLocalRegisters(null);
		
		// narazie tak musi byæ a setListAdapter(new RawAdapter());	jest i tak w updateCurrentTab;
		//updateCurrentTab();
		//mRawAdapter = new RawAdapter(local_registers);
		setListAdapter(new RawAdapter());//mRawAdapter);		
        //tabs.refreshDrawableState();
		*/
		tabs=(TabHost)findViewById(R.id.tabhost);
		tabs.setOnTabChangedListener(new OnTabChangeListener(){
            @Override
            public void onTabChanged(String tabId) {
            	updateCurrentTab(tabId);            	
            }});
		tabs.setup();
		
		if(mBlueMSP430_acc != null)
			addTab(mBlueMSP430_acc);
		if(mBlueMSP430_temp != null)
			addTab(mBlueMSP430_temp);
		
		//TODO jakoœ to dziwnie tu dzia³a ale na razi emusi byæ tak
		tabs.setCurrentTab(1);
		tabs.setCurrentTab(0);
		//updateCurrentTab(null);
		tabs.refreshDrawableState();
		
		
		registerForContextMenu(getListView());
	}
	
	/**
	 * ustawia local_registers na odpowiedni kontekst z klasy obecnie widocznej w aktywnej zak³adce
	 * @param s		rejestry którego czujnika maj¹ byæ wyœwietlane
	 */
	public void setCurrentLocalRegisters(BlueMSP430_sensor  s){
		if (s instanceof BlueMSP430_acc){
			s = (BlueMSP430_acc) s;
		}
		else if (s instanceof BlueMSP430_temp){
			s = (BlueMSP430_temp) s;	        
		}
		else{
			s = null;
		}
		if(s != null){
			if(getIntent().getBooleanExtra("CTRL", false)){
	        	//setListAdapter(new RawAdapter(mBlueMSP430_acc.control_registers));		
	        	local_registers = s.getControl_registers();
			}
	        else{
	        	//setListAdapter(new RawAdapter(mBlueMSP430_acc.registers));
	        	local_registers = s.getRegisters();
	        }
		}
	}
	
	/**
	 * ustawia odpowiednie dane do bie¿¹cej widocznej zak³adki
	 */
	public void updateCurrentTab(String tabId){
		if(tabId == null)
			tabId = tabs.getCurrentTabTag();
    	if(tabId.contains(BlueMSP430_acc.DESCRIPTION)){
	    		setCurrentLocalRegisters(mBlueMSP430_acc);
	    		//TODO coœ poprawiæ ¿by nie tworzy³o nowego obiektu a odœwierz³o te local registers
	    		//mRawAdapter.notifyDataSetChanged();
	    		setListAdapter(new RawAdapter());
	    	}
    	else if(tabId.contains(BlueMSP430_temp.DESCRIPTION)){
	    		setCurrentLocalRegisters(mBlueMSP430_temp);
	    		//TODO coœ poprawiæ ¿by nie tworzy³o nowego obiektu a odœwierz³o te local registers
	    		//mRawAdapter.notifyDataSetChanged();
	    		setListAdapter(new RawAdapter());
	    	}
    	//ArrayAdapter<Register> adapter = (ArrayAdapter<Register>)getListAdapter();
    	//adapter.notifyDataSetChanged();
	}
	
	/**
	 * tworzy zak³adkê z wybranym czujnikiem 
	 * @param s		zak³adka z jakim czujnikiem nba byæ utworzona
	 */	
	public void addTab(BlueMSP430_sensor s) {
		if (s instanceof BlueMSP430_acc){
			s = (BlueMSP430_acc) s;
		}
		else if (s instanceof BlueMSP430_temp){
			s = (BlueMSP430_temp) s;
		}
		else{
			s = null;
		}
		if(s != null){		
			TabHost.TabSpec spec=tabs.newTabSpec(s.describeSensor());
			
			spec.setContent(R.id.blue_sensors_info_tab_layout);//
			spec.setIndicator(s.describeSensor());//"Info");
			tabs.addTab(spec);
		}
		
	}
	
	//do menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_SAVE, Menu.NONE, "Save");
		menu.add(Menu.NONE, MENU_RESET, Menu.NONE, "Reset");
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case MENU_SAVE:
			finish_with_result(true);
			return (true);
		case MENU_RESET:
			//mBlueMSP430_acc = null;
			try {
				if(mBlueMSP430_acc != null)
					mBlueMSP430_acc = (BlueMSP430_acc) mBlueMSP430_acc_org.clone();
				if(mBlueMSP430_temp != null)
					mBlueMSP430_temp = (BlueMSP430_temp) mBlueMSP430_temp_org.clone();
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			updateCurrentTab(null);
        	
			return (true);
		}

		return super.onOptionsItemSelected(item);
	}
	// do edycji wartoœci rejestru
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		
		menu.add(Menu.NONE, CONTEXT_MENU_EDIT, Menu.NONE, "Edit Value");
		
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	// do edycji vartoœci rejestru
    @Override
	public boolean onContextItemSelected(MenuItem item) {
    	AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
    	//ArrayAdapter<Register> adapter = (ArrayAdapter<Register>)getListAdapter();
		
    	switch (item.getItemId()) {
	    	case CONTEXT_MENU_EDIT:
		    	final View editView = getLayoutInflater().inflate(R.layout.blue_sensors_info_edit_reg, null); 
		    	
		    	final TextView register = (TextView)editView.findViewById(R.id.blue_sensors_info_edit_reg_selected_register);
				final EditText value = (EditText)editView.findViewById(R.id.blue_sensors_info_edit_reg_value);
				final BlueMSP430_register reg = local_registers.get(info.position);
		    	
				register.setText(local_registers.get(info.position).getShortLabel());//adapter.getItem(info.position).short_label);
				value.setText(Integer.toString(local_registers.get(info.position).getVal()));//Integer.toString(adapter.getItem(info.position).val));
				if(!reg.isReadOnly()){
					new AlertDialog.Builder(this)
		    		.setTitle("Edit Registers Value")
		    		.setView(editView)
		    		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//TODO sprawdziæ czemu nie dzia³a wpisanie wartoœci przez wciœniêcie ok albo gotowe
						@Override
						public void onClick(DialogInterface dialog, int which) {
							//reg.val = Integer.parseInt(value.getText().toString());
							if(reg.setVal(Integer.parseInt(value.getText().toString()))){
								//TODO
								String s = tabs.getCurrentTabTag();
						    	if(s.contains(BlueMSP430_acc.DESCRIPTION)){
										mBlueMSP430_acc.update_any_register(reg);
										updateCurrentTab(BlueMSP430_acc.DESCRIPTION);
							    	}
						    	else if(s.contains(BlueMSP430_temp.DESCRIPTION)){
										mBlueMSP430_temp.update_any_register(reg);
										updateCurrentTab(BlueMSP430_temp.DESCRIPTION);
							    	}
						    	//mBlueMSP430_acc.update_any_register(reg);
						    	//ArrayAdapter<Register> adapter = (ArrayAdapter<Register>)getListAdapter();
								//adapter.notifyDataSetChanged();
						    	
							}
							else{
								Toast.makeText(getApplicationContext(), "Wrong Value", Toast.LENGTH_SHORT).show();
								//TODO zrobiæ ¿eby mo¿na by³o porawiæ zle wpisan¹ wartoœæ
							}
							
						}
					})
					.setNegativeButton("Cancel", null)
					.show();
				}
				else{
					Toast.makeText(getApplicationContext(), "Read only Register", Toast.LENGTH_SHORT).show();
				}
				
				return(true);
    	}
    	return(super.onContextItemSelected(item));
	}

	
	@Override
	public void onListItemClick(ListView parent, View v, int position, long id) {
		//ArrayAdapter<Register> adapter = (ArrayAdapter<Register>)getListAdapter();
		selection.setText(local_registers.get(position).getShortLabel());
		//TODO coœ poprawiæ ¿eby to lepiej wygl¹da³o
		BlueMSP430_sensor s = null;
		String t = tabs.getCurrentTabTag();
    	if(t.contains(BlueMSP430_acc.DESCRIPTION)){
    			s = mBlueMSP430_acc;
    		}
    	else if(t.contains(BlueMSP430_temp.DESCRIPTION)){
    			s = mBlueMSP430_temp;
    		}
		if(s != null){
	    	if(local_registers.get(position).getReg() == s.getCtrl()){
				Intent intent = new Intent(this, BlueMSP430_sensorsInfoTabActivity.class);
				intent.putExtra("CTRL", true);
				intent.putExtra(s.toString(), s);
				startActivityForResult(intent, BLUEMSP430_SENSORSINFOTABACTIVITY);
			}
		}
    }
	// tutaj wracamy po zamkniêciu nowego okna z dodatkowymi informacjami o rejestre obecnie CTRL
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case BLUEMSP430_SENSORSINFOTABACTIVITY:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				if(data.hasExtra(BlueMSP430_acc.CLASS_NAME)){
					mBlueMSP430_acc = (BlueMSP430_acc) data.getParcelableExtra(BlueMSP430_acc.CLASS_NAME);
					//updateCurrentTab(BlueMSP430_acc.CLASS_NAME);
				}
					
				if(data.hasExtra(BlueMSP430_temp.CLASS_NAME)){
					mBlueMSP430_temp = (BlueMSP430_temp) data.getParcelableExtra(BlueMSP430_temp.CLASS_NAME);
					//updateCurrentTab(BlueMSP430_temp.CLASS_NAME);
				}
				updateCurrentTab(null);
			}
			break;
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		//TODO  coœ to poprawiæ :/
		boolean b = false;
		if(mBlueMSP430_acc != null)
			if(!mBlueMSP430_acc.equals(mBlueMSP430_acc_org))
				b = true;
		if(mBlueMSP430_temp != null)
			if(!mBlueMSP430_temp.equals(mBlueMSP430_temp_org))
				b = true;
		if(b){	
		//if(!mBlueMSP430_acc.equalsList(mBlueMSP430_acc_org.registers)){	
			new AlertDialog.Builder(this)
			.setTitle("Save Changes?")
			.setMessage("Save changes to the registers?")
			.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish_with_result(true);
					
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish_with_result(false);
					
				}
			})
			.show();
		}
		else{
			super.onBackPressed();
		}
	}
	
	/**
	 * koñczy dzia³enie tego activity z list¹ i zwraca wynik jeœli b true
	 * @param b		jeœli true to RESULT_OK i do³¹cza zmodywikowane rejestry czujników
	 * 				jeœli false to RESULT_CANCELED
	 */
	private void finish_with_result(boolean b){
    	// Create the result Intent and include the MAC address
        //TODO tu te¿ coœ poprawiæ w nawi¹zaniu do onBackPresed()
		Intent intent = new Intent();
        if(b){
        	intent.putExtra("CTRL", true);
        	if(mBlueMSP430_acc != null)
        		if(!mBlueMSP430_acc.equals(mBlueMSP430_acc_org))
        			intent.putExtra(BlueMSP430_acc.CLASS_NAME, mBlueMSP430_acc);
        	if(mBlueMSP430_temp != null)
        		if(!mBlueMSP430_temp.equals(mBlueMSP430_temp_org))
        			intent.putExtra(BlueMSP430_temp.CLASS_NAME, mBlueMSP430_temp);
        	
        	if(mBlueMSP430_acc == null && mBlueMSP430_temp == null)
        		// Set result and finish this Activity
                setResult(Activity.RESULT_CANCELED, intent);
        	else
        		// Set result and finish this Activity
        		setResult(Activity.RESULT_OK, intent);
        }
        else{
        	// Set result and finish this Activity
            setResult(Activity.RESULT_CANCELED, intent);
        }
        finish();
    }
    
	
	class RawAdapter extends ArrayAdapter<BlueMSP430_register> {
    	ArrayList<BlueMSP430_register> regist = null;
    	RawAdapter() {
    		super(BlueMSP430_sensorsInfoTabActivity.this, 
        			R.layout.blue_sensors_info_row, R.id.blue_sensors_info_label,
        			local_registers);
    	}
    	RawAdapter(ArrayList<BlueMSP430_register> regist) {
    		super(BlueMSP430_sensorsInfoTabActivity.this, 
        			R.layout.blue_sensors_info_row, R.id.blue_sensors_info_label,
        			regist);
    		this.regist = regist;
    		
    	}
    	
    	  	
    	@Override
		public View getView(int position, View convertView, ViewGroup parent) {
    		//dzia³a ok bo ju¿ jest recyclowane wiêc nie ma co uzywaæ convertView
    		View row=super.getView(position, convertView, parent);
    		
    		ViewHolder holder=(ViewHolder)row.getTag();
    		if (holder == null) {
	    		holder=new ViewHolder(row);
	    		row.setTag(holder);
    		}
    		
    		holder.value.setText(Integer.toString(local_registers.get(position).getVal()));//regist.get(position).getVal()));//
    		return(row);
    		
    		/*View row=convertView;
    		if(row == null){
    			LayoutInflater inflator = getLayoutInflater();
    			row = inflator.inflate(R.layout.blue_sensors_info_raw, null);
    			final ViewHolder viewHolder = new ViewHolder();
    			viewHolder.text = (TextView) row.findViewById(R.id.blue_sensors_info_value);
    			row.setTag(viewHolder);
    		}
    		ViewHolder holder = (ViewHolder) row.getTag();
    		holder.text.setText(Integer.toString(local_registers.get(position).getVal()));
    		return row;*/
    	}
    }
	
	class ViewHolder {
		TextView value;
		
		ViewHolder(View base) {
			value=(TextView)base.findViewById(R.id.blue_sensors_info_value);
    	}
	}
}
