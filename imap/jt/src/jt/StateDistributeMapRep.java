package jt;

import java.io.IOException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

public class StateDistributeMapRep extends MapReduceBase implements
		Mapper<Text, Text, IntWritable, Text> {
	public void map(Text key, Text value,
			OutputCollector<IntWritable, Text> output, Reporter arg3)
			throws IOException {
		int page = Integer.parseInt(key.toString());
		output.collect(new IntWritable(page), value);
	}
}