package edu.arizona.cs;

/**
 * 
 * This file constructs a query by boosting certain terms. 
 * 
 * Documents must include:
 * Quotes
 * 
 * Boosted Terms:
 * Numbers
 * Dates
 * Names (people, cities, etc)
 * 
 * @authors Merle Crutchfield, Robert Schnell, Avram Parra
 *
 */
public class QueryBuilder {
	
	public static String buildQuery(String category, String query) {
		String combined = query + " " + category.toLowerCase() + "";
		combined = combined.replaceAll("\\r\\n", "").replaceAll(";", "").replaceAll(":", "").replaceAll(",", "").replaceAll("'", "");
	
		String newQuery = "";
		
    	// Boost quotes and phrases
    	boolean inQuote = false;
    	String[] queryArr = combined.split(" ");
    	for(int i = 0;i<queryArr.length;i++) {
    		// Boost phrases
    		// Check if in quote
    		if(queryArr[i].charAt(0) == '\"') {
    			inQuote = true;
    			queryArr[i] = "+" + queryArr[i];
    		}
    		else if(inQuote && queryArr[i].charAt(queryArr[i].length()-1) == '\"') {
    			queryArr[i] += "";
    		}
    		// Check for numbers
    		else if(Character.isDigit(queryArr[i].charAt(0))) {
    			queryArr[i] = "+" + queryArr[i];
    		}
    		// Boost Capitalized words
    		else if(Character.isUpperCase(queryArr[i].charAt(0)) && !inQuote && i != 0) {
    			queryArr[i] = queryArr[i] + "^2";
    		}
    		// Boost large words
    		else if(queryArr[i].length() >= 8 && !inQuote) {
    			queryArr[i] = queryArr[i] + "^1.5";
    		}
    		newQuery += queryArr[i] + " ";
    	}
    	//System.out.println(newQuery);
		return newQuery;
		//return newQuery + " " + category + "";
	}
	
}
