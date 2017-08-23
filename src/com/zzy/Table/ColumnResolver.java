package com.zzy.Table;

import com.zzy.Value.Value;

public interface ColumnResolver {

    Column[] getColumns();

    Value getValue(Column column);

    TableFilter getTableFilter();

    String getTableAlias();
}
