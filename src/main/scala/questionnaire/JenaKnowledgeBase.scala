package questionnaire

import org.apache.jena.rdf.model.{InfModel, ModelFactory, Property, Resource}
import org.apache.jena.reasoner.ReasonerRegistry
import org.apache.jena.util.FileManager

import scala.collection.JavaConverters._
import scala.util.{Success, Try}


class JenaKnowledgeBase(model: InfModel) extends KnowledgeBase {
  import JenaKnowledgeBase._
  import KnowledgeBase._

  override def nextQuestion(): Try[QuestionWithOptions] =
    for {
      qUrl <- selectNextQuestion()
      q <- getQuestionWithOptions(qUrl)
    } yield q

  override def registerAnswer(answer: QuestionWithAnswer): Try[UnitType.type] =
    Try.apply {
      answer.q.url.addProperty(hasSelectedOption, answer.a.url)
    }
    .map(_ => UnitType)


  private def selectNextQuestion(): Try[Resource] =
    Try.apply {
      model.listStatements(null, hasType, CandidateQuestion).toList.asScala
        .map(_.getSubject)
        .filter { candidate => !candidate.hasProperty(hasType, AnsweredQuestion) }
        .head
    }

  private def getQuestionWithOptions(subject: Resource): Try[QuestionWithOptions] =
    for {
      q <- getQuestion(subject)
      as <- getAnswers(q.url)
    } yield QuestionWithOptions(q, as)

  private def getAnswers(node: Resource): Try[List[Answer]] =
    sequence {
      node.listProperties(hasOption).toList.asScala
        .map(_.getResource).toList
        .map(getAnswer)
    }

  private def getQuestion(node: Resource): Try[Question] =
    Try.apply { node.getProperty(questionText).getString }
      .map(Question(node, _))

  private def getAnswer(node: Resource): Try[Answer] =
    Try.apply { node.getProperty(answerText).getString }
      .map(Answer(node, _))


  private lazy val hasType = model.getProperty(s"$rdfNs#type")

  private lazy val answerText: Property = model.getProperty(s"$qNs#answerText")
  private lazy val questionText: Property = model.getProperty(s"$qNs#questionText")
  private lazy val hasOption: Property = model.getProperty(s"$qNs#hasOption")
  private lazy val hasSelectedOption: Property = model.getProperty(s"$qNs#hasSelectedOption")

  private lazy val CandidateQuestion: Resource = model.getResource(s"$qNs#CandidateQuestion")
  private lazy val AnsweredQuestion: Resource = model.getResource(s"$qNs#AnsweredQuestion")
}

object JenaKnowledgeBase {
  def apply(schemaFile: String, dataFile: String): JenaKnowledgeBase = {
    val schema = FileManager.get().loadModel(s"file:$schemaFile")
    val data = FileManager.get().loadModel(s"file:$dataFile")

    val reasoner = ReasonerRegistry.getOWLReasoner.bindSchema(schema)

    val model = ModelFactory.createInfModel(reasoner, data)
    new JenaKnowledgeBase(model)
  }

  private def sequence[A](a:List[Try[A]]): Try[List[A]] = a match {
    case Nil => Success(Nil)
    case h::t => h flatMap (r => sequence(t) map (r :: _))
  }

  private val qNs = "http://www.workingontologist.org/Examples/Chapter10/questionnaire.owl"
  private val rdfNs = "http://www.w3.org/1999/02/22-rdf-syntax-ns"
}
