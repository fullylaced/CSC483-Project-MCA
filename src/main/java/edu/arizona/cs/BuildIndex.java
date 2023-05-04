package edu.arizona.cs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Properties;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.FSDirectory;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;


/**
 * 
 * This file generates two types of indexes. Changing the version field to 1 will build
 * an index using lemmatized tokens (could take over an hour). The other is a standard
 * lucene index without and lemmatization or stemming.
 * 
 * @authors Merle Crutchfield, Robert Schnell, Avram Parra
 *
 */
public class BuildIndex {
	
	public static File directory = new File("wiki-subset-20140602");
	public static File[] files = directory.listFiles();
	static int version = 0;
	
	
	public static void buildIndex(int v) throws IOException, ParseException {
		System.out.println("Building index...");
		
		// Switch index
        String path = "";
		if (v == 1) {
            version = 1;
			path += "src\\main\\resources\\lemma_index";
        } else if (v == 2) {
        	version = 2;
			path += "src\\main\\resources\\stem_index";
        } else {
			path += "src\\main\\resources\\index";
        }
		
		// Build a static index file
		FSDirectory index = FSDirectory.open(Paths.get(path));
		
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter w = new IndexWriter(index, config);
        
        Map<String, String> map = generateHashMap(w);
        
        w.close();
        
        System.out.println("Index complete!");
	}
	
	public static void main(String [] args) throws IOException, ParseException {
		buildIndex(0); // Change to 1 for lemma index, will take a long time
	}
	
	private static void addDoc(IndexWriter w, String title, String body) throws IOException {
        Document doc = new Document();
        doc.add(new StringField("title", title, Field.Store.YES));

        if(version == 1){
            // Lemmatization builder
            StringBuilder newBody = new StringBuilder();
            
            // Lemmatize
            Properties props = new Properties();
            // set the list of annotators to run
            props.setProperty("annotators", "tokenize, ssplit, pos");
            // build pipeline
            StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
            // create a document object
            CoreDocument document = pipeline.processToCoreDocument(body);
            // display tokens
            for (CoreLabel tok : document.tokens())
                newBody.append(tok.lemma());

            body = newBody.toString();
        } else if(version == 2){ // stem
        	body = body.replaceAll("ed "," ");
            body = body.replaceAll("ing "," ");
            body = body.replaceAll("ly "," ");
            body = body.replaceAll("ment "," ");
            body = body.replaceAll("er "," ");
            body = body.replaceAll("est "," ");
            body = body.replaceAll("ity "," ");
            body = body.replaceAll("ize "," ");
            body = body.replaceAll("ful "," ");
            body = body.replaceAll("less "," ");
            body = body.replaceAll("tion "," ");
        }

        doc.add(new TextField("body", body, Field.Store.YES));
        w.addDocument(doc);
    }
	
	// Prints hashmap for testing and debugging purposes
	public static void printMap(Map<String, String> map) {
		TreeMap<String, String> sorted = new TreeMap<>();
        
        sorted.putAll(map);
        
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
        	System.out.println("Title: " + entry.getKey());
        }
	}
	
	// Writes map to file for testing
	public static void writeToFile(Map<String, String> map) {
		TreeMap<String, String> sorted = new TreeMap<>();
        
        sorted.putAll(map);
        
		try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("debug.txt"));
            for (Map.Entry<String, String> entry : sorted.entrySet()) {
            	writer.write("Title: " + entry.getKey());
            	writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e);
        }
	}
	
	// Generates a hashmap to process data into lucene 
	public static Map<String, String> generateHashMap(IndexWriter w) throws IOException {   
		Map<String, String> map = new HashMap<>();
        // Map<String, ArrayList<String>> redirects = new HashMap<>();
        for (File file : files) {
        	System.out.println("File: " + file.toString());
        	try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                StringBuilder sb = new StringBuilder();
                String title = null;
                while ((line = reader.readLine()) != null) {
                	// Title line
                    if (line.startsWith("[[") && line.endsWith("]]") && !(line.contains("Image:")) && !(line.contains("File:"))) {
                    	// Not first title case
                        if (title != null) {
                        	// Add to hash map
                        	if (!(sb.toString().trim().contains("#REDIRECT"))) {
                        		map.put(title, sb.toString().trim());
                        		
                        		addDoc(w, title, sb.toString().trim());
                        	}
                        	sb.setLength(0); // Clear StringBuilder
                        }
                        // Set new title
                        title = line.substring(2, line.length()-2);
                    } else {
                        sb.append(line).append("\n"); // Add line to StringBuilder
                    }
                }
                if (title != null) {
                    map.put(title, sb.toString().trim()); // Add last document to map
                    addDoc(w, title, sb.toString().trim());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
		
        return map;
	}
}
