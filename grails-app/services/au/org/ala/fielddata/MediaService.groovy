package au.org.ala.fielddata

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils

class MediaService {

    static transactional = false

    def grailsApplication

    def THUMB = [suffix: "__thumb", size: 100f ]
    def SMALL = [suffix: "__small", size: 314f ]
    def LARGE = [suffix: "__large", size: 650f ]

    def serviceMethod() {}

    def File copyToImageDir(recordId,currentFilePath){

        File directory = new File(grailsApplication.config.fielddata.mediaDir + recordId)
        if(!directory.exists()){
            FileUtils.forceMkdir(directory)
        }
        File destFile = new File(grailsApplication.config.fielddata.mediaDir + recordId + File.separator + (new File(currentFilePath)).getName().replaceAll(" ", "_"))
        try {
            FileUtils.copyFile(new File(currentFilePath),destFile)
            //generate thumbnail, small, large
            generateAllSizes(destFile)
            destFile
        } catch (Exception e){
            log.info "Unable to copy across file: "  + currentFilePath + " for record " + recordId
            //clean up afterwards
            if(directory.listFiles().length == 0){
                FileUtils.forceDelete(directory)
            }
            null
        }
    }

    def download(recordId, idx, address){
        File mediaDir = new File(grailsApplication.config.fielddata.mediaDir  + recordId + File.separator)
        if (!mediaDir.exists()){
            FileUtils.forceMkdir(mediaDir)
        }
        def destFile = new File(grailsApplication.config.fielddata.mediaDir + recordId + File.separator + idx + "_" +address.tokenize("/")[-1])
        def out = new BufferedOutputStream(new FileOutputStream(destFile))
        log.debug("Trying to download..." + address)
        out << new URL(address).openStream()
        out.close()
        generateAllSizes(destFile)
        destFile
    }


    /** Generate thumbnails of all sizes */
    def generateAllSizes(File source){
        def fileName = source.getName()
        if(!fileName.contains(THUMB.suffix) && !fileName.contains(SMALL.suffix) && !fileName.contains(LARGE.suffix)){
            generateThumbnail(source, THUMB)
            generateThumbnail(source, SMALL)
            generateThumbnail(source, LARGE)
        }
    }

    /** Generate an image of the specified size.*/
    def generateThumbnail(source, imageSize){
        def extension = FilenameUtils.getExtension(source.getAbsolutePath())
        def targetFilePath = source.getAbsolutePath().replace("." + extension, imageSize.suffix + "." + extension)
        def target = new File(targetFilePath)
        generateThumbnail(source, target, imageSize.size)
    }

    /** Generate a thumbanail to the specified file */
    def generateThumbnail(source, target, thumbnailSize){
        def t = new ThumbnailableImage(source)
        t.writeThumbnailToFile(target, thumbnailSize)
    }
}