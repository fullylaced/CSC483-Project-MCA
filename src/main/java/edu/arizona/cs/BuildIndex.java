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
import java.util.Properties;
import java.util.TreeMap;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.ByteBuffersDirectory;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;

public class BuildIndex {
	public static File directory = new File("wiki-subset-20140602");
	public static File[] files = directory.listFiles();
	
	public static void buildIndex(int version) throws IOException, ParseException {
		System.out.println("Building index...");
		String path = "";
		if (version == 1)
			path += "src\\main\\resources\\index2";
		else
			path += "src\\main\\resources\\index";
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
		buildIndex(1);
	}
	
	private static void addDoc(IndexWriter w, String title, String body) throws IOException {
        Document doc = new Document();
        doc.add(new StringField("title", title, Field.Store.YES));
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
        
        doc.add(new TextField("body", newBody.toString(), Field.Store.YES));
        w.addDocument(doc);
    }
	
	public static void printMap(Map<String, String> map) {
		TreeMap<String, String> sorted = new TreeMap<>();
        
        sorted.putAll(map);
        
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
        	System.out.println("Title: " + entry.getKey());
        }
	}
	
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
