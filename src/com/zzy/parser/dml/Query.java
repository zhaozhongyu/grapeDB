package com.zzy.parser.dml;

import com.zzy.Expression.Expression;
import com.zzy.Table.Table;
import com.zzy.engine.SQLConnection;
import com.zzy.engine.SQLResultSet;
import com.zzy.engine.SQLStatement;
import com.zzy.parser.Prepared;
import com.zzy.result.LocalResult;

import java.util.ArrayList;

/**
 *
 */
public abstract class Query extends Prepared
{


    private Table table;
    private Expression expression;
    private ArrayList list ;
    private SQLResultSet resultSet;

    // for limit
    protected Expression offsetExpr;
    // for limit
    protected Expression limitExpr;

    private LocalResult lastResult;

    protected Query(SQLConnection connection) {
        super(connection);
    }

    public SQLResultSet query() {
        return null;
    }

    /**
     * Initialize the query.
     */
    public abstract void init();

    /**
     * Execute the query, writing the result to the target result.
     *
     * @return
     */
    public SQLResultSet execute(SQLStatement statement) {
        this.resultSet = executeImpl(statement);
        return resultSet;
    }

    protected abstract SQLResultSet executeImpl(SQLStatement statement);

    public void setLimit(Expression limit) {
        this.limitExpr = limit;
    }

    public void setOffset(Expression offset) {
        this.offsetExpr = offset;
    }

    public Expression getLimitExpr() {
        return limitExpr;
    }

    public void setLimitExpr(Expression limitExpr) {
        this.limitExpr = limitExpr;
    }

    public abstract int getColumnCount();

    public abstract ArrayList<Expression> getExpressions();


}
