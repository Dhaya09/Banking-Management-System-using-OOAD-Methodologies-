package com.bms.enums;

/**
 * UML: State Machine — Loan States
 * Draft → Submitted → UnderReview → Approved/Rejected → Disbursed → Closed
 */
public enum LoanStatus {
    DRAFT, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, DISBURSED, CLOSED
}
