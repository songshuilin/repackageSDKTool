package com.ssl.sdk.utils;

import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
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


    public static List<String> packageNames = new ArrayList<>();



    /**
     * 处理渠道aar资源
     */
    public static void handlerSDKRes() {
        packageNames.add("com.example.oaidtest2");
        FileUtils.copyDir(CHANNEL_SDK_DIR, APK_WORKSPACE_BUILD_CHANNEL_SDK_TEMP_DIR);
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
            e.printStackTrace();
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


        Path rTxtPath = aarDir.resolve("R.txt");
        Path manifestPath = aarDir.resolve("AndroidManifest.xml");

        if (rTxtPath.toFile().length() > 0) {
            SAXReader aar_reader = new SAXReader();
            Document aar_document = aar_reader.read(manifestPath.toFile());
            Element aar_rootElement = aar_document.getRootElement();
            String aarPackageName = aar_rootElement.attributeValue("package");
            if (!aarPackageName.startsWith("android")){
                packageNames.add(aarPackageName);
            }
        }
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
            List<Element> aar_meta_datas = aar_rootElement.elements("meta-data");

            temp_rootElement = getTargetDocument();

            Element temp_application = temp_rootElement.element("application");
            List<Element> temp_uses_permission = temp_rootElement.elements("uses-permission");
            List<Element> temp_permission = temp_rootElement.elements("permission");
            Element temp_supports_screens = temp_rootElement.element("supports-screens");
            Element temp_queries = temp_rootElement.element("queries");
            List<Element> temp_meta_datas = temp_rootElement.elements("meta-data");

            // 合并根节点下meta-data
            diffElement(aar_meta_datas, temp_meta_datas, temp_rootElement);

            // 合并 uses_permission
            diffElement(aar_uses_permission, temp_uses_permission, temp_rootElement);

            // 合并 permission
            diffElement(aar_permission, temp_permission, temp_rootElement);

            // 合并 queries
            diffQueriesElement(aar_queries, temp_queries, temp_rootElement);


            // 合并application
            diffApplicationElement(aar_application, temp_application, temp_rootElement);


            XMLWriter writer = new XMLWriter(new FileWriter(Paths.get(manifestFile).toFile()));
            writer.write(temp_rootElement);
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void diffApplicationElement(Element aar_application, Element temp_application, Element rootElement) {
        if (aar_application == null) {
            return;
        }
        if (temp_application == null) {
            rootElement.add(aar_application.detach());
            return;
        }
        // 合并 appilication 属性
        List<Attribute> aar_attributes = aar_application.attributes();
        List<Attribute> temp_attributes = temp_application.attributes();

        if (aar_attributes == null) {
            return;
        }
        // 合并application 属性
        for (Attribute sourceAttribute : aar_attributes) {
            String aar_name = sourceAttribute.getName().trim();
            boolean isExit = false;
            if (temp_attributes == null || temp_attributes.isEmpty()) {
                temp_application.addAttribute(sourceAttribute.getQName(), sourceAttribute.getValue());
                continue;
            }
            for (Attribute targetAttribute : temp_attributes) {
                String temp_value = targetAttribute.getName().trim();
                if (aar_name.equals(temp_value)) {
                    isExit = true;
                    break;
                }
            }
            if (!isExit) {
                temp_application.addAttribute(sourceAttribute.getQName(), sourceAttribute.getValue());
            }
        }

        List<Element> aar_activitys = aar_application.elements("activity");
        List<Element> temp_activitys = temp_application.elements("activity");
        // 合并activity
        diffElement(aar_activitys, temp_activitys, temp_application);

        List<Element> aar_activity_alias = aar_application.elements("activity-alias");
        List<Element> temp_activity_alias = aar_application.elements("activity-alias");
        // 合并activity-alias
        diffElement(aar_activity_alias, temp_activity_alias, temp_application);

        List<Element> aar_providers = aar_application.elements("provider");
        List<Element> temp_providers = aar_application.elements("provider");
        // 合并provider
        diffElement(aar_providers, temp_providers, temp_application);

        List<Element> aar_services = aar_application.elements("service");
        List<Element> temp_services = aar_application.elements("service");
        // 合并service
        diffElement(aar_services, temp_services, temp_application);

        List<Element> aar_receivers = aar_application.elements("receiver");
        List<Element> temp_receivers = aar_application.elements("receiver");
        // 合并receiver
        diffElement(aar_receivers, temp_receivers, temp_application);

        List<Element> aar_meta_datas = aar_application.elements("meta-data");
        List<Element> temp_meta_datas = aar_application.elements("meta-data");
        // 合并application节点下meta-data
        diffElement(aar_meta_datas, temp_meta_datas, temp_application);


    }

    private static void diffQueriesElement(Element aarQueries, Element tempQueries, Element tempRootElement) {
        if (aarQueries == null) {
            return;
        }

        // temp queries 不存在
        if (tempQueries == null) {
            tempRootElement.add(aarQueries.detach());
            return;
        }

        List<Element> aar_package = aarQueries.elements("package");
        List<Element> temp_package = tempQueries.elements("package");
        diffElement(aar_package, temp_package, tempQueries);

        List<Element> aar_intent = aarQueries.elements("intent");
        List<Element> temp_intent = tempQueries.elements("intent");


        if (aar_intent == null || aar_intent.isEmpty()) {
            return;
        }

        for (Element sourceElement : aar_intent) {

            String aar_value = sourceElement.element("action").attribute("name").getValue().trim();
            boolean isExit = false;
            if (temp_intent == null || temp_intent.isEmpty()) {
                tempQueries.add(sourceElement.detach());
                continue;
            }

            for (Element targetElement : temp_intent) {
                String temp_value = targetElement.element("action").attribute("name").getValue().trim();
                if (aar_value.equals(temp_value)) {
                    isExit = true;
                    break;
                }
            }
            if (!isExit) {
                tempQueries.add(sourceElement.detach());
            }
        }
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


    private static void handlerResInAar(Path resFile) throws IOException {

        if (!FileUtils.isExit(resDir)) {
            FileUtils.createDir(resDir);
        }

        final Path targetPath = Paths.get(resDir);


        Files.walkFileTree(resFile, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String resDirName = file.getParent().getFileName().toString();

                Path targetFile = targetPath.resolve(resFile.relativize(file));

                if (resDirName.startsWith("value")) {
                    // values 文件不存在  直接复制
                    if (!FileUtils.isExit(targetFile)) {
                        Files.move(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        // 合并res/values 文件
                        diffResValue(file, targetFile);
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
                    Path targetDir = targetPath.resolve(resFile.relativize(dir));
                    if (!FileUtils.isExit(targetDir)) {
                        Files.createDirectory(targetDir);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }


    // 合并values 文件
    public static void diffResValue(Path sourcePath, Path targetPath) {

        try {
            SAXReader source_reader = new SAXReader();
            Document source_document = source_reader.read(sourcePath.toFile());
            Element source_rootElement = source_document.getRootElement();
            List<Element> source_colors = source_rootElement.elements("color");
            List<Element> source_dimens = source_rootElement.elements("dimen");
            List<Element> source_strings = source_rootElement.elements("string");
            List<Element> source_styles = source_rootElement.elements("style");
            List<Element> source_declare_styleable = source_rootElement.elements("declare-styleable");
            List<Element> source_item = source_rootElement.elements("item");


            SAXReader target_reader = new SAXReader();
            Document target_document = target_reader.read(targetPath.toFile());
            Element target_rootElement = target_document.getRootElement();
            List<Element> target_colors = target_rootElement.elements("color");
            List<Element> target_dimens = target_rootElement.elements("dimen");
            List<Element> target_strings = target_rootElement.elements("string");
            List<Element> target_styles = target_rootElement.elements("style");
            List<Element> target_declare_styleable = target_rootElement.elements("declare-styleable");
            List<Element> target_item = target_rootElement.elements("item");


            //合并 color元素
            diffElement(source_colors, target_colors, target_rootElement);
            // 合并dimen元素
            diffElement(source_dimens, target_dimens, target_rootElement);
            // 合并string元素
            diffElement(source_strings, target_strings, target_rootElement);
            // 合并style元素
            diffElement(source_styles, target_styles, target_rootElement);

            // 合并item元素
            diffElement(source_item, target_item, target_rootElement);


            // 合并declare-styleable 元素
            if (source_declare_styleable != null) {
                for (Element sourceElement : source_declare_styleable) {

                    String source_value = sourceElement.attribute("name").getValue().trim();

                    boolean isExit = false;

                    if (target_declare_styleable == null || target_declare_styleable.isEmpty()) {
                        target_rootElement.add(sourceElement.detach());
                        continue;
                    }

                    for (Element targetElement : target_declare_styleable) {
                        String target_value = targetElement.attribute("name").getValue().trim();

                        if (source_value.equals(target_value)) {
                            isExit = true;
                            // 相同的节点  需要合并属性
                            diffValueDeclareStyle(sourceElement, targetElement, target_rootElement);
                            break;
                        }
                    }

                    if (!isExit) {
                        target_rootElement.add(sourceElement.detach());
                    }
                }
            }
            XMLWriter writer = new XMLWriter(new FileWriter(targetPath.toFile()));
            writer.write(target_rootElement);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void diffValueDeclareStyle(Element sourceElement, Element targetElement, Element target_rootElement) {

        List<Element> sourceAttrElement = sourceElement.elements("attr");

        List<Element> targetAttrElement = targetElement.elements("attr");

        if (sourceAttrElement == null || sourceAttrElement.isEmpty()) {
            return;
        }

        for (Element sourceAttr : sourceAttrElement) {

            String source_value = sourceAttr.attribute("name").getValue().trim();

            boolean isExit = false;

            if (targetAttrElement == null || targetAttrElement.isEmpty()) {
                targetElement.add(sourceAttr.detach());
                continue;
            }

            for (Element targetAttr : targetAttrElement) {

                String target_value = targetAttr.attribute("name").getValue().trim();

                if (source_value.equals(target_value)) {
                    isExit = true;
                    diffValueDeclareStyleAttr(sourceAttr, targetAttr);
                    break;
                }
            }

            if (!isExit) {
                targetElement.add(sourceAttr.detach());
            }
        }
    }

    public static void diffValueDeclareStyleAttr(Element sourceElement, Element targetElement) {

        List<Element> source_enum = sourceElement.elements("enum");
        List<Element> target_enum = targetElement.elements("enum");

        diffElement(source_enum, target_enum, targetElement);

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
                parentElement.add(sourceElement.detach());
            }
        }
    }


    public static void createLibsIfNoExit() {
        // 如果 不存在 新建lib目录
        if (!FileUtils.isExit(libPath)) {
            FileUtils.createDir(libPath);
        }
    }

}
