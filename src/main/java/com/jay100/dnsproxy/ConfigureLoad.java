package com.jay100.dnsproxy;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

/**
 *
 * @author music coder
 */
public class ConfigureLoad {
    private static Map<String,String> map;
    private static File currFile = new File("hosts");
    private static long lastModified = currFile.lastModified();

    public static boolean isPrint = false;

    private static Timer timer = new Timer() ;

    static {
        loadFile();
        listenFileChange();
    }

    static void loadFile(){
        map  = new HashMap<String, String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(currFile));
            System.out.println("load hosts ....");
            while (true) {
                String line = br.readLine();
                if(line==null) break;
                line = line.trim();
                if(!line.startsWith("#")){
                    String[] arr = line.split(" ");
                    if(arr.length>1)
                        map.put(arr[arr.length-1],arr[0]);
                }
            }
        }  catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    static public  Map<String,String> getAllHosts(){
        return map;
    }
    static public String getHostByDomain(String domain){
        if(Pattern.compile("\\.$").matcher(domain).find()) domain = domain.substring(0,domain.length()-1);
        if(map.containsKey(domain)) return map.get(domain);
        else {
            int last = domain.lastIndexOf(".");
            int prev = domain.lastIndexOf(".",last-1);
            if(prev>0){
                domain = "*"+domain.substring(prev,domain.length());
                if(map.containsKey(domain)) return map.get(domain);
            }
        }
        return null;
    }

    static public void listenFileChange(){
        System.out.println("开启文件监听...");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(lastModified!=currFile.lastModified()) {
                    lastModified=currFile.lastModified();
                    System.out.println("文件已修改，重新载入...");
                    loadFile();
                }
            }
        },0,1000); //文件修改后3秒钟
    }

}
