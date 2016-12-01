package net.sansa_stack.examples.spark.rdf

import java.io.File
import scala.collection.mutable
import org.apache.spark.sql.SparkSession
import net.sansa_stack.rdf.spark.graph.LoadGraph
/*
 * Computes the PageRank of Resources from an input .nt file.
 */
object PageRank {

  def main(args: Array[String]) = {
    if (args.length < 2) {
      System.err.println(
        "Usage: Resource PageRank <input>")
      System.exit(1)
    }
    val input = args(0)
    val output = args(1)
    val optionsList = args.drop(2).map { arg =>
      arg.dropWhile(_ == '-').split('=') match {
        case Array(opt, v) => (opt -> v)
        case _             => throw new IllegalArgumentException("Invalid argument: " + arg)
      }
    }
    val options = mutable.Map(optionsList: _*)

    options.foreach {
      case (opt, _) => throw new IllegalArgumentException("Invalid option: " + opt)
    }
    println("======================================")
    println("|   PageRank of resources example    |")
    println("======================================")

    val sparkSession = SparkSession.builder
      .master("local[*]")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .appName("Resource PageRank example (" + input + ")")
      .getOrCreate()

    val graph = LoadGraph.apply(input, sparkSession.sparkContext).graph

    /*
    val pagerank = graph.pageRank(0.00001).vertices

    val report = pagerank.join(vertices)
      .map({ case (k, (r, v)) => (r, v, k) })
      .sortBy(50 - _._1)

    report.take(50).foreach(println)
    */
    sparkSession.stop

  }

}