package com.ssl.sdk.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Date:2023/09/14 15:00
 * Author:songshuilin
 * Description: 文件操作类
 */
public class FileUtils {


    public static void deleteFile(Path filePath) {

        try {
            Files.delete(filePath);
        } catch (Exception e) {
            LogUtils.d(e.getMessage());
        }

    }


    // 文件移动到指定目录
    public static void moveFile2Dir(Path sourcePath, Path targetPath) {
        try {
            Path targetFile = targetPath.resolve(sourcePath.getFileName());
            Files.move(sourcePath, targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LogUtils.d("copyFolder:IOException: " + e.getMessage());
        }
    }


    // 文件夹copy
    public static void copyDir(String source, String target) {
        final Path sourcePath = Paths.get(source);
        final Path targetPath = Paths.get(target);
        try {
            Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path targetFile = targetPath.resolve(sourcePath.relativize(file));
                    Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path targetDir = targetPath.resolve(sourcePath.relativize(dir));
                    Files.createDirectory(targetDir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            LogUtils.d("copyFolder:IOException: " + e.getMessage());
        }
    }


    // 文件移动
    public static void move(String source, String target) {
        Path sourcePath = Paths.get(source);
        Path targetPath = Paths.get(target);
        try {
            Files.move(sourcePath, targetPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void move(Path sourcePath, Path targetPath, CopyOption copyOption) {
        try {
            Files.move(sourcePath, targetPath, copyOption);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static boolean isExit(String path) {
        return Files.exists(Paths.get(path));
    }


    public static boolean isExit(Path path) {
        return Files.exists(path);
    }

    public static boolean isExit(File file) {
        if (file == null) {
            return false;
        }
        return file.exists();
    }

    /**
     * 创建新目录
     *
     * @param path
     */
    public static void createDir(String path) {
        try {
            Path dirPath = Paths.get(path);
            if (!Files.exists(dirPath)) {
                Files.createDirectory(dirPath);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static void createDir(Path dirPath) {
        try {
            if (!Files.exists(dirPath)) {
                Files.createDirectory(dirPath);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除该目录下所有文件与目录  以及当前目录
     *
     * @param path
     */
    public static void deleteDir(String path) {
        Path dirPath = Paths.get(path);
        try {
            Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception e) {
            LogUtils.d(e.getMessage());
        }
    }

    /**
     * 解压
     *
     * @param fromPathStr
     * @param targetPathStr
     * @throws IOException
     */
    public static void decompression(String fromPathStr, String targetPathStr) throws IOException {
        Path fromPath = Paths.get(fromPathStr);
        final Path targetPath = Paths.get(targetPathStr);
        if (!FileUtils.isExit(targetPathStr)) {
            FileUtils.createDir(targetPath);
        }
        FileSystem fs = FileSystems.newFileSystem(fromPath, null);
        long startTime = System.currentTimeMillis();
        Files.walkFileTree(fs.getPath(File.separator), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, targetPath.resolve(file.toString().substring(1)), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (dir.getParent() != null) {
                    if (dir.getFileName().toString().equals("__MACOSX/")) { // MAC系统压缩自带的隐藏文件过滤
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    Files.createDirectories(targetPath.resolve(dir.toString().substring(1)));
                }
                return FileVisitResult.CONTINUE;
            }
        });
        long endTime = System.currentTimeMillis();
        LogUtils.d("解压文件花费时间:" + (endTime - startTime) + " ms");
    }


    public static void moveDir(String fromPathStr, String targetPathStr) throws IOException {
        Path fromPath = Paths.get(fromPathStr);
        final Path targetPath = Paths.get(targetPathStr);
        if (!FileUtils.isExit(targetPathStr)) {
            FileUtils.createDir(targetPath);
        }
        Files.walkFileTree(fromPath, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                Path targetFile = targetPath.resolve(fromPath.relativize(file));
                Files.move(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (dir.getParent() != null) {
                    Path targetDir = targetPath.resolve(fromPath.relativize(dir));
                    if (!isExit(targetDir)){
                        Files.createDirectory(targetDir);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }


    /**
     * 去除文件后缀
     *
     * @return
     */
    public static Path removeSuffix(Path filePath) {
        String fileName = filePath.getFileName().toString();
        String file = fileName.substring(0, fileName.lastIndexOf("."));
        return Paths.get(file);
    }

}
