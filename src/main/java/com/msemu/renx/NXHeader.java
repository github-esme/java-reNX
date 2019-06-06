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

import lombok.Getter;
import lombok.Setter;

import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;

@Getter
@Setter
public class NXHeader {
    private int magic;
    private long nodeCount;
    private long nodeBlockOffset;
    private long stringCount;
    private long stringTableOffset;
    private long bitmapCount;
    private long bitmapTableOffset;
    private long audioCount;
    private long audioTableOffset;

    public static int SIZE = 52;

    public NXHeader() {
    }

    public void parse(MappedByteBuffer buffer) {
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        this.magic = buffer.getInt();
        this.nodeCount = buffer.getInt();
        this.nodeBlockOffset = buffer.getLong();
        this.stringCount = buffer.getInt();
        this.stringTableOffset = buffer.getLong();
        this.bitmapCount = buffer.getInt();
        this.bitmapTableOffset = buffer.getLong();
        this.audioCount = buffer.getInt();
        this.audioTableOffset = buffer.getLong();
    }

    @Override
    public String toString() {
        return "NXHeader{" +
                "magic=" + magic +
                ", nodeCount=" + nodeCount +
                ", nodeBlockOffset=" + nodeBlockOffset +
                ", stringCount=" + stringCount +
                ", stringTableOffset=" + stringTableOffset +
                ", bitmapCount=" + bitmapCount +
                ", bitmapTableOffset=" + bitmapTableOffset +
                ", audioCount=" + audioCount +
                ", audioTableOffset=" + audioTableOffset +
                '}';
    }
}
