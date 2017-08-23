package com.zzy.Index;

import com.zzy.Table.Column;
import com.zzy.Table.Row;
import com.zzy.Value.Value;

/**
 * 包装B+树, 提供访问接口
 * */
public class Index
{
    protected Column column; //记录唯一列的名字, 唯一列在row中的存放位置i

    protected TreeGen indexTree; //建立b+树

    public Index(Column column){
        this.column = column;
        this.indexTree = new TreeGen(4); //随便取了个4阶作为默认使用
    }

    /**
     * 此构造函数, 是用于在已经有了数据的表中, 再创建一个index的场景, 再创建index时需要依次写入数据, leaf表示旧index中的最左边叶子节点
     * @param column
     * @param leaf
     */
    public Index(Column column, TreeLeaf leaf ){
        this.column = column;
        this.indexTree = new TreeGen(4);
        int i = column.getColumnid(); //当前column字段在row中数组Value[]的存放位置
        while(leaf != null){
            leaf.values.forEach(row -> indexTree.insert(((Row)row).getValue(i), row));  //从row中取出对应的column值并插入index
            leaf = leaf.rightBrother;
        }

    }

    /**
     * insert into 命令
     * @param value
     * @param row
     */
    public void insert(Value value, Row row){
        Row exist = this.search(value);
        if(exist != null){
            throw new RuntimeException("Duplicate key.");
        }
        indexTree.insert(value, row);
    }

    /**
     * select 命令
     * @param value
     * @return
     */
    public Row search(Value value){
        TreeLeaf leaf = (TreeLeaf)indexTree.search(value);
        if(leaf != null){
            for(int i = 0; i < leaf.keys.size(); i++){
                if(leaf.keys.get(i).equals(value)){
                    return (Row)leaf.values.get(i);
                }
            }
        }
        return null;
    }



    public TreeLeaf searchLeaf(Value value){
        return (TreeLeaf)indexTree.search(value);
    }

    public TreeLeaf getFirstLeaf() {
        if(indexTree != null){
            return indexTree.getFirstLeaf();
        }
        return null;
    }

    /**
     * update 命令
     * @param value
     * @param row
     */
    public void update(Value value, Row row){
        Row exist = this.search(value);
        if(exist == null){
            throw new RuntimeException("Not exist key.");
        }
        indexTree.insert(value, row);
    }

    /**
     * 获取columnName
     * @return
     */
    public String getColumnName()
    {
        return column.getName();
    }

    public TreeGen getIndexTree(){
        return this.indexTree;
    }

}
