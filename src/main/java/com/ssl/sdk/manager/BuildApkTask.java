package com.ssl.sdk.manager;

import com.ssl.sdk.utils.FileUtils;
import com.ssl.sdk.utils.HandlerChannelSDKResUtils;
import com.ssl.sdk.utils.MergeResourceUtils;
import com.ssl.sdk.utils.ShellUtils;

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
        HandlerChannelSDKResUtils.handlerSDKRes();

        MergeResourceUtils.mergeResource();
    }


    public static void decompileApk(String apkSourcePath) {

        FileUtils.deleteDir(APK_WORKSPACE_DIR);

        FileUtils.createDir(APK_WORKSPACE_DIR);

        String cmd = String.format("java -jar  %s/apktool.jar  d %s -o %s  -f", APK_TOOL_DIR, apkSourcePath, APK_WORKSPACE_BUILD_APK_TEMP_DIR);

        ShellUtils.executeCommand(cmd);

    }


}
