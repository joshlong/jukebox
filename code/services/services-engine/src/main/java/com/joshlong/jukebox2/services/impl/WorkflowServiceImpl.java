package com.joshlong.jukebox2.services.impl;

import com.joshlong.jukebox2.services.impl.util.BaseService;
import com.joshlong.jukebox2.services.workflow.WorkflowService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.jbpm.JbpmContext;
import org.jbpm.JbpmException;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.graph.exe.ProcessInstance;
import org.jbpm.graph.exe.Token;
import org.jbpm.taskmgmt.exe.TaskInstance;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springmodules.workflow.jbpm31.JbpmCallback;
import org.springmodules.workflow.jbpm31.JbpmTemplate;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;


/**
 * Integration point with our workflow engine -- should be able to query worklists, kick off new * process instances,
 * all from here *
 *
 * @author Josh Long
 */
public class WorkflowServiceImpl extends BaseService implements WorkflowService, InitializingBean {
    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private HibernateTemplate hibernateTemplate;
    @Autowired
    private JbpmTemplate jbpmTemplate;
    private Destination bpmsStartedPingDestination;

    public Collection<Long> completeAllTaskInstancesByActorAndCriteria(String actor, Map<String, Object> parms) {
        Collection<Long> taskInstanceIds = new HashSet<Long>();
        Long taskInstanceId = null;

        while ((taskInstanceId = getNextTaskInstanceByActorAndCriteria(actor, parms)) != null) {
            lockTaskInstance(taskInstanceId, actor);
            completeTaskInstance(taskInstanceId);
            taskInstanceIds.add(taskInstanceId);
        }

        return taskInstanceIds;
    }

    private Collection<Long> _processInstances(final String processDefinitionName, final Map<String, Object> criteria, final boolean started, final boolean stopped, final boolean suspended) {
        List ids = hibernateTemplate.executeFind(new HibernateCallback<List<Long>>() {
                    public List<Long> doInHibernate(Session session)
                        throws HibernateException, SQLException {
                        List<Long> ids = new ArrayList<Long>();
                        boolean predicateOnPdName = !StringUtils.isEmpty(processDefinitionName);
                        StringBuffer stringBuffer = new StringBuffer("select pi.id from  org.jbpm.graph.exe.ProcessInstance as pi where ");
                        Collection<String> predicates = new ArrayList<String>();

                        if (predicateOnPdName) {
                            predicates.add(" pi.processDefinition.name = :pdName ");
                        }

                        predicates.add(String.format("pi.start is %s null", started ? "not" : StringUtils.EMPTY));
                        predicates.add(String.format("pi.end is %s null", stopped ? "not" : StringUtils.EMPTY));
                        predicates.add(String.format("pi.isSuspended = %s", suspended ? "TRUE" : "FALSE"));

                        stringBuffer.append(StringUtils.join(predicates.iterator(), " AND "));

                        Map<String, Object> namedParamsAndVals = new HashMap<String, Object>();

                        String hql = stringBuffer.toString();

                        if ((criteria != null) && (criteria.keySet().size() > 0)) {
                            hql += " and ";

                            String q1 = " ( pi.rootToken.id IN ( select li.token.id from LongInstance li WHERE li.value = %s AND li.name =  %s ))  ";
                            String q2 = "  ( pi.rootToken.id IN ( select si.token.id from StringInstance si WHERE si.value = %s   AND si.name =  %s )) ";

                            Collection<String> crits = new ArrayList<String>();
                            int ctr = 0;

                            for (String key : criteria.keySet()) {
                                Object val = criteria.get(key);
                                String nameParam = "name" + ctr;
                                String valParam = "value" + ctr;
                                namedParamsAndVals.put(nameParam, key);
                                namedParamsAndVals.put(valParam, val);

                                String whichQuery = null;

                                if (val instanceof Number) {
                                    whichQuery = q1;
                                }

                                if (val instanceof String) {
                                    whichQuery = q2;
                                }

                                crits.add(String.format(whichQuery, ":" + valParam, ":" + nameParam));

                                ctr += 1;
                            }

                            hql += StringUtils.join(crits.iterator(), " AND ");
                        }

                        ArrayList<String> keys = new ArrayList<String>();
                        ArrayList<Object> vals = new ArrayList<Object>();

                        for (String key : namedParamsAndVals.keySet()) {
                            Object val = namedParamsAndVals.get(key);

                            if (val instanceof Number && !(val instanceof Double) && !(val instanceof Float)) {
                                vals.add(((Number) val).longValue());
                            } else if (val instanceof Double || val instanceof Float) {
                                vals.add(((Number) val).doubleValue());
                            } else {
                                vals.add(val);
                            }

                            keys.add(key);
                        }

                        if (predicateOnPdName) {
                            keys.add("pdName");
                            vals.add(processDefinitionName);
                        }

                        String[] paramNames = keys.toArray(new String[keys.size()]);
                        Object[] paramVals = vals.toArray();

                        ids = hibernateTemplate.findByNamedParam(hql, paramNames, paramVals);

                        return ids;
                    }
                });

        return ids; //To change body of implemented methods use File | Settings | File Templates.
    }

