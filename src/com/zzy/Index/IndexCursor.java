package com.zzy.Index;

import com.zzy.Expression.ConditionArithmetic;
import com.zzy.Expression.ConditionBetween;
import com.zzy.Expression.ConditionIn;
import com.zzy.Expression.Expression;
import com.zzy.Table.Row;
import com.zzy.Table.TableFilter;
import com.zzy.Value.Value;
import com.zzy.engine.SQLConnection;

import java.util.ArrayList;

/**
 * 作为TableFilter和index间的耦合类存在, 用于根据index条件获取到符合条件的row
 * 只支持单个index条件, 包括=, !=, >, >=, <, <=, in, between, 不处理like
 * 其中, get方法可以多次调用, 每次调用应得到一致的数据, 当调用了next/previous后, 再调用get时得到下一个满足条件的数据
 */
public class IndexCursor implements Cursor {
    private final int EQUAL = 0, IN= 1,  BIGGER=2, BIGGER_EQUAL=3, LESS=4, LESS_EQUAL=5, BETWEEN=6,NOT_EQUAL=7, NULL = -1;
    private Value value; //对于=, !=, >, >=, <, <= 时使用
    private int Type;

    private Value[] in;
    private Value between_min; //between是闭区间
    private Value between_max;
    private boolean Not;

    private SQLConnection connection;
    private TableFilter tableFilter;
    private Index index;

    private TreeLeaf leaf; //当前游标指向的叶子节点
    private int current = -1;
    private Row currentRow; //当前游标指向的Row
    private ArrayList<Row> rows;
    private Expression condition;


    public IndexCursor(SQLConnection connection,TableFilter filter, Index index) {
        this.connection = connection;
        this.tableFilter = filter;
        this.index = index;
        condition = null;
        leaf = null;
        rows = null;
    }

    public boolean addFilterCondition(Expression condition){ //只会有一个condition
        if(condition.getColumnName().equals(index.getColumnName())){
            this.condition = condition;
            init();
            return true;
        }
        return false;
    }

    @Override
    public Row get() {
        return rows.get(current);
    }

    @Override

    public boolean next() {
        if(rows == null){
            init();
        }
        if (current < rows.size() - 1) {
            current += 1;
            return true;
        }
        return false;
    }


    @Override
    public boolean previous() {
        if (current > 0) {
            current = current - 1;
            return true;
        }
        return false;
    }

