package config

import scala.collection.immutable.HashMap

/*
singleton object for setting configuration. Configuration is defined per environment in the config.json file
in the resources directory. The environment to be used is passed via command line argument.
The program will exit if unknown environment is provided.
 */

object Configuration {
  val environment: String = System.getProperty("ENV", "dev")
//  if (!environment.equalsIgnoreCase("test") && !environment.equalsIgnoreCase("dev")) {
//    println("********************************************************************************")
//    println(s"ERROR: No configuration for environment ${environment}!!!")
//    println("********************************************************************************")
//    sys.exit(0)
//  }

  val jsonString = os.read(os.pwd / "src" / "test" / "resources" / "config.json")
  val data = ujson.read(jsonString)
  val environmentData = data(environment)
  var url: String = environmentData("url").str

  var rampUpUsers: Int = environmentData("rampUpUsers").num.asInstanceOf[Int]
  var peakUsers: Int = environmentData("peakUsers").num.asInstanceOf[Int]
  var rampUpDuration: Int = environmentData("rampUpDuration").num.asInstanceOf[Int]
  var rampDownDuration: Int = environmentData("rampDownDuration").num.asInstanceOf[Int]
  var peakDuration: Int = environmentData("peakDuration").num.asInstanceOf[Int]

  println("*******************************************************************")
  println(s"Environment       : $environment")
  println(s"Base url          : $url")
  println(s"Ramp up users     : $rampUpUsers")
  println(s"Ramp down duration: $rampDownDuration")
  println(s"Ramp up duration  : $rampUpDuration")
  println(s"Peak users        : $peakUsers")
  println(s"Peak duration     : $peakDuration")
  println("*******************************************************************")

  val voteCitations: Seq[Int] = List(0, 1, 2, 3, 4, 5, 6, 7)
}