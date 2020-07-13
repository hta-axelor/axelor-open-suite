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
package com.axelor.admin.auth;

import com.axelor.apps.admin.db.GlobalTrackingLog;
import com.axelor.apps.admin.db.GlobalTrackingLogLine;
import com.axelor.apps.admin.db.repo.GlobalTrackingLogRepository;
import com.axelor.apps.project.db.Topic;
import com.axelor.apps.project.db.Wiki;
import com.axelor.auth.AuditInterceptor;
import com.axelor.auth.db.AuditableModel;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.team.db.TeamTask;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.type.Type;

@SuppressWarnings("serial")
public class ProjectInterceptor extends AuditInterceptor {

  private static final String UPDATED_BY = "updatedBy";
  private static final String UPDATED_ON = "updatedOn";
  private static final String CREATED_BY = "createdBy";
  private static final String CREATED_ON = "createdOn";

  private final ThreadLocal<ProjectAuditTracker> globalTracker = new ThreadLocal<>();

  @SuppressWarnings("rawtypes")
  protected static final Class[] PROJECT_ACTIVITY_CLASSES = {
    TeamTask.class, Wiki.class, Topic.class,
  };

  @SuppressWarnings("unchecked")
  @Override
  public boolean onSave(
      Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {

    if (!super.onSave(entity, id, state, propertyNames, types)
        || !Arrays.asList(PROJECT_ACTIVITY_CLASSES).contains(entity.getClass())) {
      return false;
    }

    initGlobalTracker();
    GlobalTrackingLog log =
        globalTracker
            .get()
            .addLog((AuditableModel) entity, GlobalTrackingLogRepository.TYPE_CREATE);

    for (int i = 0; i < propertyNames.length; i++) {
      if (state[i] == null
          || CREATED_ON.equals(propertyNames[i])
          || CREATED_BY.equals(propertyNames[i])) {
        continue;
      }
      Mapper mapper = Mapper.of(entity.getClass());
      Property property = mapper.getProperty(propertyNames[i]);

      GlobalTrackingLogLine logLine = new GlobalTrackingLogLine();
      logLine.setMetaFieldName(propertyNames[i]);

      if (state[i] instanceof AuditableModel) {
        logLine.setNewValue(format(property, state[i]));
      } else if (state[i] instanceof Collection) {

        String newVal = "";
        if (CollectionUtils.isNotEmpty((Collection<Object>) state[i])) {
          newVal =
              String.format(
                  "[%s]",
                  ((Collection<AuditableModel>) state[i])
                      .stream()
                          .map(AuditableModel::getId)
                          .map(String::valueOf)
                          .collect(Collectors.joining(", ")));
        }
        logLine.setNewValue(newVal);
      } else {
        logLine.setNewValue(format(property, state[i]));
      }

      log.addGlobalTrackingLogLineListItem(logLine);
    }
    if (log != null) {
      globalTracker.get().addLog(log);
    }

    return true;
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean onFlushDirty(
      Object entity,
      Serializable id,
      Object[] currentState,
      Object[] previousState,
      String[] propertyNames,
      Type[] types) {

    if (!super.onFlushDirty(entity, id, currentState, previousState, propertyNames, types)
        || !Arrays.asList(PROJECT_ACTIVITY_CLASSES).contains(entity.getClass())) {
      return false;
    }

    initGlobalTracker();
    GlobalTrackingLog log =
        globalTracker
            .get()
            .addLog((AuditableModel) entity, GlobalTrackingLogRepository.TYPE_UPDATE);

    for (int i = 0; i < propertyNames.length; i++) {

      if (Objects.equals(currentState[i], previousState[i])
          || UPDATED_ON.equals(propertyNames[i])
          || UPDATED_BY.equals(propertyNames[i])) {
        continue;
      }
      Mapper mapper = Mapper.of(entity.getClass());
      Property property = mapper.getProperty(propertyNames[i]);

      GlobalTrackingLogLine logLine = new GlobalTrackingLogLine();
      logLine.setMetaFieldName(propertyNames[i]);

      if (currentState[i] instanceof AuditableModel) {

        logLine.setNewValue(format(property, currentState[i]));
        logLine.setPreviousValue(format(property, previousState[i]));

      } else if (currentState[i] instanceof Collection) {

        String prevVal = "";
        String newVal = "";
        if (CollectionUtils.isNotEmpty((Collection<Object>) previousState[i])) {
          prevVal =
              String.format(
                  "[%s]",
                  ((Collection<AuditableModel>) previousState[i])
                      .stream()
                          .map(AuditableModel::getId)
                          .map(String::valueOf)
                          .collect(Collectors.joining(", ")));
        }
        if (CollectionUtils.isNotEmpty((Collection<Object>) currentState[i])) {
          newVal =
              String.format(
                  "[%s]",
                  ((Collection<AuditableModel>) currentState[i])
                      .stream()
                          .map(AuditableModel::getId)
                          .map(String::valueOf)
                          .collect(Collectors.joining(", ")));
        }
        logLine.setPreviousValue(prevVal);
        logLine.setNewValue(newVal);

      } else {
        logLine.setNewValue(format(property, currentState[i]));
        logLine.setPreviousValue(format(property, previousState[i]));
      }
      log.addGlobalTrackingLogLineListItem(logLine);
    }
    return true;
  }

  protected String format(Property property, Object value) {
    if (value == null) {
      return "";
    }
    if (value == Boolean.TRUE) {
      return "True";
    }
    if (value == Boolean.FALSE) {
      return "False";
    }
    switch (property.getType()) {
      case MANY_TO_ONE:
      case ONE_TO_ONE:
        try {
          return Mapper.of(property.getTarget()).get(value, property.getTargetName()).toString();
        } catch (Exception e) {
        }
        break;
      case ONE_TO_MANY:
      case MANY_TO_MANY:
        return "N/A";
      default:
        break;
    }
    if (value instanceof BigDecimal) {
      return ((BigDecimal) value).toPlainString();
    }
    return value.toString();
  }

  private void initGlobalTracker() {
    if (globalTracker.get() == null) {
      globalTracker.set(new ProjectAuditTracker());
      globalTracker.get().init();
    }
  }
}
