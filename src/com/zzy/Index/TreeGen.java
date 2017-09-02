package com.zzy.Index;

import java.util.ArrayList;
import java.util.Iterator;

public class TreeGen {

    private int _m = 4;
    private int _min = 2;
    private INode _rootNode = new TreeLeaf();
    private INode _research_result = null; //存放查找结果
    private TreeLeaf _leaf_tmp = null;

    public int getM() {
        return this._m;
    }

    public int getMin() {
        return this._min;
    }

    public TreeGen(int m) { //创建一个M阶B+树
        this._m = m;
        this._min = (int) Math.ceil((double) this._m / 2.0D);
    }

    //获取根节点
    public INode getRootNode() {
        return this._rootNode;
    }

    //查找指定节点, 返回的是INode, 通常为TreeLeaf
    public INode search(Comparable indexNO) {
        this._research_result = null;
        this.recursion_to_serach(this._rootNode, indexNO);
        return this._research_result;
    }

    //递归查找存在指定值的叶子节点
    public void recursion_to_serach(INode currentNode, Comparable indexNO){
        if (currentNode == null) {
            return;
        }
        int cindex;
        Comparable key;
        Iterator iterator;
        if (!currentNode.isLeaf && currentNode.childNodes.size() > 0) { //如果非叶子节点并且存在子节点(非叶子节点必然存在子节点, 不然就是bug)
            int indexLoc = -1;
            cindex = 0;
            for (iterator = currentNode.keys.iterator(); iterator.hasNext(); ++cindex) {
                key = (Comparable) iterator.next();
                if (indexNO.compareTo(key) < 0) {
                    break;
                }
                if (indexNO.compareTo(key) >= 0) {
                    indexLoc = cindex;
                }
            }
            if (indexLoc != -1) {
                this.recursion_to_serach((INode) currentNode.childNodes.get(indexLoc), indexNO);
            }
        } else { //如果是叶子节点或者没有子节点
            cindex = 0;
            for (iterator = currentNode.keys.iterator(); iterator.hasNext(); ++cindex) {
                key = (Comparable) iterator.next();
                if (indexNO.equals(key)) {
                    this._research_result = currentNode;
                    break;
                }
            }
        }

    }

    //插入
    public boolean insert(Comparable indexNO, Object value) {
        TreeLeaf theLeaf;
        int indexLOC;
        if (this._rootNode.childNodes.size() <= 0) { //如果根节点没有子节点, 即根节点就是叶子节点
            Comparable key;
            for(int i = 0; i < _rootNode.keys.size(); i++){
                key = _rootNode.keys.get(i);
                if(key.equals(indexNO)){
                    return false; //已存在值
                }
                if(indexNO.compareTo(key) < 0){//如果当前这个key已经大于indexNO, 则把indexNO插在key的位置上, key后移一位
                    _rootNode.keys.add( i, indexNO);
                    ((TreeLeaf)_rootNode).values.add( i, value);
                    this.recurse_division_after_insert(this._rootNode);
                    return true;
                }
            }
            _rootNode.keys.add(indexNO); //走到这里说明indexNo比所有的key都要大
            ((TreeLeaf) this._rootNode).values.add(value);
            this.recurse_division_after_insert(this._rootNode);
            return true;
        } else { //如果根节点不是叶子节点
            theLeaf = this.recursion_search_suitable_leaf(this._rootNode, indexNO); //查询匹配的叶子节点
            if (theLeaf == null) {
                return false;
            }
            indexLOC = -1;
            int cindex = 0;
            Iterator iterator = theLeaf.keys.iterator();
            while (true) {
                if (iterator.hasNext()) {
                    Comparable f1 = (Comparable) iterator.next();
                    if (f1.equals(indexNO)) {
                        return false;
                    }
                    if (indexNO.compareTo(f1) > 0) {
                        indexLOC = cindex;
                    }
                    if (indexNO.compareTo(f1) >= 0) {
                        ++cindex;
                        continue;
                    }
                }
                this.insertIndexNO(theLeaf, indexNO, value);
                if (indexLOC == -1) {
                    this.recursion_changeMinimun(theLeaf, indexNO);
                }
                this.recurse_division_after_insert(theLeaf);
                return true;
            }
        }
    }

