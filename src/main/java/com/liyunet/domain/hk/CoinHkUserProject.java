package com.liyunet.domain.hk;

/**
 * 币管家用户投资记录表
 * 
 * @author Administrator
 *
 */
public class CoinHkUserProject {
	private Integer id;
	// 投资项目id
	private Integer pid;
	// 收益率
	private String profit;
	// 前十额外收益率
	private String additional;
	// 投资金数量
	private String bidtNum;
	// 收益数量
	private String profitNum;
	// 创建时间
	private String createtime;
	// 订单号
	private String billNo;
	// 审核状态 0默认 1通过 2审核中 4驳回
	private Integer state;
	// 用户id
	private Integer userid;
	// 驳回理由
	private String reason;
	// 支付类型
	private Integer type;
	// 到账时间
	private String paymentDate;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getPid() {
		return pid;
	}

	public void setPid(Integer pid) {
		this.pid = pid;
	}

	public String getProfit() {
		return profit;
	}

	public void setProfit(String profit) {
		this.profit = profit == null ? null : profit.trim();
	}

	public String getAdditional() {
		return additional;
	}

	public void setAdditional(String additional) {
		this.additional = additional == null ? null : additional.trim();
	}

	public String getBidtNum() {
		return bidtNum;
	}

	public void setBidtNum(String bidtNum) {
		this.bidtNum = bidtNum == null ? null : bidtNum.trim();
	}

	public String getProfitNum() {
		return profitNum;
	}

	public void setProfitNum(String profitNum) {
		this.profitNum = profitNum == null ? null : profitNum.trim();
	}

	public String getCreatetime() {
		return createtime;
	}

	public void setCreatetime(String createtime) {
		this.createtime = createtime == null ? null : createtime.trim();
	}

	public String getBillNo() {
		return billNo;
	}

	public void setBillNo(String billNo) {
		this.billNo = billNo == null ? null : billNo.trim();
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Integer getUserid() {
		return userid;
	}

	public void setUserid(Integer userid) {
		this.userid = userid;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason == null ? null : reason.trim();
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(String paymentDate) {
		this.paymentDate = paymentDate == null ? null : paymentDate.trim();
	}

}