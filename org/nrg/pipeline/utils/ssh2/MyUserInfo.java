/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.utils.ssh2;

import org.nrg.pipeline.xmlbeans.ResolvedStepDocument.ResolvedStep.ResolvedResource;

import com.jcraft.jsch.UserInfo;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: MyUserInfo.java,v 1.1 2009/09/02 20:28:22 mohanar Exp $
 @since Pipeline 1.0
 */

public class MyUserInfo implements UserInfo {
    
    
    public MyUserInfo(String pwd) {
        this.pwd = pwd;
    }
    
    
    public String getPassphrase() {
      return    null;
    }
    
    public String getPassword() {
        return pwd;
    }
    
    public boolean promptPassword(String message) {
        
        return true;
    }
    
    public boolean promptPassphrase(String message) {
        return false;
    }
    
    public boolean promptYesNo(String message) {
        return false;
    }
    
    public void showMessage(String message) {
        
    }
    
    String pwd;
}
