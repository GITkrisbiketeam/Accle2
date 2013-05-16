package pl.krisbiketeam.accel2;

import pl.krisbiketeam.accel2.SensorService.OnMySensorChanegedListener;

/**
 * klasa obudowuj¹ca SensorService do rejestrowania i odrejestrowania MySensor
 * @author Krzys
 *
 */
public class MySensorManager {
	
	private SensorService mSensorService;
	
	MySensorManager(SensorService sensorService){
		mSensorService = sensorService;
	}
	
	// Allows the user to set an Listener and react to the event
	public void registerMyListener(OnMySensorChanegedListener listener, MySensor sensor) {
		mSensorService.registerOnMySensorChanegedListener(listener, sensor);
	}
	// Allows the user to set an Listener and react to the event
	public void unregisterMyListener(OnMySensorChanegedListener listener, MySensor sensor) {
		mSensorService.unregisterOnMySensorChanegedListener(listener, sensor);
	}
}
