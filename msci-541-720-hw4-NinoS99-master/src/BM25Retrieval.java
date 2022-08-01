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

public class BM25Retrieval{
	
	static HashMap<String, Integer> docWordCount = new HashMap<String, Integer>();
	static HashMap<String, Double> docBMEScore = new HashMap<String, Double >();
	
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
    
    public static int getWordCountOfDoc(String documentPath, String docNo) throws IOException {
    	
		FileInputStream fileStream = new FileInputStream (documentPath);
		BufferedReader br = new BufferedReader(new InputStreamReader(fileStream));
		String text;
		String runningText = null;
		int lineCount = 0;
		int wordCount = 0;
		ArrayList<String> tokens = new ArrayList<String>();
		
		
		while((text = br.readLine()) != null) {
			
			if(lineCount < 20) {
				lineCount++;
				continue;
			}
			
			if(!text.contains("<") && !text.contains(">")) {
				runningText = runningText + text;
			} else {
				lineCount++;
				continue;
			}
			
		}
		
		tokens = tokenize(runningText);
		wordCount = tokens.size();
		br.close();
		return wordCount;
    	
    }
    
    
    public static void getWordCountofDocs(String latimesIndexLocation) throws IOException{
    	
    	File dir = new File(latimesIndexLocation);
    	File [] directoryListing = dir.listFiles();
    	String text;
    	String docNo = null;
    	int docLength;
    	int runningDocLength = 0;
    	int lineCount = 0;
    	int docCount = 0;
    	String runningText = "";
    	ArrayList<String> words = new ArrayList<String>();
    	
    	if(directoryListing != null) {
    		for(File results : directoryListing) { //Year
    			
    			File dir1 = new File(results.getAbsolutePath());
    			File [] resultsListing = dir1.listFiles();
    			
    			
    			if(resultsListing != null) {
    			for(File results1 : resultsListing) { //Year-Month
    				
    				File dir2 = new File(results1.getAbsolutePath());
    				File [] resultsListing2 = dir2.listFiles();
    				
    				if(resultsListing2 != null) {
    				for(File results2 : resultsListing2) { //Year-Month-Year
    					
    					File dir3 = new File(results2.getAbsolutePath());
    					File [] resultsListing3 = dir3.listFiles();
    					
    					if(resultsListing3 != null) {
    					for(File results3 : resultsListing3) { //Text File
    				
    			
    						if(results3.isFile()) {

    							FileInputStream fileStream = new FileInputStream(results3);
    							BufferedReader br = new BufferedReader(new InputStreamReader(fileStream));
    			
    							while((text = br.readLine()) != null) {
    				
    								if(lineCount == 0) {
    									docNo = text.substring(7);
    								}
    				
    				
    								if(lineCount < 20) {
    									lineCount++;
    									continue;
    								}
    				
    								if(!text.contains("<") && !text.contains(">")) {
    									runningText = runningText + text;
    									lineCount++;
    								} else {
    									lineCount++;
    									continue;
    								}
    				
    							}
    						}
    						
    						words = tokenize(runningText);
    						docLength = words.size();
    						docWordCount.put(docNo,docLength);
    						runningDocLength = runningDocLength + docLength;
    						lineCount = 0;
    						docCount++;
    						runningText = "";
    			
    						}	
    		
    					}
    				}	
    			}
    		}
    			}
    		}
    	}
    	
    	docWordCount.put("totalDocLength", runningDocLength);
    }
    
