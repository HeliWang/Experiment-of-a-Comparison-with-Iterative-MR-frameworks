package jt;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeMap;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.mapred.lib.MultipleInputs;
import org.apache.hadoop.mapred.lib.MultipleOutputs;


public class DGtoAL {
	public static class DAMapper extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
		@Override
		public void map(LongWritable key, Text value,
				OutputCollector<Text, Text> col, Reporter rep)
				throws IOException {
			String line = value.toString();
//			System.out.println(line);
			if(!line.startsWith("#")){
				String[] spline = line.split("\t");
				col.collect(new Text(spline[0]), new Text(spline[1]));
			}
		}
	}
	public static class DAReducer extends MapReduceBase implements Reducer<Text, Text, Text, Text>{
		private TreeMap<Text, Text> countMap = new TreeMap<Text, Text>();

		private MultipleOutputs output;

		private Reporter rep;
		@Override
		public void configure(JobConf job) {	
			super.configure(job);
			output = new MultipleOutputs(job);
		}
		
		@Override
		public void reduce(Text key, Iterator<Text> vals,
				OutputCollector<Text, Text> col, Reporter rep)
				throws IOException {
			this.rep = rep;
			String res = "";
			while(vals.hasNext()){
				res += vals.next().toString() + " ";
			}
			countMap.put(key, new Text(res));
		}
		@Override
		public void close() throws IOException {
			
			for(Text key : countMap.descendingKeySet())
			{
				output.getCollector("graph", rep).collect(key, countMap.get(key));
				output.getCollector("rank", rep).collect(key, new Text("1"));
			}
			super.close();
		}
	}
	public static void main(String[] args) throws IOException {
		String input = args[0];
		String output = args[1];
		JobConf conf = new JobConf(DGtoAL.class);
		conf.setJobName("DtoA");
		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);
		
		conf.setMapperClass(DAMapper.class);
		conf.setReducerClass(DAReducer.class);
//		conf.setNumReduceTasks(1);
		
		MultipleOutputs.addNamedOutput(conf, "graph", TextOutputFormat.class, Text.class, Text.class);
		MultipleOutputs.addNamedOutput(conf, "rank", TextOutputFormat.class, Text.class, Text.class);
		
		MultipleInputs.addInputPath(conf, new Path(input), TextInputFormat.class, DAMapper.class);
		FileOutputFormat.setOutputPath(conf, new Path(output));
		JobClient.runJob(conf);

	}
}
