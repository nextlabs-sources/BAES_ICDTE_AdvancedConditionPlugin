package com.nextlabs.ac;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.expressions.IMultivalue;
import com.bluejungle.framework.expressions.ValueType;

public class Utils {
	
	private static final Log LOG = LogFactory.getLog(Utils.class.getName());

	private static String OS = null;

	public static String getOsName() {
		if (OS == null) {
			OS = System.getProperty("os.name");
		}
		return OS;
	}

	public static boolean isWindows() {
		return getOsName().startsWith("Windows");
	}
	
	/**
	 * Process the input arguments and return an array of arrays of strings
	 * 
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<ArrayList<String>> processValues(IEvalValue[] args) throws Exception {
		LOG.info("processValues() entered ");
		ArrayList<ArrayList<String>> sOutData = new ArrayList<ArrayList<String>>();

		for (IEvalValue ieValue : args) {
			LOG.info("ieValue " + ieValue.toString());
			if (ieValue != null) {
				if (ieValue.getType() == ValueType.MULTIVAL) {
					ArrayList<String> list = new ArrayList<String>();
					IMultivalue value = (IMultivalue) ieValue.getValue();
					Iterator<IEvalValue> ievIter = value.iterator();

					while (ievIter.hasNext()) {
						IEvalValue iev = ievIter.next();

						if (iev != null) {
							if (!iev.getValue().toString().isEmpty()) {
								list.add(iev.getValue().toString());
								LOG.debug("processValues() Processed value:" + iev.getValue().toString());
							}
						}
					}
					sOutData.add(list);
				} else if (!ieValue.getValue().toString().isEmpty()) {
					ArrayList<String> list = new ArrayList<String>();
					list.add(ieValue.getValue().toString());
					sOutData.add(list);
				}
			}

		}
		LOG.info("processValues() Input Data: " + sOutData);
		return sOutData;
	}
	
	public static String findInstallFolder() {

		String path = Utils.class.getProtectionDomain().getCodeSource().getLocation().getPath();

		try {
			path = URLDecoder.decode(path, "UTF-8");

		} catch (Exception e) {
			LOG.error(String.format("Exception while decoding the path: %s", path), e);
		}

		int endIndex = path.indexOf("jservice/jar");

		if (isWindows()) {
			path = path.substring(1, endIndex);
		} else {
			path = path.substring(0, endIndex);
		}
		return path;
	}
}
