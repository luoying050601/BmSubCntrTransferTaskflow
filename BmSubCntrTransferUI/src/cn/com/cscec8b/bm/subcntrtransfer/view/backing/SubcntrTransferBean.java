package cn.com.cscec8b.bm.subcntrtransfer.view.backing;

import cn.com.cecec8b.proxy.bm.receivevisa.types.RequestType;
import cn.com.cecec8b.proxy.bm.receivevisa.types.ResponseType;
import cn.com.cecec8b.proxy.bm.receivevisa.ws.QueryReceiveVisaBpel;
import cn.com.cecec8b.proxy.bm.receivevisa.ws.Queryreceivevisabpel_client_ep;
import cn.com.cecec8b.proxy.bm.receivreport.types.Report;
import cn.com.cecec8b.proxy.bm.receivreport.ws.QueryReceivReportBpel;
import cn.com.cecec8b.proxy.bm.receivreport.ws.Queryreceivreportbpel_client_ep;
import cn.com.cecec8b.proxy.bm.subcntraudit.ws.QueryAuditBpel;
import cn.com.cecec8b.proxy.bm.subcntraudit.ws.Queryauditbpel_client_ep;
import cn.com.cscec8b.framework.view.common.CustomManagedBean;

import cn.com.cscec8b.proxy.bm.cancelaudit.types.CancelAuditType;
import cn.com.cscec8b.proxy.bm.cancelaudit.types.RequestCancelAuditType;
import cn.com.cscec8b.proxy.bm.cancelaudit.types.ResponseCancelAuditType;
import cn.com.cscec8b.proxy.bm.cancelaudit.ws.QueryCancelAuditBpel;
import cn.com.cscec8b.proxy.bm.cancelaudit.ws.Querycancelauditbpel_client_ep;
import cn.com.cscec8b.proxy.bm.cancelvisa.types.CancelVisaType;
import cn.com.cscec8b.proxy.bm.cancelvisa.types.RequestCancelVisaType;
import cn.com.cscec8b.proxy.bm.cancelvisa.types.ResponseCancelVisaType;
import cn.com.cscec8b.proxy.bm.cancelvisa.ws.QueryCancelVisaBpel;
import cn.com.cscec8b.proxy.bm.cancelvisa.ws.Querycancelvisabpel_client_ep;
import cn.com.cscec8b.proxy.bm.subcntr.types.ResponstType;

import cn.com.cscec8b.proxy.bm.subcntr.ws.QuerySubcntrTransBpel;
import cn.com.cscec8b.proxy.bm.subcntr.ws.Querysubcntrtransbpel_client_ep;

import cn.com.cscec8b.proxy.bm.subcntrdebit.ws.QueryDebitCreditBpel;
import cn.com.cscec8b.proxy.bm.subcntrdebit.ws.Querydebitcreditbpel_client_ep;
import cn.com.cscec8b.proxy.bm.subcntrvisa.ws.QueryVisaBpel;
import cn.com.cscec8b.proxy.bm.subcntrvisa.ws.Queryvisabpel_client_ep;

import cn.com.cscec8b.utils.db.DBUtil;

import java.sql.SQLException;

import java.util.List;

import java.util.logging.Level;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import oracle.adf.model.binding.DCIteratorBinding;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.data.RichTable;

import oracle.adf.view.rich.component.rich.input.RichSelectBooleanCheckbox;
import oracle.adf.view.rich.component.rich.layout.RichPanelStretchLayout;
import oracle.adf.view.rich.component.rich.nav.RichCommandToolbarButton;
import oracle.adf.view.rich.event.DialogEvent;
import oracle.adf.view.rich.event.QueryEvent;

import oracle.binding.OperationBinding;

import oracle.jbo.Row;
import oracle.jbo.VariableValueManager;
import oracle.jbo.ViewCriteria;
import oracle.jbo.domain.Date;
import oracle.jbo.domain.Number;
import oracle.jbo.server.ViewObjectImpl;

public class SubcntrTransferBean extends CustomManagedBean {
    private RichTable transferTable;
    private RichCommandToolbarButton transferAgainButton;
    private RichCommandToolbarButton transferButton;
    private RichPanelStretchLayout transferLayout;
    private RichSelectBooleanCheckbox checkAll;
    private RichTable historyTable;
    private ADFLogger adfLogger = ADFLogger.createADFLogger(this.getClass());

    public SubcntrTransferBean() {
    }

