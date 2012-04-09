/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.utils;

import java.util.ArrayList;

import org.nrg.pipeline.xmlbeans.ParameterData;
import org.nrg.pipeline.xmlbeans.ParametersDocument;
import org.nrg.pipeline.xmlbeans.ParameterData.Values;
import org.nrg.pipeline.xmlbeans.ParametersDocument.Parameters;

import com.Ostermiller.util.CSVParser;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: CommandLineArguments.java,v 1.1 2009/09/02 20:28:21 mohanar Exp $
 @since Pipeline 1.0
 */

public class CommandLineArguments {

    

    public CommandLineArguments(String argv[]) {
        int c;
        LongOpt[] longopts = new LongOpt[12];
        notificationEmailIds = new ArrayList();
        csvFile = null; parameterFile = null; paramDir = null; configFile = null;
        debug = false;
        params = ParametersDocument.Parameters.Factory.newInstance();
        supressNotification = false;
        // 
        longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
        longopts[1] = new LongOpt("pipeline", LongOpt.REQUIRED_ARGUMENT, null, 'f'); 
        longopts[2] = new LongOpt("parameterFile", LongOpt.REQUIRED_ARGUMENT, null, 'p');
        longopts[3] = new LongOpt("startAt", LongOpt.REQUIRED_ARGUMENT, null, 'b');
        longopts[4] = new LongOpt("notify", LongOpt.REQUIRED_ARGUMENT, null, 'e');
        longopts[5] = new LongOpt("csv", LongOpt.REQUIRED_ARGUMENT, null, 'c');
        longopts[6] = new LongOpt("dir", LongOpt.REQUIRED_ARGUMENT, null, 'd');
        longopts[7] = new LongOpt("debug", LongOpt.NO_ARGUMENT, null, 'g');
        longopts[8] = new LongOpt("log", LongOpt.REQUIRED_ARGUMENT, null, 'l');
        longopts[9] = new LongOpt("config", LongOpt.REQUIRED_ARGUMENT, null, 'o');
        longopts[10] = new LongOpt("supressNotification", LongOpt.NO_ARGUMENT, null, 's');
        longopts[11] = new LongOpt("parameter", LongOpt.REQUIRED_ARGUMENT, null, 'r');
        // 
        Getopt g = new Getopt("PipelineRunner", argv, "f:p:b:e:c:d:o:r:sgh;", longopts, true);
        g.setOpterr(false); // We'll do our own error handling
        //
        while ((c = g.getopt()) != -1) {
          switch (c)
            {
               case 'f':
                   pipelineFile = g.getOptarg();
                   break;
               case 'p':
                   parameterFile = g.getOptarg();
                   break;
               case 'b':
                   startAt = g.getOptarg();
                   break;
               case 'e':
                   notificationEmailIds.add(g.getOptarg());
                   break;
               case 'c':
                   csvFile = g.getOptarg();
                   break;
               case 'd':
                   paramDir = g.getOptarg();
                   break;
               case 'o':
                   configFile = g.getOptarg();
                   break;
               case 'r':
                   addParameter(g.getOptarg(), false);
                   break;
               case 'g':
                   debug = true;
                   break;
               case 's':
                   supressNotification = true;
                   break;
               case 'l':
                   logFile = g.getOptarg();
                   break;                   
               case 'h':
                 printUsage();
                 break;
               default:
                 printUsage();
                 break;
            }
        }
    }
    
    /**
     * @return Returns the supressNotification.
     */
    public boolean isSupressNotification() {
        return supressNotification;
    }

    /**
     * @return Returns the csvFile.
     */
    public String getCsvFile() {
        return csvFile;
    }

    /**
     * @return Returns the configFile.
     */
    public String getConfigFile() {
        return configFile;
    }

    /**
     * @return Returns the paramDir.
     */
    public String getParameterDir() {
        return paramDir;
    }


    /**
     * @return Returns the notificationEmailIds.
     */
    public ArrayList getNotificationEmailIds() {
        return notificationEmailIds;
    }

    public void printUsage() {
        String usage = "PipelineRunner  -pipeline <pipeline xml file> \n";
        usage += "Options:\n";
        usage += "\t -config: Configuration file [REQUIRED]\n";
        usage += "\t -log: Log Properties file\n";
        usage += "\t -parameterFile: Path to Parameter file\n";
        usage += "\t -parameter: FORMAT: <param name>=<comma separated values> \n";
        usage += "\t -startAt: Step to start pipeline at\n";
        usage += "\t -notify: Email Ids to which notifications are to be sent\n";
        usage += "\t -csv: A comma separated file with parameters for Batch data\n";
        usage += "\t -dir: Directory which contains the parameters for a pipeline for Batch processing\n";
        usage += "\t -supressNotification: (optional) Pipeline completion emails will be supressed\n";
        usage += "\t -debug: Doesnt execute statements\n";
        System.out.println(usage);
        System.exit(1);
    }   
    
    /**
     * @return Returns the parameterFile.
     */
    public String getParameterFile() {
        return parameterFile;
    }
    /**
     * @return Returns the pipelineFile.
     */
    public String getPipelineFile() {
        return pipelineFile;
    }
    /**
     * @return Returns the startAt.
     */
    public String getStartAt() {
        return startAt;
    }
    
    public ParametersDocument getParametersDocument() {
        ParametersDocument paramDoc = ParametersDocument.Factory.newInstance();
        paramDoc.setParameters(params);
        return paramDoc;
    }
    
    private void addParameter(String paramValuePair, boolean sensitive) {
        //expected to get <name>=<csv value>
        paramValuePair = paramValuePair.trim();
        if (!sensitive) System.out.println("Param Value Pair " + paramValuePair);
        String parts[] = paramValuePair.split("=");
        if (parts == null || parts.length < 2  ) {
            System.out.println("Invalid parameter found: " + paramValuePair);
            printUsage();
            System.exit(1);
        }
        String paramName = parts[0].trim();
        String paramValues = parts[1].trim();
        if (sensitive) System.out.println("Param Value Pair " + paramName + "=********");

        ParameterData pData = params.addNewParameter();
        pData.setName(paramName);
        Values values = pData.getValues();
        if (values == null) values = pData.addNewValues();
        String[][] str = CSVParser.parse(paramValues);
        if (str==null || str.length != 1) {
            System.out.println("Invalid parameter found: " + paramValuePair);
            System.out.println("NOTE: If a parameter includes a comma or a new line, the whole field must be surrounded with double quotes.");
            System.out.println("When the field is in quotes, any quote literals must be escaped by \" Backslash literals must be escaped by \\"); 
            System.out.println("Otherwise a backslash and the character following will be treated as the following character, IE. \"\n\" is equivalent to \"n\".");
            System.out.println("Text that comes after quotes that have been closed but come before the next comma will be ignored.");
            printUsage();
            System.exit(1);
        }
        if (str[0].length == 1) {
            values.setUnique(str[0][0]);
        }else {
            for (int i = 0; i < str[0].length; i++) {
                values.addList(str[0][i]);
            }
        }
   }
    
    public String getLogPropertiesFile() {
        return logFile;
    }
    
    public boolean debug() {
        return debug;
    }
   
    
    String parameterFile;
    String configFile;
    String logFile;
    Parameters params;
    String pipelineFile;
    String startAt;
    ArrayList notificationEmailIds;
    String csvFile;
    String paramDir;
    boolean debug;
    boolean supressNotification;

}
