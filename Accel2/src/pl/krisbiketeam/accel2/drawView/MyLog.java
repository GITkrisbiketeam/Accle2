package pl.krisbiketeam.accel2.drawView;

/**
 * klasa przechowuj�ca przeliczone warto�ci potrzebne do przeskalowania liniowej skali na skal� 
 * logarytmiczn�, zakres jest podawany w trakcie tworzenia tej klasy od 0 do maxVal oraz w 
 * zale�no�ci od ilo�ci potrzebnych punkt�w do wykresu podanych jako raster czyli 
 * liczba punkt�w skali (wysoko�� wykresu)
 * @author Krzy�
 *
 */
public class MyLog extends MyScale{
	//liczba punkt�w skali logarytmicznej tylko jednego zakresu dodatniego albo ujemnego
	int numberOfPoints;
	// wsp�czynnik skalowania wymagany do zwr�cenia odpowiedniej warto�ci 
	float acc_multiplier;
	//tablica przechowuj�ca przeskalowane warto�ci odpowiadaj�ce ilo�ci punkt�w skali (wys wykresu)
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
	 * funkcja inicjalizuj�ca warto�ci w logTab[] oraz obliczaj�ca wsp�czynnik skalowania 
	 * warto�ci wej�ciowej np przyspieszenia
	 * @param maxVal	maksymalana warto�� wej�ciowa warto�ci do przeskaowania ze skali 
	 * 					liniowej na logarytmiczn�
	 * @param window	liczba punkt�w na skali maksymala warto�� w logTab[]
	 */
	@Deprecated
	public void createLogTable(float maxVal, int window){
		window = window / 2;									// bo mamy warto�ci dodatnie i ujemne ale liczymy tylko raz dla dodatnich
		float a = (float) (window / Math.log10(maxVal+1));				//+1 przesuwamy skale o 1 w prawo co by mie� warto�ci zaczynaj�ce si� od 0
		numberOfPoints = (int) (maxVal / (Math.pow(10, (1/a)) - 1)) + 1;
		logTab = new int[numberOfPoints * 2];
		float b = maxVal/numberOfPoints;
		//TODO sprawdzi� czy przypadkiem nie wpisuje warto�ci poza zakresem bo mi wykrzacza jak si� dojdzie do maksymalnej warto�ci
		for(int i = 0; i <numberOfPoints; i++){ 		// dodatnie warto�ci
			logTab[i + numberOfPoints] = (int) (window - a * Math.log10(i * b + 1));		//+1 przesuwamy skale o 1 w prawo co by mie� warto�ci zaczynaj�ce si� od 0
			logTab[i] = 2*window - logTab[i + numberOfPoints];
		}
		if(maxVal != 0)
			acc_multiplier = (numberOfPoints - 1)/ maxVal;
	}
	/**
	 * funkcja inicjalizuj�ca warto�ci w logTab[] oraz obliczaj�ca wsp�czynnik skalowania 
	 * warto�ci wej�ciowej np przyspieszenia
	 * @param maxVal	maksymalana warto�� wej�ciowa warto�ci do przeskaowania ze skali 
	 * 					liniowej na logarytmiczn�
	 * @param window	liczba punkt�w na skali maksymala warto�� w logTab[]
	 * @param resolution	minimalny skok pojedynczej warto�ci 
	 */
	@Deprecated
	public void createLogTable(float maxVal, int window, float resolution){
		numberOfPoints = (int) (maxVal / resolution) + 1;
		window = window / 2;									// bo mamy warto�ci dodatnie i ujemne ale liczymy tylko raz dla dodatnich
		float a = (float) (window / Math.log10(maxVal+1));				//+1 przesuwamy skale o 1 w prawo co by mie� warto�ci zaczynaj�ce si� od 0
		logTab = new int[numberOfPoints * 2];
		float b = resolution; //maxVal/numberOfPoints;
		//TODO sprawdzi� czy przypadkiem nie wpisuje warto�ci poza zakresem bo mi wykrzacza jak si� dojdzie do maksymalnej warto�ci
		for(int i = 0; i <numberOfPoints; i++){ 		// dodatnie warto�ci
			logTab[i + numberOfPoints] = (int) (window - a * Math.log10(i * b + 1));		//+1 przesuwamy skale o 1 w prawo co by mie� warto�ci zaczynaj�ce si� od 0
			logTab[i] = 2*window - logTab[i + numberOfPoints];
		}
		acc_multiplier = (numberOfPoints - 1)/ maxVal;
	}
	
