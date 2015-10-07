package tmt.transformations

import java.io.{ByteArrayOutputStream, ByteArrayInputStream}
import javax.imageio.ImageIO

import org.bytedeco.javacpp.helper.opencv_core.AbstractIplImage
import org.bytedeco.javacpp.opencv_core
import org.bytedeco.javacpp.opencv_core.IplImage
import org.bytedeco.javacv.Java2DFrameConverter
import org.bytedeco.javacv.OpenCVFrameConverter.ToIplImage
import tmt.shared.models.Image

object ImageRotationUtility {
  private def getIplImage(image: Image, openCVFrameConverter: ToIplImage, java2DFrameConverter: Java2DFrameConverter) = {
    val byteArrayInputStream = new ByteArrayInputStream(image.bytes)
    val bufferedImage = ImageIO.read(byteArrayInputStream)

    openCVFrameConverter.convert(java2DFrameConverter.convert(bufferedImage))
  }

  private def getImage(iplImage: IplImage, imageName: String, openCVFrameConverter: ToIplImage, java2DFrameConverter: Java2DFrameConverter) = {
    val bufferedImage = java2DFrameConverter.convert(openCVFrameConverter.convert(iplImage))

    val byteArrayOutputStream = new ByteArrayOutputStream()
    ImageIO.write(bufferedImage, "jpeg", byteArrayOutputStream)
    byteArrayOutputStream.flush()
    byteArrayOutputStream.close()

    Image(imageName, byteArrayOutputStream.toByteArray)
  }


  def rotate(image: Image) = {
    val java2DFrameConverter = new Java2DFrameConverter()
    val converter = new ToIplImage()

    val iplImage = getIplImage(image, converter, java2DFrameConverter)
    val rotatedImage = AbstractIplImage.create(iplImage.height(), iplImage.width(), iplImage.depth(), iplImage.nChannels())
    opencv_core.cvTranspose(iplImage, rotatedImage)
    opencv_core.cvFlip(rotatedImage, rotatedImage, 90)

    getImage(rotatedImage, image.name, converter, java2DFrameConverter)
  }
}
