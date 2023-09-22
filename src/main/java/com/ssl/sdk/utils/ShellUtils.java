package com.ssl.sdk.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Date:2023/09/14 14:40
 * Author:songshuilin
 * Description: 执行shell 命令工具
 */
public class ShellUtils {


    public static boolean executeCommand(String cmd) {

        BufferedReader br = null;
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = br.readLine()) != null) {
                LogUtils.d(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
}