	/**
	 * funkcja inicjalizuj�ca warto�ci w logTab[] oraz obliczaj�ca wsp�czynnik skalowania 
	 * warto�ci wej�ciowej np przyspieszenia
	 * @param minVal	minimalna warto�� wej�ciowa warto�ci do przeskaowania ze skali 
	 * 					liniowej na logarytmiczn�
	 * @param maxVal	maksymalana warto�� wej�ciowa warto�ci do przeskaowania ze skali 
	 * 					liniowej na logarytmiczn�
	 * @param window	liczba punkt�w na skali maksymala warto�� w logTab[]
	 */
	public void createLogTable(float minVal, float maxVal, int window){
		createLogTable(maxVal, window);
		/*if(maxVal != minVal && minVal < maxVal && window >1){
				
				
			
			int maxWindow = (int)(maxVal / (maxVal - minVal) * window);
			int minWindow = (int)(minVal / (maxVal - minVal) * window);
			
			float aMax = (float) (window / Math.log10(maxVal+1));				//+1 przesuwamy skale o 1 w prawo co by mie� warto�ci zaczynaj�ce si� od 0
			int numberOfPointsMax = (int) (maxVal / (Math.pow(10, (1/aMax)) - 1)) + 1;
			float aMin = (float) (window / Math.log10(-minVal+1));				//+1 przesuwamy skale o 1 w prawo co by mie� warto�ci zaczynaj�ce si� od 0
			int numberOfPointsMin = (int) (-minVal / (Math.pow(10, (1/aMin)) - 1)) + 1;
			
			logTab = new int[numberOfPoints * 2];
			float b = maxVal/numberOfPoints;
			//TODO sprawdzi� czy przypadkiem nie wpisuje warto�ci poza zakresem bo mi wykrzacza jak si� dojdzie do maksymalnej warto�ci
			for(int i = 0; i <numberOfPointsMax; i++){ 		// dodatnie warto�ci
				logTab[i] = (int) (aMax * Math.log10(i * b + 1));		//+1 przesuwamy skale o 1 w prawo co by mie� warto�ci zaczynaj�ce si� od 0
				
			}
			if(maxVal != 0)
				acc_multiplier = (numberOfPoints - 1)/ maxVal;
			
		}*/
	}
	/**
	 * funkcja inicjalizuj�ca warto�ci w logTab[] oraz obliczaj�ca wsp�czynnik skalowania 
	 * warto�ci wej�ciowej np przyspieszenia
	 * @param minVal	minimalna warto�� wej�ciowa warto�ci do przeskaowania ze skali 
	 * 					liniowej na logarytmiczn�
	 * @param maxVal	maksymalana warto�� wej�ciowa warto�ci do przeskaowania ze skali 
	 * 					liniowej na logarytmiczn�
	 * @param window	liczba punkt�w na skali maksymala warto�� w logTab[]
	 * @param resolution	minimalny skok pojedynczej warto�ci 
	 */
	public void createLogTable(float minVal, float maxVal, int window, float resolution){
		//TODO: napisa� to
		createLogTable(maxVal, window, resolution);
	}
	
	/**
	 * zwraca logarytmiczn� warto�� odczytan� z utworzonej wcze�niej tablicy z obliczonymi warto�ciami Log10 w zakresie od 0 do maxVal
	 * @param Val	warto�� do przeliczenia na log10 na przyk�ad przyspieszenia
	 * @return	logarytmiczna warto�� przeskalowanej warto�ci wej�iowej Val w zakresie od 0 do "raster"
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
	 * zwraca logarytmiczn� warto�� odczytan� z utworzonej wcze�niej tablicy z obliczonymi warto�ciami Log10 w zakresie od 0 do maxVal
	 * @param Val	warto�� do przeliczenia na log10 na przyk�ad przyspieszenia
	 * @return	logarytmiczna warto�� przeskalowanej warto�ci wej�iowej Val w zakresie od 0 do "raster"
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
