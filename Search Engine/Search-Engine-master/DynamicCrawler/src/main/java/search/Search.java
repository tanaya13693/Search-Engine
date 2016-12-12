package search;

import java.nio.file.Path;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import pageRank.PageRank;
import indexing.Indexer;
import searchServlet.SearchEngineServlets;

public class Search 
{	
/******Hadoop related variables*******/
	private static Set<String> Stopwords;
	public static HashMap<String, HashMap<String, Integer>> indexedData = new HashMap<>();
	public static Map<String, Integer> df =new HashMap<String, Integer>();
	public static Map<String, Integer> qf =new HashMap<String, Integer>();
	public static Map<String, Double> output =new HashMap<String, Double>();
	public static Queue<String> HadoopTopResultsQ = new LinkedList<String>();
	public static Queue<Double> HadoopTopResultsScoresQ = new LinkedList<Double>();
	public static Queue<Integer> HadoopFileNumber = new LinkedList<Integer>();
	org.jsoup.nodes.Document hadoopTextFile;
	
    static {
        Stopwords = new HashSet<String>();
        Stopwords.add("I"); Stopwords.add("a"); 
        Stopwords.add("about"); Stopwords.add("an"); 
        Stopwords.add("are"); Stopwords.add("as");
        Stopwords.add("at"); Stopwords.add("be"); 
        Stopwords.add("by"); Stopwords.add("com"); 
        Stopwords.add("de"); Stopwords.add("en");
        Stopwords.add("for"); Stopwords.add("from"); 
        Stopwords.add("how"); Stopwords.add("in"); 
        Stopwords.add("is"); Stopwords.add("it");
        Stopwords.add("la"); Stopwords.add("of"); 
        Stopwords.add("on"); Stopwords.add("or"); 
        Stopwords.add("that"); Stopwords.add("the");
        Stopwords.add("this"); Stopwords.add("to"); 
        Stopwords.add("was"); Stopwords.add("what"); 
        Stopwords.add("when"); Stopwords.add("where");
        Stopwords.add("who"); Stopwords.add("will"); 
        Stopwords.add("with"); Stopwords.add("and"); 
        Stopwords.add("the"); Stopwords.add("www");
        Stopwords.add("("); Stopwords.add(")");
        Stopwords.add("{"); Stopwords.add("}");
        Stopwords.add("})"); Stopwords.add("width");
        Stopwords.add("margin"); Stopwords.add("color");
        Stopwords.add("background"); Stopwords.add("border");
        Stopwords.add("id="); Stopwords.add("http");
    }//End of static variable
	/*************************************/

