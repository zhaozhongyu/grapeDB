
    private void recursion_handler_after_deletion(INode curretNode) {
    }

    //递归处理删除后问题
    private void recursion_handler_firstOneDelete(INode childNode, INode currentNode, Comparable firstIndexNO) {
        if(currentNode != null) {
            INode parentNode = currentNode.parent;
            if(currentNode.isLeaf) {
                if(parentNode != null) {
                    Comparable myFirst = (Comparable)currentNode.keys.get(0);
                    parentNode.childNodes.indexOf(currentNode);
                    this.recursion_handler_firstOneDelete(currentNode, parentNode, myFirst);
                }

            } else {
                int childIndexLoc = currentNode.childNodes.indexOf(childNode);
                if(childIndexLoc != -1) {
                    if(!currentNode.keys.get(childIndexLoc).equals(firstIndexNO)) {
                        Comparable cIndexNO;
                        if(childIndexLoc > 0) {
                            currentNode.keys.remove(childIndexLoc);
                            currentNode.keys.add(childIndexLoc, firstIndexNO);
                            if(parentNode != null) {
                                cIndexNO = (Comparable)currentNode.keys.get(0);
                                this.recursion_handler_firstOneDelete(currentNode, parentNode, cIndexNO);
                            }

                        } else if(childIndexLoc == 0) {
                            currentNode.keys.remove(0);
                            currentNode.keys.add(0, firstIndexNO);
                            cIndexNO = (Comparable)currentNode.keys.get(0);
                            this.recursion_handler_firstOneDelete(currentNode, parentNode, cIndexNO);
                        }
                    }
                }
            }
        }
    }

    //获取一个indexNo在INode中的位置
    private int getIndexLocation(INode currentNode, Comparable indexNO) {
        int indexLoc = -1;
        if(currentNode == null) {
            return indexLoc;
        } else {
            int cindex = 0;

            for(Iterator var6 = currentNode.keys.iterator(); var6.hasNext(); ++cindex) {
                Comparable f1 = (Comparable)var6.next();
                if(f1.equals(indexNO)) {
                    break;
                }
            }

            return cindex;
        }
    }

    private void recursion_combination(INode currentNode) {
        if(currentNode != null) {
            INode parentNode = currentNode.parent;
            if(currentNode.keys.size() < this._min) {
                if(currentNode.keys.size() == 1 && parentNode == null) {
                    this._rootNode = (INode)currentNode.childNodes.get(0);
                    this._rootNode.parent = null;
                } else if(parentNode != null || currentNode.keys.size() < 2) {
                    INode leftBrother = null;
                    INode rightBrother = null;
                    int theCPindex = parentNode.childNodes.indexOf(currentNode);
                    if(theCPindex != -1) {
                        if(theCPindex == 0) {
                            rightBrother = (INode)parentNode.childNodes.get(1);
                        } else if(theCPindex == parentNode.childNodes.size() - 1) {
                            leftBrother = (INode)parentNode.childNodes.get(theCPindex - 1);
                        } else {
                            leftBrother = (INode)parentNode.childNodes.get(theCPindex - 1);
                            rightBrother = (INode)parentNode.childNodes.get(theCPindex + 1);
                        }

                        if(leftBrother != null && leftBrother.keys.size() > this._min) {
                            currentNode.keys.add(0, (Comparable)leftBrother.keys.get(leftBrother.keys.size() - 1));
                            currentNode.childNodes.add(0, (INode)leftBrother.childNodes.get(leftBrother.childNodes.size() - 1));
                            ((INode)currentNode.childNodes.get(0)).parent = currentNode;
                            leftBrother.keys.remove(leftBrother.keys.size() - 1);
                            leftBrother.childNodes.remove(leftBrother.childNodes.size() - 1);
                            parentNode.keys.remove(theCPindex);
                            parentNode.keys.add(theCPindex, (Comparable)currentNode.keys.get(0));
                        } else if(rightBrother != null && rightBrother.keys.size() > this._min) {
                            currentNode.keys.add((Comparable)rightBrother.keys.get(0));
                            currentNode.childNodes.add((INode)rightBrother.childNodes.get(0));
                            ((INode)currentNode.childNodes.get(currentNode.childNodes.size() - 1)).parent = currentNode;
                            rightBrother.keys.remove(0);
                            rightBrother.childNodes.remove(0);
                            parentNode.keys.remove(theCPindex + 1);
                            parentNode.keys.add(theCPindex + 1, (Comparable)rightBrother.keys.get(0));
                        } else {
                            Comparable key1;
                            Iterator var7;
                            INode tmpNode;
                            if(leftBrother != null) {
                                var7 = currentNode.keys.iterator();

                                while(var7.hasNext()) {
                                    key1 = (Comparable)var7.next();
                                    leftBrother.keys.add(key1);
                                }

                                var7 = currentNode.childNodes.iterator();

                                while(var7.hasNext()) {
                                    tmpNode = (INode)var7.next();
                                    tmpNode.parent = leftBrother;
                                    leftBrother.childNodes.add(tmpNode);
                                }

                                parentNode.keys.remove(theCPindex);
                                parentNode.childNodes.remove(theCPindex);
                                this.recursion_combination(parentNode);
                            } else if(rightBrother != null) {
                                var7 = rightBrother.keys.iterator();

                                while(var7.hasNext()) {
                                    key1 = (Comparable)var7.next();
                                    currentNode.keys.add(key1);
                                }

                                var7 = rightBrother.childNodes.iterator();

                                while(var7.hasNext()) {
                                    tmpNode = (INode)var7.next();
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
        }
    }

    private void recursion_search_first_leaf(INode currNode) {
        if(currNode != null) {
            if(currNode.isLeaf) {
                this._leaf_tmp = (TreeLeaf)currNode;
            } else if(currNode.childNodes.size() > 0) {
                this.recursion_search_first_leaf((INode)currNode.childNodes.get(0));
            }
        }
    }

    public TreeLeaf getFirstLeaf() {
        this._leaf_tmp = null;
        this.recursion_search_first_leaf(this._rootNode);
        return this._leaf_tmp;
    }

    public ArrayList<Comparable> getAllKeyList() {
        ArrayList<Comparable> flist = new ArrayList();

        for(TreeLeaf fLeaf = this.getFirstLeaf(); fLeaf != null; fLeaf = fLeaf.rightBrother) {
            Iterator var4 = fLeaf.keys.iterator();

            while(var4.hasNext()) {
                Comparable f2 = (Comparable)var4.next();
                flist.add(f2);
            }
        }

        return flist;
    }

    /**/
    public boolean insert(Comparable indexNO, Object value) {
        TreeLeaf theLeaf;
        int indexLOC;
        if(this._rootNode.childNodes.size() <= 0) {
            if(!this._rootNode.isLeaf) {
                theLeaf = new TreeLeaf();
                theLeaf.isLeaf = true;
                Iterator iterator = this._rootNode.keys.iterator();

                while(iterator.hasNext()) {
                    Comparable var8 = (Comparable)iterator.next();
                    theLeaf.keys.add(var8);
                }

                this._rootNode = theLeaf;
            }

            int var7 = -1;
            indexLOC = 0;
            Iterator iterator = this._rootNode.keys.iterator();

            while(true) {
                if(iterator.hasNext()) {
                    Comparable var10 = (Comparable)iterator.next();
                    if(var10.equals(indexNO) ){
                        return false;
                    }

                    if(indexNO.compareTo(var10) > 0) {
                        var7 = indexLOC;
                    }

                    if(indexNO.compareTo(var10) >= 0) {
                        ++indexLOC;
                        continue;
                    }
                }

                this._rootNode.keys.add(var7 + 1, indexNO);
                ((TreeLeaf)this._rootNode).values.add(var7 + 1, value);
                this.recurse_division_after_insert(this._rootNode);
                return true;
            }
        } else {
            theLeaf = this.recursion_search_suitable_leaf(this._rootNode, indexNO);
            if(theLeaf == null) {
                return false;
            } else {
                indexLOC = -1;
                int cindex = 0;
                Iterator iterator = theLeaf.keys.iterator();

                while(true) {
                    if(iterator.hasNext()) {
                        Comparable f1 = (Comparable)iterator.next();
                        if(f1.equals(indexNO)) {
                            return false;
                        }

                        if(indexNO.compareTo(f1) > 0) {
                            indexLOC = cindex;
                        }

                        if(indexNO.compareTo(f1) >= 0) {
                            ++cindex;
                            continue;
                        }
                    }

                    this.insertIndexNO(theLeaf, indexNO, value);
                    if(indexLOC == -1) {
                        this.recursion_changeMinimun(theLeaf, indexNO);
                    }

                    this.recurse_division_after_insert(theLeaf);
                    return true;
                }
            }
        }
    }

    private boolean insertIndexNO(INode currentNode, Comparable indexNO, Object value) {
        if(currentNode == null) {
            return false;
        } else {
            int indexLOC = -1;
            int cindex = 0;
            Iterator iterator = currentNode.keys.iterator();

            while(true) {
                if(iterator.hasNext()) {
                    Comparable f1 = (Comparable) iterator.next();
                    if(f1.equals(indexNO)) {
                        return false;
                    }

                    if(indexNO.compareTo(f1) > 0) {
                        indexLOC = cindex;
                    }

                    if(indexNO.compareTo(f1) >= 0) {
                        ++cindex;
                        continue;
                    }
                }

                currentNode.keys.add(indexLOC + 1, indexNO);
                ((TreeLeaf)currentNode).values.add(indexLOC+1, value);
                return true;
            }
        }
    }
}
