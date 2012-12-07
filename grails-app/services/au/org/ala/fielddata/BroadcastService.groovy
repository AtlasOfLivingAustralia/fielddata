package au.org.ala.fielddata

import grails.converters.JSON
import javax.jms.TextMessage
import javax.jms.JMSException
import javax.jms.Session
import javax.jms.Message
import org.springframework.jms.core.MessageCreator

class BroadcastService {

    def serviceMethod() {}

    def grailsApplication

    def mediaService

    def jmsTemplate

    def userService

    def destination

    def sendMessage(method, json){
        jmsTemplate.send(destination, new MessageCreator() {
            Message createMessage(Session session) throws JMSException {
                TextMessage message = session.createTextMessage(json)
                message.setStringProperty("messageMethod", method)
                return message;
            }
        })
    }

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
        if(grailsApplication.config.enableJMS){
            def mapOfProperties = toMap(record)
            def json = mapOfProperties as JSON
            log.debug("sending create: " + json.toString(true))
            sendMessage("CREATE", json.toString(true))
        } else {
             log.debug "JMS currently disabled....not sending CREATE"
        }
    }

    def sendUpdate(record){
        if(grailsApplication.config.enableJMS){
            def mapOfProperties = toMap(record)

            def json = mapOfProperties as JSON
            println("sending update: " + json.toString(true))
            sendMessage("UPDATE", json.toString(true))
        } else {
            log.debug "JMS currently disabled....not sending UPDATE"
        }
    }

    def sendDelete(occurrenceID){
        if(grailsApplication.config.enableJMS){
            def map = [occurrenceID:occurrenceID]
            sendMessage("DELETE", (map as JSON).toString(true))
        } else {
            log.debug "JMS currently disabled....not sending DELETE"
        }
    }

    def toMap(record){
        def dbo = record.getProperty("dbo")
        def mapOfProperties = dbo.toMap()
        def id = mapOfProperties["_id"].toString()
        mapOfProperties["id"] = id
        mapOfProperties.remove("_id")
        mapOfProperties.remove("images")
        if(mapOfProperties["userId"]){
            def userMap = userService.getUserNamesForIdsMap()
            def userDisplayName = userMap.get(mapOfProperties["userId"])
            if(userDisplayName){
                 mapOfProperties["recordedBy"] = userDisplayName
            }
        }
        mediaService.setupMediaUrls(mapOfProperties)
        mapOfProperties
    }
}