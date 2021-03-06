package net.sansa_stack.examples.spark.ml.mining

import scala.collection.mutable
import org.apache.spark.sql.SparkSession
import net.sansa_stack.rdf.spark.model.JenaSparkRDDOps
import net.sansa_stack.ml.spark.mining.amieSpark.KBObject.KB
import net.sansa_stack.ml.spark.mining.amieSpark.{ RDFGraphLoader, DfLoader }
import net.sansa_stack.ml.spark.mining.amieSpark.MineRules.Algorithm
import net.sansa_stack.ml.spark.mining.amieSpark.MineRules.Algorithm

/*
 * Mine Rules
 * 
 */
object MineRules {

  def main(args: Array[String]) = {
    if (args.length < 1) {
      System.err.println(
        "Usage: Mine Rules <input> <output>")
      System.exit(1)
    }
    val input = args(0) //"src/main/resourcesMineRules_sampledata.tsv"
    val outputPath = args(1)
    val hdfsPath = outputPath + "/"

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
    println("|        Mines the Rules example     |")
    println("======================================")

    val sparkSession = SparkSession.builder
      .master("local[*]")
      .appName(" Mines the Rules example (" + input + ")")
      .getOrCreate()

    val ops = JenaSparkRDDOps(sparkSession.sparkContext)
    import ops._

    val know = new KB()
    know.sethdfsPath(hdfsPath)
    know.setKbSrc(input)

    know.setKbGraph(RDFGraphLoader.loadFromFile(know.getKbSrc(), sparkSession.sparkContext, 2))
    know.setDFTable(DfLoader.loadFromFileDF(know.getKbSrc, sparkSession.sparkContext, sparkSession.sqlContext, 2))

    val algo = new Algorithm(know, 0.01, 3, 0.1, hdfsPath)

    //var erg = algo.ruleMining(sparkSession.sparkContext, sparkSession.sqlContext)
    //println(erg)
    var output = algo.ruleMining(sparkSession.sparkContext, sparkSession.sqlContext)

    var outString = output.map { x =>
      var rdfTrp = x.getRule()
      var temp = ""
      for (i <- 0 to rdfTrp.length - 1) {
        if (i == 0) {
          temp = rdfTrp(i) + " <= "
        } else {
          temp += rdfTrp(i) + " \u2227 "
        }
      }
      temp = temp.stripSuffix(" \u2227 ")
      temp
    }.toSeq
    var rddOut = sparkSession.sparkContext.parallelize(outString)

    rddOut.saveAsTextFile(outputPath + "/testOut")

    sparkSession.stop

  }

}