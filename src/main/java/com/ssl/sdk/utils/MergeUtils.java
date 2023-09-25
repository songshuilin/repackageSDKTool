package com.ssl.sdk.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static com.ssl.sdk.constants.Constants.APK_WORKSPACE_BUILD_APK_TEMP_DIR;
import static com.ssl.sdk.constants.Constants.APK_WORKSPACE_BUILD_CHANNEL_SDK_TEMP_DIR;

/**
 * Date:2023/09/25 16:12
 * Author:songshuilin
 * Description: sdk资源与apk 合并
 */
public class MergeUtils {


    public static void mergeAssets() {

        String assetsSourceDir = APK_WORKSPACE_BUILD_CHANNEL_SDK_TEMP_DIR + File.separator + "chinasdk" + File.separator + "assets";

        String assetsTargetDir = APK_WORKSPACE_BUILD_APK_TEMP_DIR + File.separator + "assets";

        try {
            FileUtils.moveDir(assetsSourceDir, assetsTargetDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void mergelibs() {

        String libsSourceDir = APK_WORKSPACE_BUILD_CHANNEL_SDK_TEMP_DIR + File.separator + "chinasdk" + File.separator + "libs";

        String libsTargetDir = APK_WORKSPACE_BUILD_APK_TEMP_DIR + File.separator + "libs";

        try {
            FileUtils.moveDir(libsSourceDir, libsTargetDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void mergelRes() {

        String resSourceDir = APK_WORKSPACE_BUILD_CHANNEL_SDK_TEMP_DIR + File.separator + "chinasdk" + File.separator + "res";

        String resTargetDir = APK_WORKSPACE_BUILD_APK_TEMP_DIR + File.separator + "res";

        Path resSourcePath = Paths.get(resSourceDir);
        Path resTargetPath = Paths.get(resTargetDir);

        try {
            Files.walkFileTree(resSourcePath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String resDirName = file.getParent().getFileName().toString();

                    Path targetFile = resTargetPath.resolve(resSourcePath.relativize(file));

                    if (resDirName.startsWith("value")) {
                        // values 文件不存在  直接复制
                        if (!FileUtils.isExit(targetFile)) {
                            Files.move(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                        } else {
                            // 合并res/values 文件
                            merResValue(file, targetFile);
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                    } else {
                        Files.move(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (dir.getParent() != null) {
                        Path targetDir = resTargetPath.resolve(resSourcePath.relativize(dir));
                        if (!FileUtils.isExit(targetDir)) {
                            Files.createDirectory(targetDir);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static void merResValue(Path file, Path targetFile) {

    }


}
