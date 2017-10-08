package org.apache.flink.quickstart
import org.apache.flink.streaming.api.scala._


import java.util.{Calendar, TimeZone}
import com.dataartisans.flinktraining.exercises.datastream_java.sources.TaxiRideSource
import com.dataartisans.flinktraining.exercises.datastream_java.utils.GeoUtils
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.util.Collector

object AirportTrends extends App {


  sealed trait Terminal{def grid: Int};
  case object Terminal_1 extends Terminal {val grid = 71436};
  case object Terminal_2 extends Terminal {val grid = 71688};
  case object Terminal_3 extends Terminal {val grid = 71191};
  case object Terminal_4 extends Terminal {val grid = 70945};
  case object Terminal_5 extends Terminal {val grid = 70190};
  case object Terminal_6 extends Terminal {val grid = 70686};
  case object Terminal_404 extends Terminal {val grid = -1};

  val terminals : Set[Terminal] = Set(Terminal_1, Terminal_2, Terminal_3, Terminal_4, Terminal_5, Terminal_6);

  def gridToTerminal(gridCell: Int): Terminal = {
    terminals.find(t => t.grid == gridCell) match {
      case Some(terminal) => terminal;
      case None => Terminal_404;
    }
  }

  val env = StreamExecutionEnvironment.getExecutionEnvironment
  env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

  // get the taxi ride data stream - Note: you got to change the path to your local data file
  env.addSource(new TaxiRideSource("data/nycTaxiRides.gz", 60, 2000)) ;

  /**
    * Write Your application here
    */

  env.execute();

}

