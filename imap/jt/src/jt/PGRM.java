package jt;

import java.io.IOException;

import org.apache.hadoop.examples.iterative.Util;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.IterativeMapper;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

public class PGRM extends MapReduceBase
		implements
		IterativeMapper<IntWritable, DoubleWritable, IntWritable, Text, IntWritable, DoubleWritable> {
	private FileSystem fs;
	private String subGraphsDir;
	private String subRankDir;
	private int taskid;

	public void configure(JobConf job) {
		try {
			this.fs = FileSystem.get(job);
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.subRankDir = job.get("dir.substate");
		this.subGraphsDir = job.get("dir.substatic");
		this.taskid = Util.getTaskId(job);
	}

	public void map(IntWritable key, DoubleWritable value, IntWritable datakey,
			Text dataval, OutputCollector<IntWritable, DoubleWritable> output,
			Reporter report) throws IOException {
		
		System.out.println(taskid + " k1 : " + key + " : " + value + " dk : " + datakey + " : " + dataval);
		
		double rank = value.get();

		String linkstring = dataval.toString();

		double retain = 0.2D;
		output.collect(key, new DoubleWritable(retain));

		String[] links = linkstring.split(" ");
		double delta = rank * 0.8D / links.length;

		for (String link : links)
			if (!(link.equals("")))
				output.collect(new IntWritable(Integer.parseInt(link)),
						new DoubleWritable(delta));
	}

	public Path[] initStateData() throws IOException {
		Path remotePath = new Path(this.subRankDir + this.taskid);
		Path localPath = new Path("/tmp/imapreduce/statedata" + this.taskid);
		this.fs.copyToLocalFile(remotePath, localPath);
		Path[] paths = new Path[1];
		paths[0] = localPath;
		return paths;
	}

	public Path initStaticData() throws IOException {
		Path remotePath = new Path(this.subGraphsDir + this.taskid);
		Path localPath = new Path("/tmp/imapreduce/staticdata" + this.taskid);
		this.fs.copyToLocalFile(remotePath, localPath);
		return localPath;
	}

	public void map(IntWritable arg0, DoubleWritable arg1,
			OutputCollector<IntWritable, DoubleWritable> arg2, Reporter arg3)
			throws IOException {
	}

	public void iterate() {
	}
}