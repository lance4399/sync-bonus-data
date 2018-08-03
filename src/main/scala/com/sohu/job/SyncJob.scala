package com.sohu.job

import java.text.SimpleDateFormat
import java.util.Date
import org.apache.spark.sql.SparkSession
import utils.Connect2Mongo

/**
  * @Author: xiliang
  * @Create: 2018/8/1 11:32
  */
object SyncJob {
  def main(args: Array[String]): Unit = {
    val sparkSession = SparkSession.builder().appName("Sync-Bonus-Job").config("hive.exec.dynamic.partition",  "true")
      .config("spark.sql.warehouse.dir", "hdfs://dc1/user/mediaai/hive/warehouse/")
      .config("hive.exec.dynamic.partition.mode", "nonstrict").enableHiveSupport().getOrCreate()
//    val sparkSession = SparkSession.builder().appName("Sync-Bonus-Job").master("local").getOrCreate()
    val jsonArray = Connect2Mongo.getCollectionData()
    val ds = sparkSession.sparkContext.makeRDD(Seq(jsonArray.toString))
    val df = sparkSession.read.json(ds)
    df.createOrReplaceTempView("bonus_tmp_view")
    sparkSession.sql("select type,sms_id,email_template_id,mp_media_id,operator_passport,operator_name,old_level,new_level,old_mp_channel_id, new_mp_channel_id, mp_profile_id, profile_reaction,create_time from bonus_tmp_view").show(10)
    val tableName = "bonus_sync_di";
    val dtStr = new SimpleDateFormat("yyyMMdd").format(new Date())
    sparkSession.sql("use mediaai")
    sparkSession.sql("insert into table "+tableName + " partition(dt='"+dtStr+"') select type,sms_id,email_template_id,mp_media_id,operator_passport,operator_name,old_level,new_level,old_mp_channel_id, new_mp_channel_id, mp_profile_id, profile_reaction,create_time  from bonus_tmp_view")
    println("############"+dtStr+"Done! ###########")

  }
}
