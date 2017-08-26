
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
}
