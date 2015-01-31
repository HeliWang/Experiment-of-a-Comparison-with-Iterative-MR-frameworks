package jt;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Iterator;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.IterativeReducer;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

public class KMeansReduceR extends MapReduceBase implements
		IterativeReducer<IntWritable, Text, IntWritable, Text> {
	private int iteration;
	private Date start;
	private int threshold;
    private int partitions
    ;
    private OutputCollector<IntWritable, Text> outCollector = null;
	private int k;


	public void configure(JobConf job) {
		this.iteration = 0;
		this.start = new Date();
		this.threshold = job.getInt("kmeans.threshold", 0);
        partitions = job.getInt("mapred.iterative.partitions", 0);
		this.k = job.getInt("kmeans.cluster.k", 0);


	}

	public void reduce(IntWritable key, Iterator<Text> values,
			OutputCollector<IntWritable, Text> output, Reporter report)
			throws IOException {
		if(output != null){
            outCollector = output;
		}
		
		//input key: cluster's mean  (whose mean has the nearest measure distance)
        //input value: artid,avg,time artid,avg,time 
//		System.out.println(key.get() + ":");
		
		LastFMUserR base = new LastFMUserR(key.get(), "");
		while (values.hasNext()) {
			String data = ((Text) values.next()).toString();
//			System.out.println(data);
			if (KmeanR.COMBINE) {
				LastFMUserR curr = new LastFMUserR(key.get(), data, true);
				base.addinred(curr);
			} else {
				LastFMUserR curr = new LastFMUserR(key.get(), data);
				base.add(curr);
			}
		}
//		System.out.println("threshold : " + this.threshold);
		String res = base.getArtists(this.threshold);
		if (res.equals("")) res = "0,0,0";
		output.collect(key, new Text(res));
		System.out.println(key + "\t" + res);
	}

	public void iterate() {
//		try {
//            if(outCollector != null){
//                    for(int i=0; i<k; i++){
//                            outCollector.collect(new IntWritable(i), new Text("0,0,0"));
//                            System.out.println(i + "\t" + "0,0,0");
//	                }
//	         }
//	    } catch (IOException e) {
//	            // TODO Auto-generated catch block
//	            e.printStackTrace();
//	    }
		this.iteration += 1;
		Date current = new Date();
		long passed = (current.getTime() - this.start.getTime()) / 1000L;

		System.out.println("iteration " + this.iteration + " timepassed "
				+ passed);
		
	}
}