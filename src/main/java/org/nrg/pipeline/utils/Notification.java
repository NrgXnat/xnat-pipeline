/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.utils;

import java.util.Calendar;

//////////////////////////////////////////////////////////////////////////
//// ClassName
/**
 Class documentation.

 @author mohanar
 @version $Id: Notification.java,v 1.1 2009/09/02 20:28:22 mohanar Exp $
 @since Pipeline 1.0
 */

public class Notification {
    
    Calendar pipelineTimeLaunched;
    Calendar stepTimeLaunched;
    String currentStep;
    String nextStep;
    String message;
    String status;
    int currentStepIndex;
    int totalSteps;
    String command;
    String pathTopipelineDecsriptor;
    
    
    
    /**
     * @return Returns the percentageStepsCompleted.
     */
    public float getPercentageStepsCompleted() {
        return (((float)currentStepIndex)/totalSteps)*100;
         
    }

    /**
     * @return Returns the command.
     */
    public synchronized String getCommand() {
        return command;
    }
    /**
     * @param command The command to set.
     */
    public synchronized void setCommand(String command) {
        this.command = command;
    }
    /**
     * @return Returns the currentStepIndex.
     */
    public synchronized int getCurrentStepIndex() {
        return currentStepIndex;
    }
    /**
     * @param currentStepIndex The currentStepIndex to set.
     */
    public synchronized void setCurrentStepIndex(int currentStepIndex) {
        this.currentStepIndex = currentStepIndex;
    }
    /**
     * @return Returns the totalSteps.
     */
    public synchronized int getTotalSteps() {
        return totalSteps;
    }
    /**
     * @param totalSteps The totalSteps to set.
     */
    public synchronized void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
    }
    
    /**
     * @return Returns the stepTimeLaunched.
     */
    public synchronized Calendar getStepTimeLaunched() {
        return stepTimeLaunched;
    }
    /**
     * @param timeLaunched The stepTimeLaunched to set.
     */
    public synchronized void setStepTimeLaunched(Calendar timeLaunched) {
        this.stepTimeLaunched = timeLaunched;
    }

    /**
     * @return Returns the pipelinTimeLaunched.
     */
    public synchronized Calendar getPipelineTimeLaunched() {
        return pipelineTimeLaunched;
    }
    /**
     * @param pipelinTimeLaunched The pipelineTimeLaunched to set.
     */
    public synchronized void setPipelineTimeLaunched(Calendar timeLaunched) {
        this.pipelineTimeLaunched = timeLaunched;
    }

    /**
     * @return Returns the state.
     */
    public synchronized String getStatus() {
        return status;
    }
    /**
     * @param state The state to set.
     */
    public synchronized void setStatus(String state) {
        this.status = state;
    }
    /**
     * @return Returns the currentStep.
     */
    public synchronized String getCurrentStep() {
        return currentStep;
    }
    /**
     * @param currentStep The currentStep to set.
     */
    public synchronized void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
    }
    /**
     * @return Returns the message.
     */
    public synchronized String getMessage() {
        return message;
    }
    /**
     * @param message The message to set.
     */
    public synchronized void setMessage(String message) {
        this.message = message;
    }
    /**
     * @return Returns the nextStep.
     */
    public synchronized String getNextStep() {
        return nextStep;
    }
    /**
     * @param nextStep The nextStep to set.
     */
    public synchronized void setNextStep(String nextStep) {
        this.nextStep = nextStep;
    }
    /**
     * @return Returns the pathTopipelineDecsriptor.
     */
    public synchronized String getPathTopipelineDecsriptor() {
        return pathTopipelineDecsriptor;
    }
    /**
     * @param pathTopipelineDecsriptor The pathTopipelineDecsriptor to set.
     */
    public synchronized void setPathTopipelineDecsriptor(String pathTopipelineDecsriptor) {
        this.pathTopipelineDecsriptor = pathTopipelineDecsriptor;
    }
    
    public synchronized String toString() {
        String rtn ="";
        rtn += " Pipeline Launch Time: " + AdminUtils.formatTimeLaunched(this.getPipelineTimeLaunched()) + "\n";
        if (getCurrentStep() != null) rtn += " Current Step Id : " + this.getCurrentStep() + "\n";
        rtn += " Step Launch Time: " + AdminUtils.formatTimeLaunched(this.getStepTimeLaunched()) + "\n";
        rtn += " Step Status: " + this.getStatus() + "\n";
        if (getCommand() != null)rtn += " Command Launched: " + this.getCommand() + "\n";
        if (getNextStep() != null)rtn += " Next Step: " + this.getNextStep() + "\n";
        if (getMessage()!=null)rtn += " Message: " + this.getMessage() + "\n";
        rtn += " Percentage Complete: " + this.getPercentageStepsCompleted() + "%\n";
        rtn +=" Path to Pipeline Descriptor: " + this.getPathTopipelineDecsriptor()+ "\n";
        return rtn;
    }
    
}
