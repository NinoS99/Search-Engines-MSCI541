import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Scanner;

class row {
	
	String docNo;
	double score;
	
	public row(String docNo, double score) {
		
		this.docNo = docNo;
		this.score = score;
	}
}

class docNoCompare implements Comparator<row>{

	@Override
	public int compare(row o1, row o2) {
		// TODO Auto-generated method stub
		return o1.docNo.compareTo(o2.docNo);
	}
	
}

class scoreCompare implements Comparator<row>{

	@Override
	public int compare(row o1, row o2) {
		// TODO Auto-generated method stub
		double delta = o2.score - o1.score;
		if(delta > 0 ) {
			return 1;
		}
		
		if(delta < 0) {
			return -1;
		}
	
		return 0;
		
	}
	
}


public class evaluation {
	
	@SuppressWarnings("unchecked")
	static ArrayList<String>[] infoList = (ArrayList<String>[]) new ArrayList[46];
	static HashMap<String, Integer> topicMap = new HashMap<String, Integer>();
	static HashMap<String, Integer> wordCountMap = new HashMap<String, Integer>();
	
	public static double log2(double e) {
		
		double result = (Math.log(e) / Math.log(2));
		
		return result;
	}
	
	public static void orderResults(String resultsFilesLocation, String givenFolderLocation) throws IOException {
		
		File dir = new File(resultsFilesLocation);
		File [] directoryListing = dir.listFiles();
		String text;
		String topic = "401";
		String previousTopic = "401";
		String previousFileName = null;
		int rank;
		String fileName;
		ArrayList<row> rowList = new ArrayList<row>();
		String docNo;
		int lineNum = 1;
		String scoreString = null;
		double score = 0.0;
		Writer output = null;
		String filePath = null;
		String formatCheck;
		char formatCheck1;
		int ranking = 1;
		int iteration = 1;
		
		File dir1 = new File(givenFolderLocation + "/upload-to-learn/sortedResults");
		if (!dir1.exists()) {
			dir1.mkdirs();
		}
		
		if(directoryListing != null) {
			for(File results : directoryListing) {
				
				fileName = results.getName();
				fileName = fileName.split("\\.",2)[0];
				
				FileInputStream fileStream = new FileInputStream(results);
				BufferedReader br = new BufferedReader(new InputStreamReader(fileStream));
				
				if(previousFileName == null) {
					filePath = givenFolderLocation + "/upload-to-learn/sortedResults/sortedResults.txt";
					FileOutputStream file = new FileOutputStream(filePath);
					output = new BufferedWriter(new OutputStreamWriter(file));
					}
				
				
				while((text = br.readLine()) != null) {
					
					formatCheck = text.substring(7,9);
					formatCheck1 = formatCheck.charAt(0);
					
					
					if(formatCheck.contains("[") || formatCheck.contains("Q0")) {
						break;
					}
					
					if(Character.isLowerCase(formatCheck1)) {
						break;
					}
					
					topic = text.substring(0,3);
					docNo = text.substring(7,20);
					
					if(topic.equals(previousTopic) == false) {
						
						
						Collections.sort(rowList, new scoreCompare());
						

						ranking = 1;
						
						for (row row : rowList) {
							output.write(previousTopic + " Q0 " + row.docNo + " " + ranking  + " " + row.score + " " + previousFileName);
							((BufferedWriter) output).newLine();
							ranking++;
						}
						
						lineNum = 1;
						rowList.clear();
						
						}
					
				
			
					
					if(lineNum < 10) {
						
						for(int i = 23; i < 40; i++) {
							
							char c = text.charAt(i);
							
							if(!Character.isDigit(c) && c != '.' && c != '-') {
								scoreString = text.substring(23,i);
								break;
							} 
							
						}
					}
					
					if(lineNum < 100 && lineNum >= 10) {
						
						for(int i = 24; i < 41; i++) {
							char c = text.charAt(i);
							
							if(!Character.isDigit(c) && c != '.' && c != '-') {
								scoreString = text.substring(24,i);
								break;
							} 
							
						}
						
					}
					
					if(lineNum >= 100 && lineNum < 1000) {
						
						for(int i = 25; i < 42; i++) {
							char c = text.charAt(i);
							
							
							if(!Character.isDigit(c) && c != '.' && c != '-') {
								scoreString = text.substring(25,i);
								break;
							} 
							
							
						}
												
					}
					
					if(lineNum == 1000) {
												
						for(int i = 26; i < 43; i++) {
							char c = text.charAt(i);
							
							if(!Character.isDigit(c) && c != '.' && c != '-') {
								scoreString = text.substring(26,i);
								break;
							} 
							
							
						}
						
					}
					
					score = Double.parseDouble(scoreString);
					
					rowList.add(new row(docNo,score));
					
					lineNum++;
					previousTopic = topic;
					previousFileName = fileName;
				
				}
				
				lineNum = 1;
								
			}
			
			iteration++;
		}
		
		output.close();
	}
	
	
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
    
