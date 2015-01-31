package jt;

import java.io.PrintStream;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.KeyValueTextInputFormat;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.lib.NullOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class PreProcessRep extends Configured implements Tool {
	public int run(String[] args) throws Exception {
		if (args.length != 5) {
			System.err
					.println("Usage: preprocess <in_state> <in_static> <valClass> <pages> <partitions>");
			System.exit(2);
		}

		String inState = args[0];
		String inStatic = args[1];
		String valClass = args[2];
		int totalpages = Integer.parseInt(args[3]);
		int partitions = Integer.parseInt(args[4]);
        boolean broadcast = Boolean.parseBoolean(args[5]);


		JobConf job = new JobConf(getConf());
		String jobname = "distribute state data";
		job.setJobName(jobname);

		job.set("dir.substate", "_substate/substate");
		job.setInputFormat(KeyValueTextInputFormat.class);
		job.setOutputFormat(NullOutputFormat.class);
		TextInputFormat.addInputPath(job, new Path(inState));

		job.setJarByClass(PreProcessRep.class);
		job.setMapperClass(StateDistributeMapRep.class);
		job.setReducerClass(StateDistributeReduceRep.class);

		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(NullWritable.class);
		job.setPartitionerClass(UniDistIntPartitionerRep.class);

		job.set("data.val.class", valClass);
		job.setNumReduceTasks(partitions);

		JobClient.runJob(job);

		JobConf job2 = new JobConf(getConf());
		String jobname2 = "distribute static data";
		job2.setJobName(jobname2);

		job2.setInt("total.entries", totalpages);
		job2.set("dir.substatic", "_substatic/substatic");
		job2.setInputFormat(KeyValueTextInputFormat.class);
		job2.setOutputFormat(NullOutputFormat.class);
		TextInputFormat.addInputPath(job2, new Path(inStatic));

		job2.setJarByClass(PreProcessRep.class);
		job2.setMapperClass(StaticDistributeMapRep.class);
		job2.setReducerClass(StaticDistributeReduceRep.class);

		job2.setMapOutputKeyClass(IntWritable.class);
		job2.setMapOutputValueClass(Text.class);
		job2.setOutputKeyClass(NullWritable.class);
		job2.setOutputValueClass(NullWritable.class);
		job2.setPartitionerClass(UniDistIntPartitionerRep.class);

		job2.setNumReduceTasks(partitions);

		JobClient.runJob(job2);

		return 0;
	}

	public static void main(String[] args) throws Exception {
		int res = ToolRunner.run(new Configuration(), new PreProcessRep(), args);
		System.exit(res);
	}
}