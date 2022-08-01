import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class booleanAND { 
	

    public static ArrayList<String> tokenize(String text){
    	
    	text = text.toLowerCase();
    	ArrayList<String> tokens = new ArrayList<String>();
    	int start = 0;
    	int i;
    	String token;
    	
    	for(i = 0; i < text.length(); i++) {
    		char c = text.charAt(i);
    		    		
    		if (!Character.isDigit(c) && !Character.isLetter(c)){
    			
    			if ( start != i) {
    				
    				
    				token = text.substring(start,i);
    				tokens.add(token);
    				
    			}
    			
    			start = i + 1;

    			
    		}
    		
    	}
    	
    	if(start != i) {
    		
    		tokens.add(text.substring(start,i));
    	}
    	
    	return tokens;
    }
    
    public static ArrayList<Integer> getQueryTermID(ArrayList<String> tokens, HashMap <String,Integer> lexicon){
    	
    	ArrayList<Integer> queryTermIDs = new ArrayList<Integer>();
    	
    	for(String token: tokens) {
    		
    		if(lexicon.containsKey(token)) {
    			int termID = lexicon.get(token);
    			queryTermIDs.add(termID);
    			
    		}
    	}
    	
    	return queryTermIDs;
    }
    
    public static ArrayList<Integer> initialIntersect(int p1, int p2, ArrayList<Integer>[] postingsList){
    	
    	ArrayList<Integer> intersection = new ArrayList<Integer>();
    	int i = 0;
    	int j = 0;
    	 	
    	
    	while(i != postingsList[p1].size() && j != postingsList[p2].size()) {
    		
    		int iDoc = postingsList[p1].get(i);
    		int jDoc = postingsList[p2].get(j);

    		if(iDoc == jDoc) {
    			intersection.add(iDoc);
    			i = i + 2;
    			j = j + 2;

    			
    		} else if(iDoc < jDoc){
    			i = i + 2;
    			
    		} else {
    			j = j + 2;
    		}
    		
    	}
    	
    	return intersection;
    }
    
    public static ArrayList<Integer> intersect(ArrayList<Integer> intersect, int p2, ArrayList<Integer>[] postingsList){
    	
    	ArrayList<Integer> intersection = new ArrayList<Integer>();
    	int i = 0;
    	int j = 0;
    	
    	while(i != intersect.size() && j != postingsList[p2].size()) {
    		
    		int iDoc = intersect.get(i);
    		int jDoc = postingsList[p2].get(j);

    		
    		if(iDoc == jDoc) {
    			intersection.add(iDoc);
    			j = j + 2;
    			i++;
    		} else if(iDoc < jDoc){
    			i++;
    			
    		} else {
    			j = j + 2;
    		}
    		
    	}
    	
    	return intersection;
    }
    
    public static ArrayList<Integer> oneWordQuery(int p1, ArrayList<Integer>[] postingsList){
    	
    	ArrayList<Integer> results = new ArrayList<Integer>();
    	int i = 0;
    	
    	while(i != postingsList[p1].size()) {
    		
    		results.add(postingsList[p1].get(i));
    		i = i + 2;
    	}
    	return results;
    }
    
    public static ArrayList<Integer> runQuery(ArrayList<Integer>[] postingsList, ArrayList<Integer> tokenIDs){
    	
    	ArrayList<Integer> queryResult = new ArrayList<Integer>();
    	int numOfTokens = tokenIDs.size();
    	
    	int p1 = tokenIDs.get(0);
    	
    	if(numOfTokens == 1 ) {
    		queryResult = oneWordQuery(p1,postingsList);
    		return queryResult;
    	}
    	
    	int p2 = tokenIDs.get(1);
    	
    	queryResult = initialIntersect(p1,p2,postingsList);
    	
    	int size = tokenIDs.size();
    	int i = 2;
    	
    	if(size > 2) {
    		while(i < tokenIDs.size()) {
    			queryResult = intersect(queryResult,tokenIDs.get(i),postingsList);
    			i++;
    		}
    	}
    	
    	return queryResult;
    }
    
    public static void runProgram(ArrayList<Integer>[] postingsList, String indexLocation, String queryLocation, String resultsName, HashMap <String,Integer> lexicon, HashMap<Integer, String> idMapping) throws IOException {
    	
    	FileInputStream queryFile = new FileInputStream(queryLocation);
    	BufferedReader br = new BufferedReader (new InputStreamReader(queryFile));
    	
    	Writer output = null;
		File filePath = new File(indexLocation + "/" + resultsName);
		FileOutputStream file = new FileOutputStream(filePath);
		output = new BufferedWriter(new OutputStreamWriter(file));
    	
	String topicNum;
    	String queryInput;
    	int topicID = 0;
    	String q0 = "q0";
    	int rank = 1;
    	String runTag = "nspasikAND";
    	
    	while ((queryInput = br.readLine()) != null) {
		
		topicNum = queryInput.substring(0,3);
		queryInput = queryInput.substring(4);
		
    		ArrayList<String> tokens = tokenize(queryInput);
    		ArrayList<Integer> tokenIDs = getQueryTermID(tokens,lexicon);
    		
    		ArrayList<Integer> queryResults = runQuery(postingsList,tokenIDs);
    		Map<Integer,Integer> resultsMap = new HashMap<Integer,Integer>();
    		
    		if(queryResults.isEmpty()) {
    			topicID++;
    			continue;
    		}
    		
    		for(int document: queryResults) {
    			int occurance = 0;
    			for(int token: tokenIDs) {
    				int docIndex = postingsList[token].indexOf(document);
    				occurance = occurance + postingsList[token].get(docIndex+1);
    			}
    			resultsMap.put(document,occurance);
    		}
    		
    		
    		LinkedHashMap<Integer, Integer> sortedMap = 
    			    resultsMap.entrySet()
    			       .stream()             
    			       .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
    			       .collect(Collectors.toMap(e -> e.getKey(), 
    			                                 e -> e.getValue(), 
    			                                 (e1, e2) -> null, // or throw an exception
    			                                 () -> new LinkedHashMap<Integer, Integer>()));
    		
    		
    		
    		for(int doc: sortedMap.keySet()) {
    			String docNo = idMapping.get(doc);
    			int score = sortedMap.get(doc);
    			String outputString = topicNum + " " + q0 + " " + docNo + " " + rank + " " + score + " " + runTag;
    			output.write(outputString);
    			((BufferedWriter) output).newLine();
    			rank++;
    		}
    		
    		
    		rank = 1;
    		topicID++;
    		tokens.clear();
    		tokenIDs.clear();
    		queryResults.clear();
    		resultsMap.clear();
    		sortedMap.clear();
    		continue;
    		
    	}
    	
    	output.close();
    	br.close();
    }
    
	


	public static void main(String[] args) throws IOException, ClassNotFoundException {
		

		
		FileInputStream fis = new FileInputStream("lexicon.txt");
		ObjectInputStream ois = new ObjectInputStream(fis);
		@SuppressWarnings("unchecked")
		HashMap<String, Integer> lexicon = (HashMap<String, Integer>) ois.readObject();
		ois.close();
		
		FileInputStream fis1 = new FileInputStream("inverseIndex.txt");
		ObjectInputStream ois1 = new ObjectInputStream(fis1);
		@SuppressWarnings("unchecked")
		HashMap<Integer, String> inverseIndex = (HashMap<Integer, String>) ois1.readObject();
		ois1.close();
		
		FileInputStream fis2 = new FileInputStream("postingsList.txt");
		ObjectInputStream ois2 = new ObjectInputStream(fis2);
		@SuppressWarnings("unchecked")
		ArrayList<Integer>[] postingsList  = (ArrayList<Integer>[]) ois2.readObject();
		ois2.close();
		
		FileInputStream fis3 = new FileInputStream("idMapping.txt");
		ObjectInputStream ois3 = new ObjectInputStream(fis3);
		@SuppressWarnings("unchecked")
		HashMap<Integer, String> idMapping  = (HashMap<Integer, String>) ois3.readObject();
		ois3.close();
		
		Scanner userInput = new Scanner(System.in);
		System.out.println("Please input the directory location of the latimes index: ");
		String indexLocation = userInput.nextLine();
		System.out.println("Please input the directory location of the queries file: ");
		String queryLocation = userInput.nextLine();
		System.out.println("Please input the preffered name of the results file: ");
		String resultsName = userInput.nextLine();
				
		
		runProgram(postingsList,indexLocation,queryLocation,resultsName,lexicon, idMapping);
		

	}
}
