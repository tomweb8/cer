package com.liyunet.service.impl;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.liyunet.common.constant.DateHelper;
import com.liyunet.common.password.AES;
import com.liyunet.common.util.DateUtile;
import com.liyunet.common.util.IpResourceLocation;
import com.liyunet.common.util.UrlLoad;
import com.liyunet.domain.FBTPrice;
import com.liyunet.domain.hk.CoinHkContract;
import com.liyunet.domain.hk.CoinHkContractExample;
import com.liyunet.domain.hk.CoinHkControl;
import com.liyunet.domain.hk.CoinHkControlExample;
import com.liyunet.domain.hk.CoinHkOrder;
import com.liyunet.domain.hk.CoinHkProjet;
import com.liyunet.domain.hk.CoinHkProjetExample;
import com.liyunet.domain.hk.CoinHkUserProject;
import com.liyunet.domain.hk.CoinHkUserProjectExample;
import com.liyunet.domain.hk.CoinHkUserProjectExample.Criteria;
import com.liyunet.exception.ServiceException;
import com.liyunet.mapper.community.PushCommonMapper;
import com.liyunet.mapper.hkMapper.CoinHkContractMapper;
import com.liyunet.mapper.hkMapper.CoinHkControlMapper;
import com.liyunet.mapper.hkMapper.CoinHkOrderMapper;
import com.liyunet.mapper.hkMapper.CoinHkProjetMapper;
import com.liyunet.mapper.hkMapper.CoinHkUserProjectMapper;
import com.liyunet.service.CoinHousekeeperService;
import com.liyunet.util.PushRefinedCalculation;
import com.liyunet.vo.hk.CoinHKUserProjectVo;

@Service("coinHousekeeperService")
@Transactional
public class CoinHousekeeperServiceImpl implements CoinHousekeeperService {

	@Autowired
	private CoinHkProjetMapper cm;
	@Autowired
	private CoinHkUserProjectMapper cum;
	@Autowired
	private CoinHkOrderMapper com;
	@Autowired
	private CoinHkControlMapper coinHkControlMapper;
	
	@Autowired
	private CoinHkContractMapper coinHkContractMapper;
	@Autowired
	private PushCommonMapper pushCommonMapper;

	// 查询所有项目
	@Override
	public Object getHKlist(Integer page, Integer rows) {
		// TODO Auto-generated method stub
		page = (page - 1) * rows;
		List<CoinHkProjet> list = cm.getHKlist(page, rows);
		return list;
	}

