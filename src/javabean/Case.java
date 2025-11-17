package javabean;

import java.math.BigDecimal;

public class Case {
    private int id;                         // 病例ID
    private int mashupId;                   // 关联的病种ID
    private String caseId;                  // 病例编号
    private BigDecimal drgDetailTotalAmount; // DRG明细总金额

    public Case() {
        super();
    }

    public Case(int id, int mashupId, String caseId, BigDecimal drgDetailTotalAmount) {
        super();
        this.id = id;
        this.mashupId = mashupId;
        this.caseId = caseId;
        this.drgDetailTotalAmount = drgDetailTotalAmount;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMashupId() {
        return mashupId;
    }

    public void setMashupId(int mashupId) {
        this.mashupId = mashupId;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public BigDecimal getDrgDetailTotalAmount() {
        return drgDetailTotalAmount;
    }

    public void setDrgDetailTotalAmount(BigDecimal drgDetailTotalAmount) {
        this.drgDetailTotalAmount = drgDetailTotalAmount;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        result = prime * result + mashupId;
        result = prime * result + ((caseId == null) ? 0 : caseId.hashCode());
        result = prime * result + ((drgDetailTotalAmount == null) ? 0 : drgDetailTotalAmount.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Case other = (Case) obj;
        if (id != other.id)
            return false;
        if (mashupId != other.mashupId)
            return false;
        if (caseId == null) {
            if (other.caseId != null)
                return false;
        } else if (!caseId.equals(other.caseId))
            return false;
        if (drgDetailTotalAmount == null) {
            if (other.drgDetailTotalAmount != null)
                return false;
        } else if (!drgDetailTotalAmount.equals(other.drgDetailTotalAmount))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Case [id=" + id + ", mashupId=" + mashupId + ", caseId=" + caseId 
                + ", drgDetailTotalAmount=" + drgDetailTotalAmount + "]";
    }
}