import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.io.PrintStream;

public class IncludeForBackup{
    
    public static void main(String[] args){
        
        //Initializing sync folder
        File current = new File(System.getProperty("user.dir"));
        //Initializing destination folder
        String destination = args[0];
        File dest = new File(destination);
        dest = new File(dest, current.getName());
        dest.mkdirs();
        
        //Initializing excluded files and directories from list of files of the directory to sync and list of included files
        //xf and xd for cmd robocopy
        //exclude for shell rsync
        String xf = "", xd = "", exclude = "";
        boolean isIncluded;
        ArrayList<String> excludeFNames = new ArrayList<String>();
        for(File file: current.listFiles()){
            isIncluded=false;
            for(int i=1; i<args.length; i++){
                if(file.getName().equals(args[i])){
                    isIncluded=true;
                    break;
                }
            }
            if(!isIncluded){
                if(file.isFile()) xf+=" "+file.getAbsolutePath();
                if(file.isDirectory()) xd+=" "+file.getAbsolutePath();
                exclude+=" --exclude=\""+file.getAbsolutePath()+"\"";
                excludeFNames.add(file.getName());
            }
        }
        
        //deleting excluded files from destination (robocopy and rsync won't touch them, if they are excluded)
        for(File file: dest.listFiles()){
            for(String ename: excludeFNames)
                if(file.getName().equals(ename)){
                    System.out.println("Deleting from destination "+file.getAbsolutePath());
                    deleteDirectory(file);
                    break;
                }
        }
        
        //case Windows
        //writing synchronization script to bat-file and starting it
        try{
            if(isWindows()){
                String command = 
                    "set dst="+destination+"\n"+
                    "for %%* in (.) do set CurrDirName=%%~nx*\n"+
                    "robocopy . %dst%\\%CurrDirName% %* /MIR /V /XD" + xd + " /XF " + xf + "\n"+
                    "pause\n"+
                    "exit";
                writeBat(command);
                Runtime.getRuntime().exec("cmd /c start cmdexec.bat");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        
        //UNTESTED
        //case *unix
        //writing synchronization script to sh-file and starting it
        try{
            if(isUnix()){
                String command = 
                    "rsync -avh --delete "+exclude+" $(pwd) "+destination+"\n"+
                    "read -p \"Press [Enter] key to start backup...\"\n";
                writeSh(command);
                Runtime.getRuntime().exec("shellexec.sh");
            }
            
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private static String OS = System.getProperty("os.name").toLowerCase();
    
    public static boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    public static boolean isUnix() {
        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );
    }
    
    private static void writeBat(String command){
        try{
            PrintWriter writer = new PrintWriter("cmdexec.bat", "cp866");
            writer.println(command);
            writer.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private static void writeSh(String command){
        try{
            PrintWriter writer = new PrintWriter("shellexec.sh", "UTF-8");
            writer.println(command);
            writer.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private static boolean deleteDirectory(File directory) {
        if(directory.exists()){
            File[] files = directory.listFiles();
            if(null!=files){
                for(int i=0; i<files.length; i++) {
                    if(files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    }
                    else {
                        files[i].delete();
                    }
                }
            }
        }
        return(directory.delete());
    }
    
}