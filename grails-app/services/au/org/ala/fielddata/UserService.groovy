package au.org.ala.fielddata

import org.apache.commons.lang.time.DateUtils

class UserService {

    def serviceMethod() {}

    def grailsApplication

    def webService

    private def userListMap = [:]

    private def userEmailMap = [:]

    private def lastRefresh

    def getUserEmailToIdMap() {
        def now = new Date()
        if(!lastRefresh ||  DateUtils.addMinutes(lastRefresh, 10) < now){
            try {
                def replacementMap = [:]
                def userListJson = webService.doPost(grailsApplication.config.userDetails.emails.url)
                log.info "Refreshing user lists....."
                if (userListJson && !userListJson.error) {
                    userListJson.resp.each {
                        println("Adding: " + it.email +" -> " + it.id)
                        replacementMap.put(it.email.toLowerCase(),  it.id);
                    }
                } else {
                    log.info "error -  " + userListJson.getClass() + ":"+ userListJson
                }
                this.userEmailMap = replacementMap
                lastRefresh = now
            } catch (Exception e) {
                log.error "Cache refresh error" + e.message
            }
        }
        this.userEmailMap
    }

    def getUserNamesForIdsMap() {
        def now = new Date()
        if(!lastRefresh ||  DateUtils.addMinutes(lastRefresh, 10) < now){
            try {
                def replacementMap = [:]

                def userListJson = webService.doPost(grailsApplication.config.userDetails.url)
                log.info "Refreshing user lists....."
                if (userListJson && !userListJson.error) {
                    userListJson.resp.keySet().each {
                        replacementMap.put(it.toString(),  userListJson.resp[it]);
                    }
                } else {
                    log.info "error -  " + userListJson.getClass() + ":"+ userListJson
                }
                this.userListMap = replacementMap
                lastRefresh = now
            } catch (Exception e) {
                log.error "Cache refresh error" + e.message
            }
        }
        this.userListMap
    }
}
