/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */
package be.viaa.modules;

import be.viaa.modules.utils.FtpConnectionClosingStream;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import org.mule.api.annotations.*;
import org.mule.api.annotations.display.Password;
import org.mule.api.annotations.param.Default;
import org.mule.api.annotations.param.Optional;

import java.io.InputStream;

/**
 * FTP Light connector. It takes credentials in all calls. Opens and closes a connection on each
 * call to the server.
 *
 * @author MuleSoft, Inc.
 * Modifications copyright (c) 2017 VIAA vzw
 */
@Connector(name="ftplite", schemaVersion="1.0", friendlyName="FTPLite")
public class FtpLiteConnector
{
    @Config
    FtpLiteConnectorConfig config;
    /**
     * The default value for the port property in all connector operations
     */
    private final static String STANDARD_FTP_PORT = "21";

    /**
     * Get all folder and files in a Path, it defaults to the home folder when path is null
     *
     * {@sample.xml ../../../doc/Ftp-connector.xml.sample ftplite:get-folder}
     *
     * @param hostName The FTP host's name to connect to
     * @param userName The user name to use to login
     * @param password The password to use to login
     * @param port the port the FTP service is listening on
     * @param path the path to the folder to list
     * @return an array of Entries that represents directories and files in the path specified
     */
    
    @Processor
    public FTPFile[] getFolder(
            String hostName,
            String userName,
            @Password String password,
            @Default(value=STANDARD_FTP_PORT) String port,
            @Optional String path)
    {
        FTPClient client = FtpUtils.createSession(config, userName, hostName, port, password);
        FTPFile[] files = FtpUtils.listFiles(client, path);
        FtpUtils.disconnect(client);
        return files;
    }

    /**
     * Get a single file's content as a stream
     *
     * {@sample.xml ../../../doc/Ftp-connector.xml.sample ftplite:get-file-content}
     *
     * @param hostName The FTP host's name to connect to
     * @param userName The user name to use to login
     * @param password The password to use to login
     * @param port the port the SFTP service is listening on
     * @param filePath the path to the folder to list
     * @return an InputStream of the file
     */

    @Processor
    public FtpConnectionClosingStream getFileContent(
            String hostName,
            String userName,
            @Password String password,
            @Default(value=STANDARD_FTP_PORT) String port,
            String filePath,
            String fileName)
    {
        FTPClient client = FtpUtils.createSession(config, userName, hostName, port, password);
        InputStream result = FtpUtils.getFileStream(client, filePath, fileName);
        return new FtpConnectionClosingStream(client, result);
    }


    /**
     * Uploads a file to the FTP server
     *
     * {@sample.xml ../../../doc/Ftp-connector.xml.sample ftplite:upload-stream}
     *
     * @param hostName The FTP host's name to connect to
     * @param userName The user name to use to login
     * @param password The password to use to login
     * @param port the port the FTP service is listening on
     * @param filePath the path to the folder to store the file in
     * @param fileName the name of the file to store
     * @param content an InputStream with the content to store in the file
     * @return an LsEntry with the file's information
     */

    @Processor
    public void uploadStream(
            String hostName,
            String userName,
            @Password String password,
            @Default(value=STANDARD_FTP_PORT) String port,
            String filePath,
            String fileName,
            @Default(value="#[payload]") InputStream content)
    {
        FTPClient client = FtpUtils.createSession(config, userName, hostName, port, password);
        FtpUtils.putFile(client, content, filePath, fileName);
        FtpUtils.disconnect(client);
    }

    /**
     * Returns true if a file exists, false if not
     *
     * {@sample.xml ../../../doc/Ftp-connector.xml.sample ftplite:file-exists}
     *
     * @param hostName The FTP host's name to connect to
     * @param userName The user name to use to login
     * @param password The password to use to login
     * @param port the port the FTP service is listening on
     * @param filePath the path to the file to check
     * @return an LsEntry with the file's information
     */

    @Processor
    public boolean fileExists(
            String hostName,
            String userName,
            @Password String password,
            @Default(value=STANDARD_FTP_PORT) String port,
            String filePath,
            String fileName)
    {
        FTPClient client = FtpUtils.createSession(config, userName, hostName, port, password);
        boolean exists = FtpUtils.fileExists(client, filePath, fileName);
        FtpUtils.disconnect(client);
        return exists;
    }

    /**
     * Deletes a file
     *
     * {@sample.xml ../../../doc/Ftp-connector.xml.sample ftplite:delete-file}
     *
     * @param hostName The FTP host's name to connect to
     * @param userName The user name to use to login
     * @param password The password to use to login
     * @param port the port the FTP service is listening on
     * @param filePath the path to the file to check
     * @return an LsEntry with the file's information
     */

    @Processor
    public boolean deleteFile(
            String hostName,
            String userName,
            @Password String password,
            @Default(value=STANDARD_FTP_PORT) String port,
            String filePath,
            String fileName)
    {
        FTPClient client = FtpUtils.createSession(config, userName, hostName, port, password);
        boolean deleted = FtpUtils.deleteFile(client, filePath, fileName);
        FtpUtils.disconnect(client);
        return deleted;
    }

    public FtpLiteConnectorConfig getConfig() {
        return config;
    }

    public void setConfig(FtpLiteConnectorConfig config) {
        this.config = config;
    }
}
