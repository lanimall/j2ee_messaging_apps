package com.github.lanimall.messaging.samples.jmsgenerator.utils;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Karthik Lalithraj
 * @author Eric Mizell
 * @author Fabien Sanglier
 * 
 */
public class RandomUtil {
	private Random hdrRndm = new Random(System.currentTimeMillis());

	public RandomUtil(){
		hdrRndm.setSeed(System.currentTimeMillis());
	}

	public String generateRandomUUID(){
		return UUID.randomUUID().toString();
	}

	//convert the int value into a "xxx-xxx-xxxx" string
	public String generateRandomSSN() throws Exception{
		return generateRandomNumericString(3)+"-"+generateRandomNumericString(2)+"-"+generateRandomNumericString(4);
	}

	//convert the int value into a "xxx-xxx-xxxx" string
	public String generateRandomSSN(long number){
		String ssn = String.format("%09d", number);
		return ssn.substring(0, 3) + "-" + ssn.substring(3, 5) + "-" + ssn.substring(5, 9);
	}

	public boolean generateRandomBoolean(){
		return generateRandomBoolean(2);
	}

	public boolean generateRandomBoolean(int ratioTrueFalse){
		return generateRandomInt(ratioTrueFalse) == 0;
	}

	public int generateRandomInt(int length){
		int value = hdrRndm.nextInt(length);
		return value;
	}

	public int generateRandomInt(int minValue, int maxValue, boolean maxInclusive) {
		if(maxValue < minValue)
			throw new IllegalArgumentException("max value should be higher than min value");

		int rdmValue;
		if(maxInclusive)
			rdmValue = hdrRndm.nextInt(maxValue-minValue+1);
		else
			rdmValue = hdrRndm.nextInt(maxValue-minValue);

		return minValue + rdmValue;
	}

	public long generateRandomLong(){
		long value = hdrRndm.nextLong();
		return value;
	}

	public long generateRandomLong(long to) {
		return generateRandomLong(0, to);
	}
	
	public long generateRandomLong(long from, long to) {
		BigDecimal decFrom = new BigDecimal(from);
		BigDecimal decTo = new BigDecimal(to);

		BigDecimal range = decTo.subtract(decFrom);
		BigDecimal factorWithinRange = range.multiply(new BigDecimal(Math.random()));

		return decFrom.add(factorWithinRange).longValue();
	}
	
	public Double generateRandomDouble(){
		Double value = hdrRndm.nextDouble();
		return value;
	}

	public Float generateRandomFloat(){
		Float value = hdrRndm.nextFloat();
		return value;
	}

	public BigDecimal generateRandomDecimal(int Length) {
		if(Length == 0){
			return null;
		}
		String sNums = generateRandomNumericString(Length);
		String sNums2 = generateRandomNumericString(2);
		sNums = sNums + "." + sNums2;
		return new BigDecimal(sNums);
	}

