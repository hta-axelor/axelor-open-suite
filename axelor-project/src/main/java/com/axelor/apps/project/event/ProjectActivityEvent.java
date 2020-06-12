package com.axelor.apps.project.event;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.Topic;
import com.axelor.apps.project.db.Wiki;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.db.repo.TopicRepository;
import com.axelor.apps.project.db.repo.WikiRepository;
import com.axelor.apps.project.service.ProjectActivityService;
import com.axelor.event.Observes;
import com.axelor.events.PostRequest;
import com.axelor.events.RequestEvent;
import com.axelor.events.qualifiers.EntityType;
import com.axelor.team.db.TeamTask;
import com.axelor.team.db.repo.TeamTaskRepository;
import com.google.inject.Inject;
import java.util.Map;
import javax.inject.Named;

public class ProjectActivityEvent {

  private ProjectActivityService projectActivityService;
  private ProjectRepository projectRepo;
  private TeamTaskRepository teamTaskRepo;
  private WikiRepository wikiRepo;
  private TopicRepository topicRepo;

  @Inject
  public ProjectActivityEvent(
      ProjectActivityService projectActivityService,
      ProjectRepository projectRepo,
      TeamTaskRepository teamTaskRepo,
      WikiRepository wikiRepo,
      TopicRepository topicRepo) {
    this.projectActivityService = projectActivityService;
    this.projectRepo = projectRepo;
    this.teamTaskRepo = teamTaskRepo;
    this.wikiRepo = wikiRepo;
    this.topicRepo = topicRepo;
  }

  public void onSaveProject(
      @Observes @Named(RequestEvent.SAVE) @EntityType(Project.class) PostRequest event) {
    Map<String, Object> dataMap = event.getRequest().getData();
    if (dataMap != null) {
      Object id = dataMap.get("id");
      if (id != null) {
        Project project = projectRepo.find(Long.parseLong(id.toString()));
        projectActivityService.getProjectActivity(project);
      }
    }
  }

  public void onSaveTask(
      @Observes @Named(RequestEvent.SAVE) @EntityType(TeamTask.class) PostRequest event) {
    Map<String, Object> dataMap = event.getRequest().getData();
    if (dataMap != null) {
      Object id = dataMap.get("id");
      if (id != null) {
        TeamTask task = teamTaskRepo.find(Long.parseLong(id.toString()));
        projectActivityService.getProjectActivity(task);
      }
    }
  }

  public void onSaveWiki(
      @Observes @Named(RequestEvent.SAVE) @EntityType(Wiki.class) PostRequest event) {
    Map<String, Object> dataMap = event.getRequest().getData();
    if (dataMap != null) {
      Object id = dataMap.get("id");
      if (id != null) {
        Wiki wiki = wikiRepo.find(Long.parseLong(id.toString()));
        projectActivityService.getProjectActivity(wiki);
      }
    }
  }

  public void onSaveTopic(
      @Observes @Named(RequestEvent.SAVE) @EntityType(Topic.class) PostRequest event) {
    Map<String, Object> dataMap = event.getRequest().getData();
    if (dataMap != null) {
      Object id = dataMap.get("id");
      if (id != null) {
        Topic topic = topicRepo.find(Long.parseLong(id.toString()));
        projectActivityService.getProjectActivity(topic);
      }
    }
  }
}
