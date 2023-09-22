package com.ssl.sdk.utils;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultElement;

import javax.xml.parsers.SAXParser;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.List;

import static com.ssl.sdk.constants.Constants.*;

/**
 * Date:2023/09/14 18:18
 * Author:songshuilin
 * Description: 处理渠道资源
 */
public class HandlerChannelSDKResUtils {

    private static String channelSDK = APK_WORKSPACE_BUILD_CHANNEL_SDK_TEMP_DIR + File.separator + "chinasdk";

    private static String assetsTempDir = APK_WORKSPACE_BUILD_CHANNEL_SDK_TEMP_DIR + File.separator + "chinasdk" + File.separator + "assets";

    private static String libPath = APK_WORKSPACE_BUILD_CHANNEL_SDK_TEMP_DIR + File.separator + "chinasdk" + File.separator + "libs";


    private static String resDir = APK_WORKSPACE_BUILD_CHANNEL_SDK_TEMP_DIR + File.separator + "chinasdk" + File.separator + "res";

    private static String aarTempDir = APK_WORKSPACE_BUILD_CHANNEL_SDK_TEMP_DIR + File.separator + "chinasdk" + File.separator + "aarTemp";


    private static String manifestFile = APK_WORKSPACE_BUILD_CHANNEL_SDK_TEMP_DIR + File.separator + "chinasdk" + File.separator + "AndroidManifest.xml";

    private static String aarRes[] = {"res", "AndroidManifest.xml", "classes.jar", "assets", "libs", "jni"};


    /**
     * 处理渠道aar资源
     */
    public static void handlerSDKRes() {

        FileUtils.copyDir(CHANNEL_SDK_DIR, APK_WORKSPACE_BUILD_CHANNEL_SDK_TEMP_DIR);
        LogUtils.d("开始处理渠道sdk aar资源...");
        handlerAar();
    }

    public static void handlerAar() {

        if (!FileUtils.isExit(aarTempDir)) {
            FileUtils.createDir(Paths.get(aarTempDir));
        }

        try {
            // copy libs下aar文件
            Files.walkFileTree(Paths.get(libPath), new HashSet<>(), 1, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String subFileName = file.getFileName().toString();
                    if (subFileName.endsWith(".aar")) {
                        FileUtils.moveFile2Dir(file, Paths.get(aarTempDir));
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            handlerAarTemp();

        } catch (Exception e) {
            LogUtils.d(e.getMessage());
        }

    }


    public static void handlerAarTemp() {

        try {
            Files.walkFileTree(Paths.get(aarTempDir), new HashSet<>(), 1, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        handlerAar(file);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return super.visitFile(file, attrs);
                }
            });
        } catch (Exception e) {
            LogUtils.d(e.getMessage());
        }

    }


