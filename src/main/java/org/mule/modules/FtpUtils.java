/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */

package org.mule.modules;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.mule.modules.exceptions.FtpLiteException;
import org.mule.modules.exceptions.FtpLiteHostException;

import java.io.*;
import java.net.SocketException;

/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 *
 * Modifications copyright (c) 2017 VIAA vzw
 */

public class FtpUtils {

    /**
     * The protocol name
     */
    private final static String FTP = "ftp";

    private final static String STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";

    public static FTPClient createSession (FtpLiteConnectorConfig config, String userName, String hostName, String port, String password) {
    	FTPClient ftp = new FTPClient();
    	ftp.setControlEncoding(config.getEncoding());
    	try {
			ftp.connect(hostName, Integer.parseInt(port));
			ftp.login(userName, password);
		} catch (NumberFormatException e) {
			throw new FtpLiteHostException("Port was incorrect and could not be parsed");
		} catch (SocketException e) {
			throw new FtpLiteHostException("Error connecting. " + e.toString());
		} catch (IOException e) {
			throw new FtpLiteHostException("Error connecting. " + e.toString());
		}
    	return ftp;
    }
    
    public static void disconnect(FTPClient client) {
    		try {
				client.disconnect();
            } catch (IOException e) {
				throw new FtpLiteException(e.toString());
			}
    	
    }

    public static void putFile(FTPClient client, InputStream content, String filePath, String fileName) {

        try {
        	String fullPath = createFullPath(filePath, fileName);
            client.storeFile(fullPath, content);
        } catch (IOException e) {
            disconnect(client);
            throw new FtpLiteException("Error storing file into SFTP server");
        }
    }

    public static FTPFile[] listFiles (FTPClient client, String path) {
        try {
            if (path == null || path.isEmpty()) {
                path = client.printWorkingDirectory();
            }
            return client.listFiles(path);
        } catch (IOException e) {
        	try {
        		client.disconnect();
        	} catch (Exception error) {
        		throw new FtpLiteException("There was an error disconnecting. " + error.toString());
        	}
            throw new FtpLiteException("There was an error fetching files from SFTP");
		}
    }

    public static boolean fileExists(FTPClient client, String filePath, String fileName) {
        try {
            String fullPath = createFullPath(filePath, fileName);
            client.changeWorkingDirectory(filePath);
            if (client.listFiles(fileName).length == 1) {
                return true;
            }
            else {
                return false;
            }
        } catch (IOException e) {
            throw new FtpLiteException("Error looking up the file");
        }
    }

    public static InputStream getFileStream (FTPClient client, String filePath, String fileName) {
    	try {
            if (filePath == null || filePath.isEmpty()) {
                filePath = client.printWorkingDirectory();
            }
            String fullPath = createFullPath(filePath, fileName);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            if (fileExists(client, filePath, fileName)) {
                client.retrieveFile(fullPath, outStream);
                return new ByteArrayInputStream(outStream.toByteArray());
            } else {
                throw new FtpLiteException("File does not exist");
            }
        } catch (IOException e) {
            disconnect(client);
            throw new FtpLiteException("Error retrieving file stream from SFTP");
        }
    }

    public static boolean deleteFile(FTPClient client, String filePath, String fileName) {
        try {
        	String fullPath = createFullPath(filePath, fileName);
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            if (fileExists(client, filePath, fileName)) {
                return client.deleteFile(fullPath);
            } else {
                throw new FtpLiteException("File does not exist");
            }
        } catch (IOException e) {
            disconnect(client);
            throw new FtpLiteException("Error retrieving file stream from SFTP");
        }
    }
    
    /**
     * Creates a full path from a filepath and filename
     * @param filePath the path to where the file resides
     * @param fileName the filename
     * @return the full path
     */
    private static String createFullPath(String filePath, String fileName) {
    	return ((filePath.endsWith("/") && !filePath.equals("/")) ? filePath : filePath + "/") + fileName;
    }
}
