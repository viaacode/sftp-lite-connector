package org.mule.modules.exceptions;

/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */
public class FtpLiteAuthException extends FtpLiteException {
    private static final long serialVersionUID = 5642716012942133264L;

    public FtpLiteAuthException(String message) {
        super(message);
    }

}
