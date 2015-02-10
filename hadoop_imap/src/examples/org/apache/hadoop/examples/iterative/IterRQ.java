package org.apache.hadoop.examples.iterative;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.IterativeMapper;
import org.apache.hadoop.mapred.IterativeReducer;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.KeyValueTextInputFormat;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Partitioner;
import org.apache.hadoop.mapred.Projector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.lib.HashPartitioner;


public class IterRQ {
    	
	public static class DistributeDataMap extends MapReduceBase implements
		Mapper<Text, Text, Text, Text> {
		
		@Override
		public void map(Text arg0, Text value,
				OutputCollector<Text, Text> arg2, Reporter arg3)
				throws IOException {
		    // TODO prefix elimination!
		    
		    
//			int page = Integer.parseInt(arg0.toString());
		
			arg2.collect(arg0, value);
		}
		
		/*
		private LongWritable outputKey = new LongWritable();
		private Text outputVal = new Text();
		private List<String> tokenList = new ArrayList<String>();

		public void map(LongWritable key, Text value,
				OutputCollector<LongWritable, Text> output, Reporter reporter)
				throws IOException {
			tokenList.clear();

			String line = value.toString();
			StringTokenizer tokenizer = new StringTokenizer(line);

			while (tokenizer.hasMoreTokens()) {
				tokenList.add(tokenizer.nextToken());
			}

			if (tokenList.size() >= 2) {
				outputKey.set(Long.parseLong(tokenList.get(0)));
				outputVal.set(tokenList.get(1).getBytes());
				output.collect(outputKey, outputVal);
			} else if(tokenList.size() == 1){
				//no out link
				outputKey.set(Long.parseLong(tokenList.get(0)));
				output.collect(outputKey, new Text(tokenList.get(0)));
			}
			//System.out.println("output " + outputKey + "\t" + outputVal);
		}
		*/
	}
	
	public static class DistributeDataReduce extends MapReduceBase implements
		Reducer<Text, Text, Text, Text> {
		Random rand = new Random();
		@Override
		public void reduce(Text key, Iterator<Text> values,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			String outputv = "";

			while(values.hasNext()){
				String end = values.next().toString();
//				
//				if(key.get() == Long.parseLong(end)){
//					int randlong = rand.nextInt(Integer.MAX_VALUE);
//					while(randlong == Long.parseLong(end)){
//						randlong = rand.nextInt(Integer.MAX_VALUE);
//					}
//					outputv += randlong + " ";
//				}else{
					outputv += end + " ";
//				}
				
				//outputv += ends + " ";
			}
			
			output.collect(key, new Text(outputv));
			//System.out.println("output " + key + "\t" + outputv);
		}
	}
	
	public static class DistributeDataMap2 extends MapReduceBase implements
		Mapper<Text, Text, Text, Text> {
		
		@Override
		public void map(Text arg0, Text value,
				OutputCollector<Text, Text> arg2, Reporter arg3)
				throws IOException {
			arg2.collect(arg0, value);
		}
	}

	public static class DistributeDataReduce2 extends MapReduceBase implements
		Reducer<Text, Text, Text, Text> {
		Random rand = new Random();
		@Override
		public void reduce(Text key, Iterator<Text> values,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
			while(values.hasNext()){
				output.collect(key, values.next());
			}
		}
	}
	
