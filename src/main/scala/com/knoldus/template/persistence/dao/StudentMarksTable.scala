package com.knoldus.template.persistence.dao


import com.knoldus.template.persistence.db.DatabaseApi.api._
import slick.lifted.{PrimaryKey, ProvenShape}

final class StudentMarksTable(tag: Tag)
  extends Table[StudentMarks](tag, Some("template_slick"), "student_marks") {

  def studentId: Rep[String] = column[String]("student_id")

  def subjectId: Rep[String] = column[String]("subject_id")

  def subjectName: Rep[String] = column[String]("subject_name")

  def marks: Rep[Int] = column[Int]("marks")

  def * : ProvenShape[StudentMarks] =
    (studentId, subjectId, subjectName, marks).shaped <> (StudentMarks.tupled, StudentMarks.unapply)

  implicit def primary: (Rep[Option[String]], Rep[String]) =
    (studentId, subjectId)

  def pk: PrimaryKey = primaryKey("pk_a", (studentId, subjectId))
}
