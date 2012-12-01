package au.org.ala.fielddata

import org.apache.commons.io.FilenameUtils

class RecordService {

    def grailsApplication

    def serviceMethod() {}

    def toMap(record){
        def dbo = it.getProperty("dbo")
        def mapOfProperties = dbo.toMap()
        def id = mapOfProperties["_id"].toString()
        mapOfProperties["id"] = id
        mapOfProperties.remove("_id")
        setupMediaUrls(mapOfProperties)
    }

    def setupMediaUrls(mapOfProperties){
        if (mapOfProperties["associatedMedia"] != null){

            if (isCollectionOrArray(mapOfProperties["associatedMedia"])){

                def imagesArray = []

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

                    imagesArray.add(image)
                }
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
                mapOfProperties['images'] = [image]
            }
        }
    }
}
