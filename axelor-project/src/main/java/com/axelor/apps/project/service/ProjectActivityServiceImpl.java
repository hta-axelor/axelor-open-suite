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

import com.axelor.admin.auth.ProjectAuditTracker;
import com.axelor.apps.admin.db.GlobalTrackingLog;
import com.axelor.apps.admin.db.GlobalTrackingLogLine;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectActivity;
import com.axelor.apps.project.db.Topic;
import com.axelor.apps.project.db.Wiki;
import com.axelor.apps.project.db.repo.ProjectActivityRepository;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.common.StringUtils;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.team.db.TeamTask;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProjectActivityServiceImpl implements ProjectActivityService {

  protected ProjectActivityRepository projectActivityRepo;
  protected ProjectRepository ProjectRepo;

  @Inject
  public ProjectActivityServiceImpl(
      ProjectActivityRepository projectActivityRepo, ProjectRepository ProjectRepo) {
    this.projectActivityRepo = projectActivityRepo;
    this.ProjectRepo = ProjectRepo;
  }

  @Transactional
  @Override
  public ProjectActivity getProjectActivity(TeamTask task) {
    ProjectActivity projectActivity = getDefaultActivity(task.getProject(), task);
    projectActivity.setObjectUpdated(task.getClass().getSimpleName());
    projectActivity.setRecordTitle(task.getName());
    return projectActivityRepo.save(projectActivity);
  }

  @Transactional
  @Override
  public ProjectActivity getProjectActivity(Wiki wiki) {
    ProjectActivity projectActivity = getDefaultActivity(wiki.getProject(), wiki);
    projectActivity.setObjectUpdated(wiki.getClass().getSimpleName());
    projectActivity.setRecordTitle(wiki.getTitle());
    return projectActivityRepo.save(projectActivity);
  }

  @Transactional
  @Override
  public ProjectActivity getProjectActivity(Topic topic) {
    ProjectActivity projectActivity = getDefaultActivity(topic.getProject(), topic);
    projectActivity.setObjectUpdated(topic.getClass().getSimpleName());
    projectActivity.setRecordTitle(topic.getTitle());
    return projectActivityRepo.save(projectActivity);
  }

  protected ProjectActivity getDefaultActivity(Project project, Model model) {
    ProjectActivity projectActivity = new ProjectActivity();
    projectActivity.setActivity(getActivity(model));
    if (model.getId() == null && project != null) {
      project = ProjectRepo.find(project.getId());
    }
    projectActivity.setProject(project);
    projectActivity.setUser(AuthUtils.getUser());
    projectActivity.setDoneOn(LocalDateTime.now());
    return projectActivity;
  }

  protected String getActivity(Model model) {
    Mapper mapper = Mapper.of(model.getClass());
    String activity = "";
    if (model.getId() == null) {
      activity = "Record Created";
    } else {
      Set<GlobalTrackingLog> logs =
          new HashSet<GlobalTrackingLog>(ProjectAuditTracker.PROJECT_LOGS.get());
      for (GlobalTrackingLog log : logs) {
        List<GlobalTrackingLogLine> globalTrackingList = log.getGlobalTrackingLogLineList();
        for (GlobalTrackingLogLine line : globalTrackingList) {
          Property property = mapper.getProperty(line.getMetaFieldName());
          if (property != null) {
            activity += property.getTitle() + " : ";
            activity += getKeyValue(line.getPreviousValue(), property.getTargetName()) + ">>";
            activity += getKeyValue(line.getNewValue(), property.getTargetName()) + " ";
          }
        }
      }
    }
    ProjectAuditTracker.clearProjectLogs();
    return activity;
  }

  protected String getKeyValue(String complete, String toFind) {
    if (StringUtils.notBlank(complete) && StringUtils.notBlank(toFind)) {
      String pairs[] = complete.split(",");
      for (String pair : pairs) {
        String[] keyValue = pair.split("=");
        if (toFind.trim().equals(keyValue[0])) {
          return keyValue[1].trim();
        }
      }
    }
    return complete;
  }
}
