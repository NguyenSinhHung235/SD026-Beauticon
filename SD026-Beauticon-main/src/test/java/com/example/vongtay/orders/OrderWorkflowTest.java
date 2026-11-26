package com.example.vongtay.orders;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrderWorkflowTest {
    @Test void onlineFlowIsStrictlySequential() {
        assertEquals(OrderWorkflow.PROCESSING,OrderWorkflow.next(OrderWorkflow.PENDING));
        assertEquals(OrderWorkflow.PROCESSED,OrderWorkflow.next(OrderWorkflow.PROCESSING));
        assertEquals(OrderWorkflow.COMPLETED,OrderWorkflow.next(OrderWorkflow.PROCESSED));
        assertNull(OrderWorkflow.next(OrderWorkflow.COMPLETED));
        assertNull(OrderWorkflow.next(OrderWorkflow.CANCELLED));
    }
    @Test void terminalOrdersCannotBeCancelledAgain() {
        assertTrue(OrderWorkflow.mayCancel(OrderWorkflow.PENDING));
        assertTrue(OrderWorkflow.mayCancel(OrderWorkflow.PROCESSING));
        assertTrue(OrderWorkflow.mayCancel(OrderWorkflow.PROCESSED));
        assertFalse(OrderWorkflow.mayCancel(OrderWorkflow.COMPLETED));
        assertFalse(OrderWorkflow.mayCancel(OrderWorkflow.CANCELLED));
    }
}
