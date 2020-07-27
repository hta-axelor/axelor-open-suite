package com.axelor.apps.project.observer;

import com.axelor.apps.project.db.Topic;
import com.axelor.apps.project.db.Wiki;
import com.axelor.apps.project.service.ProjectActivityService;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.event.Observes;
import com.axelor.events.PreRequest;
import com.axelor.events.RequestEvent;
import com.axelor.events.qualifiers.EntityType;
import com.axelor.team.db.TeamTask;
import com.google.inject.Inject;
import java.util.Map;
import javax.inject.Named;

public class ProjectActivityObserver {

  private ProjectActivityService projectActivityService;

  @Inject
  public ProjectActivityObserver(ProjectActivityService projectActivityService) {
    this.projectActivityService = projectActivityService;
  }

  public void onSaveTask(
      @Observes @Named(RequestEvent.SAVE) @EntityType(TeamTask.class) PreRequest event) {
    Map<String, Object> dataMap = event.getRequest().getData();
    if (dataMap != null) {
      TeamTask task = getRecord(dataMap, TeamTask.class);
      projectActivityService.createProjectActivity(dataMap, task);
    }
  }

  public void onSaveWiki(
      @Observes @Named(RequestEvent.SAVE) @EntityType(Wiki.class) PreRequest event) {
    Map<String, Object> dataMap = event.getRequest().getData();
    if (dataMap != null) {
      Wiki wiki = getRecord(dataMap, Wiki.class);
      projectActivityService.createProjectActivity(dataMap, wiki);
    }
  }

  public void onSaveTopic(
      @Observes @Named(RequestEvent.SAVE) @EntityType(Topic.class) PreRequest event) {
    Map<String, Object> dataMap = event.getRequest().getData();
    if (dataMap != null) {
      Topic topic = getRecord(dataMap, Topic.class);
      projectActivityService.createProjectActivity(dataMap, topic);
    }
  }

  private <T extends Model> T getRecord(Map<String, Object> dataMap, Class<T> klass) {
    Object id = dataMap.get("id");
    return dataMap.get("id") != null
        ? JPA.find(klass, Long.parseLong(id.toString()))
        : Mapper.toBean(klass, dataMap);
  }
}