    public static ArrayList<String> getWordCountOfDoc(String documentPath, String docNo) throws IOException {
    	
		FileInputStream fileStream = new FileInputStream (documentPath);
		BufferedReader br = new BufferedReader(new InputStreamReader(fileStream));
		String text;
		String runningText = null;
		int lineCount = 0;
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
		br.close();
		return tokens;
    	
    }
		
	public static void getRelevance(String qRelsLocation) throws IOException {
		
		FileInputStream fileStream = new FileInputStream (qRelsLocation);
		BufferedReader br = new BufferedReader(new InputStreamReader(fileStream));
		String text;
		String topicNumber;
		int topicID = 0;
		String docNo;
		String relevantBoolean;
		
		while((text = br.readLine()) != null) {
			
			topicNumber = text.substring(0,3);
			docNo = text.substring(6,19);
			relevantBoolean = text.substring(20,21);
			
			
			
			if(!topicMap.containsKey(topicNumber)) {
				topicID++;
				topicMap.put(topicNumber, topicID);
				infoList[topicID] = new ArrayList<String>();
				infoList[topicID].add(docNo);
				infoList[topicID].add(relevantBoolean);
				
				
			} else {
				topicID = topicMap.get(topicNumber);
				infoList[topicID].add(docNo);
				infoList[topicID].add(relevantBoolean);
			}
			
			continue;
		}
		
		br.close();
		
	}
	
	public static void getWordCount(String qrelsLocation, String laTimesIndexLocation) throws IOException {
		
		FileInputStream fileStream = new FileInputStream (qrelsLocation);
		BufferedReader br = new BufferedReader(new InputStreamReader(fileStream));
		String text;
		String docNo;
		String documentPath;
		int wordCount;
		ArrayList<String> tokens = new ArrayList<String>();
		
		
		while((text = br.readLine()) != null) {
			
			docNo = text.substring(6,19);
			String yearPath = docNo.substring(6,8);
			yearPath = "19" + yearPath;
			String monthPath = docNo.substring(2,4);
			String dayPath = docNo.substring(4,6);
			String yearAndMonthPath = yearPath + "-" + monthPath;
			String yearMonthAndDayPath = yearPath + "-" + monthPath + "-" + dayPath;	
			documentPath = laTimesIndexLocation + "/" + yearPath + "/" + yearAndMonthPath + "/" + yearMonthAndDayPath + "/" + docNo + ".txt";
			
			tokens = getWordCountOfDoc(documentPath, docNo);
			wordCount = tokens.size();
			
			if(!wordCountMap.containsKey(docNo)) {
				wordCountMap.put(docNo,wordCount);
			}
						
		}
		
		br.close();
	}
	
