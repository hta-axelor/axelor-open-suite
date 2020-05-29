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
import com.axelor.auth.AuthUtils;
import com.axelor.team.db.TeamTask;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.LocalDateTime;

public class ProjectActivityServiceImpl implements ProjectActivityService {

  protected ProjectActivityRepository projectActivityRepo;

  @Inject
  public ProjectActivityServiceImpl(ProjectActivityRepository projectActivityRepo) {
    this.projectActivityRepo = projectActivityRepo;
  }

  // TODO: Have to add activity
  // Have to add functionality for create,delete
  @Transactional
  @Override
  public ProjectActivity getProjectActivity(Project project) {
    ProjectActivity projectActivity = getDefaultActivity(project);
    projectActivity.setObjectUpdated("Project");
    projectActivity.setRecordTitle(project.getName());
    return projectActivityRepo.save(projectActivity);
  }

  @Transactional
  @Override
  public ProjectActivity getProjectActivity(TeamTask task) {
    ProjectActivity projectActivity = getDefaultActivity(task.getProject());
    projectActivity.setObjectUpdated("Task");
    projectActivity.setRecordTitle(task.getName());
    return projectActivityRepo.save(projectActivity);
  }

  @Override
  public ProjectActivity getProjectActivity(Wiki wiki) {
    ProjectActivity projectActivity = getDefaultActivity(wiki.getProject());
    projectActivity.setObjectUpdated("Wiki");
    projectActivity.setRecordTitle(wiki.getTitle());
    return projectActivityRepo.save(projectActivity);
  }

  @Override
  public ProjectActivity getProjectActivity(Topic topic) {
    ProjectActivity projectActivity = getDefaultActivity(topic.getProject());
    projectActivity.setObjectUpdated("Topic");
    projectActivity.setRecordTitle(topic.getTitle());
    return projectActivityRepo.save(projectActivity);
  }

  private ProjectActivity getDefaultActivity(Project project) {
    ProjectActivity projectActivity = new ProjectActivity();
    projectActivity.setProject(project);
    projectActivity.setUser(AuthUtils.getUser());
    projectActivity.setDoneOn(LocalDateTime.now());
    return projectActivity;
  }
}
