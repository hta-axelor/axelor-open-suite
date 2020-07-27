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
package com.axelor.apps.project.service;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectActivity;
import com.axelor.apps.project.db.Topic;
import com.axelor.apps.project.db.Wiki;
import com.axelor.apps.project.db.repo.ProjectActivityRepository;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.db.EntityHelper;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.db.mapper.PropertyType;
import com.axelor.i18n.I18n;
import com.axelor.rpc.ContextHandlerFactory;
import com.axelor.rpc.Resource;
import com.axelor.team.db.TeamTask;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ProjectActivityServiceImpl implements ProjectActivityService {

  protected ProjectActivityRepository projectActivityRepo;
  protected ProjectRepository ProjectRepo;

  private static final String[] ignoreFields = {"id", "version"};

  @Inject
  public ProjectActivityServiceImpl(
      ProjectActivityRepository projectActivityRepo, ProjectRepository ProjectRepo) {
    this.projectActivityRepo = projectActivityRepo;
    this.ProjectRepo = ProjectRepo;
  }

  @Transactional
  @Override
  public ProjectActivity getProjectActivity(Map<String, Object> dataMap, TeamTask task) {
    ProjectActivity projectActivity = getDefaultActivity(dataMap, task.getProject(), task);
    projectActivity.setObjectUpdated(task.getClass().getSimpleName());
    projectActivity.setRecordTitle(task.getName());
    return projectActivityRepo.save(projectActivity);
  }

  @Transactional
  @Override
  public ProjectActivity getProjectActivity(Map<String, Object> dataMap, Wiki wiki) {
    ProjectActivity projectActivity = getDefaultActivity(dataMap, wiki.getProject(), wiki);
    projectActivity.setObjectUpdated(wiki.getClass().getSimpleName());
    projectActivity.setRecordTitle(wiki.getTitle());
    return projectActivityRepo.save(projectActivity);
  }

  @Transactional
  @Override
  public ProjectActivity getProjectActivity(Map<String, Object> dataMap, Topic topic) {
    ProjectActivity projectActivity = getDefaultActivity(dataMap, topic.getProject(), topic);
    projectActivity.setObjectUpdated(topic.getClass().getSimpleName());
    projectActivity.setRecordTitle(topic.getTitle());
    return projectActivityRepo.save(projectActivity);
  }

  protected ProjectActivity getDefaultActivity(
      Map<String, Object> dataMap, Project project, Model model) {
    ProjectActivity projectActivity = new ProjectActivity();
    projectActivity.setActivity(getActivity(dataMap, model));
    if (model.getId() == null && project != null) {
      project = ProjectRepo.find(project.getId());
    }
    projectActivity.setProject(project);
    projectActivity.setUser(AuthUtils.getUser());
    projectActivity.setDoneOn(LocalDateTime.now());
    return projectActivity;
  }

  protected String getActivity(Map<String, Object> dataMap, Model model) {
    if (model.getId() == null) {
      return "Record Created";
    }
    String activity = "";
    Mapper mapper = Mapper.of(model.getClass());
    Map<String, Object> map = Mapper.toMap(model);
    for (Map.Entry<String, Object> me : dataMap.entrySet()) {
      String key = me.getKey();
      if (map.containsKey(key)) {
        Property property = mapper.getProperty(key);
        if (Arrays.asList(ignoreFields).contains(property.getName())) {
          continue;
        }
        Object oldValue = map.get(me.getKey());
        Object newValue = toProxy(property, me.getValue());
        if (!isEqual(oldValue, newValue)) {
          activity += getTitle(property) + " : ";
          activity += format(property, oldValue) + ">>";
          activity += format(property, newValue) + " ";
        }
      }
    }
    return activity;
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
        return Mapper.of(property.getTarget()).get(value, property.getTargetName()).toString();
      case ONE_TO_MANY:
      case MANY_TO_MANY:
      default:
        break;
    }
    if (value instanceof BigDecimal) {
      return ((BigDecimal) value).toPlainString();
    }
    return value.toString();
  }

  @SuppressWarnings("unchecked")
  protected Object toProxy(Property property, Object context) {
    if (context == null) {
      return null;
    }
    if (property.getType() == PropertyType.DECIMAL) {
      return new BigDecimal(context.toString());
    }
    if (property.getType() == PropertyType.MANY_TO_ONE) {
      return EntityHelper.getEntity(
          ContextHandlerFactory.newHandler(
                  property.getTarget(),
                  Resource.toMapCompact(
                      Mapper.toBean(property.getTarget(), (HashMap<String, Object>) context)))
              .getProxy());
    }
    if (property.getType() == PropertyType.MANY_TO_MANY) {
      return new HashSet<>();
    }
    return context;
  }

  protected boolean isEqual(Object o1, Object o2) {
    if (o1 == null && o2 == null) {
      return true;
    }
    if (o1 == null || o2 == null) {
      return false;
    }
    if (o1 instanceof BigDecimal) {
      return ((BigDecimal) o1).compareTo((BigDecimal) o2) == 0;
    }
    return o1.equals(o2);
  }

  protected String getTitle(Property property) {
    if (property.getTitle() != null) {
      return I18n.get(property.getTitle());
    }
    return I18n.get(property.getName());
  }
}
