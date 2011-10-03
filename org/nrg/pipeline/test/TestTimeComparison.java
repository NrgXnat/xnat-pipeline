/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.test;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id$
 @since Pipeline 1.0
 */

public class TestTimeComparison {
    private static boolean earlierInTime(String time1, String time2) {
        boolean rtn = false;
        String t1[] = time1.split(":");
        String t2[] = time2.split(":");
        if (t1 != null && t2 != null ) {
            int h1 = Integer.parseInt(t1[0]);
            int m1 = Integer.parseInt(t1[1]);
            int s1 = Integer.parseInt(t1[2]);
            int h2 = Integer.parseInt(t2[0]);
            int m2 = Integer.parseInt(t2[1]);
            int s2 = Integer.parseInt(t2[2]);
            if (h1 < h2 ) {return true;}
            if (h1 > h2) {return false;}
            if (m1 < m2) {return true;}
            if (m1 > m2) {return false;}
            if (s1 < s2) {return true;}
            if (s1 > s2) {return false;}
            return true;
        }
        return rtn;
    }

    public static void main(String args[]) {
        System.out.println(TestTimeComparison.earlierInTime("11:23:1","11:23:10"));
    }
    
}
