package jt;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.examples.iterative.UniDistIntPartitioner;
import org.apache.hadoop.examples.iterative.Util;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class PGR extends Configured implements Tool {
	
	
	private int partitions;
	private int interval;
	private int iterations;
	private int nodes;
	public static final double DAMPINGFAC = 0.8D;
	public static final double RETAINFAC = 0.2D;

	public PGR() {
		this.partitions = 0;
		this.interval = 10;
		this.iterations = 50;
		this.nodes = 1000000;
	}

	private void preprocess(String instate, String instatic) throws Exception {
		String[] args = new String[5];
		args[0] = instate;
		args[1] = instatic;
		args[2] = "DoubleWritable";
		args[3] = String.valueOf(this.nodes);
		args[4] = String.valueOf(this.partitions);
        args[5] = String.valueOf(false);

		for(String ar:args){ System.out.println(ar);}

		ToolRunner.run(new Configuration(), new PreProcessRep(), args);
	}

	private int pagerank(String input, String output) throws IOException {
		JobConf job = new JobConf(getConf());
		String jobname = "pagerank";
		job.setJobName(jobname);

		job.set("dir.substate", "_substate/substate");
		job.set("dir.substatic", "_substatic/substatic");

		FileInputFormat.addInputPath(job, new Path(input));
		FileOutputFormat.setOutputPath(job, new Path(output));
		job.setOutputFormat(TextOutputFormat.class);

		if (this.partitions == 0)
			this.partitions = Util.getTTNum(job);

		job.setBoolean("mapred.job.iterative", true);
		job.setBoolean("mapred.iterative.reducesync", true);
		job.set("mapred.iterative.jointype", "one2one");
		job.setInt("mapred.iterative.partitions", this.partitions);
		job.setInt("mapred.iterative.snapshot.interval", this.interval);
		job.setInt("mapred.iterative.stop.iteration", this.iterations);

		job.setJarByClass(PGR.class);
		job.setMapperClass(PGRM.class);
		job.setReducerClass(PGRR.class);
		job.setDataKeyClass(IntWritable.class);
		job.setDataValClass(Text.class);
		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(DoubleWritable.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(DoubleWritable.class);
		job.setPartitionerClass(UniDistIntPartitionerRep.class);

		job.setNumMapTasks(this.partitions);
		job.setNumReduceTasks(this.partitions);

		JobClient.runJob(job);
		return 0;
	}

	private void printUsage() {
		System.out
				.println("pagerank [-p partitions] <InTemp> <inStateDir> <inStaticDir> <outDir>");
		System.out
				.println("\t-p # of parittions\n\t-i snapshot interval\n\t-I # of iterations\n\t-n # of nodes");

		ToolRunner.printGenericCommandUsage(System.out);
	}

	public int run(String[] args) throws Exception {
		if (args.length < 4) {
			printUsage();
			return -1;
		}

		List other_args = new ArrayList();
		for (int i = 0; i < args.length; ++i) {
			try {
				if ("-p".equals(args[i]))
					this.partitions = Integer.parseInt(args[(++i)]);
				else if ("-i".equals(args[i]))
					this.interval = Integer.parseInt(args[(++i)]);
				else if ("-I".equals(args[i]))
					this.iterations = Integer.parseInt(args[(++i)]);
				else if ("-n".equals(args[i]))
					this.nodes = Integer.parseInt(args[(++i)]);
				else
					other_args.add(args[i]);
			} catch (NumberFormatException except) {
				System.out.println("ERROR: Integer expected instead of "
						+ args[i]);
				printUsage();
				return -1;
			} catch (ArrayIndexOutOfBoundsException except) {
				System.out.println("ERROR: Required parameter missing from "
						+ args[(i - 1)]);

				printUsage();
				return -1;
			}
		}

		if (other_args.size() < 4) {
			System.out.println("ERROR: Wrong number of parameters: "
					+ other_args.size() + ".");

			printUsage();
			return -1;
		}

		String input = (String) other_args.get(1);
		String instate = (String) other_args.get(2);
		String instatic = (String) other_args.get(3);
		String output = (String) other_args.get(4);

		preprocess(instate, instatic);
		pagerank(input, output);

		return 0;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new PGR(), args);
		System.exit(res);
	}
}