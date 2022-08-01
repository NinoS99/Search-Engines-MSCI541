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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;



import java.io.FileWriter;
import java.io.IOException;


public class indexEngine  {
	
	HashMap<Integer, String> idMapping = new HashMap<Integer, String>();
	
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
	
	public static  void writeArticlesToDirectory(String gZipLocation, String directoryLocation, Map<Integer, String> idMapping) throws FileNotFoundException, IOException {
		
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
				directoryPath = null;
				output = null;
				internalID++;
				lineCount = 0;
				continue;	
			}
		}
	}
		
	public static void main(String[] args) throws FileNotFoundException, IOException {
		
		HashMap<Integer, String> idMapping = new HashMap<Integer, String>();
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
