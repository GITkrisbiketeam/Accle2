package pl.krisbiketeam.accel2.drawView;

/**
 * klasa przechowuj¹ca przeliczone wartoœci potrzebne do przeskalowania liniowej skali na skalê 
 * logarytmiczn¹, zakres jest podawany w trakcie tworzenia tej klasy od 0 do maxVal oraz w 
 * zale¿noœci od iloœci potrzebnych punktów do wykresu podanych jako raster czyli 
 * liczba punktów skali (wysokoœæ wykresu)
 * @author Krzyœ
 *
 */
public class MyLog extends MyScale{
	//liczba punktów skali logarytmicznej tylko jednego zakresu dodatniego albo ujemnego
	int numberOfPoints;
	// wspó³czynnik skalowania wymagany do zwrócenia odpowiedniej wartoœci 
	float acc_multiplier;
	//tablica przechowuj¹ca przeskalowane wartoœci odpowiadaj¹ce iloœci punktów skali (wys wykresu)
	int logTab[];
	
	float minValue, maxValue;
	
	@Deprecated	
	int temp;
	
	MyLog(){
		
	}
	@Deprecated	
	MyLog(float maxVal, int window){
		createLogTable(maxVal, window);
	}
	@Deprecated	
	MyLog(float maxVal, int window, float resolution){
		createLogTable(maxVal, window, resolution);
	}
	MyLog(float minVal, float maxVal, int window){
		createLogTable(minVal, maxVal, window);
	}
	MyLog(float minVal, float maxVal, int window, float resolution){
		createLogTable(minVal, maxVal, window, resolution);
	}
	
	/**
	 * funkcja inicjalizuj¹ca wartoœci w logTab[] oraz obliczaj¹ca wspó³czynnik skalowania 
	 * wartoœci wejœciowej np przyspieszenia
	 * @param maxVal	maksymalana wartoœæ wejœciowa wartoœci do przeskaowania ze skali 
	 * 					liniowej na logarytmiczn¹
	 * @param window	liczba punktów na skali maksymala wartoœæ w logTab[]
	 */
	@Deprecated
	public void createLogTable(float maxVal, int window){
		window = window / 2;									// bo mamy wartoœci dodatnie i ujemne ale liczymy tylko raz dla dodatnich
		float a = (float) (window / Math.log10(maxVal+1));				//+1 przesuwamy skale o 1 w prawo co by mieæ wartoœci zaczynaj¹ce siê od 0
		numberOfPoints = (int) (maxVal / (Math.pow(10, (1/a)) - 1)) + 1;
		logTab = new int[numberOfPoints * 2];
		float b = maxVal/numberOfPoints;
		//TODO sprawdziæ czy przypadkiem nie wpisuje wartoœci poza zakresem bo mi wykrzacza jak siê dojdzie do maksymalnej wartoœci
		for(int i = 0; i <numberOfPoints; i++){ 		// dodatnie wartoœci
			logTab[i + numberOfPoints] = (int) (window - a * Math.log10(i * b + 1));		//+1 przesuwamy skale o 1 w prawo co by mieæ wartoœci zaczynaj¹ce siê od 0
			logTab[i] = 2*window - logTab[i + numberOfPoints];
		}
		if(maxVal != 0)
			acc_multiplier = (numberOfPoints - 1)/ maxVal;
	}
	/**
	 * funkcja inicjalizuj¹ca wartoœci w logTab[] oraz obliczaj¹ca wspó³czynnik skalowania 
	 * wartoœci wejœciowej np przyspieszenia
	 * @param maxVal	maksymalana wartoœæ wejœciowa wartoœci do przeskaowania ze skali 
	 * 					liniowej na logarytmiczn¹
	 * @param window	liczba punktów na skali maksymala wartoœæ w logTab[]
	 * @param resolution	minimalny skok pojedynczej wartoœci 
	 */
	@Deprecated
	public void createLogTable(float maxVal, int window, float resolution){
		numberOfPoints = (int) (maxVal / resolution) + 1;
		window = window / 2;									// bo mamy wartoœci dodatnie i ujemne ale liczymy tylko raz dla dodatnich
		float a = (float) (window / Math.log10(maxVal+1));				//+1 przesuwamy skale o 1 w prawo co by mieæ wartoœci zaczynaj¹ce siê od 0
		logTab = new int[numberOfPoints * 2];
		float b = resolution; //maxVal/numberOfPoints;
		//TODO sprawdziæ czy przypadkiem nie wpisuje wartoœci poza zakresem bo mi wykrzacza jak siê dojdzie do maksymalnej wartoœci
		for(int i = 0; i <numberOfPoints; i++){ 		// dodatnie wartoœci
			logTab[i + numberOfPoints] = (int) (window - a * Math.log10(i * b + 1));		//+1 przesuwamy skale o 1 w prawo co by mieæ wartoœci zaczynaj¹ce siê od 0
			logTab[i] = 2*window - logTab[i + numberOfPoints];
		}
		acc_multiplier = (numberOfPoints - 1)/ maxVal;
	}
	
