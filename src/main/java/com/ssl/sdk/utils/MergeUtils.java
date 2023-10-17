package com.ssl.sdk.utils;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ssl.sdk.constants.Constants.*;

/**
 * Date:2023/09/25 16:12
 * Author:songshuilin
 * Description: sdk资源与apk 合并
 */
public class MergeUtils {

    private static HashMap<String, HashSet<String>> declareHashMap = new HashMap<>();


    private String[] values_dir_names = new String[]{"colors.xml", "dimens.xml", "drawables.xml", "strings.xml", "styles.xml", "arrays.xml",
            "attrs.xml", "integers.xml", "bools.xml", "plurals.xml"};

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

        String libsTargetDir = APK_WORKSPACE_BUILD_APK_TEMP_DIR + File.separator + "lib";

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
                        //  要转成对应的文件  例string.xml 、 bool.xml 等  再合并
                        mergeResValue(file, targetFile);
                        return FileVisitResult.SKIP_SUBTREE;
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

    private static String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?><resources> </resources>";

    private static void mergeResValue(Path sourceFile, Path targetFile) {
        try {

            SAXReader source_reader = new SAXReader();
            Document source_document = source_reader.read(sourceFile.toFile());
            Element source_rootElement = source_document.getRootElement();

            List<Element> elements = source_rootElement.elements();

            // 获取所有属性节点  创建对应的文件
            for (Element e : elements) {
                String eleName = e.getName().trim();

                String fileName = null;

                if (eleName.equalsIgnoreCase("string-array") || eleName.equalsIgnoreCase("integer-array")) {
                    fileName = "arrays.xml";
                } else if (eleName.equalsIgnoreCase("declare-styleable")) {
                    fileName = "attrs.xml";
                } else if (eleName.equalsIgnoreCase("item")) {
                    String itemType = e.attributeValue("type");
                    if (itemType.equals("dimen")) {
                        fileName = "dimens.xml";
                    } else if (itemType.equals("id")) {
                        fileName = "ids.xml";
                    }
                } else {
                    fileName = eleName + "s.xml";
                }

                // 该文件不存在
                if (!FileUtils.isExit(sourceFile.getParent().resolve(fileName))) {
                    Files.writeString(sourceFile.getParent().resolve(fileName), xml);
                }
            }
            //  value对应的节点 写入转换后的相应的文件中
            Files.walkFileTree(sourceFile.getParent(), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                    try {
                        String fileName = file.getFileName().toString();

                        if (fileName.startsWith("value")) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }

                        SAXReader reader = new SAXReader();
                        Document document = reader.read(file.toFile());
                        Element rootElement = document.getRootElement();


                        // 写入对应文件
                        for (Element e : elements) {
                            String eleName = e.getName().trim();

                            String eleFileName = null;

                            if (eleName.equalsIgnoreCase("string-array") || eleName.equalsIgnoreCase("integer-array")) {
                                eleFileName = "arrays.xml";
                            } else if (eleName.equalsIgnoreCase("declare-styleable")) {
                                eleFileName = "attrs.xml";
                            } else if (eleName.equalsIgnoreCase("item")) {
                                String itemType = e.attributeValue("type");
                                if (itemType.equals("dimen")) {
                                    eleFileName = "dimens.xml";
                                } else if (itemType.equals("id")) {
                                    eleFileName = "ids.xml";
                                }
                            } else {
                                eleFileName = eleName + "s.xml";
                            }
                            // 文件名跟value节点名相同时， 就写入该节点
                            if (fileName.equals(eleFileName)) {
                                rootElement.add(e.detach());
                            }
                        }


                        XMLWriter writer = new XMLWriter(new FileWriter(file.toFile()), getFormat());
                        writer.write(rootElement);
                        writer.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            // 处理完 删除文件
            //  Files.deleteIfExists(sourceFile);


            // 写完之后  再跟apk中资源合并
            Files.walkFileTree(sourceFile.getParent(), new SimpleFileVisitor<>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                    String fileName = file.getFileName().toString();

                    if (fileName.startsWith("value")) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }

                    Path convertTargetFile = targetFile.getParent().resolve(fileName);
                    // 文件不存在直接复制
                    if (!FileUtils.isExit(convertTargetFile)) {
                        Files.move(file, convertTargetFile, StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        //  合并 渠道vlues转换后的文件  与apktool解压后的value目录下相关文件
                        mergeValue(file, convertTargetFile);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (dir.getParent() != null) {
                        Path targetDir = targetFile.getParent();
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

    private static void mergeValue(Path sourceFile, Path convertTargetFile) {
        try {
            SAXReader source_reader = new SAXReader();
            Document source_document = source_reader.read(sourceFile.toFile());
            Element source_rootElement = source_document.getRootElement();
            List<Element> source_elements = source_rootElement.elements();

            SAXReader convertTarget_reader = new SAXReader();
            Document convertTarget_document = convertTarget_reader.read(convertTargetFile.toFile());
            Element convertTarget_rootElement = convertTarget_document.getRootElement();
            List<Element> convertTarget_elements = convertTarget_rootElement.elements();

            diffElement(source_elements, convertTarget_elements, convertTarget_rootElement);

            XMLWriter writer = new XMLWriter(new FileWriter(convertTargetFile.toFile()), getFormat());
            writer.write(convertTarget_rootElement);
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 处理合并后的 自定义资源文件
    public static void handleMergeDeclareStyleable() {
        String attrsFile = APK_WORKSPACE_BUILD_APK_TEMP_DIR + File.separator + "res" + File.separator + "values" + File.separator + "attrs.xml";
        Path attrsPath = Paths.get(attrsFile);

        try {
            SAXReader reader = new SAXReader();
            Document document = reader.read(attrsPath.toFile());
            Element rootElement = document.getRootElement();

            List<Element> declareElements = rootElement.elements("declare-styleable");
            List<Element> attrElements = rootElement.elements("attr");


            for (Element declareElement : declareElements) {
                List<Element> declare_attrElements = declareElement.elements("attr");

                for (Element declare_attrElement : declare_attrElements) {
                    String declareAttrName = declare_attrElement.attributeValue("name");
                    boolean isExit = false;
                    for (Element attrElement : attrElements) {
                        String attrAttrName = attrElement.attributeValue("name");
                        if (declareAttrName.equals(attrAttrName)) {
                            isExit = true;
                        }

                        // 该declare-styleable 节点下的attr节点 在全局有的话， 要转换下，不然生成R会冲突
                        if (isExit) {

                            List<Element> declare_childAttrElements = declare_attrElement.elements();

                            List<Element> list = new ArrayList<>();

                            for (Element declare_childAttrElement : declare_childAttrElements) {
                                list.add(declare_childAttrElement);
                            }

                            for (Element element : list) {
                                declare_attrElement.remove(element);
                            }

                            List<Attribute> declare_attribute = declare_attrElement.attributes();

                            List<Attribute> declare_attribute_list = new ArrayList<>();


                            for (Attribute attribute : declare_attribute) {
                                declare_attribute_list.add(attribute);
                            }

                            for (Attribute a : declare_attribute_list) {
                                String attributeName = a.getName();
                                if (!attributeName.equals("name")) {
                                    declare_attrElement.remove(a);
                                }
                            }

                            break;

                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void handleDeclareStyleable() {

        String smailDir = APK_WORKSPACE_BUILD_APK_TEMP_DIR + File.separator + "smali";

        String attrsFile = APK_WORKSPACE_BUILD_APK_TEMP_DIR + File.separator + "res" + File.separator + "values" + File.separator + "attrs.xml";

        String publicFile = APK_WORKSPACE_BUILD_APK_TEMP_DIR + File.separator + "res" + File.separator + "values" + File.separator + "public.xml";


        Path attrsPath = Paths.get(attrsFile);
        Path publicPath = Paths.get(publicFile);


        try {

            SAXReader reader = new SAXReader();
            Document document = reader.read(attrsPath.toFile());
            Element rootElement = document.getRootElement();

            SAXReader publicReader = new SAXReader();
            Document publicDocument = publicReader.read(publicPath.toFile());
            Element publicRootElement = publicDocument.getRootElement();


            // 遍历smali目录
            Files.walkFileTree(Paths.get(smailDir), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String fileName = file.getFileName().toString();
                    if (fileName.equalsIgnoreCase("R$styleable.smali")) {
                        parseStyleableFile(file);
                    }
                    return super.visitFile(file, attrs);
                }
            });


            List<Element> publicElements = publicRootElement.elements();

            List<Element> attrElements = rootElement.elements("attr");


            for (String key : declareHashMap.keySet()) {

                List<Element> declareElements = rootElement.elements("declare-styleable");
                // 当前的 element
                Element curDeclareElement = null;

                boolean isExitDeclare = false;
                for (Element e : declareElements) {
                    String nameValue = e.attributeValue("name");
                    if (key.equals(nameValue)) {
                        isExitDeclare = true;
                        curDeclareElement = e;
                        break;
                    }
                }

                if (!isExitDeclare) {
                    // 不存在 新增自定义控件名
                    curDeclareElement = rootElement.addElement("declare-styleable");
                    curDeclareElement.addAttribute("name", key);
                }

                HashSet<String> hashSet = declareHashMap.get(key);

                for (String value : hashSet) {
                    for (Element publicElement : publicElements) {
                        String publicAttrId = publicElement.attributeValue("id");

                        if (value.equals(publicAttrId)) {
                            String publicAttrName = publicElement.attributeValue("name");

                            for (Element attrElement : attrElements) {
                                String attrName = attrElement.attributeValue("name");
                                if (publicAttrName.equals(attrName)) {
                                    boolean isExitAttr = false;
                                    List<Element> curAttrElements = curDeclareElement.elements("attr");
                                    for (Element e : curAttrElements) {
                                        String attrNameValue = e.attributeValue("name");
                                        if (attrName.equals(attrNameValue)) {
                                            isExitAttr = true;
                                            break;
                                        }
                                    }
                                    if (!isExitAttr) {
                                        Element copyElement = attrElement.createCopy();
                                        List<Element> childCopyElement = copyElement.elements();

                                        for (Element e : childCopyElement) {
                                            copyElement.remove(e); // 移除所有子节点
                                        }

                                        List<Attribute> attributes = copyElement.attributes();

                                        List<Attribute> tempAttrNameList = new ArrayList<>();

                                        tempAttrNameList.addAll(attributes);

                                        for (Attribute tempAttrName : tempAttrNameList) {
                                            if (!tempAttrName.getName().equals("name")) {
                                                copyElement.remove(tempAttrName);
                                            }
                                        }
                                        curDeclareElement.add(copyElement.detach());
                                    }
                                    break;
                                }
                            }
                            break;
                        }
                    }
                }
            }


            XMLWriter writer = new XMLWriter(new FileWriter(attrsPath.toFile()), getFormat());
            writer.write(rootElement);
            writer.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析每个R$styleable.smali文件
     *
     * @param styleableFile
     */
    private static void parseStyleableFile(Path styleableFile) {

        try {
            HashMap<String, String> hashMap = new HashMap();

            String allContent = Files.readString(styleableFile);

            List<String> allLines = Files.readAllLines(styleableFile);

            String arrays[] = allContent.split("new-array");

            for (String s : arrays) {
                if (s.contains("sput-object")) {
                    String widgetName = s.substring(s.indexOf("->") + 2, s.indexOf(":[I")).trim();
                    Pattern pattern = Pattern.compile("array_(.*)\\s");
                    Matcher matcher = pattern.matcher(s);

                    Pattern pattern_value = Pattern.compile("0x[A-Fa-f0-9]+\\s");
                    Matcher matcher_value = pattern_value.matcher(s);

                    String widgetValue = null;
                    String widgetArrayValue = null;

                    if (matcher.find()) {
                        widgetValue = matcher.group().trim();
                    } else {
                        if (matcher_value.find()) {
                            widgetArrayValue = matcher_value.group().trim();
                        }
                    }
                    if (widgetValue == null) {
                        if (declareHashMap.get(widgetName) != null) {
                            if (widgetArrayValue != null) {
                                declareHashMap.get(widgetName).add(widgetArrayValue);
                            }
                        } else {
                            HashSet<String> declareHashSet = new HashSet<>();
                            if (widgetArrayValue != null) {
                                declareHashSet.add(widgetArrayValue);
                                declareHashMap.put(widgetName, declareHashSet);
                            }
                        }
                    } else {
                        hashMap.put(widgetValue, widgetName);
                    }
                }
            }


            HashSet<String> hashSet = null;
            String widgetNameKey = null;

            for (String line : allLines) {

                line = line.trim();

                if (line.startsWith(":") && line.length() > 2) {
                    line = line.substring(line.indexOf(":") + 1).trim();
                }

                if (hashMap.containsKey(line)) {
                    widgetNameKey = hashMap.get(line);
                    if (declareHashMap.get(widgetNameKey) != null) {
                        hashSet = declareHashMap.get(widgetNameKey);
                    } else {
                        hashSet = new HashSet<>();
                        declareHashMap.put(widgetNameKey, hashSet);
                    }
                }

                if (line.matches("0x[A-Fa-f0-9]+")) {
                    hashSet.add(line);
                    declareHashMap.put(widgetNameKey, hashSet);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void createR() {
        ShellUtils.res_compile_r_java();
    }



    public static void jar_compile_dex_d8() {
        ShellUtils.jar_compile_dex_d8();
    }

    public static void dex_compile_smali() {
        ShellUtils.dex_compile_smali();
    }

    public static void compile_build_apk() {
        ShellUtils.compile_build_apk();
    }

    public static void apk_sign() {
        ShellUtils.apk_sign();
    }

    public static void apk_zipalign() {
        ShellUtils.apk_zipalign();
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
            String source_value = sourceElement.attribute("name").getValue().trim();
            boolean isExit = false;

            if (targetElementList == null || targetElementList.isEmpty()) {
                parentElement.add(sourceElement.detach());
                continue;
            }

            for (Element targetElement : targetElementList) {
                String target_value = targetElement.attribute("name").getValue().trim();
                if (source_value.equals(target_value)) {
                    isExit = true;
                    break;
                }
            }
            if (!isExit) {
                parentElement.add(sourceElement.detach());
            }
        }
    }


    public static void mergeManifest() {

        String resSourceDir = APK_WORKSPACE_BUILD_CHANNEL_SDK_TEMP_DIR + File.separator + "chinasdk" + File.separator + "AndroidManifest.xml";

        String resTargetDir = APK_WORKSPACE_BUILD_APK_TEMP_DIR + File.separator + "AndroidManifest.xml";


        try {

            SAXReader source_reader = new SAXReader();
            Document source_document = source_reader.read(Paths.get(resSourceDir).toFile());
            Element source_rootElement = source_document.getRootElement();


            Element source_application = source_rootElement.element("application");
            List<Element> source_uses_permission = source_rootElement.elements("uses-permission");
            List<Element> source_permission = source_rootElement.elements("permission");
            Element source_supports_screens = source_rootElement.element("supports-screens");
            Element source_queries = source_rootElement.element("queries");
            List<Element> source_meta_datas = source_rootElement.elements("meta-data");


            SAXReader target_reader = new SAXReader();
            Document target_document = target_reader.read(Paths.get(resTargetDir).toFile());
            Element target_rootElement = target_document.getRootElement();


            Element target_application = target_rootElement.element("application");
            List<Element> target_uses_permission = target_rootElement.elements("uses-permission");
            List<Element> target_permission = target_rootElement.elements("permission");
            Element target_supports_screens = target_rootElement.element("supports-screens");
            Element target_queries = target_rootElement.element("queries");
            List<Element> target_meta_datas = target_rootElement.elements("meta-data");

            // 合并根节点下meta-data
            diffElement(source_meta_datas, target_meta_datas, target_rootElement);

            // 合并 uses_permission
            diffElement(source_uses_permission, target_uses_permission, target_rootElement);

            // 合并 permission
            diffElement(source_permission, target_permission, target_rootElement);

            // 合并 queries
            diffQueriesElement(source_queries, target_queries, target_rootElement);


            // 合并application
            diffApplicationElement(source_application, target_application, target_rootElement);


            XMLWriter writer = new XMLWriter(new FileWriter(Paths.get(resTargetDir).toFile()), getFormat());
            writer.write(target_rootElement);
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void handlePlaceholdermergeManifest() {
        // 暂时替换这几个，后续可以统一管理
//        manifestPlaceholders.put("QQ_AAPID", "102019161")
//        manifestPlaceholders.put("JPUSH_PKGNAME", applicationId)
//        manifestPlaceholders.put("JPUSH_APPKEY", "")
//        manifestPlaceholders.put("JPUSH_CHANNEL", "")

        String androidManifest = APK_WORKSPACE_BUILD_APK_TEMP_DIR + File.separator + "AndroidManifest.xml";

        Path manifestPath = Paths.get(androidManifest);
        try {

            List<String> lines = Files.readAllLines(manifestPath);

            StringBuilder sb = new StringBuilder();


            for (String line : lines) {
                String replaceLine = line.replaceAll("\\$\\{applicationId}", "com.example.oaidtest2")
                        .replaceAll("\\$\\{JPUSH_PKGNAME}", "com.example.oaidtest2")
                        .replaceAll("\\$\\{JPUSH_APPKEY}", "")
                        .replaceAll("\\$\\{QQ_AAPID}", "102019161")
                        .replaceAll("\\$\\{JPUSH_CHANNEL}", "");
                sb.append(replaceLine + "\n");
            }

            Files.writeString(manifestPath, sb.toString(), StandardOpenOption.TRUNCATE_EXISTING);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static void diffQueriesElement(Element sourceQueries, Element TargetQueries, Element tempRootElement) {
        if (sourceQueries == null) {
            return;
        }

        // temp queries 不存在
        if (TargetQueries == null) {
            tempRootElement.add(sourceQueries.detach());
            return;
        }

        List<Element> source_package = sourceQueries.elements("package");
        List<Element> target_package = TargetQueries.elements("package");
        diffElement(source_package, target_package, TargetQueries);

        List<Element> source_intent = sourceQueries.elements("intent");
        List<Element> target_intent = TargetQueries.elements("intent");


        if (source_intent == null || source_intent.isEmpty()) {
            return;
        }

        for (Element sourceElement : source_intent) {

            String source_value = sourceElement.element("action").attribute("name").getValue().trim();
            boolean isExit = false;
            if (target_intent == null || target_intent.isEmpty()) {
                TargetQueries.add(sourceElement.detach());
                continue;
            }

            for (Element targetElement : target_intent) {
                String target_value = targetElement.element("action").attribute("name").getValue().trim();
                if (source_value.equals(target_value)) {
                    isExit = true;
                    break;
                }
            }
            if (!isExit) {
                TargetQueries.add(sourceElement.detach());
            }
        }
    }

    private static void diffApplicationElement(Element source_application, Element target_application, Element rootElement) {
        if (source_application == null) {
            return;
        }
        if (target_application == null) {
            rootElement.add(source_application.detach());
            return;
        }
        // 合并 appilication 属性
        List<Attribute> source_attributes = source_application.attributes();
        List<Attribute> target_attributes = target_application.attributes();

        if (source_attributes == null) {
            return;
        }
        // 合并application 属性
        for (Attribute sourceAttribute : source_attributes) {
            String source_name = sourceAttribute.getName().trim();
            boolean isExit = false;
            if (target_attributes == null || target_attributes.isEmpty()) {
                target_application.addAttribute(sourceAttribute.getQName(), sourceAttribute.getValue());
                continue;
            }
            for (Attribute targetAttribute : target_attributes) {
                String target_value = targetAttribute.getName().trim();
                if (source_name.equals(target_value)) {
                    isExit = true;
                    break;
                }
            }
            if (!isExit) {
                target_application.addAttribute(sourceAttribute.getQName(), sourceAttribute.getValue());
            }
        }

        List<Element> source_activitys = source_application.elements("activity");
        List<Element> target_activitys = target_application.elements("activity");
        // 合并activity
        diffElement(source_activitys, target_activitys, target_application);

        List<Element> source_activity_alias = source_application.elements("activity-alias");
        List<Element> target_activity_alias = source_application.elements("activity-alias");
        // 合并activity-alias
        diffElement(source_activity_alias, target_activity_alias, target_application);

        List<Element> source_providers = source_application.elements("provider");
        List<Element> target_providers = source_application.elements("provider");
        // 合并provider
        diffElement(source_providers, target_providers, target_application);

        List<Element> source_services = source_application.elements("service");
        List<Element> target_services = source_application.elements("service");
        // 合并service
        diffElement(source_services, target_services, target_application);

        List<Element> source_receivers = source_application.elements("receiver");
        List<Element> target_receivers = source_application.elements("receiver");
        // 合并receiver
        diffElement(source_receivers, target_receivers, target_application);

        List<Element> source_meta_datas = source_application.elements("meta-data");
        List<Element> target_meta_datas = source_application.elements("meta-data");
        // 合并application节点下meta-data
        diffElement(source_meta_datas, target_meta_datas, target_application);


    }

    public static OutputFormat getFormat() {
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setIndent(true);
        format.setNewlines(true);
        format.setNewLineAfterDeclaration(true);
        return format;
    }



}