    /**
     * ??????
     * @param valueChangeEvent
     */
    public void checkAllListener(ValueChangeEvent valueChangeEvent) {
        this.processUpdates(valueChangeEvent);
        this.checkAll(valueChangeEvent, "BmSubcntrTransfer1Iterator");
        this.refreshComponent(this.getTransferTable());
    }

    public void setTransferTable(RichTable transferTable) {
        this.transferTable = transferTable;
    }

    public RichTable getTransferTable() {
        return transferTable;
    }

    public Date getCurrentDateTime() {
        return this.getCurrentDate();
    }

    /**
     * ???????????????
     * 1?????????BmsubcntrTransferT??????
     * 2?????????BmsubcntrTransferHeaderT??????
     * 3?????????BmsubcntrTransferLineT??????
     *
     * */
    public void transferActionListener(ActionEvent actionEvent) {
        List<Row> checkedRows =
            this.getCheckedRows("BmSubcntrTransfer1Iterator");
        if (checkedRows.size() == 0) {
            this.addErrorMessage("??????", "???????????????????????????");
            return;
        }
        
        //????????????????????????????????????
        String returnMsg =
            DBUtil.quickGetFunctionResult(DBUtil.STM_DS, "stm.stm_bm_subcntr_transfer_pkg.CUX_CHECK_PO_AGENT(?)",
                                          new String[] { this.getCurrentEmployeeId().toString() });
        if("N".equals(returnMsg)){
            this.addErrorMessage("??????", "???????????????????????????ERP????????????????????????");
            return ;
        }
        adfLogger.log(Level.INFO, "????????????????????????returnMsg is "+returnMsg);
        
        String errorMsg = "";
        Number inValidCount = new Number(0);
        for (Row currentRow : checkedRows) {
            //??????BmsubcntrTransferTVO??????????????????????????????????????????subcntrId??????????????????????????????
            Number subcntrId = ((Number)currentRow.getAttribute("SubcntrId"));

            OperationBinding validateBinding =
                this.findOperation("getInvalidTransferStatus");

            validateBinding.getParamsMap().put("subcntrId", subcntrId);
            inValidCount = (Number)validateBinding.execute();
            if (inValidCount.intValue() > 0) {
                errorMsg +=
                        "  " + currentRow.getAttribute("SubcntrNumber") + "  ";
            }

        }
        if (errorMsg.trim() != "") {
            this.addErrorMessage("??????",
                                 "??????????????????" + errorMsg + "??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????.");
            return;
        }
        //  ??????????????????????????????
        String validateFlag = "";
        int validateCount = 0;
        for (Row currentRow : checkedRows) {
            Date transferDate = (Date)currentRow.getAttribute("TransferDate");
            OperationBinding validateBinding =
                this.findOperation("isValidateTransferDate");
            if (transferDate == null) {
                this.addErrorMessage("??????",
                                     "???????????????:" + currentRow.getAttribute("SubcntrNumber") +
                                     "?????????????????????!");
                return;
            }
            validateBinding.getParamsMap().put("transferDate",
                                               new Date(new java.sql.Date((transferDate.getValue()).getTime())));
            validateBinding.getParamsMap().put("orgId",
                                               this.getCurrentOrgId());
            validateFlag = (String)validateBinding.execute();
            if (validateFlag != null && !"".equals(validateFlag.trim())) {
                this.addErrorMessage("??????",
                                     "???????????????:" + currentRow.getAttribute("SubcntrNumber") +
                                     "???" + validateFlag);
                validateCount++;
            }
        }
        if (validateCount > 0) {
            return;
        }
        
        for (Row currentRow : checkedRows) {
            Date transferDate = (Date)currentRow.getAttribute("TransferDate");
            Number subcntrId = (Number)currentRow.getAttribute("SubcntrId");
            Number currentAuditAmount =
                (Number)currentRow.getAttribute("CurrentAuditAmount");
            Number CurrentDeductionAmount =
                (Number)currentRow.getAttribute("CurrentDeductionAmount") ==
                null ? new Number(0) :
                (Number)currentRow.getAttribute("CurrentDeductionAmount");
            Number currentDeductionAmount =
                (Number)new Number(0).minus(CurrentDeductionAmount);
            Number currentVisaAmount =
                (Number)currentRow.getAttribute("CurrentVisaAmount");

            //??????????????????
            //   if (transferDate != null) {


            OperationBinding funcBinding = this.findOperation("transfer");
            funcBinding.getParamsMap().put("subcntrId", subcntrId);
            funcBinding.getParamsMap().put("auditAmount", currentAuditAmount);
            funcBinding.getParamsMap().put("deductionAmount",
                                           currentDeductionAmount);
            funcBinding.getParamsMap().put("visaAmount", currentVisaAmount);
            funcBinding.getParamsMap().put("transferDate", transferDate);
            funcBinding.getParamsMap().put("createBy",
                                           this.getCurrentEmployeeId());
            String documentNumber = (String)funcBinding.execute();
            if (documentNumber != null && documentNumber.contains("!")) {
                this.addErrorMessage("??????", documentNumber);
                return;
            } else if (documentNumber != null &&
                       !"".equals(documentNumber.trim())) {
                //????????????erp??????
                //??????ERP
                ResponstType response = transferERP(documentNumber);
                if (response == null || !"S".equals(response.getCode())) {
                    this.addErrorMessage("??????", "??????????????????,????????????????????????????????????!");
                    currentRow.setAttribute("TransferStatus", "FAIL");
                    //????????????????????????????????????
                    //updateTransferHeaderStatus
                    funcBinding =
                            this.findOperation("updateTransferHeaderStatus");
                    funcBinding.getParamsMap().put("documentNumber",
                                                   documentNumber);
                    funcBinding.getParamsMap().put("status", "FAIL");
                    funcBinding.execute();
                } else {
                    currentRow.setAttribute("TransferStatus", "TRANSFERING");
                }
                //???????????????
                Number actualAuditAmount = new Number(0);
                Number actualVisaAmount = new Number(0);

                queryTransferAmount(documentNumber, subcntrId);
                DCIteratorBinding transferIterator =
                    this.findIterator("BmSubcntrTransferAmount1Iterator");
                Row[] allRow = transferIterator.getAllRowsInRange();
                if (allRow != null && allRow.length > 0) {
                    for (Row row : allRow) {
                        currentAuditAmount =
                                (Number)row.getAttribute("MeasureTransferAmount") ==
                                null ? (new Number(0)) :
                                (Number)row.getAttribute("MeasureTransferAmount");
                        currentVisaAmount =
                                (row.getAttribute("VisaTransferAmount") ==
                                 null) ? (new Number(0)) :
                                (Number)row.getAttribute("VisaTransferAmount");
                        currentRow.setAttribute("CurrentAuditAmount",
                                                currentAuditAmount);
                        currentRow.setAttribute("CurrentDeductionAmount",
                                                row.getAttribute("DeductionTransferAmount"));
                        currentRow.setAttribute("CurrentVisaAmount",
                                                currentVisaAmount);
                    }
                }
                Number cumulativeAuditAmount =
                    (Number)currentRow.getAttribute("CumulativeAuditAmount");
                Number cumulativeVisaAmount =
                    (Number)currentRow.getAttribute("CumulativeVisaAmount");
                Number leftAuditAmount =
                    (Number)cumulativeAuditAmount.minus(actualAuditAmount);
                Number leftVisaAmount =
                    (Number)cumulativeVisaAmount.minus(actualVisaAmount);
                currentRow.setAttribute("LeftTransferAuditAmount",
                                        leftAuditAmount);
                currentRow.setAttribute("LeftTransferDeductionAmount",
                                        new Number(0));
                currentRow.setAttribute("LeftTransferVisaAmount",
                                        leftVisaAmount);
                currentRow.setAttribute("TransferDocumentNumber",
                                        documentNumber);


            } else {
                this.addErrorMessage("??????", "????????????????????????????????????,????????????????????????,????????????????????????!");
                return;
            }
        }

        this.addInfoMessage("??????", "??????????????????,???????????????????????????????????????????????????????????????");
        this.refreshComponent(this.getTransferTable());
    }

