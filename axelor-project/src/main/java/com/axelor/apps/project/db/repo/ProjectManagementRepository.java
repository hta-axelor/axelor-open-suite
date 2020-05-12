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
package com.axelor.apps.project.db.repo;

import com.axelor.apps.project.db.Project;
import com.axelor.team.db.Team;
import com.google.common.base.Strings;

public class ProjectManagementRepository extends ProjectRepository {

  private void setAllProjectFullName(Project project) {
    String projectCode =
        (Strings.isNullOrEmpty(project.getCode())) ? "" : project.getCode() + " - ";
    project.setFullName(projectCode + project.getName());
  }

  public static void setAllProjectMembersUserSet(Project project) {
    if (project.getExtendsMembersFromParent() && !project.getSynchronize()) {
      project.getParentProject().getMembersUserSet().forEach(project.getMembersUserSet()::add);
    }
  }

  @Override
  public Project save(Project project) {

    ProjectManagementRepository.setAllProjectMembersUserSet(project);

    if (project.getSynchronize()) {
      Team team = project.getTeam();
      if (team != null) {
        team.clearMembers();
        project.getMembersUserSet().forEach(team::addMember);
      }
    }
    setAllProjectFullName(project);

    return super.save(project);
  }

  @Override
  public Project copy(Project entity, boolean deep) {
    Project project = super.copy(entity, false);
    project.setStatusSelect(STATE_NEW);
    return project;
  }
}