	@Override
	public Object getProfitBytoken(Integer userId) {
		// TODO Auto-generated method stub
		// 根据项目id查出项目结束时间，判断收益
		String formatedDateStr = DateHelper.getFormatedDateStr(new Date(), "yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		// 根据用户id和项目id查用户投资记录
		List<CoinHkUserProject> selectByExample = cum.getProfitBytoken(userId);
		Double yesterday = 0.0;
		Double sum = 0.0;
		Map<String, Object> map = new HashMap<String, Object>();
		for (int i = 0; i < selectByExample.size(); i++) {
			CoinHkProjet selectByPrimaryKey = cm.selectByPrimaryKey(selectByExample.get(i).getPid());
			String startTime = selectByPrimaryKey.getStartTime();
			Integer danum = Integer.parseInt(selectByPrimaryKey.getProjectCycle());
			String endTime = selectByPrimaryKey.getEndTime();
			Date stime = null;
			Date etime = null;
			Date pend = null;
			Date ntime = null;
			Date jtime = null;
			try {
				stime = sdf.parse(startTime);
				etime = DateUtile.addDate(sdf.parse(endTime), 1);
				pend = DateUtile.addDate(sdf.parse(selectByPrimaryKey.getEndTime()), danum + 1);
				ntime = sdf.parse(formatedDateStr);

				jtime = sdf.parse(endTime);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			long lstime = stime.getTime();
			// 投资结束时间+1
			long letime = etime.getTime();
			long lpend = pend.getTime();
			// 本息到账时间
			long lntime = ntime.getTime();
			// 投资结束时间
			long ljtime = jtime.getTime();
			if (lntime < letime) {
				yesterday = PushRefinedCalculation.add(yesterday, 0);
				sum = PushRefinedCalculation.add(sum, 0);
			}
			if (lntime >ljtime  && lntime < letime) {
				// 昨日收益为购买的数量*利率*1
				// 总计为购买的数量*利率*天数
				if (selectByExample != null && selectByExample.size() > 0) {
					// 数量
					Double bidtNum = Double.parseDouble(selectByExample.get(i).getBidtNum());
					// 利率
					Double Profit = Double.parseDouble(selectByExample.get(i).getProfit());
					// 前十额外利率
					Double additional = Double.parseDouble(selectByExample.get(i).getAdditional());
					// 天数
					// Double cycle =
					// Double.parseDouble(selectByPrimaryKey.getProjectCycle());

					// Double dnu = (double) ((lntime - ljtime) / (1000 * 3600 *
					// 24 * 365));
					// Double xxx=(double) (1/365);

					Double dnu =PushRefinedCalculation.div(365,Math.floor(PushRefinedCalculation.div(86400000.0, (lntime - ljtime))));
					Double xxx = PushRefinedCalculation.div(365, 0);
					yesterday = PushRefinedCalculation.add(yesterday, PushRefinedCalculation.mul(bidtNum,
							PushRefinedCalculation.mul(PushRefinedCalculation.add(Profit, additional), xxx)));
					sum = PushRefinedCalculation.add(sum, PushRefinedCalculation.mul(bidtNum,
							PushRefinedCalculation.mul(PushRefinedCalculation.add(Profit, additional), dnu)));

				} else {
					yesterday = PushRefinedCalculation.add(yesterday, 0);
					sum = PushRefinedCalculation.add(sum, 0);
				}

			}
			
			

			if (lntime > letime&&lntime<lpend) {
				// 昨日收益为购买的数量*利率*1
				// 总计为购买的数量*利率*天数
				if (selectByExample != null && selectByExample.size() > 0) {
					// 数量
					Double bidtNum = Double.parseDouble(selectByExample.get(i).getBidtNum());
					// 利率
					Double Profit = Double.parseDouble(selectByExample.get(i).getProfit());
					// 前十额外利率
					Double additional = Double.parseDouble(selectByExample.get(i).getAdditional());
					// 天数
					Double dnu =PushRefinedCalculation.div(365,Math.floor(PushRefinedCalculation.div(86400000.0, (lntime - ljtime))));
					
					
					Double xxx = PushRefinedCalculation.div(365, 1);
					yesterday = PushRefinedCalculation.add(yesterday, PushRefinedCalculation.mul(bidtNum,
							PushRefinedCalculation.mul(PushRefinedCalculation.add(Profit, additional), xxx)));
					sum = PushRefinedCalculation.add(sum, PushRefinedCalculation.mul(bidtNum,PushRefinedCalculation.mul(PushRefinedCalculation.add(Profit, additional), dnu)));

				} else {
					yesterday = PushRefinedCalculation.add(yesterday, 0);
					sum = PushRefinedCalculation.add(sum, 0);
				}
				
			}
				if (lntime >= lpend) {
					// 昨日收益为购买的数量*利率*1
					// 总计为购买的数量*利率*天数
					if (selectByExample != null && selectByExample.size() > 0) {
						// 数量
						Double bidtNum = Double.parseDouble(selectByExample.get(i).getBidtNum());
						// 利率
						Double Profit = Double.parseDouble(selectByExample.get(i).getProfit());
						// 前十额外利率
						Double additional = Double.parseDouble(selectByExample.get(i).getAdditional());
						// 天数
						Double cycle = PushRefinedCalculation.div(365,
								Double.parseDouble(selectByPrimaryKey.getProjectCycle()));

						yesterday = PushRefinedCalculation.add(yesterday, PushRefinedCalculation.mul(bidtNum,
								PushRefinedCalculation.mul(PushRefinedCalculation.add(Profit, additional), 0)));
						sum = PushRefinedCalculation.add(sum, PushRefinedCalculation.mul(bidtNum,
								PushRefinedCalculation.mul(PushRefinedCalculation.add(Profit, additional), cycle)));

					} else {
						yesterday = PushRefinedCalculation.add(yesterday, 0);
						sum = PushRefinedCalculation.add(sum, 0);
					}
				
				}

			

		}

		map.put("yesterday", Math.floor(yesterday * 10) / 10 + "");
		map.put("sum", Math.floor(sum * 10) / 10 + "");

		return map;

	}

	// 根据id查项目
	@Override
	public Object getHKbyId(Integer id) {
		// TODO Auto-generated method stub
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			CoinHkProjet selectByPrimaryKey = cm.selectByPrimaryKey(id);

			String startTime = selectByPrimaryKey.getStartTime();
			Integer danum = Integer.parseInt(selectByPrimaryKey.getProjectCycle());
			String endTime = selectByPrimaryKey.getEndTime();

			// 根据id查出人数
			CoinHkUserProjectExample cue = new CoinHkUserProjectExample();
			Criteria cuc = cue.createCriteria();
			cuc.andPidEqualTo(id);
			cuc.andStateNotEqualTo(4);
			//结束时间
			Date parse = sdf.parse(endTime);
			  long etime=parse.getTime()+60000;
			  
				Date date=new Date(etime);
				String format = sdf.format(date);
			
			int countByExample = cum.countByExample(cue);
			// 查数量
			Double coinnum = cum.selectCoinCount(id) == null ? 0 : cum.selectCoinCount(id);

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("peoplenum", selectByPrimaryKey.getDummyNum() + countByExample);
			map.put("coinnum", Double.parseDouble(selectByPrimaryKey.getSubstitutesone()) + coinnum);
			map.put("CoinHkProjet", selectByPrimaryKey);
			// map.put("dateone",
			// sdf.format(DateUtile.addDate(sdf.parse(endTime), 1)));
			map.put("dateone", format);
			map.put("datetwo", sdf.format(DateUtile.addDate(sdf.parse(endTime), danum)));
			return map;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	// 支付
	@Override
	public Object hkpay(Integer userId, Integer pid, String num, Integer type, String address) {
		// TODO Auto-generated method stub

		CoinHkProjet selectByPrimaryKey = cm.selectByPrimaryKey(pid);
		String profit = selectByPrimaryKey.getProfit();
		Integer dummyNum = selectByPrimaryKey.getDummyNum();
		String projectCycle = selectByPrimaryKey.getProjectCycle();
		String minQuota = selectByPrimaryKey.getMinQuota();
		String maxQuota = selectByPrimaryKey.getMaxQuota();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String startTime = selectByPrimaryKey.getStartTime();
		String endTime = selectByPrimaryKey.getEndTime();
		String newTime = DateHelper.getFormatedDateStr(new Date(), "yyyy-MM-dd HH:mm:ss");
		Date startT = null;
		Date endT = null;
		Date newT = null;
		try {
			startT = sdf.parse(startTime);
			endT = sdf.parse(endTime);
			newT = sdf.parse(newTime);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (newT.getTime() < startT.getTime()) {
			throw ServiceException.userException("", "项目未开启");
		}
		if (newT.getTime() > endT.getTime()) {
			throw ServiceException.userException("", "项目已结束");

		}
		double v = Double.parseDouble(num);
		if (v <= 0) {
			throw ServiceException.userException("", "投资金额不能小于0!");
		}
		if (v > Double.parseDouble(maxQuota)) {
			throw ServiceException.userException("", "投资金额不能大于最大限额!");

		}
		if (v < Double.parseDouble(minQuota)) {
			throw ServiceException.userException("", "投资金额不能小于最小限额!");

		}

		// 根据用户id和pid查项目
		CoinHkUserProjectExample coinHkUserExample = new CoinHkUserProjectExample();
		Criteria createCriteria3 = coinHkUserExample.createCriteria();
		createCriteria3.andUseridEqualTo(userId);
		createCriteria3.andPidEqualTo(pid);
		createCriteria3.andStateNotEqualTo(4);
		List<CoinHkUserProject> selectByExample2 = cum.selectByExample(coinHkUserExample);
		if (selectByExample2 != null && selectByExample2.size() > 0) {
			throw ServiceException.userException("", "一个项目只能投资一次");
		}

		// 查额外奖励利率
		CoinHkControlExample coinHkControlExample = new CoinHkControlExample();
		com.liyunet.domain.hk.CoinHkControlExample.Criteria createCriteria = coinHkControlExample.createCriteria();
		createCriteria.andStateEqualTo(1);
		List<CoinHkControl> selectByExample = coinHkControlMapper.selectByExample(coinHkControlExample);
		String dumControl = selectByExample.get(0).getDumControl();
		int insert = 0;
		// 查找通过的人数
		CoinHkUserProjectExample coinHkUserProjectExample = new CoinHkUserProjectExample();
		Criteria createCriteria2 = coinHkUserProjectExample.createCriteria();
		createCriteria2.andStateEqualTo(1);
		createCriteria2.andPidEqualTo(pid);
		int countByExample = cum.countByExample(coinHkUserProjectExample);
		// 币达钱包
		if (type == 1) {
			// 调用币达钱包
			String timestampStr = System.currentTimeMillis() + "";
			String billno = UUID.randomUUID().toString();
			String formatedDateStr = DateHelper.getFormatedDateStr(new Date(), "yyyy-MM-dd HH:mm:ss");
			String s = null;

			double mul = PushRefinedCalculation.mul(10, v);
			try {
				String encode1 = URLEncoder.encode(AES.AESEncode("LYWH@#$!32", userId + ""), "UTF-8");
				String encode2 = URLEncoder.encode(AES.AESEncode("LYWH@#$!32", "618"), "UTF-8");
				String encode3 = URLEncoder.encode(AES.AESEncode("9$dz4Y33oy", mul + ""), "UTF-8");
				String encode4 = URLEncoder.encode(AES.AESEncode("9$dz4Y33oy", "yobXT2ENRb" + timestampStr), "UTF-8");
				s = UrlLoad.load(IpResourceLocation.TT_EGGWORLD_IP + "/ttc-eggworld/ttc/record",
						"openId=" + encode1 + "&muchMoney=" + encode3 + "&transactionType=32"
								+ "&app_id=3360a88e4318896c9fe3e031e64541f9c176af67fa87634f&mch_id=yobXT2ENRb&timestampStr="
								+ timestampStr + "&sign=" + encode4 + "&toopenid=" + encode2 + "&poundageType=0"
								+ "&poundage=0");

			} catch (Exception e) {
				e.printStackTrace();
			}
			JSONObject jsonObject = JSON.parseObject(s);
			String string = jsonObject.getString("state");
			JSONObject jsonObject1 = JSON.parseObject(string);
			String code = jsonObject1.getString("code");
			if ("20000".equals(code)) {
				// 存下订单
				CoinHkOrder coinHkOrder = new CoinHkOrder();
				coinHkOrder.setBillNo(billno);
				coinHkOrder.setCreatetime(formatedDateStr);
				coinHkOrder.setPayNum(num);
				coinHkOrder.setState(2);
				coinHkOrder.setToblockid(618);
				coinHkOrder.setType(1);
				coinHkOrder.setUserid(userId);
				int insert2 = com.insert(coinHkOrder);
				// 存下投资记录

				CoinHkUserProject coinHkUserProject = new CoinHkUserProject();
				coinHkUserProject.setBillNo(billno);
				coinHkUserProject.setType(type);
				// 额外
				String addnum = "";
				if (dummyNum + countByExample < 10) {
					coinHkUserProject.setAdditional(dumControl);
					addnum = dumControl;
				} else {
					coinHkUserProject.setAdditional("0");
					addnum = "0";
				}

				coinHkUserProject.setBidtNum(num);
				coinHkUserProject.setPid(pid);
				coinHkUserProject.setCreatetime(formatedDateStr);
				// 收益率
				coinHkUserProject.setProfit(profit);
				// 收益数量
				double profitDouble = Double.parseDouble(profit);
				double numDouble = Double.parseDouble(num);
				double dumControlDouble = Double.parseDouble(addnum);
				double projectCycleDouble =PushRefinedCalculation.div(365,Double.parseDouble(projectCycle));
				double mul2 = PushRefinedCalculation.mul(PushRefinedCalculation.mul(numDouble,
						PushRefinedCalculation.add(profitDouble, dumControlDouble)), projectCycleDouble);
				coinHkUserProject.setProfitNum(Math.floor(mul2 * 10) / 10 + "");
				coinHkUserProject.setState(1);
				coinHkUserProject.setUserid(userId);
				int a = cum.insertSelectKey(coinHkUserProject);
				insert = coinHkUserProject.getId();
			} else if ("20002".equals(code)) {

				String msg = jsonObject1.getString("msg");
				if (msg.equals("success")) {
					throw ServiceException.userException("", "未完成kyc二级认证");
				} else {
					throw ServiceException.userException("", msg);
				}
			}

			//

		} else {
			String formatedDateStr = DateHelper.getFormatedDateStr(new Date(), "yyyy-MM-dd HH:mm:ss");
			String billno = UUID.randomUUID().toString();
			// token钱包
			// 存下订单
			CoinHkOrder coinHkOrder = new CoinHkOrder();
			coinHkOrder.setBillNo(billno);
			coinHkOrder.setCreatetime(formatedDateStr);
			coinHkOrder.setPayNum(num);
			coinHkOrder.setState(2);
			coinHkOrder.setType(2);
			coinHkOrder.setUserid(userId);
			coinHkOrder.setTokenAddress(address);
			com.insert(coinHkOrder);
			// 存下投资记录

			CoinHkUserProject coinHkUserProject = new CoinHkUserProject();
			coinHkUserProject.setBillNo(billno);
			coinHkUserProject.setType(type);
			// 额外
			String addnum = "";
			if (dummyNum + countByExample < 10) {
				coinHkUserProject.setAdditional(dumControl);
				addnum = dumControl;
			} else {
				coinHkUserProject.setAdditional("0");
				addnum = "0";
			}

			coinHkUserProject.setBidtNum(num);
			coinHkUserProject.setPid(pid);
			coinHkUserProject.setCreatetime(formatedDateStr);
			// 收益率
			coinHkUserProject.setProfit(profit);
			// 收益数量
			double profitDouble = Double.parseDouble(profit);
			double numDouble = Double.parseDouble(num);
			double dumControlDouble = Double.parseDouble(addnum);
			double projectCycleDouble =PushRefinedCalculation.div(365,Double.parseDouble(projectCycle));
			double mul2 = PushRefinedCalculation.mul(
					PushRefinedCalculation.mul(numDouble, PushRefinedCalculation.add(profitDouble, dumControlDouble)),
					projectCycleDouble);
			coinHkUserProject.setProfitNum(Math.floor(mul2 * 10) / 10 + "");
			coinHkUserProject.setState(2);
			coinHkUserProject.setUserid(userId);
			int a = cum.insertSelectKey(coinHkUserProject);
			insert = coinHkUserProject.getId();

		}
		return insert;
	}

	// 获取总资产
	@Override
	public Object getHkcount(Integer userId) {
		// TODO Auto-generated method stub
		String formatedDateStr = DateHelper.getFormatedDateStr(new Date(), "yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		// 根据用户id和项目id查用户投资记录
		CoinHkUserProjectExample cup = new CoinHkUserProjectExample();
		Criteria cupc = cup.createCriteria();
		cupc.andUseridEqualTo(userId);
		cupc.andStateEqualTo(1);
		List<CoinHkUserProject> selectByExample = cum.selectByExample(cup);
		Double Principal = 0.0;
		Double sum = 0.0;
		Map<String, Object> map = new HashMap<String, Object>();
		for (int i = 0; i < selectByExample.size(); i++) {
			CoinHkProjet selectByPrimaryKey = cm.selectByPrimaryKey(selectByExample.get(i).getPid());
			String startTime = selectByPrimaryKey.getStartTime();
			Integer danum = Integer.parseInt(selectByPrimaryKey.getProjectCycle());
			String endTime = selectByPrimaryKey.getEndTime();
			Double bidtNum = Double.parseDouble(selectByExample.get(i).getBidtNum());
			// 利率
			Double Profit = Double.parseDouble(selectByExample.get(i).getProfit());
			// 前十额外利率
			Double additional = Double.parseDouble(selectByExample.get(i).getAdditional());

			Date stime = null;
			Date etime = null;
			Date pend = null;
			Date ntime = null;
			Date jtime = null;
			try {
				stime = sdf.parse(startTime);
				etime = DateUtile.addDate(sdf.parse(endTime), 1);
				pend = DateUtile.addDate(sdf.parse(selectByPrimaryKey.getEndTime()), danum + 1);
				ntime = sdf.parse(formatedDateStr);
				jtime = sdf.parse(endTime);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			long lstime = stime.getTime();
			long letime = etime.getTime();
			long lpend = pend.getTime();
			long lntime = ntime.getTime();
			long ljtime = jtime.getTime();
			if (lntime < ljtime) {
				Principal = PushRefinedCalculation.add(Principal, bidtNum);
				sum = PushRefinedCalculation.add(sum, 0);
			}
			if (lntime > ljtime && lntime <lpend ) {
				// 昨日收益为购买的数量*利率*1
				// 总计为购买的数量*利率*天数
				if (selectByExample != null && selectByExample.size() > 0) {
					// 数量
					// 天数
					// Double cycle =
					// Double.parseDouble(selectByPrimaryKey.getProjectCycle());
					Double dnu =PushRefinedCalculation.div(365,Math.floor(PushRefinedCalculation.div(86400000.0, (lntime - ljtime))));
					sum = PushRefinedCalculation.add(sum, PushRefinedCalculation.mul(bidtNum,PushRefinedCalculation.mul(PushRefinedCalculation.add(Profit, additional), dnu)));
					Principal = PushRefinedCalculation.add(Principal, bidtNum);

				} else {
					sum = PushRefinedCalculation.add(sum, 0);
					Principal = PushRefinedCalculation.add(Principal, bidtNum);
				}

			}

			if (lntime > lpend) {
				// 昨日收益为购买的数量*利率*1
				// 总计为购买的数量*利率*天数
				if (selectByExample != null && selectByExample.size() > 0) {
					// 天数
					Double cycle = PushRefinedCalculation.div(365, Double.parseDouble(selectByPrimaryKey.getProjectCycle()));

					sum = PushRefinedCalculation.add(sum, PushRefinedCalculation.mul(bidtNum,
							PushRefinedCalculation.mul(PushRefinedCalculation.add(Profit, additional), cycle)));
					Principal = PushRefinedCalculation.add(Principal, bidtNum);

				} else {
					sum = PushRefinedCalculation.add(sum, 0);
					Principal = PushRefinedCalculation.add(Principal, bidtNum);
				}

			}

		}
		//正在审核中的
		
		CoinHkUserProjectExample cup1 = new CoinHkUserProjectExample();
		Criteria cupc1 = cup1.createCriteria();
		cupc1.andUseridEqualTo(userId);
		cupc1.andStateEqualTo(2);
		List<CoinHkUserProject> selectByExample1 = cum.selectByExample(cup1);
		for(int i=0;i<selectByExample1.size();i++){
			double parseDouble = Double.parseDouble(selectByExample1.get(i).getBidtNum());
			Principal=PushRefinedCalculation.add(Principal,parseDouble);
		}
		
		List<FBTPrice> fbtPriceList = pushCommonMapper.getFBTPrice();
		String pNes = fbtPriceList.get(0).getPrice();
		map.put("sum", PushRefinedCalculation.add(Math.floor(sum * 10) / 10, Principal) + "");
		map.put("rmb",
				Math.ceil(PushRefinedCalculation.mul(PushRefinedCalculation.add(Math.floor(sum * 10) / 10, Principal),
						Double.parseDouble(pNes)) * 100) / 100);
		return map;
	}

	// 获取总人数
	@Override
	public Object getHKpeoplenum() {
		// TODO Auto-generated method stub
		CoinHkControl selectByPrimaryKey = coinHkControlMapper.selectByPrimaryKey(1);
		
		List<CoinHkProjet> selectByExample = cm.selectByExample(null);
		int sum = 0;
		for (int i = 0; i < selectByExample.size(); i++) {
			sum = sum + selectByExample.get(i).getDummyNum();
		}
		CoinHkUserProjectExample example = new CoinHkUserProjectExample();
		Criteria createCriteria = example.createCriteria();
		createCriteria.andStateNotEqualTo(4);
		int countByExample = cum.countByExample(example);
		sum = sum + countByExample;
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("peoplenum", sum);
		map.put("countdown", selectByPrimaryKey.getTimeControl());

		return map;
	}

	@Override
	public Object getHKpayinfo(Integer userId, Integer page, Integer rows) {
		page = (page - 1) * rows;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// TODO Auto-generated method stub
		CoinHkUserProjectExample coinHkUserProjectExample = new CoinHkUserProjectExample();
		Criteria createCriteria = coinHkUserProjectExample.createCriteria();
		createCriteria.andUseridEqualTo(userId);
		// List<CoinHkUserProject> selectByExample =
		// cum.selectByExample(coinHkUserProjectExample);
		List<CoinHkUserProject> selectByExample = cum.selectHKPayList(userId, page, rows);
		List<CoinHKUserProjectVo> list = new ArrayList<CoinHKUserProjectVo>();
		for (int i = 0; i < selectByExample.size(); i++) {
			CoinHkProjet selectByPrimaryKey = cm.selectByPrimaryKey(selectByExample.get(i).getPid());
			String startTime = selectByPrimaryKey.getStartTime();
			String endTime = selectByPrimaryKey.getEndTime();
			String newTime = DateHelper.getFormatedDateStr(new Date(), "yyyy-MM-dd HH:mm:ss");
			Date startT = null;
			Date endT = null;
			Date newT = null;
			Date penT = null;
			try {
				startT = sdf.parse(startTime);
				endT = sdf.parse(endTime);
				newT = sdf.parse(newTime);
				penT = DateUtile.addDate(endT, Integer.parseInt(selectByPrimaryKey.getProjectCycle()));
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			if (selectByExample.get(i).getState() == 1) {
				// 判断时间

				CoinHKUserProjectVo cv = new CoinHKUserProjectVo();
				cv.setCycle(selectByPrimaryKey.getProjectCycle());
				cv.setAdditional(selectByExample.get(i).getAdditional());
				cv.setBidtNum(selectByExample.get(i).getBidtNum());
				cv.setBillNo(selectByExample.get(i).getBillNo());
				cv.setCreatetime(selectByExample.get(i).getCreatetime());
				cv.setId(selectByExample.get(i).getId());
				cv.setPid(selectByExample.get(i).getPid());
				cv.setProfit(selectByExample.get(i).getProfit());
				cv.setProfitNum(selectByExample.get(i).getProfitNum());
				cv.setReason(selectByExample.get(i).getReason());
				if (newT.getTime() > startT.getTime() && newT.getTime() < endT.getTime()) {
					cv.setState(0);
				} else if (newT.getTime() > endT.getTime() && newT.getTime() < penT.getTime()) {
					cv.setState(1);
				} else {
					cv.setState(5);
				}
				list.add(cv);
			} else {

				CoinHKUserProjectVo cv = new CoinHKUserProjectVo();
				cv.setCycle(selectByPrimaryKey.getProjectCycle());
				cv.setAdditional(selectByExample.get(i).getAdditional());
				cv.setBidtNum(selectByExample.get(i).getBidtNum());
				cv.setBillNo(selectByExample.get(i).getBillNo());
				cv.setCreatetime(selectByExample.get(i).getCreatetime());
				cv.setId(selectByExample.get(i).getId());
				cv.setPid(selectByExample.get(i).getPid());
				cv.setProfit(selectByExample.get(i).getProfit());
				cv.setProfitNum(selectByExample.get(i).getProfitNum());
				cv.setReason(selectByExample.get(i).getReason());
				cv.setState(selectByExample.get(i).getState());
				list.add(cv);
			}

		}

		return list;
	}

	// 差详情
	@Override
	public Object getHKpaydetailsById(Integer id) {
		// TODO Auto-generated method stub
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		CoinHkUserProject selectByPrimaryKey2 = cum.selectByPrimaryKey(id);
		CoinHkProjet selectByPrimaryKey = cm.selectByPrimaryKey(selectByPrimaryKey2.getPid());
		String startTime = selectByPrimaryKey.getStartTime();
		String endTime = selectByPrimaryKey.getEndTime();
		String newTime = DateHelper.getFormatedDateStr(new Date(), "yyyy-MM-dd HH:mm:ss");
		Date startT = null;
		Date endT = null;
		Date newT = null;
		Date penT = null;
		try {
			startT = sdf.parse(startTime);
			endT = sdf.parse(endTime);
			newT = sdf.parse(newTime);
			penT = DateUtile.addDate(endT, Integer.parseInt(selectByPrimaryKey.getProjectCycle()));
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		CoinHKUserProjectVo cv = new CoinHKUserProjectVo();
		if (selectByPrimaryKey2.getState() == 1) {
			cv.setStartTime(selectByPrimaryKey.getStartTime());
			cv.setEndTime(selectByPrimaryKey.getEndTime());
			try {

				Date parse = sdf.parse(selectByPrimaryKey.getEndTime());
				  long estime=parse.getTime()+60000;
				  
					Date date=new Date(estime);
					String format = sdf.format(date);
				cv.setDateone(format);
				cv.setDatetwo(
						sdf.format(DateUtile.addDate(endT, Integer.parseInt(selectByPrimaryKey.getProjectCycle()))));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// 判断时间
			cv.setPaymentDate(selectByPrimaryKey2.getPaymentDate());
			cv.setCycle(selectByPrimaryKey.getProjectCycle());
			cv.setAdditional(selectByPrimaryKey2.getAdditional());
			cv.setBidtNum(selectByPrimaryKey2.getBidtNum());
			cv.setBillNo(selectByPrimaryKey2.getBillNo());
			cv.setCreatetime(selectByPrimaryKey2.getCreatetime());
			cv.setId(selectByPrimaryKey2.getId());
			cv.setPid(selectByPrimaryKey2.getPid());
			cv.setProfit(selectByPrimaryKey2.getProfit());
			cv.setProfitNum(selectByPrimaryKey2.getProfitNum());
			cv.setReason(selectByPrimaryKey2.getReason());
			if (newT.getTime() > startT.getTime() && newT.getTime() < endT.getTime()) {
				cv.setState(0);
			} else if (newT.getTime() > endT.getTime() && newT.getTime() < penT.getTime()) {
				cv.setState(1);
			} else {
				cv.setState(5);
			}
		} else {
			cv.setStartTime(selectByPrimaryKey.getStartTime());
			cv.setEndTime(selectByPrimaryKey.getEndTime());
			try {
				Date parse = sdf.parse(selectByPrimaryKey.getEndTime());
				  long estime=parse.getTime()+60000;
				  
					Date date=new Date(estime);
					String format = sdf.format(date);
				cv.setDateone(format);
				cv.setDatetwo(
						sdf.format(DateUtile.addDate(endT, Integer.parseInt(selectByPrimaryKey.getProjectCycle()))));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			cv.setPaymentDate(selectByPrimaryKey2.getPaymentDate());
			cv.setCycle(selectByPrimaryKey.getProjectCycle());
			cv.setAdditional(selectByPrimaryKey2.getAdditional());
			cv.setBidtNum(selectByPrimaryKey2.getBidtNum());
			cv.setBillNo(selectByPrimaryKey2.getBillNo());
			cv.setCreatetime(selectByPrimaryKey2.getCreatetime());
			cv.setId(selectByPrimaryKey2.getId());
			cv.setPid(selectByPrimaryKey2.getPid());
			cv.setProfit(selectByPrimaryKey2.getProfit());
			cv.setProfitNum(selectByPrimaryKey2.getProfitNum());
			cv.setReason(selectByPrimaryKey2.getReason());
			cv.setState(selectByPrimaryKey2.getState());
		}

		return cv;
	}
//0是可购买
	@Override
	public Object getUserPaystate(Integer userId, Integer pid) {
		// TODO Auto-generated method stub

	//查最新的订单
		List<CoinHkUserProject> selectByExample = cum.getUserPaystate(userId,pid);
		int a = 1;
		if (selectByExample != null && selectByExample.size() > 0) {
			
			if(selectByExample.get(0).getState()==4){
				a = 0;
			}
			
		}else{
			a = 0;
		}
		return a;
	}

	// 判断是否投资
	@Override
	public Object getUserPayinfostate(Integer userId) {
		// TODO Auto-generated method stub
		CoinHkUserProjectExample coinHkUserProjectExample = new CoinHkUserProjectExample();
		Criteria createCriteria = coinHkUserProjectExample.createCriteria();
		createCriteria.andUseridEqualTo(userId);
		List<CoinHkUserProject> selectByExample = cum.selectByExample(coinHkUserProjectExample);
		int a = 0;
		if (selectByExample != null && selectByExample.size() > 0) {
			a = 1;
		}
		return a;
	}

	//获取合约地址
	@Override
	public Object getContractAdress(Integer id) {
		// TODO Auto-generated method stub
		CoinHkContractExample example=new CoinHkContractExample();
		com.liyunet.domain.hk.CoinHkContractExample.Criteria createCriteria = example.createCriteria();
		createCriteria.andPidEqualTo(id);
		
		List<CoinHkContract> selectByExample = coinHkContractMapper.selectByExample(example);
		String contract =null;
		if(selectByExample!=null&&selectByExample.size()>0){
			 contract = selectByExample.get(0).getContract();
		}
		return contract;
	}
	
	public static void main(String[] args) throws ParseException {
		DateFormat df = new SimpleDateFormat("hh:mmMMM dd,yyyy",Locale.ENGLISH); 
		Date parse = df.parse("11:11Dec 07,2018");
		System.out.println(parse);
		DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String format = df2.format(parse);
		System.out.println(format);
	}

}
