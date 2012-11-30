package au.org.ala.fielddata

import groovy.json.JsonSlurper

class LocationController {

    static allowedMethods = [getById:'GET', create:'POST', delete:'DELETE', deleteAllForUser:'DELETE',
            retrieveAllForUser:'GET', retrieveAll:'GET']

    def ignores = ["action","controller"]

    def getById(){

        println("Get location by ID: " + params.id)
        Location r = Location.get(params.id)
        if(r){
            r.metaPropertyValues.each { println "meta: "  + it.name }
            def dbo = r.getProperty("dbo")

            def mapOfProperties = dbo.toMap()
            def id = mapOfProperties["_id"].toString()
            mapOfProperties["id"] = id
            mapOfProperties.remove("_id")

            response.setContentType("application/json")
            [record:mapOfProperties]
        } else {
            response.sendError(404, 'Unrecognised Location ID. This location may have been removed.')
        }
    }

    def create(){
        def jsonSlurper = new JsonSlurper()
        def json = jsonSlurper.parse(request.getReader())
        if (!json.userId){
            response.sendError(400, 'Missing userId')
        } else if (!json.decimalLatitude){
            response.sendError(400, 'Missing decimalLatitude')
        } else if (!json.decimalLongitude){
            response.sendError(400, 'Missing decimalLongitude')
        } else if (!json.locality){
            response.sendError(400, 'Missing locality')
        } else {
            Location l = new Location(json)
            json.each {
                if(!ignores.contains(it.key)){
                    l[it.key] = it.value
                }
            }
            Location createdLocation = l.save(true)
            // r.errors.each { println it}
            response.addHeader("content-location", grailsApplication.config.grails.serverURL + "/fielddata/location/" + createdLocation.getId().toString())
            response.addHeader("location", grailsApplication.config.grails.serverURL + "/fielddata/location/" + createdLocation.getId().toString())
            response.addHeader("entityId", createdLocation.getId().toString())
            //download the supplied images......
            response.setContentType("application/json")
            [id:createdLocation.getId().toString()]
        }
    }

    def deleteById(){
        Location l = Location.get(params.id)
        if (l){
            l.delete(flush: true)
            response.setStatus(200)
            response.setContentType("application/json")
            [success:"OK"]
        } else {
            response.setStatus(400)
        }
    }

    def deleteAllForUser(){
        log.debug("Delete all for user...")
        Location.findAllWhere([userId:params.userId], [:]).each {
            it.delete(flush: true)
        }
        response.setStatus(200)
        response.setContentType("application/json")
        [success:"OK"]
    }

    def listForUser(){
        def locations = []
        def sort = params.sort ?: "dateCreated"
        def order = params.order ?:  "desc"
        def offset = params.start ?: 0
        def max = params.pageSize ?: 10

        log.debug("Retrieving a list for user:"  + params.userId)
        Location.findAllWhere([userId:params.userId], [sort:sort,order:order,offset:offset,max:max]).each {
            def dbo = it.getProperty("dbo")
            def mapOfProperties = dbo.toMap()
            def id = mapOfProperties["_id"].toString()
            mapOfProperties["id"] = id
            mapOfProperties.remove("_id")
            locations.add(mapOfProperties)
        }
        response.setContentType("application/json")
        [locations: locations]
    }

    def list(){
        def locations = []
        def sort = params.sort ? params.sort : "dateCreated"
        def order = params.order ? params.order :  "desc"
        def offset = params.start ? params.start : 0
        def max = params.pageSize ? params.pageSize : 10

        log.debug("Retrieving a list for all users")
        Location.findAllWhere([:], [sort:sort,order:order,offset:offset,max:max]).each {
            def dbo = it.getProperty("dbo")
            def mapOfProperties = dbo.toMap()
            def id = mapOfProperties["_id"].toString()
            mapOfProperties["id"] = id
            mapOfProperties.remove("_id")
            locations.add(mapOfProperties)
        }
        response.setContentType("application/json")
        [locations: locations]
    }
}
