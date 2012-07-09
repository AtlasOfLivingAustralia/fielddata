package au.org.ala.fielddata

import groovy.json.JsonSlurper
import org.apache.commons.io.FilenameUtils

class RecordController {

    def grailsApplication

    def mediaService

    def ignores = ["action","controller","associatedMedia"]

    def getById(){
        Record r = Record.get(params.id)
       // r.metaPropertyValues.each { println "meta: "  + it.name }
        def dbo = r.getProperty("dbo")

        def mapOfProperties = dbo.toMap()
        def id = mapOfProperties["_id"].toString()
        mapOfProperties["id"] = id
        mapOfProperties.remove("_id")
        setupMediaUrls(mapOfProperties)
        response.setContentType("application/json")
        [record:mapOfProperties]
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
            def dbo = it.getProperty("dbo")
            def mapOfProperties = dbo.toMap()
            def id = mapOfProperties["_id"].toString()
            mapOfProperties["id"] = id
            mapOfProperties.remove("_id")
            setupMediaUrls(mapOfProperties)
            records.add(mapOfProperties)
        }
        response.setContentType("application/json")
        [records:records]
    }

    boolean isCollectionOrArray(object) {
        [Collection, Object[]].any { it.isAssignableFrom(object.getClass()) }
    }

    def setupMediaUrls(mapOfProperties){
        if (mapOfProperties["associatedMedia"] != null){

            if (isCollectionOrArray(mapOfProperties["associatedMedia"])){

                def imagesArray = []

                mapOfProperties["associatedMedia"].each {

                    def imagePath = it.replaceAll(grailsApplication.config.fielddata.mediaDir,
                            grailsApplication.config.fielddata.mediaUrl)
                    def extension = FilenameUtils.getExtension(imagePath)
                    def pathWithoutExt = imagePath.substring(0, imagePath.length() - extension.length() - 1 )
                    def image = [
                            thumb : pathWithoutExt + "__thumb."+extension,
                            small : pathWithoutExt + "__small."+extension,
                            large : pathWithoutExt + "__large."+extension,
                            raw : imagePath,
                    ]

                    imagesArray.add(image)
                }
                mapOfProperties['images'] = imagesArray
            } else {
                def imagePath = mapOfProperties["associatedMedia"].replaceAll(grailsApplication.config.fielddata.mediaDir,
                        grailsApplication.config.fielddata.mediaUrl)
                def extension = FilenameUtils.getExtension(imagePath)
                def pathWithoutExt = imagePath.substring(0, imagePath.length() - extension.length() - 1 )
                def image = [
                        thumb : pathWithoutExt + "__thumb."+extension,
                        small : pathWithoutExt + "__small."+extension,
                        large : pathWithoutExt + "__large."+extension,
                        raw : imagePath,
                ]
                mapOfProperties['images'] = [image]
            }
        }
    }

    def list(){
        def records = []
        def sort = params.sort ?: "dateCreated"
        def order = params.order ?:  "desc"
        def offset = params.start ?: 0
        def max = params.pageSize ?: 10

        Record.listOrderByDateCreated([sort:sort,order:order,offset:offset,max:max]).each {
            def dbo = it.getProperty("dbo")
            def mapOfProperties = dbo.toMap()
            def id = mapOfProperties["_id"].toString()
            mapOfProperties["id"] = id
            mapOfProperties.remove("_id")
            setupMediaUrls(mapOfProperties)
            records.add(mapOfProperties)
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
        Record.findAllWhere([userID:params.userID], [sort:sort,order:order,offset:offset,max:max]).each {
            def dbo = it.getProperty("dbo")
            def mapOfProperties = dbo.toMap()
            def id = mapOfProperties["_id"].toString()
            mapOfProperties["id"] = id
            mapOfProperties.remove("_id")
            setupMediaUrls(mapOfProperties)
            records.add(mapOfProperties)
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
            json.eventDate = new Date().parse("yyyy-MM-dd", json.eventDate)
            Record r = new Record()
            r = r.save(true)
            updateRecord(r,json)
            //download the supplied images......
            response.addHeader("content-location", grailsApplication.config.grails.serverURL + "/fielddata/record/" + r.id.toString())
            response.addHeader("location", grailsApplication.config.grails.serverURL + "/fielddata/record/" + r.id.toString())
            response.addHeader("entityId", r.id.toString())

            response.setContentType("application/json")
            [id:r.id.toString()]
        } else {
            response.sendError(400, 'Missing userId')
        }
    }

    private def updateRecord(r, json){

        json.each {
            if(!ignores.contains(it.key)){
                r[it.key] = it.value
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
        r.save(flush: true)
    }

    def updateById(){
        def jsonSlurper = new JsonSlurper()
        def json = jsonSlurper.parse(request.getReader())
        json.eventDate = new Date().parse("yyyy-MM-dd", json.eventDate)
        Record r = Record.get(params.id)
        updateRecord(r,json)
        response.addHeader("content-location", grailsApplication.config.grails.serverURL + "/fielddata/record/" + r.id.toString())
        response.addHeader("location", grailsApplication.config.grails.serverURL + "/fielddata/record/" + r.id.toString())
        response.addHeader("entityId", r.id.toString())
        [id:r.id.toString()]
    }


    def typeMapping = [
            "decimalLatitude" : "Float",
            "decimalLongitude" : "Float",
            "eventDate" : "Date",
    ]
}
