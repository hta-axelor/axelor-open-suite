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
package com.axelor.admin.auth;

import com.axelor.apps.admin.db.GlobalTrackingLog;
import com.axelor.auth.db.User;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Transaction;

public class ProjectAuditTracker extends GlobalAuditTracker {

  public static final ThreadLocal<List<GlobalTrackingLog>> PROJECT_LOGS = new ThreadLocal<>();

  @Override
  public void onComplete(Transaction tx, User user) {
    PROJECT_LOGS.remove();
  }

  @Override
  protected void init() {
    PROJECT_LOGS.set(new ArrayList<>());
  }

  @Override
  protected void clear() {
    PROJECT_LOGS.remove();
  }

  @Override
  protected void addLog(GlobalTrackingLog log) {
    if (PROJECT_LOGS.get() == null) {
      this.init();
    }
    PROJECT_LOGS.get().add(log);
  }

  public static void clearProjectLogs() {
    PROJECT_LOGS.remove();
  }
}
