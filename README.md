SFTP Multitenant Lite Connector
===================

This connector opens and closes a session everytime an operation is called.

Operations supported:
* checkCredentials : Tries to connect to the SFTP server just to check credentials
* getFolder : Get all folder and files in a Path, it defaults to "/" when path is null
* getFile : Get a single file's information
* getFileContent :  Get a single file's content as a stream
* uploadStream : Upload a file to the SFTP server

Modifications were made by VIAA vzw. This fork uses the Apache Commons FTP library instead of the jsch library used by the original connector.