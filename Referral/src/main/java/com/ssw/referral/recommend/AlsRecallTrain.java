package com.ssw.referral.recommend;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.ml.evaluation.RegressionEvaluator;
import org.apache.spark.ml.recommendation.ALS;
import org.apache.spark.ml.recommendation.ALSModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.IOException;
import java.io.Serializable;

//ALS召回算法的训练
public class AlsRecall implements Serializable {
    public static void main(String[] args) throws IOException {
        // 初始化spark运行环境
        SparkSession spark = SparkSession.builder().master("local").appName("ReferralApp").getOrCreate();
        JavaRDD<String> csvFile =spark.read().textFile("file:///home/maomao/GithubProject/Referral-System/Referral/src/main/resources/data/behavior.csv").toJavaRDD();

        JavaRDD<Rating> ratingJavaRDD = csvFile.map(new Function<String, Rating>() {
            @Override
            public Rating call(String s) throws Exception {
                return Rating.parseRating(s);
            }
        });

        //转化为spark中通用的数据结构,以Rating为结构的数据表
        Dataset<Row> rating = spark.createDataFrame(ratingJavaRDD,Rating.class);
        //将所有的rating数据分词8 2份 80%做训练 20%做测试
        Dataset<Row>[] splits = rating.randomSplit(new double[]{0.8,0.2});

        Dataset<Row> trainingData = splits[0]; // 训练数据
        Dataset<Row> testingData = splits[1];  // 测试数据

        //过拟合：太趋近真实数据，一旦真实数据有误差，导致模型结果不尽如人意 解决方案：增大数据规模，减少RANK,增大正则化的系数
        //欠拟合：与真实数据相差过大，没有达到与真实数据拟合的一个过程 解决方案： 增加rank，减少正则化系数
        ALS als = new ALS().setMaxIter(10).setRank(5).setRegParam(0.01).
                setUserCol("userId").setItemCol("shopId").setRatingCol("rating");

        //模型训练的过程
        ALSModel alsModel = als.fit(trainingData);
        //alsModel.save("file:///home/maomao/GithubProject/Referral-System/Referral/src/main/resources/data/alsmode");

        //模型评测
        Dataset<Row> predictions = alsModel.transform(testingData);

        //rmse 均方根误差，预测值与真实值的偏差的平方除以观测次数，开个根号
        RegressionEvaluator evaluator = new RegressionEvaluator().setMetricName("rmse")
                .setLabelCol("rating").setPredictionCol("prediction");
        double rmse = evaluator.evaluate(predictions);
        System.out.println("rmse = "+rmse);

        //alsModel.write().overwrite().save("file:///D:\\dianpingtools\\devtool\\data\\sprakData");
    }




    // 对应行为得分
    public static class Rating implements Serializable{
        private int userId;
        private int shopId;
        private int rating;

        public static Rating parseRating(String str){
            str = str.replace("\"","");
            String[] strArr = str.split(",");
            int userId = Integer.parseInt(strArr[0]);
            int shopId = Integer.parseInt(strArr[1]);
            int rating = Integer.parseInt(strArr[2]);

            return new Rating(userId,shopId,rating);
        }

        public Rating(int userId, int shopId, int rating) {
            this.userId = userId;
            this.shopId = shopId;
            this.rating = rating;
        }

        public int getUserId() {
            return userId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public int getShopId() {
            return shopId;
        }

        public void setShopId(int shopId) {
            this.shopId = shopId;
        }

        public int getRating() {
            return rating;
        }

        public void setRating(int rating) {
            this.rating = rating;
        }
    }
}
