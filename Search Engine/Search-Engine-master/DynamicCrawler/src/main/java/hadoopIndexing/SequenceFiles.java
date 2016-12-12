/*
import java.net.URI;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapreduce.Cluster;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.filecache.DistributedCache;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.util.*;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;

import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
public class SequenceFiles extends Configured implements Tool{
	public static void main(String[] args) throws Exception {

	    int exitcode= ToolRunner.run(new Configuration(), new SequenceDriver(),args);
		System.exit(exitcode);
	    
	  }
	@SuppressWarnings("deprecation")
	@Override
	public int run(String[] args) throws Exception{
		if(args.length!=3){
			System.out.println("Requires three arguments: <input Directory> <Output Directoryfor sequential file> <output for mapreduce>");
			return -1;
		}
			Configuration conf = new Configuration();
        FileSystem fileLists = FileSystem.get(conf);
        Path inputFile = new Path(args[0]);
        Path outputFile = new Path(args[1]+"/outfile1.txt");
        FSDataInputStream inputStream;
        Text key = new Text();
        Text value = new Text();
        SequenceFile.Writer writer = SequenceFile.createWriter(fs, conf,
                outputFile, key.getClass(), value.getClass());
        FileStatus[] files = fileLists.listStatus(inputFile);
		for (FileStatus file : files) {
            String str = "";
            inputStream = fs.open(file.getPath());
            key.set(file.getPath().getName());
            while(inputStream.available()>0) {
                str = str+inputStream.readLine();
            }
            value.set(str);
            writer.append(key, value);

        }
        fs.close();
        IOUtils.closeStream(writer);
		
		Configuration conf = getConf();
		
      Job job = new Job(conf, "indexing");
      job.setJarByClass(PartitionerExample.class);
		
      FileInputFormat.setInputPaths(job, new Path(args[1]));
      FileOutputFormat.setOutputPath(job,new Path(args[2]));
		
      job.setMapperClass(MapClass.class);
	  job.setReducerClass(ReduceClass.class);
      job.setPartitionerClass(CaderPartitioner.class);
	  
	  job.setMapOutputKeyClass(Text.class);
      job.setMapOutputValueClass(Text.class);
      job.setOutputKeyClass(Text.class);
	  job.setOutputValueClass(Text.class);
	
	  job.setInputFormatClass(SequenceFileInputFormat.class);
	  job.setOutputFormatClass(TextOutputFormat.class);
	  job.setNumReduceTasks(26);
		
      System.exit(job.waitForCompletion(true)? 0 : 1);
      return 0;
		return 0;
		
	}
	
}

*/