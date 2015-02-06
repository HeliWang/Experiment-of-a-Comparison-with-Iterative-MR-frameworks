package jt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.IterativeReducer;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

public class DQueryReducer extends MapReduceBase implements 
IterativeReducer<IntWritable, Text, IntWritable, Text>{


	private Date start;
	private int iteration;
	private String start_node;
	
	@Override
	public void configure(JobConf conf){
		start = new Date();
		iteration = 0;
		
		start_node = conf.get("dquery.start_node");

	}
	
	@Override
	public void reduce(IntWritable key, Iterator<Text> values,
			OutputCollector<IntWritable, Text> output, Reporter report)
			throws IOException {
//		System.out.println("k1 : " + key );
		int min = Integer.MAX_VALUE;
		while(values.hasNext()){
			Text v = values.next();
			String[] vs = v.toString().split(" ");
			for(String vsv : vs){
//				System.out.println(vsv);
				if (Integer.parseInt(vsv) < min){
					min = Integer.parseInt(vsv);
				}
			}
		}
		min += iteration;
		System.out.println("reduce " + key + " : " + min);
		output.collect(new IntWritable(Integer.parseInt(key.toString())), new Text(""+min));
	}
	
	@Override
	public void iterate(){
		iteration++;
		Date current = new Date();
		long passed = (current.getTime() - start.getTime()) / 1000;				
		System.out.println("iteration " + iteration + " timepassed " + passed);	
	}
}