    public Collection<Long> getOpenTaskInstancesByProcessInstanceAndActorAndCriteria(final long processInstanceId, final String actor, final Map<String, Object> c1) {
        final Map<String, Object> criteria = new HashMap<String, Object>();

        if (c1 != null) {
            criteria.putAll(c1);
        } // this way, its safe to assume the variable will never be null

        List tis = hibernateTemplate.executeFind(new HibernateCallback() {
                    public Object doInHibernate(Session session)
                        throws HibernateException, SQLException {
                        Collection<Long> tasks = new ArrayList<Long>();

                        //                TaskInstance taskInstance ;taskInstance.getToken().getProcessInstance().getId()
                        String hql = "SELECT ti.id FROM TaskInstance ti WHERE ti.start IS NULL and ti.actorId=  :actor and ti.isOpen = true and ti.isSuspended = false   ";

                        Map<String, Object> namedParamsAndVals = new HashMap<String, Object>();
                        namedParamsAndVals.put("actor", actor);

                        if (processInstanceId != -1) {
                            hql += " and ti.token.processInstance.id= :pid ";
                            namedParamsAndVals.put("pid", processInstanceId);
                        }

                        if ((criteria != null) && (criteria.keySet().size() > 0)) {
                            hql += " and ";

                            String q1 = "  ti.token.id IN ( select li.token.id from LongInstance li WHERE li.value = %s AND li.name =  %s )  ";
                            String q2 = " ti.token.id IN ( select si.token.id from StringInstance si WHERE si.value = '%s' AND si.name =  %s ) ";
                            Collection<String> crits = new ArrayList<String>();
                            int ctr = 0;

                            for (String key : criteria.keySet()) {
                                Object val = criteria.get(key);
                                String nameParam = "name" + ctr;
                                String valParam = "value" + ctr;
                                namedParamsAndVals.put(nameParam, key);
                                namedParamsAndVals.put(valParam, val);

                                String whichQuery = null;

                                if (val instanceof Number) {
                                    whichQuery = q1;
                                }

                                if (val instanceof String) {
                                    whichQuery = q2;
                                }

                                crits.add(String.format(whichQuery, ":" + valParam, ":" + nameParam));
                                ctr += 1;
                            }

                            hql += StringUtils.join(crits.iterator(), " AND ");
                        }

                        ArrayList<String> keys = new ArrayList<String>();
                        ArrayList<Object> vals = new ArrayList<Object>();

                        for (String key : namedParamsAndVals.keySet()) {
                            Object val = namedParamsAndVals.get(key);

                            if (val instanceof Number && !(val instanceof Double) && !(val instanceof Float)) {
                                vals.add(((Number) val).longValue());
                            } else if (val instanceof Double || val instanceof Float) {
                                vals.add(((Number) val).doubleValue());
                            } else {
                                vals.add(val);
                            }

                            keys.add(key);
                        }

                        String[] paramNames = keys.toArray(new String[keys.size()]);
                        Object[] paramVals = vals.toArray();

                        tasks = hibernateTemplate.findByNamedParam(hql, paramNames, paramVals);

                        return tasks;
                    }
                });
        return tis;
    }

    public Collection<Long> getOpenTaskInstancesByActorAndCriteria(final String actor, final Map<String, Object> criteria) {
        return this.getOpenTaskInstancesByProcessInstanceAndActorAndCriteria(-1, actor, criteria);
    }

