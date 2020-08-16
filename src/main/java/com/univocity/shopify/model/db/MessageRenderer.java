

package com.univocity.shopify.model.db;


import com.univocity.parsers.common.input.*;
import com.univocity.shopify.utils.*;
import org.apache.commons.lang3.*;
import org.slf4j.*;

import java.math.*;
import java.text.*;

import static com.univocity.shopify.model.db.Keys.*;
import static com.univocity.shopify.utils.Utils.*;


public class MessageRenderer {
	private static final Logger log = LoggerFactory.getLogger(MessageRenderer.class);

	public static final String NULL = "(N/A)";

	private final ParameterizedString body;
	private final ParameterizedString title;

	MessageRenderer(ParameterizedString title, ParameterizedString body) {
		this.title = title.clone();
		this.body = body.clone();
	}

	public String renderBody() {
		return removeLinesWithNull(body);
	}

	public String renderTitle() {
		return StringUtils.replace(title.applyParameterValues(), NULL, "");
	}

	public void setName(String name) {
		set(NAME, name);
	}

	public void setProductName(String productName) {
		set(PRODUCT_NAME, productName);
	}

	public void setOrderUrl(String url){
		if(url != null && url.contains("/authenticate?")){
			url = StringUtils.substringBefore(url, "/authenticate?");
		}
		set(ORDER_URL, url);
	}


	public void setStoreName(Shop shop) {
		if (shop != null) {
			String name = shop.getDomain();
			if (name == null) {
				name = shop.getShopName();
			}

			if (name.endsWith(".myshopify.com")) {
				name = name.substring(0, name.length() - ".myshopify.com".length());
			}
			set(STORE_NAME, name);
		}

	}

	public void setCheckoutUrl(String checkoutUrl) {
		set(CHECKOUT_URL, checkoutUrl);
	}

	private String removeLinesWithNull(ParameterizedString s) {
		String tmp = s.applyParameterValues();
		if (!tmp.contains(NULL)) {
			return tmp;
		}
		String[] lines = StringUtils.split(tmp, '\n');
		ElasticCharAppender out = borrowBuilder(tmp.length());
		try {
			boolean wasBlank = false;
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];
				if (StringUtils.isBlank(line) && wasBlank) {
					continue;
				}
				if (!line.contains(NULL)) {
					out.append(line);
					if (line.length() > 0 && line.charAt(line.length() - 1) == '\r') {
						out.append("\n");
					} else {
						out.append("\r\n");
					}
					wasBlank = StringUtils.isBlank(line);
				}
			}
			return out.toString();
		} finally {
			releaseBuilder(out);
		}
	}

	protected void set(String key, String value) {
		set(title, key, value);
		set(body, key, value);
	}

	protected void set(String key, java.sql.Date value) {
		set(title, key, value);
		set(body, key, value);
	}

	protected void set(ParameterizedString s, String key, String value) {
		if (StringUtils.isBlank(value)) {
			value = "";
		}
		if (s.contains(key)) {
			s.set(key, value);
		}
	}

	protected void set(ParameterizedString s, String key, java.sql.Date value) {
		if (!s.contains(key)) {
			return;
		}
		String format = s.getFormat(key);
		String stringValue = "";
		if (value != null) {
			if (StringUtils.isNotBlank(format)) {
				try {
					if (format.contains("m")) {
						format = format.replace('m', 'M');
					}
					SimpleDateFormat f = new SimpleDateFormat(format);
					stringValue = f.format(value);
				} catch (Exception e) {
					log.error("Can't format date '" + value + "' using mask '" + format + "'");
				}
			}
		}

		if (StringUtils.isBlank(stringValue)) {
			if (s == body) {
				s.set(key, null); //remove line
			} else {
				set(s, key, "N/A");
			}
		} else {
			set(s, key, stringValue);
		}
	}
}