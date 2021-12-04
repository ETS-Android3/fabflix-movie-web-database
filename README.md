- # General

  - #### Team#: 5

  - #### Names: Tristin Bui, Soobin Woo

  - #### Project 5 Video Demo Link: https://youtu.be/CeCvSlJNv04

  - #### Instruction of deployment:

    Deploying our application with Tomcat involved packaging our maven project
    which resulted in a war file being created that can moved into the tomcat
    webapp directory which can post the live web application on the tomcat
    server.

  - #### Collaborations and Work Distribution:
    Tristin Bui: SQL Master-Slave Replication, Load Balancing, JMeter
    Soobin Woo: Connection Pooling, TS/TJ Logs, log_processing script

- # Connection Pooling

  - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
    AddMovieServle.java, AddStarServlet.java, EmployeeLoginServlet.java, GenreServlet.java, LoginServlet.java, MetadataServlet.java, MovieServlet.java, MovieSuggestion.java, SingleMovieServlet.java, SingleStarServlet.java
  - #### Explain how Connection Pooling is utilized in the Fabflix code.
    We specify in context.xml to utilize connection pooling for each JDBC DataSource in the servlets. When each servlet runs and needs to connect to the database, it can grab a connection from a preallocated pool of connections that can be utilized to send a query to the database. When the application is done getting the database results, the application will close the connection which will go back to the pool to be used by another datasource.
  - #### Explain how Connection Pooling works with two backend SQL.
    The two backend SQLS are the master instance which reads/writes and the slave instance which reads in which each resource acts as our connection pool. Our main instance uses Apache to send requests to either the master or slave instances which is possible because of the load balancer member that was added to both slave and master instances.

- # Master/Slave

  - #### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.
    [context.xml](WebContent/META-INF/context.xml)  
    [context.xml](src/AddMovieServlet.java)  
    [context.xml](src/AddStarServlet.java)  

  - #### How read/write requests were routed to Master/Slave SQL?
    Read requests were routed to localhost on both Master and Slave SQL servers since reading can be done on any database. 
    Write requests were routed to the Master SQL database by labeling the IP address of the Master in [context.xml](WebContent/META-INF/context.xml) which all
    writes routed to this database, the Master database.

- # JMeter TS/TJ Time Logs

  - #### Instructions of how to use the `log_processing.*` script to process the JMeter logs.
    Ensure that the log files are in the same directory as the log_processing.java script if it is not already. Compile log_processing.java and run the file.

- # JMeter TS/TJ Time Measurement Report

| **Single-instance Version Test Plan**         | **Graph Results Screenshot**  | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis**                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| --------------------------------------------- | ----------------------------- | -------------------------- | ----------------------------------- | ------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Case 1: HTTP/1 thread                         | ![](jmeter_reports/case1.png) | 546                        | 322.810896                          | 322.760726                | This test plan and the scaled test plan with 1 thread are similar in the report times. Siince it is only running one thread, there is less stress on the server so it will be significantly faster compared to using multiple threads as shown in case 2/3.                                                                                                                                                                                                                |
| Case 2: HTTP/10 threads                       | ![](jmeter_reports/case2.png) | 3927                       | 476.618717                          | 476.518858                | This test case is similar in query time with case 3 which uses https. Since it uses a much more threads compared to one thread (case 1), it is significantly slower. It is also slower than the scaled version that uses multiple threads since the single instance version can not handle a heavy load as efficiently compared to the scaled version.                                                                                                                     |
| Case 3: HTTPS/10 threads                      | ![](jmeter_reports/case3.png) | 3878                       | 487.04293                           | 486.969947                | This test case is similar in report times with case 2 which uses http. Compared to sing a single thread, it runs significantly slower since the multiple threads will be more demanding on the server. However, it is faster than using the same number of threads without connection pooling because it is able to grab a preallocated connection everytime a request is made rather than having to go through the process of creating a new connection for each request. |
| Case 4: HTTP/10 threads/No connection pooling | ![](jmeter_reports/case4.png) | 4122                       | 664.201858                          | 643.614553                | No connection pooling will slow down the average query times because every request needs to create a new connection and process a query rather than simply grabbing a connection from a preallocated pool. This test plan in particular has the slowest avg query time because compared to the scaled no pooling test plan, it takes longer for the single instance since the scaled versions were created to handle more load.                                            |

| **Scaled Version Test Plan**                  | **Graph Results Screenshot**         | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis**                                                                                                                                                                                                                                                                                                                                                                                              |
| --------------------------------------------- | ------------------------------------ | -------------------------- | ----------------------------------- | ------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Case 1: HTTP/1 thread                         | ![](jmeter_reports/case1_scaled.png) | 551                        | 327.542128                          | 327.293109                | This test plan has one of the fastest report times alongside the single instance version using one thread. Since it only uses one thread, it can handle requests and queries much faster compared to using multiple threads as seen in case 2 of the scaled version.                                                                                                                                      |
| Case 2: HTTP/10 threads                       | ![](jmeter_reports/case2_scaled.png) | 2066                       | 285.730807                          | 285.60584                 | Compared to the single thread scaled test plan, this is much slower since it uses multiple threads which demands more from the server to execute the queries. This test plan however is faster than the single instance test that uses multiple threads (case 2/3) because the scaled version can manage a heavier load; thus, working faster with 10 threads compared to the single instance test plans. |
| Case 3: HTTP/10 threads/No connection pooling | ![](jmeter_reports/case3_scaled.png) | 2284                       | 317.665049                          | 301.53230                 | No connection pooling slows down the average query times because every request needs to create a new connection and process a query rather than grabbing a preallocated connection from the pool. This test plan is a little faster than the single instance test plan with no pooling since the scaled version can manage a heavier load, thus executing queries a little faster.                        |
