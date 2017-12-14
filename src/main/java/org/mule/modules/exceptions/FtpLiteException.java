package org.mule.modules.exceptions;

/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */
public class FtpLiteException extends RuntimeException {
    private static final long serialVersionUID = 8567511081840301280L;

    public FtpLiteException(String message) {
        super(message);
    }

    public FtpLiteException(Throwable t) {
        super(t);
    }

}
