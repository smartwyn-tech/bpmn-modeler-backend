package com.fastcode.bpmnModeler.commons.error;

public class NotFoundException extends BaseModelerRestException {
    private static final long serialVersionUID = 1L;

    public NotFoundException() {
    }

    public NotFoundException(String s) {
        super(s);
    }
}
