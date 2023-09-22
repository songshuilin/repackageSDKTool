package com.ssl.sdk.ui;

import com.ssl.sdk.manager.BuildApkTask;
import com.ssl.sdk.utils.FileUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

import static com.ssl.sdk.constants.Constants.*;


public class WinMain {
    private JFrame win;

    private JLabel jLabel;


    private JTextField edit_filePath;
    private JButton btn_choose;


    private JButton btn_rePackage;

    public void creatWin() {
        setUI();
        win = new JFrame("repackageSDkTool v1.0.0");
        win.setSize(1000, 600);
        win.setLocationRelativeTo(null);
        win.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        win.setResizable(false);
        win.setLayout(null);


        edit_filePath = new JTextField();
        edit_filePath.setText("请输入apk文件路径");
        edit_filePath.setBounds(232, 16, 256, 24);
        win.add(edit_filePath);


        btn_choose = new JButton();
        btn_choose.setBounds(500, 16, 100, 24);
        btn_choose.setText("文件选择");
        win.add(btn_choose);


        btn_rePackage = new JButton();
        btn_rePackage.setBounds(500, 50, 100, 24);
        btn_rePackage.setText("开始打包");
        win.add(btn_rePackage);


        btn_rePackage.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BuildApkTask.buildApk();
            }
        });


        btn_choose.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                FileNameExtensionFilter fileNameExtensionFilter = new FileNameExtensionFilter("安卓apk文件", "apk");
                fileChooser.addChoosableFileFilter(fileNameExtensionFilter);
                fileChooser.setFileFilter(fileNameExtensionFilter);
                int result = fileChooser.showOpenDialog(win.getContentPane());
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    if (file != null && file.getName().endsWith(".apk"))
                        edit_filePath.setText(file.getAbsolutePath());
                    // 选中apk 就开始解压apk
                    BuildApkTask.decompileApk(edit_filePath.getText().trim());
                }
            }
        });


        win.setVisible(true);
    }


    private void setUI() {
        String windows = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        try {
            UIManager.setLookAndFeel(windows);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}





