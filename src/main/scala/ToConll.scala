class ToConll(val line: String) {
  lazy val conllBlock = for ((w, c) <- words.zipWithIndex) yield formatLine(w, c)
  val words = line.stripLineEnd.split(" ") map prune
  val prdIdx = line.stripLineEnd.split(" ") map (x => if (x.startsWith("[")) "01" else "-")
  val totalPrd = prdIdx.count(_.contains("01"))

  private def prune(w: String) = {
    if (w.startsWith("[") && w.endsWith("]")) w.drop(1).dropRight(1)
    else w
  }

  private def formatLine(w: String, c: Int) = {
    val prdId = prdIdx(c)
    val prdField = "*\t" * totalPrd
    s"-\t-\t$c\t$w\t-\t-\t-\t$prdId\t-\t-\t*\t$prdField-\n"
  }
}