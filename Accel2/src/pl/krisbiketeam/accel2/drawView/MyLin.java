package pl.krisbiketeam.accel2.drawView;

public class MyLin extends MyScale{

	//liczba punkt�w skali liniowej tylko jednego zakresu dodatniego albo ujemnego
	int numberOfPoints;
	// wsp�czynnik skalowania wymagany do zwr�cenia odpowiedniej warto�ci 
	float acc_multiplier;
	//tablica przechowuj�ca przeskalowane warto�ci odpowiadaj�ce ilo�ci punkt�w skali (wys wykresu)
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
	 * funkcja inicjalizuj�ca warto�ci w linTab[] oraz obliczaj�ca wsp�czynnik skalowania 
	 * warto�ci wej�ciowej np przyspieszenia
	 * @param minVal	minimalna warto�� wej�ciowa warto�ci do przeskaowania
	 * @param maxVal	maksymalana warto�� wej�ciowa warto�ci do przeskaowania
	 * @param window	liczba punkt�w na skali maksymala warto�� w logTab[]
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
	 * funkcja inicjalizuj�ca warto�ci w linTab[] oraz obliczaj�ca wsp�czynnik skalowania 
	 * warto�ci wej�ciowej np przyspieszenia
	 * @param minVal	minimalna warto�� wej�ciowa warto�ci do przeskaowania
	 * @param maxVal	maksymalana warto�� wej�ciowa warto�ci do przeskaowania
	 * @param window	liczba punkt�w na skali maksymala warto�� w linTab[]
	 * @param resolution	minimalny skok pojedynczej warto�ci 
	 */
	public void createLinTable(float minVal, float maxVal, int window, float resolution){
		//TODO:  napisa� to
		createLinTable(minVal, maxVal, window);
	}
	
	/**
	 * zwraca liniow� warto�� odczytan� z utworzonej wcze�niej tablicy z obliczonymi warto�ciami od minVal do maxVal
	 * @param Val	warto�� do przeliczenia na liniow� skal� okre�lon� przez "window"
	 * @return	liniowa warto�� przeskalowanej warto�ci wej�iowej Val w zakresie od 0 do "window"
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
	 * zwraca przeskalowan� warto�� odczytan� z utworzonej wcze�niej tablicy z obliczonymi warto�ciami od minVal do maxVal
	 * @param Val	warto�� do przeliczenia na przeskalowan� skal� okre�lon� przez "window"
	 * @return	przeskalowana warto�� przeskalowanej warto�ci wej�iowej Val w zakresie od 0 do "window"
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
