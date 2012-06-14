package au.org.ala.fielddata

import grails.converters.JSON

class JSONPFilters {

    def filters = {
        all(controller:'*', action:'*') {
            before = {
                println("Before.....")


            }
            after = { Map model ->
                println("After.....")


            }
            afterView = { Exception e ->

            }
        }
    }
}
