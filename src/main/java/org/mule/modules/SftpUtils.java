/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */

package org.mule.modules;

import com.jcraft.jsch.*;
import org.mule.modules.exceptions.SftpLiteAuthException;
import org.mule.modules.exceptions.SftpLiteException;
import org.mule.modules.exceptions.SftpLiteHostException;
import java.io.InputStream;
import java.util.Vector;

public class SftpUtils {

    /**
     * The protocol name
     */
    private final static String SFTP = "sftp";

    private final static String STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";

    public static Session createSession (String userName, String hostName, String port, String password) {
        JSch jsch = new JSch();
        Session session = null;
        try {
            session = jsch.getSession(userName, hostName, Integer.parseInt(port));
            session.setPassword(password);
            java.util.Properties config = new java.util.Properties();
            config.put(STRICT_HOST_KEY_CHECKING, "no");
            session.setConfig(config);
            session.connect();
        }
        catch (JSchException e) {
            if (session == null) {
                throw new SftpLiteException("Error creating SFTP session");
            }
            else if (!session.isConnected()) {
                if (e.getCause() instanceof java.net.UnknownHostException) {
                    throw new SftpLiteHostException("Sftp connect to host failed");
                } else if (e.getCause() instanceof  java.net.ConnectException) {
                    throw new SftpLiteHostException("Failed to connect to SFTP port");
                } else {
                    throw new SftpLiteAuthException("Sftp login failed");
                }
            } else {
                releaseConnection(session, null);
                throw new SftpLiteException("Error connecting to SFTP server");
            }
        }
        return session;
    }

    public static void releaseConnection(Session session, ChannelSftp command) {
        if (command != null)
            command.exit();
        if (session != null)
            session.disconnect();
    }

    public static ChannelSftp setChannel (Session session) {
        try {
            Channel channel = session.openChannel(SFTP);
            channel.connect();
            ChannelSftp command = (ChannelSftp) channel;
            return command;
        } catch (JSchException e) {
            releaseConnection(session, null);
            throw new SftpLiteException("Error opening SFTP channel");
        }
    }

    public static void putFile(Session session, ChannelSftp command, InputStream content, String filePath, String filename) {
        try {
            command.put(content, filePath + "/" + filename);
        } catch (SftpException e) {
            releaseConnection(session, command);
            throw new SftpLiteException("Error storing file into SFTP server");
        }
    }

    public static Vector<ChannelSftp.LsEntry> listFiles (Session session, ChannelSftp command, String path) {
        try {
            Vector <ChannelSftp.LsEntry> vector = command.ls(path);
            return vector;
        } catch (SftpException e) {
            releaseConnection(session, command);
            throw new SftpLiteException("There was an error fetching files from SFTP");
        }
    }

    public static ChannelSftp.LsEntry getFile (Session session, ChannelSftp command, String filePath) {
        Vector <ChannelSftp.LsEntry> vector;
        try {
            if (filePath.equals(""))
                filePath = "/";

            vector = command.ls(filePath);

            if (vector.size() > 0)
                return vector.get(0);

        } catch (SftpException e) {
            releaseConnection(session, command);
            throw new SftpLiteException("Error retrieving file from SFTP");
        }
        return null;
    }

    public static InputStream getFileStream (Session session, ChannelSftp command, String filePath) {
        try {
            InputStream result = command.get(filePath);
            return result;
        } catch (SftpException e) {
            releaseConnection(session, command);
            throw new SftpLiteException("Error retrieving file stream from SFTP");
        }
    }
}
