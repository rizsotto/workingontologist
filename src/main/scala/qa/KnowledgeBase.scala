package qa

import org.apache.jena.rdf.model._
import org.apache.jena.reasoner.ReasonerRegistry
import org.apache.jena.util.FileManager

import scala.collection.JavaConverters._
import scala.util.Try


trait KnowledgeBase {
  import KnowledgeBase._

  def nextQuestion(): Option[QuestionWithOptions]
  def registerAnswer(answer: QuestionWithAnswer): Option[UnitType.type]
}

object KnowledgeBase {
  case object UnitType
  case class Question(url: Resource, text: String)
  case class Answer(url: Resource, text: String)
  case class QuestionWithOptions(q: Question, as: List[Answer])
  case class QuestionWithAnswer(q: Question, a: Answer)
}


class KnowledgeBaseImpl(model: InfModel) extends KnowledgeBase {
  import KnowledgeBase._
  import KnowledgeBaseImpl._

  override def nextQuestion(): Option[QuestionWithOptions] =
    for {
      qUrl <- selectNextQuestion()
      q <- getQuestionWithOptions(qUrl)
    } yield q


  override def registerAnswer(answer: QuestionWithAnswer): Option[UnitType.type] =
    getProperty("hasSelectedOption").map { property =>
      answer.q.url.addProperty(property, answer.a.url)
    }.map(_ => UnitType)

  private def selectNextQuestion(): Option[Resource] = {
    val hasType = model.getProperty(s"$rdfNs#type")

    getResource("CandidateQuestion")
      .flatMap { obj =>
        model.listStatements(null, hasType, obj).toList.asScala
          .map(_.getSubject)
          // this shall be done by the owl
          .find { candidate =>
            ! candidate.hasProperty(hasType, model.getResource(s"$qNs#AnsweredQuestion"))
          }
      }
  }

  private def getQuestionWithOptions(subject: Resource): Option[QuestionWithOptions] =
    for {
      q <- getQuestion(subject)
      as <- getAnswers(q.url)
    } yield QuestionWithOptions(q, as)


  private def getAnswers(node: Resource): Option[List[Answer]] =
    getProperty("hasOption")
      .flatMap { property =>
        val answers = node.listProperties(property).toList.asScala
          .map(_.getResource).toList
          .map(getAnswer)
        sequence(answers)
      }

  private def getQuestion(node: Resource): Option[Question] =
    getProperty("questionText")
      .map { property =>
        val text = node.getProperty(property).getString
        Question(node, text)
      }

  private def getAnswer(node: Resource): Option[Answer] =
    getProperty("answerText")
      .map { property =>
        val text = node.getProperty(property).getString
        Answer(node, text)
      }

  private def getProperty(name: String): Option[Property] =
    Try(model.getProperty(s"$qNs#$name")).toOption

  private def getResource(name: String): Option[Resource] =
    Try(model.getResource(s"$qNs#$name")).toOption
}

object KnowledgeBaseImpl {
  def apply(): KnowledgeBaseImpl = {
    val schema = FileManager.get().loadModel("file:book/ch10/questionnaire.n3")
    val data = FileManager.get().loadModel("file:book/ch10/cableprovider.n3")

    val reasoner = ReasonerRegistry.getOWLReasoner.bindSchema(schema)

    val model = ModelFactory.createInfModel(reasoner, data)
    new KnowledgeBaseImpl(model)
  }

  private def sequence[A](a:List[Option[A]]):Option[List[A]] = a match {
    case Nil => Some(Nil)
    case h::t => h flatMap (r => sequence(t) map (r :: _))
  }

  private val qNs = "http://www.workingontologist.org/Examples/Chapter10/questionnaire.owl"
  private val rdfNs = "http://www.w3.org/1999/02/22-rdf-syntax-ns"
}
