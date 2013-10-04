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
        Session session = SftpUtils.createSession(userName, hostName, port, password);

        boolean isConnected = session.isConnected();

        SftpUtils.releaseConnection(session, null);

        return isConnected;
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
        Session session = SftpUtils.createSession(userName, hostName, port, password);
        ChannelSftp command = SftpUtils.createChannel(session);
        Vector <LsEntry> vector = SftpUtils.listFiles(session, command, path);
        SftpUtils.releaseConnection(session, command);
        return vector;
    }

    /**
     * Get a single file's information
     *
     * {@sample.xml ../../../doc/Sftp-connector.xml.sample sftplite:get-file}
     *
     * @param hostName The SFTP host's name to connect to
     * @param userName The user name to use to login
     * @param password The password to use to login
     * @param port the port the SFTP service is listening on
     * @param filePath the path to the file
     * @return an LsEntry with the file information
     */
    @Processor
    public LsEntry getFile(
            String hostName,
            String userName,
            String password,
            String filePath,
            @Optional @Default(STANDARD_SFTP_PORT) String port)
    {
        Session session = SftpUtils.createSession(userName, hostName, port, password);
        ChannelSftp command = SftpUtils.createChannel(session);
        LsEntry fileEntry = SftpUtils.getFile(session, command, filePath);
        SftpUtils.releaseConnection(session, command);
        return fileEntry;
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
        Session session = SftpUtils.createSession(userName, hostName, port, password);
        ChannelSftp command = SftpUtils.createChannel(session);
        InputStream result = SftpUtils.getFileStream(session, command, filePath);
        return new SftpConnectionClosingStream(session, command, result);
    }

    /**
     * Uploads a file to the SFTP server
     *
     * {@sample.xml ../../../doc/Sftp-connector.xml.sample sftplite:upload-stream}
     *
     * @param hostName The SFTP host's name to connect to
     * @param userName The user name to use to login
     * @param password The password to use to login
     * @param port the port the SFTP service is listening on
     * @param filePath the path to the folder to store the file in
     * @param fileName the name of the file to store
     * @param content an InputStream with the content to store in the file
     * @return an LsEntry with the file's information
     */
    @Processor
    public void uploadStream(
            String hostName,
            String userName,
            String password,
            @Optional @Default(STANDARD_SFTP_PORT) String port,
            String filePath,
            String fileName,
            @Optional @Default("#[payload]") InputStream content)
    {
        Session session = SftpUtils.createSession(userName, hostName, port, password);
        ChannelSftp command = SftpUtils.createChannel(session);
        SftpUtils.putFile(session, command, content, filePath, fileName);
        SftpUtils.releaseConnection(session, command);
    }
}