	public  List<SearchResult> search (String queryString, int topk, ArrayList<String> filePaths, ArrayList<String> urls, String hadoopIndexPath,String dirPath ) 
	{
		List<SearchResult> searchResults = new ArrayList<SearchResult>();
		ScoreDoc[] results;
		try
		{
			SearchResult srchResult;
			Path path = Paths.get( SearchEngineServlets.INDEX_DIR );
			
			IndexReader indexReader = DirectoryReader.open(FSDirectory.open( path ));
			
			IndexSearcher indexSearcher = new IndexSearcher( indexReader );
			QueryParser queryparser = new QueryParser( "text", new StandardAnalyzer() );
			TopScoreDocCollector collector;
		
			StringTokenizer strtok = new StringTokenizer(queryString, " ~`!@#$%^&*()_-+={[}]|:;'<>,./?\"\'\\/\n\t\b\f\r");
			String querytoparse = "";
			
			if( SearchEngineServlets.tfidfScoring == 1)
			{
				while(strtok.hasMoreElements()) 
				{
					String token = strtok.nextToken();
					querytoparse += "text:" + token + "^1" + "title:" + token+ "^1.5" + "meta:" + token+ "^1.5";
				}
			}
			else if( SearchEngineServlets.pageRankScoring == 1)
			{
				while(strtok.hasMoreElements()) 
				{
					String token = strtok.nextToken();
					querytoparse += "text:" + token + "^1" + "title:" + token+ "^1" + "meta:" + token+ "^1";
				}
			}	
			else if( SearchEngineServlets.hadoopBM25Ranking ==1)
			{
				System.out.println("Calculate hadoop index");
				hashing(hadoopIndexPath);
				Ranking(queryString, dirPath, urls);
				System.out.println("Queue size="+HadoopTopResultsQ.size());
				
				while(!HadoopTopResultsQ.isEmpty() && HadoopTopResultsQ.size()<=10){
					System.out.println("Queue size="+HadoopTopResultsQ.size());
					String pth ="";
					int num = 0;
					String hadoopFileName=	HadoopTopResultsQ.poll();
					Double s=	HadoopTopResultsScoresQ.poll();
					String hadoopURL=urls.get(num);
					pth = (dirPath+"/"+hadoopFileName);
					File f = new File(pth);
					hadoopTextFile = Jsoup.parse(f, "UTF-8", "");
					String t = hadoopTextFile.title();
					System.out.println("hadoop file title is: "+t);
					
					String[] str = hadoopFileName.split( "[.]");
					int r = Integer.parseInt(str[0]);
					num=r-1;
					System.out.println("URL Number is: "+ num+"Parsed string is: "+str);
					
					Elements links = hadoopTextFile.select( "a[href]" );
				 	String urlVariable = "";
				 	int VarCount=0;
				 	for( Element link : links )
					{
				 		if(VarCount!=0)
				 			break;
						String url = link.attr("abs:href");
						if( url.toLowerCase().contains(".edu") )
						{
							urlVariable = link.attr("abs:href");  //System.out.println(" url: " + link.attr("abs:href") );
							//System.out.println(urlVariable);
							VarCount=VarCount+1;
						}	
						//System.out.println(hadoo);
					}

					String snippet = generateSnippet(queryString , hadoopFileName,dirPath);
					
					srchResult = new SearchResult(hadoopFileName,urlVariable,t,snippet,s); // urls.get(num),t,snippet,s);
					searchResults.add( srchResult );
				}
				System.out.println("searchResults size="+searchResults.size());
				return searchResults;
			}
			
			Query query = queryparser.parse( querytoparse );
			collector = TopScoreDocCollector.create( 10 );
			indexSearcher.search( query, collector );
			results = collector.topDocs().scoreDocs; 
			
			System.out.println( "Score Length: " + results.length + " Results Found\n" );	
 
			for(int i=0; i<results.length; i++)
			{
				int docID = results[i].doc;
				String filename = "";
				Document resultDoc = indexSearcher.doc( docID );
				Field field = (Field) resultDoc.getField( "fileName" );
					
				//String filename = resultDoc.getField( "fileName" ).stringValue();
				if( field != null )
				{
					filename = field.stringValue();
					System.out.println( filename );
				}
				String snippetToShow = generateSnippet(queryString , resultDoc.get( "fileName" ),dirPath);
				String strFileNum = filename.replaceAll( "[^0-9]", "");
		
				int fileNumber = Integer.parseInt( strFileNum );
				fileNumber = fileNumber-1;
		
				if(fileNumber<2237){
				String pth = (dirPath+"/"+filename);
				File f = new File(pth);
	
				System.out.println("Before Element links");
				hadoopTextFile = Jsoup.parse(f, "UTF-8", "");
				Elements links = hadoopTextFile.select( "a[href]" );
			 	String urlVariable = "";
			 	int VarCount=0;
			 	for( Element link : links )
				{
			 		//System.out.println("Before Element links,inside for loop");
			 		if(VarCount!=0)
			 			break;
					String url = link.attr("abs:href");
					if( url.toLowerCase().contains(".edu") )
					{
						urlVariable = link.attr("abs:href");  //System.out.println(" url: " + link.attr("abs:href") );
						//System.out.println(urlVariable);
						VarCount=VarCount+1;
					}	
				}//End of element for
			 	
				srchResult = new SearchResult( resultDoc.get( "fileName" ), urlVariable, resultDoc.get( "title" ), snippetToShow, results[i].score );
				searchResults.add( srchResult );
				System.out.println(snippetToShow+"\n");
			
				}
				else
					break;
			}//End of for
	
			if( SearchEngineServlets.pageRankScoring == 1 || SearchEngineServlets.tfidfPageRankScoring == 1 )
			{
				
			}
			return searchResults;			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
		finally 
		{
			//indexSearcher.close();
		}
		
		return null;
	}
	
	public List<SearchResult> calculateNewScore( List<SearchResult> searchResults, Map<String, Float> pageRank)
	{
		String url = "";
		double score = (float) 0.0;
		double pr = (float) 0.0;
		List<SearchResult> newScoreResult = new ArrayList<SearchResult>();
		
		for( SearchResult sr : searchResults )
		{
			url = sr.url;
			if( pageRank.get( url ) != null)
			{
				score = sr.score;
				pr = pageRank.get(url);
				score = score * pr;
				
				SearchResult nsr = new SearchResult( sr.fileName, sr.url, sr.title, sr.snippet, score );
				newScoreResult.add( nsr );
			}
		}
		
		Collections.sort( newScoreResult, new SearchResultUtil() );
		return newScoreResult;
	}

	//Generate the snippet for a given document, 
public static String generateSnippet(String queryString , String fileName, String dirPath){
		
		int snippetStartingIndex = 0;
		int snippetEndIndex = 0;
		String fileLocation=dirPath+"/"+fileName;
		
		//Modify this line to show the No of Characters to be present in the Snippet (chars twice this value will be shown)
		int CharsCount = 150;

		String htmlSnippet = "";
		String body = "";
		org.jsoup.nodes.Document htmlFile;
		File file = new File(fileLocation);
		
		try{
			htmlFile = Jsoup.parse(file, "UTF-8", "");
			if (htmlFile.body() != null)
				 body = htmlFile.body().text();
		} catch (IOException ex) {
			System.out.println("Exception in JSoup parsing");
		}
		
		
		//String documentBody = resultDoc.getField("text").stringValue();
		int termPosition = body.toLowerCase().indexOf(queryString.toLowerCase());
		
		//If the entire query is not present, use the first term from query
		if(termPosition==-1){
			String firstTerm = "";
			String queryTokens[] = queryString.split(" ");
			if(queryTokens.length >1)
				firstTerm = queryTokens[0];	
			termPosition = body.toLowerCase().indexOf(firstTerm.toLowerCase());
		}
		
		//If still the term is not found
		if(termPosition==-1 && body.length()>CharsCount*2){
			snippetStartingIndex = 0;
			snippetEndIndex = CharsCount*2;
			htmlSnippet = body.substring(snippetStartingIndex, snippetEndIndex); 
			return htmlSnippet;
		}
			
		//If the search term is appearing in the beginning of the document
		if(termPosition < CharsCount && body.length()>CharsCount*2){
			snippetStartingIndex = 0;
			snippetEndIndex = CharsCount*2;
			htmlSnippet = body.substring(snippetStartingIndex, snippetEndIndex); 
		}
		//If the search term is appearing at the end of the document
		else if(body.length()-termPosition <CharsCount && body.length()>CharsCount*2){
			snippetStartingIndex = body.length() - (CharsCount*2);
			snippetEndIndex = body.length()-1;
			htmlSnippet = body.substring(snippetStartingIndex, snippetEndIndex); 
		}
		//If the search term is appearing somewhere in the middle of the document 
		else if(body.length()>CharsCount*2){
			snippetStartingIndex = termPosition - CharsCount;
			snippetEndIndex = termPosition + CharsCount;
			htmlSnippet = body.substring(snippetStartingIndex, snippetEndIndex); 
		}
		//In case of error or Document length not sufficient
		else 
			htmlSnippet = body.substring(snippetStartingIndex, snippetEndIndex);//htmlSnippet.getField("text").stringValue(); //Piyush
		
		//Format the html snippet
		if(htmlSnippet.length()> 1){
			 String [] arr = htmlSnippet.split(Pattern.quote("."), 2);
			 if(arr.length > 1)
				 htmlSnippet =  arr[1];
			 htmlSnippet = htmlSnippet.trim();
		}
		return htmlSnippet;
	}
		//Hashing function:
		public static void hashing(String filepath) throws NumberFormatException, IOException {
			
			System.out.println("filepath is: "+filepath);
			try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
				for (String line; (line = br.readLine()) != null;) {
		
					//System.out.println("line is: "+line);
					HashMap<String, Integer> submap = new HashMap<>();
		
					int count = line.length() - line.replace(",", "").length();
		
					while (count-- > 0) {
						
						String subK = line.split("\\{")[1].split(",")[count]
								.split(" ")[0];
						//System.out.println("subK is: "+subK);
						int subV = Integer
								.valueOf(line.split("\\{")[1].split(",")[count]
										.split(" ")[1]);
						//System.out.println("subV is: "+subV);
						submap.put(subK, subV);
					}
					//System.out.println(line.split("\t")[0]);
					indexedData.put(line.split("\t")[0], submap);
				}
			}
			
		}//End of hashing function

