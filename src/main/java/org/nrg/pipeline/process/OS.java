/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.process;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: OS.java,v 1.1 2009/09/02 20:28:20 mohanar Exp $
 @since Pipeline 1.0
 */

public class OS {

    String platform;
    String platform_version;
    String machine;
    String fileSeparatorChar ="/";
    boolean isUnix;
    
    /**
     * @return Returns the fileSeparatorChar.
     */
    public String getFileSeparatorChar() {
        return fileSeparatorChar;
    }
    /**
     * @param fileSeparatorChar The fileSeparatorChar to set.
     */
    public void setFileSeparatorChar(String fileSeparatorChar) {
        this.fileSeparatorChar = fileSeparatorChar;
    }
    /**
     * @return Returns the machine.
     */
    public String getMachine() {
        return machine;
    }
    /**
     * @param machine The machine to set.
     */
    public void setMachine(String machine) {
        this.machine = machine;
    }
    /**
     * @return Returns the platform.
     */
    public String getPlatform() {
        return platform;
    }
    /**
     * @param platform The platform to set.
     */
    public void setPlatform(String platform) {
        this.platform = platform;
        if (platform.toUpperCase().startsWith("SUNOS") || platform.toUpperCase().startsWith("LINUX") || platform.toUpperCase().startsWith("UNIX") || platform.toUpperCase().startsWith("MACOS")) {
            this.setFileSeparatorChar("/");
            isUnix= true;
        }else if (platform.toUpperCase().startsWith("WINDOWS")) {
            this.setFileSeparatorChar("\\");
            isUnix = false;
        }
    }
    /**
     * @return Returns the version.
     */
    public String getVersion() {
        return platform_version;
    }
    /**
     * @param version The version to set.
     */
    public void setVersion(String version) {
        platform_version = version;
    }
    
    public String toString() {
        String rtn ="";
        rtn +=" Platform: " + this.getPlatform();
        rtn +=" Platform Version: " + this.getVersion();
        rtn +=" Machine: " + this.getMachine();
        rtn += " File separator: " + this.getFileSeparatorChar();
        rtn += " Is Unix: " + isUnix +"\n";
        return rtn;
    }

    
}
