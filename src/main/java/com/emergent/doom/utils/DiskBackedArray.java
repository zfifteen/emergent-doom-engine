package com.emergent.doom.utils;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.ByteBuffer;
import java.io.IOException;

/**
 * A disk-backed array implementation using memory-mapped files.
 */
public class DiskBackedArray {

    private final MappedByteBuffer buffer;
    private final int elementSize;

    /**
     * Constructor for DiskBackedArray.
     *
     * @param filePath The file path to store the array.
     * @param arraySize The number of elements in the array.
     * @param elementSize The size of each element in bytes.
     * @throws IOException If an I/O error occurs.
     */
    public DiskBackedArray(String filePath, int arraySize, int elementSize) throws IOException {
        this.elementSize = elementSize;
        RandomAccessFile file = new RandomAccessFile(filePath, "rw");
        file.setLength((long) arraySize * elementSize);
        FileChannel channel = file.getChannel();
        this.buffer = channel.map(FileChannel.MapMode.READ_WRITE, 0, (long) arraySize * elementSize);
    }

    /**
     * Writes an element to the array.
     *
     * @param index The index to write to.
     * @param data The data to write as a byte array.
     */
    public void write(int index, byte[] data) {
        if (data.length != elementSize) {
            throw new IllegalArgumentException("Data size must match element size.");
        }
        buffer.position(index * elementSize);
        buffer.put(data);
    }

    /**
     * Reads an element from the array.
     *
     * @param index The index to read from.
     * @return The data as a byte array.
     */
    public byte[] read(int index) {
        byte[] data = new byte[elementSize];
        buffer.position(index * elementSize);
        buffer.get(data);
        return data;
    }

    /**
     * Closes the buffer and releases resources.
     */
    public void close() {
        // No explicit close needed for MappedByteBuffer, but we can force cleanup if needed.
        // Use reflection to unmap the buffer if necessary (not recommended for general use).
    }
}
