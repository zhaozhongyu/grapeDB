package com.zzy.engine;

import com.zzy.Schema.Schema;

import java.sql.*;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class SQLDriver implements Driver
{
    static final String URL_PREFIX = "jdbc:zzydb";
    static SQLDriver driver;
    static Schema schema; //本来应该是启动服务器的时候创建schema实例, 现在demo版本, 就放到Driver里面创建好了

    static {
        try{
            driver = new SQLDriver();
            java.sql.DriverManager.registerDriver(driver);
            schema = new Schema(false, "data");
        }catch(Throwable e){
            e.printStackTrace();
        }
    }

    @Override public Connection connect(String url, Properties info) throws SQLException
    {
        if(!acceptsURL(url)){
            return null;
        }
        return new SQLConnection();
    }

    @Override public boolean acceptsURL(String url) throws SQLException
    {
        return url.startsWith(URL_PREFIX);
    }

    @Override public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
            throws SQLException
    {
        return new DriverPropertyInfo[0];
    }

    @Override public int getMajorVersion()
    {
        return 0;
    }

    @Override public int getMinorVersion()
    {
        return 0;
    }

    @Override public boolean jdbcCompliant()
    {
        return false;
    }

    @Override public Logger getParentLogger() throws SQLFeatureNotSupportedException
    {
        return null;
    }

    private Properties parse(String url, Properties info) throws SQLException {
        Properties props = (Properties)info.clone();
        if(!acceptsURL(url)){
            return props;
        }
        int idx1 = url.indexOf(':', 5); // search after "jdbc:"
        int idx2 = url.indexOf('?');
        if(idx1 > 0){
            String dbPath = (idx2 > 0) ? url.substring(idx1 + 1, idx2) : url.substring(idx1 + 1);
            props.setProperty("dbpath", dbPath);
        }
        if(idx2 > 0){
            String propsString = url.substring(idx2 + 1).replace('&', ';');
            StringTokenizer tok = new StringTokenizer(propsString, ";");
            while(tok.hasMoreTokens()){
                String keyValue = tok.nextToken().trim();
                if(keyValue.length() > 0){
                    idx1 = keyValue.indexOf('=');
                    if(idx1 > 0){
                        String key = keyValue.substring(0, idx1).toLowerCase().trim();
                        String value = keyValue.substring(idx1 + 1).trim();
                        props.put(key, value);
                    }else{
                        throw new RuntimeException("Missing equal in property.");
                    }
                }
            }
        }
        return props;
    }
}
