package jt;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.examples.iterative.Util;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class KmeanR extends Configured implements Tool {
	public static boolean COMBINE = false;
	public static final String KMEANS_INITCENTERS_DIR = "kmeans.initcenters.dir";
	public static final String KMEANS_CLUSTER_PATH = "kmeans.cluster.path";
	public static final String KMEANS_CLUSTER_K = "kmeans.cluster.k";
	public static final String KMEANS_DATA_DIR = "kmeans.data.dir";
	public static final String KMEANS_TIME_DIR = "kmeans.time.dir";
	public static final String KMEANS_THRESHOLD = "kmeans.threshold";
	private int k;
	private int threshold;
	private int partitions;
	private int iterations;
	private int nodes;

	public KmeanR() {
		this.threshold = 50;
		this.partitions = 0;
		this.iterations = 20;
		this.nodes = 1000000;
	}

	private void preprocess(String instate, String instatic) throws Exception {
		String[] args = new String[6];
		args[0] = instate;
		args[1] = instatic;
		args[2] = "Text";
		args[3] = String.valueOf(this.nodes);
		args[4] = String.valueOf(this.partitions);
        args[5] = String.valueOf(true);
		
		for(String ar:args){ System.out.println(ar);}

		ToolRunner.run(new Configuration(), new PreProcessRep(), args);
	}

	private void iterateKMeans(String input, String output) throws IOException {
		JobConf job = new JobConf(getConf());
		String jobname = "kmeans";
		job.setJobName(jobname);

		job.set("kmeans.cluster.path", output);
		job.setInt("kmeans.cluster.k", this.k);
		job.setInt("kmeans.threshold", this.threshold);

		job.set("dir.substate", "_substate/substate");
		job.set("dir.substatic", "_substatic/substatic");

		if (this.partitions == 0)
			this.partitions = Util.getTTNum(job);

		job.setBoolean("mapred.job.iterative", true);
		job.setBoolean("mapred.iterative.reducesync", true);
		job.setBoolean("mapred.iterative.mapsync", true);
		job.set("mapred.iterative.jointype", "one2all");
		job.setInt("mapred.iterative.partitions", this.partitions);
		job.setInt("mapred.iterative.snapshot.interval", 10000);
		job.setInt("mapred.iterative.stop.iteration", this.iterations);

		FileInputFormat.addInputPath(job, new Path(input));
		FileOutputFormat.setOutputPath(job, new Path(output));

		job.setJarByClass(KmeanR.class);
		job.setOutputFormat(TextOutputFormat.class);
		job.setMapperClass(KMeansMapR.class);
		job.setReducerClass(KMeansReduceR.class);
		job.setDataKeyClass(IntWritable.class);
		job.setDataValClass(Text.class);
		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);
		job.setPartitionerClass(UniDistIntPartitionerRep.class);

		job.setNumMapTasks(this.partitions);
		job.setNumReduceTasks(this.partitions);

		JobClient.runJob(job);
	}

	private void printUsage() {
		System.out
				.println("kmeans [-p partitions] <InTemp> <inStateDir> <inStaticDir> <outDir> <k>");
		System.out
				.println("\t-p # of parittions\n\t-I # of iterations\n\t-t stop threshold");

		ToolRunner.printGenericCommandUsage(System.out);
	}

	public int run(String[] args) throws Exception {
		if (args.length < 5) {
			printUsage();
			return -1;
		}

		List other_args = new ArrayList();
		for (int i = 0; i < args.length; ++i) {
			try {
				if ("-p".equals(args[i]))
					this.partitions = Integer.parseInt(args[(++i)]);
				else if ("-I".equals(args[i]))
					this.iterations = Integer.parseInt(args[(++i)]);
				else if ("-t".equals(args[i]))
					this.threshold = Integer.parseInt(args[(++i)]);
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

//		System.out.println("inkmean");
//		for(Object ar:other_args){ System.out.println((String)ar);}

		if (other_args.size() < 5) {
			System.out.println("ERROR: Wrong number of parameters: "
					+ other_args.size() + ".");

			printUsage();
			return -1;
		}

		String input = (String) other_args.get(1);
		String instate = (String) other_args.get(2);
		String instatic = (String) other_args.get(3);
		String output = (String) other_args.get(4);
		this.k = Integer.parseInt((String)other_args.get(5));

		preprocess(instate, instatic);
		iterateKMeans(input, output);

		return 0;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new KmeanR(), args);
		System.exit(res);
	}
}