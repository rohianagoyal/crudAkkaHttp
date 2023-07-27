package com.knoldus.template.routes

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.knoldus.template.handler.StudentServiceHandler
import com.knoldus.template.persistence.dao.{StudentDetails, StudentDetailsRequest}

import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

trait StudentService extends StudentServiceHandler with LazyLogging {

  //CREATE OPERATION
  private def studentPostRoutes(actor: ActorRef): Route = {
    pathPrefix("student") {
      path("create-student-record") {
        pathEnd {
          (post & entity(as[StudentDetailsRequest])) { request =>
            logger.info(s"StudentService: creating a new student:  $request")
            val response = createStudent(actor, request)
            complete(response)
          }
        }
      }

    }
  }

  // READ OPERATION
  private def studentGetRoutes(actor: ActorRef): Route = {
    pathPrefix("student") {
      path("get-student-record") {
        parameter("stdId") { stdId =>
          pathEnd {
            get {
              logger.info(s"StudentService: Getting a students: $stdId")
              val response = getStudent(actor, stdId)
              complete(response)
            }
          }
        }
      }
    }
  }

  //UPDATE OPERATION
  private def studentPutRoutes(actor: ActorRef): Route = {
    pathPrefix("student") {
     println("Routing started")
      path("update-student-record") {
        pathEnd {
          (put & entity(as[StudentDetails])) { StudentDetails =>
              logger.info(s"StudentServices: Updating a student: $StudentDetails")
              val response = updateStudent(actor, StudentDetails)
              complete(response)
          }
        }
      }
    }
  }

  //DELETE OPERATION
  private def studentDeleteRoutes(actor: ActorRef): Route = {
    pathPrefix("student") {
      path("delete-student-record") {
        parameter("stdId") { stdId =>
          pathEnd {
            delete {
              logger.info(s"StudentService: Deleting a Student: $stdId")
              val response = deleteStudent(actor, stdId)
              complete(response)
            }
          }
        }
      }
    }
  }


  def getUserRoutes(command: ActorRef): Route = studentPostRoutes(command) ~ studentGetRoutes(command) ~ studentPutRoutes(command) ~ studentDeleteRoutes(command)


}
