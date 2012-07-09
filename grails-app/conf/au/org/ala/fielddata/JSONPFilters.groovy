package au.org.ala.fielddata

import grails.converters.JSON

class JSONPFilters {

    def filters = {
        all(controller:'*', action:'*') {
            before = {
               // println("Before.....")
            }
            after = { Map model ->

                def ct = response.getContentType()
                //println("content type: "  + ct)
                if(ct?.contains("application/json")){
                    String resp = model as JSON
                    if(params.callback) {
                        resp = params.callback + "(" + resp + ")"
                    }
                    render (contentType: "application/json", text: resp)
                    false
                }
            }
            afterView = { Exception e ->

            }
        }
    }
}
