import java.io.{File, PrintWriter}

import Tex.{Footer, Header, texString}

import scala.collection.immutable.IndexedSeq
import scala.collection.mutable.ArrayBuffer
import scala.io.Source


object Viz {
  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      println("Usage:\njava -jar srl2tex.jar INPUT_PRED INPUT_GOLD\nwhere INPUT_PRED is the file containing the " +
        "output of SRL in compact CoNLL format and INPUT_GOLD are the gold SRL annotations in CoNLL2012 format.")
      System.exit(1)
    }

    val inputPred = args(0)
    val inputGold = args(1)
    val inputPredLines = Source.fromFile(inputPred).getLines()

    val outputTexFile = "output.tex"
    val outTexFile = new PrintWriter(new File(outputTexFile))

    // gold as reference
    val corpusGold = new Conll2012(inputGold)
    corpusGold.load()

    val corpusPred = new ConllPredicted(inputPred)
    corpusPred.load()

    assert(corpusGold.getNWords == corpusPred.getNWords)
    assert(corpusGold.getNPropsBio == corpusPred.getNPropsBio)


    val words: ArrayBuffer[ArrayBuffer[String]] = corpusGold.getWords
    val propsBio: ArrayBuffer[IndexedSeq[ArrayBuffer[String]]] = corpusGold.getPropsBio
    val propsBioPred: ArrayBuffer[IndexedSeq[ArrayBuffer[String]]] = corpusPred.getPropsBio
    val texStrings: IndexedSeq[String] = {
      //for ((ws, ps) <- words.zip(propsBio) if ps.nonEmpty; single_ps <- ps) yield texString(ws, single_ps)
      for (i <- words.indices; j <- propsBio(i).indices) yield texString(words(i), propsBio(i)(j), propsBioPred(i)(j))
    }
    outTexFile.write(Header)
    texStrings.foreach(outTexFile.write)
    outTexFile.write(Footer)
    outTexFile.close()

  }
}