    //递归处理插入后的节点
    private void recurse_division_after_insert(INode currentNode) {
        if (currentNode.keys.size() > this._m) { //如果插入后需要分裂
            TreeLeaf currentLeaf ;
            INode parentNode = currentNode.parent;
            int theOriginTotal;
            int cLindex;
            TreeNode theRoot;
            if (currentNode.isLeaf) { //如果当前节点是叶子节点
                currentLeaf = (TreeLeaf) currentNode;
                TreeLeaf rightLeaf = new TreeLeaf();
                rightLeaf.parent = currentLeaf.parent;
                rightLeaf.rightBrother = currentLeaf.rightBrother;
                currentLeaf.rightBrother = rightLeaf;

                for(int i = this._min; i < currentLeaf.keys.size(); i++){
                    rightLeaf.keys.add(currentLeaf.keys.get(i));
                    rightLeaf.values.add(currentLeaf.values.get(i));
                }

                theOriginTotal = currentLeaf.keys.size();
                for (cLindex = theOriginTotal - 1; cLindex >= this._min; --cLindex) { //分两个循环来加减是出于对ArrayList的效率考虑
                    currentLeaf.keys.remove(cLindex);
                    currentLeaf.values.remove(cLindex);
                }

                if (currentLeaf.parent == null) { //如果当前节点就是根节点, 则新建一个根节点进行分解
                    theRoot = new TreeNode();
                    theRoot.keys.add( currentLeaf.keys.get(0));
                    theRoot.keys.add( rightLeaf.keys.get(0));
                    theRoot.childNodes.add(currentLeaf);
                    theRoot.childNodes.add(rightLeaf);
                    currentLeaf.parent = theRoot;
                    rightLeaf.parent = theRoot;
                    this._rootNode = theRoot;
                } else { //如果当前节点不是根节点, 则递归处理
                    cLindex = parentNode.childNodes.indexOf(currentNode);
                    parentNode.keys.add(cLindex + 1, rightLeaf.keys.get(0));
                    parentNode.childNodes.add(cLindex + 1, rightLeaf);
                    this.recurse_division_after_insert(parentNode);
                }
            } else { //如果当前节点不是叶子节点
                TreeNode normalNode = (TreeNode) currentNode;
                TreeNode rightBrother = new TreeNode();
                rightBrother.parent = parentNode;
                theOriginTotal = normalNode.keys.size();

                for (cLindex = this._min; cLindex <= theOriginTotal - 1; ++cLindex) {
                    rightBrother.keys.add(normalNode.keys.get(cLindex));
                    ( normalNode.childNodes.get(cLindex)).parent = rightBrother;
                    rightBrother.childNodes.add( normalNode.childNodes.get(cLindex));
                }

                for (cLindex = theOriginTotal - 1; cLindex >= this._min; --cLindex) {
                    normalNode.childNodes.remove(cLindex);
                    normalNode.keys.remove(cLindex);
                }

                if (parentNode == null) { //如果当前节点是根节点
                    theRoot = new TreeNode();
                    theRoot.keys.add(normalNode.keys.get(0));
                    theRoot.keys.add( rightBrother.keys.get(0));
                    theRoot.childNodes.add(normalNode);
                    theRoot.childNodes.add(rightBrother);
                    normalNode.parent = theRoot;
                    rightBrother.parent = theRoot;
                    this._rootNode = theRoot;
                } else {
                    cLindex = parentNode.childNodes.indexOf(normalNode);
                    parentNode.keys.add(cLindex + 1, (Comparable) rightBrother.keys.get(0));
                    parentNode.childNodes.add(cLindex + 1, rightBrother);
                    this.recurse_division_after_insert(parentNode);
                }
            }
        }
    }

