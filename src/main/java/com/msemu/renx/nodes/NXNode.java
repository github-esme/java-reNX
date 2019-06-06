/*
 * MIT License
 *
 * Copyright (c) 2019 msemu
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.msemu.renx.nodes;

import com.msemu.renx.INXObject;
import com.msemu.renx.NXFile;
import com.msemu.renx.NXNodeType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public abstract class NXNode<T> implements Iterable<NXNode<?>>, INXObject {

    public static final long SIZE = 52;
    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PUBLIC)
    protected long nodeDataOffset;

    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PUBLIC)
    protected long nodeNameId;

    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PUBLIC)
    protected long firstChildId;

    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PUBLIC)
    private int childCount;

    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PUBLIC)
    private byte[] data;

    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    private NXFile nxFile;

    @Getter(AccessLevel.PROTECTED)
    private final Map<String, NXNode<?>> childNodes;

    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    private T cached;

    @Getter(AccessLevel.PROTECTED)
    private final ReentrantLock lock;

    @Getter
    private final NXNodeType type;

    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PRIVATE)
    private boolean parsed;

    public NXNode(NXFile nxFile, NXNodeType type, long nodeDataOffset) {
        this.nxFile = nxFile;
        this.type = type;
        this.nodeDataOffset = nodeDataOffset;
        this.data = new byte[8];
        this.lock = new ReentrantLock();
        this.childNodes = new HashMap<>();
    }

    public static NXNode ParseNode(NXFile nxFile, long nodeDataOffset) {
        MappedByteBuffer buffer = null;
        NXNode retNode = null;
        try {
            buffer = nxFile.getMappedBuffer(nodeDataOffset, NXNode.SIZE);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            long nodeNameId = buffer.getInt();
            long firstChildId = buffer.getInt();
            short childCount = buffer.getShort();
            short typeVal = buffer.getShort();
            NXNodeType type = NXNodeType.getByValue(typeVal);
            switch (type) {
                case INT64:
                    retNode = new NXInt64Node(nxFile, nodeDataOffset);
                    break;
                case STRING:
                    retNode = new NXStringNode(nxFile, nodeDataOffset);
                    break;
                case DOUBLE:
                    retNode = new NXDoubleNode(nxFile, nodeDataOffset);
                    break;
                case NONE:
                default:
                    retNode = new NXNoneNode(nxFile, nodeDataOffset);
                    break;
            }
            retNode.setNodeNameId(nodeDataOffset);
            retNode.setNodeNameId(nodeNameId);
            retNode.setFirstChildId(firstChildId);
            retNode.setChildCount(childCount);
            buffer.get(retNode.getData());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return retNode;
    }

    @Override
    public Iterator<NXNode<?>> iterator() {
        if (!this.isParsed()) this.loadChildNodes();
        return this.getChildNodes().values().iterator();
    }

    @Override
    public void dispose() {
        this.forEach(NXNode::dispose);
        this.getChildNodes().clear();
        this.setNxFile(null);
    }

    public String getName() {
        return this.getNxFile().getString(this.getNodeNameId());
    }

    public NXNode<?> getChild(String name) {
        if (!this.isParsed()) this.loadChildNodes();
        return this.getChildNodes().getOrDefault(name, null);
    }

    public boolean hasChild(String name) {
        NXNode<?> node = this.getChild(name);
        boolean result =  node != null;
        return result;
    }

    public abstract T getValue();


    protected T getValueWithLock(ILoadValueTask<T> task) {
        this.getLock().lock();
        try {
            if (this.getCached() == null) {
                this.setCached(task.loadValue());
            }
            return this.getCached();
        } finally {
            this.getLock().unlock();
        }
    }

    private void loadChildNodes() {
        for (long nodeId = this.getFirstChildId(); nodeId < this.getFirstChildId() + this.getChildCount(); nodeId++) {
            NXNode node = this.getNxFile().getNode(nodeId);
            this.getChildNodes().put(node.getName(), node);
        }
        this.setParsed(true);
    }

    public NXNode<?> resolvePath(String path) {
        String[] elements = (path.startsWith("/") ? path.substring(1) : path).split("[/\\\\]");
        NXNode node = this;
        for (String element : elements) {
            if (!element.equals("."))
                node = node.getChild(element);
        }
        return node;
    }

    @Override
    public String toString() {
        return this.getName();
    }

}
