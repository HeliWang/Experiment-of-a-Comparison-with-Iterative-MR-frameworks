package org.apache.hadoop.examples.kmeans;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;

public class NaiveKMeans {


	//static int global_currentIteration = 0;
	//static String global_outputPath;
	//static String global_init_inputPath;
	
	
	/*
	 * 1. Partition objects into K nonempty subsets
	 * 2. Compute the centroids of the clusters in the current partition
	 * 3. Assign each object to the cluster with the nearset centroid
	 * 4. Stop when no more new assignments. Otherwise go back to Step 2.
	 * 
	 * 
	 * Map function
	 * K centers are broadcasted to every map function
	 * Find the closet center among K centers for the input point
	 * 
	 */
	
	public static class KMeansMapper extends MapReduceBase implements
			Mapper<LongWritable, Text, Text, Text> {

		private HashMap<String, double[]> reducerOutput = new HashMap<String, double[]>();

		//private double rowBuffer[] = null;

		private Text idBuffer = new Text();

		private Text outputBuffer = new Text();
		
		@SuppressWarnings("deprecation")
		public void configure(JobConf conf) {
			int iteration = conf.getInt("iteration",-1);
			String outputPath = conf.get("output_path");
		//	String init_path = global_init_inputPath;
			System.err.println("iteration number " + iteration);
			
			if (iteration <= 0) {
				
				try {
					reducerOutput.clear();
					FileSystem dfs = FileSystem.get(conf);		
					Path path = new Path("kinfo");					
					System.out.println("init K centers informaiton");
					FileStatus[] files = dfs.listStatus(path);

					for (int i = 0; i < files.length; i++) {
						if (!files[i].isDir()) {
							FSDataInputStream is = dfs.open(files[i].getPath());
							String line = null;
							//System.err.println("ReadLine " + is.readLine());
							
							while ((line = is.readLine()) != null) {
								
								i++;
								System.err.println(i + " " + line);
								//(num of nodes, points vector)								
								reducerOutput.put(Integer.toString(i),parseStringToVector(line));
								
							}
							is.close();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			} else {
				try {
					reducerOutput.clear();
					FileSystem dfs = FileSystem.get(conf);
					System.out.println("mapper current iteration " + iteration);
					String location = outputPath + "/i"
							+ (iteration - 1);
					Path path = new Path(location);
					System.out.println("reducer feedback input path: "
							+ location);

					FileStatus[] files = dfs.listStatus(path);

					for (int i = 0; i < files.length; i++) {
						if (!files[i].isDir()) {
							FSDataInputStream is = dfs.open(files[i].getPath());
							String line = null;
							while ((line = is.readLine()) != null) {
								String fields[] = line.split("\t", 2);
								reducerOutput.put(fields[0],parseStringToVector(fields[1]));													
							}
							is.close();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (reducerOutput.isEmpty())
					System.out.println("init error");
				}
		}

		@Override
		public void map(LongWritable key, Text value,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {

			if (reducerOutput == null || reducerOutput.isEmpty())
				throw new IOException("reducer output is null");

			String line = value.toString();
			// skip the first line
			//if (line.indexOf("::") >= 0)
			//	return;
			//String dataString = line.substring(pos + 1);
			String dataString = line;
			
			// Only points (datastring) to Vector
			//double[] row = parseStringToVector(dataString, rowBuffer);
			double[] row = parseStringToVector(dataString);
						
			Iterator<String> keys = reducerOutput.keySet().iterator();
			double minDistance = Double.MAX_VALUE;
			String minID = "";

			// find the cluster membership for the data vector
			while (keys.hasNext()) {
				String id = keys.next();
				double[] point = reducerOutput.get(id);
				// Compare standard point with other points
				double currentDistance = distance(row, point);
				if (currentDistance < minDistance) {
					minDistance = currentDistance;
					minID = id;
					// minValue = data;
				}
			}

			// output the vector (value) and cluster membership (key)
			idBuffer.clear();
			idBuffer.append(minID.getBytes(), 0, minID.getBytes().length);

			outputBuffer.clear();
			outputBuffer.append(dataString.getBytes(), 0,
					dataString.getBytes().length);
			System.out.println("2 ID Buffer: "+ idBuffer.toString() + " Out Buffer: "+outputBuffer.toString());
			output.collect(idBuffer, outputBuffer);
		}
		
		private double distance(double[] d1, double[] d2) {
			double distance = 0;
			int len = d1.length < d2.length ? d1.length : d2.length;

			for (int i = 0; i < len; i++) {
				distance += (d1[i] - d2[i]) * (d1[i] - d2[i]);				
			}			
			return Math.sqrt(distance);
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
				System.err.println(i + " Points " + row[i]);
				i++;
				
			}
			

			return row;
		}
	}

	private static void accumulate(double[] sum, double[] array) {
		//sum[0] = 0;
		for (int i = 0; i < sum.length; i++)
		{
			sum[i] += array[i];
			System.err.println(i + " accumulate: " + sum[i]);
		}
	}

	
	
	/**
	 *Input is one of K ccenters and all data points having this center as their closet center
	 *Calculates the new center using data points
	 *
	 *Until all of new centers are not changed
	*/
	public static class KMeansReducer extends MapReduceBase implements
		Reducer<Text, Text, Text, Text> {


		public void reduce(Text key, Iterator<Text> values,
				OutputCollector<Text, Text> output, Reporter reporter)
				throws IOException {
		
			
			double[] sum = null;
			long count = 0;
			
			sum = new double[2];
			while (values.hasNext()) {
				String data = values.next().toString();
				double[] row = parseStringToVector(data);
				accumulate(sum, row);				
				count++;
			}
				
			System.err.println("2. Id: " + key.toString() + " Value: " + sum[0] + " / "+ sum[1]+" / "+count);
			// generate the new means center
	
			String result = "";
			for (int i = 0; i < sum.length; i++) {
				sum[i] = sum[i] / count;
				result += (sum[i] + ",");
				System.err.println(i+" Id: " + key.toString() + " Value: " + sum[i] + " / "+ result+" / "+count);
			}
			result.trim();
		
			output.collect(key, new Text(result));
		}
	}


	public static void main(String[] args) throws Exception {

		for(String arg : args)
		{
			System.err.println(arg);
		}

		String inputPath = args[0];
		String outputPath = args[1];		

		int specIteration = 0;
		if (args.length > 2) {
			specIteration = Integer.parseInt(args[2]);
		}	
		
		int numReducers = 3;
		if (args.length > 3) {
			numReducers = Integer.parseInt(args[3]);
		}			

		int currentIteration = 0;
		long start = System.currentTimeMillis();
		while (currentIteration < specIteration) {
			JobConf conf = new JobConf(NaiveKMeans.class);
			conf.setJobName("K-means");

			conf.setInputFormat(TextInputFormat.class);
			conf.setOutputFormat(TextOutputFormat.class);
			conf.setMapperClass(KMeansMapper.class);			
			conf.setReducerClass(KMeansReducer.class);
			conf.setCombinerClass(KMeansReducer.class);
			conf.setOutputKeyClass(Text.class);
			conf.setOutputValueClass(Text.class);

			conf.setInt("iteration",currentIteration);
			conf.set("output_path", outputPath);
			conf.setNumReduceTasks(numReducers);
			conf.setSpeculativeExecution(false);	
			//global_outputPath=outputPath;
			
			FileInputFormat.setInputPaths(conf, new Path(inputPath));
			FileOutputFormat.setOutputPath(conf, new Path(outputPath + "/i"
					+ currentIteration));

			JobClient.runJob(conf);
			currentIteration++;
		}
		long end = System.currentTimeMillis();
		System.out.println("running time " + (end - start) / 1000 + "s");
	}
}