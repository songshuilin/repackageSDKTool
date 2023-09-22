import com.ssl.sdk.ui.WinMain;
import com.ssl.sdk.utils.FileUtils;
import com.ssl.sdk.utils.HandlerChannelSDKResUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static com.ssl.sdk.constants.Constants.APK_WORKSPACE_BUILD_CHANNEL_SDK_TEMP_DIR;


public class Main {
    public static void main(String[] args) throws IOException {
       //new WinMain().creatWin();
     // test();
     //   HandlerChannelSDKResUtils.handlerAarTemp();
       HandlerChannelSDKResUtils.handlerManifestInAar(Paths.get("D:\\code\\dabaotool\\repackageSDKTool\\WorkSpace\\BuildApk\\ChannelSdkTemp\\chinasdk\\aarTemp\\YoStarSdk_1.5.5_202308311602_release\\AndroidManifest.xml"));

    }

}