    public static HashMap<String,Double> calculateBME(int tokenID, int tokenIDCount,ArrayList<Integer>[] postingsList, HashMap<Integer, String> idMapping, String laTimesIndexLocation) throws IOException{
    	
    	int docId;
    	String docNo;
    	String documentPath;
    	
    	double Ni = postingsList[tokenID].size() / 2;
    	double N = docWordCount.size() - 1;
    	double k1 = 1.2;
    	double b = 0.75;
    	double k2 = 7;
    	double K = 0.0;
    	double QFi = tokenIDCount;
    	double Fi = 0.0;
    	double dl = 0.0;
    	double avdl = docWordCount.get("totalDocLength") / N;
    	
    	double BMEscore = 0.0;
    	
    	int i = 0;
    	while(i != postingsList[tokenID].size()) {
    		
    		docId = postingsList[tokenID].get(i);
    		docNo = idMapping.get(docId);
    		
    		String yearPath = docNo.substring(6,8);
			yearPath = "19" + yearPath;
			String monthPath = docNo.substring(2,4);
			String dayPath = docNo.substring(4,6);
			String yearAndMonthPath = yearPath + "-" + monthPath;
			String yearMonthAndDayPath = yearPath + "-" + monthPath + "-" + dayPath;	
			documentPath = laTimesIndexLocation + "/" + yearPath + "/" + yearAndMonthPath + "/" + yearMonthAndDayPath + "/" + docNo + ".txt";
			
    		dl = getWordCountOfDoc(documentPath,docNo);
    		Fi = postingsList[tokenID].get(i+1);
    		
    		K = k1 * ( (1-b) + b * (dl/avdl) );
    		
    		double part1 = (k1 + 1) * Fi;
    		double part2 = K + Fi;
    		double firstThird = part1 / part2;
    		
    		double part3 = (k2 + 1) * QFi;
    		double part4 = k2 + QFi;
    		double secondThird = part3 / part4;
    		
    		double part5 = (N - Ni + 0.5) / (Ni + 0.5);
    		double thirdThird = Math.log(part5);
    			
    		BMEscore = firstThird * secondThird * thirdThird;
    		
    		if(docBMEScore.containsKey(docNo)) {
    		docBMEScore.put(docNo, docBMEScore.get(docNo) + BMEscore);
    		i = i + 2;
    		} else {
    			docBMEScore.put(docNo,BMEscore);
    			i = i + 2;
    		}
    		
    	}
    	
    	return docBMEScore;
    }
    
    
    public static void runScores(ArrayList<Integer>[] postingsList, String resultsLocation, String queryLocation, String resultsName, HashMap <String,Integer> lexicon, HashMap<Integer, String> idMapping, String laTimesIndexLocation) throws IOException {
    	
    	FileInputStream queryFile = new FileInputStream(queryLocation);
    	BufferedReader br = new BufferedReader (new InputStreamReader(queryFile));
    	
    	Writer output = null;
    	File filePath = new File(resultsLocation + "/" + resultsName);
		FileOutputStream file = new FileOutputStream(filePath);
		output = new BufferedWriter(new OutputStreamWriter(file));
		
		String topicNum;
		String queryInput;
		String q0 = "q0";
		int rank = 1;
		String runTag = "nspasikBM25";
		
		int tokenID;
		
		while ((queryInput = br.readLine()) != null) {
			
			topicNum = queryInput.substring(0,3);
			queryInput = queryInput.substring(4);
			
    		ArrayList<String> tokens = tokenize(queryInput);
    		ArrayList<Integer> tokenIDs = getQueryTermID(tokens,lexicon);
    		
    		Map<String,Double> resultsMap = new HashMap<String,Double>();
    		
    		int numOfWordsInQuery = tokenIDs.size();
    		
    		for(int i = 0; i < numOfWordsInQuery; i++) {
    			
    			tokenID = tokenIDs.get(i);
    			int tokenIDCount = Collections.frequency(tokenIDs,tokenID);
    			
    			resultsMap = calculateBME(tokenID, tokenIDCount, postingsList, idMapping,laTimesIndexLocation);
    				
    		}
    		
    		LinkedHashMap<String, Double> sortedMap = 
    			    resultsMap.entrySet()
    			       .stream()             
    			       .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
    			       .collect(Collectors.toMap(e -> e.getKey(), 
    			                                 e -> e.getValue(), 
    			                                 (e1, e2) -> null, // or throw an exception
    			                                 () -> new LinkedHashMap<String, Double>()));
    		
    		for(String doc: sortedMap.keySet()) {
    			
    			if(rank > 1000) {
    				break;
    			}
    			
    			double score = sortedMap.get(doc);
    			String outputString = topicNum + " " + q0 + " " + doc + " " + rank + " " + score + " " + runTag;
    			output.write(outputString);
    			((BufferedWriter) output).newLine();
    			rank++;
    		}
    		
    		rank = 1;
    		tokens.clear();
    		tokenIDs.clear();
    		resultsMap.clear();
    		sortedMap.clear();
    		docBMEScore.clear();
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
		
		getWordCountofDocs(indexLocation);
		runScores(postingsList,indexLocation,queryLocation,resultsName,lexicon,idMapping,indexLocation);
    }
  
}