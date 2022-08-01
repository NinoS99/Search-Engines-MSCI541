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


public class BM25RetrievalStemmed{
	
	static HashMap<String, Integer> docWordCount = new HashMap<String, Integer>();
	static HashMap<String, Double> docBMEScore = new HashMap<String, Double >();
	
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
    
    //**** ADD Stimmer Class Here ****
    
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
		
		tokens = tokenizeAndStem(runningText);
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
    						
    						words = tokenizeAndStem(runningText);
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
		String runTag = "nspasikBM25stem";
		
		int tokenID;
		
		while ((queryInput = br.readLine()) != null) {
			
			topicNum = queryInput.substring(0,3);
			queryInput = queryInput.substring(4);
			
    		ArrayList<String> tokens = tokenizeAndStem(queryInput);
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
    	
		FileInputStream fis = new FileInputStream("lexiconStemmed.txt");
		ObjectInputStream ois = new ObjectInputStream(fis);
		@SuppressWarnings("unchecked")
		HashMap<String, Integer> lexicon = (HashMap<String, Integer>) ois.readObject();
		ois.close();
		
		FileInputStream fis1 = new FileInputStream("inverseIndexStemmed.txt");
		ObjectInputStream ois1 = new ObjectInputStream(fis1);
		@SuppressWarnings("unchecked")
		HashMap<Integer, String> inverseIndex = (HashMap<Integer, String>) ois1.readObject();
		ois1.close();
		
		FileInputStream fis2 = new FileInputStream("postingsListStemmed.txt");
		ObjectInputStream ois2 = new ObjectInputStream(fis2);
		@SuppressWarnings("unchecked")
		ArrayList<Integer>[] postingsList  = (ArrayList<Integer>[]) ois2.readObject();
		ois2.close();
		
		FileInputStream fis3 = new FileInputStream("idMappingStemmed.txt");
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