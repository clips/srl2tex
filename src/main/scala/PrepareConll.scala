/**
  * import java.io.{File, PrintWriter}
  * *
  * import scala.io.Source
  * *
  *
  * object PrepareConll {
  * def main(args: Array[String]): Unit = {
  * if (args.length < 1) {
  * println("Usage:\njava -jar srl2tex.jar INPUT\nwhere INPUT is the file containing one sentence " +
  * "per line, with the predicate in square brackets.")
  *System.exit(1)
  * }
  * *
  * val inputFile = args(0) // input file with sentences
  * val inputLines = Source.fromFile(inputFile).getLines()
  * *
  * val outputConllFile = inputFile + ".conll"
  * val outConll = new PrintWriter(new File(outputConllFile))
  * *
  * for (line <- inputLines) {
  * val c = new ToConll(line)
  *c.conllBlock.foreach(outConll.write)
  *outConll.write("\n")
  * }
  *outConll.close()
  * }
  * }
  **/