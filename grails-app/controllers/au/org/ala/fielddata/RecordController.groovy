package au.org.ala.fielddata

import groovy.json.JsonSlurper
import org.apache.commons.io.FilenameUtils
import org.bson.types.ObjectId
import org.apache.commons.io.FileUtils

class RecordController {

    def grailsApplication

    def mediaService

    def broadcastService

    def recordService

    def ignores = ["action","controller","associatedMedia"]

    /**
     * JSON body looks like:
     *        {
     *          "id":"34234324324"
     *          "addImages":[....]   //array of urls to new images
     *          "removeImages":[...]  //array of urls to existing images
     *        }
     */
    def updateImages(){
        def jsonSlurper = new JsonSlurper()
        def json = jsonSlurper.parse(request.getReader())
        if (json.id){
            def record = Record.findById(new ObjectId(json.id))
            if (record){
                if(json.addImages){
                   json.addImages.each {
                    def mediaFiles = record['associatedMedia']
                    def createdFile = mediaService.download(record.id.toString(), mediaFiles.length-1, obj)
                    mediaFiles.add createdFile.getAbsolutePath()
                    record['associatedMedia'] = mediaFiles
                   }
                }
                if (json.removeImages){
                   json.removeImages.each {
                    def mediaFiles = record['associatedMedia']
                    //translate the full URL to actual path
                    def imagePath = it.replaceAll(
                            grailsApplication.config.fielddata.mediaUrl,
                            grailsApplication.config.fielddata.mediaDir
                    )
                    mediaFiles.remove(createdFile.getAbsolutePath())
                    record['associatedMedia'] = mediaFiles
                    def file = new File(imagePath)
                    if(file.exists()){
                        FileUtils.forceDelete(file) //delete the derivatives
                    }
                   }
                }
                record.save(true)
                response.setContentType("application/json")
                [id: record.id.toString(), images:record['associatedMedia']]
            } else {
                response.sendError(404, 'Record ID not recognised. JSON payload must contain "id" element for existing record.')
            }
        } else {
            response.sendError(400, 'No record ID was supplied. JSON payload must contain "id" element.')
        }
    }

    def getById(){
        Record r = Record.get(params.id)
        if(r){
            response.setContentType("application/json")
            [record:recordService.toMap(r)]
        } else {
            response.sendError(404, 'Unrecognised Record ID. This record may have been removed.')
        }
    }

    def listRecordWithImages(){
        def records = []
        def sort = params.sort ?: "dateCreated"
        def orderBy = params.order ?:  "desc"
        def offsetBy = params.start ?: 0
        def max = params.pageSize ?: 10

        def c = Record.createCriteria()
        def results = c.list {
            isNotNull("associatedMedia")
            maxResults(max)
            //order(sort,orderBy)
            offset(offsetBy)
        }
        results.each {
            records.add(recordService.toMap(it))
        }
        response.setContentType("application/json")
        [records:records]
    }



    def count(){
        response.setContentType("application/json")
        [count:Record.count()]
    }

    def list(){
        def records = []
        def sort = params.sort ?: "dateCreated"
        def order = params.order ?:  "desc"
        def offset = params.start ?: 0
        def max = params.pageSize ?: 10

        Record.listOrderByDateCreated([sort:sort,order:order,offset:offset,max:max]).each {
            records.add(recordService.toMap(it))
        }
        response.setContentType("application/json")
        [records:records]
    }

    def listForUser(){
        def records = []
        def sort = params.sort ?: "dateCreated"
        def order = params.order ?:  "desc"
        def offset = params.start ?: 0
        def max = params.pageSize ?: 10

        log.debug("Retrieving a list for user:"  + params.userId)
        Record.findAllWhere([userId:params.userId], [sort:sort,order:order,offset:offset,max:max]).each {
            records.add(recordService.toMap(it))
        }
        response.setContentType("application/json")
        [records:records]
    }

    def deleteById(){
        Record r = Record.get(params.id)
        if (r){
            r.delete(flush: true)
            response.setStatus(200)
        } else {
            response.sendError(400)
        }
    }

    /**
     * Create method with JSON body...
     */
    def create(){
        def jsonSlurper = new JsonSlurper()
        def json = jsonSlurper.parse(request.getReader())
        if (json.userId){
//            if (json.eventDate){
//                json.eventDate = new Date().parse("yyyy-MM-dd", json.eventDate)
//            }
            Record r = new Record()
            r = r.save(true)
            updateRecord(r,json)
            //download the supplied images......
            response.addHeader("content-location", grailsApplication.config.grails.serverURL + "/fielddata/record/" + r.id.toString())
            response.addHeader("location", grailsApplication.config.grails.serverURL + "/fielddata/record/" + r.id.toString())
            response.addHeader("entityId", r.id.toString())
            response.setContentType("application/json")

            broadcastService.sendCreate(r)

            [id:r.id.toString()]
        } else {
            response.sendError(400, 'Missing userId')
        }
    }

    private def updateRecord(r, json){

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
                    def imagePath = obj.replaceAll(
                            grailsApplication.config.fielddata.mediaUrl,
                            grailsApplication.config.fielddata.mediaDir
                    )
                   // println("URL already loaded - transformed image path: " + imagePath)
                    originalFilesSuppliedAgain <<  imagePath
                } else {
                   // println("URL NOT loaded. Downloading file: " + obj)
                    def createdFile = mediaService.download(r.id.toString(), i, obj)
                    mediaFiles << createdFile.getAbsolutePath()
                }
            }

            //do we need to delete any files ?
            def filesToBeDeleted = originalFiles.findAll { !originalFilesSuppliedAgain.contains(it) }
           // println("Number to be deleted: " + filesToBeDeleted.size())
            filesToBeDeleted.each {
              //  println("Deleting file: " + it)
                File fileToBeDeleted = new File(it)
                if(fileToBeDeleted.exists()){
                    FileUtils.forceDelete(fileToBeDeleted) //TODO delete the derivatives
                }
                mediaFiles.remove(it)
            }

            r['associatedMedia'] = mediaFiles
        } else if(json.associatedMedia) {
            def createdFile = mediaService.download(r.id.toString(), 0, json.associatedMedia)
            r['associatedMedia'] = createdFile.getAbsolutePath()
        }

        if(!r['occurrenceID']){
            r['occurrenceID'] = r.id.toString()
        }

        r.save(flush: true)
    }

    def resyncRecord(){
        def r = Record.get(params.id)
        if (r) {
            broadcastService.sendUpdate(r)
            response.setStatus(200)
            response.setContentType("application/json")
            [recordSynced:true]
        } else {
            response.sendError(404)
        }
    }

    def resyncAll(){
        def count = broadcastService.resyncAll()
        response.setContentType("application/json")
        [recordsSynced:count]
    }

    def updateById(){
        def jsonSlurper = new JsonSlurper()
        def json = jsonSlurper.parse(request.getReader())

        //println("JSON: " + json.toString())

        //json.eventDate = new Date().parse("yyyy-MM-dd", json.eventDate)
        //TODO add some data validation....

        Record r = Record.get(params.id)
        updateRecord(r,json)
        response.addHeader("content-location", grailsApplication.config.grails.serverURL + "/fielddata/record/" + r.id.toString())
        response.addHeader("location", grailsApplication.config.grails.serverURL + "/fielddata/record/" + r.id.toString())
        response.addHeader("entityId", r.id.toString())
        response.setContentType("application/json")

        broadcastService.sendUpdate(r)
        [id:r.id.toString()]
    }
}
