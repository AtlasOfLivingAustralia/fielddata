package au.org.ala.fielddata

import groovy.json.JsonSlurper
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils


class RecordController {

    def grailsApplication

    def ignores = ["decimalLatitude","decimalLongitude","eventDate","action","controller"]

    def getById(){
        Record r = Record.get(params.id)
       // r.metaPropertyValues.each { println "meta: "  + it.name }
        def dbo = r.getProperty("dbo")

        def mapOfProperties = dbo.toMap()
        def id = mapOfProperties["_id"].toString()
        mapOfProperties["id"] = id
        mapOfProperties.remove("_id")
        setupMediaUrls(mapOfProperties)
        mapOfProperties
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
        render(contentType: "text/json") { records }
    }

    def setupMediaUrls(mapOfProperties){
        if (mapOfProperties["associatedMedia"] != null){
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
        render(contentType: "text/json") { records }
    }

    def listForUser(){
        def records = []
        def sort = params.sort ?: "dateCreated"
        def order = params.order ?:  "desc"
        def offset = params.start ?: 0
        def max = params.pageSize ?: 10

        println("Retrieving a list for user:"  + params.userId)
        Record.findAllWhere([userId:params.userId], [sort:sort,order:order,offset:offset,max:max]).each {
            def dbo = it.getProperty("dbo")
            def mapOfProperties = dbo.toMap()
            def id = mapOfProperties["_id"].toString()
            mapOfProperties["id"] = id
            mapOfProperties.remove("_id")
            setupMediaUrls(mapOfProperties)
            records.add(mapOfProperties)
        }
        render(contentType: "text/json") { records }
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
            Record r = new Record(json)
            json.each {
                if(!ignores.contains(it.key)){
                    r[it.key] = it.value
                }
            }

            //look for associated media.....
            if (List.isCase(json.associatedMedia)){
                json.associatedMedia.eachWithIndex() { obj, i ->
                    //download to file system....
                   // println("Media to download: " + obj)
                    //download to file
                    download(i, obj)
                }
            } else {
                download(json.associatedMedia)
            }

            Record createdRecord = r.save(true)
           // r.errors.each { println it}
            response.addHeader("content-location", grailsApplication.config.grails.serverURL + "/fielddata/record/" + createdRecord.getId())
            response.addHeader("location", grailsApplication.config.grails.serverURL + "/fielddata/record/" + createdRecord.getId())
            response.addHeader("entityId", createdRecord.getId())
            //download the supplied images......
            render(contentType: "text/json") { [id:createdRecord.getId().toString()] }
        } else {
            response.sendError(400, 'Missing userId')
        }
    }

    private def download(idx, address){
        File mediaDir = new File(grailsApplication.config.fielddata.mediaDir)
        if (!mediaDir.exists()){
            FileUtils.forceMkdir(mediaDir)
        }
        def file = new FileOutputStream(grailsApplication.config.fielddata.mediaDir + idx + "_" +address.tokenize("/")[-1])
        def out = new BufferedOutputStream(file)
        log.debug("Trying to download..." + address)
        out << new URL(address).openStream()
        out.close()
    }

    def updateById(){
        def jsonSlurper = new JsonSlurper()
        def json = jsonSlurper.parse(request.getReader())
        json.eventDate = new Date().parse("yyyy-MM-dd", json.eventDate)
        Record r = Record.get(params.id)
        json.each {
            if(!ignores.contains(it.key)){
                r[it.key] = it.value
            }
        }
        r.save(flush: true)
    }
}