    /**
     *
     * ??????ERP WebService????????????
     *
     * **/
    private ResponstType transferERP(String documentNumber) {
        Querysubcntrtransbpel_client_ep querysubcntrtransbpel_client_ep =
            new Querysubcntrtransbpel_client_ep();
        QuerySubcntrTransBpel querySubcntrTransBpel =
            querysubcntrtransbpel_client_ep.getQuerySubcntrTransBpel_pt();
        cn.com.cscec8b.proxy.bm.subcntr.types.RequestType type =
            new cn.com.cscec8b.proxy.bm.subcntr.types.RequestType();
        type.setDocumentNumber(documentNumber);
        ResponstType response = querySubcntrTransBpel.process(type);
        return response;
    }

    private void queryTransferAmount(String documentNumber, Number subcntrId) {

        DCIteratorBinding transferIterator =
            this.findIterator("BmSubcntrTransferAmount1Iterator");
        ViewObjectImpl vo = (ViewObjectImpl)transferIterator.getViewObject();

        ViewCriteria vc =
            vo.getViewCriteria("BmSubcntrTransferAmountVOCriteria");

        vc.resetCriteria();

        VariableValueManager vvm = vc.ensureVariableManager(); //????????????????????????????????????
        vvm.setVariableValue("bvDocumentNumber", documentNumber);
        vvm.setVariableValue("bvSubcntrId", subcntrId);

        vo.applyViewCriteria(vc, true);
        vo.executeQuery();


    }

