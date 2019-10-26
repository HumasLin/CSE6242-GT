// Databricks notebook source
// Q2 [25 pts]: Analyzing a Large Graph with Spark/Scala on Databricks

// STARTER CODE - DO NOT EDIT THIS CELL
import org.apache.spark.sql.functions.desc
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import spark.implicits._

// COMMAND ----------

// STARTER CODE - DO NOT EDIT THIS CELL
// Definfing the data schema
val customSchema = StructType(Array(StructField("answerer", IntegerType, true), StructField("questioner", IntegerType, true),
    StructField("timestamp", LongType, true)))

// COMMAND ----------

// STARTER CODE - YOU CAN LOAD ANY FILE WITH A SIMILAR SYNTAX.
// MAKE SURE THAT YOU REPLACE THE examplegraph.csv WITH THE mathoverflow.csv FILE BEFORE SUBMISSION.
val df = spark.read
   .format("com.databricks.spark.csv")
   .option("header", "false") // Use first line of all files as header
   .option("nullValue", "null")
   .schema(customSchema)
   .load("/FileStore/tables/mathoverflow.csv")
   .withColumn("date", from_unixtime($"timestamp"))
   .drop($"timestamp")

// COMMAND ----------

//display(df)
df.show()

// COMMAND ----------

// PART 1: Remove the pairs where the questioner and the answerer are the same person.
// ALL THE SUBSEQUENT OPERATIONS MUST BE PERFORMED ON THIS FILTERED DATA

// ENTER THE CODE BELOW
val fset = df.filter($"answerer" =!= $"questioner")
fset.show()

// COMMAND ----------

// PART 2: The top-3 individuals who answered the most number of questions - sorted in descending order - if tie, the one with lower node-id gets listed first : the nodes with the highest out-degrees.

// ENTER THE CODE BELOW
val g1 = fset
        .groupBy("answerer")
        .count()
        .orderBy($"count".desc, $"answerer".asc)
        .withColumnRenamed("count","questions_answered")
g1.show(3)

// COMMAND ----------

// PART 3: The top-3 individuals who asked the most number of questions - sorted in descending order - if tie, the one with lower node-id gets listed first : the nodes with the highest in-degree.

// ENTER THE CODE BELOW
val g2 = fset
        .groupBy("questioner")
        .count()
        .orderBy($"count".desc, $"questioner".asc)
        .withColumnRenamed("count","questions_asked")
g2.show(3)

// COMMAND ----------

// PART 4: The top-5 most common asker-answerer pairs - sorted in descending order - if tie, the one with lower value node-id in the first column (u->v edge, u value) gets listed first.

// ENTER THE CODE BELOW
val g3 = fset
        .groupBy("answerer", "questioner")
        .count()
        .orderBy($"count".desc, $"answerer".asc, $"questioner".asc)
g3.show(5)

// COMMAND ----------

// PART 5: Number of interactions (questions asked/answered) over the months of September-2010 to December-2010 (i.e. from September 1, 2010 to December 31, 2010). List the entries by month from September to December.

// Reference: https://www.obstkel.com/blog/spark-sql-date-functions
// Read in the data and extract the month and year from the date column.
// Hint: Check how we extracted the date from the timestamp.

// ENTER THE CODE BELOW

val intes = sc.parallelize(List(
  ("2010-09-01 00:00:00", "2010-09-30 23:59:59","09"), 
  ("2010-10-01 00:00:00", "2010-10-31 23:59:59","10"),
  ("2010-11-01 00:00:00", "2010-11-30 23:59:59","11"),
  ("2010-12-01 00:00:00", "2010-12-31 23:59:59","12")
  )).toDF("start","end","month")

val dates = fset.select("date")
val months = dates.as("data").
    join(intes.as("inter"), 
    $"data.date".between($"inter.start", $"inter.end"))

val result = months
            .groupBy("month")
            .count()
            .withColumn("month", $"month" cast "Int" as "month")
            .orderBy($"month")
            .withColumnRenamed("count","total_interactions")
            .show()

// COMMAND ----------

// PART 6: List the top-3 individuals with the maximum overall activity, i.e. total questions asked and questions answered.

// ENTER THE CODE BELOW
val ma = fset.select("answerer").groupBy("answerer").count().withColumnRenamed("count","ta")
val mq = fset.select("questioner").groupBy("questioner").count().withColumnRenamed("count","tq")
val total = ma.as("a").
    join(mq.as("q"), 
    $"a.answerer"===$"q.questioner")
val result = total
            .select($"a.answerer", $"ta" + $"tq")
            .withColumnRenamed("answerer","userID")
            .withColumnRenamed("(ta + tq)","total_activity")
            .orderBy($"total_activity".desc)
            .show(3)
