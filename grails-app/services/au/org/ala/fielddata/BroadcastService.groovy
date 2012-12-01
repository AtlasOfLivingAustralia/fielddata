package au.org.ala.fielddata

import grails.converters.JSON
//import grails.plugin.jms.*

class BroadcastService {

    def grailsApplication

    def serviceMethod() {}

    def jmsService

    def recordService

    def resyncAll(){
        if(!grailsApplication.config.enableJMS) return 0

        def max = 100
        def offset = 0
        def synced = 0
        def finished = false
        while(!finished){
            def results = Record.list([offset:offset,max:max])
            finished = results.isEmpty()
            results.each { sendCreate(it); synced++ }
            offset += max
        }
        synced
    }

    def sendCreate(record){
        if(!grailsApplication.config.enableJMS) return;
        def mapOfProperties = recordService.toMap(record)
        mapOfProperties["messageMethod"] = "CREATE"
        def json = mapOfProperties as JSON
        log.debug("sending create: " + json.toString(true))
        sendJMSMessage(queue:'org.ala.jms.cs', json.toString(true))
    }

    def sendUpdate(record){
        if(!grailsApplication.config.enableJMS) return;
        def mapOfProperties = recordService.toMap(record)
        mapOfProperties["messageMethod"] = "UPDATE"
        def json = mapOfProperties as JSON
        log.debug("sending update: " + json.toString(true))
		sendJMSMessage("org.ala.jms.cs", json.toString(true))
    }

    def sendDelete(recordID){
        if(!grailsApplication.config.enableJMS) return;
        def map = [guid:recordID,messageMethod:"DELETE"]
		sendJMSMessage("org.ala.jms.cs", (map as JSON).toString(true))
    }

    def sendJMSMessage(queueName, message){
        println "Doing nothing for now...."
    }

}