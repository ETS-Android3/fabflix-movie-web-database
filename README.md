1. https://youtu.be/HjOSzRGo0sE

3. Deploying our application with Tomcat involved packaging our maven project
which resulted in a war file being created that can moved into the tomcat
webapp directory which can post the live web application on the tomcat
server.

3. Queries statements in MoviesServlet.java, SingleMovieServlet.java, SingleStarServlet.java. 
    https://github.com/UCI-Chenli-teaching/cs122b-fall21-team-5/blob/main/src/MoviesServlet.java
    https://github.com/UCI-Chenli-teaching/cs122b-fall21-team-5/blob/main/src/SingleStarServlet.java
    https://github.com/UCI-Chenli-teaching/cs122b-fall21-team-5/blob/main/src/SingleMovieServlet.java

4. For optimzation design we chose to use a load into script which allowed our tables to be filled within a minute for all 3 loads. We also implemented a Hash Table for checking duplicate data
    within the database. This immensely sped up the processing time.

4. SUBSTRING MATCH DESIGN: 'LIKE' predicate used in MoviesServlet for search queries to filter the movie list and browse by index to filter movies that start with selected index