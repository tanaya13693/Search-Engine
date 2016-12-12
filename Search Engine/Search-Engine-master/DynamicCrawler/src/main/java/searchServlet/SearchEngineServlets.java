package searchServlet;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import indexing.Indexer;
import indexing.ReadDocuments;
import pageRank.PageRank;
import pageRank.WebGraph;
import search.Search;
import search.SearchResult;

/**
 * Servlet implementation class SearchEngineServlets
 */
@WebServlet("/SearchEngineServlets")
public class SearchEngineServlets extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	public static String INDEX_DIR;
	public static String dirPath;
	public static String pageRankPath;
	public static String hadoopIndexPath;
	
	public static int tfidfScoring;
	public static int pageRankScoring;
	public static int tfidfPageRankScoring;
	public static int hadoopBM25Ranking;
	public static ReadDocuments readDoc;
	public static WebGraph graph;
	public static PageRank pr;
	public static Indexer indexer;
	
	
    public SearchEngineServlets() throws Exception, IOException 
    {
    	setFilePaths();
    	//System.out.println("pageRankPath="+pageRankPath);
    	//Search search1 = new Search();
    	File file = new File("D:/WebCrawler/CrawledData2/pageRankValues.txt");
    	PrintWriter writer = null;
    	try {
    		writer = new PrintWriter(file, "UTF-8");
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	readDoc = new ReadDocuments( dirPath );
    	readDoc.readAllFilesFromDir();
    	readDoc.parseDocuments();
    	
    	//PageRank calculation:
    	graph = new WebGraph();
    	//System.out.println(readDoc.urlList.);
    	graph.createWebGrpahOfAllPages( readDoc.filePaths, readDoc.urlList );
    	pr = new PageRank( WebGraph.WebGraph, WebGraph.mInComingLinks);
    	pr.calculatePageRank( writer );
    	
    	//System.out.println("Before hashing");
    	//search1.hashing(hadoopIndexPath);
    	//System.out.println("After hashing");
    	
    	// Indexing process:
    	Path path = Paths.get(INDEX_DIR);
    	boolean isDirectoryEmpty = true;
    	try
    	{
    		 isDirectoryEmpty = isDirEmpty(path);
    	}
    	catch( Exception ex )
    	{
    		System.out.println("Exception in checking index directory");
    	}
    	if(isDirectoryEmpty)
    	{
    		indexer = new Indexer();
    		indexer.createIndex(readDoc.webDocuments );
    	}
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    
    //set the file paths
    public static void setFilePaths(){
    	ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Properties properties = new Properties();
		String value = "";
		
		try {
			properties.load(classLoader.getResourceAsStream("config.properties"));
			//value = properties.getProperty(key);
			INDEX_DIR = properties.getProperty("index");
			dirPath = properties.getProperty("downloadedFiles");
			hadoopIndexPath=properties.getProperty("hadoopindex");
			//pageRankPath = properties.getProperty("pageRank");

			System.out.println(value);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
		
		System.out.println("Hello World");

		String query = request.getParameter("query");
		System.out.println("Query obtained: " + query );
		
		if( query == null || query.equals(""))
		{
			RequestDispatcher rd = request.getRequestDispatcher("/searchPage.jsp");
			rd.forward(request, response);
		}
		else
		{
			String radio1 = request.getParameter("radioOption");
					
			System.out.println(" Radio1: " + radio1 );
			
			if( radio1 != null && radio1.equals( "1" ) )
			{
				tfidfScoring = 1;
				pageRankScoring = 0;
				hadoopBM25Ranking=0;
			}
			else if( radio1 != null && radio1.equals( "2" ) )
			{
				tfidfScoring = 0;
				pageRankScoring = 1;
				hadoopBM25Ranking=0;
			}
			else if( radio1 != null && radio1.equals( "3" ) )
			{
				tfidfScoring = 0;
				pageRankScoring = 0;
				hadoopBM25Ranking=1;
			}
		/*	else if( radio1 != null && radio1.equals( "4" ))
			{
				tfidfScoring = 0;
				pageRankScoring = 0;
				tfidfPageRankScoring =0;
				hadoopBM25Ranking=1;	
			}*/
			
			System.out.println("Before Search");
			
			Search search = new Search();
			//Set<Double> searchResultsSet = new HashSet<Double>();
			List<SearchResult> searchResults2=new ArrayList<>();
			System.out.println("Before Search call");
			List<SearchResult> searchResults = search.search( query, 10, readDoc.filePaths, readDoc.urlList,hadoopIndexPath,dirPath );
			System.out.println("After Search call");
			List<SearchResult> finalSearchResult = new ArrayList<SearchResult>();
			//List<SearchResult> newSearchResults;
			
			if( pageRankScoring == 1)// || tfidfPageRankScoring == 1 )
			{
				searchResults = search.calculateNewScore( searchResults, PageRank.PageRank );
			}
		
			/*
			for( SearchResult srchRes: searchResults )
			{
				if( searchResultsSet.add( srchRes.getScore()))
				{
					finalSearchResult.add( srchRes );
				}
			}*/
			
			for( SearchResult srchRes: searchResults )
			{
				finalSearchResult.add(srchRes);
			}
			
			//Collections.sort(finalSearchResult);
			

			System.out.println("finalSearchResult"+finalSearchResult.size());
			RequestDispatcher rd = request.getRequestDispatcher("/searchResult.jsp");
			request.setAttribute( "searchResults", finalSearchResult );
			rd.forward(request, response);
		}
		
	}
	
	//Check if a directory is empty
	public static boolean isDirEmpty(final Path directory) throws IOException {
		    try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
		        return !dirStream.iterator().hasNext();
		    }
		}

	protected void controllerSearchEngine( String query )
	{
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
