package jt;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.examples.iterative.Util;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.IFile;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

public class StaticDistributeReduceRep extends MapReduceBase implements
		Reducer<IntWritable, Text, NullWritable, NullWritable> {
	private FSDataOutputStream out;
	private IFile.Writer<IntWritable, Text> writer;

	public void configure(JobConf job) {
		String outDir = job.get("dir.substatic");
		try {
			FileSystem fs = FileSystem.get(job);
			int taskid = Util.getTaskId(job);
			Path outPath = new Path(outDir + taskid);
			this.out = fs.create(outPath);
			this.writer = new IFile.Writer(job, this.out, IntWritable.class,
					Text.class, null, null);
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	public void reduce(IntWritable arg0, Iterator<Text> values,
			OutputCollector<NullWritable, NullWritable> arg2, Reporter arg3)
			throws IOException {
		while (values.hasNext()) {
			Text value = (Text) values.next();
			if(value.toString().trim().equals("")) continue;
			this.writer.append(arg0, value);
			System.out.println(arg0 + " : " + value);
		}
	}

	public void close() {
		try {
			this.writer.close();
			this.out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
