package com.knoldus.template.handler

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.knoldus.template.actor.StudentActor._
import com.knoldus.template.persistence.dao._
import com.knoldus.template.util.JsonHelper
import com.typesafe.scalalogging.LazyLogging
import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

trait StudentServiceHandler extends JsonHelper with LazyLogging {

  implicit val system: ActorSystem

  implicit val materializer: ActorMaterializer
  implicit val timeOut: Timeout = Timeout(40 seconds)

  import akka.pattern.ask
  import system.dispatcher


  def createStudent(command: ActorRef, studentDetails: StudentDetailsRequest): Future[HttpResponse] = {
    ask(command, ValidateStudent(studentDetails.email)).flatMap {
      case Validated(true) =>
        Future.successful(
          HttpResponse(status = StatusCodes.UnprocessableEntity,
            entity = write("User already exists")))
      case Validated(false) =>
        ask(
          command,
          AddStudent(
            studentDetails.copy(studentId = UUID.randomUUID().toString)))
          .map {
            case Updated(true) =>
              HttpResponse(status = StatusCodes.Created,
                entity = write("USER_ACCOUNT_CREATED"))
            case Updated(false) =>
              HttpResponse(status = StatusCodes.Conflict,
                entity = write("FAILED_TO_CREATE_ACCOUNT"))
          }
    }
  }

  def getStudent(command: ActorRef, stdId: String): Future[HttpResponse] = {
    ask(command, ValidatedStudent(stdId)).flatMap {
      case Validated(true) =>
        println(s"Validated student id $stdId")
        ask(
          command,
          GetStudent(
            stdId)).mapTo[Option[StudentDetails]]
          .map {
            case Some(value) =>
              HttpResponse(status = StatusCodes.OK, entity = write(value.toString))
            case None =>
              HttpResponse(status = StatusCodes.Conflict, entity = write("Student not Found"))
          }
    }
  }

  def updateStudent(command: ActorRef, StudentDetails: StudentDetails): Future[HttpResponse] = {
    ask(command, UpdateValidation(StudentDetails.studentId)).flatMap {
      case Validated(true) =>
        println(s"Validating student details $StudentDetails") //not working
        ask(
          command,
          UpdateStudent(
            StudentDetails)).mapTo[Option[StudentDetails]]
          .map {
            case Some(value) =>
              HttpResponse(status = StatusCodes.OK, entity = write(value.toString))
            case None =>
              HttpResponse(status = StatusCodes.Conflict, entity = write("Student not Found"))
          }

      case Validated(false) =>
        println("student not found")
    Future.successful(HttpResponse(status = StatusCodes.Conflict, entity = write("Student not Found")))
    }
  }

  def deleteStudent(command: ActorRef, stdId: String): Future[HttpResponse] = {
    ask(command, DeleteValidation(stdId)).flatMap {
      case Validated(true) =>
        println(s"Validate Id to delete $stdId")
        ask(command, DeleteStudent(stdId)).mapTo[Boolean].map { x =>
          println(s"$x")
          if (x) {
            HttpResponse(status = StatusCodes.OK, entity = write(true.toString))
          } else {
            HttpResponse(status = StatusCodes.Conflict, entity = write("Student not Found"))
          }
        }

      case Validated(false) =>
        Future.successful(HttpResponse(status = StatusCodes.Conflict, entity = write("Student not Found")))


    }
  }


}

