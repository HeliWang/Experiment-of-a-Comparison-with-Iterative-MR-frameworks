package jt;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.IterativeReducer;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

public class KMeansReduceCOS extends MapReduceBase implements
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
		if(outCollector == null){
            outCollector = output;
		}
		
		//input key: cluster's mean  (whose mean has the nearest measure distance)
        //input value: artid,avg,time artid,avg,time 
//		System.out.println(key.get() + ":");
		long count = 0;
		
		double[] sum = new double[3];
		while (values.hasNext()) {
			String data = ((Text) values.next()).toString();
			
			double[] vd = parseStringToVector(data);
			
			accumulate(sum, vd);
			count++;

			
//			System.out.println(data);
			
		}
//		System.out.println("threshold : " + this.threshold);
		String result = "";
		for (int i = 0; i < sum.length; i++) {
			sum[i] = sum[i] / count;
			result += (sum[i] + " ");
//			System.err.println(i+" Id: " + key.toString() + " Value: " + sum[i] + " / "+ result+" / "+count);
		}
		result.trim();

		output.collect(key, new Text(result));
	}
	
	private static void accumulate(double[] sum, double[] array) {
		//sum[0] = 0;
		for (int i = 0; i < sum.length; i++)
		{
			sum[i] += array[i];
			//System.err.println(i + " accumulate: " + sum[i]);
		}
	}
	
	// multi-dimensional Points to Vector
	private static double[] parseStringToVector(String line) {
			try {
				StringTokenizer tokenizer = new StringTokenizer(line, ",");
				int size = tokenizer.countTokens();

				double[] row = new double[size];
				int i = 0;
				while (tokenizer.hasMoreTokens()) {
					String attribute = tokenizer.nextToken();
					row[i] = Double.parseDouble(attribute);
					i++;
				}

				return row;
				
			} catch (Exception e) {
				StringTokenizer tokenizer = new StringTokenizer(line, " ");
				int size = tokenizer.countTokens();

				double[] row = new double[size];
				int i = 0;
				while (tokenizer.hasMoreTokens()) {
					String attribute = tokenizer.nextToken();
					row[i] = Double.parseDouble(attribute);
					//System.err.println(i + " Points " + row[i]);
					i++;
					
				}
				

				return row;
			}
		}
	public void iterate() {
		try {
            if(outCollector != null){
                    for(int i=0; i<k; i++){
                            outCollector.collect(new IntWritable(i), new Text("0,0,0"));
                            System.out.println(i + "\t" + "0,0,0");
	                }
	         }
	    } catch (IOException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	    }
		this.iteration += 1;
		Date current = new Date();
		long passed = (current.getTime() - this.start.getTime()) / 1000L;

		System.out.println("iteration " + this.iteration + " timepassed "
				+ passed);
		
	}
}