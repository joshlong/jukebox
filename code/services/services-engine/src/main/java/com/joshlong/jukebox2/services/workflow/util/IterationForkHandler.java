package com.joshlong.jukebox2.services.workflow.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.jbpm.bytes.ByteArray;
import org.jbpm.context.exe.ContextInstance;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.def.Transition;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.graph.exe.Token;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.LinkedList;
import java.util.List;

/*
 *
 * http://www.jboss.com/index.html?module=bb&op=viewtopic&p=4055349#4055349
 *
 *
 start
 fork
 sub-process
 join
 end

 * @author chadwickboggs@yahoo.com
 */

public class IterationForkHandler implements ActionHandler {
    protected List list;
    protected String as;
    private String listVariable;
    private String onEmptyListTransition;

    public String getOnEmptyListTransition() {
        return onEmptyListTransition;
    }

    public void setOnEmptyListTransition(String onEmptyListTransition) {
        this.onEmptyListTransition = onEmptyListTransition;
    }

    protected static final String FOREACH_PREFIX = "foreach.";

    /**
     * Create a new child token for each item in list.
     *
     * @param executionContext
     *
     * @throws Exception
     */
    public void execute(final ExecutionContext executionContext) throws Exception {
        initializeList(executionContext);
        final Token rootToken = executionContext.getToken();
        final Node node = executionContext.getNode();
        final List argSets = new LinkedList();

        System.out.println("size of list of elements to iterate: " + (list != null ? list.size() + "" : "null"));
        //
        // First, create a new token and execution context for each item in
        // list.
        //
        for (int i = 0; i < node.getLeavingTransitions().size(); i++) {
            final Transition transition = (Transition) node.getLeavingTransitions().get(i);

            // safe guard against transitioning tot he empty list transition if
            // there is
            if (!StringUtils.isEmpty(onEmptyListTransition)) {
                if (!StringUtils.isEmpty(transition.getName())) {
                    if (transition.getName().equalsIgnoreCase(onEmptyListTransition)) {
                        continue;
                    }
                }
            }

            for (int j = 0; list != null && j < list.size(); j++) {
                final Object item = list.get(j);
                final Token newToken = new Token(rootToken, FOREACH_PREFIX + node.getId() + "." + j);
                newToken.setTerminationImplicit(true);

                // /
                // executionContext.getProcessInstance().getJbpmSession().getSession().save(newToken);
                executionContext.getJbpmContext().getSession().save(newToken);
                final ExecutionContext newExecutionContext = new ExecutionContext(newToken);
                newExecutionContext.getContextInstance().createVariable(as, item, newToken);
                argSets.add(new Object[]{newExecutionContext, transition});
            }
        }

        //
        // Now, let each new token leave the node.
        //
        if (argSets.size() != 0) // which will only happen if theres no list
        // over which to iterate
        {
            for (int i = 0; i < argSets.size(); i++) {
                final Object[] args = (Object[]) argSets.get(i);
                node.leave((ExecutionContext) args[0], (Transition) args[1]);
            }
        }
        else {
            if (!StringUtils.isEmpty(onEmptyListTransition)) {
                Transition transition = node.getLeavingTransition(onEmptyListTransition);
                if (transition != null) {
                    node.leave(executionContext, transition);
                }
            }
            else {
                throw new RuntimeException(
                        "We have no where to transition! Iteration list size is 0. Please provide an onEmptyListTransition string");
            }
        }
    }

    public List getList() {
        return list;
    }

    public void setList(final List list) {
        this.list = list;
    }

    public String getAs() {
        return as;
    }

    public void setAs(final String as) {
        this.as = as;
    }

    public String getListVariable() {
        return listVariable;
    }

    public void setListVariable(final String listVariable) {
        this.listVariable = listVariable;
    }

    private List<Long> fromByteArray(ByteArray ba) {
        try {
            byte[] bytes = ba.getBytes();
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
            Object obj = in.readObject();
            java.util.List<Long> ids = (java.util.List<Long>) obj;
            in.close();
            return ids;
        }
        catch (Throwable t) {
            System.out.println(ExceptionUtils.getFullStackTrace(t));
        }
        return null;
    }

    protected void initializeList(final ExecutionContext executionContext) {
        if (list == null && listVariable != null) {
            ContextInstance contextInstance = executionContext.getContextInstance();
            Object val = contextInstance.getVariable(listVariable);
            if (val instanceof ByteArray) {
                list = fromByteArray((ByteArray) val);
            }
            else {
                list = (List) val;
            }

		}
	}
}
