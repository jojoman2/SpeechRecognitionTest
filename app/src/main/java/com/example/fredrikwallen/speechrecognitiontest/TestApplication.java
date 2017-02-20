package com.example.fredrikwallen.speechrecognitiontest;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

//This code serves to copy the files needed by Snowboy to the file system where the Snowboy library can access them

public class TestApplication extends Application {

    private static final String[] ASSETS_TO_COPY = new String[]{"snowboy.umdl","common.res"};

    private static final String ASSETS_MOVED_PREF = "assetsMoved";

    @Override
    public void onCreate() {
        super.onCreate();

        //Checks if the assets need to be copied
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean assetsCopied = sharedPrefs.getBoolean(ASSETS_MOVED_PREF, false);
        if(!assetsCopied) {
            boolean copiedSuccessfully = copyAssets();
            if(copiedSuccessfully){
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putBoolean(ASSETS_MOVED_PREF,true);
                editor.apply();
            }
        }

    }


    //Copies the assets needed by snowboy to the file system
    private boolean copyAssets(){
        AssetManager assetManager = this.getAssets();

        boolean copiedSuccessfully = true;

        for(String assetToMove : ASSETS_TO_COPY){
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(assetToMove);
                File outFile = new File(getFilesDir().getAbsolutePath(), assetToMove);
                if(!outFile.exists()) {
                    out = new FileOutputStream(outFile);
                    copyFile(in, out);
                }
            } catch (IOException e) {
                e.printStackTrace();
                copiedSuccessfully = false;
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    }
                    catch (IOException ignored) {}
                }
                if (out != null) {
                    try {
                        out.close();
                    }
                    catch (IOException ignored) {}
                }
            }
        }
        return copiedSuccessfully;
    }


    //Copies an inputstream to an outputstream
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
}
