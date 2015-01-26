package jt;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Iterator;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapred.IterativeReducer;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;


public class PGRR extends MapReduceBase
		implements
		IterativeReducer<IntWritable, DoubleWritable, IntWritable, DoubleWritable> {
	private Date start;
	private int iteration;

	public void configure(JobConf conf) {
		this.start = new Date();
		this.iteration = 0;
	}

	public void reduce(IntWritable key, Iterator<DoubleWritable> values,
			OutputCollector<IntWritable, DoubleWritable> output, Reporter report)
			throws IOException {
		double rank = 0.0D;
		while (values.hasNext()) {
			double v = ((DoubleWritable) values.next()).get();
			if (v != -1.0D)
				;
			rank += v;
		}

		rank = 0.2D + rank * 0.8D;
		output.collect(key, new DoubleWritable(rank));
	}

	public void iterate() {
		this.iteration += 1;
		Date current = new Date();
		long passed = (current.getTime() - this.start.getTime()) / 1000L;
		System.out.println("iteration " + this.iteration + " timepassed "
				+ passed);
	}
}