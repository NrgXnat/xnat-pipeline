/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.manager;

import java.util.Observable;

import org.nrg.pipeline.utils.Notification;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: EventManager.java,v 1.1 2009/09/02 20:28:20 mohanar Exp $
 @since Pipeline 1.0
 */

public class EventManager extends Observable {
    
    public static EventManager GetInstance() {
        if (self == null) self = new EventManager();
        return self;
    }
    
    
    
    public synchronized void notify(Notification notification) {
        this.
        setChanged();
        notifyObservers(notification);
    }
    
    
   /* public Notification getNotification() {
        return notification;
    }
    
    public void setNotification(Notification notify) {
        notification = notify;
    }*/
    
    
    private EventManager() {
    }
    
    
    //private Notification notification = null;
    static EventManager self = null;
}
