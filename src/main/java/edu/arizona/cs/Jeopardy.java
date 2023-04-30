package edu.arizona.cs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import org.apache.lucene.queryparser.classic.ParseException;

public class Jeopardy {
	public static void main(String [] args) throws ParseException, IOException {
		boolean indexExist = checkForIndex();
		System.out.println("Welcome to Jeopardy!");
		QueryEngine engine = new QueryEngine();
		while(true) {
			// Check if index exists
			if(indexExist) {
				
				System.out.println("Plese select an option");
				System.out.println("1 - Run 100 questions");
				System.out.println("2 - Enter a custom query");
				System.out.println("3 - Change mode");
				System.out.println("9 - Exit Jeopardy");
				
				Scanner selection = new Scanner(System.in);
				switch(selection.nextInt()){
				
					case 1:
						try {
							engine.runQuestions();
						} catch (ParseException e) {
							System.out.println("There was an error running a query");
							e.printStackTrace();
						}
						break;
						
					case 2:
						String category = "";
						String custQuery = "";
						System.out.println("Enter a category");
						Scanner input = new Scanner(System.in);
						category = input.nextLine();
						System.out.println("Enter a question");
						input = new Scanner(System.in);
						custQuery = input.nextLine();
						try {
							engine.runQuery(category, custQuery);
						} catch (IOException | ParseException e) {
							System.out.println("There was an error running a query");
							e.printStackTrace();
						}
						break;
						
					case 3:
						System.out.println("What mode would you like to use?");
						System.out.println("1 - BM25 Similarity");
						System.out.println("2 - Cosine Similarity");
						Scanner mode = new Scanner(System.in);
						if(mode.nextInt() == 2) {
							engine.mode = "cosine";
							System.out.println("Mode changed to Cosine Similarity");
						} else {
							engine.mode = "bm25";
							System.out.println("Mode changed to BM25 Similarity");
						}
						break;
						
					case 9:
						System.out.println("Thank  you for playing!");
						break;
				}
				// Build Index if it does not already exist
			} else { 
				System.out.println("Index does not exist");
				try {
					BuildIndex.buildIndex();
					indexExist = true;
				} catch (IOException | ParseException e) {
					System.out.println("There was an error building index");
					e.printStackTrace();
				}
			}
		}
	}
	
	public static boolean checkForIndex() {
		Path index = Paths.get("src\\main\\resources\\index");
		return Files.exists(index);
	}

}
