package com.zzy.Schema;

import com.zzy.Table.Table;

import java.util.HashMap;

public class Schema
{
    private String name;
    private boolean system; //标识是否系统自动创建的, 用户创建的设为false
    private final HashMap<String, Table> tablesAndViews;  //存放该Schema下的所有table和view, table或view不可重名

    public Schema(boolean system, String name) {
        this.system = system;
        tablesAndViews = new HashMap<String, Table>();
        this.name = name;
    }

    public Table getTableOrView(String name) {
        Table table = tablesAndViews.get(name);
        return table;
    }
    public void addTable(Table table) {
        synchronized(this) {
            if (tablesAndViews.get(table.getName()) != null) {
                throw new RuntimeException("Duplicated Table:" + table.getName());
            }
            tablesAndViews.put(table.getName(), table);
        }
    }

    public void removeTable(String tableName){
        synchronized(this){
            if (!tablesAndViews.containsKey(tableName)){
                throw new RuntimeException("Not such table:"+tableName);
            }
            tablesAndViews.remove(tableName);
        }
    }

}
