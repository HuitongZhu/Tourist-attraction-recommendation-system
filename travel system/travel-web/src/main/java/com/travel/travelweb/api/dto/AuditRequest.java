package com.travel.travelweb.api.dto;

public class AuditRequest {
    private String status;
    /** 安卓端：true=通过，false=驳回 */
    private Boolean approved;
    private String remark;

    public AuditRequest() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean approved) { this.approved = approved; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
