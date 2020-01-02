package com.axelor.apps.tool;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.axelor.apps.tool.date.DateTool;
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

  public static void setDateMap(String fieldName, Map<String, Object> map, LocalDate date) {
    if (ObjectUtils.notEmpty(date)) {
      map.put(fieldName, DateTool.toDate(date));
    }
  }
}
