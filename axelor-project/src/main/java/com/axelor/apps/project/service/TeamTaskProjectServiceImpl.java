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

import com.axelor.apps.base.service.TeamTaskServiceImpl;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectStatus;
import com.axelor.auth.db.User;
import com.axelor.common.ObjectUtils;
import com.axelor.team.db.TeamTask;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.inject.Inject;
import java.util.Optional;
import java.util.Set;

public class TeamTaskProjectServiceImpl extends TeamTaskServiceImpl
    implements TeamTaskProjectService {

  @Inject
  public TeamTaskProjectServiceImpl(TeamTaskRepository teamTaskRepo) {
    super(teamTaskRepo);
  }

  @Override
  public TeamTask create(String subject, Project project, User assignedTo) {
    TeamTask task = new TeamTask();
    task.setName(subject);
    task.setAssignedTo(assignedTo);
    task.setStatus("new");
    task.setPriority("normal");
    project.addTeamTaskListItem(task);
    return task;
  }

  @Override
  protected void updateModuleFields(TeamTask teamTask, TeamTask nextTeamTask) {
    super.updateModuleFields(teamTask, nextTeamTask);

    // Module 'project' fields
    nextTeamTask.setFullName(teamTask.getFullName());
    nextTeamTask.setProject(teamTask.getProject());
    nextTeamTask.setTeamTaskCategory(teamTask.getTeamTaskCategory());
    nextTeamTask.setProgressSelect(0);

    teamTask.getMembersUserSet().forEach(nextTeamTask::addMembersUserSetItem);

    nextTeamTask.setParentTask(teamTask.getParentTask());
    nextTeamTask.setProduct(teamTask.getProduct());
    nextTeamTask.setUnit(teamTask.getUnit());
    nextTeamTask.setQuantity(teamTask.getQuantity());
    nextTeamTask.setUnitPrice(teamTask.getUnitPrice());
    nextTeamTask.setBudgetedTime(teamTask.getBudgetedTime());
    nextTeamTask.setCurrency(teamTask.getCurrency());
  }

  @Override
  public ProjectStatus getProjectStatus(Project project) {
    Set<ProjectStatus> teamTaskStatusSet = project.getTeamTaskStatusSet();
    if (ObjectUtils.isEmpty(teamTaskStatusSet)) {
      return null;
    }
    Optional<ProjectStatus> projectStatus =
        teamTaskStatusSet.stream().filter(status -> status.getIsDefaultCompleted()).findFirst();
    return projectStatus.isPresent() ? projectStatus.get() : null;
  }
}
