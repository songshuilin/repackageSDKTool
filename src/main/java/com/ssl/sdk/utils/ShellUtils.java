package com.ssl.sdk.utils;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static com.ssl.sdk.constants.Constants.*;

/**
 * Date:2023/09/14 14:40
 * Author:songshuilin
 * Description: 执行shell 命令工具
 */
public class ShellUtils {


    public static void executeCommand(String cmd) {
        LogUtils.d("执行命令： " + cmd);

        try {

            Process proc = Runtime.getRuntime().exec(cmd);
            GobblerThread errorGobbler = new GobblerThread(proc.getErrorStream(), "ERROR");
            GobblerThread outputGobbler = new GobblerThread(proc.getInputStream(), "OUTPUT");
            errorGobbler.start();
            outputGobbler.start();
            int exitCode = proc.waitFor();
            if (exitCode != 0) {
                // 如果命令执行失败，可以通过 process.getErrorStream() 获取错误信息
                LogUtils.d("命令执行失败: " + exitCode);
            }
            proc.destroy();
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.d("Exception: " + e.getLocalizedMessage());
        }
    }


    public static void decompileApk(String apkSourcePath) {

        FileUtils.deleteDir(APK_WORKSPACE_DIR);

        FileUtils.createDir(APK_WORKSPACE_DIR);

        String cmd = String.format("java -jar  %s" + File.separator + "apktool.jar  d %s -o %s  -f", APK_TOOL_DIR, apkSourcePath, APK_WORKSPACE_BUILD_APK_TEMP_DIR);

        ShellUtils.executeCommand(cmd);

    }


    public static void res_compile_r_java() {

        String rDir = APK_WORKSPACE_BUILD_APK_TEMP_DIR + File.separator + "R";

        String resDir = APK_WORKSPACE_BUILD_APK_TEMP_DIR + File.separator + "res";

        String manifestFile = APK_WORKSPACE_BUILD_APK_TEMP_DIR + File.separator + "AndroidManifest.xml";

        // 这个目录不存在  新建
        if (!FileUtils.isExit(rDir)) {
            FileUtils.createDir(Paths.get(rDir));
        }


        for (String packgeName : HandlerChannelSDKResUtils.packageNames) {
            String cmd = String.format("%s" + File.separator + "aapt.exe p -f -m -J  %s -S %s -I %s/android.jar -M %s --custom-package %s", APK_TOOL_DIR, rDir, resDir, APK_TOOL_DIR, manifestFile, packgeName);
            ShellUtils.executeCommand(cmd);

            r_java_compile_r_class(packgeName);

            r_class_compile_r_jar(packgeName);
        }

        FileUtils.deleteDir(rDir);

    }


    public static void r_java_compile_r_class(String packageName) {

        String rDir = APK_WORKSPACE_BUILD_APK_TEMP_DIR + File.separator + "R";

        String[] packagePath = packageName.split("\\.");

        Path rDirPath = Paths.get(rDir);

        for (String path : packagePath) {
            rDirPath = rDirPath.resolve(path);
        }

        String r_Dir = rDirPath.toAbsolutePath().toString();

        String cmd = String.format("javac -source 1.8 -target 1.8 -encoding UTF-8 " + "%s" + File.separator + "*.java", r_Dir);
        ShellUtils.executeCommand(cmd);

    }


    public static void r_class_compile_r_jar(String packageName) {

        String rDir = APK_WORKSPACE_BUILD_APK_TEMP_DIR + File.separator + "R";

        String libsTargetDir = APK_WORKSPACE_BUILD_APK_TEMP_DIR + File.separator + "lib";

        String[] packagePath = packageName.split("\\.");

        Path rDirPath = Paths.get(rDir);

        for (String path : packagePath) {
            rDirPath = rDirPath.resolve(path);
        }

        String r_Dir = rDirPath.toAbsolutePath().toString();

        String cmd = String.format("cmd /c cd %s && jar cvf %s *.class", r_Dir, packageName+".R.jar");

        ShellUtils.executeCommand(cmd);


        FileUtils.moveFile2Dir(Paths.get(r_Dir, packageName + ".R.jar"), Paths.get(libsTargetDir));


    }


