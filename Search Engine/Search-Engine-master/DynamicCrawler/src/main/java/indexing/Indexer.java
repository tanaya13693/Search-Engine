package indexing;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import searchServlet.SearchEngineServlets;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;

public class Indexer 
{
	Analyzer defaultAnalyzer = new StandardAnalyzer();
	
	public void createIndex( ArrayList<WebDocuments> pages )
	{
		System.out.println("-------Creating Index-----------");
		IndexWriter writer 	= null;
		File index 			= new File( SearchEngineServlets.INDEX_DIR );
		Document luceneDoc;
		try
		{
			//Before creating the index, delete any previous indexing files
			FileUtils.cleanDirectory(new File( SearchEngineServlets.INDEX_DIR ) );
			
			IndexWriterConfig indexConfig = new IndexWriterConfig( defaultAnalyzer );
			Path path = Paths.get( SearchEngineServlets.INDEX_DIR );
			try {
				writer = new IndexWriter( FSDirectory.open( path ), indexConfig);
			} catch(Exception ex) {System.out.println(ex.toString());}
			
			System.out.println("Indexing to direcotry: " + index + " .." );
			
			for( WebDocuments page: pages )
			{
				luceneDoc = new Document();
			
				Map<String,Analyzer> analyzerPerField = new HashMap<String,Analyzer>();
				analyzerPerField.put("title", new KeywordAnalyzer());
				analyzerPerField.put("meta", new KeywordAnalyzer());
				analyzerPerField.put("anchor", new KeywordAnalyzer());
				PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(defaultAnalyzer,analyzerPerField);
				
				luceneDoc.add(new TextField("title", null, Store.YES));
				luceneDoc.add(new TextField("meta", null, Store.YES));
				luceneDoc.add(new TextField("anchor", null, Store.YES));
				writer.addDocument(luceneDoc);
				
			}
		}
		catch( Exception ex )
		{
			System.out.println("Exception in Creating Index");
		}
		finally 
		{
			if (writer !=null)
				try 
				{
					writer.commit();
					writer.close();
				} 
				catch (CorruptIndexException e) 
				{
					e.printStackTrace();
				} catch (IOException e) 
				{
					e.printStackTrace();
				}
		}
	}
}

