package com.axelor.apps.project.event;

import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectRepository;
import com.axelor.apps.project.service.ProjectActivityService;
import com.axelor.event.Observes;
import com.axelor.events.PreRequest;
import com.axelor.events.RequestEvent;
import com.axelor.events.qualifiers.EntityType;
import com.google.inject.Inject;
import javax.inject.Named;

public class ProjectActivityEvent {

  private ProjectActivityService projectActivityService;
  private ProjectRepository projectRepo;

  @Inject
  public ProjectActivityEvent(
      ProjectActivityService projectActivityService, ProjectRepository projectRepo) {
    this.projectActivityService = projectActivityService;
    this.projectRepo = projectRepo;
  }

  public void onSaveProject(
      @Observes @Named(RequestEvent.SAVE) @EntityType(Project.class) PreRequest event) {
    Object id = event.getRequest().getData().get("id");
    if (id != null) {
      Project project = projectRepo.find(Long.parseLong(id.toString()));
      projectActivityService.getProjectActivity(project);
    }
  }
}
