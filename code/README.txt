Todo:
 - need to provide a batch oriented solution to reading in the replication updates from the musicbrains ftp server (code's already written)
 - need to build a bus module to handle things like the batch replication update (use Spring Integration FTP adapter!)
 - need to build a bots module that will use gridgain as the messaging topology fabric
 - need to upgrade jbpm3 which means i need to investigate building my own SpringModules for jbpm, since the newer versions of jBPM are fantastic and the old SpringModules jbpmTemplate's broken. (use Activiti!)
 - need to investigate putting the musicbrainz database in a different schema (but same db) as the jukebox data.
  
