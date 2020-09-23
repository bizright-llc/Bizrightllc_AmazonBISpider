package com.spider.amazon.utils;

import com.google.common.io.Files;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileFilter;
import java.util.Optional;

/**
 * File Utility
 */
public class FileUtils {

    private static Logger logger = LoggerFactory.getLogger(FileUtils.class);

    /**
     * Get the files in dir with filename contains
     * @param dirPath
     * @param filename
     * @return
     */
    public static File[] getFileFromDirWithName(String dirPath, String filename){

        class MyFileFilter implements FileFilter {

            public boolean accept(File f){
                if(f.getName().contains(filename)){
                    return true;
                }
                return false;
            }
        }

        MyFileFilter filter = new MyFileFilter();

        return getFileFromDir(dirPath, filter);
    }

    /**
     * Get the last modified file in the dir
     *
     * @param dirPath
     * @return
     */
    public static File getLatestFileFromDir(String dirPath) {

        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return null;
        }

        File lastModifiedFile = files[0];
        for (int i = 1; i < files.length; i++) {
            if (lastModifiedFile.lastModified() < files[i].lastModified()) {
                lastModifiedFile = files[i];
            }
        }
        return lastModifiedFile;
    }

    /**
     * @param dirPath
     * @param filename
     * @return
     */
    public static File getLatestFileWithNameFromDir(String dirPath, String filename) {

        File[] files = getFileFromDirWithName(dirPath, filename);

        File lastModifiedFile = files[0];
        for (int i = 1; i < files.length; i++) {
            if (lastModifiedFile.lastModified() < files[i].lastModified()) {
                lastModifiedFile = files[i];
            }
        }
        return lastModifiedFile;
    }

    public static File[] getFileFromDir(String dirPath, FileFilter filter) {

        File dir = null;

        try{
            dir = new File(dirPath);
        }catch (Exception ex){

            ex.printStackTrace();

            logger.error(ex.getLocalizedMessage());

            return null;

        }

        File[] files = filter == null ? dir.listFiles() : dir.listFiles(filter);
        if (files == null || files.length == 0) {
            return null;
        }

        return files;
    }

    public static String getFileExtension(String filename){
        return Files.getFileExtension(filename);
    }

}
