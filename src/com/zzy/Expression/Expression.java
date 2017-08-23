package com.zzy.Expression;

import com.zzy.Table.ColumnResolver;
import com.zzy.Table.TableFilter;
import com.zzy.Value.Value;
import com.zzy.engine.SQLConnection;


/**
 * 表达式处理
 *
 * */
public abstract class Expression
{

    private boolean addedToFilter;
    public abstract int getType();
    // 获取当前expression的value
    public abstract Value getValue(SQLConnection connection);

    public abstract void mapColumns(ColumnResolver resolver, int level);

    public String getTableName() {
        return null;
    }

    public String getColumnName() {
        return null;
    }

    public String getTableAlias() {
        return null;
    }

    public void addFilterConditions(TableFilter filter) {
        filter.addFilterCondition(this, false);
        addedToFilter = true;
    }

    /**
     * 是否通配符, 只有_ 和 % 被识别为通配符
     * @return
     */
    public boolean isWildcard() {
        return false;
    }
}
