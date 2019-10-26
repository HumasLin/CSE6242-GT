package edu.gatech.cse6242;
import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.util.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Q1 {
    public static class TokenizerMapper
    extends Mapper<Object, Text, Text, Text>{
        
        private Text info = new Text();
        private Text word = new Text();
        
        public void map(Object key, Text value, Context context) 
        throws IOException, InterruptedException {
            String[] fields = value.toString().split("\t");
            if(fields.length == 3) {
                String node = fields[0];
                String target = fields[1];
                String nodeWeight = fields[2];
                info.set(target+","+nodeWeight);
                word.set(node);
                context.write(word, info);
            }
        }
    }

    public static class IntMaxReducer
    extends Reducer<Text,Text,Text,Text> {
        private Text result = new Text();
		private Text infos = new Text();       
        
        public void reduce(Text key, Iterable<Text> values, Context context) 
        throws IOException, InterruptedException {
            int largest = 0;
            for (Text val : values) {
				String[] fact = val.toString().split(",");
                if(fact.length == 2){
                    String weight = fact[1];
		    		int number = Integer.parseInt(weight);
                    if(number>largest) {
                        largest = number;
                        infos.set(val);
		    		}
		    		if(number == largest){
	 		 			int index = Integer.parseInt(fact[0]);
						String[] compare = infos.toString().split(",");
						int aim = Integer.parseInt(compare[0]);
						if(index < aim){
			    			infos.set(val);
						}	
		    		}	
                }
            }
        result.set(infos);
        context.write(key, result);
        }
    }


    
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Q1");
        
        /* TODO: Needs to be implemented */
        job.setJarByClass(Q1.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(IntMaxReducer.class);
        job.setReducerClass(IntMaxReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
