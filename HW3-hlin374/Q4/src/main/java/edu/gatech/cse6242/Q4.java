package edu.gatech.cse6242;
import java.util.StringTokenizer;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.util.*;
import org.apache.hadoop.mapreduce.lib.chain.ChainMapper;
import org.apache.hadoop.mapreduce.lib.chain.ChainReducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import java.io.IOException;

public class Q4 {
    
    public static class TokenizerMapper 
    extends Mapper<Object, Text, Text, IntWritable>{
        private IntWritable outd = new IntWritable(1);
        private IntWritable ind = new IntWritable(-1);
        private Text node = new Text();        
        public void map(Object key, Text value, Context context) 
        throws IOException, InterruptedException{
            String [] lines = value.toString().split("\t");
            int out = 1;
            int in = -1;
            outd.set(out);
            ind.set(in);
            if(lines.length == 2){
                node.set(lines[0]+","+outd);
                context.write(node, outd);
                node.set(lines[1]+","+ind);
                context.write(node, ind);
            }
        }
    }

    public static class IntReducer 
    extends Reducer<Text, IntWritable, Text, IntWritable>{
        private IntWritable degree = new IntWritable(1);
        private Text freq = new Text();
      
        public void reduce(Text key, Iterable<IntWritable> values, Context context) 
        throws IOException, InterruptedException{
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			String [] numbers = key.toString().split(",");
			freq.set(numbers[0]);
			degree.set(sum);
			context.write(freq, degree);
        }
    }

    public static class DegreeMapper 
    extends Mapper<Object, Text, LongWritable, IntWritable>{
        private IntWritable degree = new IntWritable(1);
        private LongWritable freq = new LongWritable();       
        public void map(Object key, Text value, Context context) 
        throws IOException, InterruptedException{
            String [] lines = value.toString().split("\t");
            if(lines.length == 2){
                freq.set(Long.parseLong(lines[1]));
                context.write(freq, degree);
            }
        }
    }

    public static class IntCntReducer 
    extends Reducer<LongWritable, IntWritable, LongWritable, IntWritable>{
        private IntWritable degree = new IntWritable(1);
        private LongWritable freq = new LongWritable();    
        public void reduce(LongWritable key, Iterable<IntWritable> values, Context context) 
        throws IOException, InterruptedException{
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			degree.set(sum);
			context.write(key, degree);
        }
    }

    public static void main(String[] args) 
    throws Exception {

        Configuration conf = new Configuration();      

        Job job1 = Job.getInstance(conf, "Q4_1");
        job1.setJarByClass(Q4.class);
        job1.setMapperClass(TokenizerMapper.class);
        job1.setCombinerClass(IntReducer.class);
        job1.setReducerClass(IntReducer.class);
        job1.setOutputKeyClass(Text.class);
        job1.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job1, new Path(args[0]));
        FileOutputFormat.setOutputPath(job1, new Path("temp"));
        job1.waitForCompletion(true);

        Job job2 = Job.getInstance(conf, "Q4_2");
        job2.setJarByClass(Q4.class);
        job2.setMapperClass(DegreeMapper.class);
        job2.setCombinerClass(IntCntReducer.class);
        job2.setReducerClass(IntCntReducer.class);
        job2.setOutputKeyClass(LongWritable.class);
        job2.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job2, new Path("temp"));
        FileOutputFormat.setOutputPath(job2, new Path(args[1]));
        System.exit(job2.waitForCompletion(true) ? 0:1);
                                       
    }
}