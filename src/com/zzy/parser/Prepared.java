package com.zzy.parser;


import com.zzy.engine.SQLConnection;
import com.zzy.engine.SQLResultSet;
import com.zzy.engine.SQLStatement;

public abstract class Prepared {
    protected SQLConnection connection;
    private int objectId;
    //  protected String sqlStatement;
    protected boolean create = true;

    public Prepared(SQLConnection connection) {
        this.connection = connection;
    }

    public boolean isQuery() {
        return false;
    }

    public abstract SQLResultSet execute(SQLStatement statement);

    public int update() {
        throw new RuntimeException("Method not allowed for query");
    }


}
