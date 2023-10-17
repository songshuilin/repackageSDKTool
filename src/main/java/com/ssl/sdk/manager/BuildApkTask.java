package com.ssl.sdk.manager;

import com.ssl.sdk.utils.*;

import java.io.File;

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

        LogUtils.d("处理母包自定义资源 attrs.xml...");
        MergeUtils.handleDeclareStyleable();

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
        LogUtils.d("合并apk 与三方manifest资源...");
        MergeUtils.mergeManifest();

        LogUtils.d("处理合并后的androidManifest中的占位符...");
        MergeUtils.handlePlaceholdermergeManifest();

        LogUtils.d("处理合并后的自定义资源 attrs.xml...");
        MergeUtils.handleMergeDeclareStyleable();


        LogUtils.d("根据资源生成R文件...");
       MergeUtils.createR();

        LogUtils.d("处理libs下所有jar文件...");
        MergeUtils.jar_compile_dex_d8();

        LogUtils.d("处理dexs下所有dex文件...");
        MergeUtils.dex_compile_smali();

//        LogUtils.d("重新生成apk...");
//        MergeUtils.compile_build_apk();
//
//        LogUtils.d("重新签名apk...");
//        MergeUtils.apk_sign();
//
//        LogUtils.d("apk对齐优化...");
//        MergeUtils.apk_zipalign();

        LogUtils.d("合并完成...");


        for (String packgeName : HandlerChannelSDKResUtils.packageNames) {
            LogUtils.d("packgeName: " +packgeName);
        }
    }


    public static void decompileApk(String apkSourcePath) {
        ShellUtils.decompileApk(apkSourcePath);
    }

}
