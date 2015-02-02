package jt;

import java.io.IOException;
import java.util.Random;

import org.apache.hadoop.examples.iterative.Util;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

public class StaticDistributeMapRep extends MapReduceBase implements
		Mapper<Text, Text, IntWritable, Text> {
	private int deadendsCounter;
	private int badCounter;
	private int totalCounter;
	private int expect;
	private int totalPages;
	private int ttnum;
	private OutputCollector<IntWritable, Text> output;
	private int taskid;

	public StaticDistributeMapRep() {
		this.deadendsCounter = 0;
		this.badCounter = 0;
		this.totalCounter = 0;
		this.expect = -1;
		this.totalPages = 0;
		this.ttnum = 0;

		this.taskid = -1;
	}

	public void configure(JobConf job) {
		this.totalPages = job.getInt("total.entries", -1);
		this.ttnum = Util.getTTNum(job);
		this.taskid = Util.getTaskId(job);
	}

	public void map(Text arg0, Text value,
			OutputCollector<IntWritable, Text> arg2, Reporter arg3)
			throws IOException {
		if (this.output == null)
			this.output = arg2;
		String skey = arg0.toString().replaceAll("A", "");
		skey = skey.replaceAll("Z", "");
		int page = Integer.parseInt(skey);

		while ((page > this.expect) && (this.expect != -1)) {
			Random rand = new Random();

			int links = rand.nextInt(10) + 1;
			for (int i = 0; i < links; ++i) {
				int linkTo = rand.nextInt(this.totalPages - 1);
				arg2.collect(new IntWritable(this.expect),
						new Text(String.valueOf(linkTo)));
			}

			System.out.println("error out " + page + " : " + links); 

			this.expect += 1;
			this.deadendsCounter += 1;
			this.totalCounter += 1;
			arg3.setStatus(String.valueOf(this.deadendsCounter) + ":"
					+ String.valueOf(this.badCounter) + ":"
					+ String.valueOf(this.totalCounter));
		}
		System.out.println("normal out " + page + " : " + value );
		arg2.collect(new IntWritable(page), value);
		this.expect = (page + 1);
		this.totalCounter += 1;

		arg3.setStatus(String.valueOf(this.deadendsCounter) + ":"
				+ String.valueOf(this.badCounter) + ":"
				+ String.valueOf(this.totalCounter));
	}

	public void close() {
		int expect = this.totalPages / this.ttnum;
		while ((expect > this.totalCounter) && (this.totalCounter < expect)) {
			try {
				this.output.collect(new IntWritable(this.totalCounter
						* this.ttnum + this.taskid), new Text(""));
			} catch (IOException e) {
				e.printStackTrace();
			}
			this.totalCounter += 1;
		}
	}
}