package org.distributed.kvstore.common.messages;

public enum OperationType {
    GET(1), PUT(2);

    final private int operationCode;
    OperationType(int operationCode) {
        this.operationCode = operationCode;
    }

    public int operationCode() {
        return operationCode;
    }
}
