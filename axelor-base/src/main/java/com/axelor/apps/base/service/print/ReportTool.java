package com.axelor.apps.base.service.print;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.axelor.common.ObjectUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;

public class ReportTool {
	
	public static Map<String, Object> getMap(Object model, List<String> fieldsList) {
	    if (model == null) {
	      return null;
	    }
	    final Map<String, Object> map = new HashMap<>();
	    final Mapper mapper = Mapper.of(model.getClass());
	    for (Property p : mapper.getProperties()) {
	      if (fieldsList.contains(p.getName()) && ObjectUtils.notEmpty(p.get(model))) {
	        map.put(p.getName(), p.get(model));
	      }
	    }
	    return map;
	  }
}
