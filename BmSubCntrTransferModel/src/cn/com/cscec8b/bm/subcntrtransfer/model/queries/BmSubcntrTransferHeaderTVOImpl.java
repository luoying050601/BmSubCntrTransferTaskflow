package cn.com.cscec8b.bm.subcntrtransfer.model.queries;

import cn.com.cscec8b.framework.model.common.CustomViewObjectImpl;

import oracle.jbo.domain.Number;
import oracle.jbo.server.ViewObjectImpl;
// ---------------------------------------------------------------------
// ---    File generated by Oracle ADF Business Components Design Time.
// ---    Mon Jul 03 18:52:45 CST 2017
// ---    Custom code may be added to this class.
// ---    Warning: Do not modify method signatures of generated methods.
// ---------------------------------------------------------------------
public class BmSubcntrTransferHeaderTVOImpl extends CustomViewObjectImpl {
    /**
     * This is the default constructor (do not remove).
     */
    public BmSubcntrTransferHeaderTVOImpl() {
    }

    /**
     * Returns the variable value for bvSubcntrId.
     * @return variable value for bvSubcntrId
     */
    public Number getbvSubcntrId() {
        return (Number)ensureVariableManager().getVariableValue("bvSubcntrId");
    }

    /**
     * Sets <code>value</code> for variable bvSubcntrId.
     * @param value value to bind as bvSubcntrId
     */
    public void setbvSubcntrId(Number value) {
        ensureVariableManager().setVariableValue("bvSubcntrId", value);
    }

    /**
     * Returns the variable value for bvProjectId.
     * @return variable value for bvProjectId
     */
    public Number getbvProjectId() {
        return (Number)ensureVariableManager().getVariableValue("bvProjectId");
    }

    /**
     * Sets <code>value</code> for variable bvProjectId.
     * @param value value to bind as bvProjectId
     */
    public void setbvProjectId(Number value) {
        ensureVariableManager().setVariableValue("bvProjectId", value);
    }
    
    //1

    public Number getSumTransferAmount() {
        return getTotal("TransferAmount");
    }
    //2

    public Number getSumCurrentBmAmount() {
        return getTotal("CurrentBmAmount");
    }
}
