package com.xinchentechnote.exchange.common;

import lombok.Data;
import org.apache.commons.lang.StringUtils;

@Data
public class FrontInfoField {
    private String protocol;
    private String ip;
    private short port;

    public FrontInfoField(String frontAddress) {
        if (StringUtils.isEmpty(frontAddress)) {
            throw new IllegalArgumentException("frontAddress is null or empty");
        }
        try {
            String[] split = frontAddress.split("://");
            this.protocol = split[0];
            String[] address = split[1].split(":");
            this.ip = address[0];
            this.port = Short.parseShort(address[1]);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid frontAddress format, expected format: protocol://ip:port", e);
        }

    }
}
