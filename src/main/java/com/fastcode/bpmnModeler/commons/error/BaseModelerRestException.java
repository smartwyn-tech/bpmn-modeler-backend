package com.fastcode.bpmnModeler.commons.error;

import java.util.HashMap;
import java.util.Map;

public class BaseModelerRestException extends RuntimeException{
    private static final long serialVersionUID = 1L;
    protected String messageKey;
    protected Map<String, Object> customData;

    public BaseModelerRestException() {
    }

    public BaseModelerRestException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseModelerRestException(String message) {
        super(message);
    }

    public BaseModelerRestException(Throwable cause) {
        super(cause);
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return this.messageKey;
    }

    public Map<String, Object> getCustomData() {
        return this.customData;
    }

    public void setCustomData(Map<String, Object> customData) {
        this.customData = customData;
    }

    public void addCustomData(String key, Object data) {
        if (this.customData == null) {
            this.customData = new HashMap();
        }

        this.customData.put(key, data);
    }
}
