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
package com.axelor.apps.project.web;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.ProjectStatus;
import com.axelor.apps.project.db.repo.ProjectStatusRepository;
import com.axelor.apps.project.service.ProjectService;
import com.axelor.common.ObjectUtils;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.meta.schema.actions.ActionView.ActionViewBuilder;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.team.db.TeamTask;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class ProjectController {

  public void importMembers(ActionRequest request, ActionResponse response) {
    Project project = request.getContext().asType(Project.class);
    if (project.getTeam() != null) {
      project.getTeam().getMembers().forEach(project::addMembersUserSetItem);
      response.setValue("membersUserSet", project.getMembersUserSet());
    }
  }

  public void getMyOpenTasks(ActionRequest request, ActionResponse response) {
    Project project = request.getContext().asType(Project.class);
    Map<String, Object> context = new HashMap<>();
    context.put("_project", project);
    context.put("typeSelect", TeamTaskRepository.TYPE_TASK);
    Map<String, Object> view =
        Beans.get(ProjectService.class)
            .getTaskView(
                "My open tasks",
                "self.assignedTo = :__user__ AND self.taskStatus.isCompleted = false AND self.typeSelect = :typeSelect AND self.project = :_project",
                context);
    response.setView(view);
  }

  public void getMyTasks(ActionRequest request, ActionResponse response) {
    Project project = request.getContext().asType(Project.class);
    Map<String, Object> context = new HashMap<>();
    context.put("_project", project);
    context.put("typeSelect", TeamTaskRepository.TYPE_TASK);
    Map<String, Object> view =
        Beans.get(ProjectService.class)
            .getTaskView(
                "My tasks",
                "self.assignedTo = :__user__ AND self.typeSelect = :typeSelect AND self.project = :_project",
                context);
    response.setView(view);
  }

  public void getAllOpenTasks(ActionRequest request, ActionResponse response) {
    Project project = request.getContext().asType(Project.class);
    Map<String, Object> context = new HashMap<>();
    context.put("_project", project);
    context.put("typeSelect", TeamTaskRepository.TYPE_TASK);
    Map<String, Object> view =
        Beans.get(ProjectService.class)
            .getTaskView(
                "All open tasks",
                "self.taskStatus.isCompleted = false AND self.typeSelect = :typeSelect AND self.project = :_project",
                context);
    response.setView(view);
  }

  public void getAllTasks(ActionRequest request, ActionResponse response) {
    Project project = request.getContext().asType(Project.class);
    Map<String, Object> context = new HashMap<>();
    context.put("_project", project);
    context.put("typeSelect", TeamTaskRepository.TYPE_TASK);
    Map<String, Object> view =
        Beans.get(ProjectService.class)
            .getTaskView(
                "All tasks", "self.typeSelect = :typeSelect AND self.project = :_project", context);
    response.setView(view);
  }

  public void perStatusKanban(ActionRequest request, ActionResponse response) {
    Project project = request.getContext().asType(Project.class);
    Map<String, Object> context = new HashMap<>();
    context.put("_project", project);
    context.put("typeSelect", TeamTaskRepository.TYPE_TASK);

    String statusColumnsTobeExcluded =
        Beans.get(ProjectStatusRepository.class)
            .all()
            .filter("self not in :allowedTeamTaskStatus")
            .bind("allowedTeamTaskStatus", project.getTeamTaskStatusSet())
            .fetchStream()
            .map(ProjectStatus::getId)
            .map(String::valueOf)
            .collect(Collectors.joining(","));

    ActionViewBuilder builder =
        ActionView.define(I18n.get("All tasks"))
            .model(TeamTask.class.getName())
            .add("kanban", "team-task-kanban")
            .add("grid", "team-task-grid")
            .add("form", "team-task-form")
            .domain("self.typeSelect = :typeSelect AND self.project = :_project")
            .param("kanban-hide-columns", statusColumnsTobeExcluded);

    if (ObjectUtils.notEmpty(context)) {
      context.forEach(builder::context);
    }
    response.setView(builder.map());
  }
}
