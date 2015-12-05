/*
 * Copyright (c) 2013-2015 Netcrest Technologies, LLC. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netcrest.pado.temporal.test;

import com.netcrest.pado.data.jsonlite.JsonLite;
import com.netcrest.pado.temporal.test.data.Trade;

public class TradeFactory
{
	private static int BUFFER_SIZE = 100; // 100 Bytes
	private static String buffer;
	
	static {
		StringBuffer buf = new StringBuffer(BUFFER_SIZE);
		for (int i = 0; i < BUFFER_SIZE; i++) {
			buf.append("a");
		}
		buffer = buf.toString();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static JsonLite createTrade(long id)
	{
		JsonLite trade = new JsonLite(Trade.getKeyType());
		trade.put(Trade.Kbuffer, buffer);
		trade.put(Trade.Kid, id);
		trade.put(Trade.Kbuy_corp_cd, (int) id);
		trade.put(Trade.Kbuy_cpcty_cd, "buy_cpcty_cd" + id);
		trade.put(Trade.Kbuy_exct_brk_mp_id, "buy_exct_brk_mp_id" + id);
		trade.put(Trade.Kbuy_part_id, "buy_part_id" + id);
		trade.put(Trade.Kccy_cd, "ccy_cd" + id);
		trade.put(Trade.Kcntry_of_orgn_cd, "cntry_of_orgn_cd" + id);
		trade.put(Trade.Kcts_ts, "cts_ts" + id);
		trade.put(Trade.Kcusip_id, "cusip_id" + id);
		trade.put(Trade.Kisin_chk_dgt_nb, "isin_chk_dgt_nb" + id);
		trade.put(Trade.Korgnl_stlmt_dt, "orgnl_stlmt_dt" + id);
		trade.put(Trade.Kpnt_of_vld_ts, "pnt_of_vld_ts" + id);
		trade.put(Trade.Kpost_trd_appl_cd, "post_trd_appl_cd" + id);
		trade.put(Trade.Kpost_trd_appl_sq, (int) id);
		trade.put(Trade.Kpost_trd_appl_ts, "post_trd_appl_ts" + id);
		trade.put(Trade.Kprcs_dt, "prcs_dt" + id);
		trade.put(Trade.Ksec_iss_type_id, "sec_iss_type_id" + id);
		trade.put(Trade.Ksell_corp_cd, (int) id);
		trade.put(Trade.Ksell_cpcty_cd, "sell_cpcty_cd" + id);
		trade.put(Trade.Ksell_exct_brk_mp_id, "sell_exct_brk_mp_id" + id);
		trade.put(Trade.Ksell_part_id, "sell_part_id" + id);
		trade.put(Trade.Kslice_id, (int) id);
		trade.put(Trade.Ksrc1_cd, "src1_cd" + id);
		trade.put(Trade.Ksrc2_cd, "src2_cd" + id);
		trade.put(Trade.Kstlmt_dt, "stlmt_dt" + id);
		trade.put(Trade.Ktrade_am, (double)id);
		trade.put(Trade.Ktrade_grnte_ts, "trade_grnte_ts" + id);
		trade.put(Trade.Ktrade_match_cd, "trade_match_cd" + id);
		trade.put(Trade.Ktrade_ntng_cd, "trade_ntng_cd" + id);
		trade.put(Trade.Ktrade_ntng_cycl_cd, "trade_ntng_cycl_cd" + id);
		trade.put(Trade.Ktrade_odd_lot_in, "trade_odd_lot_in" + id);
		trade.put(Trade.Ktrade_orgnt_in, "trade_orgnt_in" + id);
		trade.put(Trade.Ktrade_price_am, (double) id);
		trade.put(Trade.Ktrade_qt, (double) id);
		trade.put(Trade.Ktrade_rvrsl_in, "trade_rvrsl_in" + id);
		trade.put(Trade.Ktrade_spcl_val_cd, "trade_spcl_val_cd" + id);
		trade.put(Trade.Ktrade_stl_outsd_cd, "trade_stl_outsd_cd" + id);
		trade.put(Trade.Ktrade_stlmt_day_cn, (int) id);
		trade.put(Trade.Ktrade_type_cd, "trade_type_cd" + id);
		trade.put(Trade.Ktrd_dt, "trd_dt" + id);
		trade.put(Trade.Ktrd_stat_cd, "trd_stat_cd" + id);
		trade.put(Trade.Kwi_in, "wi_in" + id);
		return trade;
	}

}
