package com.knoldus.template.actor

import akka.actor.{Actor, ActorLogging, Props, Status}
import akka.pattern.pipe
import com.knoldus.template.persistence.components.StudentsDAO
import com.knoldus.template.persistence.dao._
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class StudentActor(studentsDAO: StudentsDAO) extends Actor with ActorLogging with LazyLogging {

  import StudentActor._

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason, message)
    sender() ! Status.Failure(reason)
  }

  //noinspection ScalaStyle
  override def receive: Receive = {
    //CREATE OPERATION
    case ValidateStudent(email: String) =>
      val res = checkStudent(email)
      res.pipeTo(sender())
    case AddStudent(studentDetails: StudentDetailsRequest) =>
      val res = addStudent(studentDetails)
      res.pipeTo(sender())
    //READ OPERATION
    case ValidatedStudent(studentId: String) =>
      val res = checkedStudent(studentId)
      res.pipeTo(sender())
    case GetStudent(stdId: String) =>
      val res: Future[Option[StudentDetails]] = getStudent(stdId)
      res.pipeTo(sender())
    //UPDATE OPERATION
    case UpdateValidation(studentId: String) =>
      val res: Future[Validated] = updatedStudent(studentId)
      res.pipeTo(sender())
    case UpdateStudent(studentDetails: StudentDetails) =>
      val res: Future[StudentDetails] = updateStudent(studentDetails)
      res.pipeTo(sender())
    //DELETE OPERATION
    case DeleteValidation(studentId: String) =>
      val res: Future[Validated] = deletedStudent(studentId)
      res.pipeTo(sender())
    case DeleteStudent(stdId: String) =>
      val res: Future[Boolean] = deleteStudent(stdId)
      res.pipeTo(sender())

  }

  private def checkStudent(email: String): Future[Validated] = {
    studentsDAO.checkIfStudentExists(email).map(res => Validated(res > 0))
  }

  private def checkedStudent(studentId: String): Future[Validated] = {
    studentsDAO.getifStudentFound(studentId).map(res => Validated(res > 0))
  }

  private def updatedStudent(studentId: String): Future[Validated] = {
    println("If id found to update")
    studentsDAO.updateifStudentFound(studentId).map(res => Validated(res > 0))
  }

  private def deletedStudent(studentId: String): Future[Validated] = {
    println("If the student found")
    studentsDAO.deleteifStudentFound(studentId).map(res => Validated(res > 0))
  }


  def addStudent(student: StudentDetailsRequest): Future[APIDataResponse] = {
    val studentDetails = StudentDetails(student.studentId, student.email, student.name, student.dateOfBirth) //      ,student.address)
    studentsDAO.addStudent(studentDetails).map { case 0 => logger.info(s"failed to create $studentDetails")
      Updated(false)
    case 1 => logger.info(s"created $studentDetails")
      Updated(true)
    }
  }

  def getStudent(studentId: String): Future[Option[StudentDetails]] = {
    studentsDAO.getStudent(studentId)
  }

  def updateStudent(student: StudentDetails): Future[StudentDetails] = {
    println("Updating Id") 
    val dbCall = studentsDAO.updateStudent(student: StudentDetails).map {
      case 0 =>
        println("student not found")
        student
      case _ =>
        println("Student Updated Successfully.")
        student
    }
    dbCall
  }


  def deleteStudent(studentId: String): Future[Boolean] = {
    println("deleting Id")
    val dbCall = studentsDAO.deleteStudent(studentId).map {
      case 0 => println("Student not found.")
        false
      case _ => println("Student deleted successfully.")
        true
    }
    dbCall
  }


}

object StudentActor {
  // commands
  sealed trait StudentActorMessage

  sealed trait APIDataResponse


  //responses
  final case class Updated(status: Boolean) extends APIDataResponse

  final case class Validated(status: Boolean) extends APIDataResponse

  //requests
  final case class ValidateStudent(email: String) extends StudentActorMessage

  final case class AddStudent(studentDetails: StudentDetailsRequest) extends StudentActorMessage

  final case class ValidatedStudent(studentId: String) extends StudentActorMessage

  final case class GetStudent(stdId: String) extends StudentActorMessage

  final case class UpdateValidation(studentId: String) extends StudentActorMessage

  final case class UpdateStudent(studentDetails: StudentDetails) extends StudentActorMessage

  final case class DeleteValidation(studentId: String) extends StudentActorMessage

  final case class DeleteStudent(stdId: String) extends StudentActorMessage


  def props(studentsDAO: StudentsDAO): Props = Props(new StudentActor(studentsDAO))
}
