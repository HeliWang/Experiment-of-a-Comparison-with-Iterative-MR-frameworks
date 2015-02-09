package jt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.examples.iterative.Common;
import org.apache.hadoop.examples.iterative.Util;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class DQuery extends Configured implements Tool {
	private int partitions = 0;
	private int interval = 10;
	private int iterations = 50;
	private int nodes = 1000000;
	
	private String start_node = "";
	

	private void preprocess(String instate, String instatic) throws Exception {
		String[] args = new String[6];
		
		//<in_state> <in_static> <valClass> <pages> <partitions>
		args[0] = instate;
		args[1] = instatic;
		args[2] = "Text";
		args[3] = String.valueOf(nodes);
		args[4] = String.valueOf(partitions);
        args[5] = String.valueOf(true);

		
		for(String ar:args){ System.out.println(ar);}

		ToolRunner.run(new Configuration(), new PreProcessRep(), args);
	}
	
	private int dquery(String input, String output) throws IOException{
	    JobConf job = new JobConf(getConf());
	    String jobname = "dquery";
	    job.setJobName(jobname);
	    

		job.set("dquery.start_node", this.start_node);
       
	    job.set(Common.SUBSTATE, Common.SUBSTATE_DIR);
	    job.set(Common.SUBSTATIC, Common.SUBSTATIC_DIR);
	    
	    FileInputFormat.addInputPath(job, new Path(input));
	    FileOutputFormat.setOutputPath(job, new Path(output));
	    
	    if(partitions == 0) partitions = Util.getTTNum(job);
	    
	    //set for iterative process   
	    job.setBoolean("mapred.job.iterative", true);  
	    job.setBoolean("mapred.iterative.reducesync", true);
	    job.set("mapred.iterative.jointype", "one2one");
	    
	    job.set("mapred.map.java.opt", "-Xmx512m -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8888");
	    job.set("mapred.reduce.java.opt", "-Xmx512m -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8889");

//		job.set("mapred.iterative.jointype", "one2all");
		
	    job.setInt("mapred.iterative.partitions", partitions);
	    job.setInt("mapred.iterative.snapshot.interval", interval);
	    job.setInt("mapred.iterative.stop.iteration", iterations);
	    
	    job.setJarByClass(DQuery.class);
	    job.setOutputFormat(TextOutputFormat.class);
	    job.setMapperClass(DQueryMapper.class);	
	    job.setReducerClass(DQueryReducer.class);
	    job.setDataKeyClass(IntWritable.class);				//static data key class
	    job.setDataValClass(Text.class);					//static data value class
	    job.setMapOutputKeyClass(IntWritable.class);
	    job.setMapOutputValueClass(Text.class);
	    job.setOutputKeyClass(IntWritable.class);
	    job.setOutputValueClass(Text.class);
	    job.setPartitionerClass(UniDistIntPartitionerRep.class);

	    job.setNumMapTasks(partitions);
	    job.setNumReduceTasks(partitions);
	    
	    JobClient.runJob(job);
	    return 0;
	}
	private void printUsage() {
		System.out.println("dquery [-p partitions] <InTemp> <inStateDir> <inStaticDir> <outDir>");
		System.out.println(	"\t-p # of parittions\n" +
							"\t-I # of iterations\n" +
							"\t-n # of nodes\n" +
							"\t-q query" 
							);
		ToolRunner.printGenericCommandUsage(System.out);
	}

	@Override
	public int run(String[] args) throws Exception {
		if (args.length < 4) {
			printUsage();
			return -1;
		}
		
		List<String> other_args = new ArrayList<String>();
		for(int i=0; i < args.length; ++i) {
		      try {
		          if ("-p".equals(args[i])) {
		        	partitions = Integer.parseInt(args[++i]);
		          } else if ("-i".equals(args[i])) {
		        	interval = Integer.parseInt(args[++i]);
		          } else if ("-I".equals(args[i])) {
		        	iterations = Integer.parseInt(args[++i]);
		          } else if ("-n".equals(args[i])) {
		        	nodes = Integer.parseInt(args[++i]);
		          } else if ("-q".equals(args[i])) {
		        	start_node = args[++i];
		          } else {
		    		  other_args.add(args[i]);
		    	  }
		      } catch (NumberFormatException except) {
		        System.out.println("ERROR: Integer expected instead of " + args[i]);
		        printUsage();
		        return -1;
		      } catch (ArrayIndexOutOfBoundsException except) {
		        System.out.println("ERROR: Required parameter missing from " +
		                           args[i-1]);
		        printUsage();
		        return -1;
		      }
		}
		
	    if (other_args.size() < 4) {
		      System.out.println("ERROR: Wrong number of parameters: " +
		                         other_args.size() + ".");
		      printUsage(); return -1;
		}
	    
//	    for (String arg : other_args){
//	    	System.out.println(arg);
//	    }
		String input = other_args.get(1);
	    String instate = other_args.get(2);
	    String instatic = other_args.get(3); 
	    String output = other_args.get(4);
	    
	    
	    preprocess(instate, instatic);
	    dquery(input, output);
	    
		return 0;

	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new DQuery(), args);
	    System.exit(res);
	}
}
