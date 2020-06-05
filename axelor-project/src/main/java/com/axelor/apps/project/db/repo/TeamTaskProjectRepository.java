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

import com.axelor.apps.base.db.Frequency;
import com.axelor.apps.base.db.repo.TeamTaskBaseRepository;
import com.axelor.apps.base.service.TeamTaskService;
import com.axelor.team.db.TeamTask;
import com.google.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TeamTaskProjectRepository extends TeamTaskBaseRepository {

  private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Inject protected TeamTaskService teamTaskService;

  @Override
  public TeamTask save(TeamTask teamTask) {
    List<String> composedNames = new ArrayList<>();
    if (teamTask.getId() != null) {
      composedNames.add("#" + teamTask.getId());
    }
    composedNames.add(teamTask.getName());
    teamTask.setFullName(String.join(" ", composedNames));

    if (teamTask.getDoApplyToAllNextTasks()
        && teamTask.getNextTeamTask() != null
        && teamTask.getHasDateOrFrequencyChanged()) {
      // remove next tasks
      teamTaskService.removeNextTasks(teamTask);

      // regenerate new tasks
      teamTask.setIsFirst(true);
    }

    Frequency frequency = teamTask.getFrequency();
    if (frequency != null && teamTask.getIsFirst() && teamTask.getNextTeamTask() == null) {
      teamTaskService.generateTasks(teamTask, frequency);
    }

    if (teamTask.getDoApplyToAllNextTasks()) {
      teamTaskService.updateNextTask(teamTask);
    }

    teamTask.setDoApplyToAllNextTasks(false);
    teamTask.setHasDateOrFrequencyChanged(false);
    return teamTask;
  }

  @Override
  public Map<String, Object> validate(Map<String, Object> json, Map<String, Object> context) {

    logger.debug("Validate team task:{}", json);

    logger.debug(
        "Planned progress:{}, ProgressSelect: {}, DurationHours: {}",
        json.get("plannedProgress"),
        json.get("progressSelect"),
        json.get("durationHours"));

    if (json.get("id") != null) {

      TeamTask savedTask = find(Long.parseLong(json.get("id").toString()));
      if (json.get("plannedProgress") != null) {
        BigDecimal plannedProgress = new BigDecimal(json.get("plannedProgress").toString());
        if (plannedProgress != null
            && savedTask.getPlannedProgress().intValue() != plannedProgress.intValue()) {
          logger.debug(
              "Updating progressSelect: {}", ((int) (plannedProgress.intValue() * 0.10)) * 10);
          json.put("progressSelect", ((int) (plannedProgress.intValue() * 0.10)) * 10);
        }
      } else if (json.get("progressSelect") != null) {
        Integer progressSelect = new Integer(json.get("progressSelect").toString());
        logger.debug("Updating plannedProgress: {}", progressSelect);
        json.put("plannedProgress", new BigDecimal(progressSelect));
      }
    } else {

      if (json.get("progressSelect") != null) {
        Integer progressSelect = new Integer(json.get("progressSelect").toString());
        json.put("plannedProgress", new BigDecimal(progressSelect));
      }
    }

    return super.validate(json, context);
  }

  @Override
  public TeamTask copy(TeamTask entity, boolean deep) {
    TeamTask task = super.copy(entity, deep);
    task.setProgressSelect(null);
    task.setMetaFile(null);
    return task;
  }
}
