
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

public class userInterfaceRetrieval{
	
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
    
    public static String getDocDirectoryLocation(String docNo, String indexLocation) {
    	
    	String directoryPath;
    	
		String yearPath = docNo.substring(6,8);
		yearPath = "19" + yearPath;
		String monthPath = docNo.substring(2,4);
		String dayPath = docNo.substring(4,6);
		String yearAndMonthPath = yearPath + "-" + monthPath;
		String yearMonthAndDayPath = yearPath + "-" + monthPath + "-" + dayPath;	
		directoryPath = indexLocation + "/" + yearPath + "/" + yearAndMonthPath + "/" + yearMonthAndDayPath + "/" + docNo + ".txt";
		
		return directoryPath;
    }
    
    public static String extractDocDate(String docNo) {
    	
		String yearPath = docNo.substring(6,8);
		yearPath = "19" + yearPath;
		String monthPath = docNo.substring(2,4);
		String dayPath = docNo.substring(4,6);
		String date = "(" + dayPath + "/" + monthPath + "/" + yearPath + ")";
		
		return date;
    }
    
    public static String extractHeadline(String docLocation) throws IOException {
    	
		FileInputStream fileStream = new FileInputStream (docLocation);
		BufferedReader br = new BufferedReader(new InputStreamReader(fileStream));
		
		String text;
		int lineCount = 0;
		String headline = "";
		int noHeadline = 0;
		
		while((text = br.readLine()) != null) {
			
			if(lineCount == 2) {
				
				if(!text.equals("headline: No headline")) {
					String[] headlineArray = text.split(":",2);
					headline = headlineArray[1];
					noHeadline = 0;
					
				} else {
					noHeadline = 1;
				}
			}
			
			if(lineCount == 24 && noHeadline == 1) {
				
				headline = text + "...";
			}
			
			lineCount++;	
		}
		
		return headline;
		
    }
    