	public String generateRandomAlphaString(int StringLength) {
		if(StringLength == 0){
			return null;
		}
		StringBuffer returnVal = new StringBuffer();
		String[] vals = {"a","b","c","d","e","f","g","h","i","j","k","l","m",
				"n","o","p","q","r","s","t","u","v","w","x","y","z"};
		for(int lp = 0;lp < StringLength; lp++){
			returnVal.append(vals[generateRandomInt(vals.length)]);
		}
		return returnVal.toString();
	}
	public String generateRandomText(int StringLength) {
		if(StringLength == 0){
			return null;
		}
		StringBuffer returnVal = new StringBuffer();
		String[] vals = {"a","b","c","d","e","f","g","h","i","j",
				"k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
		for(int lp = 0;lp < StringLength; lp++){
			returnVal.append(vals[generateRandomInt(vals.length)]);
		}
		return returnVal.toString();
	}
	public String generateRandomNumericString(int StringLength) {
		if(StringLength == 0){
			return null;
		}
		StringBuffer returnVal = new StringBuffer();
		String[] vals = {"0","1","2","3","4","5","6","7","8","9"};
		for(int lp = 0;lp < StringLength; lp++){
			returnVal.append(vals[generateRandomInt(vals.length)]);
		}
		return returnVal.toString();
	}

	public int[] generateRandomIntArray(int arrayLength, int from, int to) {
		int[] array = new int[arrayLength];
		for(int i=0;i<arrayLength; i++){
			array[i] = generateRandomInt(from, to, true);
		}

		return array;
	}
	
	public float[] generateRandomFloatArray(int arrayLength) {
		float[] array = new float[arrayLength];
		for(int i=0;i<arrayLength; i++){
			array[i] = generateRandomFloat();
		}

		return array;
	}
	
	public long[] generateRandomLongArray(int arrayLength, long from, long to) {
		long[] array = new long[arrayLength];
		for(int i=0;i<arrayLength; i++){
			array[i] = generateRandomLong(from, to);
		}

		return array;
	}
	
	public String[] generateRandomStringArray(int arrayLength, int randomStringLength) {
		String[] array = new String[arrayLength];
		for(int i=0;i<arrayLength; i++){
			array[i] = generateRandomText(randomStringLength);
		}

		return array;
	}

	public <T> T getRandomObjectFromList(List<T> objList) {
		if(objList == null || objList.size() == 0){
			return null;
		} else if(objList.size() == 1){
			return objList.get(0);
		}
		int lGetObjIndex = generateRandomInt(objList.size());
		return objList.get(lGetObjIndex);
	}

	public <T> T getRandomObjectFromArray(T[] objArray) {
		if(objArray == null || objArray.length == 0){
			return null;
		}else if(objArray.length == 1){
			return objArray[0];
		}
		int lGetObjIndex = generateRandomInt(objArray.length);
		return objArray[lGetObjIndex];
	}

	/**Returns a string of randomly generate alpha numeric characters. This string
	 * will be the length specified by the input parameter.
	 * @param length the total length of random characters you wish returned
	 * @return <b>string</b> a randomly generated string of alphanumeric characters only
	 */
	public String generateAlphaNumericRandom(int length) {
		String[] mapOfCharacters = getCharacterMap();
		StringBuffer sRandomString = new StringBuffer();

		// Now lets return the number of characters requested
		for (int j = 0; j < length; j++) {
			int rndm = hdrRndm.nextInt(61) + 0;
			String sItem = mapOfCharacters[rndm];
			sRandomString.append(sItem);
		}
		return sRandomString.toString();
	}

	public Date generateRandomDate() {
		return generateRandomDateBetween(generateRandomDate(1900), Calendar.getInstance().getTime());
	}

	public Date generateRandomDate(int year) {
		return generateRandomDate(year, null, true);
	}

	public Date generateRandomDate(int year, Date cutOff, boolean afterCutOff) {
		Calendar cal = Calendar.getInstance();
		cal.set(year, 0, 1);

		Calendar cal2 = Calendar.getInstance();
		cal2.set(year, 11, 31);

		if(cutOff != null){
			if(afterCutOff){
				return generateRandomDateBetween(cutOff, cal2.getTime());
			}
			else{
				return generateRandomDateBetween(cal.getTime(), cutOff);
			}
		}

		return generateRandomDateBetween(cal.getTime(), cal2.getTime());
	}

	public Date generateRandomDateBetween(Date from, Date to) {
		Calendar calFrom = Calendar.getInstance();
		calFrom.setTime(from);
		
		Calendar calTo = Calendar.getInstance();
		calTo.setTime(to);
		
		return new Date(generateRandomLong(calFrom.getTimeInMillis(), calTo.getTimeInMillis()));
	}
	
	private String[] getCharacterMap() {
		String[] universeValues = new String[62];
		int asciiAlpha = 65; // The start of the alpha ascii character set

		// Add the numbers
		for (int i = 0; i < 62; i++) {
			if (i < 10) {
				// numbers zero through 9
				universeValues[i] = new Integer(i).toString();
			} else {
				universeValues[i] = Character.toString((char) asciiAlpha);
				// 91 - 96 are not alpha characters in the ascii map
				if (asciiAlpha + 1 == 91) {
					asciiAlpha = 97;
				} else {
					asciiAlpha = asciiAlpha + 1;
				}
			}
		}
		return universeValues;
	}
}
