package qa

import qa.KnowledgeBase.QuestionWithAnswer

import scala.io.StdIn
import scala.util.{Failure, Success, Try}

object Main extends App {

  val model = KnowledgeBaseImpl()
  Stream.continually {
    for {
      q <- model.nextQuestion()
      a <- ask(q)
      _ <- model.registerAnswer(a)
    } yield KnowledgeBase.UnitType
  }.dropWhile(_.isDefined)

  def ask(question: KnowledgeBase.QuestionWithOptions): Option[KnowledgeBase.QuestionWithAnswer] = {
    println
    println(s"${question.q.text}")
    println
    question.as.zipWithIndex.foreach { case (a, idx) =>
      println(s"  $idx: ${a.text}")
    }
    println
    print("Your answer: ")

    val max = question.as.length
    Try(StdIn.readInt)
      .flatMap { idx: Int =>
        if (idx >= max) Failure(new IllegalArgumentException("index out of range")) else Success(idx)
      }
      .map { idx: Int =>
        QuestionWithAnswer(question.q, question.as(idx))
      }
      .toOption
  }
}
