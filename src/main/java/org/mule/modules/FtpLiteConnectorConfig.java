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
