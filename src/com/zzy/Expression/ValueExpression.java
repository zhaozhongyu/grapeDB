package com.zzy.Expression;

import com.zzy.Table.ColumnResolver;
import com.zzy.Value.Value;
import com.zzy.engine.SQLConnection;

/**
 * 用以表示value值的封装
 * 比如 where name = 'peter' 中, peter这个值
 */
public class ValueExpression extends Expression {
   // private static final Object NULL = new ValueExpression(ValueNull.INSTANCE);
   // private static final Object DEFAULT = new ValueExpression(ValueNull.INSTANCE);
    private final Value value;

    public ValueExpression(Value value) {
        this.value = value;
    }

    public static ValueExpression getNull() {
        return null;
    }

    public static ValueExpression getDefault() {
        return null;
    }

    // 生成对象
    public static ValueExpression get(Value value) {
        return new ValueExpression(value);
    }

    // 获取value的类型
    public int getType() {
        return value.getType();
    }

    @Override
    public Value getValue(SQLConnection connection) {
        return value;
    }

    @Override
    public void mapColumns(ColumnResolver resolver, int level) { }
}
