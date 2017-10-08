package org.apache.flink.quickstart

import com.dataartisans.flinktraining.exercises.datastream_java.sources.TaxiRideSource
import com.dataartisans.flinktraining.exercises.datastream_java.utils.GeoUtils
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._

object TaxiRideCleansing {
  def main(args: Array[String]): Unit = {
    // get an ExecutionEnvironment
    val env = StreamExecutionEnvironment.getExecutionEnvironment

    // configure event-time processing
    env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    // get the taxi ride data stream
    val rides = env.addSource(new TaxiRideSource("/home/dknoo/IdeaProjects/flinkscalaproject/src/data/nycTaxiRides.gz", 60, 600))

    // filter rides for only in NYC
    val ridesNYC = rides.filter(r => GeoUtils.isInNYC(r.startLon,r.startLat) && GeoUtils.isInNYC(r.endLon,r.endLat))

    // print filtered stream
    ridesNYC.print()

    // run execution
    env.execute("Taxi Ride Cleansing")
  }

}