    public Date decodeDateFromProcessAttribute(String date) {
        int style = DateFormat.LONG;
        Locale us = Locale.US;
        DateFormat df = DateFormat.getDateTimeInstance(style, style, us);
        Date d = null;

        try {
            d = df.parse(date);
        } catch (ParseException e) {
            // ... nothing we can do
        }

        return d;
    }

    public String encodeDateForProcessAttribute(Date date) {
        int style = DateFormat.LONG;
        Locale us = Locale.US;
        DateFormat df = DateFormat.getDateTimeInstance(style, style, us);

        return df.format(date);
    }

    public static long[] deserializeIdList(String stringReprOfIdList) {
        if (!StringUtils.isEmpty(stringReprOfIdList)) {
            String[] parts = stringReprOfIdList.split(",");
            long[] arrayOfLongs = new long[parts.length];

            for (int i = 0; i < parts.length; i++) {
                arrayOfLongs[i] = Long.parseLong(parts[i]);
            }

            return arrayOfLongs;
        }

        return new long[] {  };
    }

    public static String serializeIdList(Long... ids) {
        return StringUtils.join(ids, ",");
    }

    public long getProcessInstanceIdForTaskInstance(final long taskInstanceId) {
        return (Long) jbpmTemplate.execute(new JbpmCallback() {
                public Object doInJbpm(JbpmContext jbpmContext)
                    throws JbpmException {
                    TaskInstance ti = getTaskInstance(taskInstanceId);
                    ProcessInstance pi = ti.getToken().getProcessInstance();

                    return pi.getId();
                }
            });
    }

    public Collection<String> getActorsHavingOpenTaskInstances(String[] actors) {
        Collection<String> answer = (Collection<String>) hibernateTemplate.findByNamedParam(
            " SELECT ti.actorId FROM TaskInstance ti WHERE ti.actorId IN( :tids )  and ti.isOpen = true and ti.start IS NULL and ti.isSuspended = false GROUP BY ti.actorId",
                "tids", actors);

        return answer;
    }

    public Destination getBpmsStartedPingDestination() {
        return bpmsStartedPingDestination;
    }

    @Required
    public void setBpmsStartedPingDestination(Destination bpmsStartedPingDestination) {
        this.bpmsStartedPingDestination = bpmsStartedPingDestination;
    }

    public void startProcessInstance(final long processInstanceId) {
        jbpmTemplate.execute(new JbpmCallback() {
                public Object doInJbpm(JbpmContext jbpmContext)
                    throws JbpmException {
                    ProcessInstance processInstance = jbpmContext.getProcessInstance(processInstanceId);

                    if (!processInstance.isSuspended() && (processInstance.getStart() != null)) {
                        return null; // no need
                    }

                    processInstance.resume();
                    jbpmContext.save(processInstance);
                    processInstance.signal();
                    jbpmContext.save(processInstance);

                    jmsTemplate.send(bpmsStartedPingDestination,
                        new MessageCreator() {
                            public Message createMessage(javax.jms.Session session)
                                throws JMSException {
                                MapMessage msg = session.createMapMessage();
                                msg.setBoolean("started", true);
                                msg.setLong("processInstanceId", processInstanceId);

                                return msg;
                            }
                        });

                    return null;
                }
            });
    }

    public Date createSchedulableDateFromDate(Date date) {
        int hourOfDay = Calendar.HOUR_OF_DAY;
        Date when = DateUtils.truncate(date, hourOfDay);

        Date nextHour = DateUtils.add(when, hourOfDay, 1);

        if (DateUtils.round(date, hourOfDay).equals(nextHour)) {
            when = DateUtils.add(when, Calendar.MINUTE, 30);
        } // thus, this will preserve every half hour

        return when;
    }

    public ProcessInstance scheduleProcessInstance(String flowName, Map<String, Object> vars, Date when) {
        Map<String, Object> vars2 = new HashMap<String, Object>();
        vars2.put(WorkflowService.BPMS_PROCESS_INSTANCE_WELL_KNOWN_PROCESS_ATTRIBUTE_SCHEDULED_DATE, encodeDateForProcessAttribute(createSchedulableDateFromDate(when)));
        vars2.putAll(vars);

        return this.createProcessInstance(flowName, vars2);
    }

    public Collection<Long> getProcessInstances(String pdName, Map<String, Object> vars) {
        Map<String, Object> vars2 = new HashMap<String, Object>();

        if (vars != null) {
            vars2.putAll(vars);
        }

        return _processInstances(pdName, vars2, true, false, false);
    }

