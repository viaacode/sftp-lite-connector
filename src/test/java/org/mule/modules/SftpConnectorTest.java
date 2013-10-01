/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 */
package org.mule.modules;

import org.junit.Before;
import org.junit.Test;

public class SftpConnectorTest
{
    SftpConnector connector;

    @Before
    public void createConnector() {
        connector = new SftpConnector();
    }

    @Test
    public void testGetFolder() throws Exception
    {
        connector.getFolder("localhost", "mulesoft", "mule123", "22", "/Users/mulesoft/Documents");
    }


}
