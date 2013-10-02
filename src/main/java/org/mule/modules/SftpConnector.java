/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */
package org.mule.modules;

import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.Optional;
import org.mule.modules.exceptions.SftpLiteAuthException;
import org.mule.modules.exceptions.SftpLiteException;
import org.mule.modules.exceptions.SftpLiteHostException;

import java.io.InputStream;
import java.util.Vector;

/**
 * SFTP Light connector. It takes credentials in all calls. Opens and closes a connection on each
 * call to the server. If you need a persistent connection to an SFTP server, you would be better
 * served by using the SFTP transport in Mule.
 *
 * @author MuleSoft, Inc.
 */
@Connector(name="sftplite", schemaVersion="1.0")
public class SftpConnector
{
    /**
     * The default value for the port property in all connector operations
     */
    private final static String STANDARD_SFTP_PORT = "22";

    /**
     * The default folder name
     */
    private final static String DEFAULT_FOLDER_PATH = "/";


    /**
     * Tries to connect to the SFTP server just to check credentials
     *
     * {@sample.xml ../../../doc/Sftp-connector.xml.sample sftplite:check-credentials}
     *
     * @param hostName The SFTP host's name to connect to
     * @param userName The user name to use to login
     * @param password The password to use to login
     * @param port the port the SFTP service is listening on
     * @returns true if the connection was successful
     */
    @Processor
    public boolean checkCredentials(
            String hostName,
            String userName,
            String password,
            @Optional @Default(STANDARD_SFTP_PORT) String port)
    {
        Session session = null;
        try {
            session = setSession(userName, hostName, port, password);
            session.connect();
            return session.isConnected();
        } catch (JSchException e) {
            if (session == null) {
                throw new SftpLiteException("Error creating SFTP session");
            }
            else if (!session.isConnected()) {
                if (e.getMessage().startsWith("java.net.UnknownHostException")) {
                    throw new SftpLiteHostException("Sftp connect failed");
                } else {
                    throw new SftpLiteAuthException("Sftp login failed");
                }
            }
        } finally {
            if (session != null)
                session.disconnect();
        }
        return false;
    }

    /**
     * Get all folder and files in a Path, it defaults to "/" when path is null
     *
     * {@sample.xml ../../../doc/Sftp-connector.xml.sample sftplite:get-folder}
     *
     * @param hostName The SFTP host's name to connect to
     * @param userName The user name to use to login
     * @param password The password to use to login
     * @param port the port the SFTP service is listening on
     * @param path the path to the folder to list
     * @return an array of Entries that represents directories and files in the path specified
     */
    @Processor
    public Vector <LsEntry> getFolder(
            String hostName,
            String userName,
            String password,
            @Optional @Default(STANDARD_SFTP_PORT) String port,
            @Optional @Default(DEFAULT_FOLDER_PATH) String path)
    {
        Session session = null;
        try {
            session = setSession(userName, hostName, port, password);
            session.connect();
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp command = (ChannelSftp) channel;

            try {
                Vector <LsEntry> vector = command.ls(path);
                return vector;
            } catch (SftpException e) {
                throw new SftpLiteException("There was an error fetching files from SFTP");
            } finally {
                command.exit();
                session.disconnect();
            }
        } catch (JSchException e) {
            if (session == null) {
                throw new SftpLiteException("Error creating SFTP session");
            }
            else if (!session.isConnected()) {
                if (e.getMessage().startsWith("java.net.UnknownHostException")) {
                    throw new SftpLiteHostException("Sftp connect failed");
                } else {
                    throw new SftpLiteAuthException("Sftp login failed");
                }
            }
        }
        return null;
    }

    /**
     * Get a single file's content as a stream
     *
     * {@sample.xml ../../../doc/Sftp-connector.xml.sample sftplite:get-file}
     *
     * @param hostName The SFTP host's name to connect to
     * @param userName The user name to use to login
     * @param password The password to use to login
     * @param port the port the SFTP service is listening on
     * @param filePath the path to the file
     * @return an Entry with the file information
     */
    @Processor
    public LsEntry getFile(
            String hostName,
            String userName,
            String password,
            String filePath,
            @Optional @Default(STANDARD_SFTP_PORT) String port)
    {
        Session session = null;
        try {
            session = setSession(userName, hostName, port, password);
            session.connect();
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp command = (ChannelSftp) channel;

            try {
                if (filePath.equals("")) {
                    filePath = "/";
                }
                Vector <LsEntry> vector = command.ls(filePath);
                if (vector.size() > 0) {
                   return vector.get(0);
                }
            } catch (SftpException e) {
                throw new SftpLiteException("Error retrieving file from SFTP");
            } finally {
                command.exit();
                session.disconnect();
            }
        } catch (JSchException e) {
            if (session == null) {
                throw new SftpLiteException("Error creating SFTP session");
            }
            else if (!session.isConnected()) {
                if (e.getMessage().startsWith("java.net.UnknownHostException")) {
                    throw new SftpLiteHostException("Sftp connect failed");
                } else {
                    throw new SftpLiteAuthException("Sftp login failed");
                }
            }
        }
        return null;
    }

    /**
     * Get a single file's content as a stream
     *
     * {@sample.xml ../../../doc/Sftp-connector.xml.sample sftplite:get-file-content}
     *
     * @param hostName The SFTP host's name to connect to
     * @param userName The user name to use to login
     * @param password The password to use to login
     * @param port the port the SFTP service is listening on
     * @param filePath the path to the folder to list
     * @return an InputStream of the file
     */
    @Processor
    public InputStream getFileContent(
            String hostName,
            String userName,
            String password,
            @Optional @Default(STANDARD_SFTP_PORT) String port,
            String filePath)
    {

        Session session = null;
        InputStream result;
        try {
            session = setSession(userName, hostName, port, password);
            session.connect();
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp command = (ChannelSftp) channel;

            try {
               result = command.get(filePath);
               return new SftpConnectionClosingStream(session, command, result);
            } catch (SftpException e) {
                throw new SftpLiteException("Error retrieving file stream from SFTP");
            }
        } catch (JSchException e) {
            if (session == null) {
                throw new SftpLiteException("Error creating SFTP session");
            }
            else if (!session.isConnected()) {
                if (e.getMessage().startsWith("java.net.UnknownHostException")) {
                    throw new SftpLiteHostException("Sftp connect failed");
                } else {
                    throw new SftpLiteAuthException("Sftp login failed");
                }
            }
        }
        return null;
    }

    private Session setSession (String userName, String hostName, String port, String password) throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(userName, hostName, Integer.parseInt(port));
        session.setPassword(password);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        return session;
    }

}
