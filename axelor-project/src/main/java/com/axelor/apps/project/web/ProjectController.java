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
import com.axelor.apps.project.service.ProjectService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.inject.Singleton;
import java.util.Map;

@Singleton
public class ProjectController {

  public void getMyOpenTasks(ActionRequest request, ActionResponse response) {
    Project project = request.getContext().asType(Project.class);
    Map<String, Object> view =
        Beans.get(ProjectService.class)
            .getTaskView(
                "My open tasks",
                "self.assignedTo = :__user__ AND self.taskStatus.isCompleted = false AND self.typeSelect = '"
                    + TeamTaskRepository.TYPE_TASK
                    + "'",
                project);
    response.setView(view);
  }

  public void getMyTasks(ActionRequest request, ActionResponse response) {
    Project project = request.getContext().asType(Project.class);
    Map<String, Object> view =
        Beans.get(ProjectService.class)
            .getTaskView(
                "My tasks",
                "self.assignedTo = :__user__ AND self.typeSelect = '"
                    + TeamTaskRepository.TYPE_TASK
                    + "'",
                project);
    response.setView(view);
  }

  public void getAllOpenTasks(ActionRequest request, ActionResponse response) {
    Project project = request.getContext().asType(Project.class);
    Map<String, Object> view =
        Beans.get(ProjectService.class)
            .getTaskView(
                "All open tasks",
                "self.taskStatus.isCompleted = false AND self.typeSelect = '"
                    + TeamTaskRepository.TYPE_TASK
                    + "'",
                project);
    response.setView(view);
  }

  public void getAllTasks(ActionRequest request, ActionResponse response) {
    Project project = request.getContext().asType(Project.class);
    Map<String, Object> view =
        Beans.get(ProjectService.class)
            .getTaskView(
                "All tasks", "self.typeSelect = '" + TeamTaskRepository.TYPE_TASK + "'", project);
    response.setView(view);
  }
}
