package calculator.eval

import scala.annotation.tailrec

object Eval {
  val operators: Set[Char] = Set('+', '-', '*', '/')
  val numbers: Set[Char] = Set('1', '2', '3', '4', '5', '6', '7', '8', '9', '0')

  sealed trait ExprNode {
    def evaluate: Double
  }

  final case class Number(number: Double) extends ExprNode {
    override def evaluate: Double = {
      println(s"$this -> $number")
      number
    }
  }

  final case class Add(nodes: Seq[ExprNode]) extends ExprNode {
    override def evaluate: Double = {
      val res = nodes.map(_.evaluate).sum
      println(s"$this -> $res")
      res
    }
  }

  final case class Substract(nodes: Seq[ExprNode]) extends ExprNode {
    override def evaluate: Double = {
      val res = nodes.map(_.evaluate).reduce[Double] { case (l, r) => l - r }
      println(s"$this -> $res")
      res
    }
  }

  final case class Multiply(nodes: Seq[ExprNode]) extends ExprNode {
    override def evaluate: Double = {
      val res = nodes.map(_.evaluate).reduce[Double] { case (l, r) => l * r }
      println(s"$this -> $res")
      res
    }
  }

  final case class Divide(nodes: Seq[ExprNode]) extends ExprNode {
    override def evaluate: Double = {
      val res = nodes.map(_.evaluate).reduce[Double] {
        case (_, r) if r.isNaN || r == 0.0 => Double.NaN
        case (l, r) => println(s"$l / $r = ${l / r}"); l / r
      }
      println(s"$this -> $res")
      res
    }
  }

  final case class Expression(expression: String) extends ExprNode {
    override def evaluate: Double = {
      val res = parse(expression).evaluate
      println(s"$this -> $res")
      res
    }

    private def parse(expr: String): ExprNode = {
      if (expr.lastOption.exists(operators.contains)) parse(expr.init)
      else if (expr.contains('+')) Add(expr.split('+').map(parse))
      else if (expr.contains('-')) {
        if (expr.startsWith("--")) parse(expr.drop(2))
        else if (expr.head == '-') Substract(Seq(Number(0.0), parse(expr.tail)))
        else if (expr.contains("+-") || expr.contains("--") || expr.contains("*-") || expr.contains("/-"))
          parse(bubbleMinusUp(expr))
        else Substract(expr.split('-').map(parse))
      }
      else if (expr.contains('*')) Multiply(expr.split('*').map(parse))
      else if (expr.contains('/')) Divide(expr.split('/').map(parse))
      else Number(expr.toDoubleOption.getOrElse(Double.NaN))
    }

    // https://drive.google.com/file/d/1XEJy1LkOoYD5SdRQDR2rtZcVPpMuFhhV/view?usp=sharing
    @tailrec private def bubbleMinusUp(expr: String): String = {
      val simplifiedExpr =
        expr.replaceAll("^--", "")
          .replaceAll("\\+-", "-")
          .replaceAll("--", "+")

      val digraph =
        if (simplifiedExpr.contains("*-")) "*-"
        else if (simplifiedExpr.contains("/-")) "/-"
        else ""

      if (digraph == "") simplifiedExpr
      else expr.indexOf(digraph) match {
        case -1    => simplifiedExpr
        case index =>
          val diRemoved = simplifiedExpr.replaceFirst(digraph, if (digraph == "*-") "*" else "/")
          val prefix    = diRemoved.substring(0, index)
          prefix.findLast(operators.contains) match {
            case None =>
              bubbleMinusUp(s"-$diRemoved")
            case Some(op) =>
              val (before, after) = diRemoved.splitAt(prefix.lastIndexOf(op) + 1)
              bubbleMinusUp(s"$before-$after")
          }
      }
    }
  }

  def apply(expression: String): ExprNode = Expression(expression)
}