    /**
     * ????????????
     *
     * */
    public void transferAgainListener(ActionEvent actionEvent) {
        DCIteratorBinding transferIterator =
            this.findIterator("BmsubcntrTransferHeaderT1Iterator");
        Row currentRow = transferIterator.getCurrentRow();
        if (currentRow != null) {
            //???????????????????????????
            if ("SUCCESS".equals(currentRow.getAttribute("TransferStatus"))) {
                //????????????
                this.addErrorMessage("??????", "?????????????????????,???????????????");
            }
            if ("TRANSFERING".equals(currentRow.getAttribute("TransferStatus"))) {
                //????????????
                this.addErrorMessage("??????", "????????????????????????,???????????????");
            }
            if ("FAIL".equals(currentRow.getAttribute("TransferStatus"))) {
                //???????????????????????????
                transferByBmHeaderId(currentRow);

            }
            if ("CANCEL_FAIL".equals(currentRow.getAttribute("TransferStatus"))) {
                //????????????????????????????????????
                transferCancelByBmHeaderId(currentRow);
            }
            if ("STORAGE_FAIL".equals(currentRow.getAttribute("TransferStatus"))) {
                //??????????????????
                transferStorageStep(currentRow);
            }
        }

        this.findIterator("BmsubcntrTransferHeaderT1Iterator").executeQuery();
        this.refreshComponent(this.getHistoryTable());
    }

    /**
     * @Override
     * ????????????????????????
     *
     * **/
    public void transferHistoryQueryListener(QueryEvent queryEvent) {
        // ??????
        this.setExpressionValue("#{pageFlowScope.transferAgainFlag}", "false");
        invokeQueryEventMethodExpression("#{bindings.BmsubcntrTransferHeaderTVOCriteriaQuery.processQuery}",
                                         queryEvent);
        //??????????????????????????????????????????????????? BmsubcntrTransferHeaderT1Iterator
        DBUtil dbUtil = new DBUtil();
        java.util.List transferList =
            dbUtil.queryResultBySql(DBUtil.STM_DS, "SELECT * FROM stm.STM_BM_SUBCNTR_TRANS_HEADER_V T " +
                                    "WHERE T.TRANSFER_STATUS IN('FAIL','STORAGE_FAIL','CANCEL_FAIL')" +
                                    "and t.project_Id ='" +
                                    this.getCurrentProjectId() + "'");
        if (transferList != null && transferList.size() > 0) {
            this.setExpressionValue("#{pageFlowScope.transferAgainFlag}",
                                    "true");
        }
        //        DCIteratorBinding transferIterator =
        //            this.findIterator("BmsubcntrTransferHeaderT1Iterator");
        //        transferIterator.setRangeSize(-1);
        //        Row[] allRow = transferIterator.getAllRowsInRange();
        //        for (Row row : allRow) {
        //            if ("FAIL".equals(row.getAttribute("TransferStatus")) ||
        //                "STORAGE_FAIL".equals(row.getAttribute("TransferStatus"))||
        //                "CANCEL_FAIL".equals(row.getAttribute("TransferStatus"))) {
        //                this.setExpressionValue("#{pageFlowScope.transferAgainFlag}",
        //                                        "true");
        //                  break;
        //            }
        //        }
        this.refreshComponent(this.getTransferLayout());

    }

    /**
     * @Override
     * ????????????????????????
     *
     * **/
    public void transferQueryListener(QueryEvent queryEvent) {
        invokeQueryEventMethodExpression("#{bindings.subcntrTransferCriteriaQuery.processQuery}",
                                         queryEvent);
        //????????????????????????
        this.getTransferButton().setDisabled(false);
        this.getCheckAll().setValue(false);
        this.refreshComponent(this.getTransferButton());
    }


