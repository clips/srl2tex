import scala.collection.mutable.ArrayBuffer

/**
  * Utilities related to typesetting text in latex.
  */


object Tex {

  val Header: String =
    """\documentclass[6pt]{article}
      |\""".stripMargin +
      """usepackage[utf8]{inputenc}
        |\""".stripMargin +
      """usepackage{tikz,graphicx,tikz-dependency}
        |\""".stripMargin +
      """usepackage[paperwidth=30cm, paperheight=20cm, margin=10pt]{geometry}
        |
        |\begin{document}""".stripMargin
  val Footer: String =
    """
      |
      |\end{document}
    """.stripMargin
  val SpaceBetweenTikz: String = """\vspace{0.5cm}"""
  val DeptextFooter: String =
    """
      |\end{deptext}""".stripMargin
  val TikzFooter: String =
    """
      |\end{dependency}""".stripMargin

  def groupsAndArcs(words: ArrayBuffer[String], props: ArrayBuffer[String], isPredicted: Boolean = false): String = {
    var groupsStr = ""
    var arcsStr = ""
    val gs = groups(words, props)
    val vGroup = gs.filter(g => g.arg == "V")
    assert(vGroup.length == 1)
    val vId = vGroup(0).groupId
    val vMeanLoc = vGroup(0).meanLoc // for determining height of arc
    gs.foreach(g => {
      val color = if (g.arg == "V") "green" else "brown"
      groupsStr +=
        """
          |\wordgroup[group style={fill=%s!50,draw=%s!70}]{%s}{%s}{%s}{%s}""".stripMargin.
          format(
            color,
            color,
            if (isPredicted) "2" else "1",
            g.startI + 1,
            g.endI + 1,
            if (isPredicted) "_" + g.groupId else g.groupId // mark if coming from predicted
          )
      if (g.arg != "V") arcsStr +=
        """
          |\groupedge[edge %s]{%s}{%s}{%s}{%sex}""".stripMargin.format(
          if (isPredicted) "below" else "above",
          if (isPredicted) "_" + vId else vId,
          if (isPredicted) "_" + g.groupId else g.groupId,
          g.arg,
          (vMeanLoc - g.meanLoc).abs
        )
    })
    groupsStr + arcsStr
  }

  def texString(ws: ArrayBuffer[String], ps: ArrayBuffer[String]): String =
    createTikzHeader() +
      txt(ws) +
      DeptextFooter +
      groupsAndArcs(ws, ps) +
      TikzFooter

  def texString(ws: ArrayBuffer[String], ps: ArrayBuffer[String], ps_pred: ArrayBuffer[String]): String =
    createTikzHeader() +
      txt(ws) + "\n" +
      txt(ws) + //once for each gold and predicted
      DeptextFooter +
      groupsAndArcs(ws, ps) +
      groupsAndArcs(ws, ps_pred, isPredicted = true) +
      TikzFooter

  def createTikzHeader(colSep: Double = .5, rowSep: Double = .1): String = {
    val cSep = colSep.toString + "cm"
    val rSep = rowSep.toString + "cm"

    val tikzHeader =
      """
        |
        |\begin{dependency}[label style={font=\bfseries}]
        |\begin{deptext}[column sep=%s, row sep=%s, font=\footnotesize]
        | """.stripMargin.format(cSep, rSep)
    tikzHeader
  }

  private def txt(words: ArrayBuffer[String]): String = {
    if (words.isEmpty)
      """ \\""".stripMargin
    else if (words.length == 1) normalize(words(0)) + txt(words.tail)
    else normalize(words(0)) + """ \& """ + txt(words.tail)
  }

  private def normalize(w: String) = w match {
    case "%" | "$" | "&" => """\""" + w
    case "\u2011" => "-"
    case _ => w
  }

  private def groups(words: ArrayBuffer[String], props: ArrayBuffer[String]): ArrayBuffer[Group] = {
    val groups = ArrayBuffer.empty[Group]
    var groupId = 0
    var prev = ""
    var startI = 0
    var endI = 0
    for ((p, i) <- props.zipWithIndex) {
      if (p.startsWith("B-")) {
        if (prev.nonEmpty) {
          // create previous
          groups += Group(prev, groupId, startI, endI)
          groupId += 1
          // start new
          startI = i
          prev = p.slice(2, p.length)
          endI = i
        }
        else {
          startI = i
          prev = p.slice(2, p.length)
          endI = i
        }
      }
      else if (p.startsWith("I-")) {
        assert(prev.nonEmpty)
        endI = i
      }
      else if (p.startsWith("O")) {
        if (prev.nonEmpty) {
          groups += Group(prev, groupId, startI, endI)
          groupId += 1
          prev = ""
        }
      }
    }
    groups
  }

  case class Group(arg: String, groupId: Int, startI: Int, endI: Int) {
    lazy val meanLoc: Double = (startI.toDouble + endI.toDouble) / 2
  }
}