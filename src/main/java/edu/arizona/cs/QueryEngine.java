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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

public class QueryEngine {
	static FSDirectory index;
	static StandardAnalyzer analyzer;
	boolean indexExists=false;

    public QueryEngine() throws IOException{
    	// Retrieve index
    	index = FSDirectory.open(Paths.get("src\\main\\resources\\index"));
    	analyzer = new StandardAnalyzer();
    }
    
    public static void main(String [] args) throws IOException, ParseException {
    	QueryEngine engine = new QueryEngine();
    	System.out.println("Welcome to Jeopardy!");
    	// Add interface
    	engine.runQuestions();
    }
    
    public static String[] runQuery(String category, String query) throws IOException, ParseException {
    	 // 2. query
        String querystr = optimizeQuery(category, query);

        // the "title" arg specifies the default field to use
        // when no field is explicitly specified in the query.
        // Query q = new QueryParser("body", analyzer).parse(querystr);
        // escape method gets rid of lucene special meaning chars
        Query q = new QueryParser("body", analyzer).parse(QueryParser.escape(querystr));
        // 3. search
        int hitsPerPage = 10;
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(q, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;
        String [] results = new String[hits.length];
        // 4. display results
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            results[i] = d.get("title");
            System.out.println((i + 1) + ". " + d.get("title") + " " + hits[i].score);
        }
        // reader can only be closed when there
        // is no need to access the documents any more.
        reader.close();
		return results;
    }

    public void runQuestions() throws ParseException {
    	int correct = 0;
    	int top = 0;
    	int wrong = 0;
    	
        try (BufferedReader reader = new BufferedReader(new FileReader("src\\main\\resources\\questions.txt"))) {
        	String line;
        	String category = "";
        	String question = "";
        	String answer = "";
        	int count = 1;
        	while((line = reader.readLine()) != null) {
        		 if(count % 4 == 1) {
        			 category = line;
        		 } else if(count % 4 == 2) {
        			 question = line;
        		 } else if(count % 4 == 3) {
        			 answer = line;
        		 } else {
        			 count = 0;
        			 System.out.println("Question: " + question);
            		 String[] result = runQuery(category, question);
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
            		 System.out.println("Correct answer: " + answer);
        		 }
        		 count += 1;
        	}
        } catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("Results");
        System.out.println("Correct: " + correct);
        System.out.println("Top 10: " + top);
        System.out.println("Wrong: " + wrong);
    }
    
    public static String optimizeQuery(String category, String query) {
    	String newQuery = "";
    	
    	// Boost quotes and phrases
    	boolean inQuote = false;
    	String[] queryArr = query.split(" ");
    	for(int i = 0;i<queryArr.length;i++) {
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
    	}
    	System.out.println(newQuery);
		return query + " " + category + "";
		//return newQuery + " " + category + "";
    }
}
