guaranteeDirectory(){ mkdir -p $1; }
mvn clean install  ;
python update_model_classes.py ;
export d="../services/services-engine/src/main/java/com/joshlong/jukebox2/model/"
guaranteeDirectory "$d"
cp target/hibernate3/generated-sources/com/joshlong/jukebox2/model/*  $d;
