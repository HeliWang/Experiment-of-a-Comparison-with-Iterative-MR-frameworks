package jt;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.examples.iterative.Util;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.IFile;
import org.apache.hadoop.mapred.IFile.Writer;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

public class StateDistributeReduceRep extends MapReduceBase implements
		Reducer<IntWritable, Text, NullWritable, NullWritable> {
	private FSDataOutputStream out;
	private IFile.Writer<IntWritable, IntWritable> intWriter;
	private IFile.Writer<IntWritable, DoubleWritable> doubleWriter;
	private IFile.Writer<IntWritable, FloatWritable> floatWriter;
	private IFile.Writer<IntWritable, Text> textWriter;
	private String valClass;

	public void configure(JobConf job) {
		String outDir = job.get("dir.substate");
		this.valClass = job.get("data.val.class");
		try {
			FileSystem fs = FileSystem.get(job);
			int taskid = Util.getTaskId(job);
			Path outPath = new Path(outDir + taskid);
			this.out = fs.create(outPath);

			if (this.valClass.equals("IntWritable")) {
				this.intWriter = new IFile.Writer<IntWritable, IntWritable>(job, this.out,
						IntWritable.class, IntWritable.class, null, null);
			} else if (this.valClass.equals("DoubleWritable")) {
				this.doubleWriter = new IFile.Writer<IntWritable, DoubleWritable>(job, this.out,
						IntWritable.class, DoubleWritable.class, null, null);
			} else if (this.valClass.equals("FloatWritable")) {
				this.floatWriter = new Writer<IntWritable, FloatWritable>(job, this.out,
						IntWritable.class, FloatWritable.class, null, null);
			} else if (this.valClass.equals("Text")) {
				this.textWriter = new IFile.Writer<IntWritable, Text>(job, this.out,
						IntWritable.class, Text.class, null, null);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void reduce(IntWritable arg0, Iterator<Text> values,
			OutputCollector<NullWritable, NullWritable> arg2, Reporter arg3)
			throws IOException {
		while (values.hasNext()) {
			Text value = (Text) values.next();
			if (this.valClass.equals("IntWritable")) {
				int out = Integer.parseInt(value.toString());
				this.intWriter.append(arg0, new IntWritable(out));
			} else if (this.valClass.equals("DoubleWritable")) {
				double out = Double.parseDouble(value.toString());
				this.doubleWriter.append(arg0, new DoubleWritable(out));
			} else if (this.valClass.equals("FloatWritable")) {
				float out = Float.parseFloat(value.toString());
				this.floatWriter.append(arg0, new FloatWritable(out));
			} else if (this.valClass.equals("Text")) {
				String out = value.toString();
				this.textWriter.append(arg0, new Text(out));
			}
		}
	}

	public void close() {
		try {
			if (this.valClass.equals("IntWritable"))
				this.intWriter.close();
			else if (this.valClass.equals("DoubleWritable"))
				this.doubleWriter.close();
			else if (this.valClass.equals("FloatWritable"))
				this.floatWriter.close();
			else if (this.valClass.equals("Text")) {
				this.textWriter.close();
			}
			this.out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
