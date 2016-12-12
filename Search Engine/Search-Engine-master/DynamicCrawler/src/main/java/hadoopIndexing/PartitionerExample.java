/*import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;

import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;

import org.apache.hadoop.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PartitionerExample extends Configured implements Tool
{
	public static class MapClass extends Mapper<Text,Text,Text,Text>
   {
private static Set<String> Stopwords;
	  
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
    }
	static HashMap<String,Integer> mapper=new HashMap<String,Integer>();
      public void map(Text Key, Text value, Context context)throws IOException,InterruptedException
      {
        String docid=Key.toString();
		String line=value.toString();
		org.jsoup.nodes.Document htmlFile = Jsoup.parse(line);
		String metaDataVariable = "";
  	  	try{
			for(org.jsoup.nodes.Element meta : htmlFile.select("meta")) {
  	  	 		metaDataVariable += meta.attr("content") + " ";				
  	  	 	}
  	  	}catch(Exception e1)
		{
  	  	}
		 String titleUrl =htmlFile.title();  	
		Elements links = htmlFile.select( "a[href]" );
		for( Element link : links )
		{
			String url = link.attr("abs:href");
			if( url.toLowerCase().contains(".edu") )
			{
				titleUrl=titleUrl+ link.attr("abs:href"); 
			}	
		}
 	
		String contentVariable = null;
			if( htmlFile.body() != null ){
				contentVariable = htmlFile.body().text();
				}
			else{
			contentVariable = "";
			}
		
	
		mapping(metaDataVariable,2);
		mapping(titleUrl,3);
		mapping(contentVariable,1);
		
		for (Map.Entry<String, Integer> entry : mapper.entrySet()) {
			Text word=new Text();
			word.set((String)entry.getKey());
			context.write(word, new Text ( docid+ " " + String.valueOf(entry.getValue()) ));
		}
		
      }
	  
	  public static void mapping(String metaDataVariable, int score){
		 metaDataVariable=metaDataVariable.replaceAll("[ ](?=[ ])|[^-_,A-Za-z0-9 ]+", " ");
                metaDataVariable=metaDataVariable.replaceAll("[-,]", " ");
                String[] text = metaDataVariable.split(" ");
                for(int i=0;i<text.length;i++)
                {
                        String test=text[i].toLowerCase();
                        if(test.length()!=0){
                        if(Character.isLetter(test.charAt(0))  && !Stopwords.contains(test) ){
                                if(mapper.containsKey(test)){
                                                mapper.put(test, mapper.get(test)+score);
                                        }
                                else{
                                        mapper.put(test, score);
                                }
                        }
                        }
                }
	
	}
   }

   public static class ReduceClass extends Reducer<Text,Text,Text,Text>
   {
      public void reduce(Text key, Iterable <Text> values, Context context) throws IOException, InterruptedException
      {
		StringBuilder value=new StringBuilder();
		for(Text v:values){
			String[] split=v.toString().trim().split(" ");
			value.append(split[0] + " "+split[1]+",");
 		}
		context.write(key, new Text("{"+value.toString()+"}"));
      }
   }

	
   public static class CaderPartitioner extends Partitioner < Text, Text >
   {
      @Override
      public int getPartition(Text key, Text value, int numReduceTasks)
      {
		 char first = key.toString().toLowerCase().charAt(0);
		 return Character.getNumericValue(first) - Character.getNumericValue('a');
      }
   }
 }
*/

