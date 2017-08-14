Sample grading.xml and security.policy files for a database assignment
that uses embedded Apache Derby.

This example is based on Cay Horstmann's SimpleDataSource to configure
access to the JDBC database via the database.properties. The
/homework/program element uses a copyFile element to copy the
database.properties file into the student's grading directory for use
by the database program's SimpleDataSource class.

The security.policy file is fairly complex to allow derby.jar to read
properties and read & write database files.
