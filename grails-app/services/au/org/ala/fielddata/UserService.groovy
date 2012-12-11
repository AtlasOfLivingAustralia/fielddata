package au.org.ala.fielddata

import org.apache.commons.lang.time.DateUtils

class UserService {

    def serviceMethod() {}

    def grailsApplication

    def webService

    private def userListMap = [:]

    private def userEmailMap = [:]

    def refreshUserDetails(){
        try {
            def replacementEmailMap = [:]
            def replacementIdMap = [:]
            def userListJson = webService.doPost(grailsApplication.config.userDetailsUrl)
            log.info "Refreshing user lists....."
            if (userListJson && !userListJson.error) {
                userListJson.resp.each {
                    replacementEmailMap.put(it.email.toLowerCase(),  it.id);
                    replacementIdMap.put(it.id, it.email.toLowerCase());
                }
                log.info "Refreshing user lists.....count: " + replacementEmailMap.size()
                synchronized (this){
                    this.userEmailMap = replacementEmailMap
                    this.userListMap = replacementIdMap
                }
            } else {
                log.info "error -  " + userListJson.getClass() + ":"+ userListJson
            }
        } catch (Exception e) {
            log.error "Cache refresh error" + e.message
        }
    }

    def getUserEmailToIdMap() {
        this.userEmailMap
    }

    def getUserNamesForIdsMap() {
        this.userListMap
    }
}