    private void invokeQueryEventMethodExpression(String expression,
                                                  QueryEvent queryEvent) {
        FacesContext fctx = FacesContext.getCurrentInstance();
        ELContext elctx = fctx.getELContext();
        ExpressionFactory efactory =
            fctx.getApplication().getExpressionFactory();

        MethodExpression me =
            efactory.createMethodExpression(elctx, expression, Object.class,
                                            new Class[] { QueryEvent.class });
        me.invoke(elctx, new Object[] { queryEvent });

    }

    public void setTransferAgainButton(RichCommandToolbarButton transferAgainButton) {
        this.transferAgainButton = transferAgainButton;
    }

    public RichCommandToolbarButton getTransferAgainButton() {
        return transferAgainButton;
    }

    public void setTransferButton(RichCommandToolbarButton transferButton) {
        this.transferButton = transferButton;
    }

    public RichCommandToolbarButton getTransferButton() {
        return transferButton;
    }

    public void historyBackListener(ActionEvent actionEvent) {
        this.executeAction("back");
    }

    /**
     * =
     * ????????????????????????,??????????????????
     * CURRENTROW ???transferHeaderRow
     * */
    private void transferByBmHeaderId(Row currentRow) {
        //?????????????????????????????????
        if ("DEDUCTION".equals(currentRow.getAttribute("BmDocumentType")) &&
            !"CANCEL".equals(currentRow.getAttribute("TransferType"))) {
            //                adfLogger.log(ADFLogger.NOTIFICATION, "DEDUCTION");
            //                adfLogger.log(ADFLogger.NOTIFICATION, "?????????????????????????????????");

            Querydebitcreditbpel_client_ep querydebitcreditbpel_client_ep =
                new Querydebitcreditbpel_client_ep();
            QueryDebitCreditBpel queryDebitCreditBpel =
                querydebitcreditbpel_client_ep.getQueryDebitCreditBpel_pt();
            cn.com.cscec8b.proxy.bm.subcntrdebit.types.RequestType req =
                new cn.com.cscec8b.proxy.bm.subcntrdebit.types.RequestType();
            req.getBmDocumentNumber().add((String)currentRow.getAttribute("BmDocumentNumber"));
            cn.com.cscec8b.proxy.bm.subcntrdebit.types.ResponseType response =
                queryDebitCreditBpel.process(req);
            if ("S".equals(response.getCode())) {
                this.addInfoMessage("??????", "??????????????????,?????????...");
                OperationBinding funcBinding =
                    this.findOperation("updateHeaderStatus");
                funcBinding.getParamsMap().put("documentNumber",
                                               currentRow.getAttribute("DocumentNumber"));
                funcBinding.getParamsMap().put("bmDocumentId",
                                               currentRow.getAttribute("BmDocumentId"));
                funcBinding.getParamsMap().put("bmDocumentType", "DEDUCTION");
                funcBinding.getParamsMap().put("status", "TRANSFERING");
                funcBinding.execute();
                this.findIterator("BmsubcntrTransferHeaderT1Iterator").executeQuery();
                this.refreshComponent(this.getHistoryTable());
            } else {
                this.addErrorMessage("??????", "????????????,?????????");
            }
        }
        //????????????
        if ("MEASURE".equals(currentRow.getAttribute("BmDocumentType")) &&
            !"CANCEL".equals(currentRow.getAttribute("TransferType"))) {
            Queryauditbpel_client_ep queryauditbpel_client_ep =
                new Queryauditbpel_client_ep();
            QueryAuditBpel queryAuditBpel =
                queryauditbpel_client_ep.getQueryAuditBpel_pt();
            cn.com.cecec8b.proxy.bm.subcntraudit.types.RequestType req =
                new cn.com.cecec8b.proxy.bm.subcntraudit.types.RequestType();

            req.getBmDocumentNumber().add((String)currentRow.getAttribute("KeyDocumentNumber"));
            cn.com.cecec8b.proxy.bm.subcntraudit.types.ResponseType response =
                queryAuditBpel.process(req);
            if ("S".equals(response.getCode())) {
                this.addInfoMessage("??????", "??????????????????,?????????...");
                OperationBinding funcBinding =
                    this.findOperation("updateHeaderStatus");
                funcBinding.getParamsMap().put("documentNumber",
                                               currentRow.getAttribute("DocumentNumber"));
                funcBinding.getParamsMap().put("bmDocumentId",
                                               currentRow.getAttribute("BmDocumentId"));
                funcBinding.getParamsMap().put("bmDocumentType", "MEASURE");
                funcBinding.getParamsMap().put("status", "TRANSFERING");
                funcBinding.execute();
                this.findIterator("BmsubcntrTransferHeaderT1Iterator").executeQuery();

                this.refreshComponent(this.getHistoryTable());

            } else {
                this.addErrorMessage("??????", "????????????,?????????");
            }
        }
        //??????????????????
        if ("VISA".equals(currentRow.getAttribute("BmDocumentType")) &&
            !"CANCEL".equals(currentRow.getAttribute("TransferType"))) {
            //            adfLogger.log(ADFLogger.NOTIFICATION, "VISA");
            //            adfLogger.log(ADFLogger.NOTIFICATION, "??????????????????");
            //
            Queryvisabpel_client_ep queryvisabpel_client_ep =
                new Queryvisabpel_client_ep();
            QueryVisaBpel queryVisaBpel =
                queryvisabpel_client_ep.getQueryVisaBpel_pt();
            cn.com.cscec8b.proxy.bm.subcntrvisa.types.RequestType req =
                new cn.com.cscec8b.proxy.bm.subcntrvisa.types.RequestType();
            req.getTransferDocumentNumber().add((String)currentRow.getAttribute("DocumentNumber"));
            cn.com.cscec8b.proxy.bm.subcntrvisa.types.ResponseType response =
                queryVisaBpel.process(req);
            if ("S".equals(response.getCode())) {
                this.addInfoMessage("??????", "??????????????????,?????????...");
                //                currentRow.setAttribute("TransferStatus", "TRANSFERING");
                //                this.findOperation("Commit").execute();
                OperationBinding funcBinding =
                    this.findOperation("updateHeaderStatus");
                funcBinding.getParamsMap().put("documentNumber",
                                               currentRow.getAttribute("DocumentNumber"));
                funcBinding.getParamsMap().put("bmDocumentId",
                                               currentRow.getAttribute("BmDocumentId"));
                funcBinding.getParamsMap().put("bmDocumentType", "VISA");
                funcBinding.getParamsMap().put("status", "TRANSFERING");
                funcBinding.execute();
                this.findIterator("BmsubcntrTransferHeaderT1Iterator").executeQuery();

                this.refreshComponent(this.getHistoryTable());

            } else {
                this.addErrorMessage("??????", "????????????,?????????");
            }
        }
    }

