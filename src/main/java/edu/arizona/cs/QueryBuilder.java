package edu.arizona.cs;

import java.util.ArrayList;

public class QueryBuilder {
	ArrayList<String> keywords = new ArrayList<>();
	
	public static String buildQuery(String category, String query) {
		String combined = query + " " + category + "";
		combined = combined.replaceAll("\\r\\n", "").replaceAll(";", "").replaceAll(":", "").replaceAll(",", "");
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
    	System.out.println(newQuery.toLowerCase());
		return newQuery.toLowerCase();
		//return newQuery + " " + category + "";
	}
	
	public ArrayList<String> createKeywordList() {
		
		return keywords;
	}
	
}
