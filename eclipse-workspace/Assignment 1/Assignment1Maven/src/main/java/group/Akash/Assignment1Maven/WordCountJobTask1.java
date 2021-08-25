package group.Akash.Assignment1Maven;

//importing the required packages
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

//root class used to execute the map reduce code
public class WordCountJobTask1 extends Configured implements Tool {
	
	//Mapper Class
	public static class WordCountMapper extends Mapper<LongWritable, Text, Text, LongWritable> {
	   
		  //variable for logging purposes
		  private static final Logger LOG = Logger.getLogger(WordCountMapper.class);
		  
		  //variables to count the key and pass the key to partitioner
		  final private static LongWritable ONE = new LongWritable(1);
	      private Text tokenValue = new Text();

	      @Override
	      protected void map(LongWritable offset, Text text, Context context) throws IOException, InterruptedException {
	       
	    	  // Set log-level to debugging
		      LOG.setLevel(Level.DEBUG);
		      
		      //printing the following in syslog
		      LOG.debug("The mapper task of Akash Sunil Nirantar, s3813209");
	    	  
	    	  //Splitting line into tokens
	    	  StringTokenizer st = new StringTokenizer(text.toString());
	    	  
	    	  //iterating over the tokens
	    	  while(st.hasMoreTokens()) {
	    		  String word = st.nextToken();

	    		  //checking the word length and adding the count accordingly
	    		  
	    		  //for short words
	    		  if(word.length() >= 1 && word.length() <= 4) {
	    			  tokenValue.set("Short words");
		    		  
		    		  context.write(tokenValue, ONE);
			    	   
			      }
	    		  
	    		  //for medium words
	    		  else if(word.length() >= 5 && word.length() <= 7) {
	    			  tokenValue.set("Medium words");
		    		  
		    		  context.write(tokenValue, ONE);
	    		  }
	    		  
	    		  //for long words
	    		  else if(word.length() >= 8 && word.length() <= 10) {
	    			  tokenValue.set("Long words");
		    		  
		    		  context.write(tokenValue, ONE);
	    		  }
	    		  
	    		  //for extra-long words
	    		  else if(word.length() > 10) {
	    			  tokenValue.set("Extra-long words");
		    		  
		    		  context.write(tokenValue, ONE);
	    		  }
	    		
	    	  }
	      }
	   }
	   
	   //Partitioner class
	   public static class WordCountPartitioner extends Partitioner <Text, LongWritable>
	   {
		   //variable for logging purpose
		   private static final Logger LOG = Logger.getLogger(WordCountPartitioner.class);
		   
		   @Override
		   public int getPartition(Text key, LongWritable value, int numReduceTasks)
		   {
			   // Set log-level to debugging
		       LOG.setLevel(Level.DEBUG);
		       
		       //if there are no reducer tasks - redirect the traffic to the one and only reducer
		       if(numReduceTasks == 0)
		       {
		    	   LOG.debug("No partitioning - only ONE reducer");
		    	   return 0;
		       }
		       
		       //if the token is "Short words" or "Medium words" redirect it to reducer 1
		       if(key.toString().equals("Short words") || key.toString().equals("Medium words")) {
		    	   LOG.debug("Redirecting " + key + " to Partition 0");
		    	   return 0;
		       }
		       
		       //otherwise redirect everything to reducer 2
		       else {
		    	   LOG.debug("Redirecting " + key + " to Partition 1");
		    	   return 1;
		       }
		       
		   }
		   
		  
		   
	   }
	   
	   //Reducer class
	   static public class WordCountReducer extends Reducer<Text, LongWritable, Text, LongWritable> {
		  
		  //variable for logging purpose
	      private static final Logger LOG = Logger.getLogger(WordCountReducer.class);
	      
	      //variable for writing the output into the file
		  private LongWritable total = new LongWritable();
	      
		  @Override
	      protected void reduce(Text token, Iterable<LongWritable> counts, Context context)
	            throws IOException, InterruptedException {
	    	 
	    	 // Set log-level to debugging
		     LOG.setLevel(Level.DEBUG);
		     
		     //logging my name and student id
		     LOG.debug("The reducer task of Akash Sunil Nirantar, s3813209");
	         
		     //variable to count all the occurrences of all types of words
		     long n = 0;
	         
		     //Calculate sum of counts
	         for (LongWritable count : counts)
	            n += count.get();
	         total.set(n);
	 
	         context.write(token, total);
	      }
	   }

	   public int run(String[] args) throws Exception {
	      Configuration configuration = getConf();
	      
	      //Initializing Map Reduce Job
	      @SuppressWarnings("deprecation")
	      Job job = new Job(configuration, "Word Count");
	      
	      //Set Map Reduce main job conf class
	      job.setJarByClass(WordCountJobTask1.class);
	      
	      //Set Mapper class
	      job.setMapperClass(WordCountMapper.class);
	      
	      //Set Partitioner class
	      job.setPartitionerClass(WordCountPartitioner.class);
	      
	      //defining the number of reducers
	      job.setNumReduceTasks(2);
	      
	      //Set Combiner class
	      job.setCombinerClass(WordCountReducer.class);
	      
	      //set Reducer class
	      job.setReducerClass(WordCountReducer.class);
	      
	      //set Input Format
	      job.setInputFormatClass(TextInputFormat.class);
	      
	      //set Output Format
	      job.setOutputFormatClass(TextOutputFormat.class);

	      //set Output key class
	      job.setOutputKeyClass(Text.class);
	      
	      //set Output value class
	      job.setOutputValueClass(LongWritable.class);

	      //setting the input file path and the output path
	      FileInputFormat.setInputPaths(job, new Path(args[0]));
	      FileOutputFormat.setOutputPath(job,new Path(args[1]));

	      return job.waitForCompletion(true) ? 0 : -1;
	   }

	   public static void main(String[] args) throws Exception {
	      System.exit(ToolRunner.run(new WordCountJobTask1(), args));
	   }
	}
