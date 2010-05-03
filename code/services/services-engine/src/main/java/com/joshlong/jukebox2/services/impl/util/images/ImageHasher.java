package com.joshlong.jukebox2.services.impl.util.images;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;

/**
 * The idea behind this is that we want to build a hash of each image and use that to determine if images are the same.
 *
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class ImageHasher {

    public String stringHash  (BufferedImage b) throws Throwable
    {
        int [] data  = this.dataHash( b) ;
        StringBuilder builder = new StringBuilder();
        for(int i :data)
            builder.append(i);
        return builder.toString() ;
    }
    public static void main(String[] args) throws Throwable {

        ImageHasher imageHasher = new ImageHasher();

        File desktop = new File(SystemUtils.getUserHome(), "Desktop");

        File imgSubjects = new File(new File(desktop, "image_dupe_detection"), "subjects");

        File a = new File(imgSubjects, "10.jpg");
        File b = new File(imgSubjects, "10.tiff.jpg");

        BufferedImage bi1 = imageHasher.imageToBufferedImage(  GrayFilter.createDisabledImage  (imageHasher.fromFile(a)));
        BufferedImage bi2 = imageHasher.imageToBufferedImage( GrayFilter.createDisabledImage (imageHasher.fromFile(b)));

        String aHash = imageHasher.stringHash( bi1);
        String bHash = imageHasher.stringHash(bi2);

        System.out.println(StringUtils.difference(aHash ,bHash));
        
        //int levenshteinDistance = StringUtils.getLevenshteinDistance(  imageHasher.stringHash( bi1), imageHasher.stringHash( bi2));
        //System.out.println( "levenshteinDistance: " + levenshteinDistance ) ; 

    }

    public int ignoreSizeAndHashAndColor(BufferedImage bi, int w, int h) throws Throwable {

        BufferedImage bimg = imageToBufferedImage(GrayFilter.createDisabledImage(bi));
        return this.ignoreSizeAndHash(bimg, w, h);
    }

    /**
     * this resizes an image and then returns a hash of the image
     *
     * @param b1 the buffered image we want to obtain a hash for
     * @param w  the width we'd like it resized to before hashing
     * @param h  the height we'd like it resized to before hashing
     *
     * @return the hash
     *
     * @throws Throwable if anything should happen..
     */
    public int ignoreSizeAndHash(BufferedImage b1, int w, int h) throws Throwable {
        BufferedImage resized = this.resize(b1, w, h);
        return this.hash(resized);
    }

    public boolean compare(BufferedImage b1, BufferedImage b2) throws Throwable {
        return this.hash(b1) == this.hash(b2);
    }

    public boolean ignoreSizeAndCompare(BufferedImage ogA, BufferedImage ogB) throws Throwable {

        int w = Math.max(ogA.getWidth(), ogB.getWidth()),
                h = Math.max(ogA.getHeight(), ogB.getHeight());
        BufferedImage a = this.resize(ogA, w, h);
        BufferedImage b = this.resize(ogB, w, h);
        return this.compare(a, b);
    }

    // buffered images are just better.

    BufferedImage imageToBufferedImage(Image img) {
        BufferedImage bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bi.createGraphics();
        g2.drawImage(img, null, null);
        return bi;
    }

    int hash(BufferedImage src) throws IOException {

        return java.util.Arrays.hashCode(dataHash(src));
    }

    private int[] dataHash(BufferedImage dst) throws IOException {
        //double aspectRatio = (double) src.getHeight() / src.getWidth();
    /*    BufferedImage dst = new BufferedImage(src.getWidth(), *//*(int) (100 * aspectRatio)*//* src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, dst.getWidth(), dst.getHeight(), 0, 0, src.getWidth(), src.getHeight(), null);
        //g.drawImage(src, null, null);*/
        int[] data = ((DataBufferInt) dst.getRaster().getDataBuffer()).getData();
        return data;
    }

    BufferedImage fromFile(File f) throws IOException {
        return ImageIO.read(f);
    }

    boolean hashEquals(int[] rgb1, int[] rgb2) {
        for (int i = 0; i < rgb1.length; i++) {
            if (rgb1[i] != rgb2[i]) return false;
        }
        return true;
    }

    BufferedImage resize(final BufferedImage img, int newW, int newH) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage dimg = new BufferedImage(newW, newH, img.getType());
        Graphics2D g = dimg.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, newW, newH, 0, 0, w, h, null);
        g.dispose();
        return dimg;
    }

}
