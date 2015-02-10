#! bin/bash
# This shell will perform PageRank computation on iMapReduce

HADOOP=../bin/hadoop
JAR=../build/hadoop-examples-1.0.3-SNAPSHOT.jar

DATA_DIR=rg_dataset
STATIC=$DATA_DIR/iterativestatic

# for synthetic data, you should use gendata.sh to generate some synthetic dataset. Note that you should make # of nodes|# of partitions|data dir consistent.
#NODE=1000

# for real data, you should download them and upload them to HDFS first
# google web graph (http://rio.ecs.umass.edu/~yzhang/data/pg_google_graph, http://rio.ecs.umass.edu/~yzhang/data/pg_google_rank)
#LOCAL_STATIC=pg_google_graph
#LOCAL_STATE=pg_google_rank
#NODE=916417
# berkstan web graph (http://rio.ecs.umass.edu/~yzhang/data/pg_berkstan_graph, http://rio.ecs.umass.edu/~yzhang/data/pg_berkstan_rank)
#LOCAL_STATIC=pg_berkstan_graph
#LOCAL_STATE=pg_berkstan_rank
#NODE=685231

LOCAL_STATIC=test_graph
#NODE=5

# upload the real dataset to HDFS, it is unnecessary for the synthetic dataset
$HADOOP dfs -rmr $DATA_DIR
$HADOOP dfs -put $LOCAL_STATIC $STATIC

# perform PageRank iteration
IN=temp
OUT=IterativeRQ
ITERATIONS=4
SNAPSHOTINTERVAL=1
PARTITIONS=1

Q=0

$HADOOP dfs -put temp $IN
$HADOOP dfs -rmr $OUT	
	
$HADOOP jar $JAR iterrecursivequery $STATIC $OUT -i $SNAPSHOTINTERVAL -I $ITERATIONS -p $PARTITIONS -s false -q $Q
