package au.org.ala.fielddata

import grails.converters.JSON
import javax.jms.TextMessage
import javax.jms.JMSException
import javax.jms.Session
import javax.jms.Message
import org.springframework.jms.core.MessageCreator
//import grails.plugin.jms.*

class BroadcastService {

    def serviceMethod() {}

    def grailsApplication

    def recordService

    def jmsTemplate

    def destination

    def sendMessage(method, json){
        jmsTemplate.send(destination, new MessageCreator() {
            Message createMessage(Session session) throws JMSException {
                TextMessage message = session.createTextMessage(json)
                message.setStringProperty("messageMethod", method)
                println("Creating message.....")
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
        if(!grailsApplication.config.enableJMS) return;
        def mapOfProperties = recordService.toMap(record)
        mapOfProperties["messageMethod"] = "CREATE"
        def json = mapOfProperties as JSON
        log.debug("sending create: " + json.toString(true))
        sendMessage("CREATE", json.toString(true))
    }

    def sendUpdate(record){
        if(!grailsApplication.config.enableJMS) return;
        def mapOfProperties = recordService.toMap(record)
        mapOfProperties["messageMethod"] = "UPDATE"
        def json = mapOfProperties as JSON
        log.debug("sending update: " + json.toString(true))
        sendMessage("UPDATE", json.toString(true))
    }

    def sendDelete(recordID){
        if(!grailsApplication.config.enableJMS) return;
        def map = [guid:recordID,messageMethod:"DELETE"]
        sendMessage("DELETE", (map as JSON).toString(true))
    }
}