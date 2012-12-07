package au.org.ala.fielddata

import org.apache.commons.lang.time.DateUtils

import static groovyx.gpars.actor.Actors.actor

class ImportController {

    def importService

    def index() { }

    def importFile(){

       def filePath = params.filePath

       def theActor = actor {
            println "Starting a thread.....reload images: " + params.reloadImages

            importService.loadFile(filePath, params.reloadImages)
            println "Finishing thread."
       }

       response.setContentType("application/json")
       [started:true]
    }

}
