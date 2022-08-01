import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;



import java.io.FileWriter;
import java.io.IOException;


public class indexEngine  {
	
	//booleanAND passidMapping;
	
	static HashMap<Integer, String> idMapping = new HashMap<Integer, String>();
	static HashMap<String, Integer> lexicon = new HashMap<String, Integer>();
	static HashMap<Integer, String> reverseLexicon = new HashMap<Integer, String>();
	static HashMap<Integer, String> reverseIndex = new HashMap<Integer, String>();
	@SuppressWarnings("unchecked")
	static ArrayList<Integer>[] postingsList = (ArrayList<Integer>[]) new ArrayList[350000];
	static HashMap<Integer, String> inverseIndex = new HashMap<Integer, String>();
	

	public static void writeLexiconToMemory(String lexiconFilePath, HashMap <String, Integer> lexicon){
		File filePath = new File(lexiconFilePath);
		BufferedWriter bf = null;
		
		try {
			
			bf = new BufferedWriter(new FileWriter(filePath));
			
			for (Map.Entry<String, Integer> entry : lexicon.entrySet()) {
				
				bf.write(entry.getKey() + " " + entry.getValue());
				bf.newLine();
			}
			
			bf.flush();
		} catch(IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
			bf.close();
		}
		
		catch(Exception e) {
		}
			
		}
	}
	
	public static void writeIntegerToStringMapping(String mappingFilePath, HashMap <Integer, String> mapping){
		File filePath = new File(mappingFilePath);
		BufferedWriter bf = null;
		
		try {
			
			bf = new BufferedWriter(new FileWriter(filePath));
			
			for (Map.Entry<Integer, String> entry : mapping.entrySet()) {
				
				bf.write(entry.getKey() + " " + entry.getValue());
				bf.newLine();
			}
			
			bf.flush();
		} catch(IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
			bf.close();
		}
		
		catch(Exception e) {
		}
			
		}
	}
	
	public static void buildDirectory(String directoryLocation) {
		
		LocalDate startDate = LocalDate.parse("1989-01-01");
		
		for (int x = 0; x < 731; x++) {
			LocalDate date = startDate.plusDays(x);
			int year = date.getYear();
			int month = date.getMonthValue();
			int day = date.getDayOfMonth();
			String monthString;
			String dayString;
			
			if (month < 10) {
				 monthString = "0" + month;
			}
				else {
					 monthString = String.valueOf(month);
				}
			
			if (day < 10) {
				 dayString = "0" + day;
			}
				else {
					 dayString = String.valueOf(day);
				}
			
			String monthHiearchy = year + "-" + monthString + "/";
			String dayHiearchy = year + "-" + monthString + "-" + dayString;
			File dir = new File(directoryLocation + "/" + year + "/" + monthHiearchy + dayHiearchy );
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}
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
    
    public static ArrayList<Integer> convertTokensToIDs(ArrayList<String> tokens, HashMap <String, Integer> lexicon ){
    	
    	ArrayList<Integer> tokenIDs = new ArrayList<Integer>();
    	
    	for(String token: tokens) {
    		if(lexicon.containsKey(token)) {
    			
    			tokenIDs.add(lexicon.get(token));
    			
    		} else {
    			
    			int id = lexicon.size();
    			lexicon.put(token, id);
    			reverseLexicon.put(id, token);
    			tokenIDs.add(id);
    		}
    	}
    	
    	return tokenIDs;
    	
    }
    
    public static HashMap<Integer,Integer> countWords(ArrayList<Integer> tokenIDs){
    	
    	HashMap<Integer,Integer> wordCounts = new HashMap<Integer, Integer>();
    	
    	for(int id: tokenIDs) {
    		
    		
    		if(wordCounts.containsKey(id)) {
    			

    			wordCounts.put(id,wordCounts.get(id) + 1);
    			
    		} else {
    			

    			wordCounts.put(id,1);
    			
    		}
    	}
    	
    	return wordCounts;
    	
    }
    
    public static void addToPosting(HashMap<Integer,Integer> wordCounts, int docID, HashMap<Integer, String> inverseIndex) {
    	
    	
    	for(int termID : wordCounts.keySet()) {
    			

    		int count = wordCounts.get(termID);

    		
    		if(inverseIndex.containsKey(termID)) {
    			postingsList[termID].add(docID);
    			postingsList[termID].add(count);
    			
    		} else {
    			String token = reverseLexicon.get(termID);
    			
    			ArrayList<Integer> emptyRow = new ArrayList<Integer>();
    			emptyRow.add(docID);
    			emptyRow.add(count);
    			
    			inverseIndex.put(termID, token);
    			postingsList[termID] = new ArrayList<Integer>();
    			postingsList[termID].add(docID);
    			postingsList[termID].add(count);
    			
    		}
    		    		
    		
    	}
    	

    }
    
	public static void writeArticlesToDirectory(String gZipLocation, String directoryLocation, HashMap<Integer, String> idMapping) throws FileNotFoundException, IOException {
		
		GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(gZipLocation));		
		BufferedReader br = new BufferedReader(new InputStreamReader(gzip));
		
		
		String text;
		String docStartCheck = "<DOC>";
		String docEndCheck = "</DOC>";
		String docNoCheck = "<DOCNO>";
		String docNo = null;
		String articleHeadline = null;
		String directoryPath = null;
		int internalID = 0;
		int lineCount = 0;
		int headlineExisting = 0;
		String runningText = "The ";
		boolean keepTrack = false;
		ArrayList<String> tokens = new ArrayList<String>();
		ArrayList<Integer> tokenIDs = new ArrayList<Integer>();
		HashMap<Integer,Integer> wordCounts = new HashMap<Integer,Integer>();
		
		Writer output = null;

			
		while ((text = br.readLine()) != null) {
			
			lineCount++;
			
			if(text.contains(docStartCheck)) {
				continue;
			}
			
			//Retrieve the Document Number and Create Directory Path for Storage & Start to Write Text into New File
			if(text.contains(docNoCheck)) {
				docNo = text.replaceAll("<DOCNO>", "");
				docNo = docNo.replaceAll("</DOCNO>", "");
				docNo = docNo.replaceAll("\\s+","");
				String yearPath = docNo.substring(6,8);
				yearPath = "19" + yearPath;
				String monthPath = docNo.substring(2,4);
				String dayPath = docNo.substring(4,6);
				String yearAndMonthPath = yearPath + "-" + monthPath;
				String yearMonthAndDayPath = yearPath + "-" + monthPath + "-" + dayPath;	
				directoryPath = directoryLocation + "/" + yearPath + "/" + yearAndMonthPath + "/" + yearMonthAndDayPath + "/" + docNo + ".txt";
				
				File filePath = new File(directoryPath);
				FileOutputStream file = new FileOutputStream(filePath);
				output = new BufferedWriter(new OutputStreamWriter(file));
				output.write("docno: " + docNo);;
				((BufferedWriter) output).newLine();
				output.write("date: " + monthPath + "/" + dayPath + "/" + yearPath);
				((BufferedWriter) output).newLine();
				output.write("raw document:");
				((BufferedWriter) output).newLine();
				output.write("<DOC>");
				((BufferedWriter) output).newLine();
			}
			
			if (lineCount == 19) {
				if (text.contains("<HEADLINE>")) {
					headlineExisting = 1;
				} else {
					headlineExisting = 0;
				}
				
			}
			
			if (lineCount == 21) { //Capturing the headline of an article
				if (headlineExisting == 1) {
					articleHeadline = "headline: " + text;
				} else  {
					articleHeadline = "headline: No headline";
				}
				
			}
			
			if(keepTrack == true && !text.contains("<") && !text.contains(">")) {
				runningText = runningText + text;
			}
			
			if ((text.contains("<HEADLINE>") || text.contains("<TEXT>") || text.contains("<GRAPHIC>")) && lineCount >= 19) {
				keepTrack = true;
			}
			
			if ((text.contains("</HEADLINE>") || text.contains("</TEXT>") || text.contains("</GRAPHIC>")) && lineCount >= 19) {
				keepTrack = false;
			}
			
			if(output != null) { //Write the line that is currently being read for Gzip file
				output.write(text);
				((BufferedWriter) output).newLine();
			}
			
			//Arrival at end of the article
			if(text.contains(docEndCheck)) {				
				output.close();
				Path path = Paths.get(directoryPath);
				List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
				int position = 2;
				lines.add(position, articleHeadline);
				Files.write(path, lines, StandardCharsets.UTF_8);
				idMapping.put(internalID,docNo);
				//Call tokenize method
				tokens = tokenize(runningText);
				//Call convertTokenstoIDs method
				tokenIDs = convertTokensToIDs(tokens, lexicon);
				//Call countWords method
				wordCounts = countWords(tokenIDs);
				//Call addToPostings method
				addToPosting(wordCounts, internalID, inverseIndex);
				
				
				tokens.clear();
				tokenIDs.clear();
				wordCounts.clear();
				runningText = "";
				directoryPath = null;
				output = null;
				internalID++;
				lineCount = 0;
				continue;	
			}
		}
			
		FileOutputStream lexiconMemory = new FileOutputStream("lexicon.txt");
		ObjectOutputStream oos = new ObjectOutputStream(lexiconMemory);
		oos.writeObject(lexicon);
		oos.close();
		lexiconMemory.close();
		
		FileOutputStream inverseIndexMemory = new FileOutputStream("inverseIndex.txt");
		ObjectOutputStream oos1 = new ObjectOutputStream(inverseIndexMemory);
		oos1.writeObject(inverseIndex);
		oos1.close();
		inverseIndexMemory.close();
		
		FileOutputStream postingsListMemory = new FileOutputStream("postingsList.txt");
		ObjectOutputStream oos2 = new ObjectOutputStream(postingsListMemory);
		oos2.writeObject(postingsList);
		oos2.close();
		postingsListMemory.close();
		
		FileOutputStream idMappingMemory = new FileOutputStream("idMapping.txt");
		ObjectOutputStream oos3 = new ObjectOutputStream(idMappingMemory);
		oos3.writeObject(idMapping);
		oos3.close();
		idMappingMemory.close();
		
		
	}	
		
	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		Scanner userInput = new Scanner(System.in);
		System.out.println("Please input the file location of the latimes.gz file:");
		String gZipLocation = userInput.nextLine();
		System.out.println("Please input the preffered file location of the latimes-index directory:");
		String directoryLocation = userInput.nextLine();


		if(gZipLocation.isEmpty() || directoryLocation.isEmpty()) {
			
			System.out.println("Please make sure that both input fields are not empty!");

		} else {
			
			if(Files.isDirectory(Paths.get(directoryLocation))){
				System.out.println("Directory already exists!");
				
			} else {
				
				buildDirectory(directoryLocation);
				writeArticlesToDirectory(gZipLocation, directoryLocation, idMapping);
				System.out.println("Directory created and populated with articles from gzip file!");
				
			}
		}
		
	}	
}