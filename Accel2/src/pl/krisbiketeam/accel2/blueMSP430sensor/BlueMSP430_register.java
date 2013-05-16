package pl.krisbiketeam.accel2.blueMSP430sensor;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * klasa przechowuj¹ca rane pojedynczego rejestru
 * @author Krzyœ
 *String short_label  	skrócona nazwa rejestru
 *String label			opis rejestru
 *int reg				rejestr
 *int val				wartoœæ rejestru
 *boolean read_only		czy dany rejestr jest tylko do odczytu
 *int max_val			maksymalna wartoœæ rejestru
 */
public class BlueMSP430_register implements Parcelable, Cloneable{
	private String shortLabel;
	private String label;
	private int reg;
	private int val = 0;
	private boolean readOnly = true;
	private int maxVal = 0x0FF;
	
	public BlueMSP430_register(int reg){
		this.reg = reg;
	}
	public BlueMSP430_register(String short_label, int reg, String label){
		this.shortLabel = short_label;
		this.label = label;
		this.reg = reg;
	}
	public BlueMSP430_register(String shortLabel, int reg, String label, int val){
		this(shortLabel, reg, label);
		this.val = val;
	}
	public BlueMSP430_register(String shortLabel, int reg, String label, int val, boolean readOnly){
		this(shortLabel, reg, label, val);
		this.readOnly = readOnly;
	}
	public BlueMSP430_register(String shortLabel, int reg, String label, int val, boolean readOnly, int maxVal){
		this(shortLabel, reg, label, val, readOnly);
		this.maxVal = maxVal;
	}

	public BlueMSP430_register(Parcel in){
		label = in.readString();
		shortLabel = in.readString();
		reg = in.readInt();
		val = in.readInt();
		readOnly = (Boolean) in.readValue(null);
		maxVal = in.readInt();
		
	}
	
	@Override
	public String toString(){
		return label;
	}
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public static final Parcelable.Creator<BlueMSP430_register> CREATOR = new Parcelable.Creator<BlueMSP430_register>() {
        @Override
		public BlueMSP430_register createFromParcel(Parcel in) {
            return new BlueMSP430_register (in);
        }

        @Override
		public BlueMSP430_register [] newArray(int size) {
            return new BlueMSP430_register [size];
        }
    };
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(label);
		dest.writeString(shortLabel);
		dest.writeInt(reg);
		dest.writeInt(val);
		dest.writeValue(Boolean.valueOf(readOnly));
		dest.writeInt(maxVal);
		
	}
	//TODO dopisaæ sprawdzanie czy wstawiana wartoœæ jest w odpowiednim zakresie ew. przerobiæ na private zmienne
	/**
	 * @return the short_label
	 */
	public String getShortLabel() {
		return shortLabel;
	}
	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * @return the reg
	 */
	public int getReg() {
		return reg;
	}
	/**
	 * @return the val
	 */
	public int getVal() {
		return val;
	}
	/**
	 * @param val the val to set
	 * @return jeœli ustawiana wartoœæ jest mniejsza od max_val to true jeœli nie to false
	 */
	public boolean setVal(int val) {
		if(val <= this.maxVal && val >= 0 && !this.readOnly){
			this.val = val;
			return(true);
		}
		else{
			return(false);
		}
			
	}
	/**
	 * nie sprawdza czy ustawiana wartoœæ jest w danym zakresie
	 * @param val the val to set
	 */
	public void unsecureSetVal(int val) {
		this.val = val;
	}
	/**
	 * @return the read_only
	 */
	public boolean isReadOnly() {
		return readOnly;
	}
	/**
	 * @return the max_val
	 */
	public int getMaxVal() {
		return maxVal;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Object clone() throws CloneNotSupportedException {
		//Register temp = new Register(shortLabel, reg, label, val, readOnly, maxVal);
		return super.clone();
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof BlueMSP430_register)){
			return false;
		}
		BlueMSP430_register temp = (BlueMSP430_register) o;
		if(temp.reg == this.reg) return true;
		else return false;
		//return super.equals(o);
	}
	
	
}