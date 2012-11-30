package au.org.ala.fielddata

import grails.converters.JSON
import grails.plugin.jms.*

class BroadcastService {

    def serviceMethod() {}

    def jmsService

    def sendCreate(record){
        def dbo = record.getProperty("dbo")
        def mapOfProperties = dbo.toMap()
        def id = mapOfProperties["_id"].toString()
        mapOfProperties["id"] = id
        mapOfProperties.remove("_id")
        mapOfProperties["messageMethod"] = "CREATE"
        def json = mapOfProperties as JSON

        println "sending create: " + json.toString(true)
        sendJMSMessage(queue:'org.ala.jms.cs', json.toString(true))
    }

    def sendUpdate(record){
        def dbo = record.getProperty("dbo")
        def mapOfProperties = dbo.toMap()
        def id = mapOfProperties["_id"].toString()
        mapOfProperties["id"] = id
        mapOfProperties.remove("_id")
        mapOfProperties["messageMethod"] = "UPDATE"
        def json = mapOfProperties as JSON
        println "sending create: " + json.toString(true)

		sendJMSMessage("org.ala.jms.cs", json.toString(true))
    }

    def sendDelete(recordID){
        def map = [guid:recordID,messageMethod:"DELETE"]
		sendJMSMessage("org.ala.jms.cs", (map as JSON).toString(true))
    }
}