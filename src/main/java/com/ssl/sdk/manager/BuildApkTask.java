package com.ssl.sdk.manager;

import com.ssl.sdk.utils.*;

import static com.ssl.sdk.constants.Constants.*;

/**
 * Date:2023/09/14 14:45
 * Author:songshuilin
 * Description:
 */
public class BuildApkTask {

    private BuildApkTask buildApkTask;


    /**
     * 开始打包
     */
    public static void buildApk() {

        // 处理渠道资源
        LogUtils.d("开始处理渠道sdk aar资源...");
        HandlerChannelSDKResUtils.handlerSDKRes();
        // 合并apk与三方assets资源
        LogUtils.d("合并apk与三方assets资源...");
        MergeUtils.mergeAssets();
        // 合并apk 与三方libs资源
        LogUtils.d("合并apk 与三方libs资源...");
        MergeUtils.mergelibs();
        // 合并apk 与三方libs资源
        LogUtils.d("合并apk 与三方res资源...");
        MergeUtils.mergelRes();

        LogUtils.d("合并完成...");
    }


    public static void decompileApk(String apkSourcePath) {

        FileUtils.deleteDir(APK_WORKSPACE_DIR);

        FileUtils.createDir(APK_WORKSPACE_DIR);

        String cmd = String.format("java -jar  %s/apktool.jar  d %s -o %s  -f", APK_TOOL_DIR, apkSourcePath, APK_WORKSPACE_BUILD_APK_TEMP_DIR);

        ShellUtils.executeCommand(cmd);

    }


}