    public void endProcessInstance(final Long processInstanceId) {
        jbpmTemplate.execute(new JbpmCallback() {
                public Object doInJbpm(JbpmContext jbpmContext)
                    throws JbpmException {
                    ProcessInstance processInstance = jbpmContext.getProcessInstance(processInstanceId);
                    processInstance.end();
                    jbpmTemplate.saveProcessInstance(processInstance);

                    return processInstance;
                }
            });
    }

    public void startScheduledProcessInstances(String pdName, Map<String, Object> vars, Date when) {
        Collection<Long> processInstances = getScheduledProcessInstances(pdName, vars, when);

        for (Long pi : processInstances) {
            String dateAsString = encodeDateForProcessAttribute(createSchedulableDateFromDate(when));

            startProcessInstance(pi);
        }
    }

    public Collection<Long> getScheduledProcessInstances(String pdName, Map<String, Object> vars, Date when) {
        Map<String, Object> vars2 = new HashMap<String, Object>();

        if (when != null) {
            String dateAsString = encodeDateForProcessAttribute(createSchedulableDateFromDate(when));
            vars2.put(WorkflowService.BPMS_PROCESS_INSTANCE_WELL_KNOWN_PROCESS_ATTRIBUTE_SCHEDULED_DATE, dateAsString);
        }

        if (vars != null) {
            vars2.putAll(vars);
        }

        return _processInstances(pdName, vars2, true, false, true);
    }

    public ProcessInstance createProcessInstance(final String flowName, final Map<String, Object> vars) {
        return (ProcessInstance) jbpmTemplate.execute(new JbpmCallback() {
                public Object doInJbpm(JbpmContext jbpmContext)
                    throws JbpmException {
                    ProcessInstance processInstance = createNewProcessInstance(flowName);
                    jbpmContext.save(processInstance);

                    ContextInstance contextInstance = (ContextInstance) processInstance.getInstance(ContextInstance.class);

                    if (null != vars) {
                        for (String varName : vars.keySet()) {
                            Object value = vars.get(varName);
                            setProcessVariableFor(processInstance.getId(), varName, value);
                        }
                    }

                    jbpmContext.save(processInstance);
                    processInstance.suspend();
                    //processInstance.getRootToken().suspend();
                    jbpmContext.save(processInstance);

                    //  jbpmContext.save(processInstance.getRootToken());
                    long procId = processInstance.getId();
                    processInstance = jbpmContext.getProcessInstance(procId);
                    jbpmContext.save(processInstance);

                    return processInstance;
                }
            });
    }

    public int countOpenTaskInstances() {
        Number answer = (Number) hibernateTemplate.find(" SELECT COUNT(ti.id) FROM TaskInstance ti WHERE   ti.isOpen = true and ti.isSuspended = false    ").iterator().next();

        return answer.intValue();
    }

    public int countOpenTaskInstancesForActors(String[] actors) {
        Number answer = (Number) hibernateTemplate.findByNamedParam(" SELECT COUNT(ti.id) FROM TaskInstance ti WHERE ti.actorId IN( :tids )  and ti.isOpen = true and ti.isSuspended = false", "tids",
                actors).iterator().next();

        return answer.intValue();
    }

    public ProcessInstance createProcessInstance(String flowName) {
        return createProcessInstance(flowName, null);
    }

    public boolean lockTaskInstance(final long taskInstanceId, final String actor) {
        if (taskInstanceId <= 0) {
            return false;
        }

        Boolean answer = (Boolean) jbpmTemplate.execute(new JbpmCallback() {
                    public Object doInJbpm(JbpmContext jbpmContext)
                        throws JbpmException {
                        TaskInstance taskInstance = getTaskInstance(taskInstanceId);

                        if (taskInstance != null) {
                            if ((taskInstance.getStart() == null) && !taskInstance.isCancelled() && !taskInstance.hasEnded()) {
                                taskInstance.start(actor);

                                return Boolean.TRUE;
                            }
                        }

                        return Boolean.FALSE;
                    }
                });

        return answer;
    }

    /**
     * You give it a task instance and itll try and return the variable first from the process scope * and then from the
     * taskIntance scope. * * @param taskInstanceId * @param key * @return
     */
    public Object getVariable(long taskInstanceId, String key) {
        long processInstanceId = getProcessInstanceIdForTaskInstance(taskInstanceId);
        Object obj = getVariableByProcessInstance(processInstanceId, key);

        if (null == obj) {
            obj = getTaskInstanceVariable(taskInstanceId, key);
        }

        return obj;
    }