    //递归查找, 如果不存在则返回最左边叶子节点(即待查找的关键字比当前所有关键字更小)
    private TreeLeaf recursion_search_suitable_leaf(INode currentNode, Comparable indexNO) {
        if (currentNode == null) {
            return null;
        }
        if (!currentNode.isLeaf && currentNode.childNodes.size() > 0) {
            int indexLoc = -1;
            int cindex = 0;
            for (Iterator iterator = currentNode.keys.iterator(); iterator.hasNext(); ++cindex) {
                Comparable iNO = (Comparable) iterator.next();
                if (indexNO.compareTo(iNO) < 0) {
                    break;
                }
                if (indexNO.compareTo(iNO) > 0) {
                    indexLoc = cindex;
                }
                if (indexNO.equals(iNO)) {
                    return null;
                }
            }
            return indexLoc == -1 ? this.recursion_getLeftLeaf(currentNode) : this.recursion_search_suitable_leaf(currentNode.childNodes.get(indexLoc), indexNO);
        } else {
            return (TreeLeaf) currentNode;
        }
    }

    //获取最左边叶子节点
    private TreeLeaf recursion_getLeftLeaf(INode currentNode) {
        if(currentNode == null){
            return null;
        }
        if(currentNode.isLeaf){
            return (TreeLeaf)currentNode;
        }
        return recursion_getLeftLeaf(currentNode.childNodes.get(0)); //非叶子节点必然有子节点
    }

    //递归修改最小值, 当插入的值是整棵树的最小值时, 需要删除原有的最小值关键字并插入新的最小值关键字
    private void recursion_changeMinimun(INode currentNode, Comparable indexNO) {
        if (currentNode != null) {
            if (!currentNode.keys.get(0).equals(indexNO)) {
                currentNode.keys.remove(0);
                currentNode.keys.add(0, indexNO);
            }
            this.recursion_changeMinimun(currentNode.parent, indexNO);
        }
    }

    //将value值插入leaf节点
    private boolean insertIndexNO(INode currentNode, Comparable indexNO, Object value) {
        if (currentNode == null) {
            return false;
        }
        Comparable key;
        for(int i = 0; i < currentNode.keys.size(); i++){
            key = currentNode.keys.get(i);
            if(indexNO.equals(key)){
                return false;
            }
            if(indexNO.compareTo(key) < 0){ //如果当前这个key已经大于indexNO, 则把indexNO插在key的位置上, key后移一位
                currentNode.keys.add(i, indexNO);
                ((TreeLeaf)currentNode).values.add(i, value);
                return true;
            }
        }
        currentNode.keys.add(indexNO); //如果没有比indexNO大的, 则加到最后面
        ((TreeLeaf)currentNode).values.add(value);
        return true;
    }

    public boolean update(Comparable indexNO, Object value){
        TreeLeaf theLeaf;
        Comparable key;
        if (this._rootNode.childNodes.size() <= 0) { //如果根节点没有子节点, 即根节点就是叶子节点
            for(int i = 0; i < _rootNode.keys.size(); i++){
                key = _rootNode.keys.get(i);
                if(key.equals(indexNO)){
                    ((TreeLeaf)_rootNode).values.remove(i); //删除原来位置上的value并插入新的value
                    ((TreeLeaf)_rootNode).values.add(i, value);
                    return true; //存在值
                }
            }
            return false; //倘若不存在值
        } else { //如果根节点不是叶子节点
            theLeaf = this.recursion_search_suitable_leaf(this._rootNode, indexNO); //查询匹配的叶子节点
            if (theLeaf == null) {
                return false;
            }
            for(int i = 0; i < theLeaf.keys.size(); i++){
                key = theLeaf.keys.get(i);
                if(key.equals(indexNO)){
                    theLeaf.values.remove(i); //删除原来位置上的value并插入新的value
                    theLeaf.values.add(i, value);
                    return true; //存在值
                }
            }
            return false; //倘若不存在值
        }
    }


