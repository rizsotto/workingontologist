package questionnaire

import questionnaire.KnowledgeBase.QuestionWithAnswer

import scala.io.StdIn
import scala.util.{Failure, Success, Try}


object Main extends App {

  val kb: KnowledgeBase = JenaKnowledgeBase(
    "book/ch10/questionnaire.n3",
    "book/ch10/cableprovider.n3")

  Stream.continually {
    for {
      q <- kb.nextQuestion()
      a <- ask(q)
      _ <- kb.registerAnswer(a)
    } yield KnowledgeBase.UnitType
  }.dropWhile(_.isSuccess)

  
  def ask(question: KnowledgeBase.QuestionWithOptions): Try[KnowledgeBase.QuestionWithAnswer] = {
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
  }
}
