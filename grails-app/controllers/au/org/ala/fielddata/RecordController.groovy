package au.org.ala.fielddata

import groovy.json.JsonSlurper
import org.apache.commons.io.FilenameUtils

class RecordController {

    def grailsApplication

    def mediaService

    def broadcastService

    def recordService

    def ignores = ["action","controller","associatedMedia"]

//    def testJMS() {
//		def message = "Hi, this is a Hello World with JMS & ActiveMQ, " + new Date()
//		sendJMSMessage("queue.notification", message)
//		render message
//    }

    /*

    addImages:[]
    removeImages:[]

     */

    def updateImages(){



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

            if(grailsApplication.config.enableJMS) broadcastService.sendCreate(r)

            [id:r.id.toString()]
        } else {
            response.sendError(400, 'Missing userId')
        }
    }

    private def updateRecord(r, json){

        json.each {
            if(!ignores.contains(it.key) && it.value){
                if (it.value && it.value instanceof BigDecimal ){
                    println "Before: " + it.value
                    r[it.key] = it.value.toString()
                    println "After: " + r[it.key]
                } else {
                    r[it.key] = it.value
                }
            }
        }

        //look for associated media.....
        if (List.isCase(json.associatedMedia)){

            def mediaFiles = []

            json.associatedMedia.eachWithIndex() { obj, i ->
                def createdFile = mediaService.download(r.id.toString(), i, obj)
                mediaFiles.add createdFile.getAbsolutePath()
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
        //json.eventDate = new Date().parse("yyyy-MM-dd", json.eventDate)
        //TODO add some data validation....

        Record r = Record.get(params.id)
        updateRecord(r,json)
        response.addHeader("content-location", grailsApplication.config.grails.serverURL + "/fielddata/record/" + r.id.toString())
        response.addHeader("location", grailsApplication.config.grails.serverURL + "/fielddata/record/" + r.id.toString())
        response.addHeader("entityId", r.id.toString())
        response.setContentType("application/json")

        if(grailsApplication.config.enableJMS) broadcastService.sendUpdate(r)

        [id:r.id.toString()]
    }

//    def typeMapping = [
//            "decimalLatitude" : "Float",
//            "decimalLongitude" : "Float",
//            "eventDate" : "Date",
//    ]
}