		//Ranking Function:
		private static void Ranking(String q, String dirPath, ArrayList<String> urls) {	
			int r = 0;
			PriorityQueue<Rank> queue = new PriorityQueue<Rank>(1000000, new Comparator<Rank>() {			   
				@Override
				public int compare(Rank o1, Rank o2) {
					if (o1.getRank() > o2.getRank()) return -1;
			        if (o1.equals(o2)) return 0;
			        return +1;					
				}
			});
			System.out.println("Outside priority queue declaration");
			q=q.toLowerCase();
			String[] query = q.split(" ");
			for(int i=0;i<query.length;i++)
			{
				System.out.println(query[i]);
				if(!Stopwords.contains(query[i])){
					System.out.println("inside stop loop");
				if(qf.containsKey(query[i])){
					System.out.println("inside if loop");
					qf.put(query[i], qf.get(query[i]) + 1);
				}
				else{
					System.out.println("inside else looop");
					qf.put(query[i],1);
					System.out.println(indexedData.containsKey(query[i]));
					if(indexedData.containsKey(query[i])){
						
						df.put(query[i], indexedData.get(query[i]).size());
						
						System.out.println(df.get(query[i]));
						HashMap<String, Integer> temp = indexedData.get(query[i]);
						for(String key: temp.keySet()){
							output.put(key,0.0);
							System.out.println("output key:" + key);
						}
							
					}
				}	
				}
				//System.out.println("Ranking: end of first for");
			}
			for(String key:output.keySet()){
				System.out.println("key is : "+ key);
				double score=0;
				double dlavdl =0.9;
				double k1 =1.2;
				double k2 = 100;
				double k = 1.11;
				int N= 20;
				for(int i=0;i<query.length;i++){
					if(indexedData.containsKey(query[i])){
						if(indexedData.get(query[i])!=null && indexedData.get(query[i]).get(key)!=null &&!Stopwords.contains(query[i]) ){
							int tf=indexedData.get(query[i]).get(key);
							int Df=df.get(query[i]);
							int Qf=qf.get(query[i]);
							score=score+Math.abs((Math.log((N-Df+0.5)/(Df+0.5))*(k1+1)*tf*(k2+1)*Qf)/((k+tf)*(k2+Qf)));
						}
					}
					else
						score=0.01;
					
				}
					
				
				String[] str1 = key.split( "[.]");
				int r1 = Integer.parseInt(str1[0]);
				int num1=r1-1;
				
				float pagerank= PageRank.PageRank.get(urls.get(num1));
				Rank rank = new Rank(score+pagerank, key);
				queue.add(rank);
			}
			System.out.println("Ranking: outside second for loop");
			while(!queue.isEmpty())
			{
				Rank b = queue.poll();
				if(!(b.getRank()==0)){
				HadoopTopResultsQ.offer(b.getFile_id());
				HadoopTopResultsScoresQ.offer(b.getRank());
				}
				System.out.println("the text file is  "+ b.getFile_id()+"  the rank is  "+ b.getRank());
			}	
			System.out.println("End of ranking");
		}//End of ranking function
}
