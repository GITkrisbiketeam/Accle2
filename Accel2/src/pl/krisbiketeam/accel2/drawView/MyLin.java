package pl.krisbiketeam.accel2.drawView;

public class MyLin extends MyScale{

	//liczba punktów skali liniowej tylko jednego zakresu dodatniego albo ujemnego
	int numberOfPoints;
	// wspó³czynnik skalowania wymagany do zwrócenia odpowiedniej wartoœci 
	float acc_multiplier;
	//tablica przechowuj¹ca przeskalowane wartoœci odpowiadaj¹ce iloœci punktów skali (wys wykresu)
	int linTab[];
	
	float minValue, maxValue;
	
	
	MyLin(){
		
	}
	MyLin(float minVal, float maxVal, int window){
		createLinTable(minVal, maxVal, window);
	}
	MyLin(float minVal, float maxVal, int window, float resolution){
		createLinTable(minVal, maxVal, window, resolution);
	}
	
	/**
	 * funkcja inicjalizuj¹ca wartoœci w linTab[] oraz obliczaj¹ca wspó³czynnik skalowania 
	 * wartoœci wejœciowej np przyspieszenia
	 * @param minVal	minimalna wartoœæ wejœciowa wartoœci do przeskaowania
	 * @param maxVal	maksymalana wartoœæ wejœciowa wartoœci do przeskaowania
	 * @param window	liczba punktów na skali maksymala wartoœæ w logTab[]
	 */
	public void createLinTable(float minVal, float maxVal, int window){
		if(maxVal != minVal && minVal < maxVal && window >1){
				
			if(window>1)
				numberOfPoints = window-1;
			else
				numberOfPoints = 0;
		 
			linTab = new int[window];
			for(int i = 0; i <window; i++){   
				linTab[i] =numberOfPoints - i; 
			}
			
			acc_multiplier = (window-1)/ (maxVal - minVal);
			minValue = minVal;
			maxValue = maxVal;
		}
	}
	/**
	 * funkcja inicjalizuj¹ca wartoœci w linTab[] oraz obliczaj¹ca wspó³czynnik skalowania 
	 * wartoœci wejœciowej np przyspieszenia
	 * @param minVal	minimalna wartoœæ wejœciowa wartoœci do przeskaowania
	 * @param maxVal	maksymalana wartoœæ wejœciowa wartoœci do przeskaowania
	 * @param window	liczba punktów na skali maksymala wartoœæ w linTab[]
	 * @param resolution	minimalny skok pojedynczej wartoœci 
	 */
	public void createLinTable(float minVal, float maxVal, int window, float resolution){
		//TODO:  napisaæ to
		createLinTable(minVal, maxVal, window);
	}
	
	/**
	 * zwraca liniow¹ wartoœæ odczytan¹ z utworzonej wczeœniej tablicy z obliczonymi wartoœciami od minVal do maxVal
	 * @param Val	wartoœæ do przeliczenia na liniow¹ skalê okreœlon¹ przez "window"
	 * @return	liniowa wartoœæ przeskalowanej wartoœci wejœiowej Val w zakresie od 0 do "window"
	 */
	public int myLinValNew (float Val){
		if(numberOfPoints != 0){
			if(Val<minValue)
				return (numberOfPoints);
			else if(Val >maxValue)
				return (0);
			else{
		        Val -= minValue;
		        return(linTab[(int) (acc_multiplier*Val)]);
		        //return(numberOfPoints - (int) (acc_multiplier * Val));
			}
		}
		return 0;
	}
	
	/**
	 * zwraca przeskalowan¹ wartoœæ odczytan¹ z utworzonej wczeœniej tablicy z obliczonymi wartoœciami od minVal do maxVal
	 * @param Val	wartoœæ do przeliczenia na przeskalowan¹ skalê okreœlon¹ przez "window"
	 * @return	przeskalowana wartoœæ przeskalowanej wartoœci wejœiowej Val w zakresie od 0 do "window"
	 */
	@Override
	public int myScaledValue(float val) {
		//if(minValue == 0)
			//return myLinVal(val);
		//else
			return myLinValNew(val);
	}
	@Override
	public void creatScaleTable(float minVal, float maxVal, int window) {
		createLinTable(minVal, maxVal, window);
	}
	@Override
	public void creatScaleTable(float minVal, float maxVal, int window,	float resolution) {
		createLinTable(minVal, maxVal, window, resolution);
	}
	

}
