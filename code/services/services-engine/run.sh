mvn clean install  ; 
rm -rf rm ../services-endpoint/work/;
cd ../services-endpoint; 
mvn clean install jetty:run ;