	public static class RQMap extends MapReduceBase implements
		IterativeMapper<Text, Text, Text, Text, Text, Text> {
	    	public String start_node;
	    	@Override
	    	public void configure(JobConf job) {
	    	start_node = job.get("query");
	    	super.configure(job);
	    	}
	
		@Override
		public void map(Text statickey, Text staticval,
				Text res,
				OutputCollector<Text, Text> output,
				Reporter reporter) throws IOException {
			

			String fnode = statickey.toString();
			if (fnode.equals(start_node)){
				System.out.println("emit " + statickey.toString() + " : " + staticval.toString());
				output.collect(statickey, new Text(staticval.toString()));
				String nodes = staticval.toString();
				String[] nodelist = nodes.split(" ");
				for (String node : nodelist){
					System.out.println("in nodelist emit : " + node);
					output.collect(new Text(node), new Text("-2"));
				}
			}
			else if(res.toString().contains("-2")){ 
				System.out.println("in 2 emit " + start_node.toString() + " : " + staticval.toString());
				output.collect(new Text(start_node.toString()), new Text(staticval.toString()));
				System.out.println("in 2 emit " + start_node.toString() + " : " + statickey.toString());
				output.collect(new Text(start_node.toString()), new Text(statickey.toString()));
				String nodes = staticval.toString();
				String[] nodelist = nodes.split(" ");
				for (String node : nodelist){
					System.out.println("in 2 nodelist emit : " + node);
					output.collect(new Text(node.toString()), new Text("-2"));
				}
			}
			else{
				output.collect(statickey, res);
			}
		}

		@Override
		public Text removeLable() {
			// TODO Auto-generated method stub
			return null;
		}
	
	}
	
	public static class RQReduce extends MapReduceBase implements
		IterativeReducer<Text, Text, Text, Text> {
	    public String start_node;
	    @Override
	    public void configure(JobConf job) {
	    // TODO Auto-generated method stub
		start_node = job.get("query");
	    super.configure(job);
	    }
	
		@Override
		public void reduce(Text key, Iterator<Text> values,
				OutputCollector<Text, Text> output, Reporter report)
				throws IOException {
		    String res = "";
			ArrayList<String> outlist = new ArrayList<String>();
//			System.out.println("k1 : " + key );
			while(values.hasNext()){
				Text v = values.next();
				String[] vs = v.toString().split(" ");
				for(String vsv : vs){
//					System.out.println(vsv);
					if (!outlist.contains(vsv.toString())){
						if(key.toString().equals(start_node) && vsv.toString().trim().equals("-2")) continue;
						if(key.toString().equals(start_node) && vsv.toString().trim().equals(start_node)) continue;
						res += vsv.toString() + " ";
						outlist.add(vsv.toString());
					}
				}
			}
			System.out.println("reduce " + key + " : " + res);
			output.collect(new Text(key.toString()), new Text(res));
		}
		
		@Override
		public float distance(Text key, Text prevV,
			Text currV) throws IOException {
			// TODO Auto-generated method stub
//			return Math.abs(prevV.get() - currV.get());
		    return 0.0f;
		}

		@Override
		public Text removeLable() {
			// TODO Auto-generated method stub
			return null;
		}
	
	}
	
	public static class RQProjector implements Projector<Text, Text, Text> {

		@Override
		public void configure(JobConf job) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Text project(Text statickey) {
			return statickey;
		}

		@Override
		public Text initDynamicV(Text dynamickey) {
			return new Text("-1");
		}

		@Override
		public Partitioner<Text, Text> getDynamicKeyPartitioner() {
			// TODO Auto-generated method stub
			return new HashPartitioner<Text, Text>();
		}

		@Override
		public org.apache.hadoop.mapred.Projector.Type getProjectType() {
			return Projector.Type.ONE2ONE;
		}
	}

	private static void printUsage() {
		System.out.println("RQ [-p partitions] <inStaticDir> <outDir>");
		System.out.println(	"\t-p # of parittions\n" +
							"\t-i snapshot interval\n" +
							"\t-I # of iterations\n" +
							"\t-D initial dynamic path\n" +
							"\t-f input format\n" + 
							"\t-s run preserve job\n"+ 
							"\t-q query node");
	}

