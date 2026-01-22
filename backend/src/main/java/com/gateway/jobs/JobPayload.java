package com.gateway.jobs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobPayload {
    private String type; // ProcessPaymentJob, DeliverWebhookJob, ProcessRefundJob
    private String id;
    private String merchantId;
    private String event;
    private String payload;
    private int attempts;
}
