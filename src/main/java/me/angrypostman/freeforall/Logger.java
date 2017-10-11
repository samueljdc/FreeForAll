package me.angrypostman.freeforall;

import org.bukkit.plugin.PluginLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.LogRecord;

public class Logger extends PluginLogger{

    private FreeForAll context;
    private OutputStream outputStream;

    public Logger(FreeForAll context){
        super(context);
        this.context=context;
    }

    @Override
    public void log(LogRecord logRecord){
        super.log(logRecord);
        if(this.outputStream == null){
            File debugFile=new File(this.context.getDataFolder(), "debug.txt");
            if(debugFile.exists()){
                debugFile.delete();
            }
            this.outputStream=initOutputStream(new File(this.context.getDataFolder(), "debug.txt"));
        }
        try{
            this.outputStream.write((logRecord.getMessage()+System.lineSeparator()).getBytes());
        } catch(IOException ignored){}
    }

    public OutputStream getOutputStream(){
        return this.outputStream;
    }

    private OutputStream initOutputStream(File file){
        try{
            if(!file.exists()){
                file.createNewFile();
            }
            return new FileOutputStream(file);
        } catch(IOException ignored){
            return null;
        }
    }

}