	public static int main(String[] args) throws Exception {
		if (args.length < 2) {
			System.out.println("ERROR: Wrong Input Parameters!");
	        printUsage();
	        return -1;
		}
		
		int partitions = 0;
		int interval = 1;
		int max_iterations = Integer.MAX_VALUE;
		String init_dynamic = "";
		String data_format = "";
		boolean preserve = true;
		String query = "";
		
		List<String> other_args = new ArrayList<String>();
		for(int i=0; i < args.length; ++i) {
		      try {
		          if ("-p".equals(args[i])) {
		        	partitions = Integer.parseInt(args[++i]);
		          } else if ("-i".equals(args[i])) {
		        	interval = Integer.parseInt(args[++i]);
		          } else if ("-I".equals(args[i])) {
		        	  max_iterations = Integer.parseInt(args[++i]);
		          } else if ("-D".equals(args[i])) {
		        	  init_dynamic = args[++i];
		          } else if ("-f".equals(args[i])) {
		        	  data_format = args[++i];
		          } else if ("-q".equals(args[i])) {
		        	  query = args[++i];
		          } else if ("-s".equals(args[i])) {
		        	  preserve = Boolean.parseBoolean(args[++i]);
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
		
	    if (other_args.size() < 2) {
		      System.out.println("ERROR: Wrong number of parameters: " +
		                         other_args.size() + ".");
		      printUsage(); return -1;
		}
	    
	    String inStatic = other_args.get(0);
	    String output = other_args.get(1);
		
	    String iteration_id = "recursivequery" + new Date().getTime();
	    
		/**
		 * the initialization job, for partition the data and workload
		 */
	    long initstart = System.currentTimeMillis();
	    
	    JobConf job1 = new JobConf(IterRQ.class);
	    String jobname1 = "RecursiveQuery Init";
	    job1.setJobName(jobname1);
	    
	    job1.set("query", query);
	    
	    job1.setDataDistribution(true);
	    job1.setIterativeAlgorithmID(iteration_id);
	    
	    if(data_format.equals("KeyValueTextInputFormat")){
	    	job1.setInputFormat(KeyValueTextInputFormat.class);
	    	job1.setMapperClass(DistributeDataMap.class);
	    	job1.setReducerClass(DistributeDataReduce.class);
	    }else if(data_format.equals("SequenceFileInputFormat")){
	    	job1.setInputFormat(SequenceFileInputFormat.class);
	    	job1.setMapperClass(DistributeDataMap2.class);
	    	job1.setReducerClass(DistributeDataReduce2.class);
	    }else {
	    	job1.setInputFormat(KeyValueTextInputFormat.class);
	    	job1.setMapperClass(DistributeDataMap.class);
	    	job1.setReducerClass(DistributeDataReduce.class);
	    }
	    
	    job1.setOutputFormat(SequenceFileOutputFormat.class);
	    FileInputFormat.addInputPath(job1, new Path(inStatic));
	    FileOutputFormat.setOutputPath(job1, new Path(output + "/substatic"));

	    job1.setOutputKeyClass(Text.class);
	    job1.setOutputValueClass(Text.class);
	    
	    //new added, the order is strict
	    job1.setProjectorClass(RQProjector.class);
	    
	    /**
	     * if partitions to0 small, which limit the map performance (since map is usually more haveyly loaded),
	     * we should partition the static data into 2*partitions, and copy reduce results to the other mappers in the same scale,
	     * buf first, we just consider the simple case, static data partitions == dynamic data partitions
	     */
	    job1.setNumMapTasks(partitions);
	    job1.setNumReduceTasks(partitions);
	    
	    JobClient.runJob(job1);
	    
	    long initend = System.currentTimeMillis();
		Util.writeLog("iter.rq.log", "init job use " + (initend - initstart)/1000 + " s");
		
	    /**
	     * start iterative application jobs
	     */
	    long itertime = 0;
	    
	    //while(cont && iteration < max_iterations){
    	long iterstart = System.currentTimeMillis();
    	
	    JobConf job = new JobConf(IterRQ.class);
	    String jobname = "Iter RecursiveQuery ";
	    job.setJobName(jobname);
	    
	    job.set("query", query);
    
	    //if(partitions == 0) partitions = Util.getTTNum(job);
	    
	    //set for iterative process   
	    job.setIterative(true);
	    job.setIterativeAlgorithmID(iteration_id);		//must be unique for an iterative algorithm

	    if(max_iterations == Integer.MAX_VALUE){
	    	job.setDistanceThreshold(1);
	    }else{
	    	job.setMaxIterations(max_iterations);
	    }
	    
	    if(init_dynamic == ""){
	    	job.setInitWithFileOrApp(false);
	    }else{
	    	job.setInitWithFileOrApp(true);
	    	job.setInitStatePath(init_dynamic);
	    }
	    job.setStaticDataPath(output + "/substatic");
	    job.setDynamicDataPath(output + "/result");	
	    
	    job.setStaticInputFormat(SequenceFileInputFormat.class);
	    job.setDynamicInputFormat(SequenceFileInputFormat.class);		//MUST have this for the following jobs, even though the first job not need it
    	job.setResultInputFormat(SequenceFileInputFormat.class);		//if set termination check, you have to set this
	    job.setOutputFormat(SequenceFileOutputFormat.class);
	    
	    FileInputFormat.addInputPath(job, new Path(output + "/substatic"));
	    FileOutputFormat.setOutputPath(job, new Path(output + "/result"));
	    
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(Text.class);
	    
	    job.setIterativeMapperClass(RQMap.class);	
	    job.setIterativeReducerClass(RQReduce.class);
	    job.setProjectorClass(RQProjector.class);
	    
	    job.setNumReduceTasks(partitions);			
	    JobClient.runIterativeJob(job);

    	long iterend = System.currentTimeMillis();
    	itertime += (iterend - iterstart) / 1000;
    	Util.writeLog("iter.rq.log", "iteration computation takes " + itertime + " s");
	    	
    	
	    if(preserve){
		    //preserving job
	    	long preservestart = System.currentTimeMillis();
	    	
		    JobConf job2 = new JobConf(IterRQ.class);
		    jobname = "RQ Preserve ";
		    job2.setJobName(jobname);
	    
		    if(partitions == 0) partitions = Util.getTTNum(job2);
		    
		    //set for iterative process   
		    job2.setPreserve(true);
		    job2.setIterativeAlgorithmID(iteration_id);		//must be unique for an iterative algorithm
		    //job2.setIterationNum(iteration);					//iteration numbe
		    job2.setCheckPointInterval(interval);					//checkpoint interval
		    job2.setStaticDataPath(output + "/substatic");
		    job2.setDynamicDataPath(output + "/result/iteration-" + max_iterations);	
		    job2.setStaticInputFormat(SequenceFileInputFormat.class);
		    job2.setDynamicInputFormat(SequenceFileInputFormat.class);		//MUST have this for the following jobs, even though the first job not need it
	    	job2.setResultInputFormat(SequenceFileInputFormat.class);		//if set termination check, you have to set this
		    job2.setOutputFormat(SequenceFileOutputFormat.class);
		    job2.setPreserveStatePath(output + "/preserve");
		    
		    FileInputFormat.addInputPath(job2, new Path(output + "/substatic"));
		    FileOutputFormat.setOutputPath(job2, new Path(output + "/preserve/convergeState"));
		    
		    if(max_iterations == Integer.MAX_VALUE){
		    	job2.setDistanceThreshold(1);
		    }

		    job2.setStaticKeyClass(Text.class);
		    job2.setOutputKeyClass(Text.class);
		    job2.setOutputValueClass(Text.class);
		    
		    job2.setIterativeMapperClass(RQMap.class);	
		    job2.setIterativeReducerClass(RQReduce.class);
		    job2.setProjectorClass(RQProjector.class);
		    
		    job2.setNumReduceTasks(partitions);			

		    JobClient.runIterativeJob(job2);

	    	long preserveend = System.currentTimeMillis();
	    	long preservationtime = (preserveend - preservestart) / 1000;
	    	Util.writeLog("iter.pagerank.log", "iteration preservation takes " + preservationtime + " s");
	    }
		return 0;
	}

}
