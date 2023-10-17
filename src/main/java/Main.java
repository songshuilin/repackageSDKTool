import com.ssl.sdk.ui.WinMain;
import com.ssl.sdk.utils.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static com.ssl.sdk.constants.Constants.APK_WORKSPACE_BUILD_CHANNEL_SDK_TEMP_DIR;


public class Main {
    public static void main(String[] args) throws IOException {
       //  new WinMain().creatWin();
   //     MergeUtils.createR();

        MergeUtils.compile_build_apk();

        MergeUtils.apk_sign();

        MergeUtils.apk_zipalign();

    }

}