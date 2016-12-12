package indexing;

public class WebDocuments 
{
	public String title;
	public String body;
	public String fileName;
	public String metaData;
	public String anchor;
	
	public WebDocuments( String title, String body, String fileName, String metaData, String anchor )
	{
		this.title = title;
		this.body = body;
		this.fileName = fileName;
		this.metaData = metaData;
		this.anchor=anchor;
	}
}
