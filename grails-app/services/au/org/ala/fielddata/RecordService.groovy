package au.org.ala.fielddata

import org.apache.commons.io.FilenameUtils
import org.springframework.cache.annotation.Cacheable
import org.apache.commons.lang.time.DateUtils

class RecordService {

    def grailsApplication

    def serviceMethod() {}

    def webService

    def userListMap = [:]

    def lastRefresh

    def toMap(record){
        def dbo = record.getProperty("dbo")
        def mapOfProperties = dbo.toMap()
        def id = mapOfProperties["_id"].toString()
        mapOfProperties["id"] = id
        mapOfProperties.remove("_id")
        //add userDisplayName - Cacheable not working....
        if(mapOfProperties["userId"]){
            def userMap = getUserNamesForIdsMap()
            def userDisplayName = userMap.get(mapOfProperties["userId"])
            if(userDisplayName){
                 mapOfProperties["userDisplayName"] = userDisplayName
            }
        }
        setupMediaUrls(mapOfProperties)
        mapOfProperties
    }

    boolean isCollectionOrArray(object) {
        [Collection, Object[]].any { it.isAssignableFrom(object.getClass()) }
    }

    def setupMediaUrls(mapOfProperties){
        if (mapOfProperties["associatedMedia"] != null){

            if (isCollectionOrArray(mapOfProperties["associatedMedia"])){

                def imagesArray = []
                def originalsArray = []

                mapOfProperties["associatedMedia"].each {

                    def imagePath = it.replaceAll(grailsApplication.config.fielddata.mediaDir,
                            grailsApplication.config.fielddata.mediaUrl)
                    def extension = FilenameUtils.getExtension(imagePath)
                    def pathWithoutExt = imagePath.substring(0, imagePath.length() - extension.length() - 1 )
                    def image = [
                            thumb : pathWithoutExt + "__thumb."+extension,
                            small : pathWithoutExt + "__small."+extension,
                            large : pathWithoutExt + "__large."+extension,
                            raw : imagePath,
                    ]
                    originalsArray << imagePath
                    imagesArray << image
                }
                mapOfProperties['associatedMedia'] = originalsArray
                mapOfProperties['images'] = imagesArray
            } else {
                def imagePath = mapOfProperties["associatedMedia"].replaceAll(grailsApplication.config.fielddata.mediaDir,
                        grailsApplication.config.fielddata.mediaUrl)
                def extension = FilenameUtils.getExtension(imagePath)
                def pathWithoutExt = imagePath.substring(0, imagePath.length() - extension.length() - 1 )
                def image = [
                        thumb : pathWithoutExt + "__thumb."+extension,
                        small : pathWithoutExt + "__small."+extension,
                        large : pathWithoutExt + "__large."+extension,
                        raw : imagePath,
                ]
                mapOfProperties['associatedMedia'] = [imagePath]
                mapOfProperties['images'] = [image]
            }
        }
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