    public static void jar_compile_dex_dx() {

        String jarFilePath = "D:\\code\\dabaotool\\repackageSDKTool\\WorkSpace\\BuildApk\\apkTemp\\R\\com\\yostar\\sdk\\r.jar";


        String dexFilePath = "D:\\code\\dabaotool\\repackageSDKTool\\WorkSpace\\BuildApk\\apkTemp\\R\\com\\yostar\\sdk\\r.dex";


        String cmd = String.format("java -jar %s" + File.separator + "dx.jar --dex --output=%s %s", APK_TOOL_DIR, dexFilePath, jarFilePath);

        ShellUtils.executeCommand(cmd);

    }


    public static void jar_compile_dex_d8() {

        String libsTargetDir = APK_WORKSPACE_BUILD_APK_TEMP_DIR + File.separator + "lib";

        String dexDir = APK_WORKSPACE_BUILD_APK_TEMP_DIR + File.separator + "dexs";

        if (!FileUtils.isExit(dexDir)) {
            FileUtils.createDir(dexDir);
        }

        try {
            Files.walkFileTree(Paths.get(libsTargetDir), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String fileName = file.getFileName().toString();
                    if (fileName.endsWith(".jar")) {

                        Path renameDex = Paths.get(dexDir, fileName.replaceAll("\\.jar", ""));

                        if (!FileUtils.isExit(renameDex)) {
                            FileUtils.createDir(renameDex);
                        }

                        String cmd = String.format(APK_TOOL_DIR + File.separator + "d8.bat --release %s --output=%s",
                                file.toAbsolutePath(), renameDex);
                        ShellUtils.executeCommand(cmd);
                    }

                    return FileVisitResult.CONTINUE;
                }
            });


            Files.walkFileTree(Paths.get(libsTargetDir), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String fileName = file.getFileName().toString();
                    if (fileName.endsWith(".jar")) {
                        FileUtils.deleteFile(file);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void dex_compile_smali() {

        String dexDir = APK_WORKSPACE_BUILD_APK_TEMP_DIR + File.separator + "dexs";

        String smaliDir = APK_WORKSPACE_BUILD_APK_TEMP_DIR + File.separator + "smali";


        try {
            Files.walkFileTree(Paths.get(dexDir), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                    String cmd = String.format("java -jar %s/baksmali.jar -o %s %s", APK_TOOL_DIR, smaliDir, file.toAbsolutePath());

                    ShellUtils.executeCommand(cmd);

                    return FileVisitResult.CONTINUE;
                }
            });


            // 处理完 删除dexs 目录
            FileUtils.deleteDir(dexDir);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void compile_build_apk() {

        String apkPath = APK_WORKSPACE_BUILD_DIR + File.separator + "temp.apk";

        String cmd = String.format("java -jar  %s" + File.separator + "apktool.jar  b %s -o %s  ", APK_TOOL_DIR, APK_WORKSPACE_BUILD_APK_TEMP_DIR, apkPath);

        ShellUtils.executeCommand(cmd);

    }

    public static void apk_sign() {

        String apkTemp = APK_WORKSPACE_BUILD_DIR + File.separator + "temp.apk";

        String apkSign = APK_WORKSPACE_BUILD_DIR + File.separator + "sign.apk";

        String keystore = "D:\\code\\dabaotool\\repackageSDKTool\\sample_01.keystore";
        String keypass = "123456";
        String storepass = "123456";
        String alias = "sample_01.keystore";

        String cmd = String.format("jarsigner -verbose -keystore %s -storepass %s  -keypass %s -signedjar %s %s %s",
                keystore, keypass, storepass, apkSign, apkTemp, alias
        );

        ShellUtils.executeCommand(cmd);

    }


    public static void apk_zipalign() {

        String apkSign = APK_WORKSPACE_BUILD_DIR + File.separator + "sign.apk";

        String zipalignApkSign = APK_WORKSPACE_BUILD_DIR + File.separator + "zipalignSign.apk";

        String cmd = String.format("%s" + File.separator + "zipalign.exe -f -v 4 %s %s", APK_TOOL_DIR, apkSign, zipalignApkSign);

        ShellUtils.executeCommand(cmd);
    }


}
