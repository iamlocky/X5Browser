package com.lockyluo.x5corelibrary.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {
    //复制方法
    public static void copy(String src, String des) throws Exception {
        Log.d("copy", "copy: "+src+"\n"+des);
        //初始化文件复制
        File file1=new File(src);
        //把文件里面内容放进数组
        File[] fs=file1.listFiles();
        //初始化文件粘贴
        File file2=new File(des);
        //判断是否有这个文件有不管没有创建
        if(!file2.exists()){
            file2.mkdirs();
        }
        //遍历文件及文件夹
        for (File f : fs) {
            if(f.isFile()){
                //文件
                fileCopy(f.getPath(),des+"\\"+f.getName()); //调用文件拷贝的方法
            }else if(f.isDirectory()){
                //文件夹
                copy(f.getPath(),des+"\\"+f.getName());//继续调用复制方法      递归的地方,自己调用自己的方法,就可以复制文件夹的文件夹了
            }
        }

    }

    /**
     * 文件复制的具体方法
     */
    public static void fileCopy(String src, String des) throws Exception {
        //io流固定格式
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(src));
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(des));
        int i = -1;//记录获取长度
        byte[] bt = new byte[2014];//缓冲区
        while ((i = bis.read(bt))!=-1) {
            bos.write(bt, 0, i);
        }
        bis.close();
        bos.close();
        //关闭流
    }

    /**
     * 将asset文件写入缓存
     */
    public static boolean copyAssetAndWrite(Context context,String inputFileName,String outputFileName,String destinationPath){
        try {
            File cacheDir=new File(destinationPath);
            if (!cacheDir.exists()){
                cacheDir.mkdirs();
            }
            File outFile =new File(cacheDir,outputFileName);
            if (!outFile.exists()){
                boolean res=outFile.createNewFile();
                if (!res){
                    return false;
                }
            }else {
                outFile.delete();
            }
            InputStream is=context.getAssets().open(inputFileName);
            FileOutputStream fos = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int byteCount;
            while ((byteCount = is.read(buffer)) != -1) {
                fos.write(buffer, 0, byteCount);
            }
            fos.flush();
            is.close();
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

}