    public static String extractQueryBiasedSummary(String docLocation, ArrayList<String> tokens) throws IOException {
    	
		FileInputStream fileStream = new FileInputStream (docLocation);
		BufferedReader br = new BufferedReader(new InputStreamReader(fileStream));
		
		String text;
		String runningText = "";
		int lineCount = 0;
		int sentenceCount = 0;
		int readLines = 0;
		
		ArrayList<String> sentence = new ArrayList<String>();
		ArrayList<ArrayList<String>> tokenizedSentences = new ArrayList<ArrayList<String>>();
		HashMap<String,Integer> sentenceCountMap = new HashMap<String, Integer>();
		Map<Integer,Integer> sentenceScores = new HashMap<Integer,Integer>();
		
		while((text = br.readLine()) != null) {
			
			if(text.equals("<TEXT>")) {
				readLines = 1;
				continue;
			}
			
			if(readLines == 1) {
				
				if(text.contains("<") && text.contains(">") ) {
					lineCount++;
					continue;
				}
				
				if(text.contains(".")) {
					String [] textArray = text.split("\\.",2);
					runningText = runningText + textArray[0] + ".";
					sentence.add(runningText);
					sentenceCountMap.put(runningText,sentenceCount);
					sentenceCount++;
					runningText = null;
					runningText = textArray[1];
				}
				
				else if(text.contains("!")){
					String [] textArray = text.split("\\!",2);
					runningText = runningText + textArray[0] + "!";
					sentence.add(runningText);
					sentenceCountMap.put(runningText,sentenceCount);
					sentenceCount++;
					runningText = null;
					runningText = textArray[1];
				}
				
				else if(text.contains("?")) {
					String [] textArray = text.split("\\?",2);
					runningText = runningText + textArray[0] + "?";
					sentence.add(runningText);
					sentenceCountMap.put(runningText,sentenceCount);
					sentenceCount++;
					runningText = null;
					runningText = textArray[1];
				}
				
				else {
					runningText = runningText + text;
				}
			
			}
			
			lineCount++;
		}
		
		br.close();
		
		for(int i = 0; i < sentence.size(); i++) {
			
			String sentenceText = sentence.get(i);
			ArrayList<String> tokenizedSentence = tokenize(sentenceText);
			
			tokenizedSentences.add(tokenizedSentence);
			
		}
		
		int l;
		for(ArrayList<String> sentenceList : tokenizedSentences) {
			
			if(tokenizedSentences.indexOf(sentenceList) == 0) {
				l = 2;
			} else if(tokenizedSentences.indexOf(sentenceList) == 1) {
				l = 1;
			} else {
				l = 0;
			}
			
			int c;
			int runningC = 0;
			int d;
			int runningD = 0;
			
			for(String queryToken: tokens) {
				c = Collections.frequency(sentenceList,queryToken);
				runningC = runningC + c;
				
				if(sentenceList.contains(queryToken)) {
					d = 1;
				} else {
					d = 0;
				}
				
				runningD = runningD + d;
				
			}
			
			int tokenCounter = 0;
			int k = 0;
			int runningK = 0;
			int index = 0;
			int highestK = 0;
			int numOfTokens = tokens.size() - 1;
			
			for(String token: sentenceList) {
				
				int currentIndex = sentenceList.indexOf(token);
				
				if(tokenCounter <= numOfTokens) {
				
				if(token.equals(tokens.get(tokenCounter)) && tokens.size() > 1) {
					
					if(index == 0) {
						k = 1;
						runningK = runningK + 1;
						tokenCounter++;
						index = currentIndex;
					}
					else if(currentIndex - index == 1) {
						k = 1;
						runningK = runningK + 1;
						tokenCounter++;
						index = currentIndex;
					}
					else {
						
						if(highestK == 0 || highestK < runningK) {
							highestK = runningK;
							runningK = 0;
						} 

					}

				} 
			} else {
					highestK = 1;
				}
				
			}
			
			int sentenceScore = runningC + runningD + l + highestK;
			int sentenceNumber = tokenizedSentences.indexOf(sentenceList);
			sentenceScores.put(sentenceNumber, sentenceScore);
			
		}
		
		LinkedHashMap<Integer, Integer> sortedMap = 
			    sentenceScores.entrySet()
			       .stream()             
			       .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
			       .collect(Collectors.toMap(e -> e.getKey(), 
			                                 e -> e.getValue(), 
			                                 (e1, e2) -> null, // or throw an exception
			                                 () -> new LinkedHashMap<Integer, Integer>()));
		
		int rank = 0;
		String summaryText = null;
		
		for(int sentenceNumber: sortedMap.keySet()) {
			
			if(rank < 5) {
				String sentence1 = sentence.get(sentenceNumber);
				sentence1 = sentence1.trim();
				
				if(rank == 0) {
					summaryText = sentence1;
				} else {
					summaryText = summaryText + "\n" + sentence1;		
				}
			}
			
			rank++;
		}
		
		return summaryText;
		
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
    
    public static void printResults(LinkedHashMap<String, Double> sortedMap, HashMap<Integer, String> rankingsMap, String laTimesIndexLocation, ArrayList<String> tokens ) throws IOException {
    	
    	int rank = 1;
    	
		for(String doc: sortedMap.keySet()) {
			
			if(rank > 10) {
				break;
			}
			
			rankingsMap.put(rank,doc);
			double score = sortedMap.get(doc);
			String docLocation = getDocDirectoryLocation(doc, laTimesIndexLocation);
			String docDate = extractDocDate(doc);
			String headline = extractHeadline(docLocation);
			String summaryText = extractQueryBiasedSummary(docLocation,tokens);
			
			System.out.println(rank + ". " + headline + " " + docDate);
			System.out.println("");
			System.out.println(summaryText + " (" + doc + ")");
			System.out.println("");
			
			
			rank++;
			
		}
    }
    
    public static void printDocument(String docNo, String laTimesIndexLocation) throws IOException {
    	
		String yearPath = docNo.substring(6,8);
		yearPath = "19" + yearPath;
		String monthPath = docNo.substring(2,4);
		String dayPath = docNo.substring(4,6);
		String yearAndMonthPath = yearPath + "-" + monthPath;
		String yearMonthAndDayPath = yearPath + "-" + monthPath + "-" + dayPath;	
		String documentPath = laTimesIndexLocation + "/" + yearPath + "/" + yearAndMonthPath + "/" + yearMonthAndDayPath + "/" + docNo + ".txt";
		
		FileInputStream fileStream = new FileInputStream (documentPath);
		BufferedReader br = new BufferedReader(new InputStreamReader(fileStream));
		
		String text;
		
		while((text = br.readLine()) != null) {
			
			System.out.println(text + "\n");
		}
		
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
    
    
    public static void runScores(ArrayList<Integer>[] postingsList, String query, HashMap <String,Integer> lexicon, HashMap<Integer, String> idMapping, String laTimesIndexLocation) throws IOException {
    	

		long startTime = System.currentTimeMillis();
		String topicNum;
		String queryInput;
		String q0 = "q0";
		int rank = 1;
		String runTag = "nspasikBM25";
		
		int tokenID;
		
		if(query != null) {
			
			
    		ArrayList<String> tokens = tokenize(query);
    		ArrayList<Integer> tokenIDs = getQueryTermID(tokens,lexicon);
    		
    		Map<String,Double> resultsMap = new HashMap<String,Double>();
    		HashMap<Integer, String> rankingsMap = new HashMap<Integer, String>();
    		
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
    		
    		
    		printResults(sortedMap, rankingsMap,laTimesIndexLocation,tokens);
    		long endTime = System.currentTimeMillis();
    		float totalTime = (endTime - startTime) / 1000F;
    		System.out.println("Retrieval Time: " + totalTime + " seconds");
    		System.out.println("");
    		
    		Scanner userInput = new Scanner(System.in);
    		System.out.println("Enter the number of the document you wish to open or enter N for new query or enter Q to quit: ");
    		String userAnswer = userInput.nextLine();
    		System.out.println("");
    		
    		int numberIndicator = 0;
    		
    		for(char c : userAnswer.toCharArray()) {
    			
    			if(!Character.isDigit(c)) {
    				numberIndicator = 0;
    			} else {
    				numberIndicator = 1;
    			}
    		}
    		
    		if(numberIndicator == 1) {
    			
    			int number = Integer.parseInt(userAnswer);
    			
    			if(number <= 10) {
    				
    				String docNumber = rankingsMap.get(number);
    				printDocument(docNumber,laTimesIndexLocation);
    				System.out.println("Press any key to return to search results:");
    				String userAnswer1 = userInput.nextLine();
    				runScores(postingsList,query,lexicon,idMapping,laTimesIndexLocation);
    				
    			}
    			else {
    				System.out.println("Please enter a number less than or equal to 10! Redirecting you back to query results...");
    				runScores(postingsList,query,lexicon,idMapping,laTimesIndexLocation);
    			}
    		}
    		
    		if(userAnswer.equals("N")) {
    			
    			System.out.println("Please input your new query: ");
    			String newQuery = userInput.nextLine();
    			System.out.println("");
        		rank = 1;
        		tokens.clear();
        		tokenIDs.clear();
        		resultsMap.clear();
        		sortedMap.clear();
        		docBMEScore.clear();
        		rankingsMap.clear();
    			runScores(postingsList,newQuery,lexicon,idMapping,laTimesIndexLocation);
    			
    		}
    		
    		if(userAnswer.equals("Q")) {
    			
    			System.out.println("Thank you for using this program! :)");
    		}
    		
    		if(numberIndicator == 0) {
    			
    			if(!userAnswer.equals("N")) {
    			
    				if(!userAnswer.equals("Q")) {
    					
    					System.out.println("Please enter a valid input! Enter the number of the document you wish to open or enter N for new query or enter Q to quit, redirecting you back to query results...");
    					System.out.println("");
    					runScores(postingsList,query,lexicon,idMapping,laTimesIndexLocation);
				
    				}
				
    			}
    		
    		}
    		
    		rank = 1;
    		tokens.clear();
    		tokenIDs.clear();
    		resultsMap.clear();
    		sortedMap.clear();
    		docBMEScore.clear();
    		rankingsMap.clear();
    		
    		
		}
    	
    }
    
    
    public static void main(String[] args) throws IOException, ClassNotFoundException {
    	
    	System.out.println("Loading in lexicon, inverse index and postings list...");
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
		
		System.out.println("Load complete!");
		System.out.println("");
    	
		Scanner userInput = new Scanner(System.in);
		System.out.println("Please input the directory location of the latimes index: ");
		String indexLocation = userInput.nextLine();
		
		System.out.println("Building word count dictionary for each document...");
		getWordCountofDocs(indexLocation);
		System.out.println("Build complete!");
		System.out.println("");
		
		System.out.println("Please input your query: ");
		String query = userInput.nextLine();
		System.out.println("");
				
		runScores(postingsList,query,lexicon,idMapping,indexLocation);
    }
  
}