import scala.collection.immutable.IndexedSeq
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

case class Instance(word: String,
                    tag: String,
                    syn: String,
                    ne: String,
                    prd: String,
                    prop: Option[Array[String]]) {
  // auxiliary constructor
  def this(prd: String, prop: Option[Array[String]]) = this("", "", "", "", prd, prop)

}

class Conll2012(val filePath: String) {
  val sents = ArrayBuffer.empty[ArrayBuffer[Instance]]

  def load(): Unit = {
    var sent = ArrayBuffer.empty[Instance]
    Source.fromFile(filePath).getLines().
      filterNot(_.startsWith("#begin document")).
      filterNot(_.startsWith("#end document")).
      foreach(line => {
        val es = line.stripLineEnd.split(" +")
        val esLength = es.length
        if (esLength > 1) {
          val inst = Instance(
            es(3), //.toLowerCase,
            es(4),
            es(5),
            es(10),
            es(7),
            if (esLength > 12) Some(es.slice(11, esLength - 1)) else None
          )
          sent += inst
        }
        else {
          sents += sent
          sent = ArrayBuffer.empty[Instance]
        }
      })
    if (sent.nonEmpty) sents += sent
  }

  def getNWords = getWords.flatten.length

  def getWords = for (sent <- sents) yield sent.map(inst => inst.word)

  def getNProps = getProps.flatten.length

  def getProps = for (sent <- sents) yield sent.flatMap(inst => inst.prop)

  def getNPropsBio = getPropsBio.flatten.length

  def getPropsBio = for (sent <- sents) yield toBio(sent.flatMap(inst => inst.prop))

  def toBio(props: ArrayBuffer[Array[String]]) = {
    if (props.nonEmpty) {
      val nProps = props(0).length
      //obtain each proposition as a seq:
      val sentProps = for (i <- 0 until nProps) yield props.map(prop => prop(i))
      sentProps.map(sentProp => bio(sentProp))
    }
    else IndexedSeq.empty[ArrayBuffer[String]]
  }

  def bio(sentProp: ArrayBuffer[String]): ArrayBuffer[String] = {
    var bios = ArrayBuffer.empty[String]
    var prev = ""
    for (arg <- sentProp) {
      if (arg.startsWith("(")) {
        if (arg.endsWith(")")) {
          prev = arg.slice(1, arg.length - 2)
          bios += "B-" + prev
          prev = ""
        }
        else {
          prev = arg.slice(1, arg.length - 1)
          bios += "B-" + prev
        }
      }
      else {
        if (prev.nonEmpty) {
          bios += "I-" + prev
          if (arg.endsWith(")")) prev = ""
        }
        else bios += "O"
      }
    }
    bios
  }

}

/**
  * Representing the predicted structures, which is amputated conll format.
  */

class ConllPredicted(filePath: String) extends Conll2012(filePath) {
  override def load(): Unit = {
    var sent = ArrayBuffer.empty[Instance]
    Source.fromFile(filePath).getLines().
      filterNot(_.startsWith("#begin document")).
      filterNot(_.startsWith("#end document")).
      foreach(line => {
        val es = line.stripLineEnd.split("\t")
        val esLength = es.length
        if (line.stripLineEnd.nonEmpty) {
          val inst = Instance(
            es(0),
            if (esLength > 1) Some(es.slice(1, esLength)) else None
          )
          sent += inst
        }
        else {
          sents += sent
          sent = ArrayBuffer.empty[Instance]
        }
      })
    if (sent.nonEmpty) sents += sent
  }
}

object Instance {
  def apply(prd: String, prop: Option[Array[String]]) = new Instance(prd, prop)
}