    /**
     *
     * ????????????????????????
     * **/
    private void transferStorageStep(Row currentRow) {
        //????????????????????????
        if ("VISA".equals(currentRow.getAttribute("BmDocumentType"))) {
            //                adfLogger.log(ADFLogger.NOTIFICATION, "VISA");
            //                adfLogger.log(ADFLogger.NOTIFICATION, "????????????????????????");
            //
            Queryreceivevisabpel_client_ep queryreceivevisabpel_client_ep =
                new Queryreceivevisabpel_client_ep();
            QueryReceiveVisaBpel queryReceiveVisaBpel =
                queryreceivevisabpel_client_ep.getQueryReceiveVisaBpel_pt();
            RequestType req = new RequestType();
            req.getTransferDocumentNumber().add((String)currentRow.getAttribute("DocumentNumber"));
            ResponseType response = queryReceiveVisaBpel.process(req);
            if ("S".equals(response.getCode())) {
                this.addInfoMessage("??????", "??????????????????,?????????...");
                OperationBinding funcBinding =
                    this.findOperation("updateHeaderStatus");
                funcBinding.getParamsMap().put("documentNumber",
                                               currentRow.getAttribute("DocumentNumber"));
                funcBinding.getParamsMap().put("bmDocumentId",
                                               currentRow.getAttribute("BmDocumentId"));
                funcBinding.getParamsMap().put("bmDocumentType", "VISA");
                funcBinding.getParamsMap().put("status", "TRANSFERING");
                funcBinding.execute();
                this.findIterator("BmsubcntrTransferHeaderT1Iterator").executeQuery();

                this.refreshComponent(this.getHistoryTable());

            } else {
                this.addErrorMessage("??????", "????????????,?????????");
            }
        }
        //??????????????????
        if ("MEASURE".equals(currentRow.getAttribute("BmDocumentType"))) {
            //                adfLogger.log(ADFLogger.NOTIFICATION, "MEASURE");
            //                adfLogger.log(ADFLogger.NOTIFICATION, "??????????????????");
            Queryreceivreportbpel_client_ep queryreceivreportbpel_client_ep =
                new Queryreceivreportbpel_client_ep();
            QueryReceivReportBpel queryReceivReportBpel =
                queryreceivreportbpel_client_ep.getQueryReceivReportBpel_pt();
            cn.com.cecec8b.proxy.bm.receivreport.types.RequestType req =
                new cn.com.cecec8b.proxy.bm.receivreport.types.RequestType();
            Report report = new Report();
            report.setBmDocumentNumber((String)currentRow.getAttribute("BmDocumentNumber"));
            report.setTransferDocumentNumber((String)currentRow.getAttribute("DocumentNumber"));
            req.getReport().add(report);
            cn.com.cecec8b.proxy.bm.receivreport.types.ResponseType response =
                queryReceivReportBpel.process(req);
            if ("S".equals(response.getCode())) {
                this.addInfoMessage("??????", "??????????????????,?????????...");
                //currentRow.setAttribute("TransferStatus", "TRANSFERING");
                OperationBinding funcBinding =
                    this.findOperation("updateHeaderStatus");
                funcBinding.getParamsMap().put("documentNumber",
                                               currentRow.getAttribute("DocumentNumber"));
                funcBinding.getParamsMap().put("bmDocumentId",
                                               currentRow.getAttribute("BmDocumentId"));
                funcBinding.getParamsMap().put("bmDocumentType", "MEASURE");
                funcBinding.getParamsMap().put("status", "TRANSFERING");
                funcBinding.execute();
                //                    this.findOperation("Commit").execute();
                //                    this.findIterator("BmsubcntrTransferHeaderT1Iterator").executeQuery();

                this.refreshComponent(this.getHistoryTable());

            } else {
                this.addErrorMessage("??????", "????????????,?????????");
            }

        }
    }

