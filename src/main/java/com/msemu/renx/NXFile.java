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

package com.msemu.renx;

import com.msemu.renx.nodes.NXNode;
import lombok.Getter;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Getter
public class NXFile implements AutoCloseable, INXObject {

    private final NXHeader nxHeader;

    private final RandomAccessFile nxFile;

    private final Map<Long, NXNode> nodeTable;

    private final Map<Long, String> stringTable;

    private final ReentrantLock lock;


    public NXFile(String path, NXReadSelection flag) throws IOException {
        this.nodeTable = new ConcurrentHashMap<>();
        this.stringTable = new ConcurrentHashMap<>();
        this.nxHeader = new NXHeader();
        this.nxFile = new RandomAccessFile(path, "r");
        this.lock = new ReentrantLock();
        this.parse();
    }

    public MappedByteBuffer getMappedBuffer(long position, long size) throws IOException {
        FileChannel fc = this.getNxFile().getChannel();
        MappedByteBuffer buffer;
        try {
            buffer = fc.map(FileChannel.MapMode.READ_ONLY, position, size);

        } catch (IOException ex) {
            System.gc();
            System.runFinalization();
            buffer = fc.map(FileChannel.MapMode.READ_ONLY, position, size);
        }
        return buffer;
    }

    public void parse() throws IOException {
        MappedByteBuffer buffer = this.getMappedBuffer(0, NXHeader.SIZE);
        this.getNxHeader().parse(buffer);
    }

    @Override
    public void close() throws Exception {
        this.dispose();
    }

    @Override
    public void dispose() {
        try {
            this.getNxFile().close();
            this.getBaseNode().dispose();
        } catch (IOException ignored) {
        }
    }

    public String getString(long id) {
        return this.getStringTable().computeIfAbsent(id, this::loadString);
    }

    private String loadString(long id) {
        long tableOffset = this.getNxHeader().getStringTableOffset() + id * 8;
        String text = "";
        try {
            MappedByteBuffer buffer = this.getMappedBuffer(tableOffset, 8);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            long stringOffset = buffer.getLong();
            buffer = this.getMappedBuffer(stringOffset, 2);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            int stringSize = buffer.getShort();
            buffer = this.getMappedBuffer(stringOffset + 2, stringSize);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            if (stringSize == 0) return "";
            byte[] data = new byte[stringSize];
            buffer.get(data);
            text = new String(data, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }

    public NXNode getNode(long id) {
        return this.getNodeTable().computeIfAbsent(id, this::loadNode);
    }

    private NXNode loadNode(long id) {
        long offset = this.getNxHeader().getNodeBlockOffset() + id * 20;
        return NXNode.ParseNode(this, offset);
    }

    public NXNode getBaseNode() {
        return this.getNode(0);
    }

    public NXNode<?> resolvePath(String path) {
        String[] elements = (path.startsWith("/") ? path.substring(1) : path).split("[/\\\\]");
        NXNode node = this.getBaseNode();
        for (String element : elements) {
            if (!element.equals("."))
                node = node.getChild(element);
        }
        return node;
    }

}