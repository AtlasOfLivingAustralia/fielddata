package au.org.ala.fielddata

import org.apache.commons.io.FilenameUtils
import org.springframework.cache.annotation.Cacheable
import org.apache.commons.lang.time.DateUtils
import org.apache.commons.codec.binary.Base64

class RecordService {

    def grailsApplication

    def serviceMethod() {}

    def webService

    def ignores = ["action","controller","associatedMedia"]

    def mediaService

    def broadcastService

    def userService

    def createRecord(json){
        Record r = new Record()
        r = r.save(true)
        updateRecord(r,json)
        //download the supplied images......
        broadcastService.sendCreate(r)
        r
    }

    def addImageToRecord(Record record, String filename, byte[] imageAsByteArray){
        File createdFile = mediaService.copyBytesToImageDir(record.id.toString(), filename, imageAsByteArray)
        if(record['associatedMedia']){
            record['associatedMedia'] << createdFile.getPath()
        } else {
            record['associatedMedia'] = createdFile.getPath()
        }
        record.save(true)
    }

    def updateRecord(r, json){

        json.each {
            if(!ignores.contains(it.key) && it.value){
                if (it.value && it.value instanceof BigDecimal ){
                    //println "Before: " + it.value
                    r[it.key] = it.value.toString()
                    //println "After: " + r[it.key]
                } else {
                    r[it.key] = it.value
                }
            }
        }

        //look for associated media.....
        if (List.isCase(json.associatedMedia)){

            def mediaFiles = []
            def originalFiles = []
            if (r['associatedMedia']) {
                r['associatedMedia'].each {
                    mediaFiles << it
                    originalFiles << it
                }
            }

            if(!originalFiles) originalFiles = []

            def originalFilesSuppliedAgain = []

            json.associatedMedia.eachWithIndex() { obj, i ->
                //are any of these images existing images ?
                //println "Processing associated media URL : " + obj
                if (obj.startsWith(grailsApplication.config.fielddata.mediaUrl)){
                    //URL already loaded - do nothing
                  //  println("URL already loaded: " + obj)
                    def imagePath = grailsApplication.config.fielddata.mediaDir + (obj - grailsApplication.config.fielddata.mediaUrl)
                   // println("URL already loaded - transformed image path: " + imagePath)
                    originalFilesSuppliedAgain <<  imagePath
                } else {
                   // println("URL NOT loaded. Downloading file: " + obj)
                    def createdFile = mediaService.download(r.id.toString(), i, obj)
                    mediaFiles << createdFile.getPath()
                }
            }

            //do we need to delete any files ?
            def filesToBeDeleted = originalFiles.findAll { !originalFilesSuppliedAgain.contains(it) }
           // println("Number to be deleted: " + filesToBeDeleted.size())
            filesToBeDeleted.each {
              //  mediaService.removeImage(it) //delete original & the derivatives
                log.info("Removing :" + it)
                mediaFiles.remove(it)
            }

            r['associatedMedia'] = mediaFiles
        } else if(json.associatedMedia) {
            def createdFile = mediaService.download(r.id.toString(), 0, json.associatedMedia)
            r['associatedMedia'] = createdFile.getPath()
        }

        if(!r['occurrenceID']){
            r['occurrenceID'] = r.id.toString()
        }

        r.save(flush: true)
    }

    def toMap(record){
        def dbo = record.getProperty("dbo")
        def mapOfProperties = dbo.toMap()
        def id = mapOfProperties["_id"].toString()
        mapOfProperties["id"] = id
        mapOfProperties.remove("_id")
        //add userDisplayName - Cacheable not working....
        if(mapOfProperties["userId"]){
            def userMap = userService.getUserNamesForIdsMap()
            def userDisplayName = userMap.get(mapOfProperties["userId"])
            if(userDisplayName){
                 mapOfProperties["userDisplayName"] = userDisplayName
            }
        }
        mediaService.setupMediaUrls(mapOfProperties)
        mapOfProperties
    }
}
