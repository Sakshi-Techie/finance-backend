package com.zorvyn.finance.model;

public enum Role {
    VIEWER,   // Can only view dashboard data
    ANALYST,  // Can view records and access insights/summaries
    ADMIN     // Full management access: create, update, delete records and users
}
