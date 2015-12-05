package com.netcrest.pado.temporal.test.data.domain;

import com.netcrest.pado.data.jsonlite.IJsonLiteWrapper;
import com.netcrest.pado.data.jsonlite.JsonLite;

public class Trade implements IJsonLiteWrapper<Object> {

	private transient JsonLite<Object> jl;

	public Trade() {
		this.jl = new JsonLite<Object>(
				com.netcrest.pado.temporal.test.data.Trade.getKeyType());
	}

	public Trade(JsonLite<Object> jl) {
		this.jl = jl;
	}

	public void setid(long id) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Kid, id);
	}

	public long getid() {
		return (Long) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Kid);
	}

	public void setpost_trd_appl_cd(String post_trd_appl_cd) {
		this.jl.put(
				com.netcrest.pado.temporal.test.data.Trade.Kpost_trd_appl_cd,
				post_trd_appl_cd);
	}

	public String getpost_trd_appl_cd() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Kpost_trd_appl_cd);
	}

	public void setpost_trd_appl_sq(int post_trd_appl_sq) {
		this.jl.put(
				com.netcrest.pado.temporal.test.data.Trade.Kpost_trd_appl_sq,
				post_trd_appl_sq);
	}

	public int getpost_trd_appl_sq() {
		return (Integer) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Kpost_trd_appl_sq);
	}

	public void setpost_trd_appl_ts(String post_trd_appl_ts) {
		this.jl.put(
				com.netcrest.pado.temporal.test.data.Trade.Kpost_trd_appl_ts,
				post_trd_appl_ts);
	}

	public String getpost_trd_appl_ts() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Kpost_trd_appl_ts);
	}

	public void setcusip_id(String cusip_id) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Kcusip_id,
				cusip_id);
	}

	public String getcusip_id() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Kcusip_id);
	}

	public void setwi_in(String wi_in) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Kwi_in, wi_in);
	}

	public String getwi_in() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Kwi_in);
	}

	public void setcntry_of_orgn_cd(String cntry_of_orgn_cd) {
		this.jl.put(
				com.netcrest.pado.temporal.test.data.Trade.Kcntry_of_orgn_cd,
				cntry_of_orgn_cd);
	}

	public String getcntry_of_orgn_cd() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Kcntry_of_orgn_cd);
	}

	public void setisin_chk_dgt_nb(String isin_chk_dgt_nb) {
		this.jl.put(
				com.netcrest.pado.temporal.test.data.Trade.Kisin_chk_dgt_nb,
				isin_chk_dgt_nb);
	}

	public String getisin_chk_dgt_nb() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Kisin_chk_dgt_nb);
	}

	public void setsrc1_cd(String src1_cd) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Ksrc1_cd,
				src1_cd);
	}

	public String getsrc1_cd() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Ksrc1_cd);
	}

	public void setsrc2_cd(String src2_cd) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Ksrc2_cd,
				src2_cd);
	}

	public String getsrc2_cd() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Ksrc2_cd);
	}

	public void setccy_cd(String ccy_cd) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Kccy_cd, ccy_cd);
	}

	public String getccy_cd() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Kccy_cd);
	}

	public void setstlmt_dt(String stlmt_dt) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Kstlmt_dt,
				stlmt_dt);
	}

	public String getstlmt_dt() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Kstlmt_dt);
	}

	public void setprcs_dt(String prcs_dt) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Kprcs_dt,
				prcs_dt);
	}

	public String getprcs_dt() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Kprcs_dt);
	}

	public void settrade_qt(double trade_qt) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Ktrade_qt,
				trade_qt);
	}

	public double gettrade_qt() {
		return (Double) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Ktrade_qt);
	}

	public void settrade_am(double trade_am) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Ktrade_am,
				trade_am);
	}

	public double gettrade_am() {
		return (Double) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Ktrade_am);
	}

	public void settrade_price_am(double trade_price_am) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Ktrade_price_am,
				trade_price_am);
	}

	public double gettrade_price_am() {
		return (Double) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Ktrade_price_am);
	}

	public void settrd_stat_cd(String trd_stat_cd) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Ktrd_stat_cd,
				trd_stat_cd);
	}

	public String gettrd_stat_cd() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Ktrd_stat_cd);
	}

	public void setcts_ts(String cts_ts) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Kcts_ts, cts_ts);
	}

	public String getcts_ts() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Kcts_ts);
	}

	public void setpnt_of_vld_ts(String pnt_of_vld_ts) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Kpnt_of_vld_ts,
				pnt_of_vld_ts);
	}

	public String getpnt_of_vld_ts() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Kpnt_of_vld_ts);
	}

	public void setbuy_corp_cd(int buy_corp_cd) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Kbuy_corp_cd,
				buy_corp_cd);
	}

	public int getbuy_corp_cd() {
		return (Integer) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Kbuy_corp_cd);
	}

	public void setbuy_part_id(String buy_part_id) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Kbuy_part_id,
				buy_part_id);
	}

	public String getbuy_part_id() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Kbuy_part_id);
	}

	public void setbuy_exct_brk_mp_id(String buy_exct_brk_mp_id) {
		this.jl.put(
				com.netcrest.pado.temporal.test.data.Trade.Kbuy_exct_brk_mp_id,
				buy_exct_brk_mp_id);
	}

	public String getbuy_exct_brk_mp_id() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Kbuy_exct_brk_mp_id);
	}

	public void setsell_corp_cd(int sell_corp_cd) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Ksell_corp_cd,
				sell_corp_cd);
	}

	public int getsell_corp_cd() {
		return (Integer) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Ksell_corp_cd);
	}

	public void setsell_part_id(String sell_part_id) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Ksell_part_id,
				sell_part_id);
	}

	public String getsell_part_id() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Ksell_part_id);
	}

	public void setsell_exct_brk_mp_id(String sell_exct_brk_mp_id) {
		this.jl.put(
				com.netcrest.pado.temporal.test.data.Trade.Ksell_exct_brk_mp_id,
				sell_exct_brk_mp_id);
	}

	public String getsell_exct_brk_mp_id() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Ksell_exct_brk_mp_id);
	}

	public void settrd_dt(String trd_dt) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Ktrd_dt, trd_dt);
	}

	public String gettrd_dt() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Ktrd_dt);
	}

	public void setorgnl_stlmt_dt(String orgnl_stlmt_dt) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Korgnl_stlmt_dt,
				orgnl_stlmt_dt);
	}

	public String getorgnl_stlmt_dt() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Korgnl_stlmt_dt);
	}

	public void setslice_id(int slice_id) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Kslice_id,
				slice_id);
	}

	public int getslice_id() {
		return (Integer) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Kslice_id);
	}

	public void setbuy_cpcty_cd(String buy_cpcty_cd) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Kbuy_cpcty_cd,
				buy_cpcty_cd);
	}

	public String getbuy_cpcty_cd() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Kbuy_cpcty_cd);
	}

	public void setsell_cpcty_cd(String sell_cpcty_cd) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Ksell_cpcty_cd,
				sell_cpcty_cd);
	}

	public String getsell_cpcty_cd() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Ksell_cpcty_cd);
	}

	public void settrade_orgnt_in(String trade_orgnt_in) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Ktrade_orgnt_in,
				trade_orgnt_in);
	}

	public String gettrade_orgnt_in() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Ktrade_orgnt_in);
	}

	public void settrade_match_cd(String trade_match_cd) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Ktrade_match_cd,
				trade_match_cd);
	}

	public String gettrade_match_cd() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Ktrade_match_cd);
	}

	public void settrade_type_cd(String trade_type_cd) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Ktrade_type_cd,
				trade_type_cd);
	}

	public String gettrade_type_cd() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Ktrade_type_cd);
	}

	public void settrade_spcl_val_cd(String trade_spcl_val_cd) {
		this.jl.put(
				com.netcrest.pado.temporal.test.data.Trade.Ktrade_spcl_val_cd,
				trade_spcl_val_cd);
	}

	public String gettrade_spcl_val_cd() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Ktrade_spcl_val_cd);
	}

	public void settrade_stl_outsd_cd(String trade_stl_outsd_cd) {
		this.jl.put(
				com.netcrest.pado.temporal.test.data.Trade.Ktrade_stl_outsd_cd,
				trade_stl_outsd_cd);
	}

	public String gettrade_stl_outsd_cd() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Ktrade_stl_outsd_cd);
	}

	public void settrade_odd_lot_in(String trade_odd_lot_in) {
		this.jl.put(
				com.netcrest.pado.temporal.test.data.Trade.Ktrade_odd_lot_in,
				trade_odd_lot_in);
	}

	public String gettrade_odd_lot_in() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Ktrade_odd_lot_in);
	}

	public void settrade_ntng_cd(String trade_ntng_cd) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Ktrade_ntng_cd,
				trade_ntng_cd);
	}

	public String gettrade_ntng_cd() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Ktrade_ntng_cd);
	}

	public void settrade_rvrsl_in(String trade_rvrsl_in) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Ktrade_rvrsl_in,
				trade_rvrsl_in);
	}

	public String gettrade_rvrsl_in() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Ktrade_rvrsl_in);
	}

	public void settrade_stlmt_day_cn(int trade_stlmt_day_cn) {
		this.jl.put(
				com.netcrest.pado.temporal.test.data.Trade.Ktrade_stlmt_day_cn,
				trade_stlmt_day_cn);
	}

	public int gettrade_stlmt_day_cn() {
		return (Integer) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Ktrade_stlmt_day_cn);
	}

	public void settrade_ntng_cycl_cd(String trade_ntng_cycl_cd) {
		this.jl.put(
				com.netcrest.pado.temporal.test.data.Trade.Ktrade_ntng_cycl_cd,
				trade_ntng_cycl_cd);
	}

	public String gettrade_ntng_cycl_cd() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Ktrade_ntng_cycl_cd);
	}

	public void setsec_iss_type_id(String sec_iss_type_id) {
		this.jl.put(
				com.netcrest.pado.temporal.test.data.Trade.Ksec_iss_type_id,
				sec_iss_type_id);
	}

	public String getsec_iss_type_id() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Ksec_iss_type_id);
	}

	public void settrade_grnte_ts(String trade_grnte_ts) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Ktrade_grnte_ts,
				trade_grnte_ts);
	}

	public String gettrade_grnte_ts() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Ktrade_grnte_ts);
	}

	public void setbuffer(String buffer) {
		this.jl.put(com.netcrest.pado.temporal.test.data.Trade.Kbuffer, buffer);
	}

	public String getbuffer() {
		return (String) this.jl
				.get(com.netcrest.pado.temporal.test.data.Trade.Kbuffer);
	}

	public JsonLite<Object> toJsonLite() {
		return this.jl;
	}

	public void fromJsonLite(JsonLite<Object> jl) {
		this.jl = jl;
	}
}