    /**
     * ????????????????????????(???????????????????????????,??????type?????????)
     *
     *
     * **/
    private void transferCancelByBmHeaderId(Row currentRow) {
        //????????????
        if ("MEASURE".equals(currentRow.getAttribute("BmDocumentType")) &&
            "CANCEL".equals(currentRow.getAttribute("TransferType"))) {
            Querycancelauditbpel_client_ep querycancelauditbpel_client_ep =
                new Querycancelauditbpel_client_ep();
            QueryCancelAuditBpel queryCancelAuditBpel =
                querycancelauditbpel_client_ep.getQueryCancelAuditBpel_pt();
            RequestCancelAuditType req = new RequestCancelAuditType();
            CancelAuditType audit = new CancelAuditType();
            audit.setTransferDocumentNumber((String)currentRow.getAttribute("DocumentNumber"));
            req.getCancelAudit().add(audit);
            ResponseCancelAuditType response =
                queryCancelAuditBpel.process(req);
            if ("S".equals(response.getCode())) {
                this.addInfoMessage("??????", "??????????????????,?????????...");
                OperationBinding funcBinding =
                    this.findOperation("updateHeaderStatus");
                funcBinding.getParamsMap().put("documentNumber",
                                               currentRow.getAttribute("DocumentNumber"));
                funcBinding.getParamsMap().put("bmDocumentId",
                                               currentRow.getAttribute("BmDocumentId"));
                funcBinding.getParamsMap().put("bmDocumentType", "MEASURE");
                funcBinding.getParamsMap().put("status", "TRANSFERING");
                funcBinding.execute();
                //                    currentRow.setAttribute("TransferStatus", "TRANSFERING");
                //                    this.findOperation("Commit").execute();
                this.findIterator("BmsubcntrTransferHeaderT1Iterator").executeQuery();
                this.refreshComponent(this.getHistoryTable());

            } else {
                this.addErrorMessage("??????", "????????????,?????????");
            }

        }
        //??????????????????
        if ("VISA".equals(currentRow.getAttribute("BmDocumentType")) &&
            "CANCEL".equals(currentRow.getAttribute("TransferType"))) {

            Querycancelvisabpel_client_ep querycancelvisabpel_client_ep =
                new Querycancelvisabpel_client_ep();
            QueryCancelVisaBpel queryCancelVisaBpel =
                querycancelvisabpel_client_ep.getQueryCancelVisaBpel_pt();
            RequestCancelVisaType req = new RequestCancelVisaType();
            ResponseCancelVisaType response = new ResponseCancelVisaType();
            CancelVisaType visa = new CancelVisaType();
            visa.setTransferDocumentNumber((String)currentRow.getAttribute("DocumentNumber"));
            req.setCancelVisa(visa);
            response = queryCancelVisaBpel.process(req);
            if ("S".equals(response.getCode())) {
                this.addInfoMessage("??????", "??????????????????,?????????...");
                OperationBinding funcBinding =
                    this.findOperation("updateHeaderStatus");
                funcBinding.getParamsMap().put("documentNumber",
                                               currentRow.getAttribute("DocumentNumber"));
                funcBinding.getParamsMap().put("bmDocumentId",
                                               currentRow.getAttribute("BmDocumentId"));
                funcBinding.getParamsMap().put("bmDocumentType", "VISA");
                funcBinding.getParamsMap().put("status", "TRANSFERING");
                funcBinding.execute();
                //                currentRow.setAttribute("TransferStatus", "TRANSFERING");
                //                this.findOperation("Commit").execute();
                this.findIterator("BmsubcntrTransferHeaderT1Iterator").executeQuery();

                this.refreshComponent(this.getHistoryTable());

            } else {
                this.addErrorMessage("??????", "????????????,?????????");
            }
        }

    }

