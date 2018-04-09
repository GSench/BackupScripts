import java.util.ArrayList;
import java.net.URI;
import java.nio.charset.Charset;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BackupGitRepos {
    
    public static void main(String[] args){
        
        ArrayList<Pair<File, String>> gitRepos = new ArrayList<Pair<File, String>>();
        File current = new File(System.getProperty("user.dir"));
        System.out.println("Current directory: "+current.getAbsolutePath());
        
        System.out.println("Searching for repositories' urls");
        searchRepos(current, gitRepos);
        
        System.out.println("---------------\nFond repositories' urls:\n---------------");
        for(Pair<File, String> pair: gitRepos)
            System.out.println(pair.getElement0().getAbsolutePath()+"  "+pair.getElement1()+"\n");
        
        System.out.println("---------------\nStarting repositories' backup\n---------------");
        downloadRepos(gitRepos);
    }
    
    private static void searchRepos(File dir, ArrayList<Pair<File, String>> repos){
        if(dir.isDirectory()){
            for(File file: dir.listFiles())
                searchRepos(file, repos);
        } else {
            if(!getFileExtension(dir).equals("url")) return;
            try {
                String s = readFile(dir.getAbsolutePath(), Charset.defaultCharset());
                String[] split = s.split("URL=");
                if(split.length==1) return;
                s=split[1].trim();
                if(getDomainName(s).equals("github.com")||getDomainName(s).equals("bitbucket.org"))
                    repos.add(Pair.createPair(dir, s));
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    
    private static void downloadRepos(ArrayList<Pair<File, String>> repos){
        for(Pair<File, String> repo: repos){
            File current = repo.getElement0().getParentFile();
            String gitRepo = repo.getElement1();
            if(gitRepo.endsWith("/")) gitRepo = gitRepo.substring(0, gitRepo.length()-1);
            gitRepo+=".git";
            System.out.println("Current folder: "+current.getAbsolutePath()+" Repository git link: "+gitRepo);
            runBackupProcess(current, gitRepo);
            System.out.println("Backup process completed\n---------------");
        }
    }
    
    private static void runBackupProcess(File dir, String url){
        try {
            String repoName = getLastUrlSegment(url);
            repoName = repoName.substring(0, repoName.indexOf("."));
            System.out.println("Repository name: "+repoName);
            
            System.out.println("Starting cloning process");
            String[] command = {"cmd.exe", "/c", "git", "clone", url, repoName};
            ProcessBuilder builder = new ProcessBuilder(command);
            builder = builder.directory(dir);
            builder.redirectErrorStream(true);
            Process p = builder.start();
            p.waitFor();
            
            System.out.println("Cloning process complete");
            
            File repoFolder = new File(dir, repoName);
            File zip = new File(dir, repoName+".zip");
            create(zip);
            System.out.println("Created repository folder: "+repoFolder.getAbsolutePath()+" exists: "+repoFolder.exists());
            System.out.println("Created repository archieve: "+zip.getAbsolutePath()+" exists: "+zip.exists());
            
            System.out.println("Starting zip process...");
            ArrayList<String> fileList = new ArrayList<String>();
            generateFileList(repoFolder, repoFolder.getAbsolutePath(), fileList);
            
            zipIt(zip.getAbsolutePath(), repoFolder.getAbsolutePath(), fileList);
            System.out.println("Zip process successfully complete, repository zip length: "+zip.length());
            
            deleteDirectory(repoFolder);
            System.out.println("Repository folder deleted: "+(!repoFolder.exists()));
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    
    private static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
    
    private static String getFileExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }
    
    public static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }
    
    private static String getLastUrlSegment(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String path = uri.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
    
    public static class Pair<K, V> {

        private final K element0;
        private final V element1;
    
            public static <K, V> Pair<K, V> createPair(K element0, V element1) {
            return new Pair<K, V>(element0, element1);
        }
    
        public Pair(K element0, V element1) {
            this.element0 = element0;
            this.element1 = element1;
        }
    
        public K getElement0() {
            return element0;
        }

        public V getElement1() {
            return element1;
        }

    }
    
    private static void zipIt(String zipFile, String SOURCE_FOLDER, ArrayList<String> fileList) {
        byte[] buffer = new byte[1024];
        String source = new File(SOURCE_FOLDER).getName();
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(fos);

            FileInputStream in = null;

            for (String file: fileList) {
                ZipEntry ze = new ZipEntry(source + File.separator + file);
                zos.putNextEntry(ze);
                try {
                    in = new FileInputStream(SOURCE_FOLDER + File.separator + file);
                    int len;
                    while ((len = in .read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                } finally {
                    in.close();
                }
            }

            zos.closeEntry();
            System.out.println("Folder successfully compressed");

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                zos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void generateFileList(File node, String SOURCE_FOLDER, ArrayList<String> fileList) {
        // add file only
        if (node.isFile()) {
            fileList.add(generateZipEntry(node.toString(), SOURCE_FOLDER));
        }

        if (node.isDirectory()) {
            String[] subNote = node.list();
            for (String filename: subNote) {
                generateFileList(new File(node, filename), SOURCE_FOLDER, fileList);
            }
        }
    }

    private static String generateZipEntry(String file, String SOURCE_FOLDER) {
        return file.substring(SOURCE_FOLDER.length() + 1, file.length());
    }
    
    public static boolean create(File newFile){
        File path = newFile.getParentFile();
        path.mkdirs();
        try {
            return newFile.createNewFile();
        } catch (IOException e) {
            System.out.println("IOException: Unable to create new File with path " + path);
            e.printStackTrace();
        }
        return false;
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