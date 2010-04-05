package com.joshlong.jukebox2.services.workflow;


import org.jbpm.graph.exe.ProcessInstance;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * @author Josh Long
 *         <p/>
 *         this is designed to handle any and all workflow requirements
 */
public interface WorkflowService extends Serializable {

    static public final String BPMS_PROCESS_INSTANCE_WELL_KNOWN_PROCESS_ATTRIBUTE_SCHEDULED_DATE = "scheduledDate";

    Long lockNextTaskInstanceByActorAndCriteria(String actor, Map<String, Object> vars);

    Date createSchedulableDateFromDate(Date date);

    Collection<Long> getOpenTaskInstancesByProcessInstanceAndActorAndCriteria(
            long processInstanceId, String actor, Map<String, Object> criteria);

    ProcessInstance scheduleProcessInstance(String flowName, Map<String, Object> vars, Date when);

    void startScheduledProcessInstances(String pdName, Map<String, Object> vars, Date when);

    Collection<Long> getProcessInstances(String pdName, Map<String, Object> vars);

    Collection<Long> getScheduledProcessInstances(String pdName, Map<String, Object> vars, Date when);

    long getProcessInstanceIdForTaskInstance(final long taskInstanceId);

    void endProcessInstance(Long processInstanceId);

    Date decodeDateFromProcessAttribute(String date); // utility method

    String encodeDateForProcessAttribute(Date date); // utility method

    Collection<String> getActorsHavingOpenTaskInstances(String actors[]);

    int countOpenTaskInstancesForActors(String[] actors);

    Object getVariableByProcessInstance(long processInstanceId, String key);

    int countOpenTaskInstances();

//	@Deprecated
    //Object getTaskInstanceVariable(long taskInstanceId, String key);

    //@Deprecated
//	Object getVariableByProcessInstance(long processinstanceid, String key);

    void setProcessVariableFor(long processinstanceid, String name, Object val);

    void setProcessVariablesFor(long processInstanceId, Map<String, Object> vals);

    Long getNextTaskInstanceByActor(String actor);

    Object getVariable(long taskInstanceId, String key);

    // Collection<Long> getOpenProcessInstancesByProcessDefinitionAndCriteria( String pd,  Map <String,Object> vars) ;
    //  Collection<Long> getOpenProcessInstancesByCriteria(Map<String, Object> vars);
    //   Collection<Long> getOpenScheduledProcessInstances(Date when);

    Long getNextTaskInstanceByActorAndCriteria(String actor, Map<String, Object> vars);

    // public Map<String, Object> getProcessVariablesFor(long
    // processInstanceId);

    ProcessInstance createAndStartProcessInstance(String flowName, Map<String, Object> params);

    ProcessInstance createAndStartProcessInstance(String flowName);

    ProcessInstance createProcessInstance(String flowName, Map<String, Object> vars);

    ProcessInstance createProcessInstance(String flowName);

    void startProcessInstance(long processInstanceId);

    boolean lockTaskInstance(long taskInstanceId, String actor);

    boolean unlockTaskInstance(long taskInstanceId);

    /**
     * Returns the taskInstances that were completed
     */
    Collection<Long> completeAllTaskInstancesByActorAndCriteria(String actor, Map<String, Object> parms);

    void completeTaskInstance(long taskInstanceId);

    Collection<Long> getOpenTaskInstancesByActor(String actor);

    Collection<Long> getOpenTaskInstancesByActorAndCriteria(String actor, Map<String, Object> criteria);

    ProcessInstance getProcessInstanceById(Long processId);

    ProcessInstance createNewProcessInstance(final String defS);

    boolean isProcessInstanceScheduledFor(long processInstanceId, Date when);
}
