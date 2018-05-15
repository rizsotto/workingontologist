package questionnaire

import org.apache.jena.rdf.model.Resource


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
