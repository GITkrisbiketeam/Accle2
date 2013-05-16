package pl.krisbiketeam.accel2;

import pl.krisbiketeam.accel2.blueMSP430sensor.BlueMSP430SensorEvent;
import android.hardware.SensorEvent;


public class MySensorEvent {
	
	//private static long bootTime = (System.currentTimeMillis() - SystemClock.elapsedRealtime()) * 1000000L;
	
	public int accuracy;
	public MySensor sensor;
	/**
	 * czas uptime od uruchomienia kom�rki nie ten od 1970
	 */
	public long timestamp;
	public float values[];
	
	
	/**
	 * tworzy nowe zda�enie czujnika WEWN�TRZNEGO z czujnikiem jako NOWYM OBIEKTEM na bazie czujnika ze zda�enia wewn�trznego czujnika
	 * Z�E to bo tworzy nowy obiekt MySensorza ka�dym razem
	 * @param event zdarzenie WEWN�TRZNEGO czujnika
	 */
	@Deprecated
	MySensorEvent(SensorEvent event){
		this.accuracy = event.accuracy;
		this.sensor = new MySensor(event.sensor);
		// to jest czas "uptime" a nie od 1970 wi�c si� nie nadaje do konwesji
		//this.timestamp = event.timestamp;
		//this.timestamp = bootTime + event.timestamp;
		this.timestamp = System.currentTimeMillis();
		this.values = event.values.clone();
	}
	/**
	 * tworzy nowe zda�enie czujnika ZEWN�TRZNEGO z czujnikiem jako NOWYM OBIEKTEM na bazie czujnika ze zda�enia zewn�trznego czujnika
	 * Z�E to bo tworzy nowy obiekt MySensorza ka�dym razem
	 * @param event zdarzenie ZEWN�TRZNEGO czujnika
	 */
	@Deprecated
	MySensorEvent(BlueMSP430SensorEvent event){
		this.accuracy = event.accuracy;
		this.sensor = new MySensor(event.sensor);
		// to jest czas "uptime" a nie od 1970 wi�c si� nie nadaje do konwesji
		//this.timestamp = event.timestamp;
		//this.timestamp = bootTime + event.timestamp;
		this.timestamp = System.currentTimeMillis();
		this.values = event.values.clone();
	}
	/**
	 * tworzy nowe zda�enie czujnika WEWN�TRZNEGO z czujnikiem podanym w argumencie
	 * @param event zdarzenie WEWN�TRZNEGO czujnika
	 * @param sensor czujnik na bazie MySensor
	 */
	MySensorEvent(SensorEvent event, MySensor sensor){
		this.accuracy = event.accuracy;
		this.sensor = sensor;
		this.timestamp = event.timestamp;
		this.values = event.values.clone();
	}
	/**
	 * tworzy nowe zda�enie czujnika ZEWN�TRZNEGO z czujnikiem podanym w argumencie
	 * @param event zdarzenie ZEWN�TRZNEGO czujnika
	 * @param sensor czujnik na bazie MySensor
	 */
	MySensorEvent(BlueMSP430SensorEvent event, MySensor sensor){
		this.accuracy = event.accuracy;
		this.sensor = sensor;
		this.timestamp = event.mSensorTimeStamp;
		this.values = event.values.clone();
	}
}
