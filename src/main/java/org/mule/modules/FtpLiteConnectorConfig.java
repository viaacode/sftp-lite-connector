/**
 * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.md file.
 *
 * Modifications copyright (c) 2017 VIAA vzw
 */
package org.mule.modules;

import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.components.Configuration;
import org.mule.api.annotations.param.Default;

/**
 * Configuration type Config
 * @author VIAA vzw
 */
@Configuration(friendlyName = "Configuration")
public class FtpLiteConnectorConfig {
	/**
	 * The encoding to use when connecting to the server
	 */
    @Configurable
    @Default("UTF-8")
    private String encoding;

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
