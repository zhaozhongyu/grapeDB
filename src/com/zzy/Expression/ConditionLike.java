package com.zzy.Expression;

import com.zzy.Table.ColumnResolver;
import com.zzy.Value.Value;
import com.zzy.Value.ValueBoolean;
import com.zzy.engine.SQLConnection;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConditionLike extends Condition{

    private boolean Not; //是否有not关键词
    private Expression column; // 当前条件式的左边columnName

    private Expression pattern;



    public ConditionLike(Expression column, Expression pattern, boolean Not){
        this.column = column;
        this.Not = Not;
        this.pattern = pattern;
    }

    public String getColumnName() {
        return this.column.getColumnName();
    }

    public int getType() {
        return this.Like;
    }

    //实时将sql中的_转换为.点号,  将%转换为.* , 然后使用正则表达式匹配
    //todo 非ValueString 类型应当无法使用like语句, 此时未做类型校验
    @Override
    public Value getValue(SQLConnection connection) {
        Value right = pattern.getValue(connection);
        Value left = column.getValue(connection);
        String rightStr = right.toString();
        String leftStr = left.toString();
        rightStr = rightStr.replaceAll("_", ".");
        rightStr = rightStr.replaceAll("%", ".*");
        Pattern pattern = Pattern.compile(rightStr);
        Matcher matcher = pattern.matcher(leftStr);
        if( (!Not && matcher.find() ) || (Not && ! matcher.find()) ){
            return ValueBoolean.get(true);
        }

        return ValueBoolean.get(false);
    }

    @Override
    public void mapColumns(ColumnResolver resolver, int level) {
        this.column.mapColumns(resolver, level);
    }
}
