package com.netcrest.pado.temporal.test.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.KeyTypeManager;

/**
 * @pado 1:-1:0 1558917325260212734:-8710321381148735435
 * @version 1 dpark:08/02/14.07.34.46.EDT
 * @padocodegen Updated Sat Aug 02 07:34:46 EDT 2014
 */
public enum Trade implements KeyType {
	/**
	 * id: <b>Long</b>
	 */
	Kid("id", Long.class, false, true, "", 0),
	/**
	 * post_trd_appl_cd: <b>String</b>
	 */
	Kpost_trd_appl_cd("post_trd_appl_cd", String.class, false, true, "", 0),
	/**
	 * post_trd_appl_sq: <b>Integer</b>
	 */
	Kpost_trd_appl_sq("post_trd_appl_sq", Integer.class, false, true, "", 0),
	/**
	 * post_trd_appl_ts: <b>String</b>
	 */
	Kpost_trd_appl_ts("post_trd_appl_ts", String.class, false, true, "", 0),
	/**
	 * cusip_id: <b>String</b>
	 */
	Kcusip_id("cusip_id", String.class, false, true, "", 0),
	/**
	 * wi_in: <b>String</b>
	 */
	Kwi_in("wi_in", String.class, false, true, "", 0),
	/**
	 * cntry_of_orgn_cd: <b>String</b>
	 */
	Kcntry_of_orgn_cd("cntry_of_orgn_cd", String.class, false, true, "", 0),
	/**
	 * isin_chk_dgt_nb: <b>String</b>
	 */
	Kisin_chk_dgt_nb("isin_chk_dgt_nb", String.class, false, true, "", 0),
	/**
	 * src1_cd: <b>String</b>
	 */
	Ksrc1_cd("src1_cd", String.class, false, true, "", 0),
	/**
	 * src2_cd: <b>String</b>
	 */
	Ksrc2_cd("src2_cd", String.class, false, true, "", 0),
	/**
	 * ccy_cd: <b>String</b>
	 */
	Kccy_cd("ccy_cd", String.class, false, true, "", 0),
	/**
	 * stlmt_dt: <b>String</b>
	 */
	Kstlmt_dt("stlmt_dt", String.class, false, true, "", 0),
	/**
	 * prcs_dt: <b>String</b>
	 */
	Kprcs_dt("prcs_dt", String.class, false, true, "", 0),
	/**
	 * trade_qt: <b>Double</b>
	 */
	Ktrade_qt("trade_qt", Double.class, false, true, "", 0),
	/**
	 * trade_am: <b>Double</b>
	 */
	Ktrade_am("trade_am", Double.class, false, true, "", 0),
	/**
	 * trade_price_am: <b>Double</b>
	 */
	Ktrade_price_am("trade_price_am", Double.class, false, true, "", 0),
	/**
	 * trd_stat_cd: <b>String</b>
	 */
	Ktrd_stat_cd("trd_stat_cd", String.class, false, true, "", 0),
	/**
	 * cts_ts: <b>String</b>
	 */
	Kcts_ts("cts_ts", String.class, false, true, "", 0),
	/**
	 * pnt_of_vld_ts: <b>String</b>
	 */
	Kpnt_of_vld_ts("pnt_of_vld_ts", String.class, false, true, "", 0),
	/**
	 * buy_corp_cd: <b>Integer</b>
	 */
	Kbuy_corp_cd("buy_corp_cd", Integer.class, false, true, "", 0),
	/**
	 * buy_part_id: <b>String</b>
	 */
	Kbuy_part_id("buy_part_id", String.class, false, true, "", 0),
	/**
	 * buy_exct_brk_mp_id: <b>String</b>
	 */
	Kbuy_exct_brk_mp_id("buy_exct_brk_mp_id", String.class, false, true, "", 0),
	/**
	 * sell_corp_cd: <b>Integer</b>
	 */
	Ksell_corp_cd("sell_corp_cd", Integer.class, false, true, "", 0),
	/**
	 * sell_part_id: <b>String</b>
	 */
	Ksell_part_id("sell_part_id", String.class, false, true, "", 0),
	/**
	 * sell_exct_brk_mp_id: <b>String</b>
	 */
	Ksell_exct_brk_mp_id("sell_exct_brk_mp_id", String.class, false, true, "",
			0),
	/**
	 * trd_dt: <b>String</b>
	 */
	Ktrd_dt("trd_dt", String.class, false, true, "", 0),
	/**
	 * orgnl_stlmt_dt: <b>String</b>
	 */
	Korgnl_stlmt_dt("orgnl_stlmt_dt", String.class, false, true, "", 0),
	/**
	 * slice_id: <b>Integer</b>
	 */
	Kslice_id("slice_id", Integer.class, false, true, "", 0),
	/**
	 * buy_cpcty_cd: <b>String</b>
	 */
	Kbuy_cpcty_cd("buy_cpcty_cd", String.class, false, true, "", 0),
	/**
	 * sell_cpcty_cd: <b>String</b>
	 */
	Ksell_cpcty_cd("sell_cpcty_cd", String.class, false, true, "", 0),
	/**
	 * trade_orgnt_in: <b>String</b>
	 */
	Ktrade_orgnt_in("trade_orgnt_in", String.class, false, true, "", 0),
	/**
	 * trade_match_cd: <b>String</b>
	 */
	Ktrade_match_cd("trade_match_cd", String.class, false, true, "", 0),
	/**
	 * trade_type_cd: <b>String</b>
	 */
	Ktrade_type_cd("trade_type_cd", String.class, false, true, "", 0),
	/**
	 * trade_spcl_val_cd: <b>String</b>
	 */
	Ktrade_spcl_val_cd("trade_spcl_val_cd", String.class, false, true, "", 0),
	/**
	 * trade_stl_outsd_cd: <b>String</b>
	 */
	Ktrade_stl_outsd_cd("trade_stl_outsd_cd", String.class, false, true, "", 0),
	/**
	 * trade_odd_lot_in: <b>String</b>
	 */
	Ktrade_odd_lot_in("trade_odd_lot_in", String.class, false, true, "", 0),
	/**
	 * trade_ntng_cd: <b>String</b>
	 */
	Ktrade_ntng_cd("trade_ntng_cd", String.class, false, true, "", 0),
	/**
	 * trade_rvrsl_in: <b>String</b>
	 */
	Ktrade_rvrsl_in("trade_rvrsl_in", String.class, false, true, "", 0),
	/**
	 * trade_stlmt_day_cn: <b>Integer</b>
	 */
	Ktrade_stlmt_day_cn("trade_stlmt_day_cn", Integer.class, false, true, "", 0),
	/**
	 * trade_ntng_cycl_cd: <b>String</b>
	 */
	Ktrade_ntng_cycl_cd("trade_ntng_cycl_cd", String.class, false, true, "", 0),
	/**
	 * sec_iss_type_id: <b>String</b>
	 */
	Ksec_iss_type_id("sec_iss_type_id", String.class, false, true, "", 0),
	/**
	 * trade_grnte_ts: <b>String</b>
	 */
	Ktrade_grnte_ts("trade_grnte_ts", String.class, false, true, "", 0),
	/**
	 * buffer: <b>String</b>
	 */
	Kbuffer("buffer", String.class, false, true, "", 0);

