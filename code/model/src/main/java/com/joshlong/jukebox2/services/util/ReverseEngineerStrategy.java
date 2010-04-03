package com.joshlong.jukebox2.services.util;

import org.hibernate.cfg.reveng.DelegatingReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.ReverseEngineeringStrategy;
import org.hibernate.cfg.reveng.ReverseEngineeringStrategyUtil;
import org.hibernate.cfg.reveng.TableIdentifier;
import org.hibernate.mapping.Column;
import org.hibernate.util.StringHelper;

import java.beans.Introspector;
import java.util.List;

/**
 * The schema for the various bits of music data 
 * 
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class ReverseEngineerStrategy extends DelegatingReverseEngineeringStrategy {

    public ReverseEngineerStrategy(ReverseEngineeringStrategy reverseEngineeringStrategy) {
        super(reverseEngineeringStrategy);
    }

    static private void log(String msg, Object... pa) {
        System.out.println(String.format(msg, pa));
    }

    private String fkToEntityNameHelper(String in) {
        String in2 = toUpperCamelCase(in);
        if (Character.isUpperCase(in2.charAt(0)))
            in2 = (in2.charAt(0) + "").toLowerCase() + in2.substring(1);
        return in2;
    }

    public String foreignKeyToEntityName(String keyname, TableIdentifier fromTable, List fromColumnNames, TableIdentifier referencedTable, List referencedColumnNames, boolean uniqueReference) {
        String propertyName = Introspector.decapitalize(StringHelper.unqualify(tableToClassName(referencedTable)));

        if (uniqueReference) {
            propertyName = ((Column) fromColumnNames.get(0)).getName();
            propertyName = Introspector.decapitalize(toUpperCamelCase(propertyName));

            if (propertyName.endsWith("Id"))
                propertyName = propertyName.substring(0, propertyName.length() - 2);
        }
//        log("fromColumnNames: unique ref; keyName:%s, propertyName:%s, fromTable:%s, referencedTable:%s, referenced column:%s, from columns:%s", keyname, propertyName,
//                fromTable.getName(), referencedTable.getName(), referencedColumnNames.toString(), fromColumnNames.toString());

        if (!uniqueReference) {
            if (fromColumnNames != null && fromColumnNames.size() == 1) {
                String columnName = ((Column) fromColumnNames.get(0)).getName();

                /*     log("fromColumnNames:1; keyName:%s, propertyName:%s, fromTable:%s, referencedTable:%s, referenced column:%s, from columns:%s",  keyname , propertyName ,
                                             fromTable.getName() ,referencedTable.getName(), referencedColumnNames.toString(), fromColumnNames.toString());
                */
                //    propertyName = propertyName + "By" + toUpperCamelCase(columnName);
                propertyName = fkToEntityNameHelper(toUpperCamelCase((columnName.toLowerCase().endsWith("_id") ? columnName.substring(0, columnName.length() - 3) : columnName)));//+ toUpperCamelCase(propertyName));

                //       System.out.println( "pn: "+ propertyName+ " from first case") ;
            } else { // composite key or no columns at all safeguard
//                log("fromColumnNames:>1; keyName:%s, propertyName:%s, fromTable:%s, referencedTable:%s, referenced column:%s, from columns:%s",  keyname , propertyName ,
//                                         fromTable.getName() ,referencedTable.getName(), referencedColumnNames.toString(), fromColumnNames.toString());

                //   propertyName = propertyName + "By" + toUpperCamelCase(keyname);
                propertyName = fkToEntityNameHelper(toUpperCamelCase(keyname) + toUpperCamelCase(propertyName));
                //  System.out.println( "pn: "+ propertyName+ " from second case") ;
            }
        }

        return propertyName;
    }

    protected String toUpperCamelCase(String s) {
        return ReverseEngineeringStrategyUtil.toUpperCamelCase(s);
    }

    /*public String columnToPropertyName(TableIdentifier table, String column) {
     //   System.out.println(">>>>>> columnToPropertyName:" + table.getName() + ":" + column);
        if (column.endsWith("PK")) {
            return "id";
        } else {
            return super.columnToPropertyName(table, column);
        }
    }*/
}