    public Object getVariableByProcessInstance(long procinstId, String key) {
        Map<String, Object> vars = getProcessVariablesFor(procinstId);

        if ((vars != null) && vars.containsKey(key)) {
            return vars.get(key);
        }

        return null;
    }

    private Object getTaskInstanceVariable(long taskInstanceId, String key) {
        TaskInstance ti = getTaskInstance(taskInstanceId);

        return ti.getVariable(key);
    }

    public ProcessInstance getProcessInstanceById(Long processId) {
        ProcessInstance pi = jbpmTemplate.findProcessInstance(processId);
        Hibernate.initialize(pi);

        return pi;
    }

    protected TaskInstance getTaskInstance(final long tid) {
        return (TaskInstance) jbpmTemplate.execute(new JbpmCallback() {
                public Object doInJbpm(JbpmContext jbpmContext)
                    throws JbpmException {
                    try {
                        return _taskInstance(jbpmContext.getSession(), jbpmContext, tid);
                    } catch (Throwable throwable) {
                        // getLoggingUtils().log(throwable);
                    }

                    return null;
                }
            });
    }

    private TaskInstance _taskInstance(Session session, JbpmContext ctx, long taskInstanceId)
        throws Throwable {
        TaskInstance taskInstance = null;

        try {
            taskInstance = (TaskInstance) session.load(TaskInstance.class, taskInstanceId, LockMode.UPGRADE);
        } catch (Throwable e) {
            throw new JbpmException("couldn't get task instance '" + taskInstanceId + "'", e);
        }

        return taskInstance;
    }

    public boolean unlockTaskInstance(final long taskInstanceId) {
        if (taskInstanceId != 0) {
            jbpmTemplate.execute(new JbpmCallback() {
                    public Object doInJbpm(JbpmContext jbpmContext)
                        throws JbpmException {
                        TaskInstance ti = getTaskInstance(taskInstanceId);
                        ti.suspend();
                        jbpmContext.save(ti);

                        return null;
                    }
                });
        }

        return true;
    }

    private Long getParentProcessInstanceIdForProcessInstance(ProcessInstance pi) {
        Token to = pi.getSuperProcessToken();

        if (to != null) {
            ProcessInstance pi2 = to.getProcessInstance();

            if (pi2 != null) {
                return pi2.getId();
            }
        }

        return null;
    }

    /*  private long getParentMostProcessInstanceId(long taskInstanceId) {
        long processIntanceId = getProcessInstanceIdForTaskInstance(taskInstanceId);
        ProcessInstance pi = getProcessInstanceById(processIntanceId);
        Long parentPid = null;
        while ((parentPid = getParentProcessInstanceIdForProcessInstance(pi)) != null) {
            pi = getProcessInstanceById(parentPid);
        }
        return pi.getId();
    }*/
    public void completeTaskInstance(final long taskInstanceId) {
        if (taskInstanceId == 0) {
            return;
        }

        jbpmTemplate.execute(new JbpmCallback() {
                public Object doInJbpm(JbpmContext jbpmContext)
                    throws JbpmException {
                    final TaskInstance taskInstance = getTaskInstance(taskInstanceId);

                    if ((taskInstance.getEnd() == null) && !taskInstance.hasEnded()) {
                        taskInstance.end();
                        jbpmContext.save(taskInstance);
                        jmsTemplate.send(bpmsStartedPingDestination,
                            new MessageCreator() {
                                public Message createMessage(javax.jms.Session session)
                                    throws JMSException {
                                    MapMessage msg = session.createMapMessage();
                                    msg.setLong("taskInstanceId", taskInstanceId);
                                    msg.setString("actorId", taskInstance.getActorId());
                                    msg.setLong("processInstanceId", getProcessInstanceIdForTaskInstance(taskInstanceId));

                                    return msg;
                                }
                            });
                    }

                    return null;
                }
            });
    }

