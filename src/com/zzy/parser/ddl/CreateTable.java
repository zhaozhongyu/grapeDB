package com.zzy.parser.ddl;

import com.zzy.Schema.Schema;
import com.zzy.Table.Column;
import com.zzy.Table.Table;

import java.util.LinkedList;

/**
 *
 * 通过这个类来创建table
 */
public class CreateTable
{
    private String tableName;
    public LinkedList<Column> columns ;
    public Schema schema;
    public Column primarykey;

    public CreateTable(Schema schema){
        this.schema =schema;
        columns = new LinkedList<>();
    }

    public void addColumn(Column column){
        this.columns.add(column);
    }

    public Table create(){
        if(primarykey != null){
            Column[] columnArray = new Column[columns.size()];
            Table table = new Table(tableName, schema, columns.toArray(columnArray),primarykey );
            return table;
        }
        Table table = new Table(tableName, schema, (Column[])columns.toArray() );
        return table;
    }

    public void setTableName(String tableName)
    {
        this.tableName = tableName;
    }

    public void setPrimarykey(Column primarykey)
    {
        this.primarykey = primarykey;
    }
}
