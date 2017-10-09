package org.apache.flink.quickstart
import org.apache.flink.streaming.api.scala._
import java.util.{Calendar, TimeZone}

import com.dataartisans.flinktraining.exercises.datastream_java.sources.TaxiRideSource
import com.dataartisans.flinktraining.exercises.datastream_java.utils.GeoUtils
import com.dataartisans.flinktraining.exercises.datastream_scala.windows.PopularPlaces.GridCellMatcher
import org.apache.flink.api.common.functions.MapFunction
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala.{StreamExecutionEnvironment, _}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.util.Collector
import sun.awt.SunHints.Key

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

  // get the taxi ride data stream
  val rides = env.addSource(new TaxiRideSource("/home/dknoo/IdeaProjects/flinkscalaproject/src/data/nycTaxiRides.gz", 60, 2000)) ;

  val ridesTerminal = rides
    .map(new GridCellMatcher)
    // partition by cell id and event type
    .keyBy( k => k )
    // filter out rides that are not starting or ending at a terminal
    .filter(k => gridToTerminal(k._1) != Terminal_404)
    // map grid id to terminal
    .map(k => gridToTerminal(k._1))
    // partition again for event time
    .keyBy(k => k)
    // build tumbling window
    .timeWindow(Time.hours(1))
    // count events in window
    .apply{ (key: (Terminal), window, vals, out: Collector[(Terminal, Int, Long)]) =>
      out.collect( (key, vals.size, window.getEnd)) }
    // map longtime to hour
    .map(new LongTimetoHour)

  ridesTerminal.print()


  env.execute()

  /**
  * Map longtime values to hours
  */
  class LongTimetoHour extends MapFunction[(Terminal, Int, Long), (Terminal, Int, Int)] {

    def map(trend: (Terminal, Int, Long)): (Terminal, Int, Int) = {
      val calendar = Calendar.getInstance()
      calendar.setTimeZone(TimeZone.getTimeZone("America/New_York"))
      calendar.setTimeInMillis(trend._3)
      (trend._1, trend._2, calendar.get(Calendar.HOUR_OF_DAY))
    }
  }

}



