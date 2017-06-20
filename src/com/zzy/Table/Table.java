package com.zzy.Table;

import com.zzy.Index.TreeLeaf;
import com.zzy.Index.index;
import com.zzy.Schema.Schema;
import com.zzy.Value.Value;
import com.zzy.Value.ValueInt;

import java.util.HashMap;

public class Table
{
    private String name;
    private Column[] columns ; //存放一个column数组, insert/update时用于校验合法性, 由于columns极少发生变换, 反而在查找的时候较多, 故使用数组
    protected HashMap<String, Column> columnHashMap ; //存放column, 用于在非全量插入时使用和修改column时快速使用
    protected Schema schema;   //标识schema, 主要是权限管理用途, 关系链 user - rule - schema - table
    protected int uniqueColumns; //记录唯一列的数量, 用于在插入数据时校验是否已经输入所有唯一列
    protected HashMap<String, index> indexMap; //存放当前已建立的index, key为index标示的唯一列名字, 只允许唯一列创建index, 并且此列必须为可compare的字段
    protected index defaultIndex; //默认index, 当新增index时, 遍历defaultIndex以插入index
    private int id; //隐式primarykey的主键值

    /**
     *
     * @param name
     * @param schema
     * @param columns
     * @param primarykey
     */
    public Table (String name, Schema schema, Column[] columns, Column primarykey){
        int columnId = 0;
        this.columns = columns;
        this.schema = schema;
        this.name = name;
        this.uniqueColumns = 0;
        this.columnHashMap = new HashMap<String, Column>();
        for(Column column: columns){ //获取uniqueColumn的大小, 并且添加到columnHashmap中
            column.setColumnid(columnId++);
            columnHashMap.put(column.getName(), column);
            if(column.isUnique())
                this.uniqueColumns++;
        }
        this.indexMap = new HashMap<String, index>();
        this.id = 0;

        CreateIndex(primarykey);
        schema.addTable(this);
    }

    /**
     * 未指定主键时, 则寻找一个唯一列作为主键使用, 倘若找不到唯一列, 则生成一个自增Column作为隐藏主键
     * @param name
     * @param schema
     * @param columns
     */
    public Table(String name, Schema schema, Column[] columns){
        int columnId = 0;
        for(Column column: columns){
            if(column.isUnique()){
                this.columns = columns;
                this.schema = schema;
                this.name = name;
                this.uniqueColumns = 0;
                this.columnHashMap = new HashMap<String, Column>();
                for(Column column1: columns){ //获取uniqueColumn的大小, 并且添加到columnHashmap中
                    column1.setColumnid(columnId++);
                    columnHashMap.put(column1.getName(), column1);
                    if(column1.isUnique())
                        this.uniqueColumns++;
                }

                this.indexMap = new HashMap<String, index>();
                this.id = 0;
                CreateIndex(column);
                break;
            }
        }
        if(this.defaultIndex == null){ //倘若没有唯一列
           /* Column autoColumn = new Column("AutoColumn", ColumnType.INT, 0,true);
            new Table(name, schema, columns, autoColumn);*/
           throw new RuntimeException("Table must have unique column.");
        }
    }

    /**
     * 创建一个index, 在Table初始化的时候和新增index的时候调用
     * @param column
     */
    public void CreateIndex(Column column){
        if(!column.isUnique()){
            throw new RuntimeException("Can't create index with not unique column."); //必须要唯一列才能创建index
        }
        if(this.defaultIndex == null){ //倘若没有defaultIndex则说明表里面没有数据
            this.defaultIndex = new index(column);
            indexMap.put(column.getName(), this.defaultIndex);
            return;
        }
        //已存在defaultIndex, 则获取到defaultIndex的最左边叶子节点, 进行初始化index
        TreeLeaf leaf = this.defaultIndex.getIndexTree().getFirstLeaf();
        index newIndex = new index(column, leaf);
        indexMap.put(column.getName(), newIndex);
        return;
    }
    /**
     *  增加一个column, 在alter table时调用, 如果是nullable==false的, 调用该方法前会校验row中是否已经有数据
     * @param column
     * @return
     */
    public boolean addColumn(Column column){
        synchronized (this) {
            for(Column c: columns){
                if (c.getName() == column.getName()){
                    return false;
                }
            }
            Column[] newcolumns = new Column[columns.length + 1];
            System.arraycopy(columns, 0, newcolumns, 0, columns.length);
            column.setColumnid(columns.length+1);
            newcolumns[columns.length+1] = column;
            this.columns = newcolumns;
            columnHashMap.put(column.getName(), column);
            if(column.isUnique()){
                uniqueColumns++; //增加一个唯一列
            }
            return true;
        }
    }

    /**
     * 删除一个column, 删前检查row中是否有数据
     * @param columnName
     * @return
     */
    public boolean removeColumn(String columnName) {
        synchronized(this){
            Column[] newcolumns = new Column[columns.length - 1];
            if(!columnHashMap.containsKey(columnName)){
                return false; //没有待删除的列
            }
            for(int i = 0, index =0; i < columns.length; i++, index++){ //i对应columns, index对应newcolumns
                if (columns[i].getName() != columnName){
                    newcolumns[index] = columns[i];
                    newcolumns[index].setColumnid(index);
                } else { //需要删除的那行
                    if(columns[i].isUnique()){
                        uniqueColumns--; //删除一个唯一列
                    }
                    index --;
                }
            }
        }
        return true;
    }

    /**
     * 修改column
     * @param column
     * @return
     */
    public boolean alterColumn(Column column){
        if(!columnHashMap.containsKey(column.getName())){
            return false;
        }
        if(columnHashMap.get(column.getName()).isUnique() && ! column.isUnique()){
            uniqueColumns--;
        }
        if(! columnHashMap.get(column.getName()).isUnique() && column.isUnique()){
            uniqueColumns ++;
        }
        columnHashMap.replace(column.getName(), column);
        return true;
    }

    public boolean insert(Value[] values){
        return insert(values, this.columns); //未指定插入哪几列的情况下, 表示插入全部列
    }

    /**
     * 插入指定的几列, 所有的唯一列都必须有数据
     * @param values
     * @param columns
     * @return
     */
    public boolean insert(Value[] values, Column[] columns){
        int uniqueNum = 0;
        if(values.length != columns.length){
            throw new RuntimeException("Wrong parameter");
        }
        Value[] rowvalue = new Value[this.columns.length]; //初始化一个数组作为创建row的依据
        for(int i = 0; i < columns.length; i++){
            if(columns[i].isUnique()){
                uniqueNum++;
            }
            rowvalue[columns[i].getColumnid()] = values[i]; //将value值赋到rowvalue数组的指定位置中
        }
        if(uniqueNum != this.uniqueColumns){
            throw new RuntimeException("some unique column do not hava value");
        }
        Row row = new Row(rowvalue);
        indexMap.forEach( (k,v) -> { //遍历当前table下的所有index, 插入index
            if(k.equals("AutoColumn")){
                Value key = new ValueInt(this.id);
                v.insert(key, row);
            }else {
                Column column = columnHashMap.get(k);
                v.insert(row.getValue(column.getColumnid()), row);
            }
        });
        return true;
    }
    public void delete(Value value, String columnName){
        index currentIndex = indexMap.get(columnName);
        currentIndex.getIndexTree().delete(value);
        return;
    }

    public Row search(Value value, String ColumnName){
        index currentIndex = indexMap.get(ColumnName);
        return currentIndex.search(value);
    }

    public Column getColumn(String columnName){
        return this.columnHashMap.get(columnName);
    }

    public String getName(){
        return name;
    }
}
