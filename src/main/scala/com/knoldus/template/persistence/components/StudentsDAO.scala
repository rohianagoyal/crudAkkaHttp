package com.knoldus.template.persistence.components

import com.knoldus.template.persistence.dao.{StudentDetails, StudentTable}
import com.knoldus.template.persistence.db.DatabaseApi.api._
import com.typesafe.scalalogging.LazyLogging
import scala.concurrent.Future
import slick.lifted.TableQuery


class StudentsDAO(implicit val db: Database) extends LazyLogging {
  private val studentsQuery = TableQuery[StudentTable]
  //  val studentMarksQuery = TableQuery[StudentMarksTable]

  /**
    * ------------------------------------------------------------------------------------------
    * INSERT INTO template_slick.student
    * (student_id, email, "name", date_of_birth, address)
    * VALUES('123cc518-8f19-413c-bfde-0c5415ba00e8', 'email@gmail.com', 'name', '1996-02-18 20:40:40.000', '{
    * "street1" : "street1",
    * "street2" : "street2",
    * "landmark" : "landmark",
    * "city" : "city",
    * "state" : "state",
    * "country" : "India",
    * "pinCode" : "PB3103"
    * }'::json::json);
    * -------------------------------------------------------------------------------------------------
    */
  def addStudent(student: StudentDetails): Future[Int] = {
    db.run(studentsQuery += student)
  }

  /**
    * ----------------------------------------------------------------------------
    * select count(*) from template_slick.student s where s.student_id ='student_id';
    * ----------------------------------------------------------------------------
    */
  def checkIfStudentExists(id: String): Future[Int] = {
    db.run(
      studentsQuery
        .filter(col =>
          col.email === id ||
            col.studentId === id)
        .size
        .result)
  }

  /** SELECT * FROM template_slick.student WHERE
    * student_id='10624631-9ec0-47fa-a4d0-9fbc130c0666'; * */
  def getStudent(studentId: String): Future[Option[StudentDetails]] = {
    db.run(TableQuery[StudentTable].filter(_.studentId === studentId).result.headOption)
  }

  def getifStudentFound(id: String): Future[Int] = {
    db.run(
      studentsQuery
        .filter(col =>
          col.studentId === id)
        .size
        .result
    )
  }

  def updateStudent(student: StudentDetails): Future[Int] = {
    println(s"Updating student $StudentDetails") //not wworking
    val updateAction = TableQuery[StudentTable].filter(_.studentId === student.studentId).map { student =>
      (student.name, student.email, student.dateOfBirth)
    }.update((student.name, student.email, student.dateOfBirth))

    db.run(updateAction)
  }

  def updateifStudentFound(id: String): Future[Int] = {
    println("filtering")
    db.run(
      studentsQuery
        .filter(col =>
          col.studentId === id)
        .size
        .result
    )
  }

  def deleteStudent(studentId: String): Future[Int] = {
    println(s"Deleting the student with ID: $studentId")

    val query = TableQuery[StudentTable].filter(_.studentId === studentId).delete

    db.run(query)
  }

  def deleteifStudentFound(id: String): Future[Int] = {
    println("Filtering with the student Id")
    db.run(
      studentsQuery
        .filter(col =>
          col.studentId === id)
        .size
        .result

    )
  }


}
