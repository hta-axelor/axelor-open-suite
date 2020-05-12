package com.axelor.apps.businessproject.db.repo;

import com.axelor.apps.base.db.AppProject;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.repo.SequenceRepository;
import com.axelor.apps.base.service.administration.SequenceService;
import com.axelor.apps.businessproject.exception.IExceptionMessage;
import com.axelor.apps.project.db.Project;
import com.axelor.apps.project.db.repo.ProjectManagementRepository;
import com.axelor.apps.project.service.app.AppProjectService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import javax.persistence.PersistenceException;

public class ProjectBusinessProjectRepository extends ProjectManagementRepository {

  @Override
  public Project save(Project project) {
    try {
      AppProject appProject = Beans.get(AppProjectService.class).getAppProject();

      if (Strings.isNullOrEmpty(project.getCode()) && appProject.getGenerateProjectSequence()) {
        Company company = project.getCompany();
        String seq =
            Beans.get(SequenceService.class)
                .getSequenceNumber(SequenceRepository.PROJECT_SEQUENCE, company);

        if (seq == null) {
          throw new AxelorException(
              company,
              TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
              I18n.get(IExceptionMessage.PROJECT_SEQUENCE_ERROR),
              company.getName());
        }

        project.setCode(seq);
      }
    } catch (AxelorException e) {
      throw new PersistenceException(e.getLocalizedMessage());
    }
    return super.save(project);
  }
}
