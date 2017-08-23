package com.zzy.Index;


import com.zzy.Table.Row;

/**
 * 游标
 */
public interface Cursor {
    /**
     * Get the current Row
     *
     * @return
     */
    Row get();

    /**
     * move to the next row
     *
     * @return
     */
    boolean next();

    /**
     * move to the previous row
     *
     * @return
     */
    boolean previous();

    /**
     * init
     */
    void init();

    void reset();

}
