package au.org.ala.fielddata

import com.sun.media.jai.codec.FileSeekableStream
import javax.imageio.ImageIO
import javax.media.jai.RenderedOp
import javax.media.jai.JAI
import java.awt.Graphics2D
import java.awt.Image
import java.awt.image.BufferedImage
import org.apache.commons.io.FileUtils

class ThumbnailableImage {

    ThumbnailableImage(imageFile){ this.imageFile = imageFile }

    def imageFile
    def fss = new FileSeekableStream(imageFile)
    def originalImage = JAI.create("stream", fss)

    /**
     * Write a thumbnail to file
     */
    def writeThumbnailToFile(File newThumbnailFile, Float edgeLength) {

        def height = originalImage.getHeight()
        def width = originalImage.getWidth()
        def renderedImage = originalImage.createSnapshot() as javax.media.jai.RenderedOp
        if (!(height < edgeLength && width < edgeLength)) {
            def denom = height > width ? height : width
            def modifier = edgeLength / denom
            def w = (width * modifier).toInteger()
            def h = (height * modifier).toInteger()
            def i = renderedImage.getAsBufferedImage().getScaledInstance(w, h, Image.SCALE_SMOOTH)
            def bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
            def g = bufferedImage.createGraphics()
            g.drawImage(i, null, null)
            g.dispose()
            i.flush()
            def modifiedImage = JAI.create("awtImage", bufferedImage as Image)
            def fOut = new FileOutputStream(newThumbnailFile)
            ImageIO.write(modifiedImage, "jpg", fOut)
            fOut.flush()
            fOut.close()
        } else {
            FileUtils.copyFile(imageFile, newThumbnailFile)
        }
    }
}