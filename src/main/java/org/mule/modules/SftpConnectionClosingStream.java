package org.mule.modules;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.io.InputStream;

/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */
public class SftpConnectionClosingStream extends InputStream {

    private final Session session;

    private final InputStream stream;

    private final ChannelSftp command;

    public SftpConnectionClosingStream(Session session, ChannelSftp command, InputStream stream) {
        this.session = session;
        this.stream = stream;
        this.command = command;
    }

    @Override
    public int read() throws IOException {
        int result = this.stream.read();

        if (result == -1) {
            SftpUtils.releaseConnection(session, command);
        }
        return result;
    }

    @Override
    public int read(byte[] bytes) throws IOException {
        int result = stream.read(bytes);

        if (result == -1) {
            SftpUtils.releaseConnection(session, command);
        }
        return result;
    }

    @Override
    public int read(byte[] bytes, int i, int i2) throws IOException {
        int result = stream.read(bytes, i, i2);

        if (result == -1) {
            SftpUtils.releaseConnection(session, command);
        }
        return result;
    }

    @Override
    public long skip(long l) throws IOException {
        return stream.skip(l);
    }

    @Override
    public int available() throws IOException {
        return stream.available();
    }

    @Override
    public void close() throws IOException {
        SftpUtils.releaseConnection(session, command);
        stream.close();
    }

    @Override
    public synchronized void mark(int i) {
        stream.mark(i);
    }

    @Override
    public synchronized void reset() throws IOException {
        stream.reset();
    }

    @Override
    public boolean markSupported() {
        return stream.markSupported();
    }

    public InputStream getStream(){
        return stream;
    }
}
