CREATE SCHEMA IF NOT EXISTS template_slick;

CREATE TABLE IF NOT EXISTS template_slick.student (
student_id VARCHAR PRIMARY KEY,
email VARCHAR unique,
name VARCHAR,
date_of_birth TIMESTAMP,
address json NULL);


CREATE TABLE IF NOT EXISTS template_slick.student_marks (
student_id VARCHAR REFERENCES template_slick.student(student_id) on delete cascade,
subject_id VARCHAR,
subject_name VARCHAR,
marks integer,
PRIMARY KEY(student_id, subject_id));