    public void goHistoryListener(ActionEvent actionEvent) {
        this.setExpressionValue("#{pageFlowScope.transferAgainFlag}", "false");
        DBUtil dbUtil = new DBUtil();
        java.util.List transferList =
            dbUtil.queryResultBySql(DBUtil.STM_DS, "SELECT * FROM stm.STM_BM_SUBCNTR_TRANS_HEADER_V T " +
                                    "WHERE T.TRANSFER_STATUS IN('FAIL','STORAGE_FAIL','CANCEL_FAIL')" +
                                    "and t.project_Id ='" +
                                    this.getCurrentProjectId() + "'");
        if (transferList != null && transferList.size() > 0) {
            this.setExpressionValue("#{pageFlowScope.transferAgainFlag}",
                                    "true");
        }

        this.executeAction("goHistory");

    }

    public void setTransferLayout(RichPanelStretchLayout transferLayout) {
        this.transferLayout = transferLayout;
    }

    public RichPanelStretchLayout getTransferLayout() {
        return transferLayout;
    }


    public void setCheckAll(RichSelectBooleanCheckbox checkAll) {
        this.checkAll = checkAll;
    }

    public RichSelectBooleanCheckbox getCheckAll() {
        return checkAll;
    }

    public void setHistoryTable(RichTable historyTable) {
        this.historyTable = historyTable;
    }

    public RichTable getHistoryTable() {
        return historyTable;
    }

    public void rollbackRecordActionListener(ActionEvent actionEvent) {
        DCIteratorBinding transferIterator =
            this.findIterator("BmsubcntrTransferHeaderT1Iterator");
        Row currentRow = transferIterator.getCurrentRow();
        if (currentRow != null) {
            String transferStatus =
                (String)currentRow.getAttribute("TransferStatus");
            if (transferStatus == null) {
                return;
            }
            if ("SUCCESS".equals(transferStatus) ||
                "TRANSFERING".equals(transferStatus)) {
                this.addErrorMessage("??????", "??????????????????????????????????????????????????????!");
                return;
            }
            this.showPopupActionListener(actionEvent,
                                         "?????????????????????????????????????????????????????????????????????????????????,???????????????????????????????????????",
                                         "#{backingBeanScope.subcntrTransferBean.rollbackConfirmListener}");
        }
    }


    /**
     *
     * ????????????
     *
     * **/
    public void rollbackConfirmListener(DialogEvent dialogEvent) {
        DCIteratorBinding transferIterator =
            this.findIterator("BmsubcntrTransferHeaderT1Iterator");
        Row currentRow = transferIterator.getCurrentRow();
        if ("ok".equals(dialogEvent.getOutcome().toString())) {
            DBUtil dbUtil = new DBUtil();
            String dcoumentNumber =
                (String)currentRow.getAttribute("DocumentNumber");
            String returnMsg =
                dbUtil.quickGetFunctionResult(DBUtil.STM_DS, "stm.stm_bm_subcntr_transfer_pkg.func_rollback_transfer_record(?)",
                                              new String[] { dcoumentNumber });
            if (returnMsg != null && "SUCCESS".equals(returnMsg)) {
                this.findOperation("Commit").execute();
                this.addInfoMessage("??????", "??????????????????????????????");
                transferIterator.executeQuery();
                this.refreshComponent(this.getTransferLayout());
            } else {
                this.findOperation("Rollback").execute();
                this.addInfoMessage("??????", "??????????????????,??????????????????");
            }
        }
    }
}
