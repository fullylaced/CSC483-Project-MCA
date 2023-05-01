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

/**
 * 
 * This file is the main interface and selection menu for the Jeopardy game. This
 * allows you to switch between a different scoring modes and indexes.
 * 
 * @authors Merle Crutchfield, Robert Schnell, Avram Parra
 *
 */
public class QueryEngine {
	static FSDirectory index;
	static StandardAnalyzer analyzer;
	String mode = "bm25";
	String index_mode = "reg";

	// Constructor for query engine
    public QueryEngine() throws IOException, ParseException {
    	// Retrieve index
		if(index_mode.equals("reg")){
			try { // Standard Index
				index = FSDirectory.open(Paths.get("src\\main\\resources\\index"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try { // Lemma Index
				index = FSDirectory.open(Paths.get("src\\main\\resources\\lemma_index"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    	analyzer = new StandardAnalyzer();
    }
    
    // Constructor for query engine that takes a mode as a parameter
	public QueryEngine(String idx) throws IOException, ParseException {
		index_mode = idx;
    	// Retrieve index
		if(index_mode.equals("reg")){
			try { // Standard Index
				index = FSDirectory.open(Paths.get("src\\main\\resources\\index"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try { // Lemma Index
				index = FSDirectory.open(Paths.get("src\\main\\resources\\lemma_index"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
    	analyzer = new StandardAnalyzer();
    }
    
	// Main for testing
    public static void main(String [] args) throws IOException, ParseException {
    	QueryEngine engine = new QueryEngine();
    	System.out.println("Welcome to Jeopardy!");
    	engine.runQuestions();
    }
    
    // Runs query and prints top 10 results
    public String[] runQuery(String category, String query) throws IOException, ParseException {
    	int hitsPerPage = 10; // Change for less hits
    	
    	String queryStr = query + " " + category;
        //String queryStr = QueryBuilder.buildQuery(category, query);
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
    
    // parses all 100 test questions and prints a total score
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
    
}
