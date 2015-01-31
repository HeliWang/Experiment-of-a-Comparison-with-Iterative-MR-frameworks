package jt;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import org.apache.hadoop.examples.iterative.Util;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.IterativeMapper;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

public class KMeansMapR extends MapReduceBase
		implements
		IterativeMapper<IntWritable, Text, IntWritable, Text, IntWritable, Text> {
	private FileSystem fs;
	private String clusterDir;
	private int iteration;
	private BufferedWriter clusterWriter;
	private String samplesDir;
	private String clustersDir;
	private int taskid;
	private int partitions;
	private TreeMap<Integer, LastFMUserR> outCenters;
	private ArrayList<LastFMUserR> centers;
	private int k;
	private int counter;
	private OutputCollector<IntWritable, Text> outCollector;
	private int threshold;

	public KMeansMapR() {
		this.outCenters = new TreeMap<Integer, LastFMUserR>();
		this.centers = new ArrayList<LastFMUserR>();
		this.k = 0;
		this.counter = 0;

		this.threshold = 0;
	}

	public void configure(JobConf job) {
		this.clusterDir = job.get("kmeans.cluster.path");
		this.partitions = job.getInt("mapred.iterative.partitions", 0);
		this.threshold = (job.getInt("kmeans.threshold", 0) / this.partitions);
		this.iteration = 1;
		this.taskid = Util.getTaskId(job);
		try {
			this.fs = FileSystem.get(job);
			Path clusterPath = new Path(this.clusterDir + "/snapshot"
					+ this.iteration + "/part" + this.taskid);
			if (this.fs.exists(clusterPath)) this.fs.delete(clusterPath, true);
			FSDataOutputStream clusterOut = this.fs.create(clusterPath);
			this.clusterWriter = new BufferedWriter(new OutputStreamWriter(
					clusterOut));
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.k = job.getInt("kmeans.cluster.k", 0);
		this.clustersDir = job.get("dir.substate");
		this.samplesDir = job.get("dir.substatic");
	}

	public void map(IntWritable key, Text value, IntWritable datakey,
			Text dataval, OutputCollector<IntWritable, Text> output,
			Reporter report) throws IOException {
		
		//input key: nothing
        //input value: nothing
        //input data: user artist-id,plays tuples
        //output key: cluster id  (whose mean has the nearest measure distance)
        //output value: user-id data

		
		if (datakey == null) {
			synchronized (this.centers) {
				if (this.centers.size() == this.k) this.centers.clear();

				LastFMUserR curr = new LastFMUserR(key.get(), value.toString());
				this.centers.add(curr);
				LastFMUserR outCenter = new LastFMUserR(key.get(), "");
				this.outCenters.put(Integer.valueOf(key.get()), outCenter);
//				System.out.println("center size " + this.centers.size() + "\t"+ curr);

				return;
			}
		}

		if (this.outCollector == null) this.outCollector = output;

		LastFMUserR curr = new LastFMUserR(datakey.get(), dataval.toString());
		this.counter += 1;
		report.setStatus(String.valueOf(this.counter));
//		System.out.println(curr);

		double maxDist = -1.0D;
		LastFMUserR maxMean = null;
		synchronized (this.centers) {
			for (LastFMUserR mean : this.centers) {
				double dist = mean.ComplexDistance(curr);
//				System.out.println(curr + " distance to " + mean + " is " + dist);
//                System.out.println(dist + " comp " + maxDist);
				if (dist > maxDist) {
					maxDist = dist;
					maxMean = mean;
				}
			}
		}

		if (KmeanR.COMBINE){
			((LastFMUserR) this.outCenters.get(Integer.valueOf(maxMean.userID)))
					.add(curr);
		}
		else {
			output.collect(new IntWritable(maxMean.userID),
					new Text(curr.artistsString()));
            System.out.println(maxMean.userID + "\t" + curr.artistsString());

		}

		this.clusterWriter.write(String.valueOf(maxMean.userID) + "\t"
				+ curr.userID + "\n");
	}

	public Path[] initStateData() throws IOException {
		Path[] paths = new Path[this.partitions];
		for (int i = 0; i < this.partitions; ++i) {
			Path remotePath = new Path(this.clustersDir + i);
			Path localPath = new Path("/tmp/imapreduce/statedata" + i);
			this.fs.copyToLocalFile(remotePath, localPath);
			paths[i] = localPath;
		}

		return paths;
	}

	public Path initStaticData() throws IOException {
		Path remotePath = new Path(this.samplesDir + this.taskid);
		Path localPath = new Path("/tmp/imapreduce/staticdata" + this.taskid);
		this.fs.copyToLocalFile(remotePath, localPath);
		return localPath;
	}

	public void map(IntWritable arg0, Text arg1,
			OutputCollector<IntWritable, Text> arg2, Reporter arg3)
			throws IOException {
	}

	public void iterate() {
		System.out.println("map iteration called");
		this.iteration += 1;
		try {
			this.clusterWriter.close();
			Path clusterPath = new Path(this.clusterDir + "/" + this.iteration
					+ "/part" + this.taskid);
			if (this.fs.exists(clusterPath)) this.fs.delete(clusterPath, true);
			FSDataOutputStream clusterOut = this.fs.create(clusterPath);
			this.clusterWriter = new BufferedWriter(new OutputStreamWriter(
					clusterOut));
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (KmeanR.COMBINE) {
			 for(int meanID : this.outCenters.keySet()){
                 try {
                         outCollector.collect(new IntWritable(meanID), 
                                         new Text(outCenters.get(meanID).getArtists(threshold)));
                 } catch (IOException e) {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
                 }
			 }
			 outCenters.clear();
		} else {
			try {
				for (int i = 0; i < this.partitions; ++i){
					this.outCollector.collect(new IntWritable(i), new Text("0,0,0"));
					System.out.println(i+ " : " + "0,0,0");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}