    //删除节点
    public boolean delete(Comparable indexNO){
        INode currentNode = this.search(indexNO);//先查找, 查找不到就退出
        if (currentNode == null) {
            return false;
        }
        int indexLoc;
        if (currentNode.parent == null && currentNode.childNodes.size() <= 0) { //如果是根节点且是叶子节点
            indexLoc = this.getIndexLocation(currentNode, indexNO);
            if (indexLoc == -1) {
                return false;
            }
            currentNode.keys.remove(indexLoc); //走到这里当前的INode为TreeLeaf
            ((TreeLeaf) currentNode).values.remove(indexLoc);
            return true;
        }
        if (currentNode.childNodes.size() > 0) { //如果不是叶子节点
            return false;
        }
        if (currentNode.keys.size() > this._min) { //如果当前节点的keys数量大于_min, 即不需要进行重新分配, 走到这里必然是叶子节点
            indexLoc = this.getIndexLocation(currentNode, indexNO);
            if (indexLoc == 0) {
                currentNode.keys.remove(indexLoc);
                ((TreeLeaf) currentNode).values.remove(indexLoc);
                this.recursion_handler_firstOneDelete( null, currentNode, 0.0F); //如果是第一个key. 则需要递归更新父节点的key
                return true;
            } else {
                currentNode.keys.remove(indexLoc);
                ((TreeLeaf) currentNode).values.remove(indexLoc);
                return true;
            }
        } else { //走到这里说明删除完成后需要进行结构调整
            INode parentNode = currentNode.parent;
            indexLoc = this.getIndexLocation(currentNode, indexNO);
            int cNodePindex = parentNode.childNodes.indexOf(currentNode);
            if (cNodePindex == -1) {
                return false;
            }
            INode leftBrother = null;
            INode rightBrother = ((TreeLeaf) currentNode).rightBrother;
            if (cNodePindex > 0) {
                leftBrother = parentNode.childNodes.get(cNodePindex - 1);
            }
            if (leftBrother != null && leftBrother.keys.size() > this._min) { //如果leftbrother的keys大于最小值, 则从leftBrother中拆借最右边的那个值
                currentNode.keys.remove(indexLoc);
                currentNode.keys.add(0, leftBrother.keys.get(leftBrother.keys.size() - 1));
                leftBrother.keys.remove(leftBrother.keys.size() - 1);
                ((TreeLeaf) currentNode).values.remove(indexLoc);
                ((TreeLeaf) currentNode).values.add(0, ((TreeLeaf) leftBrother).values.get(leftBrother.keys.size() - 1));
                ((TreeLeaf) leftBrother).values.remove(leftBrother.keys.size() - 1);

                this.recursion_handler_firstOneDelete( null, currentNode, 0.0F); //currentNode的最小值更改了, 因此进行递归修改父节点
                return true;
            } else if (rightBrother != null && rightBrother.keys.size() > this._min) { //如果rightbrother的keys大于最小值, 则从rightbrother中拆借最左边的那个值
                currentNode.keys.remove(indexLoc);
                currentNode.keys.add((Comparable) rightBrother.keys.get(0));
                rightBrother.keys.remove(0);
                ((TreeLeaf) currentNode).values.remove(indexLoc);
                ((TreeLeaf) currentNode).values.add((Comparable) rightBrother.keys.get(0));
                ((TreeLeaf) rightBrother).values.remove(0);

                this.recursion_handler_firstOneDelete((INode) null, rightBrother, 0.0F); //递归处理rightBrother的最小节点被拆借的问题
                if (indexLoc == 0) {
                    this.recursion_handler_firstOneDelete((INode) null, currentNode, 0.0F);//递归处理当前节点的最小节点被删除的问题
                }
                return true;
            } else { //如果左右兄弟节点均无法拆借, 即要么不存在, 要么节点数为_min
                Comparable f1;
                Iterator iterator;
                if (leftBrother != null) { //将当前节点的key加入到左节点中, 并且删除当前节点
                    currentNode.keys.remove(indexLoc);
                    ((TreeLeaf) currentNode).values.remove(indexLoc);
                    if (indexLoc == 0) {
                        this.recursion_handler_firstOneDelete((INode) null, currentNode, 0.0F);
                    }

                    iterator = currentNode.keys.iterator();
                    while (iterator.hasNext()) {
                        f1 = (Comparable) iterator.next();
                        leftBrother.keys.add(f1);
                    }

                    ((TreeLeaf) leftBrother).rightBrother = ((TreeLeaf) currentNode).rightBrother;
                    parentNode.keys.remove(cNodePindex);
                    parentNode.childNodes.remove(cNodePindex);
                    this.recursion_combination(parentNode);
                    return true;
                } else if (rightBrother == null) { //走到这里说明没有左节点, 如果再没有右节点, 说明出bug了
                    return false;
                } else { //将右节点中的key加入到当前节点中,并且删除右节点
                    currentNode.keys.remove(indexLoc);
                    ((TreeLeaf) currentNode).values.remove(indexLoc);

                    if (indexLoc == 0) {
                        this.recursion_handler_firstOneDelete((INode) null, currentNode, 0.0F);
                    }

                    iterator = rightBrother.keys.iterator();

                    while (iterator.hasNext()) {
                        f1 = (Comparable) iterator.next();
                        currentNode.keys.add(f1);
                    }
                    ((TreeLeaf) currentNode).rightBrother = ((TreeLeaf) rightBrother).rightBrother;
                    parentNode.keys.remove(cNodePindex + 1);
                    parentNode.childNodes.remove(cNodePindex + 1);
                    this.recursion_combination(parentNode);
                    return true;
                }
            }
        }
    }