    public void setProcessVariableFor(final long processinstanceid, final String name, final Object val) {
        jbpmTemplate.execute(new JbpmCallback() {
                public Object doInJbpm(JbpmContext context)
                    throws JbpmException {
                    final String fName = StringUtils.defaultString(name);
                    boolean wasValNull = val == null;
                    final String fVal = wasValNull ? StringUtils.EMPTY : val.toString();

                    // getLoggingUtils().log(String.format("setting [%s]=[%s] ", fName, fVal, fName, !wasValNull ? "not " : ""));
                    ProcessInstance processInstance = getProcessInstanceById(processinstanceid);
                    processInstance.getContextInstance().setVariable(name, val);
                    context.save(processInstance);

                    return null;
                }
            });
    }

    public void setProcessVariablesFor(long processInstanceId, Map<String, Object> vals) {
        for (String varName : vals.keySet()) {
            setProcessVariableFor(processInstanceId, varName, vals.get(varName));
        }
    }

    public Long lockNextTaskInstanceByActorAndCriteria(String actor, Map<String, Object> vars) {
        Long tid = getNextTaskInstanceByActorAndCriteria(actor, vars);

        if (null != tid) {
            if (lockTaskInstance(tid, actor)) {
                return tid;
            }
        }

        return null;
    }

    public Long getNextTaskInstanceByActorAndCriteria(final String actor, final Map<String, Object> vars) {
        Object result = jbpmTemplate.execute(new JbpmCallback() {
                    public Object doInJbpm(JbpmContext jbpmContext)
                        throws JbpmException {
                        Collection<Long> tis = getOpenTaskInstancesByActorAndCriteria(actor, vars);

                        if ((tis != null) && (tis.size() > 0)) {
                            return tis.iterator().next();
                        }

                        return null;
                    }
                });

        if (null == result) {
            return null;
        }

        return ((Long) result);
    }

    public Long getNextTaskInstanceByActor(final String actor) {
        return getNextTaskInstanceByActorAndCriteria(actor, null);
    }

    public Map<String, Object> getProcessVariablesFor(final long processInstanceId) {
        Map<String, Object> variableMap = (Map<String, Object>) jbpmTemplate.execute(new JbpmCallback() {
                    public Object doInJbpm(JbpmContext jbpmContext)
                        throws JbpmException {
                        ProcessInstance pi = jbpmContext.getProcessInstance(processInstanceId);
                        Map vars = pi.getContextInstance().getVariables();

                        return vars;
                    }
                });

        return variableMap;
    }

    public Collection<Long> getOpenTaskInstancesByActor(final String actor) {
        Collection<Long> tis = getOpenTaskInstancesByActorAndCriteria(actor, null);

        return tis;
    }

    public ProcessInstance createAndStartProcessInstance(String flowName, Map<String, Object> params) {
        ProcessInstance p = createProcessInstance(flowName, params);
        startProcessInstance(p.getId());

        return p;
    }

    public ProcessInstance createAndStartProcessInstance(String flowName) {
        ProcessInstance p = createProcessInstance(flowName);
        startProcessInstance(p.getId());

        return p;
    }

    public ProcessInstance createNewProcessInstance(final String defS) {
        ProcessInstance inst = (ProcessInstance) jbpmTemplate.execute(new JbpmCallback() {
                    public Object doInJbpm(JbpmContext ctx)
                        throws JbpmException {
                        ProcessDefinition def = ctx.getGraphSession().findLatestProcessDefinition(defS);
                        ProcessInstance inst = def.createProcessInstance();
                        inst.suspend();
                        ctx.save(inst);

                        return inst;
                    }
                });

        return inst;
    }

    public boolean isProcessInstanceScheduledFor(long processInstanceId, Date when) {
        Object valueForSchedule = getVariableByProcessInstance(processInstanceId, BPMS_PROCESS_INSTANCE_WELL_KNOWN_PROCESS_ATTRIBUTE_SCHEDULED_DATE);

        if (valueForSchedule != null) {
            Date decodedDate = decodeDateFromProcessAttribute((String) valueForSchedule);
            Date now = new Date();
            Date topOfHour = DateUtils.truncate(now, Calendar.HOUR_OF_DAY);
            Date endOfHour = DateUtils.add(topOfHour, Calendar.HOUR_OF_DAY, 1);
            Date halfHour = DateUtils.add(topOfHour, Calendar.MINUTE, 30);
            Date[] viableDates = { now, topOfHour, endOfHour, halfHour };

            for (Date d : viableDates) {
                if (d.equals(decodedDate)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void afterPropertiesSet() throws Exception {
    }
}
