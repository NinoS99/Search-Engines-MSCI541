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

public class indexEngineStemmed  {
	
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
	    
    public static ArrayList<String> tokenizeAndStem(String text){
    	
    	text = text.toLowerCase();
    	ArrayList<String> tokens = new ArrayList<String>();
    	int start = 0;
    	int i;
    	String token;
    	String stemmedToken;
    	
    	for(i = 0; i < text.length(); i++) {
    		char c = text.charAt(i);
    		    		
    		if (!Character.isDigit(c) && !Character.isLetter(c)){
    			
    			if ( start != i) {
    				
    				
    				token = text.substring(start,i);
    				stemmedToken = stem(token);
    				tokens.add(stemmedToken);
    				
    			}
    			
    			start = i + 1;

    			
    		}
    		
    	}
    	
    	if(start != i) {
    		
			token = text.substring(start,i);
			stemmedToken = stem(token);
    		tokens.add(stemmedToken);
    	}
    	
    	return tokens;
    }
    
    public static String stem(String str)
    {
        try
        {
            String results = internalStem( str );
            if (results != null)
                return results;
            else
                return str;
        }
        catch ( Throwable t ) // best way in Java to catch all
        {
            return str;
        }
    }

    private static String internalStem(String str) 
    {
      // check for zero length
      if (str.length() > 0) {
          // all characters must be letters
          char[] c = str.toCharArray();
          for (int i = 0; i < c.length; i++) {
              if (!Character.isLetter(c[i]))
                  return null;
          }
    } else {
        return "No term entered";
    }

    str = step1a(str);
    str = step1b(str);
    str = step1c(str);
    str = step2(str);
    str = step3(str);
    str = step4(str);
    str = step5a(str);
    str = step5b(str);
    return str;
} // end stem

protected static String step1a (String str) {
    // SSES -> SS
    if (str.endsWith("sses")) {
        return str.substring(0, str.length() - 2);
    // IES -> I
    } else if (str.endsWith("ies")) {
        return str.substring(0, str.length() - 2);
    // SS -> S
    } else if (str.endsWith("ss")) {
        return str;
    // S ->
    } else if (str.endsWith("s")) {
        return str.substring(0, str.length() - 1);
    } else {
        return str;
    }
} // end step1a

protected static String step1b (String str) {
    // (m > 0) EED -> EE
    if (str.endsWith("eed")) {
        if (stringMeasure(str.substring(0, str.length() - 3)) > 0)
            return str.substring(0, str.length() - 1);
        else
            return str;
    // (*v*) ED ->
    } else if ((str.endsWith("ed")) &&
               (containsVowel(str.substring(0, str.length() - 2)))) {
        return step1b2(str.substring(0, str.length() - 2));
    // (*v*) ING ->
    } else if ((str.endsWith("ing")) &&
               (containsVowel(str.substring(0, str.length() - 3)))) {
        return step1b2(str.substring(0, str.length() - 3));
    } // end if
    return str;
} // end step1b

protected static String step1b2 (String str) {
    // AT -> ATE
    if (str.endsWith("at") ||
        str.endsWith("bl") ||
        str.endsWith("iz")) {
        return str + "e";
    } else if ((endsWithDoubleConsonent(str)) &&
               (!(str.endsWith("l") || str.endsWith("s") || str.endsWith("z")))) {
        return str.substring(0, str.length() - 1);
    } else if ((stringMeasure(str) == 1) &&
               (endsWithCVC(str))) {
        return str + "e";
    } else {
        return str;
    }
} // end step1b2

protected static String step1c(String str) {
    // (*v*) Y -> I
    if (str.endsWith("y")) {
        if (containsVowel(str.substring(0, str.length() - 1)))
            return str.substring(0, str.length() - 1) + "i";
    } // end if
    return str;
} // end step1c

protected static String step2 (String str) {
    // (m > 0) ATIONAL -> ATE
    if ((str.endsWith("ational")) &&
        (stringMeasure(str.substring(0, str.length() - 5)) > 0)) {
        return str.substring(0, str.length() - 5) + "e";
    // (m > 0) TIONAL -> TION
    } else if ((str.endsWith("tional")) &&
        (stringMeasure(str.substring(0, str.length() - 2)) > 0)) {
        return str.substring(0, str.length() - 2);
    // (m > 0) ENCI -> ENCE
    } else if ((str.endsWith("enci")) &&
        (stringMeasure(str.substring(0, str.length() - 2)) > 0)) {
        return str.substring(0, str.length() - 2);
    // (m > 0) ANCI -> ANCE
    } else if ((str.endsWith("anci")) &&
        (stringMeasure(str.substring(0, str.length() - 1)) > 0)) {
        return str.substring(0, str.length() - 1) + "e";
    // (m > 0) IZER -> IZE
    } else if ((str.endsWith("izer")) &&
        (stringMeasure(str.substring(0, str.length() - 1)) > 0)) {
        return str.substring(0, str.length() - 1);
    // (m > 0) ABLI -> ABLE
    } else if ((str.endsWith("abli")) &&
        (stringMeasure(str.substring(0, str.length() - 1)) > 0)) {
        return str.substring(0, str.length() - 1) + "e";
    // (m > 0) ENTLI -> ENT
    } else if ((str.endsWith("alli")) &&
        (stringMeasure(str.substring(0, str.length() - 2)) > 0)) {
        return str.substring(0, str.length() - 2);
    // (m > 0) ELI -> E
    } else if ((str.endsWith("entli")) &&
        (stringMeasure(str.substring(0, str.length() - 2)) > 0)) {
        return str.substring(0, str.length() - 2);
    // (m > 0) OUSLI -> OUS
    } else if ((str.endsWith("eli")) &&
        (stringMeasure(str.substring(0, str.length() - 2)) > 0)) {
        return str.substring(0, str.length() - 2);
    // (m > 0) IZATION -> IZE
    } else if ((str.endsWith("ousli")) &&
        (stringMeasure(str.substring(0, str.length() - 2)) > 0)) {
        return str.substring(0, str.length() - 2);
    // (m > 0) IZATION -> IZE
    } else if ((str.endsWith("ization")) &&
        (stringMeasure(str.substring(0, str.length() - 5)) > 0)) {
        return str.substring(0, str.length() - 5) + "e";
    // (m > 0) ATION -> ATE
    } else if ((str.endsWith("ation")) &&
        (stringMeasure(str.substring(0, str.length() - 3)) > 0)) {
        return str.substring(0, str.length() - 3) + "e";
    // (m > 0) ATOR -> ATE
    } else if ((str.endsWith("ator")) &&
        (stringMeasure(str.substring(0, str.length() - 2)) > 0)) {
        return str.substring(0, str.length() - 2) + "e";
    // (m > 0) ALISM -> AL
    } else if ((str.endsWith("alism")) &&
        (stringMeasure(str.substring(0, str.length() - 3)) > 0)) {
       return str.substring(0, str.length() - 3);
    // (m > 0) IVENESS -> IVE
    } else if ((str.endsWith("iveness")) &&
        (stringMeasure(str.substring(0, str.length() - 4)) > 0)) {
        return str.substring(0, str.length() - 4);
    // (m > 0) FULNESS -> FUL
    } else if ((str.endsWith("fulness")) &&
        (stringMeasure(str.substring(0, str.length() - 4)) > 0)) {
        return str.substring(0, str.length() - 4);
    // (m > 0) OUSNESS -> OUS
    } else if ((str.endsWith("ousness")) &&
        (stringMeasure(str.substring(0, str.length() - 4)) > 0)) {
        return str.substring(0, str.length() - 4);
    // (m > 0) ALITII -> AL
    } else if ((str.endsWith("aliti")) &&
        (stringMeasure(str.substring(0, str.length() - 3)) > 0)) {
        return str.substring(0, str.length() - 3);
    // (m > 0) IVITI -> IVE
    } else if ((str.endsWith("iviti")) &&
        (stringMeasure(str.substring(0, str.length() - 3)) > 0)) {
        return str.substring(0, str.length() - 3) + "e";
    // (m > 0) BILITI -> BLE
    } else if ((str.endsWith("biliti")) &&
        (stringMeasure(str.substring(0, str.length() - 5)) > 0)) {
        return str.substring(0, str.length() - 5) + "le";
    } // end if
    return str;
} // end step2


protected static String step3 (String str) {
    // (m > 0) ICATE -> IC
    if ((str.endsWith("icate")) &&
        (stringMeasure(str.substring(0, str.length() - 3)) > 0)) {
        return str.substring(0, str.length() - 3);
    // (m > 0) ATIVE ->
    } else if ((str.endsWith("ative")) &&
        (stringMeasure(str.substring(0, str.length() - 5)) > 0)) {
        return str.substring(0, str.length() - 5);
    // (m > 0) ALIZE -> AL
    } else if ((str.endsWith("alize")) &&
        (stringMeasure(str.substring(0, str.length() - 3)) > 0)) {
        return str.substring(0, str.length() - 3);
    // (m > 0) ICITI -> IC
    } else if ((str.endsWith("iciti")) &&
        (stringMeasure(str.substring(0, str.length() - 3)) > 0)) {
        return str.substring(0, str.length() - 3);
    // (m > 0) ICAL -> IC
    } else if ((str.endsWith("ical")) &&
        (stringMeasure(str.substring(0, str.length() - 2)) > 0)) {
        return str.substring(0, str.length() - 2);
    // (m > 0) FUL ->
    } else if ((str.endsWith("ful")) &&
        (stringMeasure(str.substring(0, str.length() - 3)) > 0)) {
        return str.substring(0, str.length() - 3);
    // (m > 0) NESS ->
    } else if ((str.endsWith("ness")) &&
        (stringMeasure(str.substring(0, str.length() - 4)) > 0)) {
        return str.substring(0, str.length() - 4);
    } // end if
    return str;
} // end step3


protected static String step4 (String str) {
    if ((str.endsWith("al")) &&
        (stringMeasure(str.substring(0, str.length() - 2)) > 1)) {
        return str.substring(0, str.length() - 2);
        // (m > 1) ANCE ->
    } else if ((str.endsWith("ance")) &&
        (stringMeasure(str.substring(0, str.length() - 4)) > 1)) {
        return str.substring(0, str.length() - 4);
    // (m > 1) ENCE ->
    } else if ((str.endsWith("ence")) &&
        (stringMeasure(str.substring(0, str.length() - 4)) > 1)) {
        return str.substring(0, str.length() - 4);
    // (m > 1) ER ->
    } else if ((str.endsWith("er")) &&
        (stringMeasure(str.substring(0, str.length() - 2)) > 1)) {
        return str.substring(0, str.length() - 2);
    // (m > 1) IC ->
    } else if ((str.endsWith("ic")) &&
        (stringMeasure(str.substring(0, str.length() - 2)) > 1)) {
        return str.substring(0, str.length() - 2);
    // (m > 1) ABLE ->
    } else if ((str.endsWith("able")) &&
        (stringMeasure(str.substring(0, str.length() - 4)) > 1)) {
        return str.substring(0, str.length() - 4);
    // (m > 1) IBLE ->
    } else if ((str.endsWith("ible")) &&
        (stringMeasure(str.substring(0, str.length() - 4)) > 1)) {
        return str.substring(0, str.length() - 4);
    // (m > 1) ANT ->
    } else if ((str.endsWith("ant")) &&
        (stringMeasure(str.substring(0, str.length() - 3)) > 1)) {
        return str.substring(0, str.length() - 3);
    // (m > 1) EMENT ->
    } else if ((str.endsWith("ement")) &&
        (stringMeasure(str.substring(0, str.length() - 5)) > 1)) {
        return str.substring(0, str.length() - 5);
    // (m > 1) MENT ->
    } else if ((str.endsWith("ment")) &&
        (stringMeasure(str.substring(0, str.length() - 4)) > 1)) {
        return str.substring(0, str.length() - 4);
    // (m > 1) ENT ->
    } else if ((str.endsWith("ent")) &&
        (stringMeasure(str.substring(0, str.length() - 3)) > 1)) {
        return str.substring(0, str.length() - 3);
    // (m > 1) and (*S or *T) ION ->
    } else if ((str.endsWith("sion") || str.endsWith("tion")) &&
        (stringMeasure(str.substring(0, str.length() - 3)) > 1)) {
        return str.substring(0, str.length() - 3);
    // (m > 1) OU ->
    } else if ((str.endsWith("ou")) &&
        (stringMeasure(str.substring(0, str.length() - 2)) > 1)) {
        return str.substring(0, str.length() - 2);
    // (m > 1) ISM ->
    } else if ((str.endsWith("ism")) &&
        (stringMeasure(str.substring(0, str.length() - 3)) > 1)) {
        return str.substring(0, str.length() - 3);
    // (m > 1) ATE ->
    } else if ((str.endsWith("ate")) &&
        (stringMeasure(str.substring(0, str.length() - 3)) > 1)) {
        return str.substring(0, str.length() - 3);
    // (m > 1) ITI ->
    } else if ((str.endsWith("iti")) &&
        (stringMeasure(str.substring(0, str.length() - 3)) > 1)) {
        return str.substring(0, str.length() - 3);
    // (m > 1) OUS ->
    } else if ((str.endsWith("ous")) &&
        (stringMeasure(str.substring(0, str.length() - 3)) > 1)) {
        return str.substring(0, str.length() - 3);
    // (m > 1) IVE ->
    } else if ((str.endsWith("ive")) &&
        (stringMeasure(str.substring(0, str.length() - 3)) > 1)) {
        return str.substring(0, str.length() - 3);
    // (m > 1) IZE ->
    } else if ((str.endsWith("ize")) &&
        (stringMeasure(str.substring(0, str.length() - 3)) > 1)) {
        return str.substring(0, str.length() - 3);
    } // end if
    return str;
} // end step4


protected static String step5a (String str) {
    if (str.length() == 0) return str;  //gets rid of an exception...
    // (m > 1) E ->
    if ((stringMeasure(str.substring(0, str.length() - 1)) > 1) &&
        str.endsWith("e"))
        return str.substring(0, str.length() -1);
    // (m = 1 and not *0) E ->
    else if ((stringMeasure(str.substring(0, str.length() - 1)) == 1) &&
             (!endsWithCVC(str.substring(0, str.length() - 1))) &&
             (str.endsWith("e")))
        return str.substring(0, str.length() - 1);
    else
        return str;
} // end step5a


protected static String step5b (String str) {
  if (str.length() == 0) return str;
    // (m > 1 and *d and *L) ->
    if (str.endsWith("l") &&
        endsWithDoubleConsonent(str) &&
        (stringMeasure(str.substring(0, str.length() - 1)) > 1)) {
        return str.substring(0, str.length() - 1);
    } else {
        return str;
    }
} // end step5b


/*
   -------------------------------------------------------
   The following are functions to help compute steps 1 - 5
   -------------------------------------------------------
*/

// does string end with 's'?
protected static boolean endsWithS(String str) {
    return str.endsWith("s");
} // end function

// does string contain a vowel?
protected static boolean containsVowel(String str) {
    char[] strchars = str.toCharArray();
    for (int i = 0; i < strchars.length; i++) {
        if (isVowel(strchars[i]))
            return true;
    }
    // no aeiou but there is y
    if (str.indexOf('y') > -1)
        return true;
    else
        return false;
} // end function

// is char a vowel?
public static boolean isVowel(char c) {
    if ((c == 'a') ||
        (c == 'e') ||
        (c == 'i') ||
        (c == 'o') ||
        (c == 'u'))
        return true;
    else
        return false;
} // end function

// does string end with a double consonent?
protected static boolean endsWithDoubleConsonent(String str) {
if (str.length() < 2) return false;
    char c = str.charAt(str.length() - 1);
    if (c == str.charAt(str.length() - 2))
        if (!containsVowel(str.substring(str.length() - 2))) {
            return true;
    }
    return false;
} // end function

// returns a CVC measure for the string
protected static int stringMeasure(String str) {
    int count = 0;
    boolean vowelSeen = false;
    char[] strchars = str.toCharArray();

    for (int i = 0; i < strchars.length; i++) {
        if (isVowel(strchars[i])) {
            vowelSeen = true;
        } else if (vowelSeen) {
            count++;
            vowelSeen = false;
        }
    } // end for
    return count;
} // end function

// does stem end with CVC?
protected static boolean endsWithCVC (String str) {
    char c, v, c2 = ' ';
    if (str.length() >= 3) {
        c = str.charAt(str.length() - 1);
        v = str.charAt(str.length() - 2);
        c2 = str.charAt(str.length() - 3);
    } else {
        return false;
    }

    if ((c == 'w') || (c == 'x') || (c == 'y')) {
        return false;
    } else if (isVowel(c)) {
        return false;
    } else if (!isVowel(v)) {
        return false;
    } else if (isVowel(c2)) {
        return false;
    } else {
        return true;
    }
} // end function

    
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
				tokens = tokenizeAndStem(runningText);
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
		
		FileOutputStream lexiconMemory = new FileOutputStream("lexiconStemmed.txt");
		ObjectOutputStream oos = new ObjectOutputStream(lexiconMemory);
		oos.writeObject(lexicon);
		oos.close();
		lexiconMemory.close();
		
		FileOutputStream inverseIndexMemory = new FileOutputStream("inverseIndexStemmed.txt");
		ObjectOutputStream oos1 = new ObjectOutputStream(inverseIndexMemory);
		oos1.writeObject(inverseIndex);
		oos1.close();
		inverseIndexMemory.close();
		
		FileOutputStream postingsListMemory = new FileOutputStream("postingsListStemmed.txt");
		ObjectOutputStream oos2 = new ObjectOutputStream(postingsListMemory);
		oos2.writeObject(postingsList);
		oos2.close();
		postingsListMemory.close();
		
		FileOutputStream idMappingMemory = new FileOutputStream("idMappingStemmed.txt");
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