    //递归处理删除后问题
    private void recursion_handler_firstOneDelete(INode childNode, INode currentNode, Comparable firstIndexNO) {
        if (currentNode != null) {
            INode parentNode = currentNode.parent;
            if (currentNode.isLeaf) {
                if (parentNode != null) {
                    Comparable myFirst = (Comparable) currentNode.keys.get(0);
                    // parentNode.childNodes.indexOf(currentNode);
                    this.recursion_handler_firstOneDelete(currentNode, parentNode, myFirst);
                }

            } else {
                int childIndexLoc = currentNode.childNodes.indexOf(childNode);
                if (childIndexLoc != -1) {
                    if (!currentNode.keys.get(childIndexLoc).equals(firstIndexNO)) {
                        Comparable cIndexNO;
                        if (childIndexLoc > 0) {
                            currentNode.keys.remove(childIndexLoc);
                            currentNode.keys.add(childIndexLoc, firstIndexNO);
                            if (parentNode != null) {
                                cIndexNO = (Comparable) currentNode.keys.get(0);
                                this.recursion_handler_firstOneDelete(currentNode, parentNode, cIndexNO);
                            }

                        } else if (childIndexLoc == 0) {
                            currentNode.keys.remove(0);
                            currentNode.keys.add(0, firstIndexNO);
                            cIndexNO = (Comparable) currentNode.keys.get(0);
                            this.recursion_handler_firstOneDelete(currentNode, parentNode, cIndexNO);
                        }
                    }
                }
            }
        }
    }

    //获取一个indexNo在INode中的位置, 如果找不到, 则返回当前INode的size + 1
    private int getIndexLocation(INode currentNode, Comparable indexNO) {
        if (currentNode == null) {
            return -1;
        }
        int cindex ;
        for (cindex = 0; cindex < currentNode.keys.size(); ++cindex) {
            Comparable key = currentNode.keys.get(cindex);
            if (key.equals(indexNO)) {
                return cindex;
            }
        }
        return cindex;
    }