	/**
	 * funkcja inicjalizuj¹ca wartoœci w logTab[] oraz obliczaj¹ca wspó³czynnik skalowania 
	 * wartoœci wejœciowej np przyspieszenia
	 * @param minVal	minimalna wartoœæ wejœciowa wartoœci do przeskaowania ze skali 
	 * 					liniowej na logarytmiczn¹
	 * @param maxVal	maksymalana wartoœæ wejœciowa wartoœci do przeskaowania ze skali 
	 * 					liniowej na logarytmiczn¹
	 * @param window	liczba punktów na skali maksymala wartoœæ w logTab[]
	 */
	public void createLogTable(float minVal, float maxVal, int window){
		createLogTable(maxVal, window);
		/*if(maxVal != minVal && minVal < maxVal && window >1){
				
				
			
			int maxWindow = (int)(maxVal / (maxVal - minVal) * window);
			int minWindow = (int)(minVal / (maxVal - minVal) * window);
			
			float aMax = (float) (window / Math.log10(maxVal+1));				//+1 przesuwamy skale o 1 w prawo co by mieæ wartoœci zaczynaj¹ce siê od 0
			int numberOfPointsMax = (int) (maxVal / (Math.pow(10, (1/aMax)) - 1)) + 1;
			float aMin = (float) (window / Math.log10(-minVal+1));				//+1 przesuwamy skale o 1 w prawo co by mieæ wartoœci zaczynaj¹ce siê od 0
			int numberOfPointsMin = (int) (-minVal / (Math.pow(10, (1/aMin)) - 1)) + 1;
			
			logTab = new int[numberOfPoints * 2];
			float b = maxVal/numberOfPoints;
			//TODO sprawdziæ czy przypadkiem nie wpisuje wartoœci poza zakresem bo mi wykrzacza jak siê dojdzie do maksymalnej wartoœci
			for(int i = 0; i <numberOfPointsMax; i++){ 		// dodatnie wartoœci
				logTab[i] = (int) (aMax * Math.log10(i * b + 1));		//+1 przesuwamy skale o 1 w prawo co by mieæ wartoœci zaczynaj¹ce siê od 0
				
			}
			if(maxVal != 0)
				acc_multiplier = (numberOfPoints - 1)/ maxVal;
			
		}*/
	}
	/**
	 * funkcja inicjalizuj¹ca wartoœci w logTab[] oraz obliczaj¹ca wspó³czynnik skalowania 
	 * wartoœci wejœciowej np przyspieszenia
	 * @param minVal	minimalna wartoœæ wejœciowa wartoœci do przeskaowania ze skali 
	 * 					liniowej na logarytmiczn¹
	 * @param maxVal	maksymalana wartoœæ wejœciowa wartoœci do przeskaowania ze skali 
	 * 					liniowej na logarytmiczn¹
	 * @param window	liczba punktów na skali maksymala wartoœæ w logTab[]
	 * @param resolution	minimalny skok pojedynczej wartoœci 
	 */
	public void createLogTable(float minVal, float maxVal, int window, float resolution){
		//TODO: napisaæ to
		createLogTable(maxVal, window, resolution);
	}
	
	/**
	 * zwraca logarytmiczn¹ wartoœæ odczytan¹ z utworzonej wczeœniej tablicy z obliczonymi wartoœciami Log10 w zakresie od 0 do maxVal
	 * @param Val	wartoœæ do przeliczenia na log10 na przyk³ad przyspieszenia
	 * @return	logarytmiczna wartoœæ przeskalowanej wartoœci wejœiowej Val w zakresie od 0 do "raster"
	 */
	@Deprecated
	public int myLogVal (float Val){
		if(numberOfPoints!=0){
			if(Val<0){
				temp = (int) (acc_multiplier*-Val);
				if(temp < numberOfPoints)
					return (logTab[temp]);//(int) (acc_multiplier*-Val)]);
				else
					return (logTab[numberOfPoints - 1]);
				
			}
			else{
				temp = (int) (acc_multiplier*Val) + numberOfPoints;
				if(temp < numberOfPoints * 2)
					return (logTab[temp]);//(int) (acc_multiplier*Val) + numberOfPoints]);
				else
					return (logTab[numberOfPoints * 2 - 1]);
			}
			/*if(Val<0){
				return (logTab[(int) (acc_multiplier*-Val)]);		
			}
			else{
				return (logTab[(int) (acc_multiplier*Val) + numberOfPoints]);
			}*/
		}
		return 0;
	}
	
	/**
	 * zwraca logarytmiczn¹ wartoœæ odczytan¹ z utworzonej wczeœniej tablicy z obliczonymi wartoœciami Log10 w zakresie od 0 do maxVal
	 * @param Val	wartoœæ do przeliczenia na log10 na przyk³ad przyspieszenia
	 * @return	logarytmiczna wartoœæ przeskalowanej wartoœci wejœiowej Val w zakresie od 0 do "raster"
	 */
	public int myLogValNew (float Val){
		return myLogVal(Val);
	}
	
	@Override
	public int myScaledValue(float val) {
		if(minValue == 0)
			return myLogVal(val);
		else
			return myLogValNew(val);
	}

	@Override
	public void creatScaleTable(float minVal, float maxVal, int window) {
		createLogTable(minVal, maxVal, window);
	}
	@Override
	public void creatScaleTable(float minVal, float maxVal, int window,	float resolution) {
		createLogTable(minVal, maxVal, window, resolution);
		
	}
}