	private static final Object ID = new UUID(1558917325260212734L,
			-8710321381148735435L);
	private static final int VERSION = 1;
	private static int keyIndex;
	private static boolean payloadKeepSerialized;
	private static Class<?> domainClass;
	private static KeyType references[] = new KeyType[] {};

	static {
		try {
			domainClass = Class
					.forName("com.netcrest.pado.temporal.test.data.domain.Trade");
		} catch (ClassNotFoundException ex) {
			domainClass = null;
		}
	}

	private static int getNextIndex() {
		keyIndex++;
		return keyIndex - 1;
	}

	private Trade(String name, Class type, boolean isDeprecated,
			boolean keyKeepSerialized, String query, int depth) {
		this.index = getNextIndex();
		this.name = name;
		this.type = type;
		this.isDeprecated = isDeprecated;
		this.keyKeepSerialized = keyKeepSerialized;
		this.query = query;
		this.depth = depth;
	}

	private int index;
	private String name;
	private Class type;
	private boolean isDeprecated;
	private boolean keyKeepSerialized;
	private String query;
	private int depth;

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public Class getType() {
		return type;
	}

	private static final Map<String, KeyType> keyNameMap;
	private static final int[] deprecatedIndexes;

	static {
		KeyType values[] = values();
		HashMap<String, KeyType> map = new HashMap<String, KeyType>(
				values.length + 1, 1f);
		List<Integer> list = new ArrayList(values.length);
		for (int i = 0; i < values.length; i++) {
			map.put(values[i].getName(), values[i]);
			if (values[i].isKeyKeepSerialized()) {
				payloadKeepSerialized = true;
			}
			if (values[i].isDeprecated()) {
				list.add(i);
			}
		}
		keyNameMap = Collections.unmodifiableMap(map);
		deprecatedIndexes = new int[list.size()];
		for (int i = 0; i < deprecatedIndexes.length; i++) {
			deprecatedIndexes[i] = list.get(i);
		}
	}

	public Object getId() {
		return ID;
	}

	public int getMergePoint() {
		return +-1;
	}

	public int getVersion() {
		return VERSION;
	}

	public int getKeyCount() {
		return values().length;
	}

	public KeyType[] getValues(int version) {
		return KeyTypeManager.getValues(this, version);
	}

	public static KeyType getKeyType() {
		return values()[0];
	}

	public KeyType getKeyType(String name) {
		return keyNameMap.get(name);
	}

	public KeyType[] getValues() {
		return values();
	}

	public boolean isDeltaEnabled() {
		return false;
	}

	public boolean isDeprecated() {
		return isDeprecated;
	}

	public int[] getDeprecatedIndexes() {
		return deprecatedIndexes;
	}

	public boolean isKeyKeepSerialized() {
		return keyKeepSerialized;
	}

	public boolean isCompressionEnabled() {
		return false;
	}

	public boolean isPayloadKeepSerialized() {
		return payloadKeepSerialized;
	}

	public Set<String> getNameSet() {
		return keyNameMap.keySet();
	}

	public boolean containsKey(String name) {
		return keyNameMap.containsKey(name);
	}

	public Class<?> getDomainClass() {
		return domainClass;
	}

	public KeyType[] getReferences() {
		return references;
	}

	public void setReferences(KeyType[] ref) {
		if (ref == null) {
			references = new KeyType[0];
		} else {
			references = ref;
		}
	}

	public boolean isReference() {
		return query != null && query.length() > 0;
	}

	public String getQuery() {
		String query = KeyTypeManager.getQuery(this);
		if (query == null) {
			return this.query;
		} 
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public int getDepth() {
		int depth = KeyTypeManager.getDepth(this);
		if (depth == -1) {
			return this.depth;
		}
		return depth;
	}


	public void setDepth(int depth) {
		this.depth = depth;
	}
}