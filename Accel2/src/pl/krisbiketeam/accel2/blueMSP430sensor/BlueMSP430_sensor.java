package pl.krisbiketeam.accel2.blueMSP430sensor;

import java.util.ArrayList;
import android.os.Parcelable;

abstract public class BlueMSP430_sensor implements Parcelable, Cloneable  {

	//public ArrayList<Register> registers = new ArrayList<Register>();
	//public ArrayList<Register> control_registers = new ArrayList<Register>();
	/**
	 * @return the registers
	 */
	abstract public ArrayList<BlueMSP430_register> getRegisters();
	/**
	 * @param registers the registers to set
	 */
	abstract public void setRegisters(ArrayList<BlueMSP430_register> registers);		
	/**
	 * @return the control_registers
	 */
	abstract public ArrayList<BlueMSP430_register> getControl_registers();
	/**
	 * @param control_registers the control_registers to set
	 */
	abstract public void setControl_registers(ArrayList<BlueMSP430_register> control_registers);
	/**
	 * @return the ctrl
	 */
	abstract public int getCtrl();
	abstract public String describeSensor();

	
}