    public static void handlerAar(Path aarPath) throws Exception {

        Path aarDir = Paths.get(aarTempDir, FileUtils.removeSuffix(aarPath.getFileName()).toString());

        FileUtils.decompression(aarPath.toString(), aarDir.toString());
        // 解压完，删除
        FileUtils.deleteFile(aarPath);

        Files.walkFileTree(aarDir, new HashSet<>(), 1, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                String subFileName = file.getFileName().toString();

                if (subFileName.equals("classes.jar")) {
                    handlerJarInAar(file, aarPath.getFileName().toString());
                } else if (subFileName.equals("libs")) {
                    handlerLibsInAar(file, aarPath.getFileName().toString());
                } else if (subFileName.equals("assets")) {
                    handlerAssetsInAar(file);
                } else if (subFileName.equals("AndroidManifest.xml")) {
                    handlerManifestInAar(file);
                } else if (subFileName.equals("jni")) {
                    handlerJniInAar(file);
                } else if (subFileName.equals("res")) {
                    handlerResInAar(file);
                }
                return super.visitFile(file, attrs);
            }
        });
    }


    private static void handlerJniInAar(Path jniDir) throws IOException {
        createLibsIfNoExit();
        FileUtils.moveDir(jniDir.toString(), Paths.get(libPath).toString());
    }


    private static void handlerLibsInAar(Path libsDir, String fileName) throws IOException {

        createLibsIfNoExit();

        Files.walkFileTree(libsDir, new HashSet<>(), 1, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                Path targetPath;

                if (file.getFileName().toString().equals("classes.jar")) {
                    targetPath = Paths.get(libPath, FileUtils.removeSuffix(Paths.get(fileName)) + "_libs.jar");
                } else {
                    targetPath = Paths.get(libPath, file.getFileName().toString());
                }
                FileUtils.move(file, targetPath, StandardCopyOption.REPLACE_EXISTING);
                return super.visitFile(file, attrs);
            }
        });

    }

    public static void handlerManifestInAar(Path aar_manifestFile) {

        try {

            SAXReader aar_reader = new SAXReader();
            Document aar_document = aar_reader.read(aar_manifestFile.toFile());
            Element aar_rootElement = aar_document.getRootElement();


            Element aar_application = aar_rootElement.element("application");
            List<Element> aar_uses_permission = aar_rootElement.elements("uses-permission");
            List<Element> aar_permission = aar_rootElement.elements("permission");
            Element aar_supports_screens = aar_rootElement.element("supports-screens");
            Element aar_queries = aar_rootElement.element("queries");


            temp_rootElement = getTargetDocument();

            Element temp_application = temp_rootElement.element("application");
            List<Element> temp_uses_permission = temp_rootElement.elements("uses-permission");
            List<Element> temp_permission = temp_rootElement.elements("permission");
            Element temp_supports_screens = temp_rootElement.element("supports-screens");
            Element temp_queries = temp_rootElement.element("queries");

            // 合并 uses_permission
            diffElement(aar_uses_permission, temp_uses_permission, temp_rootElement);

            // 合并 permission
            diffElement(aar_permission, temp_permission, temp_rootElement);


            // 合并 queries
            diffQueriesElement(aar_queries, temp_queries, temp_rootElement);


            XMLWriter writer = new XMLWriter(new FileWriter(Paths.get(manifestFile).toFile()));
            writer.write(temp_rootElement);
            writer.close();

        } catch (Exception e) {
            LogUtils.d(e.getLocalizedMessage());
        }
    }

    private static void diffQueriesElement(Element aarQueries, Element tempQueries, Element tempRootElement) {
        if (aarQueries == null) {
            return;
        }
        if (tempQueries == null) {
            tempRootElement.add(aarQueries.detach());
        }

        List<Element> aar_package = aarQueries.elements("package");
        List<Element> temp_package = tempQueries.elements("package");
        diffElement(aar_package, temp_package, tempQueries);


        //todo
        List<Element> aar_intent = aarQueries.elements("intent");

        for (Element intent : aar_intent) {
            Element action_intent = intent.element("action");
        }


        List<Element> temp_intent = tempQueries.elements("intent");


    }

    private static Element temp_rootElement;

    public static Element getTargetDocument() {
        try {
            if (temp_rootElement == null) {
                SAXReader temp_reader = new SAXReader();
                Document temp_document = temp_reader.read(Paths.get(manifestFile).toFile());
                temp_rootElement = temp_document.getRootElement();
            }
            return temp_rootElement;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public void hasValueInElement(String value, Element element) {


    }


    public static void handlerAssetsInAar(Path assetsFile) throws IOException {

        if (!FileUtils.isExit(assetsTempDir)) {
            FileUtils.createDir(Paths.get(assetsTempDir));
        }
        FileUtils.moveDir(assetsFile.toString(), Paths.get(assetsTempDir).toString());
    }

    private static void handlerJarInAar(Path jarFile, String fileName) {
        createLibsIfNoExit();
        String jarPath = libPath + File.separator + FileUtils.removeSuffix(Paths.get(fileName)) + ".jar";

        Path targetPath = Paths.get(jarPath);

        FileUtils.move(jarFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }


    private static void handlerResInAar(Path resDir) throws IOException {

        LogUtils.d("handlerResInAar: " + resDir);

        if (!FileUtils.isExit(resDir)) {
            FileUtils.createDir(resDir);
        }

        Files.walkFileTree(resDir, new HashSet<>(), 1, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                // todo res
                return super.visitFile(file, attrs);
            }

        });
    }

    /**
     * 比较两个Element差异   合并到element中
     *
     * @param sourceElementList
     * @param targetElementList
     * @param
     */
    public static void diffElement(List<Element> sourceElementList, List<Element> targetElementList, Element parentElement) {

        if (sourceElementList == null || sourceElementList.isEmpty()) {
            return;
        }

        for (Element sourceElement : sourceElementList) {
            String aar_value = sourceElement.attribute("name").getValue().trim();
            boolean isExit = false;

            if (targetElementList == null || targetElementList.isEmpty()) {
                parentElement.add(sourceElement.detach());
                continue;
            }

            for (Element targetElement : targetElementList) {
                String temp_value = targetElement.attribute("name").getValue().trim();
                if (aar_value.equals(temp_value)) {
                    isExit = true;
                    break;
                }
            }
            if (!isExit) {
                LogUtils.d("aar_value: " + aar_value);
                parentElement.add(sourceElement.detach());
            }
        }
        return;
    }


    public static void createLibsIfNoExit() {
        // 如果 不存在 新建lib目录
        if (!FileUtils.isExit(libPath)) {
            FileUtils.createDir(libPath);
        }
    }

}
