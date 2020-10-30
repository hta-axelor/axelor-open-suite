/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2020 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.tool;

import com.axelor.apps.tool.date.DateTool;
import com.axelor.common.ObjectUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
