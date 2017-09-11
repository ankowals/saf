# saf
simple automation framework for learning purposes

cd install_dir
mvn clean test -Dcucumber.options="--tags @bookByIsbn"
mvn site
mvn jetty:run
http://localhost:8080
