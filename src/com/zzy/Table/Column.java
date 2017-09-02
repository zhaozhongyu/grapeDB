package com.zzy.Table;

import static com.zzy.Table.ColumnType.*;

public class Column {
    private String name;    //名字
    private ColumnType type; //标识类型
    private int length = 0; //对于varchar等类型, 标识最大长度, 对于int等基础类型, 不生效
    private boolean isNullable = true; //是否可空, 默认为true


    private boolean primaryKey; //是否主键  update: 是否主键对column 无影响, 对table才有影响, 用于创建index
    private boolean isUnique; //是否唯一
    private int columnid; //标识在column list 中的位置, 用以在新增数据/查询数据等操作时快速获取, 在创建table的时候设置
    private Table table; //标识属于哪个table , 可以没有

    public Column(String name ){this.name = name;}

    /**
     * Instantiates a new Column.
     *
     * @param name the name
     * @param type the type
     * @param length the length
     * @param isNullable the is nullable
     * @param isUnique the is unique
     */
    public Column(String name, ColumnType type, int length,  boolean isUnique, boolean isNullable){
        this.name = name;
        this.type = type;
        this.length = length;
        this.isNullable = isNullable;
        this.isUnique = isUnique;
    }

    //此构造方法用于从磁盘文件中读取新建使用
    public Column(String name, int type, int length, boolean primaryKey, boolean isUnique, boolean isNullable){
        ColumnType columnType = ColumnType.values()[type];
        this.name = name;
        this.type = columnType;
        this.length = length;
        this.primaryKey = primaryKey;
        this.isNullable = isNullable;
        this.isUnique = isUnique;
    }

    public Column(String name, ColumnType type, int length,  boolean isUnique){ //此构造方法用于测试
        this.name = name;
        this.type = type;
        this.length = length;
        this.isUnique = isUnique;
        if(isUnique){
            this.isNullable = false;
        } else{
            this.isNullable = true;
        }
    }


    /**
     * Set table.
     *
     * @param table the table
     */
    protected void setTable(Table table){
        this.table = table;
    }

    protected void setColumnid(int columnid){
        this.columnid = columnid;
    }

    /**
     * Get columnid int.
     *
     * @return the int
     */
    public int getColumnid(){
        return this.columnid;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public ColumnType getType()
    {
        return type;
    }

    /**
     * Gets length.
     *
     * @return the length
     */
    public int getLength()
    {
        return length;
    }

    /**
     * Is nullable boolean.
     *
     * @return the boolean
     */
    public boolean isNullable()
    {
        return isNullable;
    }

    /**
     * Is unique boolean.
     *
     * @return the boolean
     */
    public boolean isUnique()
    {
        return this.isUnique;
    }

    /**
     * Gets table.
     *
     * @return the table
     */
    public Table getTable()
    {
        return table;
    }

    public void setType(ColumnType type)
    {
        this.type = type;
    }

    public void setLength(int length)
    {
        this.length = length;
    }

    public void setNullable(boolean nullable)
    {
        isNullable = nullable;
    }

    public void setPrimaryKey(boolean primaryKey)
    {
        this.primaryKey = primaryKey;
    }

    public void setUnique(boolean unique)
    {
        if(unique){
            isNullable = false; //唯一列不允许为空
        }
        isUnique = unique;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }
}
