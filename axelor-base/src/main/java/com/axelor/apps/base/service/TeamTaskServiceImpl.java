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
package com.axelor.apps.base.service;

import com.axelor.team.db.TeamTask;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.ArrayList;
import java.util.List;

public class TeamTaskServiceImpl implements TeamTaskService {

  protected TeamTaskRepository teamTaskRepo;

  @Inject
  public TeamTaskServiceImpl(TeamTaskRepository teamTaskRepo) {
    this.teamTaskRepo = teamTaskRepo;
  }

  @Override
  @Transactional
  public void updateNextTask(TeamTask teamTask) {
    TeamTask nextTeamTask = teamTask.getNextTeamTask();
    if (nextTeamTask != null) {
      updateModuleFields(teamTask, nextTeamTask);

      teamTaskRepo.save(nextTeamTask);
      updateNextTask(nextTeamTask);
    }
  }

  protected void updateModuleFields(TeamTask teamTask, TeamTask nextTeamTask) {
    nextTeamTask.setName(teamTask.getName());
    nextTeamTask.setTeam(teamTask.getTeam());
    nextTeamTask.setPriority(teamTask.getPriority());
    nextTeamTask.setStatus(teamTask.getStatus());
    nextTeamTask.setAssignedTo(teamTask.getAssignedTo());
    nextTeamTask.setDescription(teamTask.getDescription());
  }

  @Override
  @Transactional
  public void removeNextTasks(TeamTask teamTask) {
    List<TeamTask> teamTasks = getAllNextTasks(teamTask);
    teamTask.setNextTeamTask(null);
    teamTask.setHasDateOrFrequencyChanged(false);
    teamTaskRepo.save(teamTask);

    for (TeamTask teamTaskToRemove : teamTasks) {
      teamTaskRepo.remove(teamTaskToRemove);
    }
  }

  /** Returns next tasks from given {@link TeamTask}. */
  public List<TeamTask> getAllNextTasks(TeamTask teamTask) {
    List<TeamTask> teamTasks = new ArrayList<>();

    TeamTask current = teamTask;
    while (current.getNextTeamTask() != null) {
      current = current.getNextTeamTask();
      teamTasks.add(current);
    }

    for (TeamTask tt : teamTasks) {
      tt.setNextTeamTask(null);
      teamTaskRepo.save(tt);
    }

    return teamTasks;
  }
}
