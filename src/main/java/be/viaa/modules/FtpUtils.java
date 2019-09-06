/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */

package be.viaa.modules;

import be.viaa.modules.exceptions.FtpLiteException;
import be.viaa.modules.utils.Strings;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import be.viaa.modules.exceptions.FtpLiteHostException;

import java.io.*;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.Collectors;

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
        	File file = new File(fullPath);
            createDirectoryTree(file, client);
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

    public static boolean createDirectoryTree(File file, FTPClient client) throws IOException {
        Deque<String> directoryStructure = new LinkedList<>(Arrays.asList(file.getParent().split("/"))
                .stream()
                .filter(dir -> !dir.isEmpty())
                .collect(Collectors.toList()));
        Deque<String> directoryUnexistant = new LinkedList<>();

        /*
         * Scans to see which directory is already present and which directories
         * need to be created.
         */
        while (!directoryStructure.isEmpty()) {
            // If path starts with a /, add it back when changing directory (since it was removed with the filter above)
            if (!client.changeWorkingDirectory((file.getParent().startsWith("/") ? "/" : "") + Strings.join("/", directoryStructure))) {
                directoryUnexistant.addFirst(directoryStructure.removeLast());
            } else {
                break;
            }
        }

        /*
         * Creates the directories that need to be created
         */
        for (Iterator<String> iterator = directoryUnexistant.iterator(); iterator.hasNext();) {
            String directory = iterator.next();

            if (!client.makeDirectory(directory) || !client.changeWorkingDirectory(directory)) {
                throw new IOException("could not create directory tree");
            }
        }
        return true;
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