    //
    @Override
    public void init() {
        rows = new ArrayList<>();
        if(condition instanceof ConditionArithmetic){
            value = ((ConditionArithmetic) condition).getValue().getValue(connection);
            switch (((ConditionArithmetic) condition).getSymbol()){
                case "=":
                    Type = EQUAL;
                    currentRow = index.search(value);
                    if(currentRow != null){
                        rows.add(currentRow);
                    }
                    return;
                case ">":
                    Type = BIGGER;
                    leaf = index.searchLeaf(value);
                    while (leaf != null){
                        for(int i = 0; i < leaf.values.size(); i++){
                            currentRow = (Row) leaf.values.get(i);
                            if(value.compareTo(leaf.keys.get(i)) < 0){
                                rows.add(currentRow);
                            }
                        }
                        leaf = leaf.rightBrother;
                    }
                    return;
                case ">=":
                    Type = BIGGER_EQUAL;
                    leaf = index.searchLeaf(value);
                    while (leaf != null){
                        for(int i = 0; i < leaf.values.size(); i++){
                            currentRow = (Row) leaf.values.get(i);
                            if(value.compareTo(leaf.keys.get(i)) <= 0){
                                rows.add(currentRow);
                            }
                        }
                        leaf = leaf.rightBrother;
                    }
                    return;
                case "<":
                    Type = LESS;
                    leaf = index.getFirstLeaf();
                    while (leaf != null){
                        for(int i = 0; i < leaf.values.size(); i++){
                            currentRow = (Row) leaf.values.get(i);
                            if(value.compareTo(leaf.keys.get(i)) > 0){
                                rows.add(currentRow);
                            } else { //当走到这里说明后面的值都是大于的
                                return;
                            }
                        }
                        leaf = leaf.rightBrother;
                    }
                    return;
                case "<=":
                    Type = LESS_EQUAL;
                    leaf = index.getFirstLeaf();
                    while (leaf != null){
                        for(int i = 0; i < leaf.values.size(); i++){
                            currentRow = (Row) leaf.values.get(i);
                            if(value.compareTo(leaf.keys.get(i)) >= 0){
                                rows.add(currentRow);
                            } else { //当走到这里说明后面的值都是大于的
                                return;
                            }
                        }
                        leaf = leaf.rightBrother;
                    }
                    return;
                case "!=":
                    Type = NOT_EQUAL;
                    leaf = index.getFirstLeaf();
                    while (leaf != null){
                        for(int i = 0; i < leaf.values.size(); i++){
                            currentRow = (Row) leaf.values.get(i);
                            if(value.compareTo(leaf.keys.get(i)) != 0){
                                rows.add(currentRow);
                            }
                        }
                        leaf = leaf.rightBrother;
                    }
                    return;

            }

        }
        else if(condition instanceof ConditionBetween){
            Type = BETWEEN;
            between_min = ((ConditionBetween) condition).getLeft().getValue(connection);
            between_max = ((ConditionBetween) condition).getRight().getValue(connection);
            Not = ((ConditionBetween) condition).isNot();
            if(Not){ //如果存在Not关键字, 就找首尾两边
                leaf = index.getFirstLeaf();
                boolean end = false;
                while (leaf!=null){
                    for(int i = 0; i < leaf.values.size(); i++){
                        currentRow = (Row) leaf.values.get(i);
                        if(between_min.compareTo(leaf.keys.get(i)) > 0){ //寻找比min更小的
                            rows.add(currentRow);
                        } else {
                            end = true;
                        }
                    }
                    if (end){
                        break;
                    }
                    leaf = leaf.rightBrother;
                }
                leaf = index.searchLeaf(between_max);
                while (leaf!=null){
                    for(int i = 0; i < leaf.values.size(); i++){
                        currentRow = (Row) leaf.values.get(i);
                        if(between_max.compareTo(leaf.keys.get(i)) < 0){ //寻找比max更大的
                            rows.add(currentRow);
                        }
                    }
                    leaf = leaf.rightBrother;
                }
            } else {
                leaf = index.searchLeaf(between_min);
                while (leaf!=null){
                    for(int i = 0; i < leaf.values.size(); i++){
                        currentRow = (Row) leaf.values.get(i);
                        if(between_min.compareTo(leaf.keys.get(i)) <= 0 && between_max.compareTo(leaf.keys.get(i)) >= 0){ //寻找比min大 比max小
                            rows.add(currentRow);
                        } else {
                            return ;
                        }
                    }
                    leaf = leaf.rightBrother;
                }
            }
        }
        else if(condition instanceof ConditionIn){
            Type = IN;
            in = new Value[((ConditionIn) condition).getValues().size()];
            for(int i = 0; i < in.length; i++){
                in[i] = ((ConditionIn) condition).getValues().get(i).getValue(connection);
            }
            Not = ((ConditionIn) condition).isNot();
            if(Not){
                leaf = index.getFirstLeaf();
                while (leaf!=null){
                    for(int i = 0; i < leaf.values.size(); i++){
                        currentRow = (Row) leaf.values.get(i);
                        for(Value v : in) {
                            if(v.compareTo(leaf.keys.get(i)) != 0){ //寻找不在in内的
                                rows.add(currentRow);
                            }
                        }
                    }
                    leaf = leaf.rightBrother;
                }
                return ;
            } else {
                for(Value v : in){
                    leaf = index.searchLeaf(v);
                    if(leaf != null){
                        for(int i = 0; i < leaf.values.size(); i++){
                            currentRow = (Row) leaf.values.get(i);
                            if(v.compareTo(leaf.keys.get(i)) == 0){ //寻找不在in内的
                                rows.add(currentRow);
                                break;
                            }
                        }
                    }
                }
                return;
            }
        }
        else { //走到这里说明condition 为null, 此时会采取遍历
            Type = NULL;
            leaf = index.getFirstLeaf();
            while (leaf!=null){ //把所有row节点加入到rows中
                for(int i = 0; i < leaf.values.size(); i++){
                    currentRow = (Row) leaf.values.get(i);
                    rows.add(currentRow);
                }
                leaf = leaf.rightBrother;
            }
            return;
        }
    }

    // 用于重置条件
    @Override
    public void reset() {
        this.condition = null;
        this.currentRow = null;
        this.leaf = null;
        this.Type = -2;
        current = -1;
    }


}
