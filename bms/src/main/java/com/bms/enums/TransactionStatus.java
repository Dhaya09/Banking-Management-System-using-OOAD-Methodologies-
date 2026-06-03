package com.bms.enums;

/**
 * UML: State Machine — Transaction States
 * Initiated → Validating → Approved → Processing → Completed/Failed
 */
public enum TransactionStatus {
    INITIATED, VALIDATING, APPROVED, PROCESSING, COMPLETED, FAILED
}
