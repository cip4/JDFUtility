
package org.cip4.JDFUtility;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.cip4.jdflib.util.StringUtil;

/**
 *
 * @author  rainer
 */
public class JDFServletUtil extends Object {


    final static public String cwd =System.getProperty("user.dir");
    final static public String baseDir =System.getProperty("catalina.base")+"/webapps/JDFUtility/";

    public static File getTmpFile(String dirName, String tmpName, String prefix, String extension)
    {
        if(tmpName==null)
            return null;
        // we are in bin, which is a sibling directory of JDFUtilitys
        File tmpDir=new File(baseDir+dirName);

        if(!tmpDir.isDirectory())
            tmpDir.mkdirs();

        char[] tmp=tmpName.toCharArray();
        for(int i=0;i<tmp.length;i++)
        {
            if((tmp[i]>127)||(tmp[i]<=32))
                tmp[i]='_';
        }
        tmpName=String.valueOf(tmp);
        tmpName=prefix+StringUtil.pathToName(tmpName);            
        tmpName=tmpName.substring(0,tmpName.lastIndexOf("."));

        File outFile;
        try
        {
            outFile = File.createTempFile(tmpName,extension,tmpDir);
        }
        catch (IOException e)
        {
            return null;
        }
        return outFile;
    }   
    
    public static File getTmpFile(String dirName, FileItem fileItem, String prefix, String extension)
    {
        if(fileItem==null)
            return null;
        String tmpName = fileItem.getName();        
        return getTmpFile(dirName, tmpName, prefix, extension);
    }	

    public static List getFileList(HttpServletRequest request) throws ServletException
    {
        //      Create a factory for disk-based file items
        FileItemFactory factory = new DiskFileItemFactory();

        //      Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);

        List fileItems = null;
        try
        {
            fileItems = upload.parseRequest(request);       
            upload.setSizeMax(1024*1024*20); // 20 MB   
        }
        catch (FileUploadException fue)
        {
            throw new ServletException("Could not parse multipart request.", fue);
        }
        return fileItems;
    }


    /**
     * cleanup previous junk that is older than an hour
     */
    public static void cleanup(String dirNam)
    {
        long modNow=System.currentTimeMillis();
        File tmpDir=new File(baseDir+dirNam);
        File[] tmpFiles=tmpDir.listFiles();
        for(int n=0;n<tmpFiles.length;n++){
            File oldFile=tmpFiles[n];
            if(modNow-3600000>oldFile.lastModified()){ // 3600 seconds timeout
                oldFile.delete();
                System.out.println("deleting "+oldFile.getName()+" "+String.valueOf(oldFile.lastModified())+" "+String.valueOf(modNow));                    
            }
        }
        Runtime.getRuntime().gc(); // clean up memory
    }

    public static boolean isWindows()
    {
        return System.getProperty("os.name").startsWith("Win");
    }


}
