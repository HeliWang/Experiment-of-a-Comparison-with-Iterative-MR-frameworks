package jt;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapred.lib.HashPartitioner;

public class UniDistIntPartitionerRep<V> extends HashPartitioner<IntWritable, V> {
	public int getPartition(IntWritable key, V value, int numReduceTasks) {
		return (key.get() % numReduceTasks);
	}
}