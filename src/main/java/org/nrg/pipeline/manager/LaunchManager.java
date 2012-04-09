/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.manager;

import java.util.Calendar;

import org.nrg.pipeline.utils.AdminUtils;
import org.nrg.pipeline.utils.Notification;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: LaunchManager.java,v 1.1 2009/09/02 20:28:20 mohanar Exp $
 @since Pipeline 1.0
 */

public class LaunchManager {

    private static Calendar launchTime;
    private static int totalSteps = -1;
    private static int currentStepIndex;
    private static String pathToPipelineDescriptor;
    
    public static Calendar getLaunchTime() {
        if (launchTime == null) launchTime = AdminUtils.getTimeLaunched();
        return launchTime;
    }
    
    public static void registerStep(int currentStepIdx) {
        currentStepIndex = currentStepIdx;
    }
    
    public static void setPathToPipelineDescriptor(String path) {
        pathToPipelineDescriptor = path;
    }
    
    public static Notification getNotification() {
        Notification notification = new Notification();
        notification.setPipelineTimeLaunched(LaunchManager.getLaunchTime());
        notification.setPathTopipelineDecsriptor(pathToPipelineDescriptor);
        notification.setTotalSteps(totalSteps);
        notification.setCurrentStepIndex(currentStepIndex);
        return notification;
    }
    
    
    public static void setTotalSteps(int i) {
        if (totalSteps == -1) totalSteps = i;
    }
    
    
    public static int getTotalSteps() {
        return totalSteps;
    }


}
