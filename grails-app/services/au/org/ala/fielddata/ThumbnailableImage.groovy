package au.org.ala.fielddata

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import org.apache.commons.io.FileUtils
import org.imgscalr.Scalr

class ThumbnailableImage {

    ThumbnailableImage(imageFile){ this.imageFile = imageFile }

    def imageFile
    BufferedImage img = ImageIO.read(imageFile)
    //def fss = new FileSeekableStream(imageFile)
    //def originalImage = JAI.create("stream", fss)

    /**
     * Write a thumbnail to file
     */
    def writeThumbnailToFile(File newThumbnailFile, Float edgeLength) {

        def height = img.getHeight()
        def width = img.getWidth()

//        // thumbnail it
//        BufferedImage img = ImageIO.read(f)
//        BufferedImage tn = Scalr.resize(img, 100, Scalr.OP_ANTIALIAS)
//        File tnFile = new File(colDir, thumbFilename)
//        try {
//            ImageIO.write(tn, ext, tnFile)
//        } catch(IOException e) {
//            println "Write error for " + tnFile.getPath() + ": " + e.getMessage()
//        }

        //def renderedImage = originalImage.createSnapshot() as javax.media.jai.RenderedOp
        //println("height: " + height + ", edgeLength: " + edgeLength + ", width:  " + width)

        if (width > edgeLength) {
            //def denom = height > width ? height : width
            BufferedImage tn = Scalr.resize(img, edgeLength as Integer, Scalr.OP_ANTIALIAS)
            try {
                ImageIO.write(tn, "JPG", newThumbnailFile)  // accept other file formats
            } catch(IOException e) {
                println "Write error for " + newThumbnailFile.getPath() + ": " + e.getMessage()
            }

//            def denom = height > width ? height : width
//            def modifier = edgeLength / denom
//            def w = (width * modifier).toInteger()
//            def h = (height * modifier).toInteger()
//            def i = renderedImage.getAsBufferedImage().getScaledInstance(w, h, Image.SCALE_SMOOTH)
//            def bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
//            def g = bufferedImage.createGraphics()
//            g.drawImage(i, null, null)
//            g.dispose()
//            i.flush()
//            def modifiedImage = JAI.create("awtImage", bufferedImage as Image)
//            def fOut = new FileOutputStream(newThumbnailFile)
//            ImageIO.write(modifiedImage, "jpg", fOut)
//            fOut.flush()
//            fOut.close()
        } else {
            FileUtils.copyFile(imageFile, newThumbnailFile)
        }
    }
}