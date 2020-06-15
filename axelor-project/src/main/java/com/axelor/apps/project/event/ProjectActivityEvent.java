package com.axelor.apps.project.event;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.Topic;
import com.axelor.apps.project.db.Wiki;
import com.axelor.apps.project.service.ProjectActivityService;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.event.Observes;
import com.axelor.events.PostRequest;
import com.axelor.events.RequestEvent;
import com.axelor.events.qualifiers.EntityType;
import com.axelor.team.db.TeamTask;
import com.google.inject.Inject;
import java.util.Map;
import javax.inject.Named;

public class ProjectActivityEvent {

  private ProjectActivityService projectActivityService;

  @Inject
  public ProjectActivityEvent(ProjectActivityService projectActivityService) {
    this.projectActivityService = projectActivityService;
  }

  public void onSaveProject(
      @Observes @Named(RequestEvent.SAVE) @EntityType(Project.class) PostRequest event) {
    Project project = getRecord(event, Project.class);
    if (project != null) {
      projectActivityService.getProjectActivity(project);
    }
  }

  public void onSaveTask(
      @Observes @Named(RequestEvent.SAVE) @EntityType(TeamTask.class) PostRequest event) {
    TeamTask task = getRecord(event, TeamTask.class);
    if (task != null) {
      projectActivityService.getProjectActivity(task);
    }
  }

  public void onSaveWiki(
      @Observes @Named(RequestEvent.SAVE) @EntityType(Wiki.class) PostRequest event) {
    Wiki wiki = getRecord(event, Wiki.class);
    if (wiki != null) {
      projectActivityService.getProjectActivity(wiki);
    }
  }

  public void onSaveTopic(
      @Observes @Named(RequestEvent.SAVE) @EntityType(Topic.class) PostRequest event) {
    Topic topic = getRecord(event, Topic.class);
    if (topic != null) {
      projectActivityService.getProjectActivity(topic);
    }
  }

  private <T extends Model> T getRecord(RequestEvent event, Class<T> klass) {
    Map<String, Object> dataMap = event.getRequest().getData();
    if (dataMap != null) {
      Object id = dataMap.get("id");
      if (id != null) {
        return JPA.find(klass, Long.parseLong(id.toString()));
      }
    }
    return null;
  }
}
