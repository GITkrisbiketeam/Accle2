package pl.krisbiketeam.accel2;

import pl.krisbiketeam.accel2.blueMSP430sensor.BlueMSP430SensorEvent;
import android.hardware.SensorEvent;


public class MySensorEvent {
	
	//private static long bootTime = (System.currentTimeMillis() - SystemClock.elapsedRealtime()) * 1000000L;
	
	public int accuracy;
	public MySensor sensor;
	/**
	 * czas uptime od uruchomienia komórki nie ten od 1970
	 */
	public long timestamp;
	public float values[];
	
	
	/**
	 * tworzy nowe zda¿enie czujnika WEWNÊTRZNEGO z czujnikiem jako NOWYM OBIEKTEM na bazie czujnika ze zda¿enia wewnêtrznego czujnika
	 * Z£E to bo tworzy nowy obiekt MySensorza ka¿dym razem
	 * @param event zdarzenie WEWNÊTRZNEGO czujnika
	 */
	@Deprecated
	MySensorEvent(SensorEvent event){
		this.accuracy = event.accuracy;
		this.sensor = new MySensor(event.sensor);
		// to jest czas "uptime" a nie od 1970 wiêc siê nie nadaje do konwesji
		//this.timestamp = event.timestamp;
		//this.timestamp = bootTime + event.timestamp;
		this.timestamp = System.currentTimeMillis();
		this.values = event.values.clone();
	}
	/**
	 * tworzy nowe zda¿enie czujnika ZEWNÊTRZNEGO z czujnikiem jako NOWYM OBIEKTEM na bazie czujnika ze zda¿enia zewnêtrznego czujnika
	 * Z£E to bo tworzy nowy obiekt MySensorza ka¿dym razem
	 * @param event zdarzenie ZEWNÊTRZNEGO czujnika
	 */
	@Deprecated
	MySensorEvent(BlueMSP430SensorEvent event){
		this.accuracy = event.accuracy;
		this.sensor = new MySensor(event.sensor);
		// to jest czas "uptime" a nie od 1970 wiêc siê nie nadaje do konwesji
		//this.timestamp = event.timestamp;
		//this.timestamp = bootTime + event.timestamp;
		this.timestamp = System.currentTimeMillis();
		this.values = event.values.clone();
	}
	/**
	 * tworzy nowe zda¿enie czujnika WEWNÊTRZNEGO z czujnikiem podanym w argumencie
	 * @param event zdarzenie WEWNÊTRZNEGO czujnika
	 * @param sensor czujnik na bazie MySensor
	 */
	MySensorEvent(SensorEvent event, MySensor sensor){
		this.accuracy = event.accuracy;
		this.sensor = sensor;
		this.timestamp = event.timestamp;
		this.values = event.values.clone();
	}
	/**
	 * tworzy nowe zda¿enie czujnika ZEWNÊTRZNEGO z czujnikiem podanym w argumencie
	 * @param event zdarzenie ZEWNÊTRZNEGO czujnika
	 * @param sensor czujnik na bazie MySensor
	 */
	MySensorEvent(BlueMSP430SensorEvent event, MySensor sensor){
		this.accuracy = event.accuracy;
		this.sensor = sensor;
		this.timestamp = event.mSensorTimeStamp;
		this.values = event.values.clone();
	}
}
