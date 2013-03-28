package au.org.ala.fielddata

import groovy.json.JsonSlurper
import org.bson.types.ObjectId

class RecordController {

    def grailsApplication

    def mediaService

    def broadcastService

    def recordService

    def ignores = ["action","controller","associatedMedia"]
    /**
     * JSON body looks like:
     * {
     *  "id":"34234324324"
     *  "addImages":[....]   //array of urls to new images
     *  "removeImages":[...]  //array of urls to existing images
     * }
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
                    mediaFiles.remove(createdFile.getPath())
                    record['associatedMedia'] = mediaFiles
                    mediaService.removeImage(imagePath) //delete original & the derivatives
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
            order(sort,orderBy)
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
        log.debug("list request....")
        def records = []
        def sort = params.sort ?: "dateCreated"
        def order = params.order ?:  "desc"
        def offset = params.start ?: 0
        def max = params.pageSize ?: 10
        Record.list([sort:sort,order:order,offset:offset,max:max]).each {
            recordService.toMap(it)
            records.add(recordService.toMap(it))
        }
        response.setContentType("application/json")
        [records:records]
    }

    def listForUser(){
        log.debug("list request for user...." + params.userId)
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
            if(r['associatedMedia'] && r['associatedMedia']){
//               r['associatedMedia'].each {
//                   if(it && it.startsWith(grailsApplication.config.fielddata.mediaDir)){
//                    mediaService.removeImage(it)
//                   }
//               }
            }
            broadcastService.sendDelete(r["occurrenceID"])
            response.setStatus(200)
            response.setContentType("application/json")
            [success:true]
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
            def (record,errors) = recordService.createRecord(json)
            if(errors.size() == 0){
                setResponseHeadersForRecord(response, record)
                response.setContentType("application/json")
                [id:record.id.toString()]
            }
            else{
                record.delete(flush: true)
                response.addHeader 'errors', (errors as grails.converters.JSON).toString()
                response.sendError(400,"Unable to create a new record. See errors for more details." )
            }
        } else {
            response.sendError(400, 'Missing userId')
        }
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
        //json.eventDate = new Date().parse("yyyy-MM-dd", json.eventDate)
        //TODO add some data validation....
        Record r = Record.get(params.id)
        def errors=recordService.updateRecord(r,json)
        setResponseHeadersForRecord(response, r)
        //add the errors to the header too
        response.addHeader('errors', (errors as grails.converters.JSON).toString())
        response.setContentType("application/json")
        try {
            broadcastService.sendUpdate(r)
        } catch (Exception e){
            log.error(e.getMessage(), e)
        }
        [id:r.id.toString()]
    }

    def setResponseHeadersForRecord(response, record){
        response.addHeader("content-location", grailsApplication.config.grails.serverURL + "/fielddata/record/" + record.id.toString())
        response.addHeader("location", grailsApplication.config.grails.serverURL + "/fielddata/record/" + record.id.toString())
        response.addHeader("entityId", record.id.toString())
    }
}