import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;

public class getDoc extends indexEngine {
	
	//indexEngine storage = new indexEngine();
	//HashMap<Integer, String> idMapping = storage.getIdMap();
	static HashMap<Integer, String> idMapping = new HashMap<Integer, String>();
	static HashMap<String, Integer> idMapping1 = new HashMap<String, Integer>();
	static int internalId = 0;
	
    public static HashMap<Integer, String> generateInternalIds(File[] files, HashMap <Integer, String> idMapping) {
    	
    	String docNo = null;
    	
        for (File file : files) {
            if (file.isDirectory()) {
                generateInternalIds(file.listFiles(),idMapping); // Calls same method again
            } else {
            	
            	docNo = file.getName();
            	docNo = docNo.replaceAll(".txt","");    	
            	idMapping.put(internalId,docNo);
            	idMapping1.put(docNo, internalId);
            	
            }
            
            internalId++;
            
        }
        
        return idMapping;
    }
    
    public static String checkIfArticleExists(String articleIndex, String indexLocation) {
    	
    	
    	if(!articleIndex.contains("LA") && !articleIndex.matches("[0-9]+")) {
    		String directoryPath = "Article Does not exist";
    		return directoryPath;
    	}
    	
    	if(!articleIndex.contains("LA") && articleIndex.matches("[0-9]+") && articleIndex.length() >= 0) {
    		
    		int articleIndex1 = Integer.parseInt(articleIndex);
			articleIndex = idMapping.get(articleIndex1);
			
			if(!idMapping.containsKey(articleIndex1)) {
				String directoryPath = "Article does not exist";
				return directoryPath;
				
			}
    	}
    	
    	if(articleIndex.matches("LA")) {
    		String directoryPath = "Article Does not exist";
    		return directoryPath;
    	}
    	
		String yearPath = articleIndex.substring(6,8);
		yearPath = "19" + yearPath;
		String monthPath = articleIndex.substring(2,4);
		String dayPath = articleIndex.substring(4,6);
		String yearAndMonthPath = yearPath + "-" + monthPath;
		String yearMonthAndDayPath = yearPath + "-" + monthPath + "-" + dayPath;
		String directoryPath = indexLocation + "/" + yearPath + "/" + yearAndMonthPath + "/" + yearMonthAndDayPath + "/" + articleIndex + ".txt";
		return directoryPath;
    }
    
	
	 public static void main(String[] args) throws IOException {
		 
		 BufferedReader reader;
		 String text;
		 Scanner userInput = new Scanner(System.in);
		 System.out.println("Please input the directory location of the latimes-index:");
		 String indexLocation = userInput.nextLine();
		 		 
		 
		 if(indexLocation.isEmpty()) {
			 System.out.println("No arguements was passed to the program. Please enter a directory location!");
		 } else {
			 
		 File dir = new File(indexLocation);
		 idMapping = generateInternalIds(dir.listFiles(), idMapping);
		 System.out.println("Please enter the doc number or the internal id of the doc you are looking for:");
		 String articleIndex = userInput.nextLine();
		 
		 
		 String directoryPath = checkIfArticleExists(articleIndex,indexLocation);
		 File articleDirectory = new File(directoryPath);
		 
		 
		 if(articleIndex.contains("LA") && articleDirectory.exists()) {
			 System.out.println("internal id: " + idMapping1.get(articleIndex));
			 reader = new BufferedReader(new FileReader(articleDirectory));
			 
			 while ((text = reader.readLine()) != null) {
				 System.out.println(text);
			 }
		 } 
		 
		 if(!articleIndex.contains("LA") && articleDirectory.exists()) {
			 System.out.println("internal id: " + articleIndex);
			 int articleIndex1 = Integer.parseInt(articleIndex);
			 articleIndex = idMapping.get(articleIndex1);
			 reader = new BufferedReader(new FileReader(articleDirectory));
			 while ((text = reader.readLine()) != null) {
				 System.out.println(text);
			 }
		 }
			
		 if(!articleDirectory.exists()) {
			 System.out.println("Article does not exist!");
			 
		 	}
		} 
	}
}
	
	
	