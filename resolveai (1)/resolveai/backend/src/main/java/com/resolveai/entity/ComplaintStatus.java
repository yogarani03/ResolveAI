package com.resolveai.entity;

/** The complaint lifecycle. Transitions between these are validated in
 *  ComplaintService.validateTransition() - a complaint can never jump to an
 *  invalid state (e.g. CLOSED -> IN_PROGRESS directly). */
public enum ComplaintStatus {
    OPEN,
    ASSIGNED,
    IN_PROGRESS,
    ESCALATED,
    RESOLVED,
    CLOSED,
    REOPENED
}
