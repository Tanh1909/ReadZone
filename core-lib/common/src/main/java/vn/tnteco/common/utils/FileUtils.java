package vn.tnteco.common.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.web.multipart.MultipartFile;
import vn.tnteco.common.core.exception.ApiException;
import vn.tnteco.common.data.constant.ErrorResponseBase;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static org.apache.commons.io.FileUtils.sizeOf;
import static org.apache.commons.io.FileUtils.writeByteArrayToFile;


public class FileUtils {
    private static final Long MAX_FILE_SIZE = 1000000L;

    public static String getFileExtensionByFileName(String fileName) {
        String extension = fileName;
        if (fileName.contains(".")) extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        return !org.apache.commons.lang3.StringUtils.isEmpty(extension) &&
                extension.equalsIgnoreCase("jpeg") ? "jpg" : extension;
    }

    public static String getFileTypeByMineType(String mineType) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(mineType)) {
            return null;
        } else {
            if (mineType.contains("/")) {
                return mineType.substring(0, mineType.lastIndexOf("/"));
            } else {
                return mineType;
            }
        }
    }

    public static long getFileSizeByMb(File file) {
        if (file == null) return 0L;
        return file.length() / (1024 * 1024);
    }

    public static File resizeImage(File image, int newWidth) {
        return resizeImage(image, newWidth, false);
    }

    public static File resizeImage(File image, int newWidth, boolean keepFilename) {
        if (!isResizeable(image)) return image;
        try {
            File output = keepFilename ? image : generateFileBySize(image, newWidth);
            BufferedImage originalImage = convertFileToBufferedImage(image);
            if (originalImage != null) {
                if (originalImage.getWidth() > newWidth) {
                    int newHeightOfImage = calculateNewHeightOfImage(originalImage, newWidth);
                    BufferedImage outputImage = resizeImage(originalImage, newWidth, newHeightOfImage);
                    ImageIO.write(outputImage, FilenameUtils.getExtension(image.getName()), output);
                } else {
                    ImageIO.write(originalImage, FilenameUtils.getExtension(image.getName()), output);
                }
                return output;
            }

        } catch (Exception e) {
            System.out.println("Error on " + image.getAbsolutePath());
            e.printStackTrace();
        }
        return image;
    }

    private static int calculateNewHeightOfImage(BufferedImage originalImage, int newWidth) {
        int newHeightOfImage = originalImage.getHeight() * newWidth / originalImage.getWidth();
        return newHeightOfImage <= 0 ? 1 : newHeightOfImage;
    }

    private static boolean isResizeable(File image) {
        return isJPEG(image) || isPNG(image);
    }

    private static File generateFileBySize(File image, int width) {
        return new File(org.apache.commons.lang3.StringUtils.replace(image.getAbsolutePath(), ".", "_" + width + "."));
    }

    public static File convertPNGToJPGB(File pngImage) {
        try {
            File output = new File(pngImage.getAbsolutePath().replace(".png", ".jpg"));
            //read image file
            BufferedImage bufferedPNGImage = convertPNGToBufferImage(pngImage);
            // create a blank, RGB, same width and height, and a white background
            BufferedImage bufferedJPGImage = convertPNGToJPG(bufferedPNGImage);
            // write to jpeg file
            ImageIO.write(bufferedJPGImage, "jpg", output);
            pngImage.delete();
            return output;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pngImage;
    }


    private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {

        int imageType = originalImage.getTransparency() == Transparency.OPAQUE ? BufferedImage.TYPE_INT_RGB :
                BufferedImage.TYPE_INT_ARGB;
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight,
                imageType);

        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.setComposite(AlphaComposite.Src);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.drawImage(originalImage.getScaledInstance(targetWidth,
                        targetHeight, Image.SCALE_SMOOTH),
                0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
        return resizedImage;
    }

    public static BufferedImage convertPNGToJPG(BufferedImage bufferedImage) {
        // create a blank, RGB, same width and height, and a white background
        BufferedImage newBufferedImage = new BufferedImage(bufferedImage.getWidth(),
                bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        newBufferedImage.createGraphics().drawImage(bufferedImage, 0, 0,
                Color.WHITE, null);
        return newBufferedImage;
    }

    public static BufferedImage convertPNGToBufferImage(File file) {
        BufferedImage image = null;
        try {
            // TODO: Handle no reader case

            ImageInputStream input = ImageIO.createImageInputStream(file);
            ImageReader reader = ImageIO.getImageReaders(input).next();
            reader.setInput(input);
            int width = reader.getWidth(0);
            int height = reader.getHeight(0);

            // Allocate an image to be used as destination
            ImageTypeSpecifier imageType = reader.getImageTypes(0).next();
            image = imageType.createBufferedImage(width, height);

            ImageReadParam param = reader.getDefaultReadParam();
            param.setDestination(image);
            try {
                reader.read(0, param); // Read as much as possible into image
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                reader.dispose();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }

    public static BufferedImage convertJPEGToBufferedImage(File jpegImage) {
        try {
            return ImageIO.read(jpegImage);
        } catch (Exception e) {
            return null;
        }
    }

    private static BufferedImage convertFileToBufferedImage(File file) throws IOException {
        if (isJPEG(file)) return convertJPEGToBufferedImage(file);
        else if (isPNG(file)) return convertPNGToBufferImage(file);
        else return ImageIO.read(file);
    }

    private static boolean isPNG(File image) {
        return getFileExtensionByFileName(image.getName()).equals("png");
    }

    private static boolean isJPEG(File image) {
        return org.apache.commons.lang3.StringUtils.containsAny(getFileExtensionByFileName(image.getName()),
                "jpg", "jpeg");
    }


    public static String buildFileNameWithExtension(String fileName, String extension) {
        if (fileName == null) {
            fileName = UUID.randomUUID().toString();
        } else if (org.apache.commons.lang3.StringUtils.contains(fileName, ".")) {
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        }
        String randString = fileName + "-" + (System.currentTimeMillis() / 1000);
        return String.join(".", StringUtils.textToUrl(randString), extension);
    }


    public static File writeFile(byte[] bytes, String pathFolder, String fileName) throws IOException {
        File tmpFile = createFile(pathFolder, fileName);
        writeByteArrayToFile(tmpFile, bytes);
        return tmpFile;
    }

    public static String readFile(String fileName) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fileName);
            return IOUtils.toString(fis, "UTF-8");
        } catch (IOException e) {
            return null;
        }
    }

    public static File createFile(String pathFolder, String fileName) {
        return new File(String.join(File.separator, pathFolder, fileName));
    }

    public static boolean tooBigPNG(File image) {
        return getFileExtensionByFileName(image.getName()).equals("png") && sizeOf(image) > MAX_FILE_SIZE;
    }

    public static byte[] decodeBase64(String base64) {
        if (base64.contains(",")) {
            String[] split = base64.split(",");
            if (split.length > 1) base64 = split[1];
        }
        return Base64.decodeBase64(base64);
    }

    public static boolean isImage(File file) {
        boolean b = false;
        try {
            b = (ImageIO.read(file) != null);
        } catch (IOException e) {
            return false;
        }
        return b;
    }

    public static InputStream multipartToInputStream(MultipartFile file) {
        try {
            return file.getInputStream();
        } catch (Exception e) {
            throw new ApiException(ErrorResponseBase.BUSINESS_ERROR);
        }
    }
}

