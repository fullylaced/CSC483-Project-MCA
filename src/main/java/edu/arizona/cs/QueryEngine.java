package edu.arizona.cs;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class QueryEngine {
	static FSDirectory index;
	static StandardAnalyzer analyzer;
	String mode = "bm25";

    public QueryEngine(int version) throws IOException, ParseException {
    	// Retrieve index
    	try {
    		String path = "";
    		if (version == 1) {
    			System.out.println("here");
    			path += "src\\main\\resources\\index2";
    		}
    		else
    			path += "src\\main\\resources\\index";
    		// Build a static index file
			index = FSDirectory.open(Paths.get(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
    	analyzer = new StandardAnalyzer();
    }
    
    public static void main(String [] args) throws IOException, ParseException {
    	QueryEngine engine = new QueryEngine(1);
    	System.out.println("Welcome to Jeopardy!");
    	engine.runQuestions();
    }
    
    public String[] runQuery(String category, String query) throws IOException, ParseException {
    	int hitsPerPage = 10; 
    	
        String queryStr = QueryBuilder.buildQuery(category, query);
        Query q = new QueryParser("body", analyzer).parse(QueryParser.escape(queryStr));

        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        
        // Change mode
        if(mode.equals("cosine")) {
        	searcher.setSimilarity(new ClassicSimilarity());
        } else {
        	searcher.setSimilarity(new BM25Similarity());
        }
        
        TopDocs docs = searcher.search(q, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;
        String [] results = new String[hits.length];
        
        // Display Results
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            results[i] = d.get("title");
            System.out.println((i + 1) + ". " + d.get("title") + " " + hits[i].score);
        }
        
        reader.close();
		return results;
    }

    public void runQuestions() throws ParseException {
    	// Total scores
    	int correct = 0;
    	int top = 0;
    	int wrong = 0;
    	
    	// Parse questions file
        try (BufferedReader reader = new BufferedReader(new FileReader("src\\main\\resources\\questions.txt"))) {
        	String line;
        	String category = "";
        	String question = "";
        	String answer = "";
        	
        	int count = 1;
        	while((line = reader.readLine()) != null) {
        		 if(count % 4 == 1) { // Category line
        			 category = line;
        		 } else if(count % 4 == 2) { // Question line
        			 question = line;
        		 } else if(count % 4 == 3) { // Answer line
        			 answer = line;
        		 } else { // White space between questions
        			 count = 0;
        			 System.out.println("Question: " + question);
        			 // Run question
            		 String[] result = runQuery(category, question);
            		 
            		 // Counter to calculate score
            		 for(int i=0;i<result.length;i++) {
            			 if(i == result.length-1) {
            				 wrong += 1;
            			 } else if(result[i].equals(answer) && i == 0) {
            				 correct += 1;
            				 break;
            			 } else if(result[i].equals(answer)) {
            				 top += 1;
            				 break;
            			 } 
            		 }
            		 
            		 System.out.println("Correct answer: " + answer + "\n");
        		 }
        		 count += 1;
        	}
        } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        System.out.println("\nResults");
        System.out.println("Correct: " + correct);
        System.out.println("Top 10: " + top);
        System.out.println("Wrong: " + wrong);
    }
    
    // Shit does not work well
    public static String optimizeQuery(String category, String query) {
    	String newQuery = "";
    	String[] keywords = {"give", "museum", "from", "city", "capital", "state", "ucla", "alex", "name", "historical"};
    	// Boost quotes and phrases
    	boolean inQuote = false;
    	String[] queryArr = query.split(" ");
    	for(int i = 0;i<queryArr.length;i++) {
    		/**
    		// Boost phrases
    		// Check if in quote
    		if(queryArr[i].charAt(0) == '\"') {
    			inQuote = true;
    			queryArr[i] = "+" + queryArr[i];
    		}
    		if(inQuote && queryArr[i].charAt(queryArr[i].length()-1) == '\"') {
    			queryArr[i] += "";
    		} else if(inQuote) {
    			break;
    		}
    		// Boost large words
    		if(queryArr[i].length() >= 12) {
    			queryArr[i] = queryArr[i]+"^2";
    		}
    		// Reduce small words
    		if(queryArr[i].length() <= 3) {
    			queryArr[i] = queryArr[i]+"^0";
    		}
    		newQuery += queryArr[i] + " ";
    		**/
    	}
    	System.out.println(newQuery);
		query += " " + category;
		return query.replaceAll("\\r\\n", "");
		//return newQuery + " " + category + "";
    }
}
