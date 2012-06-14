package au.org.ala.fielddata

import org.apache.commons.io.FileUtils

class MediaService {

    static transactional = false

    def serviceMethod() {}

    def String mediaDirRoot = "/data/fielddata/media/"

    def File copyToImageDir(recordId,currentFilePath){

        File directory = new File(mediaDirRoot + recordId)
        if(!directory.exists()){
            FileUtils.forceMkdir(directory)
        }
        File destFile = new File(mediaDirRoot + recordId + File.separator + (new File(currentFilePath)).getName().replaceAll(" ", "_"))
        try {
            FileUtils.copyFile(new File(currentFilePath),destFile)

            //generate thumbnail, small, large



            destFile
        } catch (Exception e){
            //clean up afterwards
            if(directory.listFiles().length == 0){
                FileUtils.forceDelete(directory)
            }
            null
        }
    }
}



