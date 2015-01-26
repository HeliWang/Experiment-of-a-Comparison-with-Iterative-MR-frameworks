package jt;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.examples.iterative.Common;
import org.apache.hadoop.examples.iterative.Util;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.IterativeMapper;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;

public class DQueryMapper extends MapReduceBase 
implements IterativeMapper<IntWritable, Text, 
IntWritable, Text, IntWritable, Text>{
	private FileSystem fs;
	private String subGraphsDir;
	private String subRankDir;
	private int taskid;
	
	private String start_node;

	 
	@Override
	public void configure(JobConf job) {
		try {
			fs = FileSystem.get(job);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

		subRankDir = job.get(Common.SUBSTATE);
		subGraphsDir = job.get(Common.SUBSTATIC);
		
		start_node = job.get("dquery.start_node");
		
		taskid = Util.getTaskId(job);		
	}
	
	@Override
	public void map(IntWritable key, Text value, IntWritable datakey,
			Text dataval, OutputCollector<IntWritable, Text> output, Reporter report)
			throws IOException {
		
		System.out.println("k1 : " + key + " : " + value);
		System.out.println("dk : " + datakey + " : " + dataval);
		if (!value.toString().equals("-1")){
			String nodes = value.toString();
			ArrayList<String> outlist = new ArrayList<String>();
			String[] nodelist = nodes.split(" ");
			
			String fnode = datakey.toString();
			
			for (String node : nodelist){
				if (node.equals(fnode)){
					if(!outlist.contains(datakey)){
						System.out.println(key.toString() + " : " + datakey.toString());
						output.collect(key, new Text(datakey.toString()));
					}
					if(!outlist.contains(dataval)){
						System.out.println(key.toString() + " : " + dataval.toString());
						output.collect(key, new Text(dataval.toString()));
					}
				}
			}
		}
		else{
			String fnode = datakey.toString();
			if (fnode.equals(start_node)){
				System.out.println(datakey.toString() + " : " + dataval.toString());
				output.collect(datakey, new Text(dataval.toString()));
			}	
		}
	}

	@Override
	public Path[] initStateData() throws IOException {
		Path remotePath = new Path(this.subRankDir + taskid);
		Path localPath = new Path(Common.LOCAL_STATE + taskid);
		fs.copyToLocalFile(remotePath, localPath);
		Path[] paths = new Path[1];
		paths[0] = localPath;
		return paths;
	}
	
	@Override
	public Path initStaticData() throws IOException {
		Path remotePath = new Path(this.subGraphsDir + taskid);
		Path localPath = new Path(Common.LOCAL_STATIC + taskid);
		fs.copyToLocalFile(remotePath, localPath);
		return localPath;
	}

	@Override
	public void iterate() {

	}

	@Override
	public void map(IntWritable paramK1, Text paramV1,
			OutputCollector<IntWritable, Text> paramOutputCollector,
			Reporter paramReporter) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
