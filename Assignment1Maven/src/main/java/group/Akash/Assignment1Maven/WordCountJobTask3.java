package group.Akash.Assignment1Maven;

//importing required classes
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
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
public class WordCountJobTask3 extends Configured implements Tool {
	   
	   //Mapper Class
	   static public class WordCountMapper extends Mapper<LongWritable, Text, Text, LongWritable> {
		  
		  //variable for logging purposes
		  private static final Logger LOG = Logger.getLogger(WordCountMapper.class);
	      private Text tokenValue = new Text();
	     
	      //defining the associative array as part of the map class (for in-mapper combining with preserving state across documents)
	      private HashMap<String, Integer> wordCounts = null;
	     
	      //setuo function to initialize the hashmap
	      @Override
	      protected void setup(Context context) throws IOException, InterruptedException {
	    	  wordCounts = new HashMap<String, Integer>();
	      }
	      
	      //map function
	      @Override
	      protected void map(LongWritable offset, Text text, Context context) throws IOException, InterruptedException {
	       
	    	  // Set log-level to debugging
		      LOG.setLevel(Level.DEBUG);
		      
		      //printing my name and student id
		      LOG.debug("The mapper task of Akash Sunil Nirantar, s3813209");
		      
	    	  //Split line into tokens
	    	  StringTokenizer st = new StringTokenizer(text.toString());

	    	  //iterating over the tokens
	    	  while(st.hasMoreTokens()) {
	    		  
	    		  String word = st.nextToken();
	    		  
	    		  //try catch block to check if the word exist in the hashmap
	    		  try {
	    			  //increment the count if it exist
	    			  int countOfCurrentWord = (int) wordCounts.get(word);
	    			  wordCounts.put(word, countOfCurrentWord + 1);
	    		  }
	    		  catch(NullPointerException npe) {
	    			  //add the word to hashmap with count 1
	    			  wordCounts.put(word, 1);
	    		  }
	    	  }
	    	  
	      }
	      
	      //cleanup function to pass the hashmap's values to the reducer
	      @Override
	      protected void cleanup(Context context) throws IOException, InterruptedException {
	    	  for(Map.Entry<String, Integer> wordCount: wordCounts.entrySet()) {
	    		  tokenValue.set(wordCount.getKey());
	    		  context.write(tokenValue, new LongWritable((int) wordCount.getValue()));
	    	  }
	    	
	      }
	   }
	   
	   //Reducer class
	   static public class WordCountReducer extends Reducer<Text, LongWritable, Text, LongWritable> {
	      
		  //variable for logging purposes
	      private static final Logger LOG = Logger.getLogger(WordCountReducer.class);
	      private LongWritable total = new LongWritable();
	      
	      @Override
	      protected void reduce(Text token, Iterable<LongWritable> counts, Context context) throws IOException, InterruptedException {
	    	// Set log-level to debugging
		    LOG.setLevel(Level.DEBUG);
		    //printing my name and my student id
		    LOG.debug("The reducer task of Akash Sunil Nirantar, s3813209");
	    	 
		    //variable to save the total count of the word
		    long n = 0;
	        
		    //Calculate sum of counts
		    for (LongWritable count : counts)
		    	n += count.get();
	        
		    //setting the total count value to variable total and sending it for printing in the file
		    total.set(n);
	 
	        context.write(token, total);
	      }
	   }

	   public int run(String[] args) throws Exception {
	      Configuration configuration = getConf();
	      
	      //Initialising Map Reduce Job
	      @SuppressWarnings("deprecation")
		  Job job = new Job(configuration, "Word Count");
	      
	      //Set Map Reduce main jobconf class
	      job.setJarByClass(WordCountJobTask3.class);
	      
	      //Set Mapper class
	      job.setMapperClass(WordCountMapper.class);
	      
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
	      System.exit(ToolRunner.run(new WordCountJobTask3(), args));
	   }
	}
