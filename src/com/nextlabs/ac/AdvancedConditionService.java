package com.nextlabs.ac;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluejungle.framework.expressions.EvalValue;
import com.bluejungle.framework.expressions.IEvalValue;
import com.bluejungle.framework.expressions.Multivalue;
import com.bluejungle.pf.domain.destiny.serviceprovider.IFunctionServiceProvider;
import com.bluejungle.pf.domain.destiny.serviceprovider.ServiceProviderException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;

public class AdvancedConditionService implements IFunctionServiceProvider {

	private static final Log LOG = LogFactory.getLog(AdvancedConditionService.class.getName());

	@Override
	public void init() throws Exception {

		LOG.debug("init() started");
		LOG.debug("init() finished");

	}

	@Override
	public IEvalValue callFunction(String arg0, IEvalValue[] arg1) throws ServiceProviderException {

		IEvalValue result = getNullReturn(arg0);

		if (arg0 == null) {
			LOG.error("Invalid function called");
			return result;
		}

		LOG.info("Function call is" + arg0);

		ArrayList<ArrayList<String>> params = null;

		// process parameter list
		try {
			params = Utils.processValues(arg1);
		} catch (Exception e) {
			LOG.error("callFunction() Unable to process the parameter list", e);
			return result;
		}

		switch (arg0) {
		case "getAttributeInJSON":
			result = getAttributeFromJSOn(params);
			break;
		default:
			LOG.error("Operation " + arg0 + "not supported");
		}

		LOG.info("call_function completed. Value returned is  " + result.getValue());

		return result;
	}
	
	private IEvalValue getAttributeFromJSOn (ArrayList<ArrayList<String>> params){
		
		IEvalValue nullReturn = getNullReturn("getAttributeInJSON");

		if (params.size() == 0) {
			LOG.error("No argument received");
			return nullReturn;
		}

		if (params.get(0) == null || params.get(0).get(0) == null) {
			LOG.error("JSON string is null");
			return nullReturn;
		}

		String jsonString = params.get(0).get(0);

		LOG.debug("JSON String received is: " + jsonString);

		if (params.size() == 1) {
			LOG.error("Not enough argument received. Expect at least 2 arguments");
			return nullReturn;
		}

		if (params.get(params.size() - 1) == null || params.get(params.size() - 1).get(0) == null) {
			LOG.error("Attribute name is null");
			return nullReturn;
		}

		String path = "";

		for (int i = 1; i < params.size(); i++) {
			if (params.get(i) == null || params.get(i).get(0) == null) {
				LOG.error("Path at level " + i + " is null");
				return nullReturn;
			}

			path = path + params.get(i).get(0) + "|";
		}
		
		//remove last |
		path = path.substring(0, path.length()-1);

		LOG.info("Attribute to get is:" + path);
		
		boolean bArrayRequested = false;
		
		if (path.contains("[") && path.contains("]")) {
			
			bArrayRequested = true;
		}
		
		Map<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
	    try {
	      addKeys("", new ObjectMapper().readTree(jsonString), map, bArrayRequested);
	    } catch (IOException e) {
	    	LOG.error(e);
	    }   
//	    for (String keys : map.keySet())
//	    {
//	       System.out.println(keys);
//	    }
		ArrayList<String> result = new ArrayList<String>();
		result = map.get(path);
		               
       if (result==null || result.size() < 1) {
    	   
    	   LOG.error(path + " doesn't exist");
			return nullReturn;
       }
       else if (result.size() == 1) {
    	   return EvalValue.build(result.get(0));
       }
       else {
    	   return EvalValue.build(Multivalue.create(result));
       }
       
	}
	
	/*
	 * Add everything to hashmap so for easy searching.
	 */
	private void addKeys(String currentPath, JsonNode jsonNode, Map<String, ArrayList<String>> map, boolean bArrayRequested) {
		
	    if (jsonNode.isObject()) {
	      ObjectNode objectNode = (ObjectNode) jsonNode;
	      Iterator<Map.Entry<String, JsonNode>> iter = objectNode.fields();
	      String pathPrefix = currentPath.isEmpty() ? "" : currentPath + "|";

	      while (iter.hasNext()) {
	        Map.Entry<String, JsonNode> entry = iter.next();
	        addKeys(pathPrefix + entry.getKey(), entry.getValue(), map, bArrayRequested);
	      }
	    } else if (jsonNode.isArray()) {
	      ArrayNode arrayNode = (ArrayNode) jsonNode;
	      for (int i = 0; i < arrayNode.size(); i++) {
	    	  if (bArrayRequested)
	    		  addKeys(currentPath + "[" + i + "]", arrayNode.get(i), map, bArrayRequested);
	    	  else {
	    		  addKeys(currentPath, arrayNode.get(i), map, bArrayRequested);
	    	  }
	       }
	    } else if (jsonNode.isValueNode()) {
	      ValueNode valueNode = (ValueNode) jsonNode;
	      addValues(currentPath, valueNode.asText(), map);
	    }
	  }
	
