/*
 * Copyright (c) 2020 Jeneral Samopal Company
 * Design and Programming by Alex Dovby
 */

package com.jsc.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FileUtils {
    public static String readAssetFile (Context context, String fileName) throws IOException {
        String str = "";
        try {
            AssetManager assetManager = context.getAssets();
            InputStreamReader istream = new InputStreamReader(assetManager.open(fileName));
            BufferedReader in = new BufferedReader(istream);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            try {
                while ((line = in.readLine()) != null){
                    stringBuilder.append(line);
                }
                str = stringBuilder.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            in.close();
        } catch (FileNotFoundException e) {
            // FileNotFoundExpeption
        } catch (IOException e) {
            // IOExeption
        }
        return str;
    }

    // =========================================================
    static void SaveFile(String filePath, String FileContent) {
        //Создание объекта файла.
        File file = Environment.getExternalStorageDirectory();
        File fhandle = new File(file.getAbsolutePath() + filePath);
        try {
            //Если нет директорий в пути, то они будут созданы:
            File parentFile = fhandle.getParentFile();
            if (parentFile != null) {
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
            }

            //Если файл существует, то он будет перезаписан:
            fhandle.createNewFile();
            FileOutputStream fOut = new FileOutputStream(fhandle);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.write(FileContent);
            myOutWriter.close();
            fOut.close();
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Path " + filePath + ", " + e.toString());
        }
    }

    // =========================================================
    static String readFile(Activity main, String filePath) {
        String state = Environment.getExternalStorageState();
        StringBuilder textBuilder;
        if (!(state.equals(Environment.MEDIA_MOUNTED))) {
            Toast.makeText(main, "msg_no_sd", Toast.LENGTH_LONG).show();
        } else {
            BufferedReader reader = null;
            try {
                File file = Environment.getExternalStorageDirectory();
                File textFile = new File(file.getAbsolutePath() + filePath);
                reader = new BufferedReader(new FileReader(textFile));
                textBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    textBuilder.append(line);
                    textBuilder.append("\n");
                }
                // System.out.println("trace | Path " + filePath + ", src : " + textBuilder);
                return textBuilder.toString();

            } catch (FileNotFoundException e) {
                // TODO: handle exception
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }
}
