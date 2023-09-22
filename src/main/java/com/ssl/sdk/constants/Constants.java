package com.ssl.sdk.constants;

import java.io.File;

/**
 * Date:2023/09/14 15:37
 * Author:songshuilin
 * Description:
 */
public class Constants {

    // 项目根目录
    public static final String APK_PROJECT_DIR = System.getProperty("user.dir");

    // apk工具目录
    public static final String APK_TOOL_DIR = APK_PROJECT_DIR + File.separator + "tools";

    //打包工作目录
    public static final String APK_WORKSPACE_DIR = APK_PROJECT_DIR + File.separator + "WorkSpace";

    public static final String APK_WORKSPACE_BUILD_DIR = APK_WORKSPACE_DIR + File.separator + "BuildApk";


    public static final String APK_WORKSPACE_BUILD_APK_TEMP_DIR = APK_WORKSPACE_BUILD_DIR + File.separator + "apkTemp";

    public static final String APK_WORKSPACE_BUILD_CHANNEL_SDK_TEMP_DIR = APK_WORKSPACE_BUILD_DIR + File.separator + "ChannelSdkTemp";


    // 渠道资源目录
    public static final String CHANNEL_SDK_DIR = APK_PROJECT_DIR + File.separator + "SdkRes";


}