    //递归处理合并子节点后的问题(即当前节点的子节点可能少于最小值)
    private void recursion_combination(INode currentNode) {
        if (currentNode == null) {  return; }
        INode parentNode = currentNode.parent;
        if (currentNode.keys.size() >= this._min){
            return ;
        }
        //如果需要进行调整
        if (currentNode.keys.size() == 1 && parentNode == null) { //如果当前节点是根节点, 并且下面只有一个子节点(只在_min=2的情况下出现)
            this._rootNode = (INode) currentNode.childNodes.get(0);
            this._rootNode.parent = null;
        } else if (parentNode != null ) { //如果父节点不为空
            INode leftBrother = null;
            INode rightBrother = null;
            int theCPindex = parentNode.childNodes.indexOf(currentNode);
            if (theCPindex != -1) {
                if (theCPindex == 0) { //只有右节点
                    rightBrother = (INode) parentNode.childNodes.get(1);
                } else if (theCPindex == parentNode.childNodes.size() - 1) { //只有左节点
                    leftBrother = (INode) parentNode.childNodes.get(theCPindex - 1);
                } else { //存在左右节点
                    leftBrother = (INode) parentNode.childNodes.get(theCPindex - 1);
                    rightBrother = (INode) parentNode.childNodes.get(theCPindex + 1);
                }

                if (leftBrother != null && leftBrother.keys.size() > this._min) { //如果可以拆借左节点
                    currentNode.keys.add(0, (Comparable) leftBrother.keys.get(leftBrother.keys.size() - 1));
                    currentNode.childNodes.add(0, (INode) leftBrother.childNodes.get(leftBrother.childNodes.size() - 1));
                    ((INode) currentNode.childNodes.get(0)).parent = currentNode;
                    leftBrother.keys.remove(leftBrother.keys.size() - 1);
                    leftBrother.childNodes.remove(leftBrother.childNodes.size() - 1);
                    parentNode.keys.remove(theCPindex);
                    parentNode.keys.add(theCPindex, (Comparable) currentNode.keys.get(0));
                } else if (rightBrother != null && rightBrother.keys.size() > this._min) { //如果可以拆借右节点
                    currentNode.keys.add((Comparable) rightBrother.keys.get(0));
                    currentNode.childNodes.add((INode) rightBrother.childNodes.get(0));
                    ((INode) currentNode.childNodes.get(currentNode.childNodes.size() - 1)).parent = currentNode;
                    rightBrother.keys.remove(0);
                    rightBrother.childNodes.remove(0);
                    parentNode.keys.remove(theCPindex + 1);
                    parentNode.keys.add(theCPindex + 1, (Comparable) rightBrother.keys.get(0));
                } else { //如果左右节点均无法拆借, 则选择合并
                    Comparable key1;
                    Iterator var7;
                    INode tmpNode;
                    if (leftBrother != null) { //合并到左节点
                        var7 = currentNode.keys.iterator();
                        while (var7.hasNext()) {
                            key1 = (Comparable) var7.next();
                            leftBrother.keys.add(key1);
                        }
                        var7 = currentNode.childNodes.iterator();
                        while (var7.hasNext()) {
                            tmpNode = (INode) var7.next();
                            tmpNode.parent = leftBrother;
                            leftBrother.childNodes.add(tmpNode);
                        }
                        parentNode.keys.remove(theCPindex);
                        parentNode.childNodes.remove(theCPindex);
                        this.recursion_combination(parentNode);
                    } else if (rightBrother != null) { //合并到右节点
                        var7 = rightBrother.keys.iterator();
                        while (var7.hasNext()) {
                            key1 = (Comparable) var7.next();
                            currentNode.keys.add(key1);
                        }
                        var7 = rightBrother.childNodes.iterator();
                        while (var7.hasNext()) {
                            tmpNode = (INode) var7.next();
                            tmpNode.parent = currentNode;
                            currentNode.childNodes.add(tmpNode);
                        }

                        parentNode.keys.remove(theCPindex + 1);
                        parentNode.childNodes.remove(theCPindex + 1);
                        this.recursion_combination(parentNode);
                    }
                }
            }
        }
    }

    public TreeLeaf getFirstLeaf() {
        this._leaf_tmp = null;
        this.recursion_search_first_leaf(this._rootNode);
        return this._leaf_tmp;
    }

    private void recursion_search_first_leaf(INode currNode) {
        if (currNode != null) {
            if (currNode.isLeaf) {
                this._leaf_tmp = (TreeLeaf) currNode;
            } else if (currNode.childNodes.size() > 0) {
                this.recursion_search_first_leaf(currNode.childNodes.get(0));
            }
        }
    }
    
    public void set_rootNode(INode node){
        this._rootNode = node;
    }
}