	/*
	 * For non unique hashmap handling
	 */
	private void addValues(String key, String value, Map<String, ArrayList<String>> hashMap) {
		
		ArrayList<String> tempList = null;

		if (hashMap.containsKey(key)) {
			tempList = hashMap.get(key);
			if (tempList == null)
				tempList = new ArrayList<String>();
			tempList.add(value);
		} else {
			tempList = new ArrayList<String>();
			tempList.add(value);
		}
		hashMap.put(key, tempList);
	}

	private IEvalValue getNullReturn(String function) {
		IEvalValue result = EvalValue.NULL;
		switch (function) {
		case "getAttributeInJSON":
			result = EvalValue.NULL;
			break;
		default:
			LOG.warn("getNullReturn() Operation not support");
			result = EvalValue.NULL;
			break;
		}

		return result;
	}

	public static void main(String[] args) {
		AdvancedConditionService engine = new AdvancedConditionService();

		List<IEvalValue> paramList = new ArrayList<IEvalValue>();
		paramList.add(EvalValue.build(
				"{\"DisseminationControls\":{\"noforn\":[\"usa\",\"bra\"],\"thirdlevel\":{\"myattribute\":\"myvalue\"}},\"myattribute\":\"myvalue\"}"));
		paramList.add(EvalValue.build("DisseminationControls"));
		paramList.add(EvalValue.build("thirdlevel"));
		paramList.add(EvalValue.build("myattribute"));
		
		try {
			ArrayList<ArrayList<String>> inputs = Utils.processValues(paramList.toArray(new IEvalValue[0]));

			LOG.info(engine.getAttributeFromJSOn(inputs).getValue());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		paramList.clear();
		
		paramList.add(EvalValue.build(
						"{\"@version\":\"3\",\"HandlingAssertion\":[{\"@scope\":\"TDO\",\"@id\":\"HA-88e56248-d369-4efb-b489-4cbf5f487dcb\",\"HandlingStatement\":{\"Edh\":{\"@DESVersion\":[\"4\",\"13.201701\",\"1\"],\"@ISMCATCESVersion\":\"2\",\"@CESVersion\":\"1\",\"Identifier\":\"guide://999004/--JJJ--\",\"DataItemCreateDateTime\":\"--EEE--\",\"ResponsibleEntity\":{\"@classification\":\"C\",\"@ownerProducer\":\"--CCC--\",\"@disseminationControls\":\"REL\",\"@releasableTo\":\"USA FVEY\",\"Country\":\"--GGG--\",\"Organization\":\"--FFF--\"},\"Security\":{\"@classification\":\"C\",\"@ownerProducer\":\"--CCC--\",\"@disseminationControls\":\"REL\",\"@releasableTo\":\"USA FVEY\",\"@DESVersion\":[\"13.201701\",\"10\",\"3\"],\"@resourceElement\":\"true\",\"@compliesWith\":\"USGov\",\"@createDate\":\"--AAA--\",\"@exemptFrom\":\"DOD_DISTRO_STATEMENT\",\"@derivativelyClassifiedBy\":\"1.4(c)\",\"@derivedFrom\":\"USCENTCOM SCG CCR 380-14 14 May 2019\",\"@declassDate\":\"2044-08-21\"}}}},{\"@scope\":\"PAYL\",\"@id\":\"HA-980ac420-e2d9-4e21-af0b-d001b2309393\",\"HandlingStatement\":{\"Edh\":{\"@DESVersion\":[\"4\",\"13.201701\",\"1\"],\"@ISMCATCESVersion\":\"2\",\"@CESVersion\":\"1\",\"Identifier\":\"guide://999004/--HHH--\",\"DataItemCreateDateTime\":\"--EEE--\",\"ResponsibleEntity\":{\"@classification\":\"C\",\"@ownerProducer\":\"--CCC--\",\"@joint\":\"true\",\"@disseminationControls\":\"REL\",\"@releasableTo\":\"USA FVEY\",\"Country\":\"--GGG--\",\"Organization\":\"--FFF--\"},\"Security\":{\"@classification\":\"C\",\"@ownerProducer\":\"--CCC--\",\"@disseminationControls\":\"REL\",\"@releasableTo\":\"USA FVEY\",\"@DESVersion\":[\"13.201701\",\"10\",\"3\"],\"@resourceElement\":\"false\",\"@compliesWith\":\"USGov\",\"@createDate\":\"--AAA--\",\"@exemptFrom\":\"DOD_DISTRO_STATEMENT\",\"@derivativelyClassifiedBy\":\"1.4(c)\",\"@derivedFrom\":\"USCENTCOM SCG CCR 380-14 14 May 2019\",\"@declassDate\":\"2044-08-21\"}}}}]}"));
		paramList.add(EvalValue.build("HandlingAssertion[0]"));
		paramList.add(EvalValue.build("@scope"));
		
		try {
			ArrayList<ArrayList<String>> inputs = Utils.processValues(paramList.toArray(new IEvalValue[0]));

			LOG.info(engine.getAttributeFromJSOn(inputs).getValue());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