	public static int getWordCountOfUnlistedDoc(String docNo, String laTimesIndexLocation) throws IOException {
		
		String documentPath;
		ArrayList<String> tokens = new ArrayList<String>();
		int wordCount;
		
		String yearPath = docNo.substring(6,8);
		yearPath = "19" + yearPath;
		String monthPath = docNo.substring(2,4);
		String dayPath = docNo.substring(4,6);
		String yearAndMonthPath = yearPath + "-" + monthPath;
		String yearMonthAndDayPath = yearPath + "-" + monthPath + "-" + dayPath;	
		documentPath = laTimesIndexLocation + "/" + yearPath + "/" + yearAndMonthPath + "/" + yearMonthAndDayPath + "/" + docNo + ".txt";
		
		tokens = getWordCountOfDoc(documentPath, docNo);
		wordCount = tokens.size();
		return wordCount;
		
	}
	public static void calculateEffectivenessMeasures(String resultsFilesLocation, String outputPath, String laTimesIndexLocation) throws IOException {
		
		File dir = new File(resultsFilesLocation);
		File [] directoryListing = dir.listFiles();
		String text;
		String formatCheck;
		String fileName;
		String previousFileName = "";
		char formatCheck1;
		String topic;
		String previousTopic = "401";
		int topicID = 0;
		int previousTopicID = 0;
		String docNo;
		int index = 0;
		int booleanIndex = 0;
		int wordCount = 0;
		String relevant;
		int rank = 1;
		int numOfRelDocsForTopic = 0;
		
		Writer output = null;
		File filePath = new File(outputPath);
		FileOutputStream file = new FileOutputStream(filePath);
		output = new BufferedWriter(new OutputStreamWriter(file));
		
		output.write("Student_Number ");
		output.write("Topic_Number ");
		output.write("Average_Precision ");
		output.write("Precision_At_10 ");
		output.write("NCDG_At_10 ");
		output.write("NCDG_At_1000 ");
		output.write("Time_Biased_Gain ");
		((BufferedWriter) output).newLine();
		
		//Average Precision Variables 
		double precisionAtK = 0.0;
		double precisionAt10 = 0.0;
		double relevanceTimesPrecision = 0;
		double relevanceTimesPrecisionSummation = 0;
		double runningRelevance = 0;
		double relevantDouble = 0;
		double averagePrecision;
		
		//NDCG Variables
		double DCGat10Summation = 0.0;
		double DCGat10 = 0.0;
		int DCGat10RelevantCount = 0;
		int DCGat10NotRelevantCount = 0;
		ArrayList<Double> DCGat10ICDGList = new ArrayList<Double>();
		
		double DCGat1000Summation = 0.0;
		double DCGat1000 = 0.0;
		int DCGat1000RelevantCount = 0;
		int DCGat1000NotRelevantCount = 0;
		ArrayList<Double> DCGat1000ICDGList = new ArrayList<Double>();
		
		//ICDG Variables
		double relevanceAtRank;
		double IDCGat10 = 0.0;
		double IDCGat1000 = 0.0;
		double IDCGat10Summation = 0.0;
		double IDCGat1000Summation = 0.0;
		double NCDGat10 = 0.0;
		double NCDGat1000 = 0.0;
		
		//TBG Variables
		double summaryTime = 4.4;
		double docTime = 7.8;
		double totalTime = 0.0;
		double h = 224.0;
		double TBGatRank;
		double TBGsummation = 0.0;
		double Gk = 0.0;
		double decayFunction = 0.0;
		double decayFunctionExponent = 0.0;
		
		if(directoryListing != null) {
			for(File results : directoryListing) {
				
				fileName = results.getName();
				fileName = fileName.split("\\.",2)[0];
				
				FileInputStream fileStream = new FileInputStream(results);
				BufferedReader br = new BufferedReader(new InputStreamReader(fileStream));
				
				while((text = br.readLine()) != null) {
					
					formatCheck = text.substring(7,9);
					formatCheck1 = formatCheck.charAt(0);
					
					topic = text.substring(0,3);
					
					docNo = text.substring(7,20);
					
					if(formatCheck.contains("[") || formatCheck.contains("Q0")) {
						output.write(fileName + " ");
						output.write(topic + " ");
						output.write("BAD FORMAT ");
						((BufferedWriter) output).newLine();
						break;
					}
					
					if(Character.isLowerCase(formatCheck1)) {
						output.write(fileName + " ");
						output.write(topic + " ");
						output.write("BAD FORMAT ");
						((BufferedWriter) output).newLine();
						break;
					}
					
					
					//Check if information about current doc exists for given topic
					
					topicID = topicMap.get(topic);
					
					if(infoList[topicID].contains(docNo)) {
						
						index = infoList[topicID].indexOf(docNo);
						booleanIndex = index + 1;
						relevant = infoList[topicID].get(booleanIndex);
						wordCount = wordCountMap.get(docNo);
						
					} else {
						relevant = "0";
						docNo = docNo.replaceAll("\\s+","");
						
						if(docNo.length() == 13) {
						wordCount = getWordCountOfUnlistedDoc(docNo, laTimesIndexLocation);
						} else {
							wordCount = 0;
						}
					}

					//Check to see if new topic has started, print out current values and reset them
					if(topic.equals(previousTopic) == false) {
						
						//AVERAGE PRECISION
						
						for(int i = 1; i <= infoList[previousTopicID].size(); i = i + 2) {
							
							String relevantNumber = infoList[previousTopicID].get(i);
							
							if(relevantNumber.contains("1")) {
								numOfRelDocsForTopic++;
							}
						}
												
						double oneOverR = 1.0 / numOfRelDocsForTopic;
						
						averagePrecision = oneOverR * relevanceTimesPrecisionSummation;
						
						//NDCG @ 10 and 1000
						
						//ICDG at 10
						for(int i = 0; i < DCGat10RelevantCount; i++) {
							DCGat10ICDGList.add(1.0);
						}
						
						for(int i = 0; i < DCGat10NotRelevantCount; i++) {
							DCGat10ICDGList.add(0.0);
						
						}
						
						
						for(int i = 0; i < DCGat10ICDGList.size(); i++) {
							relevanceAtRank = DCGat10ICDGList.get(i);
														
							double e = i  + 2.0;
							double denom = log2(e);							
							IDCGat10 = relevanceAtRank / denom;
							IDCGat10Summation = IDCGat10Summation + IDCGat10;
							
							
						}
						
						//ICDG at 1000
						for(int i = 0; i < DCGat1000RelevantCount; i++) {
							DCGat1000ICDGList.add(1.0);
						}
						
						for(int i = 0; i < DCGat1000NotRelevantCount; i++) {
							DCGat1000ICDGList.add(0.0);
						
						}
						
						for(int i = 0; i < DCGat1000ICDGList.size(); i++) {
							relevanceAtRank = DCGat1000ICDGList.get(i);
							
							double e = i + 2.0;
							double denom = log2(e);
							IDCGat1000 = relevanceAtRank / denom;
							IDCGat1000Summation = IDCGat1000Summation + IDCGat1000;
							
							
						}
						
						NCDGat10 = DCGat10Summation / IDCGat10Summation;
						NCDGat1000 = DCGat1000Summation / IDCGat1000Summation;
						
						
						output.write(previousFileName + " ");
						output.write(previousTopic + " ");
						output.write(averagePrecision + " ");
						output.write(precisionAt10 + " ");
						output.write(NCDGat10 + " ");
						output.write(NCDGat1000 + " ");
						output.write(TBGsummation + " ");
						((BufferedWriter) output).newLine();
						
						//Reset all variables used in calculations
						runningRelevance = 0.0;
						relevanceTimesPrecisionSummation = 0.0;
						rank = 1;
						numOfRelDocsForTopic = 0;
						
						DCGat10Summation = 0.0;
						DCGat1000Summation = 0.0;
						IDCGat10Summation = 0.0;
						IDCGat1000Summation = 0.0;
						DCGat10RelevantCount = 0;
						DCGat10NotRelevantCount = 0;
						DCGat1000RelevantCount = 0;
						DCGat1000NotRelevantCount = 0;
						
						DCGat10ICDGList.clear();
						DCGat1000ICDGList.clear();
						
						TBGsummation = 0.0;
						
						
					}
					
					
					//Average Precision 
					relevantDouble = Double.parseDouble(relevant);
					runningRelevance = runningRelevance + relevantDouble;
										
					precisionAtK = runningRelevance / rank;
					relevanceTimesPrecision = relevantDouble * precisionAtK;
					relevanceTimesPrecisionSummation = relevanceTimesPrecisionSummation + relevanceTimesPrecision;
					
					
					//Precision at 10 
					if(rank == 10) {
						precisionAt10 = precisionAtK;
					}
					
					//DCG @ 10
					
					if(rank <= 10) {
						double e = rank + 1.0;
						double denom = log2(e);
						DCGat10 = relevantDouble / denom;
						DCGat10Summation = DCGat10Summation + DCGat10;
						
						if(relevantDouble == 1.0) {
							DCGat10RelevantCount++;
						} else {
							DCGat10NotRelevantCount++;
						}
						
					}
					
					//DCG @ 1000
					
					if(rank <= 1000) {
						double e1 = rank + 1.0;
						double denom1 = log2(e1);
						DCGat1000 = relevantDouble / denom1;
						DCGat1000Summation = DCGat1000Summation + DCGat1000;
						
						if(relevantDouble == 1.0) {
							DCGat1000RelevantCount++;
						} else {
							DCGat1000NotRelevantCount++;
						}
						
						
					}
					
					
					//Time Biased Gain
					Gk = relevantDouble * 0.64 * 0.77;
					totalTime = summaryTime + (wordCount * 0.018) + docTime;
					double e = Math.log(2);
					decayFunctionExponent = (-1 * totalTime) * (e / h);
					decayFunction = Math.exp(decayFunctionExponent);
					
					TBGatRank = Gk * decayFunction;
					TBGsummation = TBGsummation + TBGatRank;
					
					rank++;
					previousTopic = topic;
					previousTopicID = topicID;
					previousFileName = fileName;
					
					}
								
			}
			
		}
		
		output.close();
		
	}
	
	public static void main(String[] args) throws IOException {
		
		
		Scanner userInput = new Scanner(System.in);
		System.out.println("Please input the file location of the hw3-files-2021 folder: ");
		String inputFileLocation = userInput.nextLine();
		
		System.out.println("Please input the file location of the latimes-index folder: ");
		String laTimesIndexLocation = userInput.nextLine();
		
		System.out.println("Please input the desired directory of the per topic evaluation text file: ");
		String outputPath = userInput.nextLine();
		
		String qRelsLocation = inputFileLocation + "/upload-to-learn/qrels/LA-only.trec8-401.450.minus416-423-437-444-447.txt";
		String topicsLocation = inputFileLocation + "/upload-to-learn/topics/topics.401-450.txt";
		String resultsFilesLocation = inputFileLocation + "/upload-to-learn/results-files";
		userInput.close();
		
		orderResults(resultsFilesLocation, inputFileLocation);
		getRelevance(qRelsLocation);
		getWordCount(qRelsLocation,laTimesIndexLocation);
		calculateEffectivenessMeasures(resultsFilesLocation, outputPath, laTimesIndexLocation);
		